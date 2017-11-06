package com.zmartify.hub.zmarthubsrv.bluetooth;

import static com.zmartify.hub.zmarthubsrv.ZmartBTServerClass.*;

import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.bluetooth.BluetoothConsts;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.bluez5.BluezBluetoothProvider;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;

public class ZmartBTRfcommServer implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ZmartBTRfcommServer.class);
    // --- RxJava2
    protected ConnectableFlowable<Message> $receiver = null;
    protected MessageListener receiveListener;

    public Flowable<Message> receiveQueue = Flowable.create(emitter -> {
        MessageListener messageListener = new MessageListener() {

            @Override
            public void message(Message event) {
                emitter.onNext(event);
                if (event.isLast()) {
                    emitter.onComplete();
                }
            }

            @Override
            public void error(Throwable e) {
                emitter.onError(e);
            }

        };
        registerReceiveListener(messageListener);
    }, BackpressureStrategy.BUFFER);

    protected ConnectableFlowable<Message> $sender = null;
    protected MessageListener sendListener;

    public Flowable<Message> sendQueue = Flowable.create(emitter -> {
        MessageListener messageListener = new MessageListener() {

            @Override
            public void message(Message event) {
                emitter.onNext(event);
                if (event.isLast()) {
                    emitter.onComplete();
                }
            }

            @Override
            public void error(Throwable e) {
                emitter.onError(e);
            }

        };
        registerSendListener(messageListener);
    }, BackpressureStrategy.BUFFER);

    private BluetoothAcceptThread acceptThread = null;

    private BluezBluetoothProvider bluetoothProvider = new BluezBluetoothProvider();

    private BluetoothConnection connection = null;
    private MessageReceiver<Message> messageReceiver;
    private MessageSender messageSender;

    private MessageManager messageManager = new MessageManager();

    private LocalDevice localDevice = null;
    private final String localDeviceBluetoothAddress;

    private boolean started = true;
    private boolean listening = true;
    private boolean connected = true;

    public ZmartBTRfcommServer() {
        try {
            this.localDevice = LocalDevice.getLocalDevice();
        } catch (BluetoothStateException e) {
            log.error("Error getting local device information - bluetooth available?");
        }
        this.localDeviceBluetoothAddress = localDevice.getBluetoothAddress();
        this.messageReceiver = new MessageReceiver<Message>();
        this.messageSender = new MessageSender();
    }

    @Override
    public void run() {
        log.info("Bluetooth server is running " + BLUETOOTH_SERVICE_NAME);
        try {
            startListening();
        } catch (Throwable t) {
            log.error(t.toString(), t);
        }
    }

    public void startListening() {
        if (acceptThread != null) {
            stopListening();
        }

        $sender = sendQueue.publish();
        $sender.connect();
        $sender.subscribe(message -> getMessageSender().messageListener().onNext(message));

        $receiver = receiveQueue.publish();
        $receiver.connect();
        $receiver.subscribe(message -> messageManager.messageListener().onNext(message));

        acceptThread = new BluetoothAcceptThread(BluetoothConsts.RFCOMM_PROTOCOL_UUID);
        acceptThread.start();

        messageManager.startup();
        try {
            bluetoothProvider.startup();
        } catch (Exception e) {
            log.error("Error starting BluetoothProvider");
        }

        log.debug("Start listening for bluetooth clients...");
        listening = true;
    }

    public void stopListening() {
        log.debug("Stop listening for bluetooth clients ...");
        if (acceptThread != null) {
            log.debug("Interrupting and joining acceptThread ...");
            acceptThread.interrupt();
            try {
                acceptThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("AcceptThread stopped ...");
        }
        acceptThread = null;
        listening = false;

        $sender.connect().dispose();
        $sender.subscribe().dispose();

        $receiver.connect().dispose();
        $receiver.subscribe().dispose();

        messageManager.shutdown();
        try {
            bluetoothProvider.shutdown();
        } catch (Exception e) {
            log.error("Error stopping BluetoothProvider.");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        listening = false;

    }

    private class UnidentifiedDevice extends ZmartBTDevice {
        public UnidentifiedDevice() {
            super("UnidentifiedBluetoothDeviceFrom");
        }

        public void setUniqueDeviceId(String uniqueDeviceId) {
            this.uniqueDeviceId = uniqueDeviceId;
        }
    }

    public class BluetoothAcceptThread extends Thread {
        StreamConnectionNotifier streamConnectionNotifier;
        private UUID uuid = null;

        public BluetoothAcceptThread(UUID uuid) {
            this.uuid = uuid;
            setName("bluetooth-accept-thread");
        }

        @Override
        public void interrupt() {
            super.interrupt();
            if (this.streamConnectionNotifier == null) {
                return;
            }
            log.debug("Closing streamConnection...");
            try {
                this.streamConnectionNotifier.close();
            } catch (IOException e) {
                log.error("Closing streamConnection caused exception :: {}", e);
            }
        }

        @Override
        public void run() {

            String url = "btspp://" + BLUETOOTH_LOCALHOST + ":" + uuid + ";name=" + BLUETOOTH_SERVICE_NAME;
            started = true;

            log.debug("Bluetooth server Accept Thread starting...");

            // We will be visible for an unlimited time:
            try {
                LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
            } catch (BluetoothStateException e2) {
                log.error("Failed to start GIAC discoverable state: {}", e2.getMessage(), e2);
            }

            StreamConnectionNotifier service = null;
            try {
                service = (StreamConnectionNotifier) Connector.open(url, Connector.READ_WRITE);
                streamConnectionNotifier = service;
            } catch (IOException e) {
                log.error("Could not listen to RFCOMM :: {}", e.getMessage());
                started = false;
                throw new RuntimeException("Handle listen() failure");
            }

            while (!this.isInterrupted()) {
                StreamConnection socket = null;
                RemoteDevice dev = null;
                try {
                    log.debug("Creating bluetooth StreamConnection for incoming connections...");
                    listening = true;

                    socket = service.acceptAndOpen();
                    dev = RemoteDevice.getRemoteDevice(socket);
                } catch (IOException e) {
                    log.warn("ServerSocket accept faild. :: {}", e);
                }
                if (socket != null) {
                    // we got a connection gather unique id
                    UnidentifiedDevice device = new UnidentifiedDevice();
                    String readableName = null;
                    connection = new BluetoothConnection(device, socket);
                    String uniqueDeviceId = "";

                    uniqueDeviceId = "ZmartAPP";
                    device.setUniqueDeviceId(uniqueDeviceId);

                    log.debug("We received uniqueDeviceID: {}", uniqueDeviceId);

                    try {
                        readableName = dev.getFriendlyName(false);
                        if (readableName != null && !readableName.isEmpty()) {
                            device.setReadableName(readableName);
                        } else {
                            device.setReadableName(dev.getBluetoothAddress());
                        }
                    } catch (IOException e) {
                    }

                    log.debug("Got connection from client bluetoothDevice {} ({})", dev.getBluetoothAddress(),
                            device.getReadableName());

                    setConnected(connection);
                } else {
                    // We also end here, if we hit the limit of the max bluetooth connections on a
                    // bluetoothDevice!!
                    log.warn(
                            "Socket null - no client connected. This can happen if you hit the maximum connections supported by a bluetoothDevice's bluetooth hardware, on timeout or aborted calls.");
                    setDisconnected();
                    listening = false;
                }
            }
            listening = false;
            setDisconnected();

            started = false;
            log.debug("Accept Thread finished ...");
        }
    }

    public String getBluetoothAddress() {
        return localDeviceBluetoothAddress;
    }

    private void setConnected(BluetoothConnection connection) {
        connected = true;
        messageReceiver.activate(connection);
        messageSender.activate(connection);
    }

    private void setDisconnected() {
        connected = false;
        messageReceiver.deactivate();
        messageSender.deactivate();
        connection = null;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    void initialize() {
        // Called at end of constructor
        startListening();
    }

    public ConnectableFlowable<Message> get$Sender() {
        return $sender;
    }

    public ConnectableFlowable<Message> get$Receiver() {
        return $receiver;
    }

    public MessageListener getSendListener() {
        return sendListener;
    }

    public MessageListener getReceiveListener() {
        return receiveListener;
    }

    public void registerSendListener(MessageListener listener) {
        sendListener = listener;
        getMessageSender().register(listener);
        getMessageManager().register(listener);
    }

    public void registerReceiveListener(MessageListener listener) {
        receiveListener = listener;
        getMessageReceiver().register(listener);
    }

    protected MessageSender getMessageSender() {
        return messageSender;
    }

    protected MessageReceiver<Message> getMessageReceiver() {
        return messageReceiver;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

}
