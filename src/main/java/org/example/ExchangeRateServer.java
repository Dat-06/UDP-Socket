package org.example;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ExchangeRateServer {
    private static final int PORT = 2345;

    public static void main(String[] args) {
        System.out.println(">>> Exchange Rate Server dang chay tren port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String request = in.readLine();
                if (request != null && request.equalsIgnoreCase("GET_RATES")) {
                    Random rand = new Random();

                    double tokyo = 140.0 + rand.nextDouble() * 10;
                    double newyork = 1.05 + rand.nextDouble() * 0.1;
                    double hongkong = 7.75 + rand.nextDouble() * 0.2;

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String currentTime = formatter.format(new Date());

                    String response = String.format("[%s] Tokyo: %.2f JPY/USD | New York: %.2f EUR/USD | Hong Kong: %.2f HKD/USD",
                            currentTime, tokyo, newyork, hongkong);

                    out.println(response);
                }
            } catch (IOException e) {
                System.err.println("Loi xuly client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
