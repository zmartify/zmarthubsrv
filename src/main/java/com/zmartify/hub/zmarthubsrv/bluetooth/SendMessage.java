/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.bluetooth;

import static com.zmartify.hub.zmarthubsrv.ZmartBTServerClass.MAX_PAYLOAD_SIZE;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Peter Kristensen
 *
 */
public class SendMessage implements ISendMessage {

    protected MessageListener subscriber;

    protected static final AtomicInteger chunkIdGenerator = new AtomicInteger(0);
    protected static final AtomicInteger sentMessages = new AtomicInteger(0);

    public SendMessage(MessageListener subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * Queues the given message to be sent over the IBluletoothConnection this
     * object was created with.
     *
     * @param message
     *            the message to be send
     */
    public void sendMessage(Message message) {
        // check if we need to chunk this message
        final boolean needsToBeChunked = message.isContainsPayload() && message.getPayload().length > MAX_PAYLOAD_SIZE;
        if (needsToBeChunked) {
            if (message.isChunkMessage()) {
                throw new IllegalStateException("Already chunked messages should never be chunked again!");
            }
            final short chunkId = (short) chunkIdGenerator.getAndIncrement();
            List<Message> chunkMessages = message.createChunks(chunkId);
            for (Message chunkMessage : chunkMessages) {
                sendMessage(chunkMessage);
            }
            return;
        }
        sentMessages.getAndIncrement();
        subscriber.message(message);
    }

    public void sendMessage(byte[] payload) {
        sendMessage(new Message(payload));
    }

    public void sendMessage(String payloadStr) {
        sendMessage(new Message(payloadStr));
    }
}
