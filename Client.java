// Demonstrating Client-side Programming with Timer
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {
  
    // Initialize socket and input/output streams
    private Socket s = null;
    private DataInputStream termIn = null;
    private DataOutputStream out = null;
    private DataInputStream serverIn = null;

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

        // Perform at least 5 message exchanges for RTT calculations
        for (int i = 0; i < 5; i++) {
            try {
                System.out.print("Enter a message: ");
                m = termIn.readLine();

                // Start the timer
                long startTime = System.currentTimeMillis();

                // Send message to server
                out.writeUTF(m);

                // Listen for response
                m = serverIn.readUTF();

                // Stop the timer
                long endTime = System.currentTimeMillis();

                // Calculate round-trip time (RTT) in milliseconds
                long rtt = endTime - startTime;
                rttTimes.add(rtt);

                System.out.println("Server response: " + m);
                System.out.println("RTT: " + rtt + " ms");

                if (m.equals("Please send an alphabetical message.")) {
                    System.out.println("Error received. Please enter a valid string.");
                    i--; // Repeat the attempt
                }

            } catch (IOException e) {
                System.out.println("IO Exception: " + e);
            }
        }

        // Compute statistics
        computeRTTStatistics(rttTimes);

        // Close the connection gracefully
        try {
            out.writeUTF("bye");
            String response = serverIn.readUTF();
            System.out.println(response);
            if (response.equals("disconnected")) {
                System.out.println("Exit");
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
            new Client("127.0.0.1", 6000);
        } else if (args.length == 2) {
            new Client(args[0], Integer.valueOf(args[1]));
        } else {
            System.out.println("FAIL");
        }
    }
}
