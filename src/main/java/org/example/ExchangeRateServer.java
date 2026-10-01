package org.example;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ExchangeRateServer {
    private static final int PORT = 2345;

    public static void main(String[] args) {
        System.out.println(">>> UDP Exchange Rate Server đang chạy trên port " + PORT + "...");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveData = new byte[1024];

            while (true) {
                // Nhận gói tin từ Client
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String request = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim();

                if (request.equalsIgnoreCase("GET_RATES")) {
                    Random rand = new Random();
                    double tokyo = 140.0 + rand.nextDouble() * 10;
                    double newyork = 1.05 + rand.nextDouble() * 0.1;
                    double hongkong = 7.75 + rand.nextDouble() * 0.2;

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String currentTime = formatter.format(new Date());

                    String response = String.format("[%s] Tokyo: %.2f JPY/USD | New York: %.2f EUR/USD | Hong Kong: %.2f HKD/USD",
                            currentTime, tokyo, newyork, hongkong);

                    // Gửi gói tin trả về cho Client
                    byte[] sendData = response.getBytes("UTF-8");
                    DatagramPacket sendPacket = new DatagramPacket(
                            sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());

                    socket.send(sendPacket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}