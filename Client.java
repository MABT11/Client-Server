import java.io.*; //file transfer & exceptions
import java.net.*; //packets & sockets 
import java.nio.*; //byte buffer & input from keyboard
import java.nio.charset.StandardCharsets;
import java.util.*; //scanner

public class Client {
public static void main(String[] args) throws Exception {
// Create a DatagramSocket for the client on port 9870
DatagramSocket clientSocket = new DatagramSocket(9870); //datagramSocket because of UDP
// Set a timeout of 5 seconds for receiving packets from the server
clientSocket.setSoTimeout(5000); //timer for receiving packets from server (for stop and wait protocol)

// Create byte arrays for sending and receiving data
//Transfer_Type byte 
byte[] sendData = new byte[1084]; //header+byte size
byte[] receiveData = new byte[1084];

// Initialize variables for server address, port, and name, as well as other Msg parameters
InetAddress serverAddress = null;
int serverPort = 0;
String serverName = "";

String IP_Address, Transfer_Type, File_Name, Msg;
byte[] Body_Data = new byte[1024];
int Sequence_Num=0;
int ACK_Num=0;
long bLength;
String[] header;

// Create DatagramPacket objects for sending and receiving packets
DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 9876);
DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

// Print a Msg indicating that the FTP client has started
System.out.println("FTP client starting at host " + InetAddress.getLocalHost().getHostName());

// Create a loop that continues until the flag variable is false
boolean flag=true;
while(flag) {
// Create a loop that continues until the server address is not null
while (serverAddress == null) {
Sequence_Num=0;
Scanner cin=new Scanner(System.in);
// Prompt the user to enter the server host name
System.out.print("Enter server host name: ");
serverName = cin.nextLine();
// If the user enters "quit", close the client socket and set the flag variable to false to exit the loop
if(serverName.trim().equals("quit")) {
clientSocket.close();
flag=false;
break;
}
// Otherwise, try to connect to the server
else {
try {
Sequence_Num++;
// Create a loop for sending a connection request to the server and waiting for an ACK
while(true) {
// Get the InetAddress object for the server name
serverAddress = InetAddress.getByName(serverName);
// Construct a Msg with the header and body data for the connection request
sendData = new byte[1084];
sendData=sMsg(InetAddress.getLocalHost().getHostAddress(), "Response", "NULL", String.valueOf(Sequence_Num), String.valueOf("Connect".length()), "Connect".getBytes());
// Create a DatagramPacket object with the Msg and server address/port and send it to the server
sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 9876);
clientSocket.send(sendPacket);

try {
// Receive a packet from the server
receiveData = new byte[1084];
receivePacket=new DatagramPacket(receiveData, receiveData.length);
clientSocket.receive(receivePacket);

// Extract the header and Msg data from the packet
ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
header=getHeader(buffer);
IP_Address=header[0];
Transfer_Type=header[1];
File_Name=header[2];
Sequence_Num=Integer.parseInt(header[3]);
bLength=Long.parseLong(header[4]);
Msg = new String(receiveData, 60, receivePacket.getLength() - 60);

// If Msg is "ACK", send an ACK_Numwledgement Msg to the server
if(Msg.equals("ACK")) {
serverPort=receivePacket.getPort();
Sequence_Num++;
break;
}
} catch(SocketTimeoutException e) {
// If there's a timeout, continue the loop
continue;
}
}

// Print connection success Msg
System.out.println("Connection is made successfully with "+serverAddress.getHostName());
// Send ACK_Numwledgement Msg to server
// Initialize a byte array of size 1084 to hold the data to be sent
sendData = new byte[1084];
// Construct the Msg to be sent with the following parameters:
// - The local host IP_Address address
// - The Msg Transfer_Type ("Response")
// - The payload data (in this case, "NULL")
// - The sequence number (converted to a string)
// - The length of the payload data (converted to a string)
// - The payload data (in bytes)
sendData = sMsg(InetAddress.getLocalHost().getHostAddress(), "Response", "NULL", String.valueOf(Sequence_Num), String.valueOf("ACKACK".length()), "ACKACK".getBytes());
// Create a new datagram packet with the Msg data, server address, and server port
sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
// Send the datagram packet using the client socket
clientSocket.send(sendPacket);
// Increment the sequence number
Sequence_Num++;
// Catch an unknown host exception if it occurs and print an error Msg.
} catch (UnknownHostException e) {
System.out.println("Unknown host, please try again.");
}
}

}


///////////////////////////////////////////////////////////////////// FTP

// check if the flag is true, if not, exit the loop
if(!flag) break;
// set flag3 to true
boolean flag3=true;
// create a scanner object to read user input
Scanner s=new Scanner(System.in);
// prompt the user to enter the Transfer_Type of transfer
System.out.print("Enter the Transfer_Type of transfer: ");
// read the user input and store it in the 'Transfer_Type' variable
Transfer_Type = s.nextLine();
// if the user input is 'quit', terminate the program
if(Transfer_Type.equals("quit")) {
// create a byte array of size 1084
sendData = new byte[1084];
// create a Msg to be sent to the server and store it in 'sendData'
sendData=sMsg(InetAddress.getLocalHost().getHostAddress(), Transfer_Type, "NULL", String.valueOf(Sequence_Num), "0", "".getBytes());
   // create a datagram packet to send to the server
sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
// send the datagram packet to the server
clientSocket.send(sendPacket);
// close the client socket
clientSocket.close();
 // exit the loop
break;
}
// While user input is not valid, keep asking for input
while(!Transfer_Type.equals("PUT") && !Transfer_Type.equals("put") && !Transfer_Type.equals("GET") && !Transfer_Type.equals("get") && !Transfer_Type.equals("quit")) {
System.out.println("Invalid input..");
System.out.print("Enter the Transfer_Type of transfer: ");
Transfer_Type=s.nextLine();
}

//if user wants to request a file from the server
if (Transfer_Type.equals("GET") || Transfer_Type.equals("get")) {
System.out.print("Enter the file name: ");
File_Name=s.nextLine();

// if user wants to quit the program
if(File_Name.equals("quit")) {
sendData = new byte[1084];
sendData=sMsg(InetAddress.getLocalHost().getHostAddress(), "quit", "NULL", String.valueOf(Sequence_Num), "0", "".getBytes());
sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
clientSocket.send(sendPacket);
// close the socket
clientSocket.close();
// exit the loop
break;
}
// keep sending GET request until a response is received
do {
sendData = new byte[1084];
sendData=sMsg(InetAddress.getLocalHost().getHostAddress(), Transfer_Type, File_Name, String.valueOf(Sequence_Num), "0", "".getBytes());
sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
clientSocket.send(sendPacket);
System.out.println("Get Request with sequence number "+Sequence_Num+" is sent");
System.out.println("Waiting to receive the file ....");

try {
receiveData = new byte[1084];
receivePacket=new DatagramPacket(receiveData, receiveData.length);
clientSocket.receive(receivePacket);

// Retrieve the data that has been received and parse it
ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
header=getHeader(buffer); // Retrieve the header from the buffer
IP_Address=header[0];
Transfer_Type=header[1];
File_Name=header[2];
Sequence_Num=Integer.parseInt(header[3]);
bLength=Long.parseLong(header[4]);
byte[] MsgBytes = new byte[(int)bLength];
System.arraycopy(receiveData, 60, MsgBytes, 0, Math.min((int) bLength, 1024));
Body_Data = MsgBytes;

// if the Transfer_Type of transfer is "Data"
if(Transfer_Type.equals("Data")) {
// create a new FileOutputStream to receive the file with the given File_Name
FileOutputStream fileOutputStream = new FileOutputStream(File_Name); //fileOutputStream to receive the file
// write the received data packet to the FileOutputStream, up to a maximum of 1024 bytes
fileOutputStream.write(Body_Data, 0, Math.min((int) bLength, 1024));
// print a Msg indicating the size of the received data packet and its sequence number
System.out.println("Received first data packet of size "+Math.min((int) bLength, 1024)+" bytes, sequence number "+Sequence_Num);

// create a new byte array called sendData with a length of 1084
sendData = new byte[1084];
// call the sMsg method to create a new Msg to send back to the server
sendData=sMsg(InetAddress.getLocalHost().getHostAddress(), "Response", "NULL", String.valueOf(Sequence_Num), "0", "ACK".getBytes());
 // create a new DatagramPacket called sendPacket with the sendData, serverAddress and serverPort
sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
// send the packet using the clientSocket
clientSocket.send(sendPacket);
// print a Msg indicating that an ACK Msg has been sent with the corresponding sequence number
System.out.println("ACK with sequence number "+Sequence_Num+" is sent");

// calculate the remaining bytes to receive
long remainingBytes = bLength - Math.min((int) bLength, 1024);
// initialize bytesToRead to 0
int bytesToRead;
// initialize count to 1
int count=1;
// while there are still remaining bytes to be read
while (remainingBytes > 0 ) {
// print waiting Msg
System.out.println("Waiting for next packet ....");
try {
// create a buffer to store the received data
receiveData = new byte[1084];
// receive a packet from the server
receivePacket = new DatagramPacket(receiveData, receiveData.length);
clientSocket.receive(receivePacket);
// increment the count
count++;
// create a buffer to store the received packet data and extract the header information
buffer = ByteBuffer.wrap(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
header = getHeader(buffer);
// get the sequence number and body length from the header
Sequence_Num=Integer.parseInt(header[3]);
bLength=Long.parseLong(header[4]);
// create a byte array to store the Msg data
MsgBytes = new byte[1024];
// copy the Msg data from the received packet to the byte array
System.arraycopy(receiveData, 60, MsgBytes, 0, 1024);
Body_Data = MsgBytes;
// write the Msg data to the file output stream
fileOutputStream.write(Body_Data, 0, Math.min((int) bLength, 1024));
// calculate the number of bytes to read in the next iteration of the loop
bytesToRead = (int) Math.min(remainingBytes, 1024);
// subtract the number of bytes read from the remaining bytes
remainingBytes -= bytesToRead;
// print a Msg indicating the size and sequence number of the received packet
System.out.println("Received data packet "+count+" of size "+Math.min((int) bLength, 1024)+" bytes, sequence number "+Sequence_Num);
// handle the case where a timeout occurs while waiting for a packet
}catch(SocketTimeoutException e) {
continue;
}
// Construct and send ACK Msg to server
sendData = new byte[1084];
sendData=sMsg(InetAddress.getLocalHost().getHostAddress(), "Response", "NULL", String.valueOf(Sequence_Num), "0", "ACK".getBytes());
sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
clientSocket.send(sendPacket);

// Check if file transmission is complete
if (bytesToRead < 1024) {
System.out.println("Completion of transmission, file is successfully received....");
break;
}
// Output Msg and send ACK with sequence number
System.out.println("ACK with sequence number "+Sequence_Num+" is sent");

}
// Close output stream and send ACK with sequence number
fileOutputStream.close();
System.out.println("ACK with sequence number "+Sequence_Num+" is sent");

Sequence_Num++;
flag3=false;
//}
}
// Check for errors in "GET" transfer and handle them
else if(Transfer_Type.equals("Response")) {
Msg = new String(receiveData, 60, receivePacket.getLength() - 60);
if (Msg.startsWith("ERROR")) {
System.out.println(Msg);
System.out.print("Enter the name of file to be transferred: ");
File_Name=s.next();
 // Handle "quit" command
if(File_Name.equals("quit")) {
sendData = new byte[1084];
sendData=sMsg(InetAddress.getLocalHost().getHostAddress(), "quit", "NULL", String.valueOf(Sequence_Num), "0", "".getBytes());
sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
clientSocket.send(sendPacket);
clientSocket.close();
flag3=false;
flag=false;
break;
}
Transfer_Type="GET";
flag3=true;
}
}
// Handle SocketTimeoutException
} catch(SocketTimeoutException e) {
continue;
}
// Continue while flag3 is true
} while(flag3);
}

else if(Transfer_Type.equals("PUT") || Transfer_Type.equals("put")) {
File file = null;
while (true) {
System.out.print("Enter the file name: ");
File_Name=s.nextLine();
// check if the user wants to quit
if(File_Name.equals("quit")) {
sendData = new byte[1084];
sendData=sMsg(InetAddress.getLocalHost().getHostAddress(), "quit", "NULL", String.valueOf(Sequence_Num), "0", "".getBytes());
sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
clientSocket.send(sendPacket);
clientSocket.close();
flag=false;
break;
}
file = new File(File_Name);
// check if the file exists
if (!file.exists()) {
System.out.println("ERROR FILE DOES NOT EXIST");
}
else {
break;
}
}

System.out.println("Put request with sequence number "+Sequence_Num+" is sent");
System.out.println(File_Name+" size is "+file.length()+" Bytes");
Sequence_Num++;

FileInputStream fileInputStream = new FileInputStream(file);
sendData = new byte[1084];
int bytesRead;
ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
// Read the file and write to ByteArrayOutputStream
while ((bytesRead = fileInputStream.read(sendData)) != -1) {
outputStream.write(sendData, 0, bytesRead);
}
Body_Data = outputStream.toByteArray();
fileInputStream.close();
outputStream.close();
// Calculate the number of packets to be sent
int numPackets = (int) Math.ceil(Body_Data.length / 1024.0);
long l = file.length();
int i=0;
while (i < numPackets) {
// Determine the offset and length of each packet
int offset = i * 1024;
int length = Math.min(1024, Body_Data.length - offset);
byte[] packetData = Arrays.copyOfRange(Body_Data, offset, offset + length);
  // Prepare packet header and data
sendData = sMsg(InetAddress.getLocalHost().getHostAddress(), Transfer_Type, File_Name, String.valueOf(Sequence_Num), String.valueOf(l), packetData);
sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 9000);
clientSocket.send(sendPacket);
System.out.printf("Packet %d with size %d bytes is sent, sequence number %d\n",(i+1),length,Sequence_Num);
System.out.println("Waiting for ACK ....");

// Wait for ACK_Numwledgement for each packet
try {
receiveData = new byte[1084];
receivePacket=new DatagramPacket(receiveData, receiveData.length);
clientSocket.receive(receivePacket);
ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
header=getHeader(buffer);
Transfer_Type=header[1];
ACK_Num=Integer.parseInt(header[3]);
if (Transfer_Type.equals("Response")) {
if (ACK_Num==Sequence_Num) {
System.out.println("ACK with sequence number "+ACK_Num+" is received");
i++;
Sequence_Num++;
l-=length;
}
}
} catch(SocketTimeoutException e) {
System.out.println("Retransmittimg packet "+(i+1)+" with sequence number "+Sequence_Num);
continue;
}

}

System.out.println("File is sent successfully to "+serverAddress.getHostName()+",");

}

Sequence_Num+=1000;

}        
}



public static byte[] sMsg(String IP_Address, String Transfer_Type, String File_Name, String Sequence_Num, String bLength, byte[] Msg) throws IOException {
// Concatenates the input arguments to form a header string
String header = IP_Address + " " + Transfer_Type + " " + File_Name + " " + Sequence_Num + " " + bLength;
// Converts the header string to a byte array with a length of 60 bytes
byte[] headerFields = ByteBuffer.allocate(60).put(header.getBytes(StandardCharsets.UTF_8)).array();
// Concatenates the header byte array and the Msg byte array to form a packet
byte[] packet = new byte[headerFields.length + Msg.length];
System.arraycopy(headerFields, 0, packet, 0, headerFields.length);
System.arraycopy(Msg, 0, packet, headerFields.length, Msg.length);
// Returns the packet
return packet;
}


// This method takes in a ByteBuffer and returns an array of strings that represent the header fields
public static String[] getHeader(ByteBuffer b) {
// Create a new byte array with a length of 60
byte[] headerBytes = new byte[60];
// Read the first 60 bytes from the ByteBuffer and store them in the headerBytes array
b.get(headerBytes);
// Convert the header bytes to a string and remove any leading or trailing whitespace
String h = new String(headerBytes, StandardCharsets.UTF_8).trim();
// Split the header string into an array of strings using the space character as a delimiter
String[] header=new String[5];
header=h.split(" ");
// Return the header array
return header;
}


}


