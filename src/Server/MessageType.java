package Server;

public enum MessageType {

    MESSAGE(0, "Message"),
    PSEUDO(1, "Pseudo"),
    JOIN(2, "Join room"),
    ERROR(3, "Error"),
    CREATE(4, "Create room"),
    DELETE(5, "Delete room"),
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

    protected String serialize() {
        return Integer.toString(num);
    }

    protected static MessageType fromInt(int i) {
        for (MessageType m : MessageType.values()) {
            if (m.num == i) {
                return m;
            }
        }
        return null;
    }

    protected int toInt() {
        return this.num;
    }
}
