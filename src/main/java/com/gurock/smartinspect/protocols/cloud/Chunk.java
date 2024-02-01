package com.gurock.smartinspect.protocols.cloud;

import com.gurock.smartinspect.formatters.BinaryFormatter;
import com.gurock.smartinspect.formatters.Formatter;
import com.gurock.smartinspect.packets.Packet;
import com.gurock.smartinspect.packets.PacketType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class Chunk extends Packet {
    public static final Logger logger = Logger.getLogger(Chunk.class.getName());

    // chunk format (short) + packet count (int) + chunk body size in bytes (int)
    public Short headerSize = 2 + 4 + 4;
    public Short chunkFormat = 1;

    private Formatter formatter;
    private long chunkMaxSize;

    public ByteArrayOutputStream stream = new ByteArrayOutputStream();

    public int packetCount = 0;
    private int lastCompiledPacketSize;
    private long nanoTimeOfFirstPacket = 0;

    public Chunk(long chunkMaxSize) {
        this.formatter = new BinaryFormatter();
        this.chunkMaxSize = chunkMaxSize;
    }

    @Override
    public int getSize() {
        return this.headerSize + stream.size();
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.Chunk;
    }

    /**
     * Compile but don't add to the chunk yet.
     * @param packet packet to compile
     * @throws IOException io exception
     */
    public void compilePacket(Packet packet) throws IOException {
        lastCompiledPacketSize = formatter.compile(packet);
    }

    public boolean canFitFormattedPacket() {
        logger.fine(String.format(
                "Check if packet of size %d can fit into the chunk, remaining bytes - %d",
                lastCompiledPacketSize, chunkMaxSize - this.getSize()
        ));

        return lastCompiledPacketSize + this.getSize() <= chunkMaxSize;
    }

    public void chunkFormattedPacket() throws IOException {
        formatter.write(stream);

        if (packetCount == 0) {
            nanoTimeOfFirstPacket = System.nanoTime();
        }

        packetCount++;
    }

    public long millisecondsSinceTheFirstPacket() {
        long current = System.nanoTime();

        return (current - nanoTimeOfFirstPacket) / 1000000;
    }
}