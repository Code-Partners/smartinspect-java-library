package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.FileRotate;
import com.gurock.smartinspect.Level;
import com.gurock.smartinspect.LookupTable;
import com.gurock.smartinspect.protocols.cloud.CloudProtocol;

import java.util.LinkedHashMap;
import java.util.Map;

public class CloudProtocolConnectionStringBuilder extends TcpProtocolConnectionStringBuilder {
    private Map<String, String> fCustomLabels = new LinkedHashMap<>();

    @Override
    public ConnectionStringBuilder and() {
        parent.cb.addOption("customlabels", CloudProtocol.composeCustomLabelsString(fCustomLabels));
        return super.and();
    }

    public CloudProtocolConnectionStringBuilder(ConnectionStringBuilder parent) {
        super(parent);
    }

    public CloudProtocolConnectionStringBuilder setWriteKey(String writeKey) {
        parent.cb.addOption("writekey", writeKey);
        return this;
    }

    public CloudProtocolConnectionStringBuilder addCustomLabel(String key, String value) {
        fCustomLabels.put(key, value);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setRegion(String region) {
        parent.cb.addOption("region", region);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setChunkingEnabled(boolean enabled) {
        parent.cb.addOption("chunking.enabled", enabled);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setChunkingMaxSize(String maxSize) {
        long sizeInBytes = LookupTable.sizeToLong(maxSize, 0);
        parent.cb.addOption("chunking.maxsize", (int) sizeInBytes / 1024);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setChunkingMaxAgeMs(int maxAge) {
        parent.cb.addOption("chunking.maxagems", maxAge);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setMaxSize(String maxSize) {
        long sizeInBytes = LookupTable.sizeToLong(maxSize, 0);
        parent.cb.addOption("maxsize", (int) sizeInBytes / 1024);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setRotate(FileRotate rotate) {
        parent.cb.addOption("rotate", rotate);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setTlsEnabled(boolean tlsEnabled) {
        parent.cb.addOption("tls.enabled", tlsEnabled);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setTlsCertificateLocation(String tlsCertificateLocation) {
        parent.cb.addOption("tls.certificate.location", tlsCertificateLocation);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setTlsCertificateFilepath(String tlsCertificateFilepath) {
        parent.cb.addOption("tls.certificate.filepath", tlsCertificateFilepath);
        return this;
    }

    public CloudProtocolConnectionStringBuilder setTlsCertificatePassword(String tlsCertificatePassword) {
        parent.cb.addOption("tls.certificate.password", tlsCertificatePassword);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setHost(String host) {
        super.setHost(host);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setPort(int port) {
        super.setPort(port);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setTimeout(int timeout) {
        super.setTimeout(timeout);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setLevel(Level level) {
        super.setLevel(level);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setCaption(String caption) {
        super.setCaption(caption);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setReconnect(boolean reconnect) {
        super.setReconnect(reconnect);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setReconnectInterval(int reconnectInterval) {
        super.setReconnectInterval(reconnectInterval);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setBacklogEnabled(boolean backlogEnabled) {
        super.setBacklogEnabled(backlogEnabled);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setBacklogQueue(String backlogQueue) {
        super.setBacklogQueue(backlogQueue);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setBacklogFlushOn(Level backlogFlushOn) {
        super.setBacklogFlushOn(backlogFlushOn);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setBacklogKeepOpen(boolean backlogKeepOpen) {
        super.setBacklogKeepOpen(backlogKeepOpen);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setAsyncEnabled(boolean asyncEnabled) {
        super.setAsyncEnabled(asyncEnabled);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setAsyncThrottle(boolean asyncThrottle) {
        super.setAsyncThrottle(asyncThrottle);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setAsyncQueue(String asyncQueue) {
        super.setAsyncQueue(asyncQueue);
        return this;
    }

    @Override
    public CloudProtocolConnectionStringBuilder setAsyncClearOnDisconnect(boolean asyncClearOnDisconnect) {
        super.setAsyncClearOnDisconnect(asyncClearOnDisconnect);
        return this;
    }
}
