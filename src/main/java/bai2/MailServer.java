package bai2;

import java.io.*;
import java.net.*;

public class MailServer {
    private static final int PORT = 5000;
    private static final String ROOT_DIR = "mail_server_data";

    public static void main(String[] args) {
        File baseDir = new File(ROOT_DIR);
        if (!baseDir.exists()) baseDir.mkdirs();

        System.out.println(">>> UDP Mail Server đang chạy trên port " + PORT + "...");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveData = new byte[4096];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String request = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim();
                System.out.println("\n[LOG] Nhận từ Client (" + receivePacket.getAddress().getHostAddress() + "): " + request);

                // Xử lý logic và chuẩn bị chuỗi phản hồi
                String response = processRequest(request);

                // Gửi gói tin UDP phản hồi về Client
                byte[] sendData = response.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());

                socket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String processRequest(String request) {
        if (request.startsWith("REGISTER ")) {
            return handleRegister(request.substring(9).trim());
        } else if (request.startsWith("LOGIN ")) {
            return handleLogin(request.substring(6).trim());
        } else if (request.startsWith("SEND ")) {
            return handleSendEmail(request.substring(5).trim());
        } else if (request.startsWith("READ ")) {
            return handleReadEmail(request.substring(5).trim());
        }
        return "ERROR: Lệnh không hợp lệ.";
    }

    // 1. Tạo Account & File new_email.txt
    private static String handleRegister(String account) {
        File userFolder = new File(ROOT_DIR + File.separator + account);
        if (!userFolder.exists()) {
            if (userFolder.mkdirs()) {
                File welcomeFile = new File(userFolder, "new_email.txt");
                try (FileWriter writer = new FileWriter(welcomeFile)) {
                    writer.write("Thank you for using this service. we hope that you will feel comfortabl........");
                } catch (IOException e) {
                    return "ERROR: Không thể tạo file chào mừng.";
                }
                System.out.println("[LOG SUCCESS] Đã tạo tài khoản & file new_email.txt cho: " + account);
                return "SUCCESS: Đã tạo tài khoản thành công cho " + account;
            }
            return "ERROR: Không thể tạo thư mục.";
        }
        System.out.println("[LOG WARN] Tài khoản đã tồn tại: " + account);
        return "ERROR: Tài khoản đã tồn tại.";
    }

    // 2. Nhận Email & Lưu File vào thư mục người nhận
    private static String handleSendEmail(String payload) {
        String[] parts = payload.split("#", 4);
        if (parts.length < 4) return "ERROR: Cấu trúc email sai.";

        String sender = parts[0];
        String receiver = parts[1];
        String title = parts[2];
        String content = parts[3];

        File receiverFolder = new File(ROOT_DIR + File.separator + receiver);
        if (!receiverFolder.exists()) {
            System.out.println("[LOG ERROR] Gửi thất bại: Không tìm thấy " + receiver);
            return "ERROR: Người nhận không tồn tại trên hệ thống.";
        }

        String fileName = "email_from_" + sender + "_" + System.currentTimeMillis() + ".txt";
        File emailFile = new File(receiverFolder, fileName);

        try (FileWriter writer = new FileWriter(emailFile)) {
            writer.write("From: " + sender + "\n");
            writer.write("Title: " + title + "\n");
            writer.write("Content:\n" + content);

            System.out.println("[LOG SUCCESS] " + sender + " -> " + receiver + " (" + fileName + ")");
            return "SUCCESS: Đã gửi email tới " + receiver;
        } catch (IOException e) {
            return "ERROR: Lỗi lưu file email.";
        }
    }

    // 3. Đăng nhập & Liệt kê danh sách File
    private static String handleLogin(String account) {
        File userFolder = new File(ROOT_DIR + File.separator + account);
        if (!userFolder.exists()) {
            return "ERROR: Tài khoản không tồn tại.";
        }

        File[] files = userFolder.listFiles();
        if (files == null || files.length == 0) {
            return "FILES: (Thư mục trống)";
        }

        StringBuilder sb = new StringBuilder("FILES: ");
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                sb.append(files[i].getName());
                if (i < files.length - 1) sb.append(", ");
            }
        }
        System.out.println("[LOG SUCCESS] " + account + " đã lấy danh sách file.");
        return sb.toString();
    }

    // 4. Đọc nội dung file email
    private static String handleReadEmail(String payload) {
        String[] parts = payload.split("#", 2);
        if (parts.length < 2) return "ERROR: Cấu trúc lệnh đọc mail sai.";

        String account = parts[0];
        String fileName = parts[1];

        File emailFile = new File(ROOT_DIR + File.separator + account + File.separator + fileName);
        if (!emailFile.exists()) {
            return "ERROR: File email không tồn tại.";
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(emailFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            System.out.println("[LOG SUCCESS] " + account + " đã đọc file: " + fileName);
            return "NỘI DUNG EMAIL:\n-------------------------------------\n" + sb.toString().trim() + "\n-------------------------------------";
        } catch (IOException e) {
            return "ERROR: Không thể đọc file email.";
        }
    }
}