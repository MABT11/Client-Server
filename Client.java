package assignment1_networking;

import java.net.*;
import java.io.*;

public class Client3 {
    public static void main(String[] args) {
        try {
            // Create a buffered reader for user input from the console
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            // Print a message indicating that the client is starting and prompt the user for the server name
            System.out.println("TCP Client starting on host: " + InetAddress.getLocalHost().getHostName() + ".");
            System.out.print("Type name of TCP server: ");
            String serverName = userInput.readLine();
            while(!serverName.equals("Mubarak")) {
            	System.out.print("Reenter correct server name ");
            	serverName = userInput.readLine();
            	System.out.println();
            }
            System.out.print("Write any message: ");
            
            // Check if the server name is available
            InetAddress serverAddress = InetAddress.getByName(serverName);
            if (serverAddress.isReachable(3000)) {
                // Create a new socket and connect to the server at port 5000
                Socket socket = new Socket(serverAddress, 5000);

                // Create input and output streams for communication with the server
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Read input from the user until the user enters "quit"
                String userInputLine;
                while ((userInputLine = userInput.readLine()) != null) {
                    // Send the user input to the server
                    out.println(userInputLine);

                    // Exit the loop if the user enters "quit"
                    if (userInputLine.equals("quit")) {
                        break;
                    }

                    // Read the server's response and print it to the console
                    String serverResponse = in.readLine();
                    System.out.println(serverResponse);

                    // Request the file size from the server
                    System.out.print("Enter the file name with extension to get the size: ");
                    String fileName = userInput.readLine();
                    out.println(fileName);

                    // Read the server's response and print it to the console
                    String fileSize = in.readLine();
                    System.out.println(fileSize);
                }

                // Close the socket and exit the program
                socket.close();
            } else {
                // Print an error message if the server name is not available
                System.out.println("Server name is not available.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}