package Server;

public class MainServer {

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.initiate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
