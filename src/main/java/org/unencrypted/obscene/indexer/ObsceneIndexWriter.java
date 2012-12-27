package org.unencrypted.obscene.indexer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ObsceneIndexWriter extends AbstractExecutionThreadService {
    private final Logger log = LogManager.getLogger(ObsceneIndexWriter.class);
    private final int COMMIT_SIZE = 10;
    private final BlockingQueue<Document> writeQueue;
    private final IndexWriter writer;
    private final Document POISON = new Document();

    public ObsceneIndexWriter(Directory indexDirectory, BlockingQueue<Document> writeQueue) throws IOException {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
        IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_40, analyzer);
        writerConfig.setMaxBufferedDocs(COMMIT_SIZE);
        writer = new IndexWriter(indexDirectory, writerConfig);
        this.writeQueue = writeQueue;
    }

    @Override
    protected void triggerShutdown() {
        try {
            writeQueue.put(POISON);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() throws InterruptedException {
        log.info("Running index writer thread");
        while(isRunning()) {
            try {
                Document doc = writeQueue.take();
                if (doc == POISON) {
                    log.info("POISON found, breaking");
                    break;
                }
                log.debug("Indexing document");
                //TODO:dc: maybe validate document lightly?
                writer.addDocument(doc);
            } catch (IOException e) {
                log.error("Could not add document to index", e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Exiting");
        try {
            log.info("Flushing work queue");
            List<Document> finalDrain = Lists.newArrayList();
            writeQueue.drainTo(finalDrain);
            writer.addDocuments(finalDrain);
        } catch (IOException e) {
            log.error("Could not flush queue during shutdown", e);
            throw new RuntimeException(e);
        }

        try {
            writer.close(true);
        } catch (IOException e) {
            throw new RuntimeException("IO error while closing index", e);
        }
    }
}
