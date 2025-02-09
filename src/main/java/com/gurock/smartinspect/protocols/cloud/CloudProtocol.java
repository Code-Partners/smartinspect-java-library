package com.gurock.smartinspect.protocols.cloud;

import com.gurock.smartinspect.FileRotate;
import com.gurock.smartinspect.FileRotater;
import com.gurock.smartinspect.LookupTable;
import com.gurock.smartinspect.SmartInspectException;
import com.gurock.smartinspect.connections.ConnectionsBuilder;
import com.gurock.smartinspect.packets.LogHeader;
import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.PacketType;
import com.gurock.smartinspect.protocols.ProtocolException;
import com.gurock.smartinspect.protocols.TcpProtocol;
import com.gurock.smartinspect.protocols.cloud.exceptions.CloudProtocolExceptionReconnectAllowed;
import com.gurock.smartinspect.protocols.cloud.exceptions.CloudProtocolExceptionReconnectForbidden;
import com.gurock.smartinspect.protocols.cloud.exceptions.CloudProtocolExceptionWarning;
import fr.gpotter2.sslkeystorefactories.SSLSocketKeystoreFactory;

import javax.net.ssl.SSLSocket;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Used for sending packets to the SmartInspect Cloud.
 * <p>
 * This class is used for sending packets to the Cloud. Cloud protocol
 * implementation in an extension of the TCP protocol.
 * It is used when the 'cloud' protocol is specified in
 * the SmartInspect connections string. Please
 * see the IsValidOption method for a list of available protocol
 * options
 *
 * @see com.gurock.smartinspect.SmartInspect
 */
public class CloudProtocol extends TcpProtocol {

    public static final Logger logger = Logger.getLogger(CloudProtocol.class.getName());

    private boolean reconnectAllowed = true;

    private String writeKey;
    private UUID virtualFileId = UUID.randomUUID();
    private Map<String, String> customLabels = new LinkedHashMap<>();

    // packet count, does not reset to 0 when virtual file id rotates
    private int packetCount = 0;

    private long virtualFileSize = 0;

    private boolean chunkingEnabled;
    private long chunkMaxSize;
    private int chunkMaxAge; // milliseconds

    private long virtualFileMaxSize;

    private Chunk chunk = null;
    private final Object chunkingLock = new Object();

    /**
     * Default Cloud region to connect to.
     */
    private static String DEFAULT_REGION = "eu-central-1";

    private static int MAX_ALLOWED_CHUNK_MAX_SIZE = 395 * 1024;
    private static int MIN_ALLOWED_CHUNK_MAX_SIZE = 10 * 1024;

    /**
     * Maximal size of a packet that can be stored in the Cloud as a single DB record.
     * When exceeded, the packet is partitioned.
     * Chunks must be smaller than this limit.
     */
    private static int CHUNK_MAX_SIZE = 395 * 1024;

    private static int MIN_ALLOWED_CHUNK_MAX_AGE = 500;
    private static int DEFAULT_CHUNK_MAX_AGE = 1000;

    /**
     * Maximal allowed size of a log file. Used as a limit for 'maxsize'
     * option.
     */
    private static int MAX_ALLOWED_VIRTUAL_FILE_MAX_SIZE = 50 * 1024 * 1024;

    /**
     * Minimal allowed size of a log file. Used as a limit for 'maxsize'
     * option.
     */
    private static int MIN_ALLOWED_VIRTUAL_FILE_MAX_SIZE = 1 * 1024 * 1024;

    /**
     * Default size of a log file.
     */
    private static int DEFAULT_VIRTUAL_FILE_MAX_SIZE = 1 * 1024 * 1024;

    private static int MAX_ALLOWED_CUSTOM_LABEL_COUNT = 5;
    private static int MIN_ALLOWED_CUSTOM_LABEL_COMPONENT_LENGTH = 1;
    private static int MAX_ALLOWED_CUSTOM_LABEL_COMPONENT_LENGTH = 100;

    private static final byte[] PREFACE_BYTES = new byte[] { 0x29, 0x17, 0x73, 0x50 };

    private FileRotater fRotater;
    private FileRotate fRotate;

    private boolean tlsEnabled;
    private String tlsCertificateLocation;
    private String tlsCertificateFilePath;
    private String tlsCertificatePassword;

    private static String DEFAULT_TLS_CERTIFICATE_LOCATION = "resource";
    private static String DEFAULT_TLS_CERTIFICATE_FILEPATH = "client.trust";
    private static String DEFAULT_TLS_CERTIFICATE_PASSWORD = "xyh8PCNcLDVx4ZHm";

    private ScheduledExecutorService chunkFlushExecutor = null;

    public boolean isReconnectAllowed() {
        return reconnectAllowed;
    }

    private void resetChunk() {
        logger.fine("Resetting chunk");
        chunk = new Chunk(chunkMaxSize);
    }

    /**
     * Overridden. Returns 'cloud'.
     * @return 'cloud'
     */
    @Override
    protected String getName() {
        return "cloud";
    }

    /**
     * Overridden. Validates if a protocol option is supported.
     * <p>
     * The following table lists all valid options, their default values
     * and descriptions for the TCP protocol.
     * <table border="1">
     * <caption>Tcp protocol options</caption>
     * <tr>
     * <th>Valid Options</th>
     * <th>Default Value</th>
     * <th>Description</th>
     * </tr>
     *   <tr>
     *     <td>writekey</td>
     *     <td></td>
     *     <td>Write key of your SmartInspect Cloud license.</td>
     *   </tr>
     *   <tr>
     *     <td>customlabels</td>
     *     <td></td>
     *     <td>Up to 5 labels. See example below.</td>
     *   </tr>
     *   <tr>
     *     <td>region</td>
     *     <td>eu-central-1</td>
     *     <td>SCloud region.</td>
     *   </tr>
     *   <tr>
     *     <td>maxsize</td>
     *     <td>'1 MB'</td>
     *     <td>Specifies the maximum size of a log file in kilobytes. When this size is reached, the current log file is closed and a new file is opened. It is possible to specify size units like this: "1 MB". Supported units are "KB", "MB" and "GB". Min value - "1 MB", max value - "50 MB".</td>
     *   </tr>
     *   <tr>
     *     <td>rotate</td>
     *     <td>none</td>
     *     <td>Specifies the rotate mode for log files. Please see below for a list of available values. A value of "none" disables this feature.</td>
     *   </tr>
     * </table>
     *
     * For further options which affect the behavior of this protocol,
     * please have a look at the documentation of
     * Protocol.isValidOption and TcpProtocol.isValidOption methods of the parent classes.
     * <p>
     * Example:
     * <pre>
     * smartInspect.setConnections(
     *         (new CloudConnectionStringBuilder()).addCloudProtocol()
     *             .setRegion("eu-central-1")
     *             .setWriteKey("INSERT_YOUR_WRITE_KEY_HERE")
     *             .addCustomLabel("User", "Bob")
     *             .addCustomLabel("Version", "0.0.1")
     *             .and().build()
     * );
     * </pre>
     *
     * @param name The option name to validate.
     * @return True if the option is supported and false otherwise.
     */
    @Override
    protected boolean isValidOption(String name) {
        return
            name.equals("writekey")
            || name.equals("customlabels")

            || name.equals("region")

            || name.equals("chunking.enabled")
            || name.equals("chunking.maxsize")
            || name.equals("chunking.maxagems")

            || name.equals("maxsize")
            || name.equals("rotate")

            || name.equals("tls.enabled")
            || name.equals("tls.certificate.location")
            || name.equals("tls.certificate.filepath")
            || name.equals("tls.certificate.password")

            || super.isValidOption(name);
    }

    /**
     * Overridden. Loads and inspects Cloud specific options.
     * <p>
     * This method loads all relevant options and ensures their
     * correctness. See IsValidOption for a list of options which
     * are recognized by the TCP protocol.
     */
    @Override
    protected void loadOptions() {
        super.loadOptions();
        writeKey = getStringOption("writekey", "");

        String region = getStringOption("region", DEFAULT_REGION);
        // in host is not set explicitly, infer it form region
        if ((getStringOption("host", "").equals(""))) {
            fHostName = String.format("packet-receiver.%s.cloud.smartinspect.com", region);
        }

        loadChunkingOptions();
        loadVirtualFileRotationOptions();
        loadTlsOptions();

        String customLabelsOption = getStringOption("customlabels", "");
        parseCustomLabelsOption(customLabelsOption);
    }

    /**
     * Defines the default value for `reconnect` option as `true`.
     * @return true
     */
    @Override
    protected boolean getReconnectDefaultValue() {
        return true;
    }

    /**
     * Defines the default value for `async.enabled` option as `true`.
     * @return true
     */
    @Override
    protected boolean getAsyncEnabledDefaultValue() {
        return true;
    }

    /**
     * Defines the default value for `async.throttle` option as `false`.
     * @return false
     */
    @Override
    protected boolean getAsyncThrottleDefaultValue() {
        return false;
    }

    /**
     * Defines the default value for `async.queue` option as 20 megabytes.
     * Double the size of the max packet size supported by the cloud. We want async queue to fit the largest packet,
     * as have some spare space.
     * @return 20480 KB
     */
    @Override
    protected int getAsyncQueueDefaultValue() {
        return 20*1024;
    }

    private void loadChunkingOptions() {
        chunkingEnabled = getBooleanOption("chunking.enabled", true);

        chunkMaxSize = getSizeOption(
                "chunking.maxsize", CHUNK_MAX_SIZE /  LookupTable.KB_FACTOR
        );
        if (chunkMaxSize < MIN_ALLOWED_CHUNK_MAX_SIZE) chunkMaxSize = MIN_ALLOWED_CHUNK_MAX_SIZE;
        if (chunkMaxSize > MAX_ALLOWED_CHUNK_MAX_SIZE) chunkMaxSize = MAX_ALLOWED_CHUNK_MAX_SIZE;

        chunkMaxAge = getIntegerOption("chunking.maxagems", DEFAULT_CHUNK_MAX_AGE);
        if (chunkMaxAge < MIN_ALLOWED_CHUNK_MAX_AGE) chunkMaxAge = MIN_ALLOWED_CHUNK_MAX_AGE;
    }

    private void loadVirtualFileRotationOptions() {
        virtualFileMaxSize = getSizeOption(
                "maxsize", DEFAULT_VIRTUAL_FILE_MAX_SIZE / LookupTable.KB_FACTOR
        );
        if (virtualFileMaxSize < MIN_ALLOWED_VIRTUAL_FILE_MAX_SIZE) virtualFileMaxSize = MIN_ALLOWED_VIRTUAL_FILE_MAX_SIZE;
        if (virtualFileMaxSize > MAX_ALLOWED_VIRTUAL_FILE_MAX_SIZE) virtualFileMaxSize = MAX_ALLOWED_VIRTUAL_FILE_MAX_SIZE;

        fRotate = getRotateOption("rotate", FileRotate.None);

        fRotater = new FileRotater();
        fRotater.setMode(fRotate);
    }

    private void loadTlsOptions() {
        tlsEnabled = getBooleanOption("tls.enabled", true);
        tlsCertificateLocation = getStringOption("tls.certificate.location", DEFAULT_TLS_CERTIFICATE_LOCATION);
        tlsCertificateFilePath = getStringOption("tls.certificate.filepath", DEFAULT_TLS_CERTIFICATE_FILEPATH);
        tlsCertificatePassword = getStringOption("tls.certificate.password", DEFAULT_TLS_CERTIFICATE_PASSWORD);
    }

    /**
     * Overridden. Fills a TSiConnectionsBuilder instance with the
     * options currently used by this Cloud protocol.
     * @param builder ConnectionsBuilder object to fill with the current options of this protocol.
     */
    @Override
    protected void buildOptions(ConnectionsBuilder builder) {
        super.buildOptions(builder);
        builder.addOption("writekey", writeKey);
        builder.addOption("customlabels", composeCustomLabelsString(customLabels));

        builder.addOption("chunking.enabled", chunkingEnabled);
        builder.addOption("chunking.maxsize", (int) (chunkMaxSize / 1024));
        builder.addOption("chunking.maxagems", chunkMaxAge);

        builder.addOption("maxsize", (int) (virtualFileMaxSize / 1024));
        builder.addOption("rotate", this.fRotate);

        builder.addOption("tls.enabled", tlsEnabled);
        builder.addOption("tls.certificate.location", tlsCertificateLocation);
        builder.addOption("tls.certificate.filepath", tlsCertificateFilePath);
        builder.addOption("tls.certificate.password", tlsCertificatePassword);
    }

    /**
     * Overrides TCP header packet composition, adds cloud-specific fields,
     * such as write key, virtual file id, custom labels.
     * @return log header packet
     */
    @Override
    protected LogHeader composeLogHeaderPacket() {
        LogHeader packet = super.composeLogHeaderPacket();
        packet.addValue("writekey", writeKey);
        packet.addValue("virtualfileid", virtualFileId.toString());
        packet.addValue("customlabels", composeCustomLabelsString(customLabels));

        return packet;
    }

    private void parseCustomLabelsOption(String option) {
        // due to protocols class hierarchy design flaws, this function can be called
        // even before class fields are initialized
        if (customLabels == null) {
            return;
        }

        for(String keyValue : option.split(" *; *")) {
            String[] pairs = keyValue.split(" *= *", 2);
            if (pairs.length == 2) {
                String name = pairs[0];
                String value = pairs[1];

                if (
                        (name.length() <= MAX_ALLOWED_CUSTOM_LABEL_COMPONENT_LENGTH)
                        && (value.length() <= MAX_ALLOWED_CUSTOM_LABEL_COMPONENT_LENGTH)

                        && (name.length() >= MIN_ALLOWED_CUSTOM_LABEL_COMPONENT_LENGTH)
                        && (value.length() >= MIN_ALLOWED_CUSTOM_LABEL_COMPONENT_LENGTH)
                ) {
                    customLabels.put(name, value);
                }
            }

            if (customLabels.size() == MAX_ALLOWED_CUSTOM_LABEL_COUNT) {
                break;
            }
        }
    }

    public static String composeCustomLabelsString(Map<String, String> customLabels) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> pair : customLabels.entrySet()) {
            if (!result.toString().equals("")) {
                result.append(";");
            }

            result.append(pair.getKey());
            result.append("=");
            result.append(pair.getValue());
        }

        return result.toString();
    }

    /**
     * Overrides TCP protocol handshake by reversing the order,
     * for compatibility with TLS.
     * @throws IOException io exception
     * @throws SmartInspectException failed to read banner
     */
    @Override
    protected void doHandShake() throws IOException, SmartInspectException {
        sendClientBanner();
        readServerBanner();
    }

    @Override
    public void writePacket(Packet packet) throws ProtocolException {
        if (!fConnected && !reconnectAllowed) {
            logger.fine("Connection is closed and reconnect is forbidden, skip packet processing");

            return;
        }

        maybeRotateVirtualFileId(packet);

        if (!chunkingEnabled) {
            if (validatePacketSize(packet)) {
                super.writePacket(packet);
            } else {
                logger.fine("Packet exceed the max size and is ignored");
            }
        } else {
            synchronized (chunkingLock) {
                if (chunk == null) {
                    resetChunk();
                }

                if (packet.getPacketType() == PacketType.LogHeader) {
                    logger.fine("Chunking is enabled, but log header packet must be sent separately");

                    super.writePacket(packet);
                } else {
                    try {
                        chunk.compilePacket(packet);

                        if (chunk.canFitFormattedPacket()) {
                            logger.fine(String.format("Adding packet #%d to the chunk", packetCount));

                            chunk.chunkFormattedPacket();
                        } else {
                            logger.fine(String.format(
                                    "Bundle is full, packet #%d won't fit, compiling the chunk and writing it",
                                    packetCount
                            ));

                            if (chunk.packetCount > 0) {
                                super.writePacket(chunk);
                            } else {
                                logger.fine("Do not flush chunk when packet does not fit, the chunk is empty");
                            }

                            resetChunk();
                            chunk.compilePacket(packet);

                            if (chunk.canFitFormattedPacket()) {
                                logger.fine(String.format("Adding packet #%d to the chunk", packetCount));

                                chunk.chunkFormattedPacket();
                            } else {
                                logger.fine(String.format("Packet #%d won't fit even in an empty chunk, writing it raw", packetCount));

                                if (validatePacketSize(packet)) {
                                    super.writePacket(packet);
                                } else {
                                    logger.fine("Packet exceed the max size and is ignored");
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.warning("Exception while handling chunk: " + e.getClass().getName() + " - " + e.getMessage());

                        throw new RuntimeException(e);
                    }
                }
            }
        }

        if (validatePacketSize(packet)) {
            virtualFileSize += packet.getSize();
        }

        packetCount++;
    }

    private void maybeRotateVirtualFileId(Packet packet) throws ProtocolException {
        int packetSize = packet.getSize();
        logger.fine(String.format(
                "Check if packet of size %d can fit into virtual file, remaining space - %d",
                packetSize, virtualFileMaxSize - virtualFileSize - packetSize
        ));

        if (virtualFileSize + packetSize > virtualFileMaxSize) {
            logger.fine(String.format("Rotating virtual file by max size - %d", virtualFileMaxSize));

            doRotateVirtualFileId();
        } else if (this.fRotate != FileRotate.None) {
            try {
                if (this.fRotater.update(new Date())) {
                    logger.fine("Rotating virtual file by datetime");

                    doRotateVirtualFileId();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doRotateVirtualFileId() throws ProtocolException {
        if (chunkingEnabled && (chunk != null)) {
            synchronized (chunkingLock) {
                if (chunk.packetCount > 0) {
                    logger.fine("Flushing chunk before rotating virtual file id");

                    try {
                        super.writePacket(chunk);
                    } catch (Exception e) {
                        logger.fine("Exception caught");
                    }

                    resetChunk();
                }
            }
        }

        logger.info(">>>>>> CHANGE VIRTUAL FILE ID ON ROTATION");
        virtualFileId = UUID.randomUUID();

        virtualFileSize = 0;

        Packet logHeader = composeLogHeaderPacket();
        super.writePacket(logHeader);
    }

    @Override
    public void connect() throws ProtocolException {
        fRotater.initialize(new Date());

        if (chunkingEnabled) {
            chunkFlushExecutor = Executors.newScheduledThreadPool(1);

            chunkFlushExecutor.scheduleWithFixedDelay(
                    () -> flushChunkByAge(false),
                    0, 100, TimeUnit.MILLISECONDS
            );
        }

        super.connect();
    }

    @Override
    public void disconnect() throws ProtocolException {
        flushChunkByAge(true);

        if (chunkFlushExecutor != null) {
            chunkFlushExecutor.shutdown();
            try {
                chunkFlushExecutor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        super.disconnect();
    }

    @Override
    protected void internalValidateWritePacketAnswer(int bytesRead, byte[] answerBytes) throws Exception {
        String answer = new String(answerBytes, 0, bytesRead, StandardCharsets.UTF_8);

        logger.fine("Answer = " + answer + "; byte read count = " + bytesRead);

        if ((bytesRead == 2) && (answer.equals("OK"))) {
            // success
        } else if (answer.startsWith("SmartInspectProtocolException")) {
            try {
                handleErrorReply(answer);
            } catch (CloudProtocolExceptionWarning e) {
                // log warning, do nothing else
                logger.warning("SmartInspect cloud protocol warning - " + e.getMessage());
            } catch (CloudProtocolExceptionReconnectAllowed e) {
                logger.warning("SmartInspect cloud protocol error allowing reconnects - " + e.getMessage());

                // rethrow exception to trigger disconnect in the super method
                super.internalValidateWritePacketAnswer(bytesRead, answerBytes);
            } catch (CloudProtocolExceptionReconnectForbidden e) {
                logger.warning("SmartInspect cloud protocol error forbidding reconnects - " + e.getMessage());

                reconnectAllowed = false;

                // rethrow exception to trigger disconnect in the super method
                super.internalValidateWritePacketAnswer(bytesRead, answerBytes);
            }
        } else {
            // unknown reply
            super.internalValidateWritePacketAnswer(bytesRead, answerBytes);
        }
    }

    @Override
    protected Socket internalInitializeSocket() throws Exception {
        if (tlsEnabled) {
            String location = tlsCertificateLocation;

            InputStream resource;
            if (location.equals("resource")) {
                resource = getClass().getClassLoader().getResourceAsStream(tlsCertificateFilePath);
            } else {
                resource = new FileInputStream(tlsCertificateFilePath);
            }

            if (resource == null) {
                logger.fine("SSL certificate resource loading failed");

                throw new Exception("SSL certificate resource loading failed");
            }

            long timestamp = System.nanoTime();
            SSLSocket socket = SSLSocketKeystoreFactory.getSocketWithCert(
                    fHostName, fPort, resource, tlsCertificatePassword, SSLSocketKeystoreFactory.SecureType.TLSv1_2
            );
            long elapsedMs = (System.nanoTime() - timestamp) / 1000000;

            logger.fine("SSL socket created in " + elapsedMs + "ms");

            if (socket != null) {
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(this.fTimeout);

                socket.startHandshake();
            } else {
                logger.fine("SSL socket creation failed");

                throw new Exception("SSL socket creation failed");
            }

            return socket;
        } else {
            return super.internalInitializeSocket();
        }
    }

    @Override
    protected void internalWritePacket(Packet packet) throws Exception {
        fOstream.write(PREFACE_BYTES);
        super.internalWritePacket(packet);
    }

    @Override
    protected boolean internalReconnect() throws Exception {
        if (reconnectAllowed) {
            return super.internalReconnect();
        } else {
            logger.fine("Reconnect forbidden");

            return false;
        }
    }

    private void handleErrorReply(String errorMessage) {
        String[] parts = errorMessage.split(" - ", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("errorMessage must split into 2 parts by ' - ' separator");
        }

        String exceptionType = parts[0];
        String message = parts[1];

        if (exceptionType.startsWith("SmartInspectProtocolExceptionWarning")) {
            throw new CloudProtocolExceptionWarning(message);
        } else if (exceptionType.startsWith("SmartInspectProtocolExceptionReconnectAllowed")) {
            throw new CloudProtocolExceptionReconnectAllowed(message);
        } else if (exceptionType.startsWith("SmartInspectProtocolExceptionReconnectForbidden")) {
            throw new CloudProtocolExceptionReconnectForbidden(message);
        } else {
            throw new IllegalArgumentException("Unknown protocol exception type prefix");
        }
    }

    private void flushChunkByAge(boolean forceFlush) {
        synchronized (chunkingLock) {
            if (chunkingEnabled && (chunk != null)) {
                boolean timeToFlush = chunk.millisecondsSinceTheFirstPacket() > chunkMaxAge;
                if (chunk.packetCount > 0) {
                    if (timeToFlush || forceFlush) {
                        if (timeToFlush) {
                            logger.fine(String.format(
                                    "More than %dms passed since the chunk was started, time to flush it",
                                    chunkMaxAge
                            ));
                        } else {
                            logger.fine("Forced chunk flush");
                        }

                        try {
                            super.writePacket(chunk);
                        } catch (Exception e) {
                            logger.fine("Exception caught");
                        }

                        resetChunk();
                    }
                }
            }
        }
    }

    /**
     * Validate size of an individual packet. After partitioning was implemented, the upper limit of the packet size
     * is no longer set as a hard limit in the clients, but in case it'll be done in the future, this method
     * is left here undeleted.
     * @param packet packet
     * @return `true` if packet size <= max allowed size
     */
    private boolean validatePacketSize(Packet packet) {
        return true;
    }
}
