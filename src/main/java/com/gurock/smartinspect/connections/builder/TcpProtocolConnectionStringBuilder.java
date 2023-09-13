package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.Level;

public class TcpProtocolConnectionStringBuilder extends ProtocolConnectionStringBuilder {
    public TcpProtocolConnectionStringBuilder(ConnectionStringBuilder parent) {
        super(parent);
    }

    public TcpProtocolConnectionStringBuilder setHost(String host) {
        parent.cb.addOption("host", host);
        return this;
    }

    public TcpProtocolConnectionStringBuilder setPort(int port) {
        parent.cb.addOption("port", port);
        return this;
    }

    public TcpProtocolConnectionStringBuilder setTimeout(int timeout) {
        parent.cb.addOption("timeout", timeout);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setLevel(Level level) {
        super.setLevel(level);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setCaption(String caption) {
        super.setCaption(caption);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setReconnect(boolean reconnect) {
        super.setReconnect(reconnect);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setReconnectInterval(int reconnectInterval) {
        super.setReconnectInterval(reconnectInterval);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setBacklogEnabled(boolean backlogEnabled) {
        super.setBacklogEnabled(backlogEnabled);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setBacklogQueue(String backlogQueue) {
        super.setBacklogQueue(backlogQueue);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setBacklogFlushOn(Level backlogFlushOn) {
        super.setBacklogFlushOn(backlogFlushOn);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setBacklogKeepOpen(boolean backlogKeepOpen) {
        super.setBacklogKeepOpen(backlogKeepOpen);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setAsyncEnabled(boolean asyncEnabled) {
        super.setAsyncEnabled(asyncEnabled);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setAsyncThrottle(boolean asyncThrottle) {
        super.setAsyncThrottle(asyncThrottle);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setAsyncQueue(String asyncQueue) {
        super.setAsyncQueue(asyncQueue);
        return this;
    }

    @Override
    public TcpProtocolConnectionStringBuilder setAsyncClearOnDisconnect(boolean asyncClearOnDisconnect) {
        super.setAsyncClearOnDisconnect(asyncClearOnDisconnect);
        return this;
    }
}
