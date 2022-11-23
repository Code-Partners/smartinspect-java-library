package com.gurock.smartinspect.connections.builder;

public class FileProtocolConnectionStringBuilder extends ProtocolConnectionStringBuilder {
    public FileProtocolConnectionStringBuilder(ConnectionStringBuilder parent) {
        super(parent);
    }

    public FileProtocolConnectionStringBuilder setFilename(String filename) {
        parent.cb.addOption("filename", filename);

        return this;
    }
}
