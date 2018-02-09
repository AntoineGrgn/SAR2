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
    private Integer userId;
    private String room;

    private Message currentMessage; //Stocke un message en cours de lecture
    private ByteBuffer currentBuffer; //Stocke un message en cours d'écriture

    private UsersList usersMap; //Stocke les autres utilisateurs de la chatroom
    private Map<String, ChatRoom> rooms; //Stocke la liste des chatrooms

    private Charset charSet = Charset.forName("UTF-8");



    User(SocketChannel chan, Selector selector) {
        this.channel = chan;
        this.currentMessage = new Message();
        this.messages = new ArrayBlockingQueue(20);
        this.currentBuffer = ByteBuffer.allocate(0);
        this.writeSelector = selector;
    }

    protected SocketChannel getSocketChannel() {
        return channel;
    }

    protected String getUserName() {
        return userName;
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
    }

    protected void setRooms(Map<String, ChatRoom> rooms) {
        this.rooms = rooms;
    }

    protected void readMessage() throws IOException {
        //Format lecture :  <type (int)><messageLength (int)><message (parsing selon le type)>

        //TODO : gérer read() = -1
        if (currentMessage.headerBuf.hasRemaining()) {
            //Si le header du message en cours de lecture n'est pas complet, on continue de lire le header
            channel.read(currentMessage.headerBuf);
            if (currentMessage.headerBuf.hasRemaining()) return;
            //Si le header est complet, mise en forme du message, allocation du buffer pour la lecture de la payload
            //La longueur de la payload est contenue dans l'entête
            currentMessage.setHeader(userId);
            currentMessage.messageBuf = ByteBuffer.allocate(currentMessage.getMessageLength());
        }
        channel.read(currentMessage.messageBuf);
        //lecture de la payload
        if (!currentMessage.messageBuf.hasRemaining()) {
            //Une fois que le message est complet, gestion du message
            currentMessage.setMessage(currentMessage.messageBuf);
            handleMessage(currentMessage);
            clearCurrentMessage();
        }
    }

    protected void readCompleteMessage() throws IOException {
        /**
         *** Deprecated ***
         * Reads a complete message at once
         * Shouldn't be used - use readMessage instead
         */
        System.out.println("Reading message : ");
        ByteBuffer bufType = ByteBuffer.allocate(Integer.BYTES);
        int n = 0;
        while (n != Integer.BYTES) n += channel.read(bufType);
        bufType.flip();
        int type = bufType.getInt();

        ByteBuffer bufLen = ByteBuffer.allocate(Integer.BYTES);
        channel.read(bufLen);
        bufLen.flip();
        int len = bufLen.getInt();

        ByteBuffer buf = ByteBuffer.allocate(len);
        channel.read(buf);
        String str = new String(buf.array(), charSet);

        handleMessage(new Message(MessageType.fromInt(type), str, userId));

    }

    private void handleMessage(Message m) {

        MessageType t = m.getType();
        switch (t) {
            case MESSAGE:
                broadcastMessage(m);
                break;
            case PSEUDO:
                //Message reçu seulement au début de la connexion
                User user = usersMap.getUser(m.getIdFrom());
                user.setUserName(m.getMessage());
                this.rooms.get("default").broadcastUsersList(); //on lui envoie la liste des utilisateurs dans la room default à laquelle il vient de se connecter
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
                    addMessageToQueue(new Message(MessageType.ERROR, "Erreur : Room déjà existante", userId));
                }
                break;
            case DELETE:
                //deleteRoom mets les utilisateurs actuels de la room dans la room default
                if (rooms.get(m.getMessage()).deleteRoom(this))
                    rooms.remove(m.getMessage());
                break;
            case USERLIST:
                addMessageToQueue(new Message(this.usersMap));
                break;
            case CHATROOMLIST:
                addMessageToQueue(new Message(this.rooms.keySet()));
                break;
            default:
                System.err.println("Type de message non géré : " + t);
        }
    }

    protected void changeRoom(String room) {
        if (rooms.containsKey(room)) {
            if (!this.usersMap.equals(null))
                //A l'initialisation de la connexion, utilisation de changeRoom pour mettre l'utilisateur dans default, mais le user n'avait pas encore de usersMap
                //Sinon, on le retire de la liste d'utilisateurs précédente
                this.usersMap.removeClient(this);
            ChatRoom r = rooms.get(room);
            r.addUser(this);
            this.room = room;
            addMessageToQueue(new Message(MessageType.JOIN, room, 0));
            r.broadcastUsersList();

        } else {
            addMessageToQueue(new Message(MessageType.ERROR, "Erreur : Room inexistante", userId));
        }
    }

    private void clearCurrentMessage() {
        //Clear le message en cours de reception
        this.currentMessage = new Message();
    }

    private void broadcastMessage(Message message) {
        System.out.println("Broadcasting message");
        this.usersMap.usersMap.forEach((Integer k, User v) -> v.addMessageToQueue(message));
    }

    protected void addMessageToQueue(Message m) {
        try {
            messages.add(m);
            System.out.println("ajout d'un message à la queue de " + this.userId + " : " + messages + ", queue size:" + this.messages.size());
            SelectionKey key = this.channel.register(this.writeSelector, SelectionKey.OP_WRITE);
            key.attach(this);
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
            //Si un message était en cours d'envoi, envoi de la fin
            try {
                sendRemaining();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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
                //S'il n'y a plus de messages dans la queue, on retire le selecteur d'écriture
                channel.keyFor(writeSelector).cancel();
            }
        }
    }

    private void sendRemaining() throws IOException {
        //Reprise de l'envoi d'un message
        System.out.println("Envoi restant du message");
        channel.write(currentBuffer);
    }

    private void sendMessage(Message message) throws IOException {
        //TODO : write
        ByteBuffer buf = message.messageToByteBuffer();
        System.out.println("Envoi du message à " + this.userId + " : " + message.toString() + ", messages restant dans la queue : " + messages.size());
        channel.write(buf);
        if (buf.hasRemaining())
            //Si tout le message n'est pas envoyé en une fois
            this.currentBuffer = buf;
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