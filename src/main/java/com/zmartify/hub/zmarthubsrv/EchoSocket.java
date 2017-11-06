/**
 * 
 */
package com.zmartify.hub.zmarthubsrv;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * @author Peter Kristensen
 *
 *         Basic Echo Client Socket
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class EchoSocket {
    private final CountDownLatch closeLatch;
    @SuppressWarnings("unused")
    private Session session;

    public EchoSocket() {
        this.closeLatch = new CountDownLatch(1);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.printf("Got connect: %s%n", session);
        this.session = session;
        try {
            Future<Void> fut;
            fut = session.getRemote().sendStringByFuture("Hello");
            fut.get(2, TimeUnit.SECONDS); // wait for send to complete.

            fut = session.getRemote().sendStringByFuture("Thanks for the conversation.");
            fut.get(2, TimeUnit.SECONDS); // wait for send to complete.

            session.close(StatusCode.NORMAL, "I'm done");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        System.out.printf("Got msg: %s%n", msg);
    }

    /**
     * @param duration
     * @return
     * @throws InterruptedException
     */
    public boolean awaitClose(int duration) throws InterruptedException {
        // TODO Auto-generated method stub
        return this.closeLatch.await(duration, TimeUnit.SECONDS);
    }

}
