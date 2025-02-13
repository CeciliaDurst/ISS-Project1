// Demonstrating Server-side Programming
import java.net.*;
import java.io.*;
import java.util.regex.Pattern;


public class Server {
  
    // Initialize socket and input stream
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;

    // Constructor with port
    public Server(int port) {
      
        // Starts server and waits for a connection
        try
        {
            ss = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            s = ss.accept();
            System.out.println("Client accepted");

            // Takes input from the client socket
            in = new DataInputStream(
                new BufferedInputStream(s.getInputStream()));

            String m = "";

            // Reads message from client until "Over" is sent
            while (!m.equals("Over"))
            {
                try
                {
                    m = in.readUTF();

                    String alphaRegex = "[a-zA-Z]+"; // Regex to match on alphabetical characters
                    if (Pattern.matches(alphaRegex, m)) { // Checks that the input string matches the given pattern
                      
                      // TODO: Write function that will capitalize the string
                      System.out.println("Received alphabetical string: " + m);

                    } else {

                      // TODO: Ask the client to send an alphabetical string
                      System.out.println(m + " is not alphabetical, asking the client to resend");

                    }

                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");

            // Close connection
            s.close();
            in.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    @SuppressWarnings("unused")
    public static void main(String args[])
    {
        Server s = new Server(6000);
    }
}

