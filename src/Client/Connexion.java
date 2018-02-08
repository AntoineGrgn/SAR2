package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Connexion {

    private Charset charSet = Charset.forName("UTF-8");
    private SocketChannel clientChannel;

    protected Connexion() throws IOException {

        clientChannel = SocketChannel.open();
        //clientChannel.configureBlocking(false);
    }

    protected void connectServer() throws IOException {
        String server = "localhost";
        int servPort = 3333;

        if (!clientChannel.connect(new InetSocketAddress(server, servPort))) {
            while (!clientChannel.finishConnect()) {
                System.out.print("Tentative de connexion");
            }
        }
    }

    protected void sendMessage(Message message) throws IOException {
        //Ajouter des tags à chaque message pour identifier son type
        ByteBuffer buf = message.serializeToByteBuffer();
        while (buf.hasRemaining()) {
            clientChannel.write(buf);
        }
    }

    protected void receiveMessages() throws IOException {
        //System.out.println("Receiving message");
        ByteBuffer bufType = ByteBuffer.allocate(Integer.BYTES);
        while (bufType.remaining()!=0) this.clientChannel.read(bufType);
        bufType.flip();
        int type = bufType.getInt();

        ByteBuffer bufId = ByteBuffer.allocate(Integer.BYTES);
        while (bufId.remaining()!=0) this.clientChannel.read(bufId);
        bufId.flip();
        int id = bufId.getInt();

        ByteBuffer bufLen= ByteBuffer.allocate(Integer.BYTES);
        while (bufLen.remaining()!=0) this.clientChannel.read(bufLen);
        bufLen.flip();
        int len = bufLen.getInt();

        ByteBuffer buf= ByteBuffer.allocate(len);
        while (buf.remaining()!=0) this.clientChannel.read(buf);
        buf.flip();
        String str = new String(buf.array(), charSet);

        switch (MessageType.fromInt(type)){
            case MESSAGE:
                System.out.println("[" + id + "]: " + str);
                break;
            case CHATROOMLIST:
                System.out.println("Chatroomlist received, longueur " + len + " : " + str);
                break;
            case USERLIST:
                System.out.println("Userslist received, longueur " + len + " : " + buf.getInt());
                break;
        }
        System.out.println("Message received : " + MessageType.fromInt(type) + " from " + id + ", longueur " + len + " : " + str);


//        switch (type) {
//            case
//        }
        //handleMessage(new Message(MessageType.fromInt(type), ))

    }

    protected void disconnectServer() throws IOException {
        clientChannel.close();

    }

    private void handleMessage(Message message) {
        //TODO : handle message coté client
    }
}
