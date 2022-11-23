package com.gurock.smartinspect.connections.builder;

public class ProtocolConnectionStringBuilder {

    protected ConnectionStringBuilder parent;

    public ProtocolConnectionStringBuilder(ConnectionStringBuilder parent) {
        this.parent = parent;
    }

    public ConnectionStringBuilder and() {
        parent.cb.endProtocol();

        return parent;
    }

}
