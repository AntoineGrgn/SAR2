package Client;

import java.util.Scanner;

public class Client {

    public static void main(String args[]) throws Exception {

        Scanner scan = new Scanner(System.in);
        boolean boucle = true;

        Connexion connexion = new Connexion();
        connexion.connectServer();

        Receiver receiver = new Receiver(connexion);
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();

        System.out.print("Choisissez votre pseudo : ");
        String pseudo = scan.nextLine();
        //TODO : vérification sur l'input
        connexion.sendMessage(new Message(MessageType.PSEUDO, pseudo));

        while (true) {
            if (scan.hasNextLine()) {
                String input = scan.nextLine();
                //TODO : vérification sur l'input
                if (input.equals("chatroom1")) {
                    connexion.sendMessage(new Message(MessageType.JOIN, input));
                } else if (input.equals("chatroom2")) {
                    connexion.sendMessage(new Message(MessageType.JOIN, input));
                } else {
                    Message message = new Message(MessageType.MESSAGE, input);
                    connexion.sendMessage(message);
                }
            }
        }
        //connexion.disconnectServer();

    }
}