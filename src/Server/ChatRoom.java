package Server;

import java.util.HashMap;

public class ChatRoom {

    private String name;
    private UsersList usersMap;
    private User owner = null;

    ChatRoom(String name) {
        this.name = name;
        this.usersMap = new UsersList();
    }

    ChatRoom(String name, User user) {
        this.name = name;
        this.owner = user;
        this.usersMap = new UsersList();
    }

    ChatRoom(String def, UsersList usersMap) {
        this.name = def;
        this.usersMap = usersMap;
    }

    protected void addUser(User user) {
        this.usersMap.addClient(user);
        user.setUsersMap(usersMap);
    }

    protected void broadcastUsersList() {
        this.usersMap.usersMap.forEach((Integer k, User v) -> {
            v.addMessageToQueue(new Message(this.usersMap));
        });
    }

    protected boolean deleteRoom(User user) {
        if (this.owner != null) {
            if (this.owner.equals(user)) {

                HashMap<Integer, User> copy = new HashMap<>(this.usersMap.usersMap);
                //Utilisation d'une copie de la usersMap pour éviter un problème d'accès concurrents
                copy.forEach((Integer k, User v) ->
                        v.addMessageToQueue(new Message(MessageType.ERROR, "Chatroom supprimée", user.getUserId())));
                copy.forEach((Integer k, User v) ->
                        v.changeRoom("default"));
                //Utilisation de 2 boucles pour avoir les messages dans le bon ordre à l'affichage
                return true;
            }
        }
        user.addMessageToQueue(new Message(MessageType.ERROR, "Erreur : Impossible de supprimer le groupe", user.getUserId()));
        return false;
    }

}
