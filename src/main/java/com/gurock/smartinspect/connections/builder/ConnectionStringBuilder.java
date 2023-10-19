package com.gurock.smartinspect.connections.builder;

import com.gurock.smartinspect.ConnectionsBuilder;

/**
 * Class for convenient composition of the connection string.
 * Employs fluent interface pattern.
 * <p>
 * Examples:
 * <pre>
 * // TCP protocol:
 * si.setConnections(
 *     (new ConnectionStringBuilder()).addTcpProtocol()
 *         .setHost("localhost")
 *         .setReconnect(true)
 *         .and().build()
 * );
 *
 * // File protocol:
 * si.setConnections(
 *     (new ConnectionStringBuilder()).addFileProtocol()
 *         .setFilename("log.sil")
 *         .and().build()
 * );
 * </pre>
 */
public class ConnectionStringBuilder {
	ConnectionsBuilder cb = new ConnectionsBuilder();

	/**
	 * Adds Pipe protocol, returns PipeProtocolConnectionStringBuilder instance with property setters.
	 * @return Connection string builder
	 */
	public PipeProtocolConnectionStringBuilder addPipeProtocol() {
		cb.beginProtocol("pipe");
		return new PipeProtocolConnectionStringBuilder(this);
	}

	/**
	 * Adds File protocol, returns FileProtocolConnectionStringBuilder
	 * instance with property setters.
	 * @return Connection string builder
	 */
	public FileProtocolConnectionStringBuilder addFileProtocol() {
		cb.beginProtocol("file");
		return new FileProtocolConnectionStringBuilder(this);
	}

	/**
	 * Adds Memory protocol. Returns a MemoryProtocolConnectionStringBuilder
	 * instance with property setters.
	 * @return Connection string builder
	 */
	public MemoryProtocolConnectionStringBuilder addMemoryProtocol() {
		cb.beginProtocol("mem");
		return new MemoryProtocolConnectionStringBuilder(this);
	}

	/**
	 * Adds Tcp protocol, returns TcpProtocolConnectionStringBuilder
	 * instance with property setters.
	 * @return Connection string builder
	 */
	public TcpProtocolConnectionStringBuilder addTcpProtocol() {
		cb.beginProtocol("tcp");
		return new TcpProtocolConnectionStringBuilder(this);
	}

	/**
	 * Adds Text protocol, returns TextProtocolConnectionStringBuilder
	 * instance with property setters.
	 * @return Connection string builder
	 */
	public TextProtocolConnectionStringBuilder addTextProtocol() {
		cb.beginProtocol("text");
		return new TextProtocolConnectionStringBuilder(this);
	}

	/**
	 * Builds the resulting connection string.
	 *
	 * @return connection string
	 */
	public String build() {
		return cb.getConnections();
	}
}
