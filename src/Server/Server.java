package Server;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

//Fonctionnement de base inspiré de tutorials.jenkov.com/java-nio/


public class Server {

    private int port = 3333;
    private BlockingQueue<User> connexionQueue;
    private ClientsThread clientsManager;
    private ServerSocketChannel serverSocketChannel;

    public void initiate() throws Exception{

        connexionQueue = new ArrayBlockingQueue(1024);

        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);

        this.clientsManager = new ClientsThread(connexionQueue);

        Thread clientsThread = new Thread(this.clientsManager);
        clientsThread.start();

        while (true) {

            SocketChannel clientChannel = this.serverSocketChannel.accept();
            if (clientChannel != null) {
                this.connexionQueue.add(new User(clientChannel));
            }
        }

    }



}
