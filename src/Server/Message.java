package Server;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

public class Message {

    private byte[] message; //TODO : longueur maximale de message ?

    private MessageType type;
    private Integer idFrom;
    private int messageLength;
    protected ByteBuffer headerBuf;

    protected ByteBuffer messageBuf;
    private Charset charSet = Charset.forName("UTF-8");


    Message(MessageType t, String m, int id) {
        //Constructeur générique pour transmettre une String
        this.message = m.getBytes(charSet);
        this.idFrom = id;
        this.type = t;
        this.messageLength = message.length;
    }

    Message(UsersList list) {
        //Constructeur pour un message de type UserList
        //Rempli la payload avec la liste des utilisateurs au format <id (int)><nameLength (int)><name (utf8)>
        this.type = MessageType.USERLIST;
        this.idFrom = 0;
        ByteBuffer temp = ByteBuffer.allocate(1024); //TODO : limitation au nombre de personnes dans une chatroom ?
        for (Map.Entry<Integer, User> entry : list.usersMap.entrySet()) {
            byte[] user = entry.getValue().getUserName().getBytes(charSet);
            int id = entry.getKey();
            temp.putInt(id);
            temp.putInt(user.length);
            temp.put(user);
        }
        temp.flip();
        ByteBuffer buf = ByteBuffer.allocate(temp.limit());
        buf.put(temp);
        byte[] message = buf.array();
        this.message = message;
        this.messageLength = message.length;
    }

    Message(Set<String> rooms) {
        //Constructeur pour un message de type RoomsList
        //Rempli la payload avec la liste des rooms au format <nameLength (int)><name (utf8)>
        this.type = MessageType.CHATROOMLIST;
        this.idFrom = 0;
        ByteBuffer temp = ByteBuffer.allocate(1024); //Comme pour la liste d'utilisateurs, ça limite la quantité de données que l'on peut envoyer
        for (String chatroom : rooms) {
            byte[] name = chatroom.getBytes(charSet);
            temp.putInt(name.length);
            temp.put(name);
        }
        temp.flip();
        ByteBuffer buf = ByteBuffer.allocate(temp.limit());
        buf.put(temp);
        byte[] message = buf.array();
        this.message = message;
        this.messageLength = message.length;
    }

    Message() {
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
        //A la reception d'un message, récupère le type, la longueur de la payload et l'id de l'expéditeur dans l'entête
        headerBuf.flip();
        this.type = MessageType.fromInt(headerBuf.getInt());
        this.messageLength = headerBuf.getInt();
        this.idFrom = userId;
    }

    protected ByteBuffer messageToByteBuffer() {
        //Construit le buffer contenant le message complet
        //Format <type (int)><idFrom (int)><messageLength (int)><message (parsing selon le type)>
        ByteBuffer buf = ByteBuffer.allocate(3*Integer.BYTES + messageLength);
        buf.putInt(type.toInt());
        buf.putInt(idFrom);
        buf.putInt(messageLength);
        buf.put(message);
        buf.flip();
        return buf;
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
