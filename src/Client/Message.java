package Client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Message {

    private Charset charSet = Charset.forName("UTF-8");
    private String message;
    private MessageType type;
    private String strFrom;
    private int length;

    public Message(MessageType type, String message, String pseudoFrom) {
        this.message = message;
        this.strFrom = pseudoFrom;
        this.type = type;
        this.length = message.length();
    }

    public Message(MessageType type, String message) {
        this.message = message;
        this.type = type;
        this.length = message.length();
    }

    protected String getMessage() {
        return this.message;
    }

    protected String getStrFrom() {
        return this.strFrom;
    }

    protected MessageType getType() {
        return this.type;
    }

    protected ByteBuffer serializeToByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(2*Integer.BYTES+length);
        buf.putInt(type.getNum());
        buf.putInt(length);
        buf.put(ByteBuffer.wrap(message.getBytes(charSet)));
        buf.flip();
        //System.out.println(buf);
        return(buf);
    }
}
