package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.Level;
import com.gurock.smartinspect.LookupTable;

public class ProtocolConnectionStringBuilder {

    protected ConnectionStringBuilder parent;

    public ProtocolConnectionStringBuilder(ConnectionStringBuilder parent) {
        this.parent = parent;
    }

    public ConnectionStringBuilder and() {
        parent.cb.endProtocol();

        return parent;
    }

    public ProtocolConnectionStringBuilder setLevel(Level level) {
        parent.cb.addOption("level", level);
        return this;
    }

    public ProtocolConnectionStringBuilder setCaption(String caption) {
        parent.cb.addOption("caption", caption);
        return this;
    }

    public ProtocolConnectionStringBuilder setReconnect(boolean reconnect) {
        parent.cb.addOption("reconnect", reconnect);
        return this;
    }

    public ProtocolConnectionStringBuilder setReconnectInterval(int reconnectInterval) {
        parent.cb.addOption("reconnect.interval", reconnectInterval);
        return this;
    }

    public ProtocolConnectionStringBuilder setBacklogEnabled(boolean backlogEnabled) {
        parent.cb.addOption("backlog.enabled", backlogEnabled);
        return this;
    }

    public ProtocolConnectionStringBuilder setBacklogQueue(String backlogQueue) {
        long sizeInBytes = LookupTable.sizeToLong(backlogQueue, 0);
        parent.cb.addOption("backlog.queue", (int)sizeInBytes / 1024);
        return this;
    }

    public ProtocolConnectionStringBuilder setBacklogFlushOn(Level backlogFlushOn) {
        parent.cb.addOption("backlog.flushon", backlogFlushOn);
        return this;
    }

    public ProtocolConnectionStringBuilder setBacklogKeepOpen(boolean backlogKeepOpen) {
        parent.cb.addOption("backlog.keepopen", backlogKeepOpen);
        return this;
    }

    public ProtocolConnectionStringBuilder setAsyncEnabled(boolean asyncEnabled) {
        parent.cb.addOption("async.enabled", asyncEnabled);
        return this;
    }

    public ProtocolConnectionStringBuilder setAsyncThrottle(boolean asyncThrottle) {
        parent.cb.addOption("async.throttle", asyncThrottle);
        return this;
    }

    public ProtocolConnectionStringBuilder setAsyncQueue(String asyncQueue) {
        long sizeInBytes = LookupTable.sizeToLong(asyncQueue, 0);
        parent.cb.addOption("async.queue", (int)sizeInBytes / 1024);
        return this;
    }

    public ProtocolConnectionStringBuilder setAsyncClearOnDisconnect(boolean asyncClearOnDisconnect) {
        parent.cb.addOption("async.clearondisconnect", asyncClearOnDisconnect);
        return this;
    }
}
