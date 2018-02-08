package Server;

public class ChatRoom {

    private String name;
    private UsersList usersMap;
    private User owner = null;

    public ChatRoom(String name) {
        this.name = name;
        this.usersMap = new UsersList();
    }

    public ChatRoom(String name, User user) {
        this.name = name;
        this.owner = user;
        this.usersMap = new UsersList();
    }

    public ChatRoom(String def, UsersList usersMap) {
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

    protected void removeUser(User user) {
        this.usersMap.removeClient(user);
    }

    protected boolean deleteRoom(User user) {
        if (this.owner != null) {
            if (this.owner.equals(user)) {
                this.usersMap.usersMap.forEach((Integer k, User v) -> {
                    v.addMessageToQueue(new Message(MessageType.ERROR, "Chatroom supprimée", user.getUserId()));
                    v.changeRoom("default");
                });
                return true;
            }
        }
        user.addMessageToQueue(new Message(MessageType.ERROR, "Erreur : Impossible de supprimer le groupe", user.getUserId()));
        return false;
    }

}
