package Server;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class Message {

    private String message;

    private MessageType type;
    private Integer idFrom;
    private int messageLength;
    protected ByteBuffer headerBuf;

    protected ByteBuffer messageBuf;
    private Charset charSet = Charset.forName("UTF-8");


    public Message(MessageType t, String m, int id) {
        this.message = m;
        this.idFrom = id;
        this.type = t;
        this.messageLength = message.length();
    }

    public Message(UsersList list) {
        this.type = MessageType.USERLIST;
        this.idFrom = 0;
        String message = new String();
        for (Map.Entry<Integer, User> entry : list.usersMap.entrySet()) {
            Integer id = entry.getKey();
            User user = entry.getValue();
            message += id.toString() + user.getNameLength().toString() + user.getUserName();
            System.out.println("id : " + id + " length : " + user.getNameLength() + " name : " + user.getUserName());
        }
        this.message = message;
        this.messageLength = message.length();
    }

    public Message() {
        this.headerBuf = ByteBuffer.allocate(2*Integer.BYTES);
    }

    protected String getMessage() {
        return this.message;
    }

    protected int getMessageLength() {
        return this.messageLength;
    }

    protected int getIdFrom() {
        return this.idFrom;
    }

    protected MessageType getType() {
        return this.type;
    }

    public void setMessage(ByteBuffer message) {
        this.message = new String(message.array(), charSet);
    }

    protected void setHeader(int userId) {
        headerBuf.flip();
        this.type = MessageType.fromInt(headerBuf.getInt());
        this.messageLength = headerBuf.getInt();
        this.idFrom = userId;
    }

    protected ByteBuffer messageToByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(3*Integer.BYTES + message.length());
        buf.put(Utils.intToByteArray(type.toInt()));
        buf.put(Utils.intToByteArray(idFrom));
        buf.put(Utils.intToByteArray(message.length()));
        buf.put(message.getBytes(charSet));
        buf.flip();
        return buf;
    }

    protected void clear() {
        this.message = null;
        this.type = null;
        this.idFrom = null;

        this.headerBuf = ByteBuffer.allocate(2*Integer.BYTES);
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", type=" + type +
                ", idFrom=" + idFrom +
                '}';
    }
}
