//file transfer & exceptions
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//packets & sockets
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
// byte buffer & input from UTF-8
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
//Arrays
import java.util.Arrays;

public class Server {
	public static void main(String[] args) throws Exception {
		// Create a new DatagramSocket on port 9876 and set a timeout of 5 seconds.
		DatagramSocket serverSocket = new DatagramSocket(9876);
		serverSocket.setSoTimeout(5000);
		// Initialize serverPort to 9875.
		int serverPort = 9875;

		System.out.println("FTP Server starting at host: " + InetAddress.getLocalHost().getHostName());
		System.out.println("Waiting to be contacted by client");

		// Continuously listen for incoming client connections.
		while (true) {
			// Create a byte array of size 1084 to receive data from the client.
			byte[] receiveData = new byte[1084];
			// Create a new DatagramPacket to receive data into the byte array.
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			// wait for client to connect
			try {
				serverSocket.receive(receivePacket);
				// Get the address and port of the client that sent the packet.
				InetAddress clientAddress = receivePacket.getAddress();
				int clientPort = receivePacket.getPort();

				// Create a new ClientHandler thread to handle the incoming client.
				ClientHandler clientHandler = new ClientHandler(receivePacket, clientAddress, clientPort, receiveData,
						serverPort--);
				Thread thread = new Thread(clientHandler);
				thread.start();
			} catch (SocketTimeoutException e) {
				// If no client connects within the timeout period, continue listening.
				continue;
			}

		}
	}
}

//Define the ClientHandler class that implements the Runnable interface.
class ClientHandler implements Runnable {

	// Declare instance variables for the ClientHandler class
	DatagramSocket serverSocket;
	private DatagramPacket receivePacket;
	private DatagramPacket sendPacket;
	private InetAddress clientAddress = null;
	private int clientPort = 0;
	private byte[] receiveData;
	private byte[] sendData;
	private int serverPort;

	// Create a constructor for the ClientHandler class.
	public ClientHandler(DatagramPacket receivePacket, InetAddress clientAddress, int clientPort, byte[] receiveData,
			int serverPort) {
		this.serverPort = serverPort;
		this.receivePacket = receivePacket;
		this.clientAddress = clientAddress;
		this.clientPort = clientPort;
		this.receiveData = receiveData;
		this.sendData = new byte[1084];
		this.sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
	}

	// Declare instance variables for the FTP protocol.
	String IP_address, Transfer_Type, File_Name, Msg;
	byte[] Body_Data;
	int Sequence_Num = 0;
	int ACK_Num = 0;
	long bLength;
	String[] header;

	// Implement the run method for the ClientHandler class.
	public void run() {

		// Continuously receive data from the client.
		while (true) {

			// Continuously wait for a "Connect" Msg from the client.
			Sequence_Num = 0;
			while (true) {
				// Get the header information from the incoming packet
				ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData(), receivePacket.getOffset(),
						receivePacket.getLength());
				
				header = getHeader(buffer);
				IP_address = header[0];
				Transfer_Type = header[1];
				File_Name = header[2];
				Sequence_Num = Integer.parseInt(header[3]);
				bLength = Long.parseLong(header[4]);
				Msg = new String(receiveData, 60, receivePacket.getLength() - 60);
				
				// If the Msg is "Connect," break out of the loop.
				if (Msg.equals("Connect")) {
					break;
				}
			}
			// Print the hostname of the client that has started the connection.
			System.out.println(clientAddress.getHostName() + " has started the connection");
			// Create a new DatagramSocket and set its timeout to 5000ms.
			try {
				serverSocket = new DatagramSocket(serverPort); // random port number
				serverSocket.setSoTimeout(5000);
			} catch (SocketException e2) {
			}

			// Retransmit "ACK" Msgs to the client until "ACKACK" is received.
			while (true) {
				sendData = new byte[1084];
				try {
					sendData = sMsg(InetAddress.getLocalHost().getHostAddress(), "Response", "NULL",
							String.valueOf(Sequence_Num), String.valueOf("ACK".length()), "ACK".getBytes());
					sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
					serverSocket.send(sendPacket);
				} catch (UnknownHostException e1) {

				} catch (IOException e1) {
				}

				try {
					// Construct the "ACK" Msg.
					receiveData = new byte[1084];
					receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData(), receivePacket.getOffset(),
							receivePacket.getLength());
					header = getHeader(buffer);
					IP_address = header[0];
					Transfer_Type = header[1];
					File_Name = header[2];
					Sequence_Num = Integer.parseInt(header[3]);
					bLength = Long.parseLong(header[4]);
					Msg = new String(receiveData, 60, receivePacket.getLength() - 60);

					// If the Msg is "ACKACK," break out of the loop.
					if (Msg.equals("ACKACK")) {
						break;
					}
				} catch (SocketTimeoutException e) {
					continue;
				} catch (IOException e) {
				}

			}

			//////////////////////////////////////////////////////////////////////////////////// FTP

			// Continuously receive data from the client.
			while (true) {
				try {

					receiveData = new byte[1084];
					receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData(), receivePacket.getOffset(),
							receivePacket.getLength());
					header = getHeader(buffer);
					IP_address = header[0];
					Transfer_Type = header[1];
					File_Name = header[2];
					Sequence_Num = Integer.parseInt(header[3]);
					bLength = Long.parseLong(header[4]);
					System.out.println("HEADER: "+header+" IP_address: "+IP_address);
					System.out.println("Transfer_Type: "+Transfer_Type+" File_Name: "+File_Name);
					System.out.println("Sequence_Num: "+Sequence_Num+" bLength: "+bLength);
					System.out.println("Msg: "+Msg);
					// Copy the packet's body data into a byte array.
					byte[] MsgBytes = new byte[(int) bLength];
					System.arraycopy(receiveData, 60, MsgBytes, 0, Math.min((int) bLength, 1024));
					Body_Data = MsgBytes;

					// If the request Transfer_Type is GET, process the request
					if (Transfer_Type.equals("GET") || Transfer_Type.equals("get")) {
						// Print the details of the request
						System.out.println(clientAddress.getHostName() + " has requested " + File_Name);

						// Check if the requested file exists
						File file = new File(File_Name);
						if (file.exists()) {

							// If the file exists, print its size and update the sequence number
							System.out.println(File_Name + " size is " + file.length() + " Bytes");
							Sequence_Num++;

							// Read the file and send it in packets
							FileInputStream fileInputStream = new FileInputStream(file);
							sendData = new byte[1084];
							int bytesRead;
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							while ((bytesRead = fileInputStream.read(sendData)) != -1) {
								outputStream.write(sendData, 0, bytesRead);
							}
							Body_Data = outputStream.toByteArray();
							fileInputStream.close();
							outputStream.close();
							//if file is big split
							int numPackets = (int) Math.ceil(Body_Data.length / 1024.0);
							long l = file.length();
							int i = 0;

							// Loop through each packet, send it, wait for the ACK and then proceed to the
							// next packet
							while (i < numPackets) {
								int offset = i * 1024;
								int length = Math.min(1024, Body_Data.length - offset);
								byte[] packetData = Arrays.copyOfRange(Body_Data, offset, offset + length);

								// Create the packet and send it
								sendData = sMsg(InetAddress.getLocalHost().getHostAddress(), "Data", File_Name,
										String.valueOf(Sequence_Num), String.valueOf(l), packetData);
								sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, 9000);
								serverSocket.send(sendPacket);
								System.out.println("Packet " + (i + 1) + " with size " + length + " bytes is sent to "
										+ clientAddress.getHostName() + ", sequence number " + Sequence_Num);
								System.out.println("Waiting for ACK ....");
								// Wait for the ACK
								try {
									receiveData = new byte[1084];
									receivePacket = new DatagramPacket(receiveData, receiveData.length);
									serverSocket.receive(receivePacket);
									buffer = ByteBuffer.wrap(receivePacket.getData(), receivePacket.getOffset(),
											receivePacket.getLength());
									header = getHeader(buffer);
									Transfer_Type = header[1];
									ACK_Num = Integer.parseInt(header[3]);
									if (Transfer_Type.equals("Response")) {
										if (ACK_Num == Sequence_Num) {
											System.out.println("ACK with sequence number " + ACK_Num
													+ " is received from " + clientAddress.getHostName());
											i++;
											Sequence_Num++;
											l -= length;
										}
									}
								} catch (SocketTimeoutException e) {
									// If ACK not received within timeout, retransmit the packet
									System.out.println("Retransmittimg packet " + (i + 1) + " with sequence number "
											+ Sequence_Num + " to " + clientAddress.getHostName());
									continue;
								}

							}

							System.out.println(
									File_Name + " is sent successfully to " + clientAddress.getHostName() + ",");
							System.out.println("On port: "+serverPort+" Waiting...");
							continue;

						}

						// get error
						else {
							System.out.println("ERROR FILE DOES NOT EXIST");
							sendData = new byte[1084];
							sendData = sMsg(InetAddress.getLocalHost().getHostAddress(), "Response", "NULL",
									String.valueOf(Sequence_Num), "0", "ERROR FILE DOES NOT EXIST".getBytes());
							sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
							serverSocket.send(sendPacket);
							System.out.println("Waiting...");
							continue;
						}
					}

					// If the request Transfer_Type is PUT
					else if (Transfer_Type.equals("PUT") || Transfer_Type.equals("put")) {

						System.out.println(clientAddress.getHostName() + " has requested to put " + File_Name);
						System.out.println("Waiting to receive the file ....");

						// Create a file output stream to receive the file
						FileOutputStream fileOutputStream = new FileOutputStream(File_Name);
						// Write the first chunk of file data to the output stream and print status
						fileOutputStream.write(Body_Data, 0, Math.min((int) bLength, 1024));
						System.out.println("Received first data packet of size " + Math.min((int) bLength, 1024)
								+ " bytes from " + clientAddress.getHostName() + ", sequence number " + Sequence_Num);

						// Send an acknowledgement response to the client
						sendData = new byte[1084];
						sendData = sMsg(InetAddress.getLocalHost().getHostAddress(), "Response", "NULL",
								String.valueOf(Sequence_Num), "0", "ACK".getBytes());
						sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
						serverSocket.send(sendPacket);
						System.out.println(
								"ACK with sequence number " + Sequence_Num + " is sent to " + clientAddress.getHostName());

						// Continue to read and write subsequent chunks of file data until the entire
						// file is received
						long remainingBytes = bLength - Math.min((int) bLength, 1024);
						int bytesToRead;
						int count = 1;

						while (remainingBytes > 0) {
							// Wait for the next packet of data to be received
							System.out.println("Waiting for next packet ....");
							try {
								receiveData = new byte[1084];
								receivePacket = new DatagramPacket(receiveData, receiveData.length);
								serverSocket.receive(receivePacket);
								count++;
								buffer = ByteBuffer.wrap(receivePacket.getData(), receivePacket.getOffset(),
										receivePacket.getLength());
								header = getHeader(buffer);
								Sequence_Num = Integer.parseInt(header[3]);
								bLength = Long.parseLong(header[4]);
								MsgBytes = new byte[1024];
								System.arraycopy(receiveData, 60, MsgBytes, 0, 1024);
								Body_Data = MsgBytes;
								fileOutputStream.write(Body_Data, 0, Math.min((int) bLength, 1024));
								bytesToRead = (int) Math.min(remainingBytes, 1024);
								remainingBytes -= bytesToRead;
								System.out.println("Received data packet " + count + " of size "
										+ Math.min((int) bLength, 1024) + " bytes from " + clientAddress.getHostName()
										+ ", sequence number " + Sequence_Num);

							} catch (SocketTimeoutException e) {
								continue;
							}

							sendData = new byte[1084];
							sendData = sMsg(InetAddress.getLocalHost().getHostAddress(), "Response", "NULL",
									String.valueOf(Sequence_Num), "0", "ACK".getBytes());
							sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
							serverSocket.send(sendPacket);

							if (bytesToRead < 1024) {
								System.out.println("Completion of transmission, file " + File_Name
										+ " is successfully received from " + clientAddress.getHostName());
								break;
							}
							System.out.println(
									"ACK with sequence number " + Sequence_Num + " is sent to " + clientAddress.getHostName());

						}
						fileOutputStream.close();
						System.out.println(
								"ACK with sequence number " + Sequence_Num + " is sent to" + clientAddress.getHostName());
						System.out.println("Waiting...");
						continue;
					} else if (Transfer_Type.equals("quit")) {
						System.out.println(clientAddress.getHostName() + " has terminated the connection..");
						clientAddress = null;
						serverSocket.close();
						break;
					}

				} catch (SocketTimeoutException e) {
					continue;
				} catch (IOException e1) {
				}
			}

		}
	}

	// This method takes in several parameters and returns a byte array that
	// represents a Msg packet
	public static byte[] sMsg(String IP_address, String Transfer_Type, String File_Name, String Sequence_Num, String bLength,
			byte[] Msg) throws IOException {
		// Combine the header fields into a single string
		String header = IP_address + " " + Transfer_Type + " " + File_Name + " " + Sequence_Num + " " + bLength;
		// Convert the header string to a byte array and store it in a variable
		byte[] headerFields = ByteBuffer.allocate(60).put(header.getBytes(StandardCharsets.UTF_8)).array();
		// Create a new byte array with a length equal to the length of the header
		// fields plus the length of the Msg
		byte[] packet = new byte[headerFields.length + Msg.length];
		// Copy the header fields to the beginning of the packet byte array
		System.arraycopy(headerFields, 0, packet, 0, headerFields.length);
		// Copy the Msg to the end of the packet byte array
		System.arraycopy(Msg, 0, packet, headerFields.length, Msg.length);
		// Return the packet byte array
		return packet;
	}

	// This method takes in a ByteBuffer and returns an array of strings that
	// represent the header fields
	public static String[] getHeader(ByteBuffer b) {
		// Create a new byte array with a length of 60
		byte[] headerBytes = new byte[60];
		// Read the first 60 bytes from the ByteBuffer and store them in the headerBytes
		// array
		b.get(headerBytes);
		// Convert the header bytes to a string and remove any leading or trailing
		// whitespace
		String h = new String(headerBytes, StandardCharsets.UTF_8).trim();
		// Split the header string into an array of strings using the space character as
		// a delimiter
		String[] header = new String[5];
		header = h.split(" ");
		// Return the header array
		return header;
	}

}