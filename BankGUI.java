import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

// --- DATA CLASS (Serializable) ---
class User implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
    String accNum;
    String password;
    String email;
    double balance;
    ArrayList<String> transactions;
    ArrayList<String> accessLogs;

    public User(String name, String accNum, String password, String email, double balance) {
        this.name = name;
        this.accNum = accNum;
        this.password = password;
        this.email = email;
        this.balance = balance;
        this.transactions = new ArrayList<>();
        this.accessLogs = new ArrayList<>();
        
        String time = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        this.transactions.add("Account opened with ₹" + balance + " | " + time);
    }
}

public class BankGUI {

    // --- COLORS ---
    static final Color COL_SIDEBAR = new Color(30, 39, 46);
    static final Color COL_ACTIVE  = new Color(52, 152, 219);
    static final Color COL_BG      = new Color(245, 246, 250);
    static final Color COL_ACCENT  = new Color(46, 204, 113);
    static final Font FONT_SIDE    = new Font("Segoe UI", Font.PLAIN, 16);
    static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 22);

    // --- DATA ---
    static HashMap<String, User> allUsers = new HashMap<>();
    static User currentUser = null;
    static final String ADMIN_ID = "admin";
    static final String ADMIN_PASS = "sai@7078";
    static final String FILE_NAME = "bank_data.dat";

    // --- UI COMPONENTS ---
    static JFrame frame;
    static CardLayout mainLayout; 
    static JPanel mainPanel;
    
    // --- DASHBOARD COMPONENTS ---
    static CardLayout dashContentLayout; 
    static JPanel dashContentPanel;
    static JLabel lblDashBalance, lblDashName, lblDashAcc;
    static DefaultTableModel homeModel;
    static DefaultTableModel historyModel;

    public static void main(String[] args) {
        loadData();

        frame = new JFrame("National Bank - Professional Suite");
        frame.setSize(1100, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { saveData(); }
        });

        mainLayout = new CardLayout();
        mainPanel = new JPanel(mainLayout);

        mainPanel.add(createLoginScreen(), "Login");
        mainPanel.add(createRegisterScreen(), "Register");
        mainPanel.add(createForgotScreen(), "Forgot");
        mainPanel.add(createUserDashboard(), "UserDash");
        mainPanel.add(createAdminDashboard(), "AdminDash");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // =========================================================================
    // 1. DATA SAVING & LOADING
    // =========================================================================
    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(allUsers);
        } catch (IOException e) { System.out.println("Error saving data"); }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            allUsers = (HashMap<String, User>) ois.readObject();
        } catch (Exception e) { allUsers = new HashMap<>(); }
    }

    private static String getCurrentTime() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    // =========================================================================
    // 2. LOGIN SCREEN (LOGO SPREAD OUT & LOOKING NICE)
    // =========================================================================
    private static JPanel createLoginScreen() {
        JPanel panel = new JPanel(null);
        panel.setBackground(COL_SIDEBAR);

        JPanel card = new JPanel(null);
        // INCREASED HEIGHT SIGNIFICANTLY (580px) to fit big logo nicely
        card.setBounds(350, 50, 400, 580); 
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 5, 0, COL_ACTIVE));
        panel.add(card);

        JLabel logo = new JLabel("NB", SwingConstants.CENTER);
        try {
            ImageIcon icon = new ImageIcon("logo.png");
            // SPREAD IT: Increased scaling to 220x220 for a big, clear look
            Image img = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(img));
            logo.setText("");
        } catch (Exception e) { 
            logo.setFont(new Font("SansSerif", Font.BOLD, 80)); logo.setForeground(COL_ACTIVE);
        }
        // RE-CENTERED the bigger logo: (400 width - 220 logo) / 2 = 90 x-offset
        logo.setBounds(90, 20, 220, 220);
        card.add(logo);

        // SHIFTED ALL COMPONENTS DOWN to make space for the big logo
        JLabel lblTitle = new JLabel("Secure Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COL_SIDEBAR);
        lblTitle.setBounds(50, 260, 300, 30); // Shifted down
        card.add(lblTitle);

        JTextField txtId = createStyledField("Account Number");
        txtId.setBounds(50, 310, 300, 40); // Shifted down
        card.add(txtId);

        JPasswordField txtPass = createStyledPassField("Password");
        txtPass.setBounds(50, 360, 300, 40); // Shifted down
        card.add(txtPass);

        JButton btnLogin = createStyledButton("LOGIN", COL_ACTIVE);
        btnLogin.setBounds(50, 420, 300, 45); // Shifted down
        card.add(btnLogin);

        JLabel lblForgot = new JLabel("Forgot Password?", SwingConstants.CENTER);
        lblForgot.setForeground(COL_ACTIVE);
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgot.setBounds(50, 480, 300, 20); // Shifted down
        card.add(lblForgot);

        JLabel lblReg = new JLabel("Create New Account", SwingConstants.CENTER);
        lblReg.setForeground(Color.GRAY);
        lblReg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblReg.setBounds(50, 510, 300, 20); // Shifted down
        card.add(lblReg);

        // Actions
        btnLogin.addActionListener(e -> {
            String id = txtId.getText();
            String pass = new String(txtPass.getPassword());

            if (id.equals(ADMIN_ID) && pass.equals(ADMIN_PASS)) {
                refreshAdminTable();
                mainLayout.show(mainPanel, "AdminDash");
                txtId.setText("Account Number"); txtPass.setText("Password"); txtPass.setEchoChar((char) 0);
            } else if (allUsers.containsKey(id) && allUsers.get(id).password.equals(pass)) {
                currentUser = allUsers.get(id);
                currentUser.accessLogs.add(0, "Logged In | " + getCurrentTime());
                saveData();
                updateDashboardData();
                dashContentLayout.show(dashContentPanel, "Home"); 
                mainLayout.show(mainPanel, "UserDash");
                txtId.setText("Account Number"); txtPass.setText("Password"); txtPass.setEchoChar((char) 0);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid Credentials");
            }
        });

        lblReg.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { mainLayout.show(mainPanel, "Register"); }
        });
        
        lblForgot.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { mainLayout.show(mainPanel, "Forgot"); }
        });
        
        addPlaceholder(txtId, "Account Number");
        addPlaceholderPass(txtPass, "Password");

        return panel;
    }

    // =========================================================================
    // 3. REGISTER SCREEN
    // =========================================================================
    private static JPanel createRegisterScreen() {
        JPanel panel = new JPanel(null);
        panel.setBackground(COL_BG);

        JPanel card = new JPanel(null);
        card.setBounds(300, 50, 500, 500);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(5, 0, 0, 0, COL_SIDEBAR));
        panel.add(card);

        JLabel title = new JLabel("Open New Account");
        title.setFont(FONT_BOLD);
        title.setBounds(40, 30, 300, 30);
        card.add(title);

        JTextField txtName = createStyledField("Full Name");
        txtName.setBounds(40, 80, 420, 40); card.add(txtName);

        JTextField txtEmail = createStyledField("Email Address");
        txtEmail.setBounds(40, 130, 420, 40); card.add(txtEmail);

        JPasswordField txtPass = createStyledPassField("Password (Min 8 Chars)");
        txtPass.setBounds(40, 180, 420, 40); card.add(txtPass);

        JTextField txtDep = createStyledField("Initial Deposit (₹)");
        txtDep.setBounds(40, 230, 420, 40); card.add(txtDep);

        JButton btnReg = createStyledButton("CREATE ACCOUNT", COL_ACCENT);
        btnReg.setBounds(40, 300, 420, 45); card.add(btnReg);

        JButton btnBack = createStyledButton("CANCEL", Color.GRAY);
        btnBack.setBounds(40, 355, 420, 35); card.add(btnBack);

        addPlaceholder(txtName, "Full Name");
        addPlaceholder(txtEmail, "Email Address");
        addPlaceholderPass(txtPass, "Password (Min 8 Chars)");
        addPlaceholder(txtDep, "Initial Deposit (₹)");

        btnReg.addActionListener(e -> {
            String name = txtName.getText();
            String pass = new String(txtPass.getPassword());
            String email = txtEmail.getText();
            String dep = txtDep.getText();

            if (pass.length() < 8 || name.equals("Full Name")) {
                JOptionPane.showMessageDialog(frame, "Invalid Inputs!"); return;
            }
            try {
                double bal = Double.parseDouble(dep);
                Random rand = new Random();
                String accNum = String.valueOf(100000 + rand.nextInt(900000));
                
                User u = new User(name, accNum, pass, email, bal);
                allUsers.put(accNum, u);
                saveData();
                
                JOptionPane.showMessageDialog(frame, "Account Created!\nAccount No: " + accNum);
                mainLayout.show(mainPanel, "Login");
                txtName.setText("Full Name"); txtEmail.setText("Email Address"); 
                txtPass.setText("Password (Min 8 Chars)"); txtPass.setEchoChar((char) 0);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Invalid Amount"); }
        });

        btnBack.addActionListener(e -> mainLayout.show(mainPanel, "Login"));

        return panel;
    }

    // =========================================================================
    // 4. FORGOT PASSWORD SCREEN
    // =========================================================================
    private static JPanel createForgotScreen() {
        JPanel panel = new JPanel(null);
        panel.setBackground(COL_BG);

        JPanel card = new JPanel(null);
        card.setBounds(300, 100, 500, 400);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(5, 0, 0, 0, Color.ORANGE));
        panel.add(card);

        JLabel title = new JLabel("Reset Password");
        title.setFont(FONT_BOLD);
        title.setBounds(40, 30, 300, 30);
        card.add(title);

        JTextField txtAcc = createStyledField("Enter Account Number");
        txtAcc.setBounds(40, 80, 420, 40); card.add(txtAcc);

        JTextField txtEmail = createStyledField("Enter Registered Email");
        txtEmail.setBounds(40, 130, 420, 40); card.add(txtEmail);

        JPasswordField txtNewPass = createStyledPassField("New Password");
        txtNewPass.setBounds(40, 180, 420, 40); card.add(txtNewPass);

        JButton btnReset = createStyledButton("RESET PASSWORD", Color.ORANGE);
        btnReset.setBounds(40, 250, 420, 45); card.add(btnReset);

        JButton btnBack = createStyledButton("BACK TO LOGIN", Color.GRAY);
        btnBack.setBounds(40, 305, 420, 35); card.add(btnBack);

        addPlaceholder(txtAcc, "Enter Account Number");
        addPlaceholder(txtEmail, "Enter Registered Email");
        addPlaceholderPass(txtNewPass, "New Password");

        btnReset.addActionListener(e -> {
            String acc = txtAcc.getText();
            String email = txtEmail.getText();
            String newPass = new String(txtNewPass.getPassword());

            if (allUsers.containsKey(acc)) {
                User u = allUsers.get(acc);
                if (u.email.equalsIgnoreCase(email)) {
                    if (newPass.length() >= 8) {
                        u.password = newPass;
                        saveData();
                        JOptionPane.showMessageDialog(frame, "Password Reset Successfully!");
                        mainLayout.show(mainPanel, "Login");
                        txtAcc.setText("Enter Account Number"); txtEmail.setText("Enter Registered Email");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Password must be 8+ chars!");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Email does not match this account!");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Account Not Found!");
            }
        });

        btnBack.addActionListener(e -> mainLayout.show(mainPanel, "Login"));

        return panel;
    }

    // =========================================================================
    // 5. USER DASHBOARD
    // =========================================================================
    private static JPanel createUserDashboard() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBackground(COL_SIDEBAR);

        JLabel lblApp = new JLabel("NATIONAL BANK", SwingConstants.CENTER);
        lblApp.setForeground(Color.WHITE);
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 20)); 
        lblApp.setBounds(0, 30, 220, 40);
        sidebar.add(lblApp);

        JButton btnHome = createSidebarBtn("Home", 100);
        JButton btnDep = createSidebarBtn("Deposit", 150);
        JButton btnWith = createSidebarBtn("Withdraw", 200);
        JButton btnTrans = createSidebarBtn("Transfer Money", 250); 
        JButton btnHist = createSidebarBtn("History", 300);
        JButton btnLogout = createSidebarBtn("Logout", 550);
        btnLogout.setForeground(new Color(231, 76, 60)); 

        sidebar.add(btnHome); sidebar.add(btnDep); sidebar.add(btnWith); 
        sidebar.add(btnTrans); sidebar.add(btnHist); sidebar.add(btnLogout);

        dashContentLayout = new CardLayout();
        dashContentPanel = new JPanel(dashContentLayout);
        dashContentPanel.setBackground(COL_BG);

        dashContentPanel.add(createHomeView(), "Home");
        dashContentPanel.add(createTransactionView("DEPOSIT", COL_ACCENT), "Deposit");
        dashContentPanel.add(createTransactionView("WITHDRAW", new Color(231, 76, 60)), "Withdraw");
        dashContentPanel.add(createTransferView(), "Transfer");
        dashContentPanel.add(createHistoryView(), "History");

        panel.add(sidebar, BorderLayout.WEST);
        panel.add(dashContentPanel, BorderLayout.CENTER);

        btnHome.addActionListener(e -> { updateDashboardData(); dashContentLayout.show(dashContentPanel, "Home"); });
        btnDep.addActionListener(e -> dashContentLayout.show(dashContentPanel, "Deposit"));
        btnWith.addActionListener(e -> dashContentLayout.show(dashContentPanel, "Withdraw"));
        btnTrans.addActionListener(e -> dashContentLayout.show(dashContentPanel, "Transfer"));
        btnHist.addActionListener(e -> { updateDashboardData(); dashContentLayout.show(dashContentPanel, "History"); });
        
        btnLogout.addActionListener(e -> { 
            if (currentUser != null) {
                currentUser.accessLogs.add(0, "Logged Out | " + getCurrentTime());
                saveData();
            }
            currentUser = null;
            mainLayout.show(mainPanel, "Login"); 
        });

        return panel;
    }

    private static JPanel createHomeView() {
        JPanel p = new JPanel(null);
        p.setBackground(COL_BG);

        JPanel header = new JPanel(null);
        header.setBounds(40, 30, 650, 150);
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 6, 0, 0, COL_ACTIVE));
        
        lblDashName = new JLabel("Welcome User");
        lblDashName.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDashName.setBounds(20, 20, 300, 20);
        header.add(lblDashName);

        lblDashBalance = new JLabel("₹ 0.00");
        lblDashBalance.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblDashBalance.setForeground(COL_SIDEBAR);
        lblDashBalance.setBounds(20, 50, 400, 60);
        header.add(lblDashBalance);

        lblDashAcc = new JLabel("AC: 000000");
        lblDashAcc.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblDashAcc.setForeground(Color.GRAY);
        lblDashAcc.setBounds(500, 20, 150, 20);
        header.add(lblDashAcc);

        p.add(header);

        JLabel lblH = new JLabel("Login Activity");
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblH.setBounds(40, 200, 200, 30);
        p.add(lblH);

        homeModel = new DefaultTableModel(new String[]{"Access Logs"}, 0);
        JTable table = new JTable(homeModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(40, 240, 650, 300);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        p.add(scroll);

        return p;
    }

    private static JPanel createHistoryView() {
        JPanel p = new JPanel(null);
        p.setBackground(COL_BG);

        JLabel lblH = new JLabel("Transaction History");
        lblH.setFont(FONT_BOLD);
        lblH.setBounds(40, 30, 300, 30);
        p.add(lblH);

        historyModel = new DefaultTableModel(new String[]{"Transaction Details"}, 0);
        JTable table = new JTable(historyModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(40, 80, 650, 450);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        p.add(scroll);

        return p;
    }

    private static JPanel createTransactionView(String type, Color btnColor) {
        JPanel p = new JPanel(null);
        p.setBackground(COL_BG);

        JLabel lblTitle = new JLabel(type + " MONEY");
        lblTitle.setFont(FONT_BOLD);
        lblTitle.setBounds(50, 50, 300, 30);
        p.add(lblTitle);

        JTextField txtAmt = createStyledField("Enter Amount");
        txtAmt.setBounds(50, 100, 400, 50);
        p.add(txtAmt);
        addPlaceholder(txtAmt, "Enter Amount");

        JButton btnSubmit = createStyledButton("CONFIRM " + type, btnColor);
        btnSubmit.setBounds(50, 170, 200, 50);
        p.add(btnSubmit);

        btnSubmit.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(txtAmt.getText());
                String time = getCurrentTime();
                if (type.equals("DEPOSIT")) {
                    currentUser.balance += amt;
                    currentUser.transactions.add(0, "Deposited ₹" + amt + " | " + time);
                    saveData();
                    JOptionPane.showMessageDialog(frame, "Success! New Balance: ₹" + currentUser.balance);
                } else {
                    if (amt <= currentUser.balance) {
                        currentUser.balance -= amt;
                        currentUser.transactions.add(0, "Withdrew ₹" + amt + " | " + time);
                        saveData();
                        JOptionPane.showMessageDialog(frame, "Withdrawal Successful!");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Insufficient Balance");
                    }
                }
                txtAmt.setText("Enter Amount"); txtAmt.setForeground(Color.GRAY);
                updateDashboardData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Invalid Amount"); }
        });

        return p;
    }

    private static JPanel createTransferView() {
        JPanel p = new JPanel(null);
        p.setBackground(COL_BG);

        JLabel lblTitle = new JLabel("TRANSFER FUNDS");
        lblTitle.setFont(FONT_BOLD);
        lblTitle.setBounds(50, 50, 300, 30);
        p.add(lblTitle);

        JTextField txtRec = createStyledField("Recipient Account Number");
        txtRec.setBounds(50, 100, 400, 50);
        p.add(txtRec);

        JTextField txtAmt = createStyledField("Amount to Send");
        txtAmt.setBounds(50, 160, 400, 50);
        p.add(txtAmt);

        JButton btnSend = createStyledButton("SEND MONEY", new Color(142, 68, 173)); 
        btnSend.setBounds(50, 230, 200, 50);
        p.add(btnSend);

        addPlaceholder(txtRec, "Recipient Account Number");
        addPlaceholder(txtAmt, "Amount to Send");

        btnSend.addActionListener(e -> {
            String recId = txtRec.getText();
            try {
                double amt = Double.parseDouble(txtAmt.getText());
                String time = getCurrentTime();
                
                if (!allUsers.containsKey(recId)) {
                    JOptionPane.showMessageDialog(frame, "Recipient Account Not Found!");
                } else if (amt > currentUser.balance) {
                    JOptionPane.showMessageDialog(frame, "Insufficient Balance!");
                } else if (recId.equals(currentUser.accNum)) {
                    JOptionPane.showMessageDialog(frame, "Cannot send to self!");
                } else {
                    User recipient = allUsers.get(recId);
                    currentUser.balance -= amt;
                    recipient.balance += amt;
                    
                    currentUser.transactions.add(0, "Sent ₹" + amt + " to " + recipient.name + " | " + time);
                    recipient.transactions.add(0, "Received ₹" + amt + " from " + currentUser.name + " | " + time);
                    
                    saveData();
                    JOptionPane.showMessageDialog(frame, "Transfer Successful!");
                    updateDashboardData();
                    txtRec.setText("Recipient Account Number"); txtAmt.setText("Amount to Send");
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Invalid Data"); }
        });

        return p;
    }

    private static void updateDashboardData() {
        if (currentUser == null) return;
        lblDashName.setText("Hello, " + currentUser.name);
        lblDashAcc.setText("AC: " + currentUser.accNum);
        lblDashBalance.setText("₹ " + String.format("%.2f", currentUser.balance));
        
        homeModel.setRowCount(0);
        for(String s : currentUser.accessLogs) homeModel.addRow(new Object[]{s});
        
        historyModel.setRowCount(0);
        for(String s : currentUser.transactions) historyModel.addRow(new Object[]{s});
    }

    // =========================================================================
    // 6. ADMIN DASHBOARD
    // =========================================================================
    static DefaultTableModel adminModel;
    static JTable adminTable;

    private static JPanel createAdminDashboard() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        head.setBackground(COL_SIDEBAR);
        
        JLabel l = new JLabel("  ADMIN PANEL"); l.setForeground(Color.WHITE); l.setFont(FONT_BOLD);
        JButton btnDel = new JButton("Delete Selected User"); 
        btnDel.setBackground(Color.ORANGE);
        
        JButton out = new JButton("Logout"); out.setBackground(Color.RED); out.setForeground(Color.WHITE);
        
        head.add(l); head.add(btnDel); head.add(out);
        p.add(head, BorderLayout.NORTH);

        adminModel = new DefaultTableModel(new String[]{"Acc No", "Name", "Email", "Pass", "Balance"}, 0);
        adminTable = new JTable(adminModel);
        adminTable.setRowHeight(30);
        p.add(new JScrollPane(adminTable), BorderLayout.CENTER);

        btnDel.addActionListener(e -> {
            int row = adminTable.getSelectedRow();
            if (row != -1) {
                String acc = (String) adminModel.getValueAt(row, 0);
                allUsers.remove(acc);
                saveData();
                refreshAdminTable();
                JOptionPane.showMessageDialog(frame, "User Deleted Successfully");
            } else {
                JOptionPane.showMessageDialog(frame, "Select a user row first!");
            }
        });

        out.addActionListener(e -> mainLayout.show(mainPanel, "Login"));
        return p;
    }
    private static void refreshAdminTable() {
        adminModel.setRowCount(0);
        for(User u : allUsers.values()) adminModel.addRow(new Object[]{u.accNum, u.name, u.email, u.password, "₹ " + u.balance});
    }

    // --- HELPERS (CRITICAL FOR STYLING) ---
    private static JButton createSidebarBtn(String text, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(0, y, 220, 50);
        btn.setFont(FONT_SIDE);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COL_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(0, 30, 0, 0)); 
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(50, 60, 70)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(COL_SIDEBAR); }
        });
        return btn;
    }
    private static JTextField createStyledField(String ph) {
        JTextField t = new JTextField(ph);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setForeground(Color.GRAY);
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return t;
    }
    private static JPasswordField createStyledPassField(String ph) {
        JPasswordField t = new JPasswordField(ph);
        t.setEchoChar((char) 0);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setForeground(Color.GRAY);
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return t;
    }
    private static JButton createStyledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private static void addPlaceholder(JTextField t, String ph) {
        t.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { if(t.getText().equals(ph)) { t.setText(""); t.setForeground(Color.BLACK); } }
            public void focusLost(java.awt.event.FocusEvent e) { if(t.getText().isEmpty()) { t.setForeground(Color.GRAY); t.setText(ph); } }
        });
    }
    private static void addPlaceholderPass(JPasswordField t, String ph) {
        t.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { if(String.valueOf(t.getPassword()).equals(ph)) { t.setText(""); t.setForeground(Color.BLACK); t.setEchoChar('•'); } }
            public void focusLost(java.awt.event.FocusEvent e) { if(String.valueOf(t.getPassword()).isEmpty()) { t.setForeground(Color.GRAY); t.setText(ph); t.setEchoChar((char)0); } }
        });
    }
}