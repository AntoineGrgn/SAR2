package Client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Message {

    private Charset charSet = Charset.forName("UTF-8");
    private String message;
    private MessageType type;
    private int idFrom;
    private int length;

    public Message(MessageType type, String message, int id) {
        this.message = message;
        this.idFrom = id;
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
