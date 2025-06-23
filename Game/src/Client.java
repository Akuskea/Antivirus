import javafx.scene.input.KeyCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    public Client(String serverHost, int serverPort) throws IOException {
        socket = new Socket(serverHost, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void start() {
        try {

            // Example: Receiving a message from the server
            String receivedMessage = receiveMessage();
            sendMessage(receivedMessage);
            System.out.println("Received from server: " + receivedMessage);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void sendKeyPress(KeyCode keyCode) {
        sendMessage("KEY_EVENT " + keyCode.toString());
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void disconnect() {
        sendMessage("DISCONNECT");
        try {
            Thread.sleep(10000); // 1-second delay as an example
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        close();
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}