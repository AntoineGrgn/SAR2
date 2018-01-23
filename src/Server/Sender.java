package Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class Sender implements Runnable {

    private Selector writeSelector;
    private UsersList usersMap;

    public Sender(UsersList usersMap, Selector writeSelector) {
        this.usersMap = usersMap;
        this.writeSelector = writeSelector;
    }

    @Override
    public void run() {
        while(true) {
            try {
                readUsersMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void readUsersMessages() throws IOException {

        int messages = this.writeSelector.selectNow();

        if (messages > 0) {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //TODO : ajouter le sélecteur WRITE sur la socket
            Set<SelectionKey> selectedKeys = this.writeSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while(keyIterator.hasNext()) {
                System.out.println("boucle while sender");
                SelectionKey key = keyIterator.next();
                User user = (User) key.attachment();
                user.sendMessages();


                keyIterator.remove();
            }
            selectedKeys.clear();
        }
    }
}
