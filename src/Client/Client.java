package Client;

import java.util.Objects;
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
        System.out.println("Tapez !help pour afficher les commandes disponibles");
        connexion.sendMessage(new Message(MessageType.PSEUDO, pseudo));

        while (true) {
            if (scan.hasNextLine()) {
                String input = scan.nextLine();
                if (Objects.equals(input, "!help")) {
                    System.out.println("Liste des commandes disponibles");
                    System.out.println("!join <nom> : rejoindre la chatroom nom");
                    System.out.println("!create <nom> : crérer la chatroom nom");
                    System.out.println("!delete <nom> : supprime la chatroom nom (si vous l'avez créée)");
                    System.out.println("!listRooms : liste les chatrooms existantes");
                    System.out.println("!listUsers : liste les utilisateurs présents dans la chatroom");
                }
                else if (input.startsWith("!join")) {
                    if(input.substring(5).length()>0){
                        connexion.sendMessage(new Message(MessageType.JOIN, input.substring(6)));
                    }
                    else {
                        System.out.println("Veuillez préciser le nom de la room");
                    }
                } else if (input.startsWith("!create")) {
                    if(input.substring(7).length()>0){
                        connexion.sendMessage(new Message(MessageType.CREATE, input.substring(8)));
                    }
                    else {
                        System.out.println("Veuillez préciser le nom de la room");
                    }
                } else if (input.startsWith("!delete")) {
                    if(input.substring(7).length()>0){
                        connexion.sendMessage(new Message(MessageType.DELETE, input.substring(8)));
                    }
                    else {
                        System.out.println("Veuillez préciser le nom de la room");
                    }
                } else if (input.startsWith("!listRooms")) {
                    connexion.sendMessage(new Message(MessageType.CHATROOMLIST,""));
                } else if (input.startsWith("!listUsers")) {
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