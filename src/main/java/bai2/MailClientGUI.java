package bai2;
import javax.swing.*;
import java.awt.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class MailClientGUI extends JFrame {
    private JTextField txtServerIP, txtServerPort, txtUser, txtReceiver, txtTitle;
    private JPasswordField txtPass;
    private JTextArea txtContent, txtEmailBody;
    private DefaultListModel<String> listModel;
    private JList<String> listEmails;
    private JLabel lblStatus;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String currentUser = "";

    public MailClientGUI() {
        setTitle("UDP Mail Client - Giao Diện");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "LOGIN_SCREEN");
        mainPanel.add(createDashboardPanel(), "DASHBOARD_SCREEN");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN_SCREEN");
    }

    // 1. Màn hình Đăng nhập / Đăng ký
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("ĐĂNG NHẬP / ĐĂNG KÝ"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtServerIP = new JTextField("127.0.0.1", 15);
        txtServerPort = new JTextField("5000", 15);
        txtUser = new JTextField(15);
        txtPass = new JPasswordField(15);

        JButton btnLogin = new JButton("Đăng nhập");
        JButton btnRegister = new JButton("Đăng ký mới");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Server IP:"), gbc);
        gbc.gridx = 1; panel.add(txtServerIP, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Server Port:"), gbc);
        gbc.gridx = 1; panel.add(txtServerPort, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Tài khoản:"), gbc);
        gbc.gridx = 1; panel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; panel.add(txtPass, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        btnLogin.addActionListener(e -> performLogin());
        btnRegister.addActionListener(e -> performRegister());

        return panel;
    }

    // 2. Màn hình Bảng điều khiển Email
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Thanh công cụ trên cùng
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblStatus = new JLabel("Tài khoản: ");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        JButton btnRefresh = new JButton("Tải lại danh sách");
        JButton btnLogout = new JButton("Đăng xuất");

        topPanel.add(lblStatus);
        topPanel.add(btnRefresh);
        topPanel.add(btnLogout);
        panel.add(topPanel, BorderLayout.NORTH);

        // Bảng trung tâm (Chia 2 tab: Đọc Email & Gửi Email)
        JTabbedPane tabbedPane = new JTabbedPane();

        // TAB 1: Danh sách & Đọc Mail
        JPanel readPanel = new JPanel(new BorderLayout(5, 5));
        listModel = new DefaultListModel<>();
        listEmails = new JList<>(listModel);
        JScrollPane listScroll = new JScrollPane(listEmails);
        listScroll.setPreferredSize(new Dimension(220, 0));

        txtEmailBody = new JTextArea();
        txtEmailBody.setEditable(false);
        JScrollPane bodyScroll = new JScrollPane(txtEmailBody);

        readPanel.add(listScroll, BorderLayout.WEST);
        readPanel.add(bodyScroll, BorderLayout.CENTER);
        tabbedPane.addTab("Đọc Email", readPanel);

        // TAB 2: Soạn & Gửi Mail
        JPanel sendPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtReceiver = new JTextField(20);
        txtTitle = new JTextField(20);
        txtContent = new JTextArea(8, 20);
        JButton btnSend = new JButton("Gửi Email");

        gbc.gridx = 0; gbc.gridy = 0; sendPanel.add(new JLabel("Người nhận:"), gbc);
        gbc.gridx = 1; sendPanel.add(txtReceiver, gbc);

        gbc.gridx = 0; gbc.gridy = 1; sendPanel.add(new JLabel("Tiêu đề:"), gbc);
        gbc.gridx = 1; sendPanel.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 2; sendPanel.add(new JLabel("Nội dung:"), gbc);
        gbc.gridx = 1; sendPanel.add(new JScrollPane(txtContent), gbc);

        gbc.gridx = 1; gbc.gridy = 3; sendPanel.add(btnSend, gbc);
        tabbedPane.addTab("Soạn Email", sendPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);

        // Sự kiện
        listEmails.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listEmails.getSelectedValue() != null) {
                readSelectedEmail(listEmails.getSelectedValue());
            }
        });

        btnRefresh.addActionListener(e -> refreshEmailList());
        btnSend.addActionListener(e -> performSendEmail());
        btnLogout.addActionListener(e -> {
            currentUser = "";
            cardLayout.show(mainPanel, "LOGIN_SCREEN");
        });

        return panel;
    }

    // XL Đăng nhập
    private void performLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        String res = sendUDPCommand("LOGIN " + user + "#" + pass);
        if (res.startsWith("FILES:")) {
            currentUser = user;
            lblStatus.setText("Đang đăng nhập: " + currentUser);
            updateEmailListFromResponse(res);
            cardLayout.show(mainPanel, "DASHBOARD_SCREEN");
        } else {
            JOptionPane.showMessageDialog(this, res, "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        }
    }

    // XL Đăng ký
    private void performRegister() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tài khoản và mật khẩu muốn tạo!");
            return;
        }

        String res = sendUDPCommand("REGISTER " + user + "#" + pass);
        JOptionPane.showMessageDialog(this, res);
    }

    // XL Tải lại danh sách
    private void refreshEmailList() {
        String pass = new String(txtPass.getPassword()).trim();
        String res = sendUDPCommand("LOGIN " + currentUser + "#" + pass);
        if (res.startsWith("FILES:")) {
            updateEmailListFromResponse(res);
            txtEmailBody.setText("");
        }
    }

    private void updateEmailListFromResponse(String response) {
        listModel.clear();
        String filesStr = response.substring(6).trim();
        if (!filesStr.equals("(Không có email nào)")) {
            String[] files = filesStr.split(",");
            for (String f : files) {
                listModel.addElement(f.trim());
            }
        }
    }

    // XL Đọc mail được chọn
    private void readSelectedEmail(String fileName) {
        String res = sendUDPCommand("READ " + currentUser + "#" + fileName);
        txtEmailBody.setText(res);
    }

    // XL Gửi mail
    private void performSendEmail() {
        String receiver = txtReceiver.getText().trim();
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();

        if (receiver.isEmpty() || title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ người nhận, tiêu đề và nội dung!");
            return;
        }

        String payload = currentUser + "#" + receiver + "#" + title + "#" + content;
        String res = sendUDPCommand("SEND " + payload);
        JOptionPane.showMessageDialog(this, res);

        if (res.startsWith("SUCCESS:")) {
            txtReceiver.setText("");
            txtTitle.setText("");
            txtContent.setText("");
        }
    }

    // Hàm gửi/nhận UDP chung
    private String sendUDPCommand(String command) {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            clientSocket.setSoTimeout(3000);
            InetAddress ip = InetAddress.getByName(txtServerIP.getText().trim());
            int port = Integer.parseInt(txtServerPort.getText().trim());

            byte[] sendData = command.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
            clientSocket.send(sendPacket);

            byte[] receiveData = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            return new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
        } catch (SocketTimeoutException e) {
            return "ERROR: Hết thời gian chờ phản hồi từ Server (Timeout).";
        } catch (Exception e) {
            return "ERROR: Không thể kết nối Server: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MailClientGUI().setVisible(true));
    }
}
