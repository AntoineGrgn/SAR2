package Client;

public enum MessageType {

    MESSAGE(0, "Message"),
    PSEUDO(1, "Pseudo"),
    JOIN(2, "Join room"),
    ERROR(3, "Error"),
    CREATE(4, "Create Room"),
    DELETE(5, "Delete Room"),
    USERLIST(6, "Get Users List"),
    CHATROOMLIST(7, "Get chatrooms list");

    private int num;
    private String name;

    MessageType(int i, String n) {
        this.num = i;
        this.name = n;
    }

    public String toString() {
        return name;
    }

    protected int getNum() {
        return num;
    }

    protected static MessageType fromInt(int i) {
        for (MessageType m : MessageType.values()) {
            if (m.num == i) {
                return m;
            }
        }
        return null;
    }
}