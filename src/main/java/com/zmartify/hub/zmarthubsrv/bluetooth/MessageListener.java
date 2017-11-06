package com.zmartify.hub.zmarthubsrv.bluetooth;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class MessageListener {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private final int ID;

    public MessageListener() {
        ID = COUNTER.getAndIncrement();
    }

    public abstract void message(Message msg);

    public abstract void error(Throwable throwable);

    @Override
    public String toString() {
        return String.format("Listener ID:%d:%s", ID, super.toString());
    }
}
