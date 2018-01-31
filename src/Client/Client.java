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
        while(pseudo.length()<1){
            System.out.print("Choisissez votre pseudo : ");
            pseudo = scan.nextLine();
        }
        System.out.println("Votre pseudo est: " + pseudo);
        System.out.println("Vous êtes dans la chatroom par défaut");
        connexion.sendMessage(new Message(MessageType.PSEUDO, pseudo));

        while (true) {
            if (scan.hasNextLine()) {
                String input = scan.nextLine();
                if (input.startsWith("join")) {
                    connexion.sendMessage(new Message(MessageType.JOIN, input.substring(5)));
                } else if (input.startsWith("create")) {
                    connexion.sendMessage(new Message(MessageType.CREATE, input.substring(7)));
                } else if (input.startsWith("delete")) {
                    connexion.sendMessage(new Message(MessageType.DELETE, input.substring(7)));
                } else if (input.startsWith("listRooms")) {
                    connexion.sendMessage(new Message(MessageType.CHATROOMLIST,""));
                } else if (input.startsWith("listUsers")) {
                    connexion.sendMessage(new Message(MessageType.USERLIST, ""));
                }
                else {
                    Message message = new Message(MessageType.MESSAGE, input);
                    connexion.sendMessage(message);
                }
            }
        }
        //connexion.disconnectServer();

    }
}