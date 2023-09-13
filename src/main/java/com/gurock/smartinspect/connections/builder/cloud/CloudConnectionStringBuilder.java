package com.gurock.smartinspect.connections.builder.cloud;

import com.gurock.smartinspect.connections.builder.CloudProtocolConnectionStringBuilder;
import com.gurock.smartinspect.connections.builder.ConnectionStringBuilder;

public class CloudConnectionStringBuilder extends ConnectionStringBuilder {
    public CloudProtocolConnectionStringBuilder addCloudProtocol() {
        cb.beginProtocol("cloud");
        return new CloudProtocolConnectionStringBuilder(this);
    }
}