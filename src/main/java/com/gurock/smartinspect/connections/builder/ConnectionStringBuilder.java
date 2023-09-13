package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.ConnectionsBuilder;

public class ConnectionStringBuilder {
    ConnectionsBuilder cb = new ConnectionsBuilder();

    public PipeProtocolConnectionStringBuilder addPipeProtocol() {
        cb.beginProtocol("pipe");
        return new PipeProtocolConnectionStringBuilder(this);
    }

    public FileProtocolConnectionStringBuilder addFileProtocol() {
        cb.beginProtocol("file");
        return new FileProtocolConnectionStringBuilder(this);
    }

    public MemoryProtocolConnectionStringBuilder addMemoryProtocol() {
        cb.beginProtocol("mem");
        return new MemoryProtocolConnectionStringBuilder(this);
    }

    public TcpProtocolConnectionStringBuilder addTcpProtocol() {
        cb.beginProtocol("tcp");
        return new TcpProtocolConnectionStringBuilder(this);
    }

    public TextProtocolConnectionStringBuilder addTextProtocol() {
        cb.beginProtocol("text");
        return new TextProtocolConnectionStringBuilder(this);
    }

    public String build() {
        return cb.getConnections();
    }
}
