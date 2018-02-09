package Client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Message {

    private Charset charSet = Charset.forName("UTF-8");
    private byte[] message;
    private MessageType type;
    private int length;


    Message(MessageType type, String message) {
        this.message = message.getBytes(charSet);
        this.type = type;
        this.length = this.message.length;
    }

    protected ByteBuffer serializeToByteBuffer() {
        //Construit le buffer contenant le message complet
        //Format <type (int)><messageLength (int)><message (parsing selon le type)>
        ByteBuffer buf = ByteBuffer.allocate(2*Integer.BYTES+length);
        buf.putInt(type.getNum());
        buf.putInt(length); //Sécurité si l'utilisateur change manuellement la taille du message ?
        buf.put(ByteBuffer.wrap(message));
        buf.flip();
        return(buf);
    }

}
