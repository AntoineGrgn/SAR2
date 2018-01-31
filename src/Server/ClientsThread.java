package Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class ClientsThread implements Runnable {

    private BlockingQueue<User> connexionQueue;

    private Selector readSelector;

    private Map<String, ChatRoom> rooms;
    private UsersList usersMap = null;
    private Integer userId = 1;

    private Charset charSet = Charset.forName("UTF-8");


    public ClientsThread(BlockingQueue<User> queue, UsersList usersMap) throws IOException {
        this.connexionQueue = queue;
        this.readSelector = Selector.open();
        this.rooms = new HashMap<>();
        this.usersMap = usersMap;
        this.rooms.put("default", new ChatRoom("default", usersMap));
        this.rooms.put("chatroom1", new ChatRoom("chatroom1"));
        this.rooms.put("chatroom2", new ChatRoom("chatroom2"));
    }



    @Override
    public void run() {
        while(true) {
            try {
                connectClient();
                readUsersMessages();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectClient() throws IOException {
        User newUser = this.connexionQueue.poll();

        while (newUser != null) {

            newUser.setUserId(this.userId++);
            newUser.setUsersMap(usersMap);
            newUser.setRooms(rooms);
            newUser.getSocketChannel().configureBlocking(false);

            rooms.get("default").addUser(newUser);
            this.usersMap.addClient(newUser);

            SelectionKey key = newUser.getSocketChannel().register(this.readSelector, SelectionKey.OP_READ);
            key.attach(newUser);

            newUser = this.connexionQueue.poll();

        }
    }

    private void readUsersMessages() throws IOException {

        int messages = this.readSelector.selectNow();

        if (messages > 0) {
            Set<SelectionKey> selectedKeys = this.readSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                User user = (User) key.attachment();
                try {
                    user.readMessage();
                    System.out.println("read user messages : " + user.getMessages());
                } catch (IOException e) {
                    usersMap.removeClient(user);
                    key.cancel();
                    //e.printStackTrace();
                }


                keyIterator.remove();
            }
            selectedKeys.clear();
        }
    }


}
