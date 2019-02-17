import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Network implements Closeable {
    DataOutputStream out;
    DataInputStream in;
    Scanner scanner = new Scanner(System.in);

    private Socket socket;
    private sendMsg messageSender;
    private String AUTH_PATTERN= "/auth %s %s";
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^/w (\\w+) (.+)", Pattern.MULTILINE);
    private static final String USER_LIST_PATTERN = "/userlist";
    private static final String MESSAGE_SEND_PATTERN = "/w %s %s";
    private final Thread receiver;
    private String hostname;
    private int port;
    private String username;

    public  Network(String hostname, int port, sendMsg messageSender) {
        this.hostname =hostname;
        this.port=port;
        this.messageSender = messageSender;
        this.receiver = createReceiverThread();
    }

    private Thread createReceiverThread(){
     return new Thread(new Runnable() {

         @Override
         public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String msg = in.readUTF();
                        System.out.println("Message: " + msg);
                        Matcher matcher = MESSAGE_PATTERN.matcher(msg);
                        if (matcher.matches()){
                            Message message = new Message(matcher.group(1),username,matcher.group(2));
                            messageSender.sendMsg(message);
                        } else if (msg.startsWith(USER_LIST_PATTERN)){
                            //Обновить список
                        }
                              } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
             System.out.printf("Network connection is closed for user %s%n", username);
         }
                });
    }

    public void authorise(String username, String password) throws IOException {
        socket = new Socket(hostname,port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        try {
            out.writeUTF(String.format(AUTH_PATTERN, username, password));
            out.flush();
            String response = in.readUTF();
            if (response.equals("/auth succesful")){
              this.username=username;
             receiver.start();
            } else {
                throw new AuthException(){};

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToUser(Message message) {
        sendMessage(String.format(MESSAGE_SEND_PATTERN, message.getUserTo(), message.getText()));
    }

        public void sendMessage(String msg){
        try {
        out.writeUTF(msg);
        out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public String getUsername() {
        return username;
    }
    @Override
    public void close()  {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        receiver.interrupt();
        try {
            receiver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
