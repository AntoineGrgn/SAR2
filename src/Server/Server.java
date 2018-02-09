package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

//Fonctionnement de base inspiré de tutorials.jenkov.com/java-nio/


public class Server {

    private int port = 3333;

    public void initiate() throws IOException {

        //Nombre de connexions en attente possible
        BlockingQueue<User> connexionQueue = new ArrayBlockingQueue(10);


        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);

        Selector writeSelector = Selector.open();

        UsersList usersMap = new UsersList();
        ClientsThread clientsManager = new ClientsThread(connexionQueue, usersMap);
        Sender sender = new Sender(writeSelector);
        Thread senderThread = new Thread(sender);
        Thread clientsThread = new Thread(clientsManager);
        senderThread.start(); //Thread gérant l'envoi des messages sur les sockets (selecteur write)
        clientsThread.start(); //Thread gérant les connexions et la réception des messages (selecteur read)

        while (true) {

            SocketChannel clientChannel = null;
            try {
                clientChannel = serverSocketChannel.accept();
            } catch (IllegalStateException e) {
                System.err.println("Serveur saturé - Refus d'une connexion");
            }
            if (clientChannel != null) {
                //Réceptionne les demandes de connexion et les transmet au thread ClientThread
                connexionQueue.add(new User(clientChannel, writeSelector));
            }
        }

    }



}
