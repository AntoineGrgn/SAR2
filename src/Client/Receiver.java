package Client;

import java.io.IOException;

public class Receiver implements Runnable {

    private Connexion connexion;

    Receiver(Connexion conn) {
        this.connexion = conn;
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.connexion.receiveMessages();
            } catch (IOException e) {
                System.out.println("Serveur indisponible - Relancer le client");
                System.exit(0);
            }
        }
    }
}
