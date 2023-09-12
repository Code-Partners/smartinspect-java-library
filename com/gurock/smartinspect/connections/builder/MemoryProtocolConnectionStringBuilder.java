package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.Level;
import com.gurock.smartinspect.LookupTable;

public class MemoryProtocolConnectionStringBuilder extends ProtocolConnectionStringBuilder {
    public MemoryProtocolConnectionStringBuilder(ConnectionStringBuilder parent) {
        super(parent);
    }

    public MemoryProtocolConnectionStringBuilder setMaxSize(String maxSize) {
        long sizeInBytes = LookupTable.sizeToLong(maxSize, 0);
        parent.cb.addOption("maxsize", (int) sizeInBytes / 1024);
        return this;
    }

    public MemoryProtocolConnectionStringBuilder setAsText(boolean asText) {
        parent.cb.addOption("astext", asText);
        return this;
    }

    public MemoryProtocolConnectionStringBuilder setIndent(boolean indent) {
        parent.cb.addOption("indent", indent);
        return this;
    }

    public MemoryProtocolConnectionStringBuilder setPattern(String pattern) {
        parent.cb.addOption("pattern", pattern);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setLevel(Level level) {
        super.setLevel(level);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setCaption(String caption) {
        super.setCaption(caption);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setReconnect(boolean reconnect) {
        super.setReconnect(reconnect);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setReconnectInterval(int reconnectInterval) {
        super.setReconnectInterval(reconnectInterval);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setBacklogEnabled(boolean backlogEnabled) {
        super.setBacklogEnabled(backlogEnabled);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setBacklogQueue(String backlogQueue) {
        super.setBacklogQueue(backlogQueue);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setBacklogFlushOn(Level backlogFlushOn) {
        super.setBacklogFlushOn(backlogFlushOn);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setBacklogKeepOpen(boolean backlogKeepOpen) {
        super.setBacklogKeepOpen(backlogKeepOpen);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setAsyncEnabled(boolean asyncEnabled) {
        super.setAsyncEnabled(asyncEnabled);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setAsyncThrottle(boolean asyncThrottle) {
        super.setAsyncThrottle(asyncThrottle);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setAsyncQueue(String asyncQueue) {
        super.setAsyncQueue(asyncQueue);
        return this;
    }

    @Override
    public MemoryProtocolConnectionStringBuilder setAsyncClearOnDisconnect(boolean asyncClearOnDisconnect) {
        super.setAsyncClearOnDisconnect(asyncClearOnDisconnect);
        return this;
    }
}
