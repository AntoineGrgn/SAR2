package Server;

import java.util.*;

public class UsersList {

    protected Map<Integer, User> usersMap;

    UsersList() {
        usersMap = new HashMap<>();
    }

    protected void addClient(User user) {
        //TODO : exceptions si null
        usersMap.put(user.getUserId(), user);
    }

    protected void removeClient(User user) {
        System.out.println("Removing : " + user);
    }

    protected User getUser(int id) {
        return usersMap.get(id);
    }

}
