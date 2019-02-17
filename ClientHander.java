import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHander {
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^/w (\\w+) (.+)", Pattern.MULTILINE);
    private static final String MESSAGE_SEND_PATTERN = "/w %s %s";

    private final Thread handleThread;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ChatServer server;
    private final String username;
    private final Socket socket;

    public ClientHander(String username, Socket socket, ChatServer server) throws IOException {
     this.username = username;
       this.socket = socket;
        this.server = server;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        this.handleThread = new Thread(new Runnable() {
            @Override
            public void run() {
               try {
                    while (!Thread.currentThread().isInterrupted()){
                        String msg = in.readUTF();
                        System.out.printf("Message from user %s: %s%n", username, msg);

                        Matcher matcher = MESSAGE_PATTERN.matcher(msg);
                        if (matcher.matches()){
                            String userTo = matcher.group(1);
                            String message = matcher.group(2);

                                server.sendMessage(userTo, username, message);

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.printf("Client %s disconnected%n", username);
                    try {
                       socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                   }
                    server.unsubscribeClient(ClientHander.this);
                }
            }
        });
        handleThread.start();
    }

    public void sendMessage(String userTo, String msg) throws IOException {
        out.writeUTF(String.format(MESSAGE_SEND_PATTERN, userTo, msg));
    }

    public String getUsername() {
        return username;
    }
}
