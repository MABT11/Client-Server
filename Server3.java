package assignment1_networking;
import java.net.*;
import java.io.*;
import java.util.Date;

public class Server3 {
    public static void main(String[] args) {
        try {
            // Create a new server socket and bind it to port 5000
            ServerSocket serverSocket = new ServerSocket(5000);
            // Print a message indicating that the server is running and waiting for a client to connect
            System.out.println("TCP Server starting at host: " + InetAddress.getLocalHost().getHostName() + " waiting to be contacted by a Client...");

            // Continuously wait for incoming client connections
            while (true) {
                // Accept an incoming client connection and create a new socket for communication
                Socket clientSocket = serverSocket.accept();
                // Print a message indicating that a connection has been established with a client
                System.out.println("A connection is established with a Client");

                // Create input and output streams for communication with the client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                try {
                    // Read input from the client until the client disconnects or sends "quit"
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.equals("quit")) {
                            break;
                        }

                        // Print the message received from the client along with the client's hostname
                        System.out.println(inputLine + " message is received from Client [" + clientSocket.getInetAddress().getHostName() + "]..");

                        // Respond back with the timestamp of receiving the message
                        Date date = new Date();
                        out.println("Message received successfully at " + date.toString());

                        // Receive the file name from the client and send back the size of the file
                        String fileName = in.readLine();
                        System.out.println("Client requested the size of the file " + fileName + ".");
                        File file = new File(fileName);
                        if (file.exists()) {
                            out.println("The File size is " + file.length() + " Bytes.");
                        } else {
                            out.println("File not found.");
                        }
                    }
                } catch (IOException e) {
                    // Handle client disconnection gracefully
                    System.out.println("Client [" + clientSocket.getInetAddress().getHostName() + "] has disconnected.");
                } finally {
                    // Close the input and output streams and the client socket
                    in.close();
                    out.close();
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}