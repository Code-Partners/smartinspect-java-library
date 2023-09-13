package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.FileRotate;
import com.gurock.smartinspect.Level;

public class TextProtocolConnectionStringBuilder extends FileProtocolConnectionStringBuilder {
    public TextProtocolConnectionStringBuilder(ConnectionStringBuilder parent) {
        super(parent);
    }

    public TextProtocolConnectionStringBuilder setPattern(String pattern) {
        parent.cb.addOption("pattern", pattern);
        return this;
    }

    public TextProtocolConnectionStringBuilder setIndent(boolean indent) {
        parent.cb.addOption("indent", indent);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setFilename(String filename) {
        super.setFilename(filename);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setAppend(boolean append) {
        super.setAppend(append);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setBuffer(String buffer) {
        super.setBuffer(buffer);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setRotate(FileRotate rotate) {
        super.setRotate(rotate);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setMaxSize(String maxSize) {
        super.setMaxSize(maxSize);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setMaxParts(int maxParts) {
        super.setMaxParts(maxParts);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setKey(String key) {
        super.setKey(key);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setEncrypt(boolean encrypt) {
        super.setEncrypt(encrypt);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setLevel(Level level) {
        super.setLevel(level);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setCaption(String caption) {
        super.setCaption(caption);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setReconnect(boolean reconnect) {
        super.setReconnect(reconnect);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setReconnectInterval(int reconnectInterval) {
        super.setReconnectInterval(reconnectInterval);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setBacklogEnabled(boolean backlogEnabled) {
        super.setBacklogEnabled(backlogEnabled);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setBacklogQueue(String backlogQueue) {
        super.setBacklogQueue(backlogQueue);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setBacklogFlushOn(Level backlogFlushOn) {
        super.setBacklogFlushOn(backlogFlushOn);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setBacklogKeepOpen(boolean backlogKeepOpen) {
        super.setBacklogKeepOpen(backlogKeepOpen);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setAsyncEnabled(boolean asyncEnabled) {
        super.setAsyncEnabled(asyncEnabled);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setAsyncThrottle(boolean asyncThrottle) {
        super.setAsyncThrottle(asyncThrottle);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setAsyncQueue(String asyncQueue) {
        super.setAsyncQueue(asyncQueue);
        return this;
    }

    @Override
    public TextProtocolConnectionStringBuilder setAsyncClearOnDisconnect(boolean asyncClearOnDisconnect) {
        super.setAsyncClearOnDisconnect(asyncClearOnDisconnect);
        return this;
    }
}
