package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.FileRotate;
import com.gurock.smartinspect.Level;
import com.gurock.smartinspect.LookupTable;

public class FileProtocolConnectionStringBuilder extends ProtocolConnectionStringBuilder {
    public FileProtocolConnectionStringBuilder(ConnectionStringBuilder parent) {
        super(parent);
    }

    public FileProtocolConnectionStringBuilder setFilename(String filename) {
        parent.cb.addOption("filename", filename);
        return this;
    }

    public FileProtocolConnectionStringBuilder setAppend(boolean append) {
        parent.cb.addOption("append", append);
        return this;
    }

    public FileProtocolConnectionStringBuilder setBuffer(String buffer) {
        long sizeInBytes = LookupTable.sizeToLong(buffer, 0);
        parent.cb.addOption("buffer", (int)sizeInBytes / 1024);
        return this;
    }

    public FileProtocolConnectionStringBuilder setRotate(FileRotate rotate) {
        parent.cb.addOption("rotate", rotate);
        return this;
    }

    public FileProtocolConnectionStringBuilder setMaxSize(String maxSize) {
        long sizeInBytes = LookupTable.sizeToLong(maxSize, 0);
        parent.cb.addOption("maxsize", (int)sizeInBytes / 1024);
        return this;
    }

    public FileProtocolConnectionStringBuilder setMaxParts(int maxParts) {
        parent.cb.addOption("maxparts", maxParts);
        return this;
    }

    public FileProtocolConnectionStringBuilder setKey(String key) {
        parent.cb.addOption("key", key);
        return this;
    }

    public FileProtocolConnectionStringBuilder setEncrypt(boolean encrypt) {
        parent.cb.addOption("encrypt", encrypt);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setLevel(Level level) {
        super.setLevel(level);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setCaption(String caption) {
        super.setCaption(caption);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setReconnect(boolean reconnect) {
        super.setReconnect(reconnect);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setReconnectInterval(int reconnectInterval) {
        super.setReconnectInterval(reconnectInterval);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setBacklogEnabled(boolean backlogEnabled) {
        super.setBacklogEnabled(backlogEnabled);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setBacklogQueue(String backlogQueue) {
        super.setBacklogQueue(backlogQueue);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setBacklogFlushOn(Level backlogFlushOn) {
        super.setBacklogFlushOn(backlogFlushOn);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setBacklogKeepOpen(boolean backlogKeepOpen) {
        super.setBacklogKeepOpen(backlogKeepOpen);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setAsyncEnabled(boolean asyncEnabled) {
        super.setAsyncEnabled(asyncEnabled);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setAsyncThrottle(boolean asyncThrottle) {
        super.setAsyncThrottle(asyncThrottle);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setAsyncQueue(String asyncQueue) {
        super.setAsyncQueue(asyncQueue);
        return this;
    }

    @Override
    public FileProtocolConnectionStringBuilder setAsyncClearOnDisconnect(boolean asyncClearOnDisconnect) {
        super.setAsyncClearOnDisconnect(asyncClearOnDisconnect);
        return this;
    }
}
