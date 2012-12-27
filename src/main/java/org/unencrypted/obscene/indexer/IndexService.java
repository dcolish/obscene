package org.unencrypted.obscene.indexer;

import com.google.common.util.concurrent.AbstractIdleService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.unencrypted.obscene.ObsceneConfiguration;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class IndexService extends AbstractIdleService {
    private BlockingQueue<Document> writeQueue;
    private final SearcherManager manager;
    private final ObsceneIndexWriter writer;
    private Logger log = LogManager.getLogger(IndexService.class);

    public IndexService(ObsceneConfiguration configuration) throws IOException {
        Directory indexDirectory;
        try {
            indexDirectory = new MMapDirectory(configuration.indexPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory", e);
        }
        writeQueue  = new LinkedBlockingQueue<Document>(configuration.indexWriterQueueSize);
        writer = new ObsceneIndexWriter(indexDirectory, writeQueue);
        manager = new SearcherManager(indexDirectory, new SearcherFactory());
    }

    public BlockingQueue<Document> getWriteQueue() {
        return writeQueue;
    }

    public SearcherManager getManager() {
        return manager;
    }

    @Override
    protected void startUp() throws Exception {
        log.info("Starting writer thread");
        writer.startAndWait();
        log.info("Writer thread started");
        log.info("Starting Reader Pool");
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Stopping writer thread");
        writer.stopAndWait();
        log.info("Writer thread stopped");
    }
}
