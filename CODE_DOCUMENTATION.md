# GPA Tracker Application - Code Documentation

## Overview
This is a comprehensive line-by-line explanation of what each part of the CollegeGPATracker.java application does.

## Import Statements
```java
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
```

## Main Class Declaration
```java
/**
 * CollegeGPATracker - Main application class for tracking college GPA
 * This class manages user authentication, course data, and GPA calculations
 */
public class CollegeGPATracker {
```

## Data Storage Variables
```java
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
// user -> semester(1-4) -> className -> ClassData object
// This allows each user to have 4 semesters, each with multiple classes
private static Map<String, Map<Integer, Map<String, ClassData>>> userData = new HashMap<>();

// Boolean flag to track if dark mode is enabled for the UI
private static boolean darkMode = false;
```

## File Paths and Constants
```java
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

// Gson instance for converting Java objects to/from JSON format
private static final Gson gson = new Gson();
```

## UI Color Scheme
```java
// ===== UI COLOR SCHEME =====

// Left gradient panel colors (teal to purple gradient)
private static final Color LEFT_TOP = new Color(0x14B8A6);      // Teal color for top of gradient
private static final Color LEFT_BOTTOM = new Color(0x6440FF);   // Purple color for bottom of gradient

// Light theme colors
private static final Color RIGHT_BG = new Color(0xF6F7F9);      // Soft off-white background
private static final Color CARD_BG = new Color(0xFAFAFC);       // Card background color

// Dark theme colors for login screen
private static final Color RIGHT_BG_DARK = new Color(0x121418); // Deep charcoal background
private static final Color CARD_BG_DARK = new Color(0x23272B);  // Dark card surface color
private static final Color INPUT_BG_DARK = new Color(0x1E2225); // Dark input field background
private static final Color INPUT_BORDER_DARK = new Color(0x2E3438); // Dark input field border
private static final Color PRIMARY_DARK = new Color(0x2F80ED);  // Primary blue color for buttons

// Label that displays the overall GPA on the dashboard
private static JLabel overallGpaLabel;
```

## Data Structure Classes

### ClassData Class
```java
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
```

### Assignment Class
```java
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
```

## Application Entry Point
```java
/**
 * main - Entry point for the application
 * Sets up data directories, loads saved data, and launches the login UI
 */
public static void main(String[] args) {
    ensureDataDir();                              // Create data directory if it doesn't exist
    loadUsers();                                  // Load user accounts from JSON file
    loadAllUserData();                           // Load all academic data from JSON file
    PasswordResetStore.init(RESET_CODES_FILE);   // Initialize password reset token system
    
    // Launch the GUI on the Event Dispatch Thread (required for Swing)
    SwingUtilities.invokeLater(CollegeGPATracker::showLoginUI);
}
```

## Login UI Creation

### Main Login Function
```java
/**
 * showLoginUI - Creates and displays the login/registration interface
 * Features a two-panel design: gradient left panel with title, dark right panel with login form
 */
private static void showLoginUI() {
    // Create the main window frame
    JFrame frame = new JFrame("Login - GPA Tracker");  // Set window title
    frame.setSize(1000, 560);                          // Set window dimensions (width x height)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Close app when window is closed
    frame.setLayout(new BorderLayout());               // Use BorderLayout for two-panel design
```

### Left Panel Creation
```java
    // ===== LEFT PANEL: Gradient background with app title =====
    // Create custom gradient panel with teal-to-purple gradient
    JPanel leftPanel = new GradientPanel(LEFT_TOP, LEFT_BOTTOM);
    leftPanel.setPreferredSize(new Dimension(420, 0));  // Set width, height will fill

    // Create title label with emoji and styled HTML text
    JLabel msg = new JLabel("<html><center>🎓<br><span style='font-size:20pt'>Your College<br>GPA Tracker</span></center></html>", SwingConstants.CENTER);
    msg.setFont(new Font("SansSerif", Font.BOLD, 28));  // Set large bold font
    msg.setForeground(Color.WHITE);                     // White text on gradient background
    
    // Position title in center of left panel
    leftPanel.setLayout(new BorderLayout());
    leftPanel.add(msg, BorderLayout.CENTER);
```

### Right Panel Creation
```java
    // ===== RIGHT PANEL: Dark themed login form =====
    JPanel rightPanel = new JPanel(new GridBagLayout());  // GridBagLayout for flexible positioning
    rightPanel.setBackground(RIGHT_BG_DARK);              // Dark charcoal background
    GridBagConstraints gbc = new GridBagConstraints();    // Layout constraints object
    gbc.insets = new Insets(10, 10, 10, 10);             // Add 10px padding around components
```

### Login Box Container
```java
    // ===== LOGIN BOX: Rounded card container =====
    JPanel loginBox = new JPanel();                           // Container for all login elements
    loginBox.setLayout(new BoxLayout(loginBox, BoxLayout.Y_AXIS));  // Stack elements vertically
    loginBox.setBackground(CARD_BG_DARK);                     // Dark card background color
    
    // Add compound border: outer line border + inner padding
    loginBox.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(INPUT_BORDER_DARK, 1, true),  // 1px rounded border
        new EmptyBorder(28, 28, 28, 28)                              // 28px padding on all sides
    ));
    loginBox.setMaximumSize(new Dimension(420, 420));         // Limit card size
```

### Input Fields
```java
    // ===== INPUT FIELDS: Username/email and password =====
    // Create placeholder text fields with custom styling
    JTextField usernameOrEmail = new PlaceholderTextField("Username or email");
    JPasswordField password = new PlaceholderPasswordField("Password");
    
    // Center the text within input fields and set text color
    usernameOrEmail.setHorizontalAlignment(SwingConstants.CENTER);  // Center typed text
    usernameOrEmail.setForeground(new Color(40,40,40));             // Dark gray text
    password.setHorizontalAlignment(SwingConstants.CENTER);         // Center typed text  
    password.setForeground(new Color(40,40,40));                    // Dark gray text
    
    // Set consistent size for both input fields so they align properly
    Dimension inputPref = new Dimension(360, 48);  // Width: 360px, Height: 48px
    usernameOrEmail.setPreferredSize(inputPref);
    password.setPreferredSize(inputPref);
```

### Button Creation
```java
    // ===== BUTTONS: Login, signup, forgot password, Google sign-in =====
    
    // Primary login button with blue styling
    JButton loginBtn = pillButton("Login");                    // Create rounded button
    loginBtn.setBackground(PRIMARY_DARK);                      // Blue background
    loginBtn.setForeground(Color.WHITE);                       // White text
    loginBtn.setFont(new Font("SansSerif", Font.BOLD, 16));    // Bold font
    loginBtn.setMaximumSize(new Dimension(360, 46));           // Set button size
    loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);        // Center in container

    // Secondary create account button
    JButton signupBtn = pillButton("Create account");
    signupBtn.setMaximumSize(new Dimension(360, 42));
    signupBtn.setBackground(new Color(0x2A2F33));              // Dark gray background
    signupBtn.setForeground(Color.WHITE);
    signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Forgot password button with red styling
    JButton forgotBtn = pillButton("Forgot password");
    forgotBtn.setMaximumSize(new Dimension(360, 42));
    forgotBtn.setBackground(new Color(0x7A1414));              // Dark red background
    forgotBtn.setForeground(Color.WHITE);
    forgotBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Google sign-in button with custom icon
    JButton googleBtn = pillButton("Sign in with Google", new GoogleIcon(18, 18));
    googleBtn.setBackground(new Color(0x2A2F33));              // Dark surface
    googleBtn.setForeground(Color.WHITE);
    googleBtn.setMaximumSize(new Dimension(360, 42));
    googleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
```

## Key Functions Explained

### Data Persistence
- `ensureDataDir()` - Creates the "data" folder if it doesn't exist
- `loadUsers()` - Reads user accounts from users.json file
- `saveUsers()` - Writes user accounts to users.json file  
- `loadAllUserData()` - Reads academic data from user_data.json
- `saveAllUserData()` - Writes academic data to user_data.json

### GPA Calculations
- `calculateClassPercent()` - Calculates weighted percentage for a class
- `calculateClassGPA()` - Converts class percentage to 4.0 scale GPA
- `calculateOverallGPA()` - Calculates credit-weighted overall GPA
- `percentToGPA()` - Converts percentage to letter grade GPA

### UI Helper Functions
- `pillButton()` - Creates rounded buttons with hover animations
- `makeRow()` - Creates input rows with icons and rounded backgrounds
- `applyTheme()` - Applies dark/light mode colors
- `blend()` - Blends two colors for smooth animations

### Custom UI Components
- `PlaceholderTextField` - Text field that shows placeholder text
- `PlaceholderPasswordField` - Password field with placeholder text
- `GradientPanel` - Panel with vertical color gradient
- `RoundedCard` - Panel with rounded corners and shadow
- `GoogleIcon` - Custom painted Google "G" logo
- `PersonIcon` - Simple person silhouette icon
- `LockIcon` - Simple lock icon

### Password Reset System
- `PasswordResetStore` - Manages email verification codes
- `generateTokenFor()` - Creates 6-digit verification code
- `persistToken()` - Saves code after successful email send
- `consume()` - Uses and removes verification code

This application provides a complete GPA tracking system with user authentication, data persistence, email verification, Google OAuth integration, and a polished user interface with dark theme support.