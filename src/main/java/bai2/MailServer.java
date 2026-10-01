package bai2;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MailServer {
    private static final int PORT = 5000;
    private static final String ROOT_DIR = "mail_server_data";

    private static String getTimestamp() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
    }

    public static void main(String[] args) {
        File baseDir = new File(ROOT_DIR);
        if (!baseDir.exists()) baseDir.mkdirs();

        System.out.println(">>> [" + getTimestamp() + "] UDP Mail Server đang chạy trên port " + PORT + "...");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveData = new byte[4096];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String request = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim();
                System.out.println("\n[" + getTimestamp() + "] [LOG] Nhận từ Client ("
                        + receivePacket.getAddress().getHostAddress() + "): " + request);

                String response = processRequest(request);

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

    // 1. Đăng ký Account + Mật khẩu + In Ngày giờ
    private static String handleRegister(String payload) {
        String[] parts = payload.split("#", 2);
        if (parts.length < 2) return "ERROR: Thiếu tài khoản hoặc mật khẩu.";

        String account = parts[0].trim();
        String password = parts[1].trim();

        File userFolder = new File(ROOT_DIR + File.separator + account);
        if (userFolder.exists()) {
            System.out.println("[" + getTimestamp() + "] [LOG WARN] Đăng ký thất bại: Tài khoản " + account + " đã tồn tại.");
            return "ERROR: Tài khoản đã tồn tại.";
        }

        if (userFolder.mkdirs()) {
            // Lưu file mật khẩu
            File passFile = new File(userFolder, "password.txt");
            try (FileWriter writer = new FileWriter(passFile)) {
                writer.write(password);
            } catch (IOException e) {
                return "ERROR: Không thể lưu mật khẩu.";
            }

            // Lưu thêm file thông tin thời gian đăng ký
            File infoFile = new File(userFolder, "created_at.txt");
            try (FileWriter writer = new FileWriter(infoFile)) {
                writer.write("Registered at: " + getTimestamp());
            } catch (IOException e) {}

            // Tạo thư chào mừng
            File welcomeFile = new File(userFolder, "new_email.txt");
            try (FileWriter writer = new FileWriter(welcomeFile)) {
                writer.write("From: System\nTitle: Welcome\nContent:\nCam on ban da su dung dich vu UDP Mail!");
            } catch (IOException e) { }

            System.out.println("[" + getTimestamp() + "] [LOG SUCCESS] Đã tạo tài khoản & mật khẩu cho: " + account);
            return "SUCCESS: Đã tạo tài khoản thành công!";
        }
        return "ERROR: Không thể tạo tài khoản.";
    }

    // 2. Đăng nhập + In Ngày giờ
    private static String handleLogin(String payload) {
        String[] parts = payload.split("#", 2);
        if (parts.length < 2) return "ERROR: Thiếu tài khoản hoặc mật khẩu.";

        String account = parts[0].trim();
        String password = parts[1].trim();

        File userFolder = new File(ROOT_DIR + File.separator + account);
        if (!userFolder.exists()) {
            System.out.println("[" + getTimestamp() + "] [LOG WARN] Đăng nhập thất bại: Tài khoản " + account + " không tồn tại.");
            return "ERROR: Tài khoản không tồn tại.";
        }

        File passFile = new File(userFolder, "password.txt");
        if (!passFile.exists()) return "ERROR: Mật khẩu chưa được khởi tạo.";

        try (BufferedReader reader = new BufferedReader(new FileReader(passFile))) {
            String savedPassword = reader.readLine();
            if (savedPassword == null || !savedPassword.equals(password)) {
                System.out.println("[" + getTimestamp() + "] [LOG WARN] Sai mật khẩu cho tài khoản: " + account);
                return "ERROR: Mật khẩu không chính xác.";
            }
        } catch (IOException e) {
            return "ERROR: Lỗi kiểm tra mật khẩu.";
        }

        File[] files = userFolder.listFiles();
        if (files == null || files.length <= 1) {
            return "FILES: (Không có email nào)";
        }

        StringBuilder sb = new StringBuilder("FILES: ");
        boolean first = true;
        for (File f : files) {
            // Bỏ qua các file cấu hình hệ thống
            if (f.isFile() && !f.getName().equals("password.txt") && !f.getName().equals("created_at.txt")) {
                if (!first) sb.append(", ");
                sb.append(f.getName());
                first = false;
            }
        }
        System.out.println("[" + getTimestamp() + "] [LOG SUCCESS] Tài khoản '" + account + "' đã đăng nhập thành công.");
        return sb.toString();
    }

    // 3. Gửi Email
    private static String handleSendEmail(String payload) {
        String[] parts = payload.split("#", 4);
        if (parts.length < 4) return "ERROR: Cấu trúc email sai.";

        String sender = parts[0];
        String receiver = parts[1];
        String title = parts[2];
        String content = parts[3];

        File receiverFolder = new File(ROOT_DIR + File.separator + receiver);
        if (!receiverFolder.exists()) {
            return "ERROR: Người nhận '" + receiver + "' không tồn tại.";
        }

        String fileName = "email_from_" + sender + "_" + System.currentTimeMillis() + ".txt";
        File emailFile = new File(receiverFolder, fileName);

        try (FileWriter writer = new FileWriter(emailFile)) {
            writer.write("From: " + sender + "\n");
            writer.write("Date: " + getTimestamp() + "\n");
            writer.write("Title: " + title + "\n");
            writer.write("Content:\n" + content);

            System.out.println("[" + getTimestamp() + "] [LOG SUCCESS] " + sender + " -> " + receiver + " (" + fileName + ")");
            return "SUCCESS: Đã gửi email tới " + receiver;
        } catch (IOException e) {
            return "ERROR: Lỗi lưu file email.";
        }
    }

    // 4. Đọc nội dung email
    private static String handleReadEmail(String payload) {
        String[] parts = payload.split("#", 2);
        if (parts.length < 2) return "ERROR: Lệnh đọc mail sai.";

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
            System.out.println("[" + getTimestamp() + "] [LOG SUCCESS] " + account + " đã đọc file: " + fileName);
            return sb.toString().trim();
        } catch (IOException e) {
            return "ERROR: Không thể đọc file email.";
        }
    }
}