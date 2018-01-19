package Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

public class User {

    private SocketChannel channel;
    private String userName;
    private Integer nameLength;
    private Integer userId;

    private Message currentMessage;

    private UsersList usersMap;
    private Map<String, ChatRoom> rooms;

    private Charset charSet = Charset.forName("UTF-8");



    protected User(SocketChannel chan) {
        this.channel = chan;
        this.currentMessage = new Message();
    }

    protected SocketChannel getSocketChannel() {
        return channel;
    }

    protected String getUserName() {
        return userName;
    }

    protected Integer getNameLength() {
        return nameLength;
    }

    protected Integer getUserId() {
        return this.userId;
    }


    public void setUsersMap(UsersList usersMap) {
        this.usersMap = usersMap;
    }

    protected void setUserId(Integer id) {
        this.userId = id;
    }

    private void setUserName(String user) {
        this.userName = user;
        this.nameLength = user.length();
    }

    protected void setRooms(Map<String, ChatRoom> rooms) {
        this.rooms = rooms;
    }

    protected void readMessage() throws IOException {
        //TODO : gérer read() = -1
        if (currentMessage.headerBuf.remaining() != 0) {
            channel.read(currentMessage.headerBuf);
            if (currentMessage.headerBuf.remaining() != 0) return;
            System.out.println(currentMessage.headerBuf);
            currentMessage.setHeader(userId);
            currentMessage.messageBuf = ByteBuffer.allocate(currentMessage.getMessageLength());
        }
        channel.read(currentMessage.messageBuf);
        if (currentMessage.messageBuf.remaining() == 0) {
            currentMessage.setMessage(currentMessage.messageBuf);
            handleMessage(currentMessage);
            clearCurrentMessage();
        }
    }

    protected void readCompleteMessage() throws IOException {
        /**
         * Reads a complete message at once
         * Shouldn't be used - use readMessage instead
         */
        System.out.println("Reading message : ");
        ByteBuffer bufType = ByteBuffer.allocate(Integer.BYTES);
        int n = 0;
        while (n != Integer.BYTES) n += channel.read(bufType);
        bufType.flip();
        int type = bufType.getInt();

        System.out.println("type " + type);

        ByteBuffer bufLen = ByteBuffer.allocate(Integer.BYTES);
        channel.read(bufLen);
        bufLen.flip();
        int len = bufLen.getInt();

        System.out.println("len " + len);

        ByteBuffer buf = ByteBuffer.allocate(len);
        channel.read(buf);
        String str = new String(buf.array(), charSet);

        System.out.println("str" + str);

        handleMessage(new Message(MessageType.fromInt(type), str, userId));

    }

    private void handleMessage(Message m) throws IOException {

        MessageType t = m.getType();
        switch (t) {
            case MESSAGE:
                broadcastMessage(m);
                break;
            case PSEUDO:
                User user = usersMap.getUser(m.getIdFrom());
                user.setUserName(m.getMessage());
                sendUsersList(user);
                break;
            case JOIN:
                String room = m.getMessage();
                if (rooms.containsKey(room)) {
                    this.usersMap.removeClient(this);
                    rooms.get(room).addUser(this);
                } else {
                    sendMessage(new Message(MessageType.ERROR, "Room inexistante", userId), this);
                }
                break;
            case CREATE:
                String newRoom = m.getMessage();
                if (!rooms.containsKey(newRoom)) {
                    ChatRoom chatRoom = new ChatRoom(newRoom, this);
                    rooms.put(newRoom, chatRoom);
                    this.usersMap.removeClient(this);
                    chatRoom.addUser(this);
                } else {
                    sendMessage(new Message(MessageType.ERROR, "Room déjà existante", userId), this);
                }
            case DELETE:

            default:
                System.err.println("Message non géré : " + m.getMessage());
        }
    }

    private void clearCurrentMessage() {
        this.currentMessage.clear();
    }

    private void broadcastMessage(Message message) {
        this.usersMap.usersMap.forEach((Integer k, User v) -> {
            try {
                sendMessage(message, v);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendUsersList(User user) {
        //TODO : sendUsersList
    }

    private void sendMessage(Message message, User user) throws IOException {
        ByteBuffer buf = message.messageToByteBuffer();
        System.out.println("send message : " + buf.toString());
        SocketChannel channel = user.getSocketChannel();
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}