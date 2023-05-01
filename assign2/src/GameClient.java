import java.net.*;
import java.util.Scanner;
import java.util.UUID;
import java.io.*;

public class GameClient {
    BufferedReader input;
    PrintWriter output;
    String userName;
    UUID userToken;
    String hostname;
    int port;

    public GameClient(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    public void handleAuthentication(String userName){
        try{
            String fileName = "tokens/"+ userName + "token.txt";
            File tokenFile = new File(fileName);

            if( tokenFile.exists() ){
                System.out.println("Attempting to login as existing user... ");
                BufferedReader reader = new BufferedReader(new FileReader(tokenFile)) ;

                output.println(MessageType.LOGIN + ":" + reader.readLine());
                

                //reader.close();
            }
            System.out.println("No token found...");

            output.println(MessageType.REGISTRATION + ":" +userName);//send username to server
        }catch(IOException e){
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    public void handleRegistration(String token){
        try{
            String fileName = "tokens/"+ userName + "token.txt";
            
            userToken = UUID.fromString(token);
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(token);
            fileWriter.close();
            System.out.println("Saving token...");
            output.println(MessageType.SUCCESS);
        }catch(IOException e){
            System.out.println("I/O error: " + e.getMessage());
        }
    }
    
    public void run() {
        Scanner scanner = new Scanner(System.in);
 
        try (Socket socket = new Socket(hostname, port)) {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            String serverMessage;
            String fileName;
            File tokenFile;

            while (true){
                String response = input.readLine();
                String messageContent = null;
                if(response != null){
                    String[] parts = response.split(":",2);
                    MessageType message = MessageType.valueOf(parts[0]);
                    if (parts.length ==2){
                        messageContent = parts[1];
                    }

                    switch (message){
                        case WELCOME:
                            System.out.println("Enter username:");
                            userName = scanner.nextLine();
                            this.handleAuthentication(userName);
                            break;
                        case AUTHENTICATION_SUCESS:
                            System.out.println("Login attempt was successful, welcome back " + userName + "!");
                            break;
                        case AUTHENTICATION_FAILURE:
                            output.println(MessageType.REGISTRATION + ":" + userName);
                            System.out.println("Login attempt was unsuccessful...");
                            System.out.println("Attempting to register...");
                            break;
                        case AUTHENTICATION_RESPONSE:
                            if( messageContent != null){
                                this.handleRegistration(messageContent);
                                System.out.println("Registration was sucessfull, hello " + userName + "!");
                            }else{
                                System.out.println("Registration was unsucessfull...");
                            }
                            break;
                        case MAIN_MENU_PICK_OPTION:
                            System.out.println("Select an option:\n" +
                                                "1. Find an opponent\n" +
                                                "2. Quit");
                            break;
                        default:
                            System.out.println("New message type detected: " + message);
                    }

                }
            }
            
 
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }finally{
            scanner.close();
            //input.close();
            output.close();
            //socket.close();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) return;
        GameClient client = new GameClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }
}