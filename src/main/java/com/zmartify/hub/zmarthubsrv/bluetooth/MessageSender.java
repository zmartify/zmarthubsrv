package com.zmartify.hub.zmarthubsrv.bluetooth;

import static com.zmartify.hub.zmarthubsrv.ZmartBTServerClass.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * The message sender simply queues messages that are going to be sent over the
 * IBlaubotConnection for which this message sender was created for.
 * 
 * The sender can be activated/deactivated, meaning stopping and starting a
 * queue consuming thread that serializes and sends the queued messages (if any)
 * over the given IBlaubotConnection.
 *
 * TODO: handle failing connections
 */
public class MessageSender {

    private static Logger log = LoggerFactory.getLogger(MessageReceiver.class);

    /**
     * The connection over which the messages are send
     */
    private BluetoothConnection connection = null;

    // private MessageListener subscriber = null;

    private static AtomicInteger sentMessages = new AtomicInteger(0);
    private static AtomicInteger chunkIdGenerator = new AtomicInteger(0);

    private long sentPayloadBytes = 0;

    private MessageListener subscriber = null;

    public MessageSender() {
    }

    public void activate(BluetoothConnection newConnection) {
        log.info("Bluetooth message sender activated");
        this.connection = newConnection;
    }

    public void deactivate() {
        this.connection = null;
    }

    /**
     * @return sent bytes so far
     */
    public long getSentPayloadBytes() {
        return sentPayloadBytes;
    }

    /**
     * sent messages
     *
     * @return sent messages so far
     */
    public int getSentMessages() {
        return sentMessages.get();
    }

    /**
     * The connection that is managed by this message sender.
     *
     * @return the managed connection
     */
    protected BluetoothConnection getConnection() {
        return connection;
    }

    /**
     * Queues the given message to be sent over the IBluletoothConnection this
     * object was created with.
     *
     * @param message
     *            the message to be send
     * @throws IOException
     */
    public void sendMessage(Message message) throws IOException {
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
        connection.writeRawMessage(message.toByteArray());
    }

    public Observer<Message> messageListener() {
        return new Observer<Message>() {

            @Override
            public void onSubscribe(Disposable d) {
                log.info("Started sender for connection " + connection + " :: " + d.isDisposed());
            }

            @Override
            public void onNext(Message messageToSend) {
                try {
                    log.info("Sending message: " + messageToSend);

                    sendMessage(messageToSend);

                    // maintain statistics
                    sentMessages.incrementAndGet();
                    sentPayloadBytes = +messageToSend.getPayload().length;

                } catch (IOException | NullPointerException e) {
                    log.warn(
                            "Trying to send message without being connected - message back on queue and wait {} seconds",
                            (WAIT_TIME_ON_FAILED_SEND / 1000));
                    // back to queue on fail
                    subscriber.message(messageToSend);
                    try {
                        // wait an amount of time to mitigate busy waits on
                        // failed connections
                        Thread.sleep(WAIT_TIME_ON_FAILED_SEND);
                    } catch (InterruptedException interruptedException) {
                        // Just continue
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                log.error("onError " + e.getMessage(), e);
            }

            @Override
            public void onComplete() {
                // TODO Auto-generated method stub
            }

        };
    }

    public void register(MessageListener listener) {
        log.debug("Register messageListener : " + listener);
        this.subscriber = listener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MessageSender that = (MessageSender) o;

        if (connection != null ? !connection.equals(that.connection) : that.connection != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return connection != null ? connection.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "BlaubotMessageSender{" + "blaubotConnection=" + connection + '}';
    }

    public boolean isConnected() {
        return (connection != null);
    }
}
