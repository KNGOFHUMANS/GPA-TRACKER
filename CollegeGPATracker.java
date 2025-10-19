import javax.swing.*; // entire swing library
import javax.swing.border.EmptyBorder; // only border
import javax.swing.table.DefaultTableModel; // table model
import javax.swing.plaf.basic.BasicProgressBarUI; // progress bar UI
import javax.swing.JComponent; // only JComponent
import javax.swing.Box; // only Box

import java.awt.*; // entire awt library
import java.awt.event.*; // entire awt event library
import java.awt.geom.Arc2D; // only Arc2D
import java.awt.geom.Ellipse2D; // only Ellipse2D
import java.util.List; // only List
import java.util.*; // entire util library

import java.io.File; // only File
import java.io.FileReader; // only FileReader
import java.io.FileWriter; // only FileWriter
import java.io.IOException; // only IOException

import com.google.gson.Gson; // only Gson
import com.google.gson.reflect.TypeToken; // only TypeToken

public class CollegeGPATracker { // main application class
    // username -> [password, email]
    private static Map<String, String[]> users = new HashMap<>(); // username -> [password, email]
    private static Map<String, Long> lastUsernameChange = new HashMap<>(); // username -> timestamp
    private static String currentUser; // currently logged-in user

    // user -> semester(int) -> className -> ClassData
    private static Map<String, Map<Integer, Map<String, ClassData>>> userData = new HashMap<>(); // user -> semester(int) -> className -> ClassData
    private static boolean darkMode = false; // dark mode flag

    private static final String DATA_DIR = "data"; // data directory
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.json"; // users file
    private static final String USERDATA_FILE = DATA_DIR + File.separator + "user_data.json"; // user data file
    private static final String USERNAME_CHANGES_FILE = DATA_DIR + File.separator + "username_changes.json";// username changes file
    private static final String RESET_CODES_FILE = DATA_DIR + File.separator + "reset_tokens.json"; // reset tokens file
    private static final Gson gson = new Gson(); // Gson instance

    // Design tokens (hex values)
    private static final Color LEFT_TOP = new Color(0x14B8A6);      // teal
    private static final Color LEFT_BOTTOM = new Color(0x6440FF);   // purple
    private static final Color RIGHT_BG = new Color(0xF6F7F9);      // soft off-white
    private static final Color CARD_BG = new Color(0xFAFAFC);       // card background
    // Dark theme tokens (login full-screen)
    private static final Color RIGHT_BG_DARK = new Color(0x121418); // deep charcoal
    private static final Color CARD_BG_DARK = new Color(0x23272B);  // card dark surface
    private static final Color INPUT_BG_DARK = new Color(0x1E2225); // input field dark
    private static final Color INPUT_BORDER_DARK = new Color(0x2E3438);
    private static final Color PRIMARY_DARK = new Color(0x2F80ED);

    private static JLabel overallGpaLabel;

    // ===== CLASS DATA =====
    static class ClassData {
        Map<String, List<Assignment>> assignments = new HashMap<>();
        Map<String, Integer> weights = new HashMap<>();
        List<Double> historyPercent = new ArrayList<>();
        int credits = 3; // default credit hours per class
// default categories and weights
        public ClassData() {
            assignments.put("Homework", new ArrayList<>());
            assignments.put("Exam", new ArrayList<>());
            assignments.put("Project", new ArrayList<>());
            weights.put("Homework", 40);
            weights.put("Exam", 40);
            weights.put("Project", 20);
        }
    }
// ===== ASSIGNMENT DATA =====
    static class Assignment {
        String name; double score; String category;
        Assignment(String name, double score, String category) {
            this.name = name; this.score = score; this.category = category;
        }
    }
// ===== MAIN METHOD =====
    public static void main(String[] args) {
        ensureDataDir();
        loadUsers();
        loadAllUserData();
        PasswordResetStore.init(RESET_CODES_FILE);
        SwingUtilities.invokeLater(CollegeGPATracker::showLoginUI);
    }

    // ===== LOGIN PAGE =====
    private static void showLoginUI() {
        // Normal login UI — no startup mail configuration prompt
    JFrame frame = new JFrame("Login - GPA Tracker");
    frame.setSize(1000, 560);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

    // prettier left panel with teal purple gradient and larger title
    JPanel leftPanel = new GradientPanel(LEFT_TOP, LEFT_BOTTOM);
        leftPanel.setPreferredSize(new Dimension(420, 0));
        JLabel msg = new JLabel("<html><center>🎓<br><span style='font-size:20pt'>Your College<br>GPA Tracker</span></center></html>", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.BOLD, 28));
        msg.setForeground(Color.WHITE);
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(msg, BorderLayout.CENTER);
// add some padding
        leftPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
    JPanel rightPanel = new JPanel(new GridBagLayout());
    rightPanel.setBackground(RIGHT_BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
    // login form elements
    JLabel loginLabel = new JLabel("Login");
    loginLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
    loginLabel.setForeground(Color.WHITE);
    rightPanel.add(loginLabel, gbc);
    gbc.gridy++;
    JTextField usernameField = new JTextField(20);
    usernameField.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    rightPanel.add(usernameField, gbc);
    gbc.gridy++;
    JPasswordField passwordField = new JPasswordField(20);
    passwordField.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    rightPanel.add(passwordField, gbc);
    gbc.gridy++;
    JButton loginButton = new JButton("Login");
    loginButton.setBackground(new Color(0, 150, 136));
    loginButton.setForeground(Color.WHITE);
    rightPanel.add(loginButton, gbc);
    gbc.gridy++;
    JButton registerButton = new JButton("Register");
    registerButton.setBackground(new Color(0, 150, 136));
    registerButton.setForeground(Color.WHITE);
    rightPanel.add(registerButton, gbc);
    // Card-style login box (stacked, centered)
    JPanel loginBox = new JPanel();
    loginBox.setLayout(new BoxLayout(loginBox, BoxLayout.Y_AXIS));
    loginBox.setBackground(CARD_BG_DARK);
    loginBox.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(INPUT_BORDER_DARK, 1, true),
        new EmptyBorder(28, 28, 28, 28)
    ));
    loginBox.setMaximumSize(new Dimension(420, 420));

    // Inputs
    JTextField usernameOrEmail = new PlaceholderTextField("Username or email");
    JPasswordField password = new PlaceholderPasswordField("Password");
    // center text and use a slightly darker foreground for better contrast
    usernameOrEmail.setHorizontalAlignment(SwingConstants.CENTER);
    usernameOrEmail.setForeground(new Color(40,40,40));
    password.setHorizontalAlignment(SwingConstants.CENTER);
    password.setForeground(new Color(40,40,40));
    // make the text fields a consistent size so rows align
    Dimension inputPref = new Dimension(360, 48);
    usernameOrEmail.setPreferredSize(inputPref);
    password.setPreferredSize(inputPref);

    // helper to create an input row with left icon and rounded background
    java.util.function.BiFunction<Icon, JComponent, JComponent> makeRow = (ic, comp) -> {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // fill rounded background
                g2.setColor(getBackground());
                int arc = 14;
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                // draw border
                g2.setColor(INPUT_BORDER_DARK);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false); // we paint the background ourselves
        row.setBackground(INPUT_BG_DARK);
        // icon area sized to keep icon centered vertically and give padding
    Icon smallIc = sizedIcon(ic, 18);
    JLabel iconLabel = new JLabel(smallIc);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(42, 48));
        iconLabel.setBorder(new EmptyBorder(0, 8, 0, 8));
        row.add(iconLabel, BorderLayout.WEST);
        // make the input component transparent so the rounded bg shows through
        comp.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        if (comp instanceof JComponent) {
            ((JComponent) comp).setOpaque(false);
        }
        row.add(comp, BorderLayout.CENTER);
        row.setBorder(new EmptyBorder(2,2,2,2));
        row.setMaximumSize(new Dimension(360, 48));
        return row;
    };

    Icon userIc = UIManager.getIcon("FileView.fileIcon");
    Icon lockIc = UIManager.getIcon("FileView.hardDriveIcon");
    // fallback simple icons (person/lock) with slightly larger size to fit the rounded row
    if (userIc == null) userIc = new PersonIcon(18,18);
    if (lockIc == null) lockIc = new LockIcon(18,18);

    JComponent userRow = makeRow.apply(userIc, usernameOrEmail);
    JComponent passRow = makeRow.apply(lockIc, password);
    // enforce consistent sizes and alignment for rows
    userRow.setPreferredSize(new Dimension(360, 48));
    passRow.setPreferredSize(new Dimension(360, 48));
    userRow.setAlignmentX(Component.CENTER_ALIGNMENT);
    passRow.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Buttons: primary blue login + secondary actions (dark styles)
    JButton loginBtn = pillButton("Login");
    loginBtn.setBackground(PRIMARY_DARK);
    loginBtn.setForeground(Color.WHITE);
    loginBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
    loginBtn.setMaximumSize(new Dimension(360, 46));
    loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton signupBtn = pillButton("Create account");
    signupBtn.setMaximumSize(new Dimension(360, 42));
    signupBtn.setBackground(new Color(0x2A2F33));
    signupBtn.setForeground(Color.WHITE);
    signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton forgotBtn = pillButton("Forgot password");
    forgotBtn.setMaximumSize(new Dimension(360, 42));
    // dark red tone
    forgotBtn.setBackground(new Color(0x7A1414));
    forgotBtn.setForeground(Color.WHITE);
    forgotBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

    // removed Enter reset code button to match desired flow (email contains code and user uses dialog only)

    JButton googleBtn = pillButton("Sign in with Google", new GoogleIcon(18, 18));
    // Google button dark style: dark surface with subtle inner shadow
    googleBtn.setBackground(new Color(0x2A2F33));
    googleBtn.setForeground(Color.WHITE);
    googleBtn.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(INPUT_BORDER_DARK),
        googleBtn.getBorder()));
    googleBtn.setMaximumSize(new Dimension(360, 42));
    googleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

    // assemble with spacing to match the provided design
    loginBox.add(Box.createVerticalGlue());
    JLabel title = new JLabel("", SwingConstants.CENTER);
    title.setPreferredSize(new Dimension(0,8));
    loginBox.add(title);
    userRow.setBackground(new Color(250,250,250));
    passRow.setBackground(new Color(250,250,250));
    // align rows
    userRow.setAlignmentX(Component.CENTER_ALIGNMENT);
    passRow.setAlignmentX(Component.CENTER_ALIGNMENT);
    loginBox.add(userRow);
    loginBox.add(Box.createVerticalStrut(12));
    loginBox.add(passRow);
    loginBox.add(Box.createVerticalStrut(18));
    loginBox.add(loginBtn);
    loginBox.add(Box.createVerticalStrut(12));
    loginBox.add(signupBtn);
    loginBox.add(Box.createVerticalStrut(8));
    loginBox.add(forgotBtn);
    JLabel mailHelp = new JLabel("Need help with email?", SwingConstants.CENTER);
    mailHelp.setFont(mailHelp.getFont().deriveFont(Font.PLAIN, 11f));
    mailHelp.setForeground(new Color(180,180,180));
    mailHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    mailHelp.setToolTipText("Use a Gmail App Password (create one in your Google Account under Security → App passwords).\nYou can enter it once in the prompt; optionally saved locally (plaintext).");
    mailHelp.setAlignmentX(Component.CENTER_ALIGNMENT);
    loginBox.add(mailHelp);
    loginBox.add(Box.createVerticalStrut(20));
    loginBox.add(googleBtn);
    loginBox.add(Box.createVerticalGlue());

    // wrap visually with a rounded card
    RoundedCard card = new RoundedCard(12, CARD_BG, new Color(200,200,210,110));
    card.setLayout(new GridBagLayout());
    card.add(loginBox);
    JPanel wrapper = new JPanel(new GridBagLayout());
    wrapper.setBackground(RIGHT_BG);
    wrapper.add(card);
    // Ensure the right panel contains only the centered login card.
    // Some earlier code added multiple components directly to rightPanel which
    // caused layout collisions. Clear them and reconfigure the layout so the
    // card is centered and sized consistently.
    rightPanel.removeAll();
    rightPanel.setLayout(new GridBagLayout());
    rightPanel.setBackground(RIGHT_BG_DARK);
    rightPanel.add(wrapper);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);

        // LOGIN
        loginBtn.addActionListener(_ -> {
            String id = usernameOrEmail.getText().trim();
            String pass = new String(password.getPassword());
            String user = id;

            if (!users.containsKey(user)) user = findUserByEmail(id);

            if (user != null && users.containsKey(user) && Objects.equals(users.get(user)[0], pass)) {
                currentUser = user;
                ensureUserStructures(currentUser);
                frame.dispose();
                showDashboard();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials.");
            }
        });

        // SIGNUP
        signupBtn.addActionListener(_ -> {
            String newUser = JOptionPane.showInputDialog(frame, "Choose a username:");
            if (newUser == null || newUser.trim().isEmpty()) return;
            if (users.containsKey(newUser)) {
                JOptionPane.showMessageDialog(frame, "Username already exists!");
                return;
            }
            String email = JOptionPane.showInputDialog(frame, "Enter email:");
            if (email == null || email.trim().isEmpty()) return;
            if (findUserByEmail(email) != null) {
                JOptionPane.showMessageDialog(frame, "Email already used.");
                return;
            }
            JPasswordField p1 = new JPasswordField();
            JPasswordField p2 = new JPasswordField();
            int ok = JOptionPane.showConfirmDialog(frame, new Object[]{"Password:", p1,"Confirm Password:", p2}, "Create password", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
            String newPass = new String(p1.getPassword());
            String confirmPass = new String(p2.getPassword());
            if (newPass.isEmpty() || !newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(frame, "Passwords don't match.");
                return;
            }

            users.put(newUser, new String[]{newPass, email});
            lastUsernameChange.put(newUser, System.currentTimeMillis());
            ensureUserStructures(newUser);
            saveUsers();
            saveAllUserData();

            JOptionPane.showMessageDialog(frame, "Account created! Please log in.");
        });

        // FORGOT PASSWORD: prompt for email, generate a transient token, attempt to send it by email.
        // Only persist the token after the email has successfully been sent. Never show the code in the UI.
        forgotBtn.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                String email = JOptionPane.showInputDialog(frame, "Enter your account email:");
                if (email == null || email.trim().isEmpty()) return;
                String user = findUserByEmail(email);
                if (user == null) {
                    JOptionPane.showMessageDialog(frame, "No account with that email.");
                    return;
                }

                // generate a token but don't persist it until we confirm email delivery
                String code = PasswordResetStore.generateTokenFor(user);
                String subject = "GPA Tracker — Password reset code";
                String body = "Your password reset code: " + code + "\n\n" +
                        "Enter this code in the app to set a new password.\nIf you did not request this, ignore this message.";

                int maxAttempts = 3;
                boolean sent = false;
                for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                    try {
                        MailSender.sendEmail(email, subject, body);
                        sent = true;
                        break;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        int opt = JOptionPane.showConfirmDialog(frame, "Failed to send reset email (attempt " + attempt + "). Retry?", "Send failed", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (opt != JOptionPane.YES_OPTION) break;
                    }
                }

                if (sent) {
                    // persist the token so it can be used to reset the password
                    PasswordResetStore.persistToken(code, user);
                    JOptionPane.showMessageDialog(frame, "A reset code was sent to " + email + ".\nCheck your inbox (and spam).\n\nYou will now be prompted to enter the code.");

                    // Immediately prompt user to enter the code they received by email
                    String provided = JOptionPane.showInputDialog(frame, "Enter the 6-digit reset code from your email:");
                    if (provided == null || provided.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "No code entered. You can enter the code later using 'Enter reset code'.");
                        return;
                    }
                    String matched = PasswordResetStore.consume(provided.trim());
                    if (matched == null || !matched.equals(user)) {
                        JOptionPane.showMessageDialog(frame, "Invalid or expired reset code. Please check your email and try again.");
                        return;
                    }
                    // prompt for new password
                    JPasswordField p1 = new JPasswordField();
                    JPasswordField p2 = new JPasswordField();
                    int ok = JOptionPane.showConfirmDialog(frame, new Object[]{"New password:", p1, "Confirm:", p2}, "Set new password", JOptionPane.OK_CANCEL_OPTION);
                    if (ok != JOptionPane.OK_OPTION) return;
                    String np = new String(p1.getPassword());
                    String np2 = new String(p2.getPassword());
                    if (np.isEmpty() || !np.equals(np2)) {
                        JOptionPane.showMessageDialog(frame, "Passwords do not match or are empty.");
                        return;
                    }
                    users.get(user)[0] = np;
                    saveUsers();
                    JOptionPane.showMessageDialog(frame, "Password updated — you can now log in.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to send reset email. Please check the application's SMTP settings and try again later. If the problem persists, contact support.");
                }
            }
        });

        // Enter-reset-code UI removed — flow is: Forgot password -> email sent with code -> user uses that code in the app (we'll provide the dialog when they click Forgot again or we can add a small entry flow later)

        // GOOGLE SIGN-IN (OAuth; requires GoogleSignIn.java and client_secret.json)
        googleBtn.addActionListener(_ -> {
           try {
               String[] result = GoogleSignIn.authenticate(); // [email, suggestedUsername]
               String email = result[0];
             String suggested = result[1];

               String existing = findUserByEmail(email);
                boolean isNew = (existing == null);
             String useUsername = isNew ? suggested : existing;

            users.putIfAbsent(useUsername, new String[]{"", email}); // empty pass = Google login
               if (isNew) lastUsernameChange.put(useUsername, System.currentTimeMillis());
               currentUser = useUsername;

               ensureUserStructures(currentUser);
               saveUsers();
               saveAllUserData();

                frame.dispose();
                showDashboard();
           } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Google Sign-In failed: " + e.getMessage());
                 e.printStackTrace();
             }
        });

        // 'Enter reset code' removed — reset flow happens immediately after using 'Forgot password'

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ===== DASHBOARD =====
    private static void showDashboard() {
        JFrame frame = new JFrame("College GPA Dashboard — " + currentUser);
        frame.setSize(1150, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("User");
        JMenuItem profile = new JMenuItem("Profile");
        JMenuItem signOut = new JMenuItem("Sign Out");
    JMenuItem signOutGoogle = new JMenuItem("Sign Out (Google)");
        userMenu.add(profile);
        userMenu.add(signOut);
    userMenu.add(signOutGoogle);

        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleDark = new JMenuItem("Toggle Dark Mode");
        viewMenu.add(toggleDark);

        menuBar.add(userMenu);
        menuBar.add(viewMenu);
        frame.setJMenuBar(menuBar);

        // Top title
        overallGpaLabel = new JLabel("Overall GPA: " + String.format("%.2f", calculateOverallGPA(currentUser)), SwingConstants.CENTER);
        overallGpaLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        overallGpaLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        frame.add(overallGpaLabel, BorderLayout.NORTH);

        // Center: semesters
        JTabbedPane semesters = new JTabbedPane();
        for (int i = 1; i <= 4; i++) {
            semesters.add("Semester " + i, createSemesterPanel(i));
        }
        frame.add(semesters, BorderLayout.CENTER);

        // handlers
        signOut.addActionListener(_ -> {
            currentUser = null;
            frame.dispose();
            showLoginUI();
        });

        signOutGoogle.addActionListener(_ -> {
            try {
                GoogleSignIn.clearStoredCredentials();
            } catch (Exception ignored) {}
            currentUser = null;
            frame.dispose();
            showLoginUI();
        });

        profile.addActionListener(_ -> showUserPanel(frame));

        toggleDark.addActionListener(_ -> {
            darkMode = !darkMode;
            applyTheme(frame.getContentPane());
            SwingUtilities.updateComponentTreeUI(frame);
        });

        applyTheme(frame.getContentPane());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ===== SEMESTER PANEL =====
    private static JPanel createSemesterPanel(int semesterNum) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(8,8,8,8));

        // Left: class list with progress bars
        DefaultListModel<String> classListModel = new DefaultListModel<>();
        JList<String> classList = new JList<>(classListModel);
        classList.setCellRenderer(new ClassRenderer(semesterNum));
        classList.setFixedCellHeight(64);

        JScrollPane classScroll = new JScrollPane(classList);
        classScroll.setPreferredSize(new Dimension(260, 0));

        JButton addClassBtn = pillButton("+ Add Class");
        JButton deleteClassBtn = pillButton("Delete Class");
        JPanel leftTop = new JPanel(new GridLayout(1, 2, 10, 10));
        leftTop.add(addClassBtn);
        leftTop.add(deleteClassBtn);

        JPanel left = new JPanel(new BorderLayout(8,8));
        left.add(leftTop, BorderLayout.NORTH);
        left.add(classScroll, BorderLayout.CENTER);

        // Center: table + pie + trend + badges
        String[] cols = {"Assignment", "Category", "Score (%)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0){
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel classTitle = new JLabel("Select a class", SwingConstants.CENTER);
        classTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        classTitle.setBorder(BorderFactory.createTitledBorder(""));

        JButton addAssignmentBtn = pillButton("+ Add Assignment");
        JButton deleteAssignmentBtn = pillButton("Delete Assignment");
        JButton weightsBtn = pillButton("\uD83D\uDD11 Weights");
        JButton creditsBtn = pillButton("\uD83D\uDCB0 Credits");

        JLabel classGpaLabel = new JLabel("Class GPA: —", SwingConstants.LEFT);
        classGpaLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel centerTop = new JPanel(new BorderLayout());
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        rightControls.add(creditsBtn);
        rightControls.add(weightsBtn);
        centerTop.add(classTitle, BorderLayout.CENTER);
        centerTop.add(rightControls, BorderLayout.EAST);

        JPanel centerBottom = new JPanel(new BorderLayout());
        JPanel bottomLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomLeft.add(classGpaLabel);
        JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRight.add(deleteAssignmentBtn);
        bottomRight.add(addAssignmentBtn);
        centerBottom.add(bottomLeft, BorderLayout.WEST);
        centerBottom.add(bottomRight, BorderLayout.EAST);

        JScrollPane tableScroll = new JScrollPane(table);

        // Right dashboard column
        PiePanel piePanel = new PiePanel();
        TrendPanel trendPanel = new TrendPanel();
        BadgePanel badgePanel = new BadgePanel();

        JPanel rightDash = new JPanel();
        rightDash.setLayout(new BoxLayout(rightDash, BoxLayout.Y_AXIS));
        JPanel weightsBox = new JPanel(new BorderLayout());
        weightsBox.setBorder(BorderFactory.createTitledBorder("Breakdown"));
        weightsBox.add(piePanel, BorderLayout.CENTER);

        JPanel trendBox = new JPanel(new BorderLayout());
        trendBox.setBorder(BorderFactory.createTitledBorder("Trend"));
        trendBox.add(trendPanel, BorderLayout.CENTER);

        JPanel badgesBox = new JPanel(new BorderLayout());
        badgesBox.setBorder(BorderFactory.createTitledBorder("Badges"));
        badgesBox.add(badgePanel, BorderLayout.CENTER);

        weightsBox.setMaximumSize(new Dimension(340, 240));
        trendBox.setMaximumSize(new Dimension(340, 180));
        badgesBox.setMaximumSize(new Dimension(340, 120));

        rightDash.add(weightsBox);
        rightDash.add(Box.createVerticalStrut(8));
        rightDash.add(trendBox);
        rightDash.add(Box.createVerticalStrut(8));
        rightDash.add(badgesBox);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                tableScroll, rightDash);
        centerSplit.setResizeWeight(0.72);
        centerSplit.setDividerSize(6);

        JPanel center = new JPanel(new BorderLayout(8,8));
        center.add(centerTop, BorderLayout.NORTH);
        center.add(centerSplit, BorderLayout.CENTER);
        center.add(centerBottom, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, center);
        split.setResizeWeight(0.26);
        split.setDividerSize(6);

        root.add(split, BorderLayout.CENTER);

        // ensure storage
        ensureUserStructures(currentUser);
        userData.get(currentUser).putIfAbsent(semesterNum, new HashMap<>());
        // fill list
        for (String cls : userData.get(currentUser).get(semesterNum).keySet()) {
            classListModel.addElement(cls);
        }

        // interactions
        addClassBtn.addActionListener(_ -> {
            String className = JOptionPane.showInputDialog(root, "Enter class name:");
            if (className == null || className.trim().isEmpty()) return;
            if (userData.get(currentUser).get(semesterNum).containsKey(className)) {
                JOptionPane.showMessageDialog(root, "Class already exists.");
                return;
            }
            int credits = 3;
            String creditsStr = JOptionPane.showInputDialog(root, "Credit hours (e.g., 3):", "3");
            try {
                if (creditsStr != null && !creditsStr.trim().isEmpty()) {
                    credits = Math.max(0, Integer.parseInt(creditsStr.trim()));
                }
            } catch (Exception ignored) {}
            ClassData cd = new ClassData();
            cd.credits = credits;
            userData.get(currentUser).get(semesterNum).put(className, cd);
            classListModel.addElement(className);
            saveAllUserData();
            updateOverallGpaLabel();
            classList.repaint();
        });

        deleteClassBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            userData.get(currentUser).get(semesterNum).remove(selectedClass);
            classListModel.removeElement(selectedClass);
            model.setRowCount(0);
            classTitle.setText("Select a class");
            classGpaLabel.setText("Class GPA: —");
            piePanel.setData(0,0,0);
            trendPanel.setData(new ArrayList<>());
            badgePanel.setBadges(false, false);
            saveAllUserData();
            updateOverallGpaLabel();
            classList.repaint();
        });

        classList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            classTitle.setText(selectedClass + " — Assignments");

            model.setRowCount(0);
            ClassData cd = userData.get(currentUser).get(semesterNum).get(selectedClass);
            for (String cat : cd.assignments.keySet()) {
                for (Assignment a : cd.assignments.get(cat)) {
                    model.addRow(new Object[]{a.name, a.category, a.score});
                }
            }
            double classPercent = calculateClassPercent(cd);
            double classGPA = percentToGPA(classPercent);
            classGpaLabel.setText("Class GPA: " + String.format("%.2f", classGPA));

            // update charts
            double hwAvg = avgFor(cd, "Homework");
            double exAvg = avgFor(cd, "Exam");
            double prAvg = avgFor(cd, "Project");
            piePanel.setData(hwAvg, exAvg, prAvg);
            trendPanel.setData(cd.historyPercent);
            badgePanel.setBadges(classGPA >= 3.8, isComeback(cd));
        });

        addAssignmentBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            ClassData cd = userData.get(currentUser).get(semesterNum).get(selectedClass);

            String aName = JOptionPane.showInputDialog(root, "Assignment name:");
            if (aName == null || aName.trim().isEmpty()) return;

            String[] categories = {"Homework", "Exam", "Project"};
            String category = (String) JOptionPane.showInputDialog(
                    root, "Select category:", "Assignment Type",
                    JOptionPane.PLAIN_MESSAGE, null, categories, "Homework");
            if (category == null) return;

            String sText = JOptionPane.showInputDialog(root, "Score (%):");
            if (sText == null || sText.trim().isEmpty()) return;

            try {
                double score = Double.parseDouble(sText);
                Assignment a = new Assignment(aName, score, category);
                cd.assignments.get(category).add(a);
                ((DefaultTableModel)table.getModel()).addRow(new Object[]{aName, category, score});
                pushHistory(cd);
                saveAllUserData();

                double classPercent = calculateClassPercent(cd);
                classGpaLabel.setText("Class GPA: " + String.format("%.2f", percentToGPA(classPercent)));
                updateOverallGpaLabel();

                // refresh visuals
                classList.repaint();
                piePanel.setData(avgFor(cd,"Homework"), avgFor(cd,"Exam"), avgFor(cd,"Project"));
                trendPanel.setData(cd.historyPercent);
                badgePanel.setBadges(percentToGPA(classPercent) >= 3.8, isComeback(cd));
            } catch (Exception ignored) {}
        });

        deleteAssignmentBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            int row = table.getSelectedRow();
            if (row < 0) return;

            ClassData cd = userData.get(currentUser).get(semesterNum).get(selectedClass);
            String aName = (String) model.getValueAt(row, 0);
            String category = (String) model.getValueAt(row, 1);

            List<Assignment> list = cd.assignments.get(category);
            list.removeIf(a -> a.name.equals(aName));
            model.removeRow(row);

            pushHistory(cd);
            saveAllUserData();

            double classPercent = calculateClassPercent(cd);
            classGpaLabel.setText("Class GPA: " + String.format("%.2f", percentToGPA(classPercent)));
            updateOverallGpaLabel();

            classList.repaint();
            piePanel.setData(avgFor(cd,"Homework"), avgFor(cd,"Exam"), avgFor(cd,"Project"));
            trendPanel.setData(cd.historyPercent);
            badgePanel.setBadges(percentToGPA(classPercent) >= 3.8, isComeback(cd));
        });

        weightsBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            ClassData cd = userData.get(currentUser).get(semesterNum).get(selectedClass);

            try {
                int hw = Integer.parseInt(JOptionPane.showInputDialog(root, "Homework weight %:", cd.weights.get("Homework")));
                int ex = Integer.parseInt(JOptionPane.showInputDialog(root, "Exam weight %:", cd.weights.get("Exam")));
                int pr = Integer.parseInt(JOptionPane.showInputDialog(root, "Project weight %:", cd.weights.get("Project")));
                if (hw + ex + pr != 100) {
                    JOptionPane.showMessageDialog(root, "Weights must total 100%.");
                    return;
                }
                cd.weights.put("Homework", hw);
                cd.weights.put("Exam", ex);
                cd.weights.put("Project", pr);

                pushHistory(cd);
                saveAllUserData();

                double classPercent = calculateClassPercent(cd);
                classGpaLabel.setText("Class GPA: " + String.format("%.2f", percentToGPA(classPercent)));
                updateOverallGpaLabel();

                classList.repaint();
                piePanel.setData(avgFor(cd,"Homework"), avgFor(cd,"Exam"), avgFor(cd,"Project"));
                trendPanel.setData(cd.historyPercent);
                badgePanel.setBadges(percentToGPA(classPercent) >= 3.8, isComeback(cd));
            } catch (Exception ignored) {}
        });

        creditsBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            ClassData cd = userData.get(currentUser).get(semesterNum).get(selectedClass);
            String newC = JOptionPane.showInputDialog(root, "Credit hours:", cd.credits);
            try {
                if (newC != null && !newC.trim().isEmpty()) {
                    int val = Math.max(0, Integer.parseInt(newC.trim()));
                    cd.credits = val;
                    saveAllUserData();
                    updateOverallGpaLabel();
                }
            } catch (Exception ignored) {}
        });

        return root;
    }

    // ===== User Profile =====
    private static void showUserPanel(JFrame parent) {
        String[] data = users.get(currentUser);
        String password = data[0];
        String email = data[1];

        JPanel panel = new JPanel(new GridLayout(6,2,10,10));
        panel.setBorder(new EmptyBorder(10,10,10,10));

        JTextField emailField = new JTextField(email);
        emailField.setEditable(false);
        JTextField userField = new JTextField(currentUser);
        userField.setEditable(false);

        JPasswordField passField = new JPasswordField(password);

        JButton savePass = pillButton("Save Password");
        JButton changeUsernameBtn = pillButton("Change Username");
        JButton unlinkGoogleBtn = pillButton("Unlink Google");

        long lastChange = lastUsernameChange.getOrDefault(currentUser, 0L);
        long daysSince = (System.currentTimeMillis() - lastChange) / (1000L*60*60*24);
        changeUsernameBtn.setEnabled(daysSince >= 15);

        panel.add(new JLabel("Email:")); panel.add(emailField);
        panel.add(new JLabel("Username:")); panel.add(userField);
        panel.add(new JLabel("Password:")); panel.add(passField);
        panel.add(new JLabel()); panel.add(savePass);
        panel.add(new JLabel("Change Username (every 15 days):")); panel.add(changeUsernameBtn);
        panel.add(new JLabel("Google Link:")); panel.add(unlinkGoogleBtn);

        // change password
        savePass.addActionListener(_ -> {
            users.get(currentUser)[0] = new String(passField.getPassword());
            saveUsers();
            JOptionPane.showMessageDialog(parent, "Password updated.");
        });

        // change username
        changeUsernameBtn.addActionListener(_ -> {
            String newUsername = JOptionPane.showInputDialog(parent, "Enter new username:");
            if (newUsername == null || newUsername.trim().isEmpty()) return;
            if (users.containsKey(newUsername)) {
                JOptionPane.showMessageDialog(parent, "That username is taken.");
                return;
            }
            users.put(newUsername, users.remove(currentUser));
            userData.put(newUsername, userData.remove(currentUser));
            lastUsernameChange.put(newUsername, System.currentTimeMillis());
            currentUser = newUsername;
            saveUsers();
            saveAllUserData();
            JOptionPane.showMessageDialog(parent, "Username changed. Locked for 15 days.");
        });

        // unlink Google (empty password = was created via Google)
        unlinkGoogleBtn.addActionListener(_ -> {
            String[] info = users.get(currentUser);
            if (info[0].isEmpty()) {
                String newPass = JOptionPane.showInputDialog(parent, "Set a new password:");
                if (newPass != null && !newPass.trim().isEmpty()) {
                    info[0] = newPass;
                    saveUsers();
                    JOptionPane.showMessageDialog(parent, "Google account unlinked. You can now log in with email + password.");
                }
            } else {
                JOptionPane.showMessageDialog(parent, "This account already has a password.");
            }
        });

        JOptionPane.showMessageDialog(parent, panel, "Your Profile", JOptionPane.PLAIN_MESSAGE);
    }

    // ===== Utilities & GPA =====
    private static JButton pillButton(String text) {
        return pillButton(text, null);
    }

    private static JButton pillButton(String text, Icon icon) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                // Let JButton draw icon + text normally (we handle background only)
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        if (icon != null) {
            b.setIcon(icon);
            b.setHorizontalTextPosition(SwingConstants.RIGHT);
            // leave some left padding so icon + text have breathing room
            b.setBorder(BorderFactory.createEmptyBorder(10, 14 + icon.getIconWidth(), 10, 16));
        } else {
            b.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
        }
    b.setContentAreaFilled(false);
    b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(new Color(240,240,240));
        b.setForeground(Color.BLACK);
        b.setOpaque(false);
        // Hover animation: smoothly blend background towards hover color
        final int animMs = 220;
        final javax.swing.Timer[] timer = new javax.swing.Timer[1];
        // remember the original background at the time the mouse first enters so we can reliably
        // animate back to that exact color on exit (avoids no-op/incorrect target calculations)
        final Color[] originalBase = new Color[1];
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (timer[0] != null && timer[0].isRunning()) timer[0].stop();
                // capture the true original base color once per hover cycle
                originalBase[0] = b.getBackground();
                final Color startColor = originalBase[0];
                final Color hover = blend(startColor, new Color(220,235,255), 0.6f);
                long start = System.currentTimeMillis();
                timer[0] = new javax.swing.Timer(15, new ActionListener() {
                    @Override public void actionPerformed(ActionEvent ev) {
                        float t = Math.min(1f, (System.currentTimeMillis() - start) / (float) animMs);
                        b.setBackground(blend(startColor, hover, t));
                        if (t >= 1f) timer[0].stop();
                    }
                });
                timer[0].start();
            }
            @Override public void mouseExited(MouseEvent e) {
                if (timer[0] != null && timer[0].isRunning()) timer[0].stop();
                final Color current = b.getBackground();
                final Color target = originalBase[0] != null ? originalBase[0] : new Color(240,240,240);
                long start = System.currentTimeMillis();
                timer[0] = new javax.swing.Timer(15, new ActionListener() {
                    @Override public void actionPerformed(ActionEvent ev) {
                        float t = Math.min(1f, (System.currentTimeMillis() - start) / (float) animMs);
                        b.setBackground(blend(current, target, t));
                        if (t >= 1f) timer[0].stop();
                    }
                });
                timer[0].start();
            }
            @Override public void mousePressed(MouseEvent e) {
                Color base = b.getBackground();
                Color hover = blend(base, new Color(220,235,255), 0.9f);
                b.setBackground(hover);
            }
            @Override public void mouseReleased(MouseEvent e) {
                // leave the current hovered color; mouseExited will animate it back when the cursor leaves
            }
        });
        return b;
    }

    // blend two colors by t (0..1)
    private static Color blend(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int alpha = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, alpha);
    }

    // create a square ImageIcon sized to `size` pixels (keeps aspect and centers the source icon)
    private static Icon sizedIcon(Icon ic, int size) {
        if (ic == null) return null;
        int iw = ic.getIconWidth();
        int ih = ic.getIconHeight();
        if (iw == size && ih == size) return ic;
        java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double scale = Math.min((double)size / Math.max(1, iw), (double)size / Math.max(1, ih));
        int dw = (int) Math.round(iw * scale);
        int dh = (int) Math.round(ih * scale);
        int dx = (size - dw) / 2;
        int dy = (size - dh) / 2;
        // render source icon into a temp image if it's not an ImageIcon
        if (ic instanceof ImageIcon) {
            Image img = ((ImageIcon) ic).getImage().getScaledInstance(dw, dh, Image.SCALE_SMOOTH);
            g2.drawImage(img, dx, dy, null);
        } else {
            java.awt.image.BufferedImage tmp = new java.awt.image.BufferedImage(iw, ih, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D t = tmp.createGraphics();
            t.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ic.paintIcon(null, t, 0, 0);
            t.dispose();
            Image scaled = tmp.getScaledInstance(dw, dh, Image.SCALE_SMOOTH);
            g2.drawImage(scaled, dx, dy, null);
        }
        g2.dispose();
        return new ImageIcon(out);
    }

    private static void applyTheme(java.awt.Container root) {
        if (darkMode) {
            root.setBackground(new Color(35, 35, 35));
            if (overallGpaLabel != null) overallGpaLabel.setForeground(Color.WHITE);
        } else {
            root.setBackground(Color.WHITE);
            if (overallGpaLabel != null) overallGpaLabel.setForeground(Color.BLACK);
        }
    }

    private static void updateOverallGpaLabel() {
        if (overallGpaLabel != null && currentUser != null) {
            overallGpaLabel.setText("Overall GPA: " + String.format("%.2f", calculateOverallGPA(currentUser)));
        }
    }

    private static String findUserByEmail(String email) {
        if (email == null) return null;
        for (Map.Entry<String, String[]> e : users.entrySet()) {
            if (email.equalsIgnoreCase(e.getValue()[1])) return e.getKey();
        }
        return null;
    }

    // trend check
    private static boolean isComeback(ClassData cd) {
        List<Double> h = cd.historyPercent;
        if (h.size() < 3) return false;
        double n = h.get(h.size()-1);
        double prev = h.get(h.size()-2);
        double back3 = h.get(h.size()-3);
        return (back3 < 70 && n >= 80 && n > prev); // rough rule
    }

    private static void pushHistory(ClassData cd) {
        double p = calculateClassPercent(cd);
        if (Double.isNaN(p)) p = 0;
        cd.historyPercent.add(Math.max(0, Math.min(100, p)));
        if (cd.historyPercent.size() > 30) {
            cd.historyPercent.remove(0);
        }
    }

    private static double avgFor(ClassData cd, String cat) {
        return cd.assignments.get(cat).stream().mapToDouble(a -> a.score).average().orElse(0);
    }

    private static Color barColorFor(double percent) {
        if (percent >= 85) return new Color(46, 204, 113);   // green
        if (percent >= 75) return new Color(243, 156, 18);   // yellow/orange
        return new Color(231, 76, 60);                       // red
    }

    // ===== GPA/Percent CALCULATIONS =====
    private static double calculateClassPercent(ClassData cd) {
        if (cd == null) return 0.0;
        double weightedTotal = 0;
        double weightSum = 0;
        for (String cat : cd.assignments.keySet()) {
            double avg = avgFor(cd, cat);
            int weight = cd.weights.getOrDefault(cat, 0);
            weightedTotal += avg * (weight / 100.0);
            weightSum += weight;
        }
        return (weightSum > 0) ? weightedTotal : 0.0;
    }

    private static double calculateClassGPA(ClassData cd) {
        return percentToGPA(calculateClassPercent(cd));
    }

    private static double calculateOverallGPA(String user) {
        if (!userData.containsKey(user)) return 0.0;
        double totalPoints = 0.0;
        int totalCredits = 0;
        for (int sem : userData.get(user).keySet()) {
            for (ClassData cd : userData.get(user).get(sem).values()) {
                double gpa = calculateClassGPA(cd);
                totalPoints += gpa * cd.credits;
                totalCredits += cd.credits;
            }
        }
        return (totalCredits > 0) ? totalPoints / totalCredits : 0.0;
    }

    private static double percentToGPA(double percent) {
        if (percent >= 90) return 4.0;
        else if (percent >= 80) return 3.0;
        else if (percent >= 70) return 2.0;
        else if (percent >= 60) return 1.0;
        else return 0.0;
    }

    // ===== SAVE/LOAD (JSON with Gson) =====
    private static void ensureDataDir() {
        File d = new File(DATA_DIR);
        if (!d.exists()) d.mkdirs();
    }

    private static void ensureUserStructures(String user) {
        userData.putIfAbsent(user, new HashMap<>());
        for (int i = 1; i <= 4; i++) {
            userData.get(user).putIfAbsent(i, new HashMap<>());
        }
    }

    private static void saveUsers() {
        try (FileWriter fw = new FileWriter(USERS_FILE)) {
            gson.toJson(users, fw);
        } catch (IOException e) { e.printStackTrace(); }

        try (FileWriter fw = new FileWriter(USERNAME_CHANGES_FILE)) {
            gson.toJson(lastUsernameChange, fw);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void loadUsers() {
        File f = new File(USERS_FILE);
        if (f.exists()) {
            try (FileReader fr = new FileReader(f)) {
                Map<String, String[]> map = gson.fromJson(fr, new TypeToken<Map<String, String[]>>(){}.getType());
                if (map != null) users = map;
            } catch (IOException e) { e.printStackTrace(); }
        }

        File f2 = new File(USERNAME_CHANGES_FILE);
        if (f2.exists()) {
            try (FileReader fr = new FileReader(f2)) {
                Map<String, Long> map = gson.fromJson(fr, new TypeToken<Map<String, Long>>(){}.getType());
                if (map != null) lastUsernameChange = map;
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private static void saveAllUserData() {
        try (FileWriter fw = new FileWriter(USERDATA_FILE)) {
            gson.toJson(userData, fw);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void loadAllUserData() {
        File f = new File(USERDATA_FILE);
        if (!f.exists()) return;
        try (FileReader fr = new FileReader(f)) {
            Map<String, Map<Integer, Map<String, ClassData>>> map =
                    gson.fromJson(fr, new TypeToken<Map<String, Map<Integer, Map<String, ClassData>>>>(){}.getType());
            if (map != null) userData = map;
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ===== Custom Renderers & Panels =====
    static class ClassRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel name = new JLabel();
        private final JProgressBar bar = new JProgressBar(0, 100);
        private final int semester;
        ClassRenderer(int semester) {
            super(new BorderLayout(6,6));
            this.semester = semester;
            setBorder(new EmptyBorder(8,8,8,8));
            name.setFont(new Font("SansSerif", Font.BOLD, 16));
            bar.setStringPainted(true);
            bar.setUI(new BasicProgressBarUI(){
                @Override
                protected void paintDeterminate(Graphics g, JComponent c) {
                    super.paintDeterminate(g, c);
                }
            });
            add(name, BorderLayout.NORTH);
            add(bar, BorderLayout.CENTER);
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            name.setText(value);
            try {
                ClassData cd = userData.get(currentUser).get(semester).get(value);
                double p = calculateClassPercent(cd);
                bar.setValue((int)Math.round(p));
                bar.setForeground(barColorFor(p));
            } catch (Exception ignored) {}
            setBackground(isSelected ? new Color(232, 244, 255) : Color.WHITE);
            return this;
        }
    }

    static class PiePanel extends JPanel {
        double hw, ex, pr;
        void setData(double hw, double ex, double pr) { this.hw=hw; this.ex=ex; this.pr=pr; repaint(); }
        @Override public Dimension getPreferredSize(){ return new Dimension(320,200); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 40;
            int x = 20, y = 20;
            double sum = Math.max(1, hw + ex + pr);

            Color c1 = new Color(52, 152, 219);
            Color c2 = new Color(231, 76, 60);
            Color c3 = new Color(241, 196, 15);

            double start = 0;
            double a1 = 360 * (hw / sum);
            double a2 = 360 * (ex / sum);
            double a3 = 360 * (pr / sum);

            g2.setColor(c1); g2.fill(new Arc2D.Double(x, y, size, size, start, a1, Arc2D.PIE)); start += a1;
            g2.setColor(c2); g2.fill(new Arc2D.Double(x, y, size, size, start, a2, Arc2D.PIE)); start += a2;
            g2.setColor(c3); g2.fill(new Arc2D.Double(x, y, size, size, start, a3, Arc2D.PIE));

            // legend
            int lx = x + size + 12;
            int ly = y;
            g2.setColor(c1); g2.fill(new Ellipse2D.Double(lx, ly, 12, 12)); g2.setColor(getForeground());
            g2.drawString("Homework  " + (int)Math.round(hw) + " %", lx+18, ly+11);
            ly += 20;
            g2.setColor(c2); g2.fill(new Ellipse2D.Double(lx, ly, 12, 12)); g2.setColor(getForeground());
            g2.drawString("Exams     " + (int)Math.round(ex) + " %", lx+18, ly+11);
            ly += 20;
            g2.setColor(c3); g2.fill(new Ellipse2D.Double(lx, ly, 12, 12)); g2.setColor(getForeground());
            g2.drawString("Projects  " + (int)Math.round(pr) + " %", lx+18, ly+11);
        }
    }

    static class TrendPanel extends JPanel {
        List<Double> data = new ArrayList<>();
        void setData(List<Double> d){ data = new ArrayList<>(d); repaint(); }
        @Override public Dimension getPreferredSize(){ return new Dimension(320,120); }
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth()-30, h = getHeight()-30;
            int ox = 15, oy = 10;

            // axes
            g2.setColor(new Color(200,200,200));
            g2.drawLine(ox, oy+h, ox+w, oy+h);
            g2.drawLine(ox, oy, ox, oy+h);

            if (data == null || data.size() < 2) return;
            g2.setColor(new Color(52,152,219));
            g2.setStroke(new BasicStroke(2f));
            int n = data.size();
            for (int i=1;i<n;i++){
                int x1 = ox + (i-1)*w/(n-1);
                int x2 = ox + i*w/(n-1);
                int y1 = oy+h - (int)Math.round((data.get(i-1)/100.0)*h);
                int y2 = oy+h - (int)Math.round((data.get(i)/100.0)*h);
                g2.drawLine(x1,y1,x2,y2);
            }
        }
    }

    static class BadgePanel extends JPanel {
        boolean perfect, comeback;
        void setBadges(boolean p, boolean c){ perfect=p; comeback=c; repaint(); }
        @Override public Dimension getPreferredSize(){ return new Dimension(320,80); }
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = 16, y = 16;

            if (perfect) {
                g2.setColor(new Color(231,76,60));
                g2.fillOval(x, y, 36, 36);
                g2.setColor(Color.WHITE); g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                g2.drawString("Perfect Score", x+46, y+22);
                x += 160;
            }
            if (comeback) {
                g2.setColor(new Color(52,152,219));
                g2.fillOval(x, y, 36, 36);
                g2.setColor(Color.WHITE); g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                g2.drawString("Comeback Kid", x+46, y+22);
            }
            if (!perfect && !comeback) {
                g2.setColor(new Color(150,150,150));
                g2.drawString("No badges yet — keep going!", x, y+22);
            }
        }
    }

    // Simple two-color vertical gradient panel
    static class GradientPanel extends JPanel {
        private final Color top, bottom;
        GradientPanel(Color top, Color bottom) { this.top = top; this.bottom = bottom; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bottom);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    // Small painted Google-style 'G' icon (no external assets)
    static class GoogleIcon implements Icon {
        private final int w, h;
        GoogleIcon(int w, int h) { this.w = w; this.h = h; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(w, h);
            int stroke = Math.max(2, size/6);
            int pad = 1;
            int cx = x + pad, cy = y + pad, s = size - pad*2;
            // Blue segment
            g2.setColor(new Color(66,133,244));
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(cx, cy, s, s, 30, 120);
            // Red
            g2.setColor(new Color(219,68,55));
            g2.drawArc(cx, cy, s, s, 150, 80);
            // Yellow
            g2.setColor(new Color(244,180,0));
            g2.drawArc(cx, cy, s, s, 230, 40);
            // Green short tail
            g2.setColor(new Color(15,157,88));
            g2.drawLine(cx + s - stroke - 2, cy + s/2, cx + s - stroke - 2 - (s/4), cy + s/2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return w; }
        @Override public int getIconHeight() { return h; }
    }

    // Rounded card panel with subtle shadow
    static class RoundedCard extends JPanel {
        private final int radius;
        private final Color bg;
        private final Color shadow;
        RoundedCard(int radius, Color bg, Color shadow) { this.radius = radius; this.bg = bg; this.shadow = shadow; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            int w = getWidth(); int h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // softer shadow: paint multiple translucent layers
            for (int i = 6; i >= 2; i--) {
                int alpha = Math.max(10, 40 - (i*4));
                g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), alpha));
                g2.fillRoundRect(i, i, w - i*2, h - i*2, radius + i, radius + i);
            }
            // background
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w-12, h-12, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // simple person glyph icon
    static class PersonIcon implements Icon {
        private final int w,h;
        PersonIcon(int w, int h){ this.w=w; this.h=h; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(120,120,120));
            int cx = x + w/2; int cy = y + h/3;
            int r = Math.min(w,h)/4;
            g2.fillOval(cx - r, cy - r, r*2, r*2);
            g2.fillRoundRect(x + w/6, y + h/2, w - w/3, h/3, 6,6);
            g2.dispose();
        }
        @Override public int getIconWidth(){ return w; }
        @Override public int getIconHeight(){ return h; }
    }

    // simple lock glyph icon
    static class LockIcon implements Icon {
        private final int w,h;
        LockIcon(int w, int h){ this.w=w; this.h=h; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(120,120,120));
            int boxW = w - 4; int boxH = h - 6;
            g2.fillRoundRect(x+2, y+6, boxW, boxH-6, 4,4);
            g2.setStroke(new BasicStroke(2f));
            g2.drawArc(x + boxW/4, y+2, boxW/2, boxH/2, 0, 180);
            g2.dispose();
        }
        @Override public int getIconWidth(){ return w; }
        @Override public int getIconHeight(){ return h; }
    }

    // Simple placeholder-supporting text field
    static class PlaceholderTextField extends JTextField {
        private final String placeholder;
        PlaceholderTextField(String placeholder) { super(); this.placeholder = placeholder; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isFocusOwner() && getText().isEmpty() && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(120,120,120));
                g2.setFont(getFont().deriveFont(Font.PLAIN, 13f));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(placeholder);
                int x = Math.max(4, (getWidth() - textWidth) / 2);
                int y = getHeight() / 2 + fm.getAscent() / 2 - 2;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    static class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;
        PlaceholderPasswordField(String placeholder) { super(); this.placeholder = placeholder; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isFocusOwner() && getPassword().length == 0 && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(120,120,120));
                g2.setFont(getFont().deriveFont(Font.PLAIN, 13f));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(placeholder);
                int x = Math.max(4, (getWidth() - textWidth) / 2);
                int y = getHeight() / 2 + fm.getAscent() / 2 - 2;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    // ===== Reset Code Store =====
    static class PasswordResetStore {
        private static Map<String,String> tokenToUser = new HashMap<>();
        private static String file = "";

        static void init(String filePath) {
            file = filePath;
            File f = new File(file);
            if (!f.exists()) { persist(); return; }
            try (FileReader fr = new FileReader(f)) {
                Map<String,String> map = gson.fromJson(fr, new TypeToken<Map<String,String>>(){}.getType());
                tokenToUser = (map == null) ? new HashMap<>() : map;
            } catch (IOException e) { tokenToUser = new HashMap<>(); }
        }

        static String issueTokenFor(String username) {
            String token = generateCode();
            tokenToUser.put(token, username);
            persist();
            return token;
        }

        // Generate a token in-memory but do not persist it yet. Returns the token.
        static String generateTokenFor(String username) {
            return generateCode();
        }

        // Persist a previously generated token mapped to user (used after successful email send)
        static void persistToken(String token, String username) {
            tokenToUser.put(token, username);
            persist();
        }

        static String consume(String token) {
            String u = tokenToUser.remove(token);
            persist();
            return u;
        }

        private static String generateCode() {
            // 6-digit numeric code
            int n = 100000 + new java.util.Random().nextInt(900000);
            return String.valueOf(n);
        }
// 
        private static void persist() {
            try (FileWriter fw = new FileWriter(file)) {
                gson.toJson(tokenToUser, fw);
            } catch (IOException ignored) {}
        }
    }
}
