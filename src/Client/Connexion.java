package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;

public class Connexion {

    private Charset charSet = Charset.forName("UTF-8");
    private SocketChannel clientChannel;
    private HashMap<Integer,String> usersMap = new HashMap<>();

    Connexion() throws IOException {

        clientChannel = SocketChannel.open();
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
        ByteBuffer buf = message.serializeToByteBuffer();
        while (buf.hasRemaining()) {
            clientChannel.write(buf);
        }
    }

    protected void receiveMessages() throws IOException {
        //Format <type (int)><idFrom (int)><messageLength (int)><message (parsing selon le type)>

        ByteBuffer bufType = ByteBuffer.allocate(Integer.BYTES);
        while (bufType.hasRemaining()) this.clientChannel.read(bufType);
        bufType.flip();
        int type = bufType.getInt();

        ByteBuffer bufId = ByteBuffer.allocate(Integer.BYTES);
        while (bufId.hasRemaining()) this.clientChannel.read(bufId);
        bufId.flip();
        int id = bufId.getInt();

        ByteBuffer bufLen= ByteBuffer.allocate(Integer.BYTES);
        while (bufLen.hasRemaining()) this.clientChannel.read(bufLen);
        bufLen.flip();
        int len = bufLen.getInt();

        ByteBuffer buf= ByteBuffer.allocate(len);
        String str;
        while (buf.hasRemaining()) this.clientChannel.read(buf);
        buf.flip();

        switch (MessageType.fromInt(type)){
            case MESSAGE:
                str = new String(buf.array(), charSet);
                System.out.println("" + usersMap.get(id) + ": " + str);
                break;
            case CHATROOMLIST:
                //Liste des rooms au format <nameLength (int)><name (utf8)>
                System.out.println("Liste des chatrooms disponibles:");
                while(buf.hasRemaining()){
                    int nameLen = buf.getInt();
                    byte[] nameArray = new byte[nameLen];
                    buf.get(nameArray,0,nameLen);
                    System.out.println(new String(nameArray, charSet));
                }
                break;
            case USERLIST:
                //Liste des utilisateurs au format <id (int)><nameLength (int)><name (utf8)>
                System.out.println("Liste des utilisateurs dans la chatroom:");
                while(buf.hasRemaining()){
                    int userId = buf.getInt();
                    int nameLen = buf.getInt();
                    byte[] nameArray = new byte[nameLen];
                    buf.get(nameArray,0,nameLen);
                    String userName = new String(nameArray, charSet);
                    System.out.println(userName);
                    usersMap.put(userId,userName);
                }
                break;
            case ERROR:
                str = new String(buf.array(), charSet);
                System.out.println(str);
                break;
            case JOIN:
                str = new String(buf.array(), charSet);
                System.out.println("Vous avez rejoint la chatroom " + str);
                break;
        }
    }
}
