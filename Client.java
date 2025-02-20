// Demonstrating Client-side Programming
import java.io.*;
import java.net.*;

public class Client {
  
    // Initialize socket and input/output streams
    private Socket s = null;
    private DataInputStream termIn = null;
    private DataOutputStream out = null;
    private DataInputStream serverIn = null;

    // Constructor to put IP address and port
    @SuppressWarnings("deprecation")
    public Client(String addr, int port)
    {
        // Establish a connection
        try {
            s = new Socket(addr, port);
            System.out.println("Connected");

            // Takes input from terminal
            termIn = new DataInputStream(System.in);

            // Sends output to the server
            out = new DataOutputStream(s.getOutputStream());

            // Takes input from server messages
            serverIn = new DataInputStream(s.getInputStream());
        }
        catch (UnknownHostException u) {
            System.out.println("Unknown host exception " + u);
            return;
        }
        catch (IOException i) {
            System.out.println("IO exception " + i);
            return;
        }

        // String to read message from input
        String m = "";

        // Listen for Hello!
        try {
        m = serverIn.readUTF();
        System.out.println(m);
        }
        catch (IOException i) {
            System.out.println("Failed to receive 'Hi!' : " + i);
        }

        // Keep reading until "bye" is input
        while (!m.equals("bye")) {
            try {
                // TODO: Timer functionality

                // Read client input
                m = termIn.readLine();
                out.writeUTF(m);

                // Listen to server
                if(!m.equals("bye")){
                    m = serverIn.readUTF();
                    System.out.println(m);
                }
            }
            catch (IOException i) {
                System.out.println(i);
            }
        }

        // Close the connection
        try {
            termIn.close();
            out.close();
            serverIn.close();
            s.close();
        }
        catch (IOException i) {
            System.out.println(i);
        }
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {

      if (args.length == 0) {

        Client c = new Client("127.0.0.1", 6000);
      } else if ( args.length == 2) {

        Client c = new Client(args[0], Integer.valueOf(args[1]));
      } else {

        System.out.println("FAIL");
      }
    }
}