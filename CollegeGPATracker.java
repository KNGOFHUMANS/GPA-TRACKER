// Import statements - bringing in external libraries and classes needed for the application

// Swing imports - for creating the graphical user interface (GUI)
import javax.swing.*; // Imports all Swing classes (JFrame, JButton, JLabel, etc.)
import javax.swing.border.EmptyBorder; // For creating empty borders around components
import javax.swing.table.DefaultTableModel; // For managing data in JTable components
import javax.swing.plaf.basic.BasicProgressBarUI; // For customizing progress bar appearance
import javax.swing.JComponent; // Base class for all Swing components
import javax.swing.Box; // For creating invisible spacing components

// AWT (Abstract Window Toolkit) imports - for graphics, colors, fonts, and layout
import java.awt.*; // Imports all AWT classes (Color, Font, Graphics, etc.)
import java.awt.event.*; // For handling user interactions (clicks, key presses, etc.)
import java.awt.geom.Arc2D; // For drawing pie chart arcs in the grade breakdown
import java.awt.geom.Ellipse2D; // For drawing circles in the pie chart legend
import java.awt.image.BufferedImage; // For creating custom images from icons

// Java utility imports - for collections and data structures
import java.util.List; // For using List interface (ordered collections)
import java.util.*; // Imports all utility classes (Map, HashMap, ArrayList, etc.)

// File I/O imports - for reading and writing data to files
import java.io.File; // For working with file system paths
import java.io.FileReader; // For reading text files
import java.io.FileWriter; // For writing text files
import java.io.IOException; // Exception thrown when file operations fail

// Google Gson imports - for converting objects to/from JSON format
import com.google.gson.Gson; // Main class for JSON serialization/deserialization
import com.google.gson.reflect.TypeToken; // For handling complex generic types in JSON

/**
 * CollegeGPATracker - Main application class for tracking college GPA
 * This class manages user authentication, course data, and GPA calculations
 */
public class CollegeGPATracker {
    
    // ===== DATA STORAGE VARIABLES =====
    // Map that stores all user accounts: username -> [password, email]
    // Each user has a String array where [0] = password, [1] = email address
    private static Map<String, String[]> users = new HashMap<>();
    
    // Tracks when users last changed their username (for 15-day restriction)
    // Maps username -> timestamp (milliseconds since epoch)
    private static Map<String, Long> lastUsernameChange = new HashMap<>();
    
    // Stores the currently logged-in user's username
    private static String currentUser;

    // Complex nested data structure for storing all user academic data:
    // user -> semesterName -> className -> ClassData object
    // This allows each user to have multiple named semesters (e.g., "Fall 2023", "Spring 2024")
    private static Map<String, Map<String, Map<String, ClassData>>> userData = new HashMap<>();
    
    // Maps semester names to display order for consistent tab ordering
    // Maps username -> semesterName -> order number
    private static Map<String, Map<String, Integer>> semesterOrder = new HashMap<>();
    
    // Boolean flag to track if dark mode is enabled for the UI
    private static boolean darkMode = false;

    // ===== FILE PATHS AND CONSTANTS =====
    // Directory name where all data files are stored
    private static final String DATA_DIR = "data";
    
    // Full path to the JSON file storing user account information
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.json";
    
    // Full path to the JSON file storing all academic data
    private static final String USERDATA_FILE = DATA_DIR + File.separator + "user_data.json";
    
    // Full path to file tracking username change timestamps
    private static final String USERNAME_CHANGES_FILE = DATA_DIR + File.separator + "username_changes.json";
    
    // Full path to file storing password reset tokens
    private static final String RESET_CODES_FILE = DATA_DIR + File.separator + "reset_tokens.json";
    
    // Full path to file storing login session information
    private static final String SESSION_FILE = DATA_DIR + File.separator + "session.json";
    
    // Gson instance for converting Java objects to/from JSON format
    private static final Gson gson = new Gson();

    // ===== GRADERISE BRANDING COLOR SCHEME =====
    // Primary GradeRise brand colors (matching logo)
    private static final Color BRAND_PRIMARY = new Color(0x8B1538);   // Dark red from logo
    private static final Color GRADIENT_START = new Color(0x6D1028);  // Darker red start
    private static final Color GRADIENT_END = BRAND_PRIMARY;          // Main red end
    
    // Grey theme background with dark red accents
    private static final Color BG_LIGHT = new Color(0xE5E7EB);       // Light grey background
    private static final Color CARD_LIGHT = new Color(0xF9FAFB);     // Light grey cards
    
    // Text colors
    private static final Color TEXT_DARK = new Color(0x1F2937);      // Dark text on light
    private static final Color TEXT_MUTED = new Color(0x6B7280);     // Muted grey text
    
    // Status colors with brand harmony
    private static final Color SUCCESS_EMERALD = new Color(0x10B981); // Success green
    private static final Color WARNING_AMBER = new Color(0xF59E0B);   // Warning amber
    private static final Color DANGER_ROSE = BRAND_PRIMARY;           // Use brand red for danger
    private static final Color INFO_BRAND = new Color(0x3B82F6);      // Blue for info
    
    // Enhanced accent colors
    private static final Color BORDER_SUBTLE = new Color(0xD1D5DB);    // Subtle grey borders

    // Legacy compatibility colors
    private static final Color RIGHT_BG = BG_LIGHT;
    private static final Color CARD_BG = CARD_LIGHT;
    private static final Color BORDER_LIGHT = BORDER_SUBTLE;
    private static final Color TEXT_PRIMARY = TEXT_DARK;
    private static final Color TEXT_SECONDARY = TEXT_MUTED;
    private static final Color SUCCESS_GREEN = SUCCESS_EMERALD;
    private static final Color WARNING_ORANGE = WARNING_AMBER;

    // Dark theme colors for login screen
    private static final Color RIGHT_BG_DARK = new Color(0x4B5563);
    private static final Color CARD_BG_DARK = new Color(0x6B7280);
    private static final Color INPUT_BG_DARK = new Color(0x9CA3AF);
    private static final Color INPUT_BORDER_DARK = new Color(0xD1D5DB);
    private static final Color PRIMARY_DARK = BRAND_PRIMARY;

    // Label that displays the overall GPA on the dashboard
    private static JLabel overallGpaLabel;

    // ===== INNER CLASSES FOR DATA STRUCTURE =====
    
    /**
     * ClassData - Stores all information about a single class/course
     * Contains assignments grouped by category, weights for each category,
     * and historical performance data for trend analysis
     */
    static class ClassData {
        // Maps category name (Homework, Exam, Project) to list of assignments in that category
        Map<String, List<Assignment>> assignments = new HashMap<>();
        
        // Maps category name to its weight percentage (must total 100%)
        Map<String, Integer> weights = new HashMap<>();
        
        // Historical list of class percentage scores for trend tracking
        List<Double> historyPercent = new ArrayList<>();
        
        // Number of credit hours this class is worth (affects overall GPA calculation)
        int credits = 3; // Default to 3 credit hours per class
        
        // Class status: true = active (affects current GPA), false = past (archived)
        boolean isActive = true; // Default to active for new classes
        
        // Final grade for past classes (when assignments aren't tracked individually)
        // -1 means no final grade set, 0-100 represents the final percentage
        double finalGrade = -1.0;
        
        // Letter grade for past classes (A, B, C, D, F)
        // Empty string means no letter grade set
        String letterGrade = "";

        /**
         * Constructor - Sets up default categories and weights for a new class
         * Creates three default categories: Homework (40%), Exam (40%), Project (20%)
         */
        public ClassData() {
            // Initialize empty assignment lists for each category
            assignments.put("Homework", new ArrayList<>());
            assignments.put("Exam", new ArrayList<>());
            assignments.put("Project", new ArrayList<>());
            
            // Set default weight distribution (totals 100%)
            weights.put("Homework", 40);  // Homework worth 40% of grade
            weights.put("Exam", 40);      // Exams worth 40% of grade
            weights.put("Project", 20);   // Projects worth 20% of grade
        }
    }
    
    /**
     * Assignment - Represents a single assignment/test/project
     * Simple data class holding the assignment's name, score, and category
     */
    static class Assignment {
        String name;     // Name of the assignment (e.g., "Homework 1", "Midterm Exam")
        double score;    // Score as a percentage (0-100)
        String category; // Which category this belongs to (Homework, Exam, Project)
        
        /**
         * Constructor - Creates a new assignment with the given details
         */
        Assignment(String name, double score, String category) {
            this.name = name;         // Store assignment name
            this.score = score;       // Store percentage score
            this.category = category; // Store which category it belongs to
        }
    }
    // ===== APPLICATION ENTRY POINT =====
    
    /**
     * main - Entry point for the application
     * Sets up data directories, loads saved data, and launches the login UI
     */
    public static void main(String[] args) {
        ensureDataDir();                              // Create data directory if it doesn't exist
        loadUsers();                                  // Load user accounts from JSON file
        loadAllUserData();                           // Load all academic data from JSON file
        PasswordResetStore.init(RESET_CODES_FILE);   // Initialize password reset token system
        
        // Check for existing login session
        String savedUser = loadSession();
        if (savedUser != null && users.containsKey(savedUser)) {
            currentUser = savedUser;
            ensureUserStructures(currentUser);
            // Launch directly to dashboard
            SwingUtilities.invokeLater(CollegeGPATracker::showDashboard);
        } else {
            // Launch the login UI
            SwingUtilities.invokeLater(CollegeGPATracker::showLoginUI);
        }
    }

    // ===== LOGIN PAGE =====
    private static void showLoginUI() {
        // Modern login UI with gradient background and card-style form
        JFrame frame = new JFrame("Login - GPA Tracker");
        frame.setSize(1000, 560);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // Set custom GradeRise icon
        frame.setIconImage(iconToImage(new GradeRiseIcon(32, 32)));

        // Left panel with gradient background and title
        JPanel leftPanel = new GradientPanel(GRADIENT_START, GRADIENT_END);
        leftPanel.setPreferredSize(new Dimension(420, 0));
        JLabel msg = new JLabel("<html><center><span style='font-size:36pt'>🎓</span><br><span style='font-size:20pt; font-weight:bold'>GradeRise</span><br><span style='font-size:14pt'>Rise Above Average</span></center></html>", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.BOLD, 28));
        msg.setForeground(Color.WHITE);
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(msg, BorderLayout.CENTER);
        leftPanel.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Right panel with dark background for the login form
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(RIGHT_BG_DARK);

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
    
    // Title spacing
    JLabel title = new JLabel("", SwingConstants.CENTER);
    title.setPreferredSize(new Dimension(0, 8));
    loginBox.add(title);
    
    // Input rows with consistent backgrounds
    userRow.setBackground(INPUT_BG_DARK);
    passRow.setBackground(INPUT_BG_DARK);
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
    mailHelp.setForeground(new Color(180, 180, 180));
    mailHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    mailHelp.setToolTipText("Use a Gmail App Password (create one in your Google Account under Security → App passwords).\nYou can enter it once in the prompt; optionally saved locally (plaintext).");
    mailHelp.setAlignmentX(Component.CENTER_ALIGNMENT);
    loginBox.add(mailHelp);
    loginBox.add(Box.createVerticalStrut(20));
    loginBox.add(googleBtn);
    loginBox.add(Box.createVerticalGlue());

    // wrap visually with a rounded card
    RoundedCard card = new RoundedCard(12, CARD_BG_DARK, new Color(200,200,210,110));
    card.setLayout(new GridBagLayout());
    card.add(loginBox);
    
    JPanel wrapper = new JPanel(new GridBagLayout());
    wrapper.setBackground(RIGHT_BG_DARK);
    GridBagConstraints wrapperGbc = new GridBagConstraints();
    wrapperGbc.gridx = 0;
    wrapperGbc.gridy = 0;
    wrapperGbc.weightx = 1.0;
    wrapperGbc.weighty = 1.0;
    wrapperGbc.anchor = GridBagConstraints.CENTER;
    wrapper.add(card, wrapperGbc);
    
    rightPanel.add(wrapper, BorderLayout.CENTER);

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
                saveSession(currentUser);  // Save login session
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
               System.out.println("DEBUG: Starting Google Sign-In process...");
               
               // Show progress dialog
               JDialog progressDialog = new JDialog(frame, "Google Sign-In", true);
               progressDialog.setSize(300, 150);
               progressDialog.setLocationRelativeTo(frame);
               JLabel progressLabel = new JLabel("Connecting to Google...", SwingConstants.CENTER);
               progressDialog.add(progressLabel);
               
               // Start authentication in background thread
               SwingWorker<String[], Exception> worker = new SwingWorker<String[], Exception>() {
                   @Override
                   protected String[] doInBackground() throws Exception {
                       return GoogleSignIn.authenticate();
                   }
                   
                   @Override
                   protected void done() {
                       progressDialog.dispose();
                       try {
                           String[] result = get();
                           System.out.println("DEBUG: Google auth successful, email: " + result[0]);
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
                           saveSession(currentUser);  // Save login session

                           frame.dispose();
                           showDashboard();
                       } catch (Exception e) {
                           System.err.println("DEBUG: Google Sign-In error details:");
                           e.printStackTrace();
                           
                           String errorMsg = e.getMessage();
                           if (errorMsg != null && errorMsg.contains("OAuth credentials not configured")) {
                               // OAuth configuration error - show detailed setup instructions
                               JOptionPane.showMessageDialog(frame, 
                                   "Google Sign-In Setup Required\n\n" +
                                   "OAuth credentials are not configured properly.\n\n" +
                                   "Setup Instructions:\n" +
                                   "1. Go to https://console.cloud.google.com/\n" +
                                   "2. Create a new project or select existing\n" +
                                   "3. Enable Google+ API and Gmail API\n" +
                                   "4. Create OAuth 2.0 credentials (Desktop application)\n" +
                                   "5. Download the JSON file and rename to 'client_secret.json'\n" +
                                   "6. Place it in the application directory\n\n" +
                                   "See SETUP.md for detailed instructions.", 
                                   "OAuth Setup Required", JOptionPane.WARNING_MESSAGE);
                           } else if (errorMsg != null && errorMsg.contains("400")) {
                               // Google API 400 error - likely OAuth config issue
                               JOptionPane.showMessageDialog(frame,
                                   "Google Authentication Error (400)\n\n" +
                                   "This usually indicates OAuth configuration issues:\n" +
                                   "• Invalid client ID or secret\n" +
                                   "• Redirect URI mismatch\n" +
                                   "• Project not properly configured\n\n" +
                                   "Please check your client_secret.json file and\n" +
                                   "Google Cloud Console project settings.\n\n" +
                                   "See SETUP.md for detailed instructions.",
                                   "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                           } else {
                               // General error
                               JOptionPane.showMessageDialog(frame, 
                                   "Google Sign-In failed: " + errorMsg + 
                                   "\n\nPlease check:\n1. Internet connection\n2. Browser opens for authorization\n3. Console output for detailed error\n4. OAuth credentials in client_secret.json", 
                                   "Sign-In Failed", JOptionPane.ERROR_MESSAGE);
                           }
                       }
                   }
               };
               
               worker.execute();
               progressDialog.setVisible(true);
               
           } catch (Exception e) {
                System.err.println("DEBUG: Google Sign-In error details:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Google Sign-In failed: " + e.getMessage() + "\n\nPlease check:\n1. Internet connection\n2. client_secret.json file is present\n3. Console output for detailed error");
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
        
        // Set custom GradeRise icon
        frame.setIconImage(iconToImage(new GradeRiseIcon(32, 32)));

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

        // GradeRise Branded Header with Gradient
        JPanel headerPanel = createGradeRiseHeader(currentUser);
        frame.add(headerPanel, BorderLayout.NORTH);

        // GradeRise Modern Pill Tabs
        JTabbedPane semesters = createModernTabbedPane();
        
        // Load existing semesters for current user
        Map<String, Map<String, ClassData>> userSemesters = userData.get(currentUser);
        if (userSemesters.isEmpty()) {
            // Default: add first semester for new users
            String defaultSemester = "Semester 1";
            userSemesters.put(defaultSemester, new HashMap<>());
            // Initialize semester ordering without using a lambda parameter to avoid unused-parameter warnings/errors
            semesterOrder.putIfAbsent(currentUser, new HashMap<>());
            semesterOrder.get(currentUser).put(defaultSemester, 1);
        }
        
        // Add tabs with modern pill design and icons (sorted by order)
        List<String> sortedSemesters = new ArrayList<>(userSemesters.keySet());
        Map<String, Integer> userOrder = semesterOrder.get(currentUser);
        if (userOrder != null) {
            sortedSemesters.sort((a, b) -> {
                Integer orderA = userOrder.getOrDefault(a, 999);
                Integer orderB = userOrder.getOrDefault(b, 999);
                return orderA.compareTo(orderB);
            });
        }
        
        for (String semesterName : sortedSemesters) {
            JPanel semesterPanel = createSemesterPanel(semesterName);
            semesterPanel.setBackground(RIGHT_BG);
            String tabIcon = semesterName.equals(getCurrentSemester(userSemesters)) ? "📘" : "📗";
            semesters.add(tabIcon + " " + semesterName, semesterPanel);
        }
        
        // Add "+" tab with enhanced styling
        JPanel addSemesterPanel = createAddSemesterPanel(semesters, frame);
        semesters.add("➕ New", addSemesterPanel);
        semesters.setToolTipTextAt(semesters.getTabCount() - 1, "Add New Semester");
        
        // Style the main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(RIGHT_BG);
        mainContainer.add(semesters, BorderLayout.CENTER);
        frame.add(mainContainer, BorderLayout.CENTER);

        // handlers
        signOut.addActionListener(_ -> {
            currentUser = null;
            clearSession();  // Clear saved session
            frame.dispose();
            showLoginUI();
        });

        signOutGoogle.addActionListener(_ -> {
            try {
                GoogleSignIn.clearStoredCredentials();
                JOptionPane.showMessageDialog(frame, 
                    "Google account signed out successfully.\n\n" +
                    "Next time you sign in with Google, you'll be prompted to choose an account.", 
                    "Google Sign-Out", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                System.err.println("Error clearing Google credentials: " + e.getMessage());
            }
            currentUser = null;
            clearSession();  // Clear saved session
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

    // ===== GRADERISE HEADER =====
    private static JPanel createGradeRiseHeader(String username) {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, GRADIENT_START,
                    getWidth(), 0, GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle glow effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight() / 3);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        // Left side - Logo and Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        leftPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel("🎓");
        logoLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel("GradeRise Dashboard");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        leftPanel.add(logoLabel);
        leftPanel.add(titleLabel);
        
        // Center - GPA Display with Achievement Badge
        double gpa = calculateOverallGPA(username);
        String achievementText = gpa >= 3.7 ? "⭐ High Achiever" : 
                                gpa >= 3.0 ? "📈 On Track" : 
                                "💪 Keep Going";
        
        overallGpaLabel = new JLabel("Overall GPA: " + String.format("%.2f", gpa) + " " + achievementText);
        overallGpaLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        overallGpaLabel.setForeground(Color.WHITE);
        overallGpaLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add text shadow effect
        overallGpaLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(overallGpaLabel, BorderLayout.CENTER);
        
        return headerPanel;
    }

    // ===== MODERN TABBED PANE =====
    private static JTabbedPane createModernTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Custom tab rendering with rounded corners and gradient underline
                int selectedIndex = getSelectedIndex();
                if (selectedIndex >= 0) {
                    Rectangle tabBounds = getBoundsAt(selectedIndex);
                    if (tabBounds != null) {
                        // Gradient underline for active tab
                        GradientPaint gradient = new GradientPaint(
                            tabBounds.x, tabBounds.y + tabBounds.height - 3,
                            GRADIENT_START,
                            tabBounds.x + tabBounds.width, tabBounds.y + tabBounds.height - 3,
                            GRADIENT_END
                        );
                        g2d.setPaint(gradient);
                        g2d.fillRoundRect(tabBounds.x + 10, tabBounds.y + tabBounds.height - 3, 
                                        tabBounds.width - 20, 3, 3, 3);
                    }
                }
            }
        };
        
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabbedPane.setBackground(RIGHT_BG);
        tabbedPane.setBorder(new EmptyBorder(10, 25, 15, 25));
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        
        return tabbedPane;
    }
    
    private static String getCurrentSemester(Map<String, Map<String, ClassData>> userSemesters) {
        // Return the most recently added semester (highest order number)
        Map<String, Integer> userOrder = semesterOrder.get(currentUser);
        if (userOrder == null || userOrder.isEmpty()) {
            return userSemesters.keySet().stream().findFirst().orElse("Semester 1");
        }
        
        return userOrder.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Semester 1");
    }
    
    // ===== ANALYTICS PANEL =====
    private static JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(RIGHT_BG);
        return panel;
    }
    
    private static JPanel createAnalyticsCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 6, 12, 12);
                
                // Card background
                g2d.setColor(CARD_LIGHT);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 4, 12, 12);
                
                g2d.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Title with gradient accent
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_DARK);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }

    // ===== ADD SEMESTER PANEL =====
    private static JPanel createAddSemesterPanel(JTabbedPane semesters, JFrame parentFrame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0xF8F9FA));
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1),
            new EmptyBorder(40, 40, 40, 40)
        ));
        card.setMaximumSize(new Dimension(400, 300));
        
        JLabel icon = new JLabel("📚", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel title = new JLabel("Add New Semester", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitle = new JLabel("Track classes for a new semester", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0x666666));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton addBtn = pillButton("+ Add Semester");
        addBtn.setBackground(new Color(0x2F80ED));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add semester button logic
        addBtn.addActionListener(_ -> {
            // Get semester details with suggestions
            JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            JTextField nameField = new JTextField();
            JComboBox<String> suggestionBox = new JComboBox<>(new String[]{
                "Fall 2024", "Spring 2025", "Summer 2025", "Fall 2025",
                "Winter 2024", "Spring 2024", "Summer 2024"
            });
            suggestionBox.setEditable(true);
            
            inputPanel.add(new JLabel("Semester Name:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("Or choose from suggestions:"));
            inputPanel.add(suggestionBox);
            
            int result = JOptionPane.showConfirmDialog(parentFrame, inputPanel, 
                "Add New Semester", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result != JOptionPane.OK_OPTION) return;
            
            String semesterName = nameField.getText().trim();
            if (semesterName.isEmpty()) {
                Object selected = suggestionBox.getSelectedItem();
                semesterName = selected != null ? selected.toString().trim() : "";
            }
            
            if (semesterName.isEmpty()) {
                // Generate default name
                Map<String, Integer> userOrder = semesterOrder.get(currentUser);
                int nextNum = userOrder != null ? userOrder.size() + 1 : 1;
                semesterName = "Semester " + nextNum;
            }
            
            // Check if semester already exists
            if (userData.get(currentUser).containsKey(semesterName)) {
                JOptionPane.showMessageDialog(parentFrame, 
                    "A semester with that name already exists!", "Duplicate Name", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Add new semester
            userData.get(currentUser).put(semesterName, new HashMap<>());
            
            // Set semester order (avoid unused lambda parameter by using get/put)
            Map<String, Integer> userOrderMap = semesterOrder.get(currentUser);
            if (userOrderMap == null) {
                userOrderMap = new HashMap<>();
                semesterOrder.put(currentUser, userOrderMap);
            }
            int maxOrder = userOrderMap.values().stream().max(Integer::compareTo).orElse(0);
            userOrderMap.put(semesterName, maxOrder + 1);
            
            saveAllUserData();
            
            // Add tab before the "+" tab
            int insertIndex = semesters.getTabCount() - 1;
            semesters.insertTab("📗 " + semesterName, null, createSemesterPanel(semesterName), 
                semesterName, insertIndex);
            
            // Switch to new semester
            semesters.setSelectedIndex(insertIndex);
            
            // Update overall GPA
            updateOverallGpaLabel();
        });
        
        card.add(icon);
        card.add(Box.createVerticalStrut(16));
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(addBtn);
        
        panel.add(card);
        return panel;
    }

    // ===== SEMESTER PANEL =====
    private static JPanel createSemesterPanel(String semesterName) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(RIGHT_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Left: class list with progress bars
        DefaultListModel<String> classListModel = new DefaultListModel<>();
        JList<String> classList = new JList<>(classListModel);
        classList.setCellRenderer(new ClassRenderer(semesterName));
        classList.setFixedCellHeight(64);

        JScrollPane classScroll = new JScrollPane(classList);
        classScroll.setPreferredSize(new Dimension(260, 0));

        JButton addClassBtn = createGradeRiseButton("➕ Add Class", SUCCESS_EMERALD, true);
        JButton deleteClassBtn = createGradeRiseButton("🗑️ Delete Class", DANGER_ROSE, false);
        JButton toggleStatusBtn = createGradeRiseButton("📚 Toggle Status", INFO_BRAND, true);
        JButton removeSemesterBtn = createGradeRiseButton("⚠️ Remove Semester", DANGER_ROSE, false);
        removeSemesterBtn.setToolTipText("Delete this entire semester");
        
        JPanel leftTop = new JPanel(new GridLayout(2, 2, 5, 5));
        leftTop.add(addClassBtn);
        leftTop.add(deleteClassBtn);
        leftTop.add(toggleStatusBtn);
        leftTop.add(removeSemesterBtn);
        leftTop.add(new JLabel()); // Empty space

        JPanel left = new JPanel(new BorderLayout(12, 12));
        left.setBackground(CARD_BG);
        left.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            new EmptyBorder(16, 16, 16, 16)
        ));
        left.add(leftTop, BorderLayout.NORTH);
        left.add(classScroll, BorderLayout.CENTER);

        // Enhanced table with modern styling
        String[] cols = {"📝 Assignment", "📁 Category", "📊 Score (%)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0){
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setBackground(CARD_BG);
        table.setGridColor(BORDER_LIGHT);
        table.getTableHeader().setBackground(new Color(0xF7FAFC));
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));
        table.setSelectionBackground(new Color(0xEDF2F7));
        table.setSelectionForeground(TEXT_PRIMARY);

        JLabel classTitle = new JLabel("Select a class", SwingConstants.CENTER);
        classTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        classTitle.setBorder(BorderFactory.createTitledBorder(""));

        JButton addAssignmentBtn = createGradeRiseButton("➕ Add Assignment", SUCCESS_EMERALD, true);
        JButton deleteAssignmentBtn = createGradeRiseButton("🗑️ Delete Assignment", DANGER_ROSE, false);
        JButton weightsBtn = createGradeRiseButton("⚖️ Weights", INFO_BRAND, true);
        JButton creditsBtn = createGradeRiseButton("💎 Credits", WARNING_AMBER, false);
        JButton letterGradeBtn = createGradeRiseButton("🎓 Letter Grade", new Color(0x9333EA), false);
        letterGradeBtn.setToolTipText("Set letter grade for past classes (A, B, C, D, F)");
        
        // Modern class status controls
        JToggleButton showActiveBtn = new JToggleButton("✅ Active", true);
        JToggleButton showPastBtn = new JToggleButton("📚 Archived", false);
        ButtonGroup viewGroup = new ButtonGroup();
        viewGroup.add(showActiveBtn);
        viewGroup.add(showPastBtn);
        
        // Enhanced toggle button styling
        styleToggleButton(showActiveBtn, SUCCESS_GREEN, true);
        styleToggleButton(showPastBtn, TEXT_SECONDARY, false);
        
        // Enhanced action listeners for class status toggle
        showActiveBtn.addActionListener(_ -> {
            styleToggleButton(showActiveBtn, SUCCESS_GREEN, true);
            styleToggleButton(showPastBtn, TEXT_SECONDARY, false);
            refreshClassTable(model, semesterName, true); // Show only active classes
        });
        
        showPastBtn.addActionListener(_ -> {
            styleToggleButton(showActiveBtn, TEXT_SECONDARY, false);
            styleToggleButton(showPastBtn, WARNING_ORANGE, true);
            refreshClassTable(model, semesterName, false); // Show only past classes
        });

        JLabel classGpaLabel = new JLabel("Class GPA: —", SwingConstants.LEFT);
        classGpaLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel centerTop = new JPanel(new BorderLayout());
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        rightControls.add(showActiveBtn);
        rightControls.add(showPastBtn);
        rightControls.add(creditsBtn);
        rightControls.add(weightsBtn);
        rightControls.add(letterGradeBtn);
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

        // Enhanced GradeRise Analytics Panel
        JPanel rightDash = createAnalyticsPanel();
        PiePanel piePanel = new PiePanel();
        TrendPanel trendPanel = new TrendPanel();
        BadgePanel badgePanel = new BadgePanel();

        // Modern card containers with rounded corners and shadows
        JPanel weightsCard = createAnalyticsCard("📊 Grade Breakdown", piePanel);
        JPanel trendCard = createAnalyticsCard("📈 Performance Trend", trendPanel);
        JPanel badgesCard = createAnalyticsCard("🏆 Achievements", badgePanel);

        weightsCard.setMaximumSize(new Dimension(340, 260));
        trendCard.setMaximumSize(new Dimension(340, 200));
        badgesCard.setMaximumSize(new Dimension(340, 140));

        rightDash.add(weightsCard);
        rightDash.add(Box.createVerticalStrut(12));
        rightDash.add(trendCard);
        rightDash.add(Box.createVerticalStrut(12));
        rightDash.add(badgesCard);

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
        userData.get(currentUser).putIfAbsent(semesterName, new HashMap<>());
        // fill list
        for (String cls : userData.get(currentUser).get(semesterName).keySet()) {
            classListModel.addElement(cls);
        }

        // interactions
        addClassBtn.addActionListener(_ -> {
            String className = JOptionPane.showInputDialog(root, "Enter class name:");
            if (className == null || className.trim().isEmpty()) return;
            if (userData.get(currentUser).get(semesterName).containsKey(className)) {
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
            userData.get(currentUser).get(semesterName).put(className, cd);
            classListModel.addElement(className);
            saveAllUserData();
            updateOverallGpaLabel();
            classList.repaint();
        });

        deleteClassBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            userData.get(currentUser).get(semesterName).remove(selectedClass);
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
        
        toggleStatusBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) {
                JOptionPane.showMessageDialog(root, "Please select a class to toggle its status.");
                return;
            }
            
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);
            cd.isActive = !cd.isActive; // Toggle between active and past
            
            String status = cd.isActive ? "Active" : "Past";
            JOptionPane.showMessageDialog(root, 
                selectedClass + " is now marked as " + status + ".\n" +
                (cd.isActive ? "This class will affect your current GPA." : "This class is archived and won't affect your current GPA."));
            
            saveAllUserData();
            updateOverallGpaLabel();
            
            // Refresh the current view
            if (showActiveBtn.isSelected()) {
                refreshClassTable(model, semesterName, true);
            } else {
                refreshClassTable(model, semesterName, false);
            }
        });

        // Remove entire semester
        removeSemesterBtn.addActionListener(_ -> {
            // Prevent removing the last semester
            if (userData.get(currentUser).size() <= 1) {
                JOptionPane.showMessageDialog(root, "Cannot remove the last semester.\nYou must have at least one semester.");
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(root, 
                "Are you sure you want to delete " + semesterName + "?\n" +
                "This will permanently remove all classes and assignments in this semester.", 
                "Delete Semester", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Remove semester data
                userData.get(currentUser).remove(semesterName);
                // Also remove from semester order tracking
                Map<String, Integer> userOrder = semesterOrder.get(currentUser);
                if (userOrder != null) {
                    userOrder.remove(semesterName);
                }
                saveAllUserData();
                updateOverallGpaLabel();
                
                // Refresh the entire dashboard to update tabs
                SwingUtilities.getWindowAncestor(root).dispose();
                showDashboard();
            }
        });

        classList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            classTitle.setText(selectedClass + " — Assignments");

            model.setRowCount(0);
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);
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
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);

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

            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);
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
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);

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
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);
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

        // Letter Grade Button - For setting letter grades on past classes
        letterGradeBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) {
                JOptionPane.showMessageDialog(root, "Please select a class first.");
                return;
            }
            
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);
            
            // Letter grade options
            String[] grades = {"A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"};
            
            JPanel gradePanel = new JPanel(new GridLayout(3, 1, 10, 10));
            JComboBox<String> gradeCombo = new JComboBox<>(grades);
            if (!cd.letterGrade.isEmpty()) {
                gradeCombo.setSelectedItem(cd.letterGrade);
            }
            
            JLabel infoLabel = new JLabel("<html><center>Set the final letter grade for this class.<br>This is especially useful for past/archived classes.</center></html>");
            infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            
            JCheckBox isPastCheck = new JCheckBox("Mark as Past/Archived Class", !cd.isActive);
            isPastCheck.setToolTipText("Past classes use letter grades instead of individual assignments");
            
            gradePanel.add(infoLabel);
            gradePanel.add(new JLabel("Letter Grade:"));
            gradePanel.add(gradeCombo);
            gradePanel.add(isPastCheck);
            
            int result = JOptionPane.showConfirmDialog(root, gradePanel, 
                "Set Letter Grade - " + selectedClass, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String selectedGrade = (String) gradeCombo.getSelectedItem();
                if (selectedGrade != null) {
                    cd.letterGrade = selectedGrade;
                    cd.finalGrade = letterGradeToPercentage(selectedGrade);
                    
                    // Update active status if checkbox changed
                    boolean wasPast = !cd.isActive;
                    cd.isActive = !isPastCheck.isSelected();
                    
                    saveAllUserData();
                    updateOverallGpaLabel();
                    
                    // Refresh the class list and table
                    classList.repaint();
                    if (wasPast != isPastCheck.isSelected()) {
                        // Status changed, refresh the appropriate view
                        if (showActiveBtn.isSelected()) {
                            refreshClassTable(model, semesterName, true);
                        } else {
                            refreshClassTable(model, semesterName, false);
                        }
                    }
                    
                    // Update class title to show letter grade
                    if (!cd.letterGrade.isEmpty()) {
                        classTitle.setText(selectedClass + " - Grade: " + cd.letterGrade);
                    }
                    
                    JOptionPane.showMessageDialog(root, 
                        "Letter grade '" + selectedGrade + "' set for " + selectedClass + 
                        (isPastCheck.isSelected() ? " (marked as past class)" : " (active class)"));
                }
            }
        });

        return root;
    }

    // ===== User Profile =====
    private static void refreshClassTable(DefaultTableModel model, String semesterName, boolean showActive) {
        model.setRowCount(0); // Clear existing rows
        
        Map<String, ClassData> semesterData = userData.get(currentUser).get(semesterName);
        if (semesterData == null) return;
        
        for (String className : semesterData.keySet()) {
            ClassData classData = semesterData.get(className);
            
            // Check if class matches the active filter
            if (classData.isActive == showActive) {
                boolean hasAssignments = false;
                
                // Add all assignments for this class, from all categories
                for (String category : classData.assignments.keySet()) {
                    List<Assignment> categoryAssignments = classData.assignments.get(category);
                    for (Assignment assignment : categoryAssignments) {
                        Object[] row = {
                            className,
                            assignment.name,
                            assignment.score + "%",
                            classData.weights.get(category) + "%"
                        };
                        model.addRow(row);
                        hasAssignments = true;
                    }
                }
                
                // If no assignments, still show the class
                if (!hasAssignments) {
                    Object[] row = {className, "No assignments", "", ""};
                    model.addRow(row);
                }
            }
        }
    }

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
    private static JButton createGradeRiseButton(String text, Color bgColor, boolean isGradient) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient or solid background
                if (isGradient || bgColor == INFO_BRAND) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, GRADIENT_START,
                        getWidth(), getHeight(), GRADIENT_END
                    );
                    g2d.setPaint(gradient);
                } else {
                    g2d.setColor(isHovered ? bgColor.brighter() : bgColor);
                }
                
                // Draw rounded rectangle
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Add glow effect on hover
                if (isHovered) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g2d.setColor(Color.WHITE);
                    g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                }
                
                g2d.dispose();
                super.paintComponent(g);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                // No border painting - custom shape
            }
        };
        
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        
        // Enhanced hover effects with animation
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JButton) e.getSource()).putClientProperty("isHovered", true);
                e.getComponent().repaint();
                
                // Add glow shadow effect
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(2, 2, 4, 2),
                    new EmptyBorder(8, 16, 8, 16)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                ((JButton) e.getSource()).putClientProperty("isHovered", false);
                e.getComponent().repaint();
            }
        });
        
        return button;
    }
    
    private static void styleToggleButton(JToggleButton button, Color bgColor, boolean isActive) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Create custom painted button with GradeRise styling
        button.putClientProperty("isActive", isActive);
        button.putClientProperty("bgColor", bgColor);
        
        // Override paint method for custom rendering
        button.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean active = Boolean.TRUE.equals(c.getClientProperty("isActive"));
                Color color = (Color) c.getClientProperty("bgColor");
                
                if (active) {
                    // Gradient background for active state
                    if (color == SUCCESS_EMERALD) {
                        GradientPaint gradient = new GradientPaint(0, 0, GRADIENT_START, c.getWidth(), 0, color);
                        g2d.setPaint(gradient);
                    } else {
                        g2d.setColor(color);
                    }
                    g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                    g2d.setColor(Color.WHITE);
                } else {
                    // Inactive state
                    g2d.setColor(new Color(0xF7FAFC));
                    g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                    g2d.setColor(new Color(0xE5E7EB));
                    g2d.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 8, 8);
                    g2d.setColor(TEXT_MUTED);
                }
                
                // Draw text
                FontMetrics fm = g2d.getFontMetrics(c.getFont());
                String text = ((AbstractButton) c).getText();
                int x = (c.getWidth() - fm.stringWidth(text)) / 2;
                int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        });
        
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
    }
    
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

    // Helper method to create BufferedImage from Icon for window icons
    private static BufferedImage iconToImage(Icon icon) {
        BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        return img;
    }

    private static void applyTheme(java.awt.Container root) {
        if (darkMode) {
            root.setBackground(RIGHT_BG_DARK);
            if (overallGpaLabel != null) overallGpaLabel.setForeground(Color.WHITE);
        } else {
            root.setBackground(RIGHT_BG);
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
        
        // If this class has a letter grade set (for past classes), use that
        if (cd.letterGrade != null && !cd.letterGrade.isEmpty()) {
            return letterGradeToPercentage(cd.letterGrade);
        }
        
        // Otherwise, calculate from assignments as before
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
        if (cd == null) return 0.0;
        
        // If this class has a letter grade set (for past classes), use that directly
        if (cd.letterGrade != null && !cd.letterGrade.isEmpty()) {
            return letterGradeToGPA(cd.letterGrade);
        }
        
        // Otherwise, calculate from percentage as before
        return percentToGPA(calculateClassPercent(cd));
    }

    private static double calculateOverallGPA(String user) {
        if (!userData.containsKey(user)) return 0.0;
        double totalPoints = 0.0;
        int totalCredits = 0;
        for (String semesterName : userData.get(user).keySet()) {
            for (ClassData cd : userData.get(user).get(semesterName).values()) {
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
    
    // Convert letter grade to percentage for past classes
    private static double letterGradeToPercentage(String letterGrade) {
        return switch (letterGrade.toUpperCase()) {
            case "A" -> 95.0;
            case "A-" -> 90.0;
            case "B+" -> 87.0;
            case "B" -> 85.0;
            case "B-" -> 80.0;
            case "C+" -> 77.0;
            case "C" -> 75.0;
            case "C-" -> 70.0;
            case "D+" -> 67.0;
            case "D" -> 65.0;
            case "D-" -> 60.0;
            case "F" -> 50.0;
            default -> 0.0;
        };
    }
    
    // Convert letter grade directly to GPA points
    private static double letterGradeToGPA(String letterGrade) {
        return switch (letterGrade.toUpperCase()) {
            case "A" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "C-" -> 1.7;
            case "D+" -> 1.3;
            case "D" -> 1.0;
            case "D-" -> 0.7;
            case "F" -> 0.0;
            default -> 0.0;
        };
    }

    // ===== SAVE/LOAD (JSON with Gson) =====
    private static void ensureDataDir() {
        File d = new File(DATA_DIR);
        if (!d.exists()) d.mkdirs();
    }

    private static void ensureUserStructures(String user) {
        userData.putIfAbsent(user, new HashMap<>());
        semesterOrder.putIfAbsent(user, new HashMap<>());
        
        // If user has no semesters, add a default one
        if (userData.get(user).isEmpty()) {
            String defaultSemester = "Semester 1";
            userData.get(user).put(defaultSemester, new HashMap<>());
            semesterOrder.get(user).put(defaultSemester, 1);
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
        
        // Also save semester order
        try (FileWriter fw = new FileWriter(DATA_DIR + File.separator + "semester_order.json")) {
            gson.toJson(semesterOrder, fw);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void loadAllUserData() {
        File f = new File(USERDATA_FILE);
        if (!f.exists()) return;
        try (FileReader fr = new FileReader(f)) {
            // Try to load the new String-based format first
            try {
                Map<String, Map<String, Map<String, ClassData>>> map =
                        gson.fromJson(fr, new TypeToken<Map<String, Map<String, Map<String, ClassData>>>>(){}.getType());
                if (map != null) {
                    userData = map;
                    // Load semester order if available
                    loadSemesterOrder();
                    return;
                }
            } catch (Exception ignored) {}
            
            // If that fails, try to migrate from old Integer-based format
            try {
                fr.close();
                FileReader fr2 = new FileReader(f);
                Map<String, Map<Integer, Map<String, ClassData>>> oldMap =
                        gson.fromJson(fr2, new TypeToken<Map<String, Map<Integer, Map<String, ClassData>>>>(){}.getType());
                if (oldMap != null) {
                    // Migrate old format to new format
                    for (String user : oldMap.keySet()) {
                        Map<String, Map<String, ClassData>> newUserData = new HashMap<>();
                        Map<String, Integer> userOrderMap = new HashMap<>();
                        
                        for (Integer semNum : oldMap.get(user).keySet()) {
                            String semesterName = "Semester " + semNum;
                            newUserData.put(semesterName, oldMap.get(user).get(semNum));
                            userOrderMap.put(semesterName, semNum);
                        }
                        
                        userData.put(user, newUserData);
                        semesterOrder.put(user, userOrderMap);
                    }
                    
                    // Save the migrated data
                    saveAllUserData();
                }
                fr2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private static void loadSemesterOrder() {
        File f = new File(DATA_DIR + File.separator + "semester_order.json");
        if (!f.exists()) return;
        try (FileReader fr = new FileReader(f)) {
            Map<String, Map<String, Integer>> map =
                    gson.fromJson(fr, new TypeToken<Map<String, Map<String, Integer>>>(){}.getType());
            if (map != null) semesterOrder = map;
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ===== Custom Renderers & Panels =====
    static class ClassRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel name = new JLabel();
        private final JProgressBar bar = new JProgressBar(0, 100);
        private final String semesterName;
        ClassRenderer(String semesterName) {
            super(new BorderLayout(6,6));
            this.semesterName = semesterName;
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
            try {
                ClassData cd = userData.get(currentUser).get(semesterName).get(value);
                
                // Check if this class has a letter grade (past class)
                if (cd.letterGrade != null && !cd.letterGrade.isEmpty()) {
                    name.setText(value + " - " + cd.letterGrade);
                    double p = letterGradeToPercentage(cd.letterGrade);
                    bar.setValue((int)Math.round(p));
                    bar.setString(cd.letterGrade + " (" + String.format("%.1f%%", p) + ")");
                    bar.setForeground(barColorFor(p));
                } else {
                    // Regular class with assignments
                    name.setText(value);
                    double p = calculateClassPercent(cd);
                    bar.setValue((int)Math.round(p));
                    bar.setString(String.format("%.1f%%", p));
                    bar.setForeground(barColorFor(p));
                }
            } catch (Exception ignored) {
                name.setText(value);
                bar.setValue(0);
                bar.setString("No Data");
            }
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
    static class GradeRiseIcon implements Icon {
        private final int w, h;
        GradeRiseIcon(int w, int h) { this.w = w; this.h = h; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Scale factor for responsive sizing
            float scale = Math.min(w, h) / 32.0f;
            
            // Colors from GradeRise branding - red/coral background with dark graduation cap
            Color bgColor = new Color(240, 82, 82); // Coral red background
            Color capColor = new Color(139, 34, 52); // Dark red for graduation cap
            Color tassColor = new Color(220, 150, 50); // Gold for tassel
            
            // Background (optional subtle circle)
            int bgSize = (int)(w * 0.9f);
            int bgX = x + (w - bgSize) / 2;
            int bgY = y + (h - bgSize) / 2;
            
            // Draw subtle circular background using bgColor (previously unused)
            g2.setColor(bgColor);
            g2.fillOval(bgX, bgY, bgSize, bgSize);
            
            // Draw graduation cap
            int capSize = (int)(Math.min(w, h) * 0.7f);
            int capX = x + (w - capSize) / 2;
            int capY = y + (h - capSize) / 2;
            
            // Cap base (mortarboard)
            g2.setColor(capColor);
            int capWidth = (int)(capSize * 0.8f);
            int capHeight = (int)(capSize * 0.3f);
            g2.fillRoundRect(capX + (capSize - capWidth)/2, capY + capHeight/2, capWidth, capHeight/2, 2, 2);
            
            // Cap top (flat square)
            int topSize = (int)(capSize * 0.9f);
            g2.fillRoundRect(capX + (capSize - topSize)/2, capY, topSize, capHeight/2, 3, 3);
            
            // Tassel
            g2.setColor(tassColor);
            g2.setStroke(new BasicStroke(Math.max(1, scale * 2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int tasselX = capX + topSize - 2;
            int tasselY = capY + 2;
            g2.drawLine(tasselX, tasselY, tasselX + (int)(scale * 6), tasselY + (int)(scale * 8));
            g2.drawLine(tasselX + (int)(scale * 6), tasselY + (int)(scale * 8), 
                       tasselX + (int)(scale * 4), tasselY + (int)(scale * 12));
            
            g2.dispose();
        }
        @Override public int getIconWidth() { return w; }
        @Override public int getIconHeight() { return h; }
    }

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

        private static void persist() {
            try (FileWriter fw = new FileWriter(file)) {
                gson.toJson(tokenToUser, fw);
            } catch (IOException ignored) {}
        }
    }

    // ===== SESSION MANAGEMENT =====
    
    /**
     * Save current user login session to file
     */
    private static void saveSession(String username) {
        if (username == null) return;
        try (FileWriter fw = new FileWriter(SESSION_FILE)) {
            Map<String, Object> session = new HashMap<>();
            session.put("username", username);
            session.put("timestamp", System.currentTimeMillis());
            gson.toJson(session, fw);
        } catch (IOException e) {
            System.err.println("Failed to save session: " + e.getMessage());
        }
    }
    
    /**
     * Load saved login session from file
     * Returns username if valid session exists, null otherwise
     */
    private static String loadSession() {
        File f = new File(SESSION_FILE);
        if (!f.exists()) return null;
        
        try (FileReader fr = new FileReader(f)) {
            Map<String, Object> session = gson.fromJson(fr, new TypeToken<Map<String, Object>>(){}.getType());
            if (session == null) return null;
            
            String username = (String) session.get("username");
            Object timestampObj = session.get("timestamp");
            
            if (username == null || timestampObj == null) return null;
            
            // Check if session is not too old (30 days)
            long timestamp = ((Number) timestampObj).longValue();
            long now = System.currentTimeMillis();
            long maxAge = 30L * 24 * 60 * 60 * 1000; // 30 days in milliseconds
            
            if (now - timestamp > maxAge) {
                clearSession(); // Session expired, clear it
                return null;
            }
            
            return username;
        } catch (Exception e) {
            System.err.println("Failed to load session: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Clear saved login session
     */
    private static void clearSession() {
        File f = new File(SESSION_FILE);
        if (f.exists()) {
            f.delete();
        }
    }
}
