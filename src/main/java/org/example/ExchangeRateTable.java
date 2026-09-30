package org.example;

import java.io.*;
import java.net.*;

public class ExchangeRateTable {
    private static final String SERVER_IP = "100.92.122.114";
    private static final int SERVER_PORT = 2345;
    public static void main(String[] args) {
        System.out.println(">>> Exchange Rate Client dang ket noi toi Server: " + SERVER_IP);
        System.out.println("-------------------------------------------------------------------------------");
        while (true) {
            try (
                    Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {

                out.println("GET_RATES");

                String response = in.readLine();
                if (response != null) {
                    System.out.println(response);
                }

                Thread.sleep(1000);

            } catch (UnknownHostException e) {
                System.err.println("Khong tim thấy Server IP: " + SERVER_IP);
                break;
            } catch (IOException e) {
                System.err.println("Loi ket noi Server. Thu lai sau 1s...");
                try { Thread.sleep(1000); } catch (InterruptedException ex) {}
            } catch (InterruptedException e) {
                System.out.println("Chuong trinh dung.");
                break;
            }
        }
    }
}
