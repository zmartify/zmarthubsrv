package com.zmartify.hub.zmarthubsrv.bluetooth;

import static com.zmartify.hub.zmarthubsrv.ZmartBTServerClass.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Message {

    // private static final Logger log = LoggerFactory.getLogger(Message.class);

    private static final short BITINDEX_CONTAINSPAYLOAD = 0;
    private static final short BITINDEX_ISCHUNKMESSAGE = 1;

    private static final int HEADER_BASE_LENGTH = 2;
    private static final int HEADER_CHUNK_LENGTH = 4;

    private short chunkId;
    private short chunkNo;

    private boolean containsPayload = false;
    private boolean isChunkMessage = false;

    /*
     * THE MESSAGE PAYLOAD
     */
    private byte[] payload;

    /**
     * The connection over which this message was received.
     * 
     */
    private BluetoothConnection originatorConnection;

    public Message() {
    }

    /*
     * A list of constructor to ease construction of messages from various objects
     */

    public Message(byte[] rawPayload) {
        super();
        ByteBuffer byteBuffer = ByteBuffer.wrap(rawPayload).order(BYTE_ORDER);
        deserialize(byteBuffer);
    }

    public Message(ByteBuffer byteBuffer) {
        super();
        byteBuffer.rewind();
        deserialize(byteBuffer);
    }

    public Message(String payloadAsString) {
        super();

        if (payloadAsString != null) {
            if (!payloadAsString.isEmpty()) {
                this.containsPayload = true;
                this.payload = payloadAsString.getBytes();
            }
        }
    }

    /**
     * Creates chunks of this message containing the given chunkId. The chunk
     * messages are numbered. The numbers can be retrieved via #getChunkNumber(). To
     * signal a potential receiver that a chunk message is the last message of a
     * chunkId, a chunk message with less than MAX_PAYLOAD_SIZE payload size is
     * send. If the last message's payload is equal to MAX_PAYLOAD_SIZE the last
     * message will not contain any payload.
     *
     * @param chunkId
     *            the chunk id to identify this message
     * @return the ordered list of chunks
     * @throws IllegalArgumentException
     *             iff the message contains too much payload to chunk (more than
     *             Short.MAX_VALUE resulting chunks)
     */
    public List<Message> createChunks(short chunkId) {
        final int payloadLength = getPayload().length;
        if ((payload.length <= 0) || !containsPayload) {
            throw new IllegalStateException("createChunks() was called for a message without any payload!");
        }

        final int maxChunkSize = MAX_PAYLOAD_SIZE;
        final int numChunks = (payloadLength + maxChunkSize - 1) / maxChunkSize; // rounded
                                                                                 // up
        if (numChunks > USHORT_MAX_VALUE) { // unsigned
                                            // short
                                            // max value
            throw new IllegalArgumentException("The message contains " + payloadLength
                    + "bytes payload which results in " + numChunks
                    + " chunks. The number of chunks exceeds the message header field (short, 2 bytes) and is therefore too big");
        }
        final ByteBuffer byteBuffer = ByteBuffer.wrap(getPayload()).order(BYTE_ORDER);
        final List<Message> chunks = new ArrayList<>();

        for (int chunkNo = 1; chunkNo <= numChunks; chunkNo += 1) {
            // create chunk messages
            final byte[] chunkPayload;
            if (byteBuffer.remaining() >= maxChunkSize) {
                chunkPayload = new byte[maxChunkSize];
            } else {
                chunkPayload = new byte[byteBuffer.remaining()];
            }
            byteBuffer.get(chunkPayload);

            chunks.add(createChunk(chunkId, (short) chunkNo, chunkPayload));
        }

        // now we check if the last message equals our maxChunkSize and we
        // therefore have to add an "end marker" message
        Message lastMessage = chunks.get(chunks.size() - 1);
        if (lastMessage.getPayload().length == maxChunkSize) {
            // we add an empty message to signal that this is the last chunk
            chunks.add(createChunk(chunkId, (short) (chunks.size() + 1), new byte[0]));
        }

        return chunks;
    }

    /**
     * Creates a message from multiple chunks
     * 
     * @param chunks
     *            the complete list of chunks with the same chunkId. Must not be
     *            ordered.
     * @return the message
     */
    public Message fromChunks(List<Message> chunks) {
        // sort by chunkNo
        Collections.sort(chunks, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return Short.valueOf(o1.chunkNo).compareTo(Short.valueOf(o2.chunkNo));
            }
        });

        Message message = new Message();

        int firstChunkId = -1;
        int totalSize = 0;
        int i = 0;
        for (Message chunk : chunks) {
            // validate id on the run
            if (i++ == 0) {
                // initially set up some vars
                copyHeader(chunk, message);
                message.isChunkMessage = false;

                firstChunkId = chunk.getChunkId();
                message.originatorConnection = chunk.getOriginatorConnection();
            } else if (firstChunkId != chunk.getChunkId()) {
                throw new IllegalArgumentException("The list contained chunk messages of multiple chunkIds. ");
            }
            if (chunk.containsPayload) {
                totalSize += chunk.payload.length;
            }
        }

        // create the combined payload byte array
        final ByteBuffer byteBuffer = ByteBuffer.allocate(totalSize).order(BYTE_ORDER);
        chunks.forEach(chunk -> {
            if (chunk.containsPayload) {
                byteBuffer.put(chunk.getPayload());
            }
        });

        byteBuffer.flip();
        final byte[] payload = byteBuffer.array();
        message.setPayload(payload);
        return message;
    }

    private Message createChunk(short chunkId, short chunkNo, byte[] chunkPayload) {
        Message chunk = new Message();
        copyHeader(this, chunk);
        chunk.chunkId = chunkId;
        chunk.chunkNo = chunkNo;
        chunk.payload = payload;
        chunk.isChunkMessage = true;
        return chunk;
    }

    private void copyHeader(Message from, Message to) {
        to.containsPayload = from.containsPayload;
        to.isChunkMessage = from.isChunkMessage;
        to.chunkNo = from.chunkNo;
        to.chunkId = from.chunkId;
    }

    protected BluetoothConnection getOriginatorConnection() {
        return originatorConnection;
    }

    protected void setLastOriginatorConnection(BluetoothConnection originatorConnection) {
        this.originatorConnection = originatorConnection;
    }

    /**
     * Retrieve this message's payload
     * 
     * @return payload as byte array (max 65535 bytes)
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Set the payload of this message.
     * 
     * @param payload
     *            the payload bytes
     */
    public void setPayload(byte[] payload) {
        if (payload != null && payload.length > 0) {
            containsPayload = true;
        } else {
            containsPayload = false;
        }
        this.payload = payload;
    }

    public void setPayload(String payload) {
        setPayload(payload.getBytes());
    }

    /**
     * Applies all data from the message schema except the payload, which has to be
     * set afterwards by setPayload(), to this instance. The payload's length is set
     * and returned;
     *
     * @param headerBytes
     *            the byte array containing all header informations
     */
    public void deserialize(ByteBuffer byteBuffer) {
        byteBuffer.rewind();

        // #1-2 CONTROL BITS
        deserializeControlShort(byteBuffer.getShort());

        // CHUNKID & CHUNKNO
        if (isChunkMessage) {
            // #3-4 CHUNKID
            chunkId = byteBuffer.getShort();
            // #5-6 CHUNKNO
            chunkNo = byteBuffer.getShort();
        }

        // Check if there is any payload
        if (containsPayload) {
            // If any, rest of rawMessage is payload
            payload = new byte[byteBuffer.remaining()];
            byteBuffer.get(payload);
        } else {
            payload = new byte[0];
        }
    }

    public ByteBuffer toByteBuffer() {
        int length = getHeaderLength() + payload.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(length).order(BYTE_ORDER);

        byteBuffer.putShort(serializeControlShort());

        if (isChunkMessage) {
            byteBuffer.putShort(chunkId);
            byteBuffer.putShort(chunkNo);
        }

        if (containsPayload) {
            byteBuffer.put(payload);
        }

        return byteBuffer;
    }

    public byte[] toByteArray() {
        ByteBuffer byteBuffer = toByteBuffer();
        byteBuffer.rewind();
        byte[] byteArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    private short setBit(short b, int pos, boolean trueFalse) {
        if (trueFalse)
            return (short) (b | (1 << pos));
        else
            return b;
    }

    private boolean isBitSet(short b, int pos) {
        return (b & (1 << pos)) != 0;
    }

    public short serializeControlShort() {
        short b = 0;
        b = setBit(b, BITINDEX_CONTAINSPAYLOAD, containsPayload);
        b = setBit(b, BITINDEX_ISCHUNKMESSAGE, isChunkMessage);
        return b;
    }

    public void deserializeControlShort(short b) {
        containsPayload = isBitSet(b, BITINDEX_CONTAINSPAYLOAD);
        isChunkMessage = isBitSet(b, BITINDEX_ISCHUNKMESSAGE);
    }

    /**
     * Calculates a messages total header length by a given BlaubotMessageType
     * 
     * @param messageType
     *            the message type
     * @return the length of all header fields excluding the payload bytes.
     */
    protected int getHeaderLength() {
        // calculate the total header length needed
        int totalLength = HEADER_BASE_LENGTH;

        if (isChunkMessage) {
            totalLength += HEADER_CHUNK_LENGTH;
        }

        return totalLength;
    }

    /**
     * If this message is a chunk message, returns the chunk number.
     * 
     * @return the chunk number
     */
    public short getChunkNo() {
        return chunkNo;
    }

    /**
     * Sets the chunk number
     * 
     * @param chunkNo
     *            has to be 1-based
     */
    public void setChunkNo(short chunkNo) {
        if (chunkNo == 0) {
            throw new IllegalArgumentException("chunkNo is 1-based");
        }
        this.chunkNo = chunkNo;
    }

    /**
     * If this message is a chunk message, gets the chunk id.
     * 
     * @return the chunk id
     */
    public short getChunkId() {
        return chunkId;
    }

    /**
     * sets the chunk id
     * 
     * @param chunkId
     *            the chunk id
     */
    public void setChunkId(short chunkId) {
        this.chunkId = chunkId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Message that = (Message) o;

        if (!Arrays.equals(payload, that.payload))
            return false;

        return true;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Message{");
        if (isChunkMessage) {
            sb.append("chunkId=").append(chunkId);
            sb.append("chunkNo=").append(chunkNo).append(" ,");
        }
        sb.append("containsPayload=" + this.containsPayload);
        sb.append(", isChunkMessage=" + this.isChunkMessage);
        sb.append(", payload=");
        if (payload == null)
            sb.append("null");
        else {
            sb.append(payload.length + " bytes");
        }
        sb.append(", originatorConnection=").append(originatorConnection);
        sb.append('}');
        return sb.toString();
    }

    public boolean isContainsPayload() {
        return containsPayload;
    }

    public boolean isChunkMessage() {
        return isChunkMessage;
    }

    public boolean isLast() {
        return false;
    }

}
