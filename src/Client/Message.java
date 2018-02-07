package Client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Message {

    private Charset charSet = Charset.forName("UTF-8");
    private byte[] message;
    private MessageType type;
    private int idFrom;
    private int length;

    public Message(MessageType type, String message, int id) {
        this.message = message.getBytes(charSet);
        this.idFrom = id;
        this.type = type;
        this.length = this.message.length;
    }

    public Message(MessageType type, String message) {
        this.message = message.getBytes(charSet);
        this.type = type;
        this.length = this.message.length;
    }

    protected String getMessage() {
        return new String(this.message, charSet);
    }

    protected MessageType getType() {
        return this.type;
    }

    protected ByteBuffer serializeToByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(2*Integer.BYTES+length);
        buf.putInt(type.getNum());
        buf.putInt(length); //Sécurité si l'utilisateur change manuellement la taille du message ?
        buf.put(ByteBuffer.wrap(message));
        buf.flip();
        //System.out.println(buf);
        return(buf);
    }

}
