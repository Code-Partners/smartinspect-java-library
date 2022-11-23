package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.connections.ConnectionsBuilder;

public class ConnectionStringBuilder {

    ConnectionsBuilder cb = new ConnectionsBuilder();

    public CloudProtocolConnectionStringBuilder addCloudProtocol() {
        cb.beginProtocol("cloud");

        return new CloudProtocolConnectionStringBuilder(this);
    }

    public FileProtocolConnectionStringBuilder addFileProtocol() {
        cb.beginProtocol("file");

        return new FileProtocolConnectionStringBuilder(this);
    }

    public String build() {
        return cb.getConnections();
    }
}
