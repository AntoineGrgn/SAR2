package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
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
    private Sender sender;
    private ServerSocketChannel serverSocketChannel;
    private Selector writeSelector;

    public void initiate() throws IOException {

        //Nombre de connexions en attente possible
        connexionQueue = new ArrayBlockingQueue(10);


        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);

        writeSelector = Selector.open();

        UsersList usersMap = new UsersList();
        this.clientsManager = new ClientsThread(connexionQueue, usersMap);
        this.sender = new Sender(usersMap, writeSelector);
        Thread senderThread = new Thread(this.sender);
        Thread clientsThread = new Thread(this.clientsManager);
        senderThread.start();
        clientsThread.start();

        while (true) {

            SocketChannel clientChannel = null;
            try {
                clientChannel = this.serverSocketChannel.accept();
            } catch (IllegalStateException e) {
                System.err.println("Serveur saturé - Réessayer ultérieurement");
            }
            if (clientChannel != null) {
                this.connexionQueue.add(new User(clientChannel, writeSelector));
            }
        }

    }



}
