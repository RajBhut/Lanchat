import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.*;

public class Server {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private BufferedReader consoleReader;
    private AtomicBoolean running = new AtomicBoolean(true);

    public Server() {
        try {
            serverSocket = new ServerSocket(8888);
            clientSocket = serverSocket.accept();

            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream());
            consoleReader = new BufferedReader(new InputStreamReader(System.in));

            // Thread to read from client
            new Thread(() -> {
                try {
                    String message;
                    while (running.get() && (message = reader.readLine()) != null) {
                        System.out.println("Client: " + message);
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

            // Thread to read from console and write to client
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
                    System.out.println("Error writing to client: " + e.getMessage());
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
            if (clientSocket != null) {
                clientSocket.close();
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
        new Server();
    }
}
