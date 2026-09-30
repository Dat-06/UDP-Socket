package bai2;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MailClient {
    private static final String SERVER_IP = "100.92.122.114";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n========== MAIL CLIENT MENU ==========");
            System.out.println("1. Tạo account mới");
            System.out.println("2. Đăng nhập (Xem danh sách email)");
            System.out.println("3. Gửi Email");
            System.out.println("0. Thoát");
            System.out.print("Chọn chức năng (0-3): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Nhập tên account mới cần tạo: ");
                    String newAcc = scanner.nextLine();
                    sendCommand("REGISTER " + newAcc);
                    break;

                case "2":
                    System.out.print("Nhập account đăng nhập: ");
                    String loginAcc = scanner.nextLine();
                    sendCommand("LOGIN " + loginAcc);
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

                    // Đóng gói nội dung cách nhau bởi dấu #
                    String payload = sender + "#" + receiver + "#" + title + "#" + content;
                    sendCommand("SEND " + payload);
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

    private static void sendCommand(String command) {
        try (
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println(command);
            String response = in.readLine();
            System.out.println(">> Server trả về: " + response);
        } catch (IOException e) {
            System.err.println("Không thể kết nối tới Server tại " + SERVER_IP + ":" + SERVER_PORT);
        }
    }
}
