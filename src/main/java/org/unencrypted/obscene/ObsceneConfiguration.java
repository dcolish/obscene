package org.unencrypted.obscene;


import com.google.common.net.HostAndPort;
import org.apache.commons.configuration.Configuration;

import java.io.File;

public class ObsceneConfiguration {
    public final File indexPath;
    public final int indexWriterQueueSize;
    public final HostAndPort hostAndPort;

    public ObsceneConfiguration(Configuration configuration) {
        indexPath = new File(configuration.getString("obscene.indexer.path", "/tmp/obscene"));
        indexWriterQueueSize = configuration.getInt("obscene.indexer.queueSize", 100);
        hostAndPort = HostAndPort.fromString(
                configuration.getString("obscene.server.hostAndPort", "localhost:8080"));
    }
}
