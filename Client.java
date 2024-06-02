
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.*;
import java.net.*;

public class Client {

    private Socket serverSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private BufferedReader consoleReader;
    private AtomicBoolean running = new AtomicBoolean(true);

    public Client() {
        try {
            serverSocket = new Socket("localhost", 8888);

            reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            writer = new PrintWriter(serverSocket.getOutputStream());
            consoleReader = new BufferedReader(new InputStreamReader(System.in));

            // Thread to read from server
            new Thread(() -> {
                try {
                    String message;
                    while (running.get() && (message = reader.readLine()) != null) {
                        System.out.println("Server: " + message);
                        if (message.equalsIgnoreCase("exit")) {
                            running.set(false);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Server disconnected!");
                    running.set(false);
                } finally {
                    closeResources();
                }
            }).start();

            // Thread to read from console and write to server
            new Thread(() -> {
                try {
                    String message;
                    while (running.get() && (message = consoleReader.readLine()) != null) {
                        writer.println(message);
                        writer.flush(); // Ensure data is sent immediately
                        if (message.equalsIgnoreCase("exit")) {
                            running.set(false);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error writing to server: " + e.getMessage());
                    running.set(false);
                } finally {
                    closeResources();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeResources() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
