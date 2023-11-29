package com.gurock.smartinspect.connections.builder.cloud;

import com.gurock.smartinspect.connections.builder.CloudProtocolConnectionStringBuilder;
import com.gurock.smartinspect.connections.builder.ConnectionStringBuilder;

/**
 * Class for convenient composition of the connection string.
 * Extends ConnectionStringBuilder by adding cloud protocol.
 * <p>
 * Example:
 * <pre>
 * si.setConnections(
 *         (new CloudConnectionStringBuilder()).addCloudProtocol()
 *             .setRegion("eu-central-1")
 *             .setWriteKey("INSERT_YOUR_WRITE_KEY_HERE")
 *             .addCustomLabel("User", "Bob")
 *             .addCustomLabel("Version", "0.0.1")
 *             .and().build()
 * );
 * </pre>
 */
public class CloudConnectionStringBuilder extends ConnectionStringBuilder {
    /**
     * Composes an instance of ISiCloudConnectionStringBuilder.
     * See {@link CloudProtocolConnectionStringBuilder} for a list of available methods.
     * @return CloudProtocolConnectionStringBuilder instance
     */
    public CloudProtocolConnectionStringBuilder addCloudProtocol() {
        cb.beginProtocol("cloud");
        return new CloudProtocolConnectionStringBuilder(this);
    }
}