// Demonstrating Server-side Programming
import java.net.*;
import java.io.*;
import java.util.regex.Pattern;


public class Server {
  
    // Initialize socket and input/output stream
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    // Constructor with port
    public Server(int port) {
      
        // Starts server and waits for a connection
        try
        {
            ss = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
            
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            s = ss.accept();
            System.out.println("Client accepted");

            // Sends responses to the client
            out = new DataOutputStream(s.getOutputStream());
            out.writeUTF("Hello!");

            // Takes input from the client socket
            in = new DataInputStream(
                new BufferedInputStream(s.getInputStream()));

            String m = "";

            // Reads message from client until "bye" is sent
            while (!m.equals("bye"))
            {
                try
                {
                    m = in.readUTF();
                    if(m.equals("bye")){
                        out.writeUTF("disconnected");
                    }
                    else {

                        String alphaRegex = "[a-zA-Z]+"; // Regex to match on alphabetical characters
                        if (Pattern.matches(alphaRegex, m)) { // Checks that the input string matches the given pattern
                        
                        System.out.println("Received alphabetical string: " + m);
                        String cappedString = m.toUpperCase();
                        out.writeUTF(cappedString);

                        } else {

                        System.out.println(m + " is not alphabetical, asking the client to resend");
                        out.writeUTF("Please send an alphabetical message.");

                        }
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
            out.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    @SuppressWarnings("unused")
    public static void main(String args[])
    {
        if (args.length == 0) {
            Server s = new Server(6000);
        } else if ( args.length == 1) {
            try{
                Server s = new Server(Integer.valueOf(args[0]));
            }
            catch(Error e) {
                System.out.println("Please enter a valid port number.");
            }
            
        } else {
    
            System.out.println("Please enter a valid port number or leave the command line arguments blank to connect to port 6000.");
        }
    }
}

