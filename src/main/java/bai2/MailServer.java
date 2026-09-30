package bai2;

import java.io.*;
import java.net.*;

public class MailServer {
    private static final int PORT = 5000;
    private static final String ROOT_DIR = "mail_server_data";

    public static void main(String[] args) {
        File baseDir = new File(ROOT_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        System.out.println(">>> Mail Server đang chạy trên port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new MailClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MailClientHandler implements Runnable {
        private Socket socket;

        public MailClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String clientRequest = in.readLine();
                if (clientRequest == null) return;

                if (clientRequest.startsWith("REGISTER ")) {
                    String account = clientRequest.substring(9).trim();
                    handleRegister(account, out);
                } else if (clientRequest.startsWith("LOGIN ")) {
                    String account = clientRequest.substring(6).trim();
                    handleLogin(account, out);
                } else if (clientRequest.startsWith("SEND ")) {
                    String payload = clientRequest.substring(5).trim();
                    handleSendEmail(payload, out);
                } else {
                    out.println("ERROR: Lệnh không hợp lệ.");
                }

            } catch (IOException e) {
                System.err.println("Lỗi xử lý Client: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException e) {}
            }
        }

        // 1. Tạo Account & File new_email.txt
        private void handleRegister(String account, PrintWriter out) {
            File userFolder = new File(ROOT_DIR + File.separator + account);
            if (!userFolder.exists()) {
                boolean created = userFolder.mkdirs();
                if (created) {
                    File welcomeFile = new File(userFolder, "new_email.txt");
                    try (FileWriter writer = new FileWriter(welcomeFile)) {
                        writer.write("Thank you for using this service. we hope that you will feel comfortabl........");
                    } catch (IOException e) {
                        out.println("ERROR: Không thể tạo file chào mừng.");
                        return;
                    }
                    out.println("SUCCESS: Đã tạo tài khoản thành công cho " + account);
                } else {
                    out.println("ERROR: Không thể tạo thư mục tài khoản.");
                }
            } else {
                out.println("ERROR: Tài khoản đã tồn tại.");
            }
        }

        // 2. Nhận Email & Tạo File trong Thư mục Người nhận
        private void handleSendEmail(String payload, PrintWriter out) {
            // Định dạng: sender#receiver#title#content
            String[] parts = payload.split("#", 4);
            if (parts.length < 4) {
                out.println("ERROR: Cấu trúc email sai.");
                return;
            }

            String sender = parts[0];
            String receiver = parts[1];
            String title = parts[2];
            String content = parts[3];

            File receiverFolder = new File(ROOT_DIR + File.separator + receiver);
            if (!receiverFolder.exists()) {
                out.println("ERROR: Người nhận không tồn tại trên hệ thống.");
                return;
            }

            String fileName = "email_from_" + sender + "_" + System.currentTimeMillis() + ".txt";
            File emailFile = new File(receiverFolder, fileName);

            try (FileWriter writer = new FileWriter(emailFile)) {
                writer.write("From: " + sender + "\n");
                writer.write("Title: " + title + "\n");
                writer.write("Content:\n" + content);
                out.println("SUCCESS: Đã gửi email tới " + receiver);
            } catch (IOException e) {
                out.println("ERROR: Lỗi lưu file email.");
            }
        }

        // 3. Đăng nhập & Trả về Danh sách Các File Email
        private void handleLogin(String account, PrintWriter out) {
            File userFolder = new File(ROOT_DIR + File.separator + account);
            if (!userFolder.exists()) {
                out.println("ERROR: Tài khoản không tồn tại.");
                return;
            }

            File[] files = userFolder.listFiles();
            if (files == null || files.length == 0) {
                out.println("FILES: (Thư mục trống)");
            } else {
                StringBuilder sb = new StringBuilder("FILES: ");
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        sb.append(files[i].getName());
                        if (i < files.length - 1) sb.append(", ");
                    }
                }
                out.println(sb.toString());
            }
        }
    }
}