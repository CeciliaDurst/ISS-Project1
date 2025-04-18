// Demonstrating Client-side Programming with Timer
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Client {
  
    // Initialize socket and input/output streams
    private Socket s = null;
    private DataInputStream termIn = null;
    private DataOutputStream out = null;
    private DataInputStream serverIn = null;
    private DataInputStream in = null;


    public void receiveBatch() {

        // Create client directory if it doesn't exist.
        File directory = new File("client");
        if(!directory.exists()){
            directory.mkdirs();
        }

        // Begin reading files
        try {
            // Read total file amount in batch
            in = new DataInputStream(s.getInputStream());
            int fileAmount = in.readInt();

            // Read each file
            for(int i = 0; i < fileAmount; i++){
                String fileName = in.readUTF();
                long fileSize = in.readLong();

                // File not found on server side (denoted by a -1)
                if(fileSize == -1){
                    System.out.println("File " + fileName + " not found on server.");
                    continue;
                }

                FileOutputStream fout = new FileOutputStream("client/" + fileName);
                byte[] buffer = new byte[1024];
                long bytesRemaining = fileSize;

                while (bytesRemaining > 0) {
                    int bytesToRead = (int)Math.min(buffer.length, bytesRemaining);
                    int bytesRead = in.read(buffer, 0, bytesToRead);
                    fout.write(buffer, 0, bytesRead);
                    bytesRemaining -= bytesRead;
                }
                fout.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
      }
    }

    // Constructor to put IP address and port
    @SuppressWarnings("deprecation")
    public Client(String addr, int port) {
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
            System.out.println("Failed to receive 'Hello!' : " + i);
        }

        // Timer stats storage
        ArrayList<Long> rttTimes = new ArrayList<>();
        int exchanges = 0;

        // Perform at least 5 message exchanges for RTT calculations
        while(!m.equals("bye")) {
            exchanges ++;
            try {
                m = termIn.readLine();
                if(!m.equals("bye")){
                    // Start the timer
                    long startTime = System.currentTimeMillis();

                    out.writeUTF(m);
                    m = serverIn.readUTF();

                    // Handle Batch Request
                    if(m.equals("Awaiting batch request")){
                        // List<String> fileRequests = getRandomBatchRequest();
                        List<String> fileRequests = List.of("sample02.bmp", "sample05.bmp", "sample01.bmp");
                        ObjectOutputStream sendList = new ObjectOutputStream(s.getOutputStream());
                        sendList.writeObject(fileRequests);
                        sendList.flush();
                        receiveBatch();
                    }
                    else {
                        System.out.println(m);
                    }
                    
                    // Stop the timer
                    long endTime = System.currentTimeMillis();

                    // Calculate round-trip time (RTT) in milliseconds
                    long rtt = endTime - startTime;
                    rttTimes.add(rtt);

                    System.out.println("RTT: " + rtt + " ms");

                    // Compute statistics
                    if(exchanges >= 5){
                        computeRTTStatistics(rttTimes);
                    }
                }
                else{
                    // Send bye to server
                    out.writeUTF(m);
                }

            } catch (IOException e) {
                System.out.println("IO Exception: " + e);
            }
        }


        // Close the connection gracefully
        try {
            out.writeUTF("bye");
            String response = serverIn.readUTF();
            System.out.println(response);
            if (response.equals("disconnected")) {
                System.out.println("exit");
            }
            else {
                System.out.println("Error disconnecting.");
            }
            termIn.close();
            out.close();
            serverIn.close();
            s.close();
    
        }
        catch (IOException i) {
            System.out.println(i);
        }
    }

    // Compute RTT Statistics (Min, Max, Mean, Standard Deviation)
    private void computeRTTStatistics(ArrayList<Long> rttTimes) {
        if (rttTimes.isEmpty()) return;

        long minRTT = Long.MAX_VALUE, maxRTT = Long.MIN_VALUE, sum = 0;
        for (long rtt : rttTimes) {
            minRTT = Math.min(minRTT, rtt);
            maxRTT = Math.max(maxRTT, rtt);
            sum += rtt;
        }
        double mean = sum / (double) rttTimes.size();

        // Compute Standard Deviation
        double variance = 0;
        for (long rtt : rttTimes) {
            variance += Math.pow(rtt - mean, 2);
        }
        variance /= rttTimes.size();
        double stdDev = Math.sqrt(variance);

        System.out.println("\nRTT Statistics:");
        System.out.println("Min RTT: " + minRTT + " ms");
        System.out.println("Max RTT: " + maxRTT + " ms");
        System.out.println("Mean RTT: " + mean + " ms");
        System.out.println("Standard Deviation: " + stdDev + " ms");
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        if (args.length == 0) {
        Client c = new Client("127.0.0.1", 6000);
      } else if ( args.length == 2) {
        try{
            Client c = new Client(args[0], Integer.valueOf(args[1]));
        }
        catch(Error e) {
            System.out.println("Please enter a valid IP address and port number.");
        }
      } else {

        System.out.println("Please enter a valid IP address and port number or leave the command line arguments blank to connect to the local 6000 port.");
      }

    }
}
