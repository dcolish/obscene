package org.unencrypted.obscene.indexer;

import com.google.common.util.concurrent.AbstractIdleService;
import com.sun.jersey.spi.inject.Injectable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.SimpleFSDirectory;
import org.unencrypted.obscene.ObsceneConfiguration;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


public class IndexService extends AbstractIdleService {
    private BlockingQueue<Document> writeQueue;
    private final ExecutorService readerPool = Executors.newFixedThreadPool(10);
    private final SimpleFSDirectory indexDirectory;
    private final ObsceneIndexWriter writer;
    private Logger log = LogManager.getLogger(IndexService.class);

    public IndexService(ObsceneConfiguration configuration) throws IOException {
        try {
            this.indexDirectory = new SimpleFSDirectory(configuration.indexPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory", e);
        }
        writeQueue  = new LinkedBlockingQueue<Document>(configuration.indexWriterQueueSize);
        writer = new ObsceneIndexWriter(indexDirectory, writeQueue);
    }

    public BlockingQueue<Document> getWriteQueue() {
        return writeQueue;
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
