package com.zmartify.hub.zmarthubsrv.bluetooth;

import static com.zmartify.hub.zmarthubsrv.ZmartBTServerClass.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReceiver<T> {

    private static Logger log = LoggerFactory.getLogger(MessageReceiver.class);

    /**
     * Locks the access to the chunked message mappings
     */
    private final Object chunkLock = new Object();
    private Map<Short, Boolean> receivedLastChunkMapping = new HashMap<>();
    private Map<Short, List<Message>> receivedChunks = new HashMap<>();

    private BluetoothConnection connection = null;
    private volatile MessageReceivingThread messageReceivingThread;

    /**
     * Monitor to avoid execution of two MessageReceivingThreads at the same time on
     * this instance. (could happen on fast activate/deactivate calls)
     * 
     */

    private final Object receiverMonitor = new Object();
    private static AtomicInteger receivedMessages = new AtomicInteger(0);
    private static AtomicInteger receivedChunkMessages = new AtomicInteger(0);

    private long receivedPayloadBytes = 0;

    // ----- RxJava2 implementation -----
    private MessageListener subscriber = null;
    private transient boolean running = false;

    public MessageReceiver() {
    }

    /**
     * The blaubot connection used to receive messages.
     *
     * @return the connection object used to receive messages
     */
    public BluetoothConnection getConnection() {
        return connection;
    }

    /**
     * Activates the message receiver (reading from the connection). If the receiver
     * was already started, it will start a new consumer-thread that will
     * sequentially take over the work from the previous thread.
     */
    public void activate(BluetoothConnection newConnection) {
        this.connection = newConnection;
        MessageReceivingThread mrt = new MessageReceivingThread();
        mrt.setName("msg-receiver-" + connection.toString() + ", " + mrt.getId());
        synchronized (activationLock) {
            // we don't interrupt a possibly already running receive thread
            // here, see comment in BlaubotMessageManager
            // deactivate() method.
            messageReceivingThread = mrt;
        }
        mrt.start();

        // --- RxJava2
        log.debug("Receiver Activated");
        running = true;
    }

    /**
     * Deactivates the message receiver (completes current message readings, if any
     * and then shuts down)
     *
     * @param actionListener
     *            callback to be informed when the receiver was closed (thread
     *            finished), can be null
     */
    public void deactivate() {
        final MessageReceivingThread mrt;
        synchronized (activationLock) {
            mrt = messageReceivingThread;
            messageReceivingThread = null;
        }
        if (mrt != null) {
            mrt.interrupt(); // we don't interrupt the thread, because we
            // could end up in a out of sync bytestream this way
        }
        // clear chunk mappings
        synchronized (chunkLock) {
            receivedLastChunkMapping.clear();
        }

        // --- RxJava2
        log.debug("Receiver Deactivated");
    }

    public void register(MessageListener listener) {
        log.debug("Register messageListener : " + listener);
        subscriber = listener;
    }

    /**
     * Monitor for activation/deactivation calls.
     */
    private Object activationLock = new Object();

    /**
     * @return number of received messages so far (including chunks)
     */
    public int getReceivedMessages() {
        return receivedMessages.get();
    }

    /**
     * @return number of received payload bytes so far (including chunks)
     */
    public long getReceivedPayloadBytes() {
        return receivedPayloadBytes;
    }

    /**
     * @return number of received chunk messages (chunks themselves)
     */
    public int getReceivedChunkMessages() {
        return receivedChunkMessages.get();
    }

    /**
     * Called by the receiving thread if a chunk message was received.
     *
     * @param chunkMessage
     *            the message
     */
    private void onChunkMessageReceived(Message chunkMessage) {
        /*
         * Chunked messages are processed as follows: - we store a mapping chunkId ->
         * List of received messages regarding this chunk id - additionally we store a
         * mapping chunkId -> Boolean which indicates, that we received the last chunk,
         * which is determined by receiving a chunked message with the chunkId, that has
         * less than BlaubotConstants.MAX_PAYLOAD_SIZE bytes as payload - if we receive
         * a chunked message, we check if the last message was received. - if no: do
         * nothing - if yes: check if we have all the chunks (all numbers from 0 to
         * chunkNo of the last message) - if no: do nothin - if yes: - built the
         * resulting message from the chunked messages and notify our listeners - clear
         * the mapping
         */
        final short chunkId = chunkMessage.getChunkId();
        List<Message> completeListOfChunks = null;
        synchronized (chunkLock) {
            final boolean contained = receivedChunks.containsKey(chunkId);
            if (!contained) {
                receivedChunks.put(chunkId, new ArrayList<Message>());
            }
            final List<Message> messageList = receivedChunks.get(chunkId);
            messageList.add(chunkMessage);

            boolean isLastChunkMessage = chunkMessage.getPayload().length < MAX_PAYLOAD_SIZE;
            if (isLastChunkMessage) {
                receivedLastChunkMapping.put(chunkId, true);
            }

            if (isLastChunkMessage || receivedLastChunkMapping.get(chunkId) != null) {
                int sum = 0;
                int maxChunkNo = -1;
                for (Message chunk : messageList) {
                    final int chunkNo = chunk.getChunkNo() & 0xffff; // unsigned
                                                                     // shorts
                    sum += chunkNo;
                    if (chunkNo > maxChunkNo) {
                        maxChunkNo = chunkNo;
                    }
                }

                // check if we have all chunkNo n * (n+1)/2 == sum (Gauss)
                boolean weHaveAllChunks = (maxChunkNo * (maxChunkNo + 1) / 2) == sum;
                if (weHaveAllChunks) {
                    // fill the completeListOfChunks variable and forget about
                    // everything
                    completeListOfChunks = messageList;

                    receivedLastChunkMapping.remove(chunkId);
                    receivedChunks.remove(chunkId);
                }
            }
        }
        if (completeListOfChunks != null) {
            Message msg = new Message().fromChunks(completeListOfChunks);
            subscriber.message(msg);
        }
    }

    /**
     * Consumes the connection's byte stream and deserializes Messages from it.
     */
    class MessageReceivingThread extends Thread {
        /**
         * Milliseconds to wait if an io exception happens on read to not block the
         * whole system in this cases and let the listeners do their onConnectionClosed
         * magic a little faster.
         */
        private static final long SLEEP_TIME_ON_IO_FAILURE = 350;

        @Override
        public void run() {
            synchronized (receiverMonitor) {
                log.debug("Started receiver for connection: {}", connection);
                while (messageReceivingThread == this && !isInterrupted()) {
                    // Read from the InputStream
                    try {
                        Message message = new Message(connection.readRawMessage());
                        // maintain statistics
                        receivedMessages.incrementAndGet();

                        receivedPayloadBytes += message.getPayload().length;

                        // check if we need to process a chunked message
                        boolean isChunk = message.isChunkMessage();
                        if (isChunk) {
                            receivedChunkMessages.incrementAndGet();
                            onChunkMessageReceived(message);
                        } else {
                            // notify all listeners
                            subscriber.message(message);
                        }

                    } catch (IOException e) {
                        // on connection failure the message receiver will
                        // transition to an inactive state
                        // failed connection are handled by the connection
                        // manager and this receiver will
                        // get a deactivate() call
                        log.debug("IOException ({}) while reading from connection: {}", e.getMessage(), connection);
                        deactivate();
                        try {
                            Thread.sleep(SLEEP_TIME_ON_IO_FAILURE);
                        } catch (InterruptedException e1) {
                            break; // got interrupted, we exit
                        }
                    }
                }
                log.debug("Stopped receiver for connection: {}", connection);
            }
        }

    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MessageReceiver<?> receiver = (MessageReceiver<?>) o;

        if (connection != null ? !connection.equals(receiver.connection) : receiver.connection != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return connection != null ? connection.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MessageReceiver{Connection=" + connection + "}";
    }

}
