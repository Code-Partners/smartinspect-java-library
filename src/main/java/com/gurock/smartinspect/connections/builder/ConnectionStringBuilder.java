package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.connections.ConnectionsBuilder;

// <summary>
//   Class for convenient composition of the connection string.
//   Employs fluent interface pattern.
// </summary>
// <example>
// TCP protocol:
// <code>
// si.setConnections(
//     (new ConnectionStringBuilder()).addTcpProtocol()
//         .setHost("localhost")
//         .setReconnect(true)
//         .and().build()
// );
// </code>
//
// File protocol:
// <code>
// si.setConnections(
//     (new ConnectionStringBuilder()).addFileProtocol()
//         .setFilename("log.sil")
//         .and().build()
// );
// </code>
// </example>

public class ConnectionStringBuilder {
    protected ConnectionsBuilder cb = new ConnectionsBuilder();

    // <summary>
    //   Adds Pipe protocol, returns PipeProtocolConnectionStringBuilder
    //   instance with property setters.
    // </summary>

    public PipeProtocolConnectionStringBuilder addPipeProtocol() {
        cb.beginProtocol("pipe");
        return new PipeProtocolConnectionStringBuilder(this);
    }

    // <summary>
    //   Adds File protocol, returns FileProtocolConnectionStringBuilder
    //   instance with property setters.
    // </summary>

    public FileProtocolConnectionStringBuilder addFileProtocol() {
        cb.beginProtocol("file");
        return new FileProtocolConnectionStringBuilder(this);
    }

    // <summary>
    //   Adds Memory protocol, returns MemoryProtocolConnectionStringBuilder
    //   instance with property setters.
    // </summary>

    public MemoryProtocolConnectionStringBuilder addMemoryProtocol() {
        cb.beginProtocol("mem");
        return new MemoryProtocolConnectionStringBuilder(this);
    }

    // <summary>
    //   Adds Tcp protocol, returns TcpProtocolConnectionStringBuilder
    //   instance with property setters.
    // </summary>

    public TcpProtocolConnectionStringBuilder addTcpProtocol() {
        cb.beginProtocol("tcp");
        return new TcpProtocolConnectionStringBuilder(this);
    }

    // <summary>
    //   Adds Text protocol, returns TextProtocolConnectionStringBuilder
    //   instance with property setters.
    // </summary>

    public TextProtocolConnectionStringBuilder addTextProtocol() {
        cb.beginProtocol("text");
        return new TextProtocolConnectionStringBuilder(this);
    }

    // <summary>
    //   Builds the resulting connection string.
    // </summary>

    public String build() {
        return cb.getConnections();
    }
}
