package com.flipkart.app;

import com.flipkart.automation.FlipkartAutomationEngine;
import com.flipkart.model.OrderDetails;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MainApp — Flipkart Clone Selenium Automation
 *
 * Java Swing desktop application. Layout:
 * ┌─────────────────────────────────────────────────────┐
 * │  Flipkart Clone — Full Selenium Automation by reppyop         │
 * ├──────────────────────────┬──────────────────────────┤
 * │  Config Panel (left)     │  Automation Log (right)  │
 * │  - Webapp Path + Server  │  Green console output    │
 * │  - Product checkboxes    │  Progress bar            │
 * │  - Delivery Address      │  [RUN] [STOP] [CLEAR]    │
 * │  - Payment Details       │                          │
 * │  - Options               │                          │
 * └──────────────────────────┴──────────────────────────┘
 */
public class MainApp extends JFrame {

    // ── Flipkart brand colours ────────────────────────────────────────────
    private static final Color FK_BLUE    = new Color(40,  116, 240);
    private static final Color FK_YELLOW  = new Color(255, 215,   0);
    private static final Color FK_ORANGE  = new Color(251, 100,  27);
    private static final Color FK_DARK    = new Color(33,  33,  33);
    private static final Color FK_NAVY    = new Color(18,  40,  76);
    private static final Color GREEN_OK   = new Color(0,  140,  60);
    private static final Color RED_ERR    = new Color(180,  0,   0);
    private static final Color LIGHT_BG   = new Color(241, 243, 246);
    private static final Color WHITE      = Color.WHITE;

    // ── State ─────────────────────────────────────────────────────────────
    private LocalWebServer             server;
    private FlipkartAutomationEngine   engine;
    private volatile boolean           running = false;

    // ── Product config ─────────────────────────────────────────────────────
    private JCheckBox[] productCheckboxes;
    private final String[] PRODUCT_IDS = {
        "add-to-cart-1","add-to-cart-2","add-to-cart-3",
        "add-to-cart-4","add-to-cart-5","add-to-cart-6"
    };
    private final String[] PRODUCT_NAMES = {
        "Home Essentials Bundle (Rs.2,199)",
        "Bluetooth Speaker (Rs.3,499)",
        "Pet Care Grooming Kit (Rs.1,899)",
        "Computer Accessories Pack (Rs.4,299)",
        "Smart Fitness Watch Pro (Rs.5,999)",
        "4K Smart Android TV 55\" - DEAL (Rs.32,999)"
    };

    // ── Form fields ────────────────────────────────────────────────────────
    private JTextField fFirstName, fLastName, fAddress1, fAddress2;
    private JTextField fCity, fZip, fPhone;
    private JComboBox<String> fState;
    private JTextField fCardName, fCardNumber, fCardExpiry, fCardCvv;
    private JCheckBox  headlessCheck;
    private JTextField webappPathField;
    private JLabel     serverStatusLabel;

    // ── Log widgets ────────────────────────────────────────────────────────
    private JTextArea    logArea;
    private JProgressBar progressBar;
    private JLabel       statusLabel;
    private JButton      runBtn, stopBtn, clearBtn;

    // =====================================================================
    public MainApp() {
        super("Flipkart Clone - Full Selenium Automation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 800);
        setMinimumSize(new Dimension(920, 620));
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
        autoDetectWebapp();
    }

    // =====================================================================
    // UI BUILD
    // =====================================================================
    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildTitleBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildConfigPanel(), buildLogPanel());
        split.setDividerLocation(490);
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ---- Title bar -------------------------------------------------------
    private JPanel buildTitleBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(FK_NAVY);
        p.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("  Flipkart Clone  —  Full Selenium Automation");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(FK_YELLOW);

        JLabel sub = new JLabel("Select products  ->  Fill details  ->  Run automation");
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(new Color(180, 200, 240));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);
        p.add(left, BorderLayout.WEST);

        JLabel badge = new JLabel("Selenium 4  |  Java Swing  |  Flipkart Clone");
        badge.setFont(new Font("Arial", Font.BOLD, 11));
        badge.setForeground(FK_YELLOW);
        badge.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(FK_YELLOW, 1, true),
            new EmptyBorder(3, 12, 3, 12)));
        p.add(badge, BorderLayout.EAST);
        return p;
    }

    // ---- Config panel (scrollable left) ----------------------------------
    private JScrollPane buildConfigPanel() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(LIGHT_BG);
        c.setBorder(new EmptyBorder(10, 10, 10, 10));

        c.add(buildWebappSection());
        c.add(Box.createVerticalStrut(10));
        c.add(buildProductSection());
        c.add(Box.createVerticalStrut(10));
        c.add(buildAddressSection());
        c.add(Box.createVerticalStrut(10));
        c.add(buildPaymentSection());
        c.add(Box.createVerticalStrut(10));
        c.add(buildOptionsSection());
        c.add(Box.createVerticalStrut(10));
        c.add(buildRunButtons());
        c.add(Box.createVerticalGlue());

        JScrollPane sc = new JScrollPane(c);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(12);
        return sc;
    }

    private JPanel buildWebappSection() {
        JPanel p = section("Webapp Configuration");
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        webappPathField = field("Path to webapp folder");
        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> browseWebapp());

        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        row.add(new JLabel("Webapp Path: "), BorderLayout.WEST);
        row.add(webappPathField, BorderLayout.CENTER);
        row.add(browseBtn, BorderLayout.EAST);

        p.add(label("Folder path containing index.html:"));
        p.add(Box.createVerticalStrut(4));
        p.add(row);
        p.add(Box.createVerticalStrut(8));

        JPanel sRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sRow.setOpaque(false);
        JButton startBtn = smallBtn("Start Server", GREEN_OK);
        JButton stopBtn2 = smallBtn("Stop Server",  RED_ERR);
        serverStatusLabel = new JLabel("  Server: Not started");
        serverStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        serverStatusLabel.setForeground(Color.GRAY);
        startBtn.addActionListener(e -> startServer());
        stopBtn2.addActionListener(e -> stopServer());
        sRow.add(startBtn);
        sRow.add(stopBtn2);
        sRow.add(serverStatusLabel);
        p.add(sRow);
        return p;
    }

    private JPanel buildProductSection() {
        JPanel p = section("Select Products to Add to Cart");
        p.setLayout(new GridLayout(0, 2, 8, 5));
        productCheckboxes = new JCheckBox[PRODUCT_NAMES.length];
        for (int i = 0; i < PRODUCT_NAMES.length; i++) {
            productCheckboxes[i] = new JCheckBox(PRODUCT_NAMES[i]);
            productCheckboxes[i].setBackground(WHITE);
            productCheckboxes[i].setFont(new Font("Arial", Font.PLAIN, 12));
            if (i == 1) productCheckboxes[i].setSelected(true); // default: Bluetooth Speaker
            p.add(productCheckboxes[i]);
        }
        return p;
    }

    private JPanel buildAddressSection() {
        JPanel p = section("Delivery Address (India)");
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill   = GridBagConstraints.HORIZONTAL;

        fFirstName = field("Rahul"); fFirstName.setText("Rahul");
        fLastName  = field("Sharma"); fLastName.setText("Sharma");
        fAddress1  = field("42, MG Road"); fAddress1.setText("42, MG Road");
        fAddress2  = field("Near Bus Stand (optional)");
        fCity      = field("Mumbai"); fCity.setText("Mumbai");
        fZip       = field("400001"); fZip.setText("400001");
        fPhone     = field("9876543210"); fPhone.setText("9876543210");

        String[] states = {
            "","AP","AR","AS","BR","CT","DL","GA","GJ","HR","HP",
            "JH","KA","KL","MP","MH","MN","ML","MZ","NL","OD",
            "PB","RJ","SK","TN","TG","TR","UP","UK","WB"
        };
        fState = new JComboBox<>(states);
        fState.setSelectedItem("MH");
        fState.setFont(new Font("Arial", Font.PLAIN, 13));

        int row = 0;
        addRow(p, c, row++, "First Name *", fFirstName, "Last Name *", fLastName);
        addRow(p, c, row++, "Address Line 1 *", fAddress1, "Address Line 2", fAddress2);

        c.gridwidth=1; c.gridx=0; c.gridy=row; p.add(lbl("City *"), c);
        c.gridx=1; p.add(fCity, c);
        c.gridx=2; p.add(lbl("State *"), c);
        c.gridx=3; p.add(fState, c);
        row++;
        c.gridx=0; c.gridy=row; p.add(lbl("Pincode *"), c);
        c.gridx=1; p.add(fZip, c);
        c.gridx=2; p.add(lbl("Mobile *"), c);
        c.gridx=3; p.add(fPhone, c);
        return p;
    }

    private JPanel buildPaymentSection() {
        JPanel p = section("Payment Details");
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill   = GridBagConstraints.HORIZONTAL;

        fCardName   = field("Rahul Sharma"); fCardName.setText("Rahul Sharma");
        fCardNumber = field("4111 1111 1111 1111"); fCardNumber.setText("4111 1111 1111 1111");
        fCardExpiry = field("12/26"); fCardExpiry.setText("12/26");
        fCardCvv    = field("123"); fCardCvv.setText("123");

        int row = 0;
        c.gridwidth=1; c.gridx=0; c.gridy=row; p.add(lbl("Name on Card *"), c);
        c.gridx=1; c.gridwidth=3; p.add(fCardName, c);
        row++;
        c.gridwidth=1; c.gridx=0; c.gridy=row; p.add(lbl("Card Number *"), c);
        c.gridx=1; c.gridwidth=3; p.add(fCardNumber, c);
        row++;
        c.gridwidth=1; c.gridx=0; c.gridy=row; p.add(lbl("Expiry (MM/YY) *"), c);
        c.gridx=1; p.add(fCardExpiry, c);
        c.gridx=2; p.add(lbl("CVV *"), c);
        c.gridx=3; p.add(fCardCvv, c);
        return p;
    }

    private JPanel buildOptionsSection() {
        JPanel p = section("Options");
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        headlessCheck = new JCheckBox("Run headless (no browser window)");
        headlessCheck.setBackground(WHITE);
        headlessCheck.setFont(new Font("Arial", Font.PLAIN, 13));
        p.add(headlessCheck);
        return p;
    }

    private JPanel buildRunButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        p.setBackground(LIGHT_BG);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));

        runBtn   = bigBtn("RUN AUTOMATION", FK_BLUE);
        stopBtn  = bigBtn("STOP",           RED_ERR);
        clearBtn = bigBtn("CLEAR LOG",      FK_DARK);
        stopBtn.setEnabled(false);

        runBtn.addActionListener(e   -> runAutomation());
        stopBtn.addActionListener(e  -> stopAutomation());
        clearBtn.addActionListener(e -> logArea.setText(""));

        p.add(runBtn); p.add(stopBtn); p.add(clearBtn);
        return p;
    }

    // ---- Log panel (right) -----------------------------------------------
    private JPanel buildLogPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(FK_DARK);
        p.setBorder(new EmptyBorder(10, 8, 8, 10));

        JLabel title = new JLabel("  Automation Log");
        title.setForeground(FK_YELLOW);
        title.setFont(new Font("Consolas", Font.BOLD, 14));
        p.add(title, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(18, 18, 18));
        logArea.setForeground(new Color(80, 220, 120));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setMargin(new Insets(8, 10, 8, 10));

        DefaultCaret caret = new DefaultCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        logArea.setCaret(caret);

        logArea.setText(
            "  Flipkart Clone Automation Ready.\n" +
            "  ===========================================\n" +
            "  Steps to run:\n" +
            "   1. Set the webapp folder path\n" +
            "   2. Click  Start Server\n" +
            "   3. Tick products you want to purchase\n" +
            "   4. Fill in delivery address (India)\n" +
            "   5. Fill in card payment details\n" +
            "   6. Click  RUN AUTOMATION\n" +
            "  ===========================================\n"
        );

        JScrollPane sc = new JScrollPane(logArea);
        sc.setBorder(new LineBorder(new Color(50, 50, 50), 1));
        p.add(sc, BorderLayout.CENTER);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Idle");
        progressBar.setForeground(FK_BLUE);
        progressBar.setBackground(new Color(40, 40, 40));
        progressBar.setFont(new Font("Arial", Font.BOLD, 11));
        p.add(progressBar, BorderLayout.SOUTH);
        return p;
    }

    // ---- Status bar ------------------------------------------------------
    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(FK_NAVY);
        p.setBorder(new EmptyBorder(4, 15, 4, 15));
        statusLabel = new JLabel("Ready. Configure settings and click RUN.");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JLabel credit = new JLabel("Flipkart Clone Automation v1.0  |  Selenium 4  |  Java Swing");
        credit.setForeground(new Color(100, 120, 160));
        credit.setFont(new Font("Arial", Font.PLAIN, 11));
        p.add(statusLabel, BorderLayout.WEST);
        p.add(credit, BorderLayout.EAST);
        return p;
    }

    // =====================================================================
    // BUSINESS LOGIC
    // =====================================================================
    private void autoDetectWebapp() {
        String[] candidates = {
            "webapp",
            "../webapp",
            "FlipkartAutomation/webapp",
            System.getProperty("user.dir") + File.separator + "webapp"
        };
        for (String path : candidates) {
            File f = new File(path);
            if (f.exists() && new File(f, "index.html").exists()) {
                webappPathField.setText(f.getAbsolutePath());
                log("Auto-detected webapp: " + f.getAbsolutePath());
                return;
            }
        }
    }

    private void browseWebapp() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Select webapp folder (containing index.html)");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            webappPathField.setText(fc.getSelectedFile().getAbsolutePath());
    }

    private void startServer() {
        String path = webappPathField.getText().trim();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please set the webapp path first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!new File(path, "index.html").exists()) {
            JOptionPane.showMessageDialog(this, "index.html not found in:\n" + path, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        stopServer();
        int port = freePort(8766);
        server = new LocalWebServer(path, port);
        try {
            server.start();
            serverStatusLabel.setText("  Server: http://localhost:" + port);
            serverStatusLabel.setForeground(GREEN_OK);
            log("HTTP server started -> " + server.getBaseUrl());
            log("Serving: " + path);
        } catch (IOException ex) {
            serverStatusLabel.setText("  Server: FAILED");
            serverStatusLabel.setForeground(RED_ERR);
            log("ERROR: " + ex.getMessage());
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop(); server = null;
            serverStatusLabel.setText("  Server: Stopped");
            serverStatusLabel.setForeground(Color.GRAY);
        }
    }

    private void runAutomation() {
        if (server == null) {
            int opt = JOptionPane.showConfirmDialog(this,
                "HTTP Server is not running. Start it now?",
                "Server not running", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) startServer();
            if (server == null) return;
        }

        java.util.List<String> ids = new java.util.ArrayList<>();
        for (int i = 0; i < productCheckboxes.length; i++)
            if (productCheckboxes[i].isSelected()) ids.add(PRODUCT_IDS[i]);
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select at least one product.", "No products", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) return;   // ✅ FIXED: was validate()

        OrderDetails order = new OrderDetails();
        order.setFirstName(fFirstName.getText().trim());
        order.setLastName(fLastName.getText().trim());
        order.setAddress1(fAddress1.getText().trim());
        order.setAddress2(fAddress2.getText().trim());
        order.setCity(fCity.getText().trim());
        order.setState((String) fState.getSelectedItem());
        order.setZip(fZip.getText().trim());
        order.setPhone(fPhone.getText().trim());
        order.setCardName(fCardName.getText().trim());
        order.setCardNumber(fCardNumber.getText().trim());
        order.setCardExpiry(fCardExpiry.getText().trim());
        order.setCardCvv(fCardCvv.getText().trim());
        order.setDeliveryOption("standard");

        boolean headless = headlessCheck.isSelected();
        running = true;
        runBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Running...");
        logArea.setText("");
        log("==========================================");
        log("  FLIPKART CLONE AUTOMATION STARTED");
        log("  " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        log("==========================================");

        final String[] pids = ids.toArray(new String[0]);
        new Thread(() -> {
            engine = new FlipkartAutomationEngine(server.getBaseUrl());
            engine.setLogger(msg      -> SwingUtilities.invokeLater(() -> appendLog(msg)));
            engine.setStatusUpdater(msg -> SwingUtilities.invokeLater(() -> {
                statusLabel.setText(msg);
                progressBar.setString(msg);
            }));
            try {
                String orderId = engine.runFullAutomation(order, pids, headless);
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    progressBar.setString("Completed!");
                    JOptionPane.showMessageDialog(MainApp.this,
                        "Order Placed Successfully!\n\nOrder ID: " + orderId +
                        "\n\nCheck the browser for the confirmation page.",
                        "Flipkart Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    progressBar.setString("Failed");
                    appendLog("ERROR: " + ex.getMessage());
                    statusLabel.setText("Error: " + ex.getMessage());
                    if (ex.getMessage() != null && !ex.getMessage().contains("cancelled"))
                        JOptionPane.showMessageDialog(MainApp.this,
                            "Automation failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    running = false;
                    runBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                });
            }
        }, "fk-automation-thread").start();
    }

    private void stopAutomation() {
        if (engine != null) engine.cancel();
        log("Stopping automation...");
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString("Stopped");
        statusLabel.setText("Stopped by user.");
        runBtn.setEnabled(true);
        stopBtn.setEnabled(false);
    }

    // ✅ FIXED: renamed from validate() to validateForm()
    private boolean validateForm() {
        java.util.List<String> missing = new java.util.ArrayList<>();
        if (fFirstName.getText().isBlank())  missing.add("First Name");
        if (fLastName.getText().isBlank())   missing.add("Last Name");
        if (fAddress1.getText().isBlank())   missing.add("Address");
        if (fCity.getText().isBlank())       missing.add("City");
        if (fState.getSelectedIndex() == 0)  missing.add("State");
        if (fZip.getText().isBlank())        missing.add("Pincode");
        if (fPhone.getText().isBlank())      missing.add("Mobile");
        if (fCardName.getText().isBlank())   missing.add("Card Name");
        if (fCardNumber.getText().isBlank()) missing.add("Card Number");
        if (fCardExpiry.getText().isBlank()) missing.add("Card Expiry");
        if (fCardCvv.getText().isBlank())    missing.add("CVV");
        if (!missing.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in:\n- " + String.join("\n- ", missing),
                "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void appendLog(String msg) {
        String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logArea.append("[" + ts + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    private void log(String msg) { appendLog(msg); }

    // =====================================================================
    // UI HELPERS
    // =====================================================================
    private JPanel section(String title) {
        JPanel p = new JPanel();
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(FK_BLUE, 1, true), "  " + title + "  ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12), FK_DARK),
            new EmptyBorder(8, 10, 10, 10)));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        return p;
    }

    private JTextField field(String hint) {
        JTextField f = new JTextField(15);
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(180,180,180), 1),
            new EmptyBorder(4, 6, 4, 6)));
        return f;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(new Color(50,50,70));
        return l;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void addRow(JPanel p, GridBagConstraints c, int row,
                        String l1, JComponent f1, String l2, JComponent f2) {
        c.gridwidth=1;
        c.gridx=0; c.gridy=row; p.add(lbl(l1), c);
        c.gridx=1; p.add(f1, c);
        c.gridx=2; p.add(lbl(l2), c);
        c.gridx=3; p.add(f2, c);
    }

    private JButton bigBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(175, 38));
        return b;
    }

    private JButton smallBtn(String text, Color fg) {
        JButton b = new JButton(text);
        b.setForeground(fg);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static int freePort(int preferred) {
        try (ServerSocket s = new ServerSocket(preferred)) { return preferred; }
        catch (IOException e) {
            try (ServerSocket s = new ServerSocket(0)) { return s.getLocalPort(); }
            catch (IOException ex) { return preferred; }
        }
    }

    // =====================================================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(MainApp::new);
    }
}