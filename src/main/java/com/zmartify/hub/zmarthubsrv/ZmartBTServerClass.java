package com.zmartify.hub.zmarthubsrv;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class ZmartBTServerClass {

    public final static int HTTP_CUSTOM_ABORTED = 299;
    public final static String HTTP_CUSTOM_ABORTED_TEXT = "Operation aborted";

    public final static byte PROTOCOL_VERSION = 1;

    /**
     * A monitor that can be used to avoid concurrent bluetooth operations due to
     * some bad bluetooth adapters that will fail when stressed.
     */
    public static final Semaphore BLUETOOTH_ADAPTER_LOCK = new Semaphore(3);

    /**
     * Charset used for Strings
     */
    public static final Charset STRING_CHARSET = Charset.forName("UTF-8");

    /**
     * The default byte order used within all Blaubot serializations
     */
    public static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    /**
     * Current version of the message schema (used in every BlaubotMessage's header
     */
    public static final byte MESSAGE_SCHEMA_VERSION = (byte) 0;

    /**
     * Max value of an unsigned short
     */
    public static final int USHORT_MAX_VALUE = 0xffff;

    /**
     * Maximum payload size for BlaubotMessages in bytes. == (max unsigned short
     * value) - full blaubot message header length.
     *
     * Every BlaubotMessage with bigger payload than this constant will be
     * automatically chunked by the MessageSenders and MessageReceivers.
     *
     */
    public static final int MAX_PAYLOAD_SIZE = 60000;

    /**
     * The service name used for all RFCOMM based acceptors
     */
    public static final String BLUETOOTH_SERVICE_NAME = "ZmartifyHubServer";
    public static final String BLUETOOTH_CONNECTION_UUID = "0000110100001000800000805F9B34FB";
    public final static String BLUETOOTH_LOCALHOST = "localhost";

    /**
     * Constants for message sending
     * 
     */
    public static final long WAIT_TIME_ON_FAILED_SEND = 500;

    public final static byte MESSAGE_PROTOCOL_VERSION = 1;

    public static final int HEADER_CHUNK_LENGTH = 2 + 2;
    public static final int HEADER_PAYLOADSIZE_LENGTH = 2;

    public static final int BITINDEX_CONTAINSPAYLOAD = 4;
    public static final int BITINDEX_ISCHUNKMESSAGE = 5;

    public final static String MESSAGE_METHOD = "method";
    public final static String MESSAGE_URI = "url";

    public final static String MESSAGE_JSON_PATH = "path";
    public final static String MESSAGE_JSON_BODY = "body";

    public final static String MESSAGE_JSON_TYPE = "type";
    public final static String MESSAGE_JSON_STATUS_CODE = "status-code";
    public final static String MESSAGE_JSON_STATUS_TEXT = "status";
    public final static String MESSAGE_JSON_REQID = "reqId";
    public final static String MESSAGE_JSON_RESULT = "result";
    public final static String MESSAGE_JSON_RESULT_MESSAGE = "message";
    public final static String MESSAGE_JSON_RESULT_KIND = "kind";

    public final static String JSON_TYPE_ERROR = "error";
    public final static String JSON_TYPE_ABORT = "abort";
    public final static String JSON_TYPE_ASYNC = "async";
    public final static String JSON_TYPE_SYNC = "sync";

    public enum Method {
        POST(1),
        GET(2),
        PUT(3),
        PATCH(4),
        DELETE(5),
        UNKNOWN((byte) -1);
        private final byte value;

        private Method(int value) {
            this.value = (byte) value;
        }

        private static final List<Method> HTTPCommands = Arrays.asList(POST, GET, PUT, PATCH, DELETE);

        public static boolean isHTTPCommand(Method command) {
            return HTTPCommands.contains(command);
        }

        private static final Map<Byte, Method> typeByValue = new HashMap<Byte, Method>();

        static {
            for (Method m : Method.values())
                typeByValue.put(m.value, m);
        }

        public static Method fromString(String method) {
            return Method.valueOf(method.toUpperCase());
        }

        public static Method fromByte(byte value) {
            return typeByValue.get(value);
        }

        public byte get() {
            return value;
        }

    }

}
