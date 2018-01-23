package Server;

import java.nio.channels.SocketChannel;
import java.util.*;

public class UsersList {

    protected Map<Integer, User> usersMap;


    protected UsersList() {
        usersMap = new HashMap<>();
    }


    protected void addClient(User user) {
        //TODO : exceptions si null
        usersMap.put(user.getUserId(), user);
    }

    protected void removeClient(User user) {
        System.out.println("Removing : " + user);
        System.out.println(this.usersMap.remove(user.getUserId(), user));
    }


    protected User getUser(int id) {
        return usersMap.get(id);
    }
    /*
    protected ArrayList<String> getClientList() {
        ArrayList<String> users = new ArrayList<String>();

        for (User u : usersMap){
            users.add(u.getUserName());
        }
        return users;
    }*/



}
