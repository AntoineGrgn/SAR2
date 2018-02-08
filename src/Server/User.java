package Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

public class User {

    private ArrayBlockingQueue<Message> messages;
    private SocketChannel channel;
    private Selector writeSelector;
    private String userName;
    private Integer nameLength;
    private Integer userId;
    private String room;

    private Message currentMessage;
    private ByteBuffer currentBuffer;

    private UsersList usersMap;
    private Map<String, ChatRoom> rooms;

    private Charset charSet = Charset.forName("UTF-8");



    protected User(SocketChannel chan, Selector selector) {
        this.channel = chan;
        this.currentMessage = new Message();
        this.messages = new ArrayBlockingQueue(20);
        this.currentBuffer = ByteBuffer.allocate(0);
        this.writeSelector = selector;
        this.room = "default";
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

    public ArrayBlockingQueue<Message> getMessages() {
        return messages;
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
            //System.out.println("header buf.remaining != 0");
            channel.read(currentMessage.headerBuf);
            if (currentMessage.headerBuf.remaining() != 0) return;
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
                System.out.println("Message reçu, broadcasting");
                broadcastMessage(m);
                break;
            case PSEUDO:
                User user = usersMap.getUser(m.getIdFrom());
                user.setUserName(m.getMessage());
                sendUsersList(user);
                break;
            case JOIN:
                String room = m.getMessage();
                changeRoom(room);
                break;
            case CREATE:
                String newRoom = m.getMessage();
                if (!rooms.containsKey(newRoom)) {
                    ChatRoom chatRoom = new ChatRoom(newRoom, this);
                    rooms.put(newRoom, chatRoom);
                    changeRoom(newRoom);
                } else {
                    addMessageToQueue(new Message(MessageType.ERROR, "Room déjà existante", userId));
                }
                break;
            case DELETE:
                if (rooms.get(this.room).deleteRoom(this))
                    rooms.remove(this.room);
                break;
            case USERLIST:
                addMessageToQueue(new Message(this.usersMap));
                break;
            case CHATROOMLIST:
                //TODO chatroomlist
                break;
            default:
                System.err.println("Message non géré : " + m.getMessage());
        }
    }

    protected void changeRoom(String room) {
        if (rooms.containsKey(room)) {
            this.usersMap.removeClient(this);
            rooms.get(room).addUser(this);
            this.room = room;
            addMessageToQueue(new Message(this.usersMap));
        } else {
            addMessageToQueue(new Message(MessageType.ERROR, "Room inexistante", userId));
        }
    }

    private void clearCurrentMessage() {
        this.currentMessage = new Message();
    }

    private void broadcastMessage(Message message) {
        this.usersMap.usersMap.forEach((Integer k, User v) -> {
            v.addMessageToQueue(message);
            System.out.println(message.toString());
        });
    }

    private void sendUsersList(User user) {
        //TODO : sendUsersList
    }

    protected void addMessageToQueue(Message m) {
        try {
            messages.add(m);
            System.out.println("addMessage messages : " + messages);
            System.out.println("addMessage queue size : " + messages.size());
            SelectionKey key = this.channel.register(this.writeSelector, SelectionKey.OP_WRITE);
            key.attach(this);
            System.out.println("write selector registered");
        } catch (IllegalStateException e) {
            System.err.println("File d'attente saturée - Message non traité");
            //TODO : informer le client ?
        } catch (ClosedChannelException e) {
            System.err.println("Envoi du message impossible - client déconnecté");
            usersMap.removeClient(this);
        }
    }

    protected void sendMessages() {
        if (currentBuffer.hasRemaining()) {
            try {
                sendRemaining();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("sendMessages queue size : " + messages.size());
            System.out.println("sendMessage messages : " + messages);
            Message m = null;
            try {
                m = messages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!m.equals(null)) {
                try {
                    sendMessage(m);
                } catch (IOException e) {
                    //TODO : gérer l'exception
                    e.printStackTrace();
                }
            } else {
                System.err.println("Appel de sendMessage sur une file vide");
            }
            if (messages.isEmpty()) {
                System.out.println("removing selector");
                channel.keyFor(writeSelector).cancel();
            }
        }
    }

    private void sendRemaining() throws IOException {
        System.out.println("has remaining");
        channel.write(currentBuffer);
    }

    private void sendMessage(Message message) throws IOException {
        //TODO : write
        System.out.println("début send Message");
        ByteBuffer buf = message.messageToByteBuffer();
        System.out.println("send message : " + buf.toString());
        channel.write(buf);
        if (buf.hasRemaining()) this.currentBuffer = buf;
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