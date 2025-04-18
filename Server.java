// Demonstrating Server-side Programming
import java.net.*;
import java.util.List;
import java.io.*;

public class Server {
  
    // Initialize socket and input/output stream
    private Socket s = null;
    private ServerSocket ss = null;

    // Constructor with port
    public Server(int port) {
      
        // Starts server and waits for a connection
        try
        {
            ss = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
            
            System.out.println("Server started on port " + port);

            System.out.println("Waiting for a client ...");

            while(true) {
                s = ss.accept();
                System.out.println("Client accepted");
                Thread concurrentClient = new Thread(new handleClient(s));
                concurrentClient.start();
            }
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

class handleClient implements Runnable {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    public handleClient(Socket s) {
        this.clientSocket = s;
    }

    private void sendBatch(List<String> files) {
        try {
            out = new DataOutputStream(clientSocket.getOutputStream());
  
            // Start by sending the amount of files in the batch 
            // (We always know it should be 10, but we're formatting the batch.)
            out.writeInt(files.size());

            // Send each file
            for(String fileName : files) {
                File file = new File(fileName);

                // If file does not exist, send a -1 and message to the client asking for a different file
                if (!file.exists()) {
                    out.writeLong(-1);
                    out.writeUTF("File not found");
                    out.flush();
                    continue;
                }

                FileInputStream fin = new FileInputStream(file);
                out.writeUTF(file.getName());
                out.writeLong(file.length());

                byte[] buffer = new byte[1024];
                int bytesRead = 0;
        
                while ((bytesRead = fin.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                fin.close();
            }

          out.flush();
  
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    // Main thread function
    public void run() {
        try{
            System.out.println("Client connected on: " + clientSocket.getInetAddress());
            // Establish datastreams
            in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            out = new DataOutputStream(clientSocket.getOutputStream());

            // Send responses to the client
            out.writeUTF("Hello!");

            // Takes input from the client socket
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
                    else if(m.equals("SEND")){
                        out.writeUTF("Awaiting batch request");
                        ObjectInputStream receiveList = new ObjectInputStream(clientSocket.getInputStream());
                        List<String> files = (List<String>) receiveList.readObject();
                        sendBatch(files);
                    }
                    else {
                    System.out.println("Received invalid command");
                    out.writeUTF("Please send 'SEND' to begin batch transfer or 'bye' to quit.");
                    }
                }
                catch(IOException i)
                {
                    System.out.println(i);
                    break;
                } catch (ClassNotFoundException e) {
                    // List received not in List<String> format
                    e.printStackTrace();
                }
            }

            // Close connection
            System.out.println("Closing connection with client on: " + clientSocket.getInetAddress());
            clientSocket.close();
            in.close();
            out.close();
        } catch(IOException i) {
            System.out.println(i);
        }
    }
}
