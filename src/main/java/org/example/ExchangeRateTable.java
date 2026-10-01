package org.example;

import java.io.*;
import java.net.*;

import java.net.*;

public class ExchangeRateTable {
    private static final String SERVER_IP = "100.92.122.114";
    private static final int SERVER_PORT = 2345;

    public static void main(String[] args) {
        System.out.println(">>> UDP Exchange Rate Client bắt đầu...");
        System.out.println("-------------------------------------------------------------------------------");

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            clientSocket.setSoTimeout(2000); // Chờ tối đa 2s
            InetAddress IPAddress = InetAddress.getByName(SERVER_IP);

            while (true) {
                try {
                    String message = "GET_RATES";
                    byte[] sendData = message.getBytes("UTF-8");

                    // Gửi request UDP
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SERVER_PORT);
                    clientSocket.send(sendPacket);

                    // Nhận response UDP
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);

                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                    System.out.println(response);

                } catch (SocketTimeoutException e) {
                    System.err.println("Không nhận được phản hồi từ Server (Timeout).");
                }

                // Tạm dừng 1 giây
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}