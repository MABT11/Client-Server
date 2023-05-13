import java.io.IOException;
import java.net.*;
//new
public class Router {
    
    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;
    
    public Router(int receivePort, int sendPort) throws SocketException {
        receiveSocket = new DatagramSocket(receivePort);
        sendSocket = new DatagramSocket(sendPort);
    }
    
    public void forwardPacket() throws IOException {
    InetAddress serverAddress = InetAddress.getByName("10.10.187.159");
    InetAddress clientAddress = InetAddress.getByName("MUBARAK");
    int serverPort = 9875;
    int clientPort = 9870;
   
        byte[] buffer = new byte[1084];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        receiveSocket.receive(packet);

        // simulate 10% packet loss
        if (Math.random() < 0.1) {
            System.out.println("Packet dropped: " + new String(packet.getData(), 0, packet.getLength()));
            return;
        }
        
        InetAddress destAddress ;
        int destPort ;
        if(packet.getAddress().equals(serverAddress) &&  packet.getPort() == serverPort )
        {
        destAddress = clientAddress;
        destPort = clientPort;
        System.out.println("recieved packet from server");
        }else
        {
        destAddress = serverAddress;
        destPort = serverPort;
        System.out.println("recieved packet from client");
        }
        
        DatagramPacket sendPacket = new DatagramPacket(packet.getData(), packet.getLength(), destAddress, destPort);
        sendSocket.send(sendPacket);
        System.out.println("Packet forwarded: " + new String(packet.getData(), 0, packet.getLength()));
    }
    
    public void close() {
        receiveSocket.close();
        sendSocket.close();
    }
    
    public static void main(String[] args) throws IOException {
        // create a router instance
        Router router = new Router(9000, 9001);
        
        // forward packets until user interrupts the program
        while (true) {
            try {
                router.forwardPacket();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        
        // close the sockets
        router.close();
    }
}