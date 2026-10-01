package bai2;

import java.net.*;
import java.util.Scanner;

public class MailClient {
    // Thay "127.0.0.1" thành IP thực tế của máy Server khi chạy 2 máy
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n========== UDP MAIL CLIENT MENU ==========");
            System.out.println("1. Tạo account mới");
            System.out.println("2. Đăng nhập (Xem danh sách email)");
            System.out.println("3. Gửi Email");
            System.out.println("4. Đọc nội dung 1 Email");
            System.out.println("0. Thoát");
            System.out.print("Chọn chức năng (0-4): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Nhập tên account mới cần tạo: ");
                    String newAcc = scanner.nextLine();
                    sendUDPCommand("REGISTER " + newAcc);
                    break;

                case "2":
                    System.out.print("Nhập account đăng nhập: ");
                    String loginAcc = scanner.nextLine();
                    sendUDPCommand("LOGIN " + loginAcc);
                    break;

                case "3":
                    System.out.print("Tên người gửi (Account của bạn): ");
                    String sender = scanner.nextLine();
                    System.out.print("Tên người nhận: ");
                    String receiver = scanner.nextLine();
                    System.out.print("Tiêu đề email: ");
                    String title = scanner.nextLine();
                    System.out.print("Nội dung email: ");
                    String content = scanner.nextLine();

                    String payload = sender + "#" + receiver + "#" + title + "#" + content;
                    sendUDPCommand("SEND " + payload);
                    break;

                case "4":
                    System.out.print("Nhập account của bạn: ");
                    String myAcc = scanner.nextLine();
                    System.out.print("Nhập tên file cần đọc (VD: new_email.txt): ");
                    String fileName = scanner.nextLine();
                    sendUDPCommand("READ " + myAcc + "#" + fileName);
                    break;

                case "0":
                    System.out.println("Đã thoát chương trình.");
                    scanner.close();
                    return;

                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void sendUDPCommand(String command) {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            clientSocket.setSoTimeout(3000); // Set timeout 3s
            InetAddress IPAddress = InetAddress.getByName(SERVER_IP);

            byte[] sendData = command.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SERVER_PORT);
            clientSocket.send(sendPacket);

            byte[] receiveData = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
            System.out.println("\n>> Server trả về:\n" + response);

        } catch (SocketTimeoutException e) {
            System.err.println("Khong nhan duoc phan hoi tu Mail Server (Timeout).");
        } catch (Exception e) {
            System.err.println("Lỗi gửi dữ liệu qua UDP: " + e.getMessage());
        }
    }
}