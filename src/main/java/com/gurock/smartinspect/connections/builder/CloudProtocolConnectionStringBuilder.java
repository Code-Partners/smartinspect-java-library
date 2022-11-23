package com.gurock.smartinspect.connections.builder;

public class CloudProtocolConnectionStringBuilder extends ProtocolConnectionStringBuilder {
    public CloudProtocolConnectionStringBuilder(ConnectionStringBuilder parent) {
        super(parent);
    }

    public CloudProtocolConnectionStringBuilder setHost(String host) {
        parent.cb.addOption("host", host);

        return this;
    }

    public CloudProtocolConnectionStringBuilder setPort(Integer port) {
        parent.cb.addOption("port", port);

        return this;
    }
}
