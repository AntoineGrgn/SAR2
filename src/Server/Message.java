package Server;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

public class Message {

    private byte[] message; //TODO : longueur maximale de message ?

    private MessageType type;
    private Integer idFrom;
    private int messageLength;
    protected ByteBuffer headerBuf;

    protected ByteBuffer messageBuf;
    private Charset charSet = Charset.forName("UTF-8");


    public Message(MessageType t, String m, int id) {
        this.message = m.getBytes(charSet);
        this.idFrom = id;
        this.type = t;
        this.messageLength = message.length;
    }

    public Message(UsersList list) {
        //Constructeur pour un message de type UserList
        this.type = MessageType.USERLIST;
        this.idFrom = 0;
        int totalLength = 0;
        int n;
        ByteBuffer temp = ByteBuffer.allocate(1024);
        for (Map.Entry<Integer, User> entry : list.usersMap.entrySet()) {
            byte[] user = entry.getValue().getUserName().getBytes(charSet);
            n = Integer.BYTES*2 + user.length;
            //ByteBuffer temp = ByteBuffer.allocate(n);
            int id = entry.getKey();
            temp.putInt(id);
            temp.putInt(user.length);
            temp.put(user);
            System.out.println("Userslist message bytebuffer1 : " + temp);
            System.out.println("id : " + id + " length (byte): " + user.length + " user : " + new String(user, charSet));
            totalLength += n;
        }
        temp.flip();
        ByteBuffer buf = ByteBuffer.allocate(temp.limit());
        buf.put(temp);
        byte[] message = buf.array();
        System.out.println("UsersList message from byte[] : " + new String(message, charSet) + " array length : " + message.length);
        this.message = message;
        this.messageLength = message.length;
    }


    public Message() {
        this.headerBuf = ByteBuffer.allocate(2*Integer.BYTES);
    }

    protected String getMessage() {
        return new String(this.message, charSet);
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
        this.message = message.array();
    }

    protected void setHeader(int userId) {
        headerBuf.flip();
        this.type = MessageType.fromInt(headerBuf.getInt());
        this.messageLength = headerBuf.getInt();
        this.idFrom = userId;
    }

    protected ByteBuffer messageToByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(3*Integer.BYTES + messageLength);
        buf.putInt(type.toInt());
        buf.putInt(idFrom);
        buf.putInt(messageLength);
        buf.put(message);
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
