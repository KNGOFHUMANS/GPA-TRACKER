// Import statements - bringing in external libraries and classes needed for the application

// Swing imports - for creating the graphical user interface (GUI)
import javax.swing.*; // Imports all Swing classes (JFrame, JButton, JLabel, etc.)
import javax.swing.border.EmptyBorder; // For creating empty borders around components
import javax.swing.table.DefaultTableModel; // For managing data in JTable components
import javax.swing.plaf.basic.BasicProgressBarUI; // For customizing progress bar appearance
import javax.swing.JComponent; // Base class for all Swing components
import javax.swing.Box; // For creating invisible spacing components
import javax.swing.UIManager; // For setting look and feel properties

// AWT (Abstract Window Toolkit) imports - for graphics, colors, fonts, and layout
import java.awt.*; // Imports all AWT classes (Color, Font, Graphics, etc.)
import java.awt.event.*; // For handling user interactions (clicks, key presses, etc.)
import java.awt.geom.Arc2D; // For drawing pie chart arcs in the grade breakdown
import java.awt.geom.Ellipse2D; // For drawing circles in the pie chart legend
import java.awt.image.BufferedImage; // For creating custom images from icons

// Java utility imports - for collections and data structures
import java.util.List; // For using List interface (ordered collections)
import java.util.*; // Imports all utility classes (Map, HashMap, ArrayList, etc.)
import java.util.HashMap; // Explicitly import HashMap\nimport java.util.Objects; // For null-safe operations

// Import external Assignment class to avoid name collision
// (Note: This line will import the external Assignment.class file)

// File I/O imports - for reading and writing data to files
import java.io.File; // For working with file system paths
import java.io.FileReader; // For reading text files
import java.io.FileWriter; // For writing text files
import java.io.IOException; // Exception thrown when file operations fail
import java.io.PrintWriter; // For writing formatted text to files

// Google Gson imports - for converting objects to/from JSON format
import com.google.gson.Gson; // Main class for JSON serialization/deserialization
import com.google.gson.reflect.TypeToken; // For handling complex generic types in JSON

// Firebase Admin SDK imports - for cloud data sync
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import java.io.FileInputStream;

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
    
    // ===== FIREBASE CLOUD SYNC VARIABLES =====
    // Firebase database reference for cloud data sync
    private static DatabaseReference firebaseDatabase;
    // Flag to track if Firebase is initialized and ready
    private static boolean firebaseInitialized = false;
    // Current user's email for cloud sync
    private static String currentUserEmail = null;
    
    // Stores the currently logged-in user's username
    private static String currentUser;

    // Complex nested data structure for storing all user academic data:
    // user -> semesterName -> className -> ClassData object
    // This allows each user to have multiple named semesters (e.g., "Fall 2023", "Spring 2024")
    private static Map<String, Map<String, Map<String, ClassData>>> userData = new HashMap<>();
    
    // Maps semester names to display order for consistent tab ordering
    // Maps username -> semesterName -> order number
    private static Map<String, Map<String, Integer>> semesterOrder = new HashMap<>();
    


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
    
    // ===== MODERN TYPOGRAPHY HIERARCHY =====
    private static final Font HEADING_LARGE = new Font("SansSerif", Font.BOLD, 28);
    private static final Font HEADING_MEDIUM = new Font("SansSerif", Font.BOLD, 20);
    private static final Font BODY_LARGE = new Font("SansSerif", Font.PLAIN, 16);
    private static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 14);
    
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



    // Label that displays the overall GPA on the dashboard
    private static JLabel overallGpaLabel;

    // ===== INNER CLASSES FOR DATA STRUCTURE =====
    
    /**
     * ClassData - Stores all information about a single class/course
     * Contains assignments grouped by category, weights for each category,
     * and historical performance data for trend analysis
     */
    static class ClassData {
        // Maps category name to list of assignments in that category
        Map<String, List<Assignment>> assignments = new HashMap<>();
        
        // Maps category name to its weight percentage (should total 100% in weighted mode)
        Map<String, Double> weights = new HashMap<>();
        
        // Historical list of class percentage scores for trend tracking
        List<Double> historyPercent = new ArrayList<>();
        
        // Number of credit hours this class is worth (affects overall GPA calculation)
        int credits = 3;
        
        // Class status: true = active (affects current GPA), false = past (archived)
        boolean isActive = true;
        
        // Final grade for past classes (when assignments aren't tracked individually)
        double finalGrade = -1.0;
        
        // Letter grade for past classes (A, B, C, D, F)
        String letterGrade = "";
        
        // Grading calculation mode
        public enum GradingMode {
            WEIGHTED,     // Category averages multiplied by weights
            TOTAL_POINTS  // Sum of all earned / sum of all possible points
        }
        
        GradingMode gradingMode = GradingMode.WEIGHTED; // Default to weighted mode

        /**
         * Constructor - Creates empty class with no default categories
         * User must add their own categories and weights
         */
        public ClassData() {
            // Start with empty categories - user will add their own
            // No default weights - completely customizable
        }
        
        /**
         * Add a new category with specified weight
         */
        public void addCategory(String categoryName, double weight) {
            if (!assignments.containsKey(categoryName)) {
                assignments.put(categoryName, new ArrayList<>());
                weights.put(categoryName, weight);
            }
        }
        
        /**
         * Remove a category and all its assignments
         */
        public void removeCategory(String categoryName) {
            assignments.remove(categoryName);
            weights.remove(categoryName);
        }
        
        /**
         * Get total weight of all categories
         */
        public double getTotalWeight() {
            return weights.values().stream().mapToDouble(Double::doubleValue).sum();
        }
        
        /**
         * Check if weights are properly configured (sum to 100% in weighted mode)
         */
        public boolean hasValidWeights() {
            if (gradingMode == GradingMode.TOTAL_POINTS) return true;
            double total = getTotalWeight();
            return Math.abs(total - 100.0) < 0.01; // Allow small floating point errors
        }
        
        /**
         * Get category average percentage
         */
        public double getCategoryAverage(String category) {
            List<Assignment> categoryAssignments = assignments.get(category);
            if (categoryAssignments == null || categoryAssignments.isEmpty()) {
                return 0.0;
            }
            
            return categoryAssignments.stream()
                .mapToDouble(Assignment::getPercentage)
                .average().orElse(0.0);
        }
    }
    
    /**
     * Assignment - Represents a single assignment/test/project
     * Uses earned/total points system with automatic percentage calculation
     */
    static class Assignment {
        String name;        // Name of the assignment (e.g., "Homework 1", "Midterm Exam")
        double earnedPoints;  // Points earned (can be > totalPoints for extra credit)
        double totalPoints;   // Total points possible
        String category;    // Which category this belongs to
        
        /**
         * Constructor - Creates assignment with earned/total points
         */
        Assignment(String name, double earnedPoints, double totalPoints, String category) {
            this.name = name;
            this.earnedPoints = earnedPoints;
            this.totalPoints = totalPoints;
            this.category = category;
        }
        
        /**
         * Backward compatibility constructor for percentage-based scores
         * Converts percentage to earned/total format (assuming 100 total points)
         */
        Assignment(String name, double score, String category) {
            this.name = name;
            this.earnedPoints = score;
            this.totalPoints = 100.0;
            this.category = category;
        }
        
        /**
         * Calculate percentage from earned/total points
         * Allows for extra credit (>100%)
         */
        public double getPercentage() {
            if (totalPoints == 0) return 0.0;  // Prevent division by zero
            return (earnedPoints / totalPoints) * 100.0;
        }
        
        /**
         * Check if this assignment has extra credit
         */
        public boolean hasExtraCredit() {
            return earnedPoints > totalPoints;
        }
        
        /**
         * Get display string for points
         */
        public String getPointsDisplay() {
            return String.format("%.1f/%.1f", earnedPoints, totalPoints);
        }
    }
    // ===== APPLICATION ENTRY POINT =====
    
    /**
     * main - Entry point for the application
     * Sets up data directories, loads saved data, and launches the login UI
     */
    public static void main(String[] args) {
        // Create a debug log file for Launch4j troubleshooting
        try {
            File debugFile = new File("graderise-debug.log");
            PrintWriter debugWriter = new PrintWriter(new FileWriter(debugFile, true));
            debugWriter.println("=== GradeRise Startup Debug Log ===");
            debugWriter.println("Timestamp: " + new java.util.Date());
            debugWriter.println("Java Version: " + System.getProperty("java.version"));
            debugWriter.println("Working Directory: " + System.getProperty("user.dir"));
            debugWriter.println("Classpath: " + System.getProperty("java.class.path"));
            debugWriter.flush();
            
            try {
                // Set system look and feel for better EXE compatibility
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    debugWriter.println("✓ System Look and Feel set successfully");
                } catch (Exception e) {
                    // Fallback to default if system L&F fails
                    System.err.println("Could not set system look and feel: " + e.getMessage());
                    debugWriter.println("✗ System Look and Feel failed: " + e.getMessage());
                }
                
                // Force dark text colors for all UI components to ensure readability
                // JOptionPane popup dialog buttons
                UIManager.put("OptionPane.buttonForeground", Color.BLACK);
                UIManager.put("OptionPane.okButtonText", "OK");
                UIManager.put("OptionPane.cancelButtonText", "Cancel");
                UIManager.put("OptionPane.yesButtonText", "Yes");
                UIManager.put("OptionPane.noButtonText", "No");
                
                // Initialize Firebase for cloud sync
                initializeFirebase(debugWriter);
                
                // All button types
                UIManager.put("Button.foreground", Color.BLACK);
                UIManager.put("Button.focus", Color.BLACK);
                UIManager.put("Button.select", Color.BLACK);
                UIManager.put("ToggleButton.foreground", Color.BLACK);
                UIManager.put("ToggleButton.focus", Color.BLACK);
                
                // JOptionPane content
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.foreground", Color.BLACK);
                
                // Other UI components
                UIManager.put("Label.foreground", Color.BLACK);
                UIManager.put("TextField.foreground", Color.BLACK);
                UIManager.put("ComboBox.foreground", Color.BLACK);
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("Tree.textForeground", Color.BLACK);
                UIManager.put("Table.foreground", Color.BLACK);
                
                // Panel and background settings for better contrast
                UIManager.put("Panel.background", Color.WHITE);
                UIManager.put("OptionPane.background", Color.WHITE);
                UIManager.put("Button.background", Color.LIGHT_GRAY);
                
                debugWriter.println("✓ Dark text colors set for all UI components including JOptionPane dialogs");
                
                // Set system properties for better EXE behavior
                System.setProperty("java.awt.headless", "false");
                System.setProperty("file.encoding", "UTF-8");
                debugWriter.println("✓ System properties set");
                
                // Test Gson library availability for EXE compatibility
                try {
                    gson.toJson("test");
                    debugWriter.println("✓ Gson library test successful");
                } catch (Exception e) {
                    debugWriter.println("✗ Gson library test failed: " + e.getMessage());
                    throw new RuntimeException("Gson library not available in EXE environment", e);
                }
            
                ensureDataDir();                              // Create data directory if it doesn't exist
                debugWriter.println("✓ Data directory ensured");
                
                // Initialize data system
                debugWriter.println("✓ Using JSON file system for data storage");
                
                loadUsers();                                  // Load user accounts from database/JSON file
                debugWriter.println("✓ Users loaded");
                loadAllUserData();                           // Load all academic data from database/JSON file
                debugWriter.println("✓ User data loaded");
                PasswordResetStore.init(RESET_CODES_FILE);   // Initialize password reset token system
                debugWriter.println("✓ Password reset store initialized");
                
                // Set UIManager properties for better dialog styling
                try {
                    UIManager.put("OptionPane.messageforeground", Color.BLACK);
                    UIManager.put("OptionPane.messageForeground", Color.BLACK);
                    UIManager.put("Button.foreground", Color.BLACK);
                    UIManager.put("Button.background", Color.WHITE);
                    UIManager.put("Panel.background", Color.WHITE);
                    UIManager.put("OptionPane.background", Color.WHITE);
                    debugWriter.println("✓ UIManager properties set for dialogs");
                } catch (Exception e) {
                    debugWriter.println("✗ UIManager setup failed: " + e.getMessage());
                }
                
                // Initialize modern theme system
                try {
                    ModernThemeSystem.initialize();
                    debugWriter.println("✓ Modern theme system initialized");
                } catch (Exception e) {
                    debugWriter.println("✗ Modern theme system failed: " + e.getMessage());
                }
                
                // Check for existing login session
                String savedUser = loadSession();
                if (savedUser != null && users.containsKey(savedUser)) {
                    currentUser = savedUser;
                    ensureUserStructures(currentUser);
                    debugWriter.println("✓ Launching dashboard for saved user: " + savedUser);
                    // Launch directly to dashboard
                    SwingUtilities.invokeLater(CollegeGPATracker::showDashboard);
                } else {
                    debugWriter.println("✓ Launching login UI");
                    // Launch the login UI
                    SwingUtilities.invokeLater(CollegeGPATracker::showLoginUI);
                }
                
                debugWriter.println("✓ Application startup completed successfully");
                
            } catch (Exception e) {
                debugWriter.println("✗ Application startup failed: " + e.getMessage());
                debugWriter.println("Stack trace:");
                e.printStackTrace(debugWriter);
                debugWriter.close();
                
                // Show error dialog for EXE deployment issues
                System.err.println("Me No Work try Later: " + e.getMessage());
                e.printStackTrace();
                
                // Try to show a user-friendly error dialog
                try {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            null,
                            "GradeRise encountered an error during startup.\n\n" +
                            "Error: " + e.getMessage() + "\n\n" +
                            "Check graderise-debug.log for detailed error information.\n" +
                            "Please ensure all files are in the same directory as the application.",
                            "GradeRise - Startup Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                        System.exit(1);
                    });
                } catch (Exception dialogError) {
                    System.exit(1);
                }
            }
            
            debugWriter.close();
        } catch (Exception logError) {
            // Even logging failed, show basic error
            System.err.println("Critical error - could not even create debug log: " + logError.getMessage());
            try {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        null,
                        "GradeRise encountered a critical startup error.\n\n" +
                        "Could not create debug log: " + logError.getMessage() + "\n\n" +
                        "Please check file permissions and try running as administrator.",
                        "GradeRise - Critical Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    System.exit(1);
                });
            } catch (Exception finalError) {
                System.exit(1);
            }
        }
    }

    // ===== FIREBASE CLOUD SYNC METHODS =====
    
    /**
     * Initialize Firebase for cloud data synchronization
     */
    private static void initializeFirebase(PrintWriter debugWriter) {
        try {
            debugWriter.println("DEBUG: Initializing Firebase cloud sync...");
            
            // Check if Firebase service account key exists
            File serviceAccountKey = new File("graderisecloud-firebase-adminsdk-fbsvc-fb2cff8c34.json");
            if (!serviceAccountKey.exists()) {
                debugWriter.println("DEBUG: Firebase service account key not found - cloud sync disabled");
                firebaseInitialized = false;
                return;
            }
            
            // Initialize Firebase App if not already done
            if (FirebaseApp.getApps().isEmpty()) {
                try {
                    FileInputStream serviceAccount = new FileInputStream(serviceAccountKey);
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setDatabaseUrl("https://graderisecloud-default-rtdb.firebaseio.com/")
                        .build();
                    
                    FirebaseApp.initializeApp(options);
                    debugWriter.println("DEBUG: Firebase App initialized successfully");
                    serviceAccount.close();
                } catch (Exception initEx) {
                    debugWriter.println("DEBUG: Firebase initialization with service account failed, trying default: " + initEx.getMessage());
                    
                    // Fallback to default initialization
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setDatabaseUrl("https://graderisecloud-default-rtdb.firebaseio.com/")
                        .build();
                    
                    FirebaseApp.initializeApp(options);
                    debugWriter.println("DEBUG: Firebase App initialized with default options");
                }
            }
            
            // Get database reference
            firebaseDatabase = FirebaseDatabase.getInstance().getReference();
            firebaseInitialized = true;
            
            debugWriter.println("DEBUG: Firebase cloud sync initialized successfully");
            
        } catch (Exception e) {
            debugWriter.println("DEBUG: Firebase initialization failed: " + e.getMessage());
            e.printStackTrace(debugWriter);
            firebaseInitialized = false;
        }
    }
    
    /**
     * Sync user data to Firebase cloud
     */
    private static void syncToCloud(String userEmail) {
        if (!firebaseInitialized || userEmail == null || currentUser == null) {
            System.out.println("DEBUG: Cloud sync skipped - Firebase not initialized or missing data");
            return;
        }
        
        try {
            // Create user data object for cloud sync using existing userData structure
            Map<String, Object> cloudUserData = new HashMap<>();
            cloudUserData.put("username", currentUser);
            cloudUserData.put("email", userEmail);
            cloudUserData.put("lastSync", System.currentTimeMillis());
            
            // Get user's semester data from existing userData structure
            Map<String, Map<String, ClassData>> userSemesters = userData.get(currentUser);
            if (userSemesters != null) {
                Map<String, Object> semesterData = new HashMap<>();
                
                for (Map.Entry<String, Map<String, ClassData>> semesterEntry : userSemesters.entrySet()) {
                    String semesterName = semesterEntry.getKey();
                    Map<String, ClassData> courses = semesterEntry.getValue();
                    
                    Map<String, Object> coursesData = new HashMap<>();
                    for (Map.Entry<String, ClassData> courseEntry : courses.entrySet()) {
                        String courseName = courseEntry.getKey();
                        ClassData classData = courseEntry.getValue();
                        
                        Map<String, Object> courseData = new HashMap<>();
                        courseData.put("credits", classData.credits);
                        courseData.put("finalGrade", classData.finalGrade);
                        courseData.put("letterGrade", classData.letterGrade);
                        courseData.put("assignments", classData.assignments);
                        courseData.put("weights", classData.weights);
                        courseData.put("isActive", classData.isActive);
                        courseData.put("historyPercent", classData.historyPercent);
                        
                        coursesData.put(courseName, courseData);
                    }
                    
                    semesterData.put(semesterName, coursesData);
                }
                
                cloudUserData.put("semesters", semesterData);
            }
            
            // Sync to Firebase using email as key (sanitized for Firebase key format)
            String firebaseKey = userEmail.replace(".", "_").replace("@", "_at_");
            firebaseDatabase.child("users").child(firebaseKey).setValue(cloudUserData, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if (error != null) {
                        System.err.println("DEBUG: Firebase sync failed: " + error.getMessage());
                    } else {
                        System.out.println("DEBUG: Successfully synced data to cloud for user: " + userEmail);
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("DEBUG: Failed to sync data to cloud: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load user data from Firebase cloud
     */
    private static void loadFromCloud(String userEmail, Runnable onComplete) {
        if (!firebaseInitialized || userEmail == null) {
            System.out.println("DEBUG: Cloud load skipped - Firebase not initialized");
            if (onComplete != null) onComplete.run();
            return;
        }
        
        try {
            String firebaseKey = userEmail.replace(".", "_").replace("@", "_at_");
            
            firebaseDatabase.child("users").child(firebaseKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            System.out.println("DEBUG: Loading cloud data for user: " + userEmail);
                            
                            // Load semester data
                            DataSnapshot semesterSnapshot = dataSnapshot.child("semesters");
                            Map<String, Map<String, ClassData>> newUserData = new HashMap<>();
                            
                            for (DataSnapshot semesterData : semesterSnapshot.getChildren()) {
                                String semesterName = semesterData.getKey();
                                Map<String, ClassData> semesterCourses = new HashMap<>();
                                
                                // Load courses for this semester
                                for (DataSnapshot courseData : semesterData.getChildren()) {
                                    String courseName = courseData.getKey();
                                    
                                    // Create ClassData object from cloud data
                                    ClassData classData = new ClassData();
                                    
                                    if (courseData.hasChild("credits")) {
                                        classData.credits = courseData.child("credits").getValue(Integer.class);
                                    }
                                    if (courseData.hasChild("finalGrade")) {
                                        classData.finalGrade = courseData.child("finalGrade").getValue(Double.class);
                                    }
                                    if (courseData.hasChild("letterGrade")) {
                                        classData.letterGrade = courseData.child("letterGrade").getValue(String.class);
                                    }
                                    if (courseData.hasChild("isActive")) {
                                        classData.isActive = courseData.child("isActive").getValue(Boolean.class);
                                    }
                                    
                                    // Load assignments
                                    if (courseData.hasChild("assignments")) {
                                        DataSnapshot assignmentsData = courseData.child("assignments");
                                        Map<String, List<Assignment>> assignments = new HashMap<>();
                                        
                                        for (DataSnapshot categoryData : assignmentsData.getChildren()) {
                                            String categoryName = categoryData.getKey();
                                            List<Assignment> categoryAssignments = new ArrayList<>();
                                            
                                            for (DataSnapshot assignmentData : categoryData.getChildren()) {
                                                // Reconstruct Assignment objects from cloud data
                                                if (assignmentData.hasChild("name") && assignmentData.hasChild("earnedPoints") && assignmentData.hasChild("totalPoints")) {
                                                    String name = assignmentData.child("name").getValue(String.class);
                                                    Double earnedPoints = assignmentData.child("earnedPoints").getValue(Double.class);
                                                    Double totalPoints = assignmentData.child("totalPoints").getValue(Double.class);
                                                    Assignment assignment = new Assignment(name, earnedPoints, totalPoints, categoryName);
                                                    categoryAssignments.add(assignment);
                                                }
                                            }
                                            
                                            if (!categoryAssignments.isEmpty()) {
                                                assignments.put(categoryName, categoryAssignments);
                                            }
                                        }
                                        
                                        classData.assignments = assignments;
                                    }
                                    
                                    // Load weights
                                    if (courseData.hasChild("weights")) {
                                        DataSnapshot weightsData = courseData.child("weights");
                                        Map<String, Double> weights = new HashMap<>();
                                        for (DataSnapshot weightData : weightsData.getChildren()) {
                                            String category = weightData.getKey();
                                            Double weight = weightData.getValue(Double.class);
                                            weights.put(category, weight);
                                        }
                                        classData.weights = weights;
                                    }
                                    
                                    // Load grade history
                                    if (courseData.hasChild("historyPercent")) {
                                        DataSnapshot historyData = courseData.child("historyPercent");
                                        List<Double> history = new ArrayList<>();
                                        for (DataSnapshot historyItem : historyData.getChildren()) {
                                            Double grade = historyItem.getValue(Double.class);
                                            if (grade != null) {
                                                history.add(grade);
                                            }
                                        }
                                        classData.historyPercent = history;
                                    }
                                    
                                    semesterCourses.put(courseName, classData);
                                }
                                
                                newUserData.put(semesterName, semesterCourses);
                            }
                            
                            // Update local userData with cloud data
                            if (!newUserData.isEmpty()) {
                                userData.put(currentUser, newUserData);
                                
                                // Save loaded cloud data to local files
                                saveAllUserData();
                                
                                System.out.println("DEBUG: Successfully loaded data from cloud");
                            }
                        } else {
                            System.out.println("DEBUG: No cloud data found for user: " + userEmail);
                        }
                    } catch (Exception e) {
                        System.err.println("DEBUG: Error processing cloud data: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    if (onComplete != null) onComplete.run();
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("DEBUG: Failed to load cloud data: " + databaseError.getMessage());
                    if (onComplete != null) onComplete.run();
                }
            });
            
        } catch (Exception e) {
            System.err.println("DEBUG: Failed to load from cloud: " + e.getMessage());
            e.printStackTrace();
            if (onComplete != null) onComplete.run();
        }
    }
    


    // ===== LOGIN PAGE =====
    private static void showLoginUI() {
        // Modern login UI with gradient background and card-style form using ModernUIFramework
        JFrame frame = new JFrame("GradeRise - Login");
        frame.setSize(1000, 600);
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

        // Right panel with modern UI framework styling
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(ModernUIFramework.getTheme().background);

        // Modern login card - using JPanel temporarily to avoid layout conflicts
        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setPreferredSize(new Dimension(400, 500));
        loginCard.setMaximumSize(new Dimension(400, 500));
        loginCard.setBackground(ModernUIFramework.getTheme().surface);
        loginCard.setBorder(new EmptyBorder(32, 32, 32, 32));

        // GradeRise brand header
        JLabel brandLabel = new JLabel("GradeRise", SwingConstants.CENTER);
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        brandLabel.setForeground(ModernUIFramework.getTheme().primary);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel welcomeLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        
        JLabel subtitleLabel = new JLabel("Sign in to your account", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(ModernUIFramework.getTheme().onSurface);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));

        // Modern input fields with clean placeholder styling
        PlaceholderTextField usernameOrEmail = new PlaceholderTextField("Email");
        usernameOrEmail.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameOrEmail.setMaximumSize(new Dimension(370, 56));
        usernameOrEmail.setPreferredSize(new Dimension(370, 56));
        usernameOrEmail.setFont(BODY_LARGE);
        usernameOrEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        usernameOrEmail.setBackground(Color.WHITE);
        usernameOrEmail.setForeground(new Color(50, 50, 50));
        
        PlaceholderPasswordField password = new PlaceholderPasswordField("Password");
        password.setAlignmentX(Component.CENTER_ALIGNMENT);
        password.setMaximumSize(new Dimension(370, 56));
        password.setPreferredSize(new Dimension(370, 56));
        password.setFont(new Font("SansSerif", Font.PLAIN, 15));
        password.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        password.setBackground(Color.WHITE);
        password.setForeground(new Color(50, 50, 50));
        password.setEchoChar('•');

        // Modern UI components are created above - old row creation code removed

        // Modern buttons with clean styling to match target design
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(370, 52));
        loginBtn.setPreferredSize(new Dimension(370, 52));
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginBtn.setBackground(new Color(193, 80, 122)); // Rose/pink primary from Lavender theme
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JButton signupBtn = new JButton("Create Account");
        signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupBtn.setMaximumSize(new Dimension(370, 52));
        signupBtn.setPreferredSize(new Dimension(370, 52));
        signupBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        signupBtn.setBackground(Color.WHITE);
        signupBtn.setForeground(new Color(193, 80, 122));
        signupBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(193, 80, 122), 2, true),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        signupBtn.setFocusPainted(false);
        signupBtn.setContentAreaFilled(false);
        signupBtn.setOpaque(true);
        signupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Google Sign-In button with clean styling
        JButton googleBtn = new JButton("Sign in with Google");
        googleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        googleBtn.setMaximumSize(new Dimension(370, 52));
        googleBtn.setPreferredSize(new Dimension(370, 52));
        googleBtn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        googleBtn.setBackground(Color.WHITE);
        googleBtn.setForeground(new Color(50, 50, 50));
        googleBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        googleBtn.setFocusPainted(false);
        googleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        googleBtn.setIcon(new GoogleIcon(20, 20));
        googleBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        googleBtn.setIconTextGap(8);

        // Assemble the login card with clean spacing matching target design
        loginCard.add(Box.createVerticalStrut(20));
        
        // Add header labels
        loginCard.add(welcomeLabel);  
        loginCard.add(subtitleLabel);
    
        // Add input fields
        loginCard.add(usernameOrEmail);
        loginCard.add(Box.createVerticalStrut(16));
        loginCard.add(password);
        
        // Forgot password link
        JLabel forgotPasswordLink = new JLabel("<html><u>Forgot password?</u></html>", SwingConstants.CENTER);
        forgotPasswordLink.setFont(new Font("SansSerif", Font.PLAIN, 14));
        forgotPasswordLink.setForeground(new Color(193, 80, 122)); // Rose color to match primary
        forgotPasswordLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotPasswordLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLink.setBorder(new EmptyBorder(12, 0, 0, 0));
        
        loginCard.add(forgotPasswordLink);
        loginCard.add(Box.createVerticalStrut(24));
        
        // Add primary login button
        loginCard.add(loginBtn);
        loginCard.add(Box.createVerticalStrut(16));
        
        // Add secondary buttons
        loginCard.add(signupBtn);
        loginCard.add(Box.createVerticalStrut(16));
        
        // OR section
        JLabel orLabel = new JLabel("OR", SwingConstants.CENTER);
        orLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        orLabel.setForeground(new Color(120, 120, 120));
        orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCard.add(orLabel);
        loginCard.add(Box.createVerticalStrut(16));
        
        // Google Sign-In section
        loginCard.add(googleBtn);
        loginCard.add(Box.createVerticalStrut(20));

        // Wrapper panel for centering the login card
        JPanel loginWrapper = new JPanel(new GridBagLayout());
        loginWrapper.setBackground(ModernUIFramework.getTheme().background);
        GridBagConstraints loginGbc = new GridBagConstraints();
        loginGbc.gridx = 0;
        loginGbc.gridy = 0;
        loginGbc.weightx = 1.0;
        loginGbc.weighty = 1.0;
        loginGbc.anchor = GridBagConstraints.CENTER;
        loginWrapper.add(loginCard, loginGbc);
    
        JPanel wrapper = loginWrapper;
        wrapper.setBackground(ModernUIFramework.getTheme().background);
        
        // Add wrapper to rightPanel with proper GridBagConstraints
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightGbc.weightx = 1.0;
        rightGbc.weighty = 1.0;
        rightGbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(wrapper, rightGbc);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);

        // LOGIN - Enhanced with security features
        loginBtn.addActionListener(_ -> {
            String id = usernameOrEmail.getText().trim();
            String pass = new String(password.getPassword());
            
            // Get client identifier for rate limiting (could use IP in production)
            String clientIdentifier = System.getProperty("user.name") + "_" + 
                                    System.getProperty("os.name").replaceAll("\\s", "");
            
            try {
                // Use JSON-based authentication
                String authenticatedUser = authenticateUserJSON(id, pass);
                
                if (authenticatedUser != null) {
                    currentUser = authenticatedUser;
                    ensureUserStructures(currentUser);
                    
                    // Create secure session
                    String sessionToken = SecurityManager.createSession(currentUser);
                    if (sessionToken != null) {
                        saveSession(currentUser);  // Save login session for compatibility
                    }
                    
                    SecurityManager.logSecurityEvent("Login successful", currentUser, true);
                    frame.dispose();
                    showDashboard();
                } else {
                    // Check if user is rate limited
                    if (SecurityManager.isRateLimited(clientIdentifier)) {
                        long remaining = SecurityManager.getRemainingLockoutSeconds(clientIdentifier);
                        JOptionPane.showMessageDialog(frame, 
                            "Too many failed login attempts.\n" +
                            "Please try again in " + remaining + " seconds.",
                            "Account Temporarily Locked",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        showCustomMessageDialog(frame, "Invalid credentials.", "Login Failed");
                    }
                }
            } catch (Exception e) {
                System.err.println("Login error: " + e.getMessage());
                SecurityManager.logSecurityEvent("Login error", id, false);
                showCustomMessageDialog(frame, "Login failed due to system error.", "System Error");
            }
        });

        // SIGNUP - Enhanced with security validation
        signupBtn.addActionListener(_ -> {
            try {
                String newUser = showCustomInputDialog(frame, "Choose a username:", "Create Account", "");
                if (newUser == null || newUser.trim().isEmpty()) return;
                
                // Validate username format
                String validatedUsername = SecurityManager.validateUsername(newUser);
                
                // Check if user exists in database first
                if (users.containsKey(validatedUsername)) {
                    JOptionPane.showMessageDialog(frame, "Username already exists!");
                    return;
                }
                
                String email = showCustomInputDialog(frame, "Enter email:", "Email Address", "");
                if (email == null || email.trim().isEmpty()) return;
                
                // Validate email format
                String validatedEmail = SecurityManager.validateEmail(email);
                
                // Check if email already exists
                if (emailExistsJSON(validatedEmail)) {
                    JOptionPane.showMessageDialog(frame, "Email already used.");
                    return;
                }
                
                JPasswordField p1 = new JPasswordField();
                JPasswordField p2 = new JPasswordField();
                int ok = JOptionPane.showConfirmDialog(frame, 
                    new Object[]{"Password:", p1,"Confirm Password:", p2}, 
                    "Create password", JOptionPane.OK_CANCEL_OPTION);
                if (ok != JOptionPane.OK_OPTION) return;
                
                String newPass = new String(p1.getPassword());
                String confirmPass = new String(p2.getPassword());
                if (newPass.isEmpty() || !newPass.equals(confirmPass)) {
                    JOptionPane.showMessageDialog(frame, "Passwords don't match.");
                    return;
                }
                
                // Validate password strength
                SecurityManager.validatePassword(newPass);
                
                // Create user securely with bcrypt hashing
                if (createUserJSON(validatedUsername, newPass, validatedEmail)) {
                    // Also add to legacy users map for compatibility
                    String hashedPassword = SecurityManager.hashPassword(newPass);
                    users.put(validatedUsername, new String[]{hashedPassword, validatedEmail});
                    lastUsernameChange.put(validatedUsername, System.currentTimeMillis());
                    ensureUserStructures(validatedUsername);
                    saveUsers();
                    saveAllUserData();
                    
                    String strength = SecurityManager.getPasswordStrength(newPass);
                    JOptionPane.showMessageDialog(frame, 
                        "Account created successfully!\n" +
                        "Password strength: " + strength + "\n" +
                        "Please log in with your new credentials.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to create account. Please try again.");
                }
                
            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(frame, 
                    "Account creation failed:\n" + e.getMessage(), 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                System.err.println("Signup error: " + e.getMessage());
                JOptionPane.showMessageDialog(frame, "Account creation failed due to system error.");
            }
        });

        // FORGOT PASSWORD: prompt for email, generate a transient token, attempt to send it by email.
        // Only persist the token after the email has successfully been sent. Never show the code in the UI.
        forgotPasswordLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String email = showCustomInputDialog(frame, "Enter your account email:", "Password Reset", "");
                if (email == null || email.trim().isEmpty()) return;
                String user = findUserByEmail(email);
                if (user == null) {
                    JOptionPane.showMessageDialog(frame, "No account with that email.");
                    return;
                }

                // generate a token but don't persist it until we confirm email delivery
                String code = PasswordResetStore.generateTokenFor(user);
                String subject = "GradeRise — Password reset code";
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
                    String provided = showCustomInputDialog(frame, "Enter the 6-digit reset code from your email:", "Reset Code", "");
                    if (provided == null || provided.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "No code entered. You can enter the code later.");
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
                           currentUserEmail = email;  // Store email for cloud sync

                           ensureUserStructures(currentUser);
                           saveUsers();
                           
                           // Load cloud data before showing dashboard
                           if (!isNew && firebaseInitialized) {
                               // For existing users, load their cloud data first
                               loadFromCloud(email, () -> {
                                   SwingUtilities.invokeLater(() -> {
                                       saveAllUserData();
                                       saveSession(currentUser);
                                       frame.dispose();
                                       showDashboard();
                                   });
                               });
                           } else {
                               // For new users or when Firebase unavailable, proceed normally
                               saveAllUserData();
                               saveSession(currentUser);
                               
                               // Sync new user data to cloud if Firebase available
                               if (firebaseInitialized) {
                                   syncToCloud(email);
                               }
                               
                               frame.dispose();
                               showDashboard();
                           }
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

        // Apply modern styling to the login frame
        ModernThemeSystem.applyModernStyling(frame);
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ===== DASHBOARD =====
    private static void showDashboard() {
        JFrame frame = new JFrame("College GPA Dashboard — " + currentUser);
        frame.setSize(1150, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        // Apply theme background
        try {
            frame.getContentPane().setBackground(ModernThemeSystem.getCurrentColors().background);
        } catch (Exception e) {
            frame.getContentPane().setBackground(Color.darkGray);
        }
        
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
        JMenuItem themeSettings = new JMenuItem("🎨 Theme Settings");
        
        viewMenu.add(themeSettings);

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

        // Modern theme menu action listeners
        themeSettings.addActionListener(_ -> ModernThemeSystem.showThemeDialog(frame));

        // Apply modern styling to the entire frame
        ModernThemeSystem.applyModernStyling(frame);
        
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
                
                // Create gradient background using current theme primary
                Color primary = ModernThemeSystem.getCurrentColors().primary;
                Color start = darken(primary, 0.25f);
                GradientPaint gradient = new GradientPaint(
                    0, 0, start,
                    getWidth(), 0, primary
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
        titleLabel.setFont(HEADING_MEDIUM);
        titleLabel.setForeground(Color.WHITE);
        
        leftPanel.add(logoLabel);
        leftPanel.add(titleLabel);
        
        // Center - GPA Display with Achievement Badge
        double gpa = calculateOverallGPA(username);
        String achievementText = gpa >= 3.7 ? "⭐ High Achiever" : 
                                gpa >= 3.0 ? "📈 On Track" : 
                                "💪 Keep Going";
        
        overallGpaLabel = new JLabel("Overall GPA: " + String.format("%.2f", gpa) + " " + achievementText);
        overallGpaLabel.setFont(HEADING_LARGE);
        overallGpaLabel.setForeground(Color.WHITE);
        overallGpaLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add text shadow effect
        overallGpaLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Right side - Cloud Sync Status
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setOpaque(false);
        
        JLabel cloudStatusLabel = new JLabel();
        cloudStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        if (firebaseInitialized && currentUserEmail != null) {
            cloudStatusLabel.setText("☁️ Cloud Sync: " + currentUserEmail);
            cloudStatusLabel.setForeground(new Color(144, 238, 144)); // Light green
            cloudStatusLabel.setToolTipText("Your data is automatically synced to the cloud");
        } else {
            cloudStatusLabel.setText("🔒 Local Only");
            cloudStatusLabel.setForeground(new Color(255, 215, 0)); // Gold
            cloudStatusLabel.setToolTipText("Data stored locally. Sign in with Google for cloud sync.");
        }
        
        rightPanel.add(cloudStatusLabel);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(overallGpaLabel, BorderLayout.CENTER);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
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
                        // Gradient underline for active tab using current theme primary
                        Color primary = ModernThemeSystem.getCurrentColors().primary;
                        Color start = darken(primary, 0.25f);
                        GradientPaint gradient = new GradientPaint(
                            tabBounds.x, tabBounds.y + tabBounds.height - 3,
                            start,
                            tabBounds.x + tabBounds.width, tabBounds.y + tabBounds.height - 3,
                            primary
                        );
                        g2d.setPaint(gradient);
                        g2d.fillRoundRect(tabBounds.x + 10, tabBounds.y + tabBounds.height - 3, 
                                        tabBounds.width - 20, 3, 3, 3);
                    }
                }
            }
        };
        
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        try {
            tabbedPane.setBackground(ModernThemeSystem.getCurrentColors().background);
        } catch (Exception e) {
            tabbedPane.setBackground(RIGHT_BG);
        }
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
                
                // Multi-layer shadow for enhanced depth
                for (int i = 0; i < 5; i++) {
                    int alpha = Math.max(5, 25 - (i * 4));
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(i + 1, i + 3, getWidth() - (i * 2) - 2, 
                                    getHeight() - (i * 2) - 3, 16 - i, 16 - i);
                }
                
                // Card background with subtle gradient
                GradientPaint cardGradient = new GradientPaint(
                    0, 0, CARD_LIGHT,
                    0, getHeight(), new Color(0xFEFEFE)
                );
                g2d.setPaint(cardGradient);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 4, 16, 16);
                
                // Subtle inner highlight
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.drawRoundRect(1, 1, getWidth() - 4, getHeight() - 6, 15, 15);
                
                g2d.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Enhanced title with better typography
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BODY_BOLD);
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        
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
            
            JLabel nameLabel = new JLabel("Semester Name:");
            nameLabel.setForeground(Color.BLACK);
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            
            JLabel suggestLabel = new JLabel("Or choose from suggestions:");
            suggestLabel.setForeground(Color.BLACK);
            suggestLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            
            nameField.setForeground(Color.BLACK);
            nameField.setBackground(Color.WHITE);
            nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            suggestionBox.setForeground(Color.BLACK);
            suggestionBox.setBackground(Color.WHITE);
            suggestionBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            inputPanel.setBackground(Color.WHITE);
            inputPanel.add(nameLabel);
            inputPanel.add(nameField);
            inputPanel.add(suggestLabel);
            inputPanel.add(suggestionBox);
            
            boolean result = showCustomComplexDialog(parentFrame, inputPanel, "Add New Semester");
            
            if (!result) return;
            
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

        JButton addClassBtn = createFloatingButton("➕ Add Class", SUCCESS_EMERALD);
        JButton deleteClassBtn = createFloatingButton("🗑️ Delete Class", DANGER_ROSE);
        JButton toggleStatusBtn = createFloatingButton("📚 Toggle Status", INFO_BRAND);
        JButton removeSemesterBtn = createFloatingButton("⚠️ Remove Semester", new Color(0xDC2626));
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

        JButton addAssignmentBtn = createFloatingButton("➕ Add Assignment", SUCCESS_EMERALD);
        JButton deleteAssignmentBtn = createFloatingButton("🗑️ Delete Assignment", DANGER_ROSE);
        JButton weightsBtn = createFloatingButton("⚖️ Weights", INFO_BRAND);
        JButton creditsBtn = createFloatingButton("💎 Credits", WARNING_AMBER);
        JButton letterGradeBtn = createFloatingButton("🎓 Letter Grade", new Color(0x9333EA));
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

        // Category breakdown panel
        JPanel categoryBreakdownPanel = new JPanel();
        categoryBreakdownPanel.setLayout(new BoxLayout(categoryBreakdownPanel, BoxLayout.Y_AXIS));
        categoryBreakdownPanel.setBackground(Color.WHITE);
        categoryBreakdownPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            "Category Breakdown", 
            0, 0, 
            new Font("SansSerif", Font.BOLD, 12), 
            Color.BLACK));
        
        JLabel gradingModeLabel = new JLabel("Mode: —");
        gradingModeLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gradingModeLabel.setForeground(Color.GRAY);
        categoryBreakdownPanel.add(gradingModeLabel);

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
        JPanel categoriesCard = createAnalyticsCard("📋 Categories", categoryBreakdownPanel);
        JPanel trendCard = createAnalyticsCard("📈 Performance Trend", trendPanel);
        JPanel badgesCard = createAnalyticsCard("🏆 Achievements", badgePanel);

        weightsCard.setMaximumSize(new Dimension(340, 260));
        categoriesCard.setMaximumSize(new Dimension(340, 180));
        trendCard.setMaximumSize(new Dimension(340, 200));
        badgesCard.setMaximumSize(new Dimension(340, 140));

        rightDash.add(weightsCard);
        rightDash.add(Box.createVerticalStrut(12));
        rightDash.add(categoriesCard);
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
            // Use custom dialog with guaranteed dark text buttons
            String className = showCustomInputDialog(root, "Enter class name:", "Add New Class", "");
            if (className == null || className.trim().isEmpty()) return;
            if (userData.get(currentUser).get(semesterName).containsKey(className)) {
                showCustomConfirmDialog(root, "Class already exists.", "Error");
                return;
            }
            int credits = 3;
            String creditsStr = showCustomInputDialog(root, "Credit hours (e.g., 3):", "Set Credits", "3");
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
            
            boolean confirm = showCustomConfirmDialog(root, "Delete class '" + selectedClass + "'?\nThis will remove all assignments.", "Confirm Delete");
            if (!confirm) return;
            
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
                showCustomMessageDialog(root, "Please select a class to toggle its status.", "No Class Selected");
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
            
            boolean confirm = showCustomConfirmDialog(root, 
                "Are you sure you want to delete " + semesterName + "?\n" +
                "This will permanently remove all classes and assignments in this semester.", 
                "Delete Semester");
            
            if (confirm) {
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
                    // Display as "earned/total (percentage%)" format
                    String displayScore = String.format("%.1f/%.1f (%.1f%%)", 
                        a.earnedPoints, a.totalPoints, a.getPercentage());
                    model.addRow(new Object[]{a.name, a.category, displayScore});
                }
            }
            double classPercent = calculateClassPercent(cd);
            double classGPA = percentToGPA(classPercent);
            classGpaLabel.setText("Class GPA: " + String.format("%.2f", classGPA));

            // Update category breakdown display
            updateCategoryBreakdown(cd, categoryBreakdownPanel, gradingModeLabel);

            // update charts
            updatePieChartWithAllCategories(cd, piePanel);
            trendPanel.setData(cd.historyPercent);
            badgePanel.setBadges(classGPA >= 3.8, isComeback(cd));
        });

        addAssignmentBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);

            // Create custom assignment name dialog with dark text
            JPanel assignmentPanel = new JPanel(new BorderLayout(5, 5));
            assignmentPanel.setBackground(Color.WHITE);
            
            JLabel assignmentLabel = new JLabel("Assignment name:");
            assignmentLabel.setForeground(Color.BLACK);
            assignmentLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            JTextField assignmentField = new JTextField();
            assignmentField.setForeground(Color.BLACK);
            assignmentField.setBackground(Color.WHITE);
            assignmentField.setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            assignmentPanel.add(assignmentLabel, BorderLayout.NORTH);
            assignmentPanel.add(assignmentField, BorderLayout.CENTER);
            
            int assignmentResult = showCustomComplexDialog(root, assignmentPanel, "Add Assignment") ? 
                JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION;
            
            String aName = null;
            if (assignmentResult == JOptionPane.OK_OPTION) {
                aName = assignmentField.getText();
            }
            if (aName == null || aName.trim().isEmpty()) return;

            // Get existing categories from the class data, plus option to add new one
            java.util.Set<String> existingCategories = cd.weights.keySet();
            java.util.List<String> categoryList = new java.util.ArrayList<>(existingCategories);
            categoryList.add("+ Add New Category");
            
            String[] categories = categoryList.toArray(new String[0]);
            // Use first category as default, or show custom category creation
            String category = categories.length > 0 ? categories[0] : null;
            if (categories.length > 1) {
                // If multiple categories exist, let user pick
                category = showCustomInputDialog(root, "Select category (or type new name):\nExisting: " + 
                    String.join(", ", java.util.Arrays.copyOf(categories, categories.length-1)), 
                    "Assignment Category", categories[0]);
            }
            if (category == null) return;
            
            // Handle new category creation
            if ("+ Add New Category".equals(category)) {
                category = showCustomInputDialog(root, "Enter new category name:", "New Category", "");
                if (category == null || category.trim().isEmpty()) return;
                category = category.trim();
                
                // Add new category with 0% weight initially
                if (!cd.weights.containsKey(category)) {
                    cd.weights.put(category, 0.0);
                    cd.assignments.put(category, new java.util.ArrayList<>());
                    showCustomMessageDialog(root, 
                        "New category '" + category + "' added with 0% weight.\n" +
                        "Don't forget to adjust weights using the Weights button!", 
                        "Category Added");
                }
            }

            // Create custom points input dialog
            JPanel pointsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
            pointsPanel.setBackground(Color.WHITE);
            
            JLabel earnedLabel = new JLabel("Points Earned:");
            earnedLabel.setForeground(Color.BLACK);
            JTextField earnedField = new JTextField();
            earnedField.setForeground(Color.BLACK);
            earnedField.setBackground(Color.WHITE);
            
            JLabel totalLabel = new JLabel("Total Points:");
            totalLabel.setForeground(Color.BLACK);
            JTextField totalField = new JTextField();
            totalField.setForeground(Color.BLACK);
            totalField.setBackground(Color.WHITE);
            
            JLabel exampleLabel = new JLabel("Example: 102 earned / 100 total = 102% (extra credit)");
            exampleLabel.setForeground(Color.GRAY);
            exampleLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            
            pointsPanel.add(earnedLabel);
            pointsPanel.add(earnedField);
            pointsPanel.add(totalLabel);
            pointsPanel.add(totalField);
            pointsPanel.add(new JLabel()); // Spacer
            pointsPanel.add(exampleLabel);
            
            boolean pointsResult = showCustomComplexDialog(root, pointsPanel, "Enter Assignment Points");
            
            if (!pointsResult) return;
            
            try {
                double earnedPoints = Double.parseDouble(earnedField.getText().trim());
                double totalPoints = Double.parseDouble(totalField.getText().trim());
                
                // Validation
                if (totalPoints <= 0) {
                    showCustomMessageDialog(root, "Total points must be greater than 0!", 
                        "Invalid Input");
                    return;
                }
                
                // Validate points before creating assignment
                if (Double.isNaN(earnedPoints) || Double.isNaN(totalPoints)) {
                    showCustomMessageDialog(root, "Please enter valid numbers for points.", "Invalid Input");
                    return;
                }
                
                Assignment a = new Assignment(aName, earnedPoints, totalPoints, category);
                cd.assignments.get(category).add(a);
                
                // Show in table with points format and percentage
                String displayScore = String.format("%.1f/%.1f (%.1f%%)", 
                    earnedPoints, totalPoints, a.getPercentage());
                ((DefaultTableModel)table.getModel()).addRow(new Object[]{aName, category, displayScore});
                
                // Show extra credit warning if applicable
                if (a.hasExtraCredit()) {
                    showCustomMessageDialog(root, 
                        "Extra credit detected! " + a.getPointsDisplay() + " = " + 
                        String.format("%.1f%%", a.getPercentage()),
                        "Extra Credit");
                }
                
                pushHistory(cd);
                saveAllUserData();

                double classPercent = calculateClassPercent(cd);
                classGpaLabel.setText("Class GPA: " + String.format("%.2f", percentToGPA(classPercent)));
                updateOverallGpaLabel();

                // refresh visuals
                classList.repaint();
                updateCategoryBreakdown(cd, categoryBreakdownPanel, gradingModeLabel);
                updatePieChartWithAllCategories(cd, piePanel);
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
            updatePieChartWithAllCategories(cd, piePanel);
            trendPanel.setData(cd.historyPercent);
            badgePanel.setBadges(percentToGPA(classPercent) >= 3.8, isComeback(cd));
        });

        weightsBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);

            showCustomWeightsDialog(root, cd, semesterName);
        });

        creditsBtn.addActionListener(_ -> {
            String selectedClass = classList.getSelectedValue();
            if (selectedClass == null) return;
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);
            JTextField creditField = new JTextField(String.valueOf(cd.credits));
            creditField.setForeground(Color.BLACK);
            creditField.setBackground(Color.WHITE);
            creditField.setFont(new Font("SansSerif", Font.PLAIN, 14));
            JPanel creditPanel = new JPanel(new BorderLayout());
            creditPanel.setBackground(Color.WHITE);
            JLabel creditLabel = new JLabel("Credit hours:");
            creditLabel.setForeground(Color.BLACK);
            creditPanel.add(creditLabel, BorderLayout.NORTH);
            creditPanel.add(creditField, BorderLayout.CENTER);
            
            String newC = showCustomInputDialog(root, "Credit hours:", "Edit Credit Hours", String.valueOf(cd.credits));
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
                showCustomConfirmDialog(root, "Please select a class first.", "No Class Selected");
                return;
            }
            
            ClassData cd = userData.get(currentUser).get(semesterName).get(selectedClass);
            
            // Letter grade options with GPA values
            String[] grades = {"A (4.0)", "A- (3.7)", "B+ (3.3)", "B (3.0)", "B- (2.7)", "C+ (2.3)", "C (2.0)", "C- (1.7)", "D+ (1.3)", "D (1.0)", "D- (0.7)", "F (0.0)"};
            String[] gradeValues = {"A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"};
            
            // Create modern styled panel with padding and proper layout
            JPanel gradePanel = new JPanel();
            gradePanel.setLayout(new BoxLayout(gradePanel, BoxLayout.Y_AXIS));
            gradePanel.setBackground(Color.WHITE);
            gradePanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
            
            // Header section with icon and title
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            headerPanel.setBackground(Color.WHITE);
            
            JLabel iconLabel = new JLabel("🎓");
            iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
            
            JLabel titleLabel = new JLabel("Set Final Grade");
            titleLabel.setForeground(new Color(0x2D3748));
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            
            headerPanel.add(iconLabel);
            headerPanel.add(titleLabel);
            
            // Info section with better styling
            JLabel infoLabel = new JLabel("<html><div style='color: #718096; font-size: 13px;'>Choose the final letter grade for <b>" + selectedClass + "</b></div></html>");
            infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            infoLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 20, 0));
            
            // Grade selection section
            JLabel gradeLabel = new JLabel("Letter Grade:");
            gradeLabel.setForeground(new Color(0x2D3748));
            gradeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            gradeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            
            JComboBox<String> gradeCombo = new JComboBox<>(grades);
            gradeCombo.setForeground(new Color(0x2D3748));
            gradeCombo.setBackground(Color.WHITE);
            gradeCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
            gradeCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            gradeCombo.setMaximumSize(new Dimension(250, 40));
            gradeCombo.setPreferredSize(new Dimension(250, 40));
            
            // Set selected grade if it exists
            if (!cd.letterGrade.isEmpty()) {
                for (int i = 0; i < gradeValues.length; i++) {
                    if (gradeValues[i].equals(cd.letterGrade)) {
                        gradeCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            // Custom renderer for better dropdown appearance
            gradeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (isSelected) {
                        setBackground(new Color(0xEDF2F7));
                        setForeground(new Color(0x2D3748));
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(new Color(0x2D3748));
                    }
                    setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                    return c;
                }
            });
            
            // Class status section with better styling
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            statusPanel.setBackground(Color.WHITE);
            statusPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            
            JCheckBox isPastCheck = new JCheckBox("Mark as Past/Archived Class", !cd.isActive);
            isPastCheck.setForeground(new Color(0x4A5568));
            isPastCheck.setBackground(Color.WHITE);
            isPastCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
            isPastCheck.setToolTipText("Past classes use letter grades instead of individual assignments");
            isPastCheck.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            statusPanel.add(isPastCheck);
            
            // Add components with proper spacing
            gradePanel.add(headerPanel);
            gradePanel.add(infoLabel);
            gradePanel.add(gradeLabel);
            gradePanel.add(Box.createVerticalStrut(5));
            gradePanel.add(gradeCombo);
            gradePanel.add(statusPanel);
            gradePanel.add(Box.createVerticalStrut(10));
            
            boolean result = showCustomComplexDialog(root, gradePanel, 
                "Set Letter Grade - " + selectedClass);
            
            if (result) {
                int selectedIndex = gradeCombo.getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < gradeValues.length) {
                    String selectedGrade = gradeValues[selectedIndex];
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
                            String.format("%.1f%%", assignment.getPercentage()),
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
            String newUsername = showCustomInputDialog(parent, "Enter new username:", "Change Username", "");
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
                String newPass = showCustomInputDialog(parent, "Set a new password:", "New Password", "");
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

    // ===== MODERN UI COMPONENT FACTORIES =====
    
    /**
     * Creates a floating action button with enhanced shadows and hover effects
     */
    private static JButton createFloatingButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get hover state from client properties
                Boolean hoveredObj = (Boolean) getClientProperty("isHovered");
                boolean isCurrentlyHovered = hoveredObj != null && hoveredObj;
                
                Float shadowIntensityObj = (Float) getClientProperty("shadowIntensity");
                float currentShadowIntensity = shadowIntensityObj != null ? shadowIntensityObj : 0.3f;
                
                // Enhanced shadow system
                int shadowSize = isCurrentlyHovered ? 8 : 4;
                for (int i = 0; i < shadowSize; i++) {
                    int alpha = (int) (currentShadowIntensity * (shadowSize - i) * 255 / shadowSize);
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(i + 2, i + 4, getWidth() - (i * 2) - 4, 
                                    getHeight() - (i * 2) - 6, getHeight(), getHeight());
                }
                
                // Button background with gradient
                Color startColor = isCurrentlyHovered ? color.brighter() : color;
                Color endColor = isCurrentlyHovered ? color : color.darker();
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, getHeight(), getHeight());
                
                // Highlight effect
                if (isCurrentlyHovered) {
                    g2d.setColor(new Color(255, 255, 255, 30));
                    g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() / 2, getHeight(), getHeight());
                }
                
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        
        button.setFont(BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        
        // Smooth hover animation with safe implementation
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Stop any existing animation
                javax.swing.Timer existingTimer = (javax.swing.Timer) button.getClientProperty("hoverTimer");
                if (existingTimer != null && existingTimer.isRunning()) {
                    existingTimer.stop();
                }
                
                // Animate shadow intensity increase
                javax.swing.Timer hoverTimer = new javax.swing.Timer(20, evt -> {
                    Float currentVal = (Float) button.getClientProperty("shadowIntensity");
                    float current = currentVal != null ? currentVal : 0.3f;
                    current = Math.min(0.6f, current + 0.05f);
                    button.putClientProperty("shadowIntensity", current);
                    button.putClientProperty("isHovered", true);
                    button.repaint();
                    
                    if (current >= 0.6f) {
                        ((javax.swing.Timer) evt.getSource()).stop();
                    }
                });
                button.putClientProperty("hoverTimer", hoverTimer);
                hoverTimer.start();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // Stop any existing animation
                javax.swing.Timer existingTimer = (javax.swing.Timer) button.getClientProperty("hoverTimer");
                if (existingTimer != null && existingTimer.isRunning()) {
                    existingTimer.stop();
                }
                
                // Animate shadow intensity decrease
                javax.swing.Timer hoverTimer = new javax.swing.Timer(20, evt -> {
                    Float currentVal = (Float) button.getClientProperty("shadowIntensity");
                    float current = currentVal != null ? currentVal : 0.6f;
                    current = Math.max(0.3f, current - 0.05f);
                    button.putClientProperty("shadowIntensity", current);
                    
                    if (current <= 0.3f) {
                        button.putClientProperty("isHovered", false);
                        ((javax.swing.Timer) evt.getSource()).stop();
                    }
                    button.repaint();
                });
                button.putClientProperty("hoverTimer", hoverTimer);
                hoverTimer.start();
            }
        });
        
        return button;
    }
    
    // ===== Utilities & GPA =====
    
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

    // Darken a color by factor (0..1)
    private static Color darken(Color color, float factor) {
        factor = Math.max(0f, Math.min(1f, factor));
        int r = Math.max(0, (int) (color.getRed() * (1 - factor)));
        int g = Math.max(0, (int) (color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int) (color.getBlue() * (1 - factor)));
        return new Color(r, g, b, color.getAlpha());
    }

    // Removed unused sizedIcon method - not needed with ModernUIFramework

    // Helper method to create BufferedImage from Icon for window icons
    private static BufferedImage iconToImage(Icon icon) {
        BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        return img;
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
        if (cd == null || cd.assignments == null || cat == null) return 0.0;
        List<Assignment> categoryAssignments = cd.assignments.get(cat);
        if (categoryAssignments == null || categoryAssignments.isEmpty()) {
            return 0.0;
        }
        return categoryAssignments.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Assignment::getPercentage)
                .average().orElse(0);
    }

    private static Color barColorFor(double percent) {
        if (percent >= 85) return new Color(46, 204, 113);   // green
        if (percent >= 75) return new Color(243, 156, 18);   // yellow/orange
        return new Color(231, 76, 60);                       // red
    }

    // ===== GPA/Percent CALCULATIONS =====
    private static double calculateClassPercent(ClassData cd) {
        if (cd == null || cd.assignments == null) return 0.0;
        
        // If this class has a letter grade set (for past classes), use that
        if (cd.letterGrade != null && !cd.letterGrade.isEmpty()) {
            return letterGradeToPercentage(cd.letterGrade);
        }
        
        // Calculate based on selected grading mode
        if (cd.gradingMode == ClassData.GradingMode.TOTAL_POINTS) {
            return calculateTotalPointsPercent(cd);
        } else {
            return calculateWeightedPercent(cd);
        }
    }
    
    /**
     * Calculate percentage using weighted category averages
     */
    private static double calculateWeightedPercent(ClassData cd) {
        double weightedTotal = 0;
        double weightSum = 0;
        
        for (String cat : cd.assignments.keySet()) {
            List<Assignment> assignments = cd.assignments.get(cat);
            if (assignments != null && !assignments.isEmpty()) {
                double avg = avgFor(cd, cat);
                double weight = cd.weights.getOrDefault(cat, 0.0);
                weightedTotal += avg * (weight / 100.0);
                weightSum += weight;
            }
        }
        return (weightSum > 0) ? weightedTotal : 0.0;
    }
    
    /**
     * Calculate percentage using total points method (sum earned / sum possible)
     */
    private static double calculateTotalPointsPercent(ClassData cd) {
        double totalEarned = 0;
        double totalPossible = 0;
        
        for (String cat : cd.assignments.keySet()) {
            List<Assignment> assignments = cd.assignments.get(cat);
            if (assignments != null) {
                for (Assignment assignment : assignments) {
                    totalEarned += assignment.earnedPoints;
                    totalPossible += assignment.totalPoints;
                }
            }
        }
        
        if (totalPossible == 0) return 0.0;
        return (totalEarned / totalPossible) * 100.0;
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


    // ===== CUSTOM DIALOG UTILITIES WITH GUARANTEED DARK TEXT =====
    
    /**
     * Shows a custom input dialog with guaranteed dark text buttons
     */
    private static String showCustomInputDialog(Component parent, String message, String title, String initialValue) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parent);
        
        // Main panel with white background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Message label with dark text
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.BLACK);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // Input field with dark text
        JTextField inputField = new JTextField(initialValue != null ? initialValue : "");
        inputField.setForeground(Color.BLACK);
        inputField.setBackground(Color.WHITE);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        // OK and Cancel buttons with guaranteed dark text
        JButton okButton = new JButton("OK");
        okButton.setForeground(Color.BLACK);
        okButton.setBackground(Color.LIGHT_GRAY);
        okButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        okButton.setFocusPainted(false);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setBackground(Color.LIGHT_GRAY);
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelButton.setFocusPainted(false);
        
        final String[] result = new String[1];
        
        okButton.addActionListener(e -> {
            result[0] = inputField.getText();
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> {
            result[0] = null;
            dialog.dispose();
        });
        
        // Enter key support
        inputField.addActionListener(e -> okButton.doClick());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(messageLabel, BorderLayout.NORTH);
        mainPanel.add(inputField, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
        
        return result[0];
    }
    
    /**
     * Shows a custom message dialog with guaranteed dark text buttons
     */
    private static void showCustomMessageDialog(Component parent, String message, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(parent);
        
        // Main panel with white background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Message label with dark text
        JLabel messageLabel = new JLabel("<html><body style='width: 300px'>" + message + "</body></html>");
        messageLabel.setForeground(Color.BLACK);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // OK button with guaranteed dark text
        JButton okButton = new JButton("OK");
        okButton.setForeground(Color.BLACK);
        okButton.setBackground(Color.LIGHT_GRAY);
        okButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);
        
        mainPanel.add(messageLabel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }



    /**
     * Shows a custom complex dialog with guaranteed dark text buttons
     */
    private static boolean showCustomComplexDialog(Component parent, JPanel content, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setLayout(new BorderLayout());
        
        // Auto-size based on content or use sensible defaults
        int width = Math.max(450, content.getPreferredSize().width + 40);
        int height = Math.max(300, content.getPreferredSize().height + 100);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
        
        // Ensure content has white background and dark text
        content.setBackground(Color.WHITE);
        ensureAllComponentsHaveDarkText(content);
        
        // Modern button panel with proper spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Modern styled Save and Cancel buttons with black text
        JButton okButton = new JButton("Save");
        okButton.setForeground(Color.BLACK);
        okButton.setBackground(new Color(0x3182CE));
        okButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        okButton.setFocusPainted(false);
        okButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setBackground(new Color(0xF7FAFC));
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE2E8F0), 1),
            BorderFactory.createEmptyBorder(9, 19, 9, 19)
        ));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        final boolean[] result = new boolean[1];
        
        okButton.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
        
        return result[0];
    }
    
    /**
     * Recursively ensures all components have dark text
     */
    private static void ensureAllComponentsHaveDarkText(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                comp.setForeground(Color.BLACK);
                comp.setFont(new Font("SansSerif", Font.BOLD, 14));
            } else if (comp instanceof JTextField) {
                comp.setForeground(Color.BLACK);
                comp.setBackground(Color.WHITE);
            } else if (comp instanceof JComboBox) {
                comp.setForeground(Color.BLACK);
                comp.setBackground(Color.WHITE);
            } else if (comp instanceof JCheckBox) {
                comp.setForeground(Color.BLACK);
                comp.setBackground(Color.WHITE);
            } else if (comp instanceof Container) {
                ensureAllComponentsHaveDarkText((Container) comp);
            }
        }
    }
    
    /**
     * Shows a custom confirmation dialog with guaranteed dark text buttons
     */
    private static boolean showCustomConfirmDialog(Component parent, String message, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(parent);
        
        // Main panel with white background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Message label with dark text
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.BLACK);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        // OK and Cancel buttons with guaranteed dark text
        JButton okButton = new JButton("OK");
        okButton.setForeground(Color.BLACK);
        okButton.setBackground(Color.LIGHT_GRAY);
        okButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        okButton.setFocusPainted(false);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setBackground(Color.LIGHT_GRAY);
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelButton.setFocusPainted(false);
        
        final boolean[] result = new boolean[1];
        
        okButton.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(messageLabel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
        
        return result[0];
    }


    
    // ===== SAVE/LOAD (JSON with Gson) =====
    private static void ensureDataDir() {
        try {
            File d = new File(DATA_DIR);
            if (!d.exists()) {
                boolean created = d.mkdirs();
                if (!created) {
                    System.err.println("Warning: Could not create data directory: " + DATA_DIR);
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating data directory: " + e.getMessage());
            // Just log the error - fallback will be handled elsewhere if needed
        }
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
        try {
            // Load from JSON file system
            File f = new File(USERS_FILE);
            if (f.exists()) {
                try (FileReader fr = new FileReader(f)) {
                    Map<String, String[]> map = gson.fromJson(fr, new TypeToken<Map<String, String[]>>(){}.getType());
                    if (map != null) users = map;
                } catch (Exception e) { 
                    System.err.println("Error loading users file: " + e.getMessage());
                    users = new HashMap<>();
                }
            } else {
                users = new HashMap<>();
            }

            // Load username changes (still from JSON for now)
            File f2 = new File(USERNAME_CHANGES_FILE);
            if (f2.exists()) {
                try (FileReader fr = new FileReader(f2)) {
                    Map<String, Long> map = gson.fromJson(fr, new TypeToken<Map<String, Long>>(){}.getType());
                    if (map != null) lastUsernameChange = map;
                } catch (Exception e) { 
                    System.err.println("Error loading username changes file: " + e.getMessage());
                    lastUsernameChange = new HashMap<>();
                }
            } else {
                lastUsernameChange = new HashMap<>();
            }
        } catch (Exception e) {
            System.err.println("Critical error in loadUsers: " + e.getMessage());
            // Ensure maps are initialized even if everything fails
            users = new HashMap<>();
            lastUsernameChange = new HashMap<>();
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
        
        // Auto-sync to cloud when data is saved
        if (firebaseInitialized && currentUserEmail != null && currentUser != null) {
            syncToCloud(currentUserEmail);
        }
    }

    private static void loadAllUserData() {
        try {
            userData = new HashMap<>();
            semesterOrder = new HashMap<>();
            
            // For now, we'll load from database later - after resolving circular dependencies
            // The migration will have already moved the data to the database
            
            // Fallback to JSON if database is empty
            if (userData.isEmpty()) {
                File f = new File(USERDATA_FILE);
                if (!f.exists()) {
                    return;
                }
                
                try (FileReader fr = new FileReader(f)) {
                    // Try to load the new String-based format first
                    try {
                        Map<String, Map<String, Map<String, ClassData>>> map =
                                gson.fromJson(fr, new TypeToken<Map<String, Map<String, Map<String, ClassData>>>>(){}.getType());
                        if (map != null) {
                            userData = map;
                            loadSemesterOrder();
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("Could not load new format user data: " + e.getMessage());
                    }
                } catch (IOException e) { 
                    System.err.println("Error reading user data file: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Critical error in loadAllUserData: " + e.getMessage());
            userData = new HashMap<>();
            semesterOrder = new HashMap<>();
        }
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
        private static final long serialVersionUID = 1L;
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
        private static final long serialVersionUID = 1L;
        private transient List<Double> data = new ArrayList<>();
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
        private static final long serialVersionUID = 1L;
        private final String placeholder;
        PlaceholderTextField(String placeholder) { super(); this.placeholder = placeholder; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isFocusOwner() && getText().isEmpty() && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Light gray placeholder color for clean modern look
                g2.setColor(new Color(160, 160, 160));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                Insets insets = getInsets();
                int x = insets.left;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    static class PlaceholderPasswordField extends JPasswordField {
        private static final long serialVersionUID = 1L;
        private final String placeholder;
        PlaceholderPasswordField(String placeholder) { super(); this.placeholder = placeholder; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isFocusOwner() && getPassword().length == 0 && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Light gray placeholder color for clean modern look
                g2.setColor(new Color(160, 160, 160));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                Insets insets = getInsets();
                int x = insets.left;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
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
    
    // ===== Enhanced Grade Management Methods =====
    
    /**
     * Show Grade Analytics Dialog with predictions and statistics
     */
    
    // Future enhancement placeholders for advanced analytics features
    // - Grade charts dialog with visual analytics
    // - What-if scenarios dialog for grade planning 
    // - Export dialog for generating reports
    // - Course analytics conversion methods
    // - Statistics and predictions panels
    
    /**
     * Create Trends Panel for Analytics
     */
    
    /**
     * Display scenario result in text area
     */
    
    /**
     * Open Interactive Data Visualization Dashboard
     */
    
    // JSON-based user management helper methods
    
    /**
     * Authenticate user using JSON data
     */
    private static String authenticateUserJSON(String username, String password) {
        if (users.containsKey(username)) {
            String[] userData = users.get(username);
            if (userData != null && userData.length > 0 && userData[0].equals(password)) {
                return username;
            }
        }
        return null;
    }
    
    /**
     * Check if email exists in JSON data
     */
    private static boolean emailExistsJSON(String email) {
        for (String[] userData : users.values()) {
            if (userData != null && userData.length > 1 && email.equals(userData[1])) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create new user in JSON data
     */
    private static boolean createUserJSON(String username, String password, String email) {
        try {
            users.put(username, new String[]{password, email});
            saveUsers();
            return true;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Show custom weights dialog for managing category weights
     */
    private static void showCustomWeightsDialog(JPanel parent, ClassData cd, String semesterName) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parent), "Grade Calculation Settings", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Grading mode selection panel
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.setBackground(Color.WHITE);
        modePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Grading Mode", 
            0, 0, 
            new Font("SansSerif", Font.BOLD, 14), 
            Color.BLACK));
        
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton weightedMode = new JRadioButton("Weighted (Category averages × weights)", 
            cd.gradingMode == ClassData.GradingMode.WEIGHTED);
        JRadioButton totalPointsMode = new JRadioButton("Total Points (Sum earned ÷ sum possible)", 
            cd.gradingMode == ClassData.GradingMode.TOTAL_POINTS);
        
        weightedMode.setForeground(Color.BLACK);
        weightedMode.setBackground(Color.WHITE);
        totalPointsMode.setForeground(Color.BLACK);
        totalPointsMode.setBackground(Color.WHITE);
        
        modeGroup.add(weightedMode);
        modeGroup.add(totalPointsMode);
        modePanel.add(weightedMode);
        modePanel.add(totalPointsMode);
        
        // Category weights panel
        JPanel weightsPanel = new JPanel(new GridBagLayout());
        weightsPanel.setBackground(Color.WHITE);
        weightsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Custom Categories & Weights", 
            0, 0, 
            new Font("SansSerif", Font.BOLD, 14), 
            Color.BLACK));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Show message if no categories exist
        if (cd.weights.isEmpty()) {
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
            JLabel noCategories = new JLabel("No categories added yet. Add your first category below!");
            noCategories.setForeground(Color.GRAY);
            noCategories.setFont(new Font("SansSerif", Font.ITALIC, 14));
            weightsPanel.add(noCategories, gbc);
            gbc.gridy = 1; gbc.gridwidth = 1;
        }
        
        Map<String, JTextField> weightFields = new HashMap<>();
        int row = cd.weights.isEmpty() ? 1 : 0;
        
        // Add existing categories (all are deletable since no defaults)
        for (String category : cd.weights.keySet()) {
            gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
            JLabel categoryLabel = new JLabel(category + ":");
            categoryLabel.setForeground(Color.BLACK);
            categoryLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            weightsPanel.add(categoryLabel, gbc);
            
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            JTextField field = new JTextField(String.format("%.1f", cd.weights.get(category)), 10);
            field.setForeground(Color.BLACK);
            field.setBackground(Color.WHITE);
            field.setFont(new Font("SansSerif", Font.PLAIN, 14));
            weightFields.put(category, field);
            weightsPanel.add(field, gbc);
            
            gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
            JLabel percentLabel = new JLabel("%");
            percentLabel.setForeground(Color.BLACK);
            weightsPanel.add(percentLabel, gbc);
            
            // Add category average display
            gbc.gridx = 3;
            double categoryAvg = cd.getCategoryAverage(category);
            String avgText = cd.assignments.get(category).isEmpty() ? "No assignments" : 
                String.format("Avg: %.1f%%", categoryAvg);
            JLabel avgLabel = new JLabel(avgText);
            avgLabel.setForeground(Color.GRAY);
            avgLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            weightsPanel.add(avgLabel, gbc);
            
            // Add delete button (all categories can be deleted)
            gbc.gridx = 4;
            JButton deleteBtn = new JButton("✖");
            deleteBtn.setForeground(Color.RED);
            deleteBtn.setBackground(Color.WHITE);
            deleteBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            deleteBtn.setToolTipText("Delete " + category);
            deleteBtn.addActionListener(e -> {
                List<Assignment> categoryAssignments = cd.assignments.get(category);
                if (categoryAssignments == null || categoryAssignments.isEmpty() || 
                    JOptionPane.showConfirmDialog(dialog, 
                        "Delete category '" + category + "' and all its " + 
                        categoryAssignments.size() + " assignments?", 
                        "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    cd.removeCategory(category);
                    dialog.dispose();
                    showCustomWeightsDialog(parent, cd, semesterName);
                }
            });
            weightsPanel.add(deleteBtn, gbc);
            row++;
        }
        
        // Add new category button
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton addCategoryBtn = new JButton("+ Add New Category");
        addCategoryBtn.setForeground(Color.BLACK);
        addCategoryBtn.setBackground(new Color(230, 230, 230));
        addCategoryBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        addCategoryBtn.addActionListener(e -> {
            String newCategory = showCustomInputDialog(dialog, 
                "Enter new category name:\n(Examples: Homework, Quizzes, Midterms, Final, Projects, Participation)", 
                "New Category", "");
            if (newCategory != null && !newCategory.trim().isEmpty()) {
                newCategory = newCategory.trim();
                if (!cd.weights.containsKey(newCategory)) {
                    cd.addCategory(newCategory, 0.0);
                    dialog.dispose();
                    showCustomWeightsDialog(parent, cd, semesterName);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Category '" + newCategory + "' already exists!");
                }
            }
        });
        weightsPanel.add(addCategoryBtn, gbc);
        
        // Weight validation info
        gbc.gridx = 0; gbc.gridy = row + 1; gbc.gridwidth = 5;
        double currentTotal = cd.getTotalWeight();
        String weightInfo = cd.gradingMode == ClassData.GradingMode.WEIGHTED ? 
            String.format("Current total: %.1f%% (should equal 100%% for weighted mode)", currentTotal) :
            "Weights not used in Total Points mode";
        JLabel weightInfoLabel = new JLabel(weightInfo);
        weightInfoLabel.setForeground(cd.gradingMode == ClassData.GradingMode.WEIGHTED && 
            Math.abs(currentTotal - 100.0) > 0.01 ? Color.RED : Color.GRAY);
        weightInfoLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        weightsPanel.add(weightInfoLabel, gbc);
        
        // Enable/disable weight fields based on mode
        ActionListener modeListener = e -> {
            boolean isWeightedMode = ((JRadioButton) e.getSource()).getText().contains("Weighted");
            for (JTextField field : weightFields.values()) {
                field.setEnabled(isWeightedMode);
            }
        };
        weightedMode.addActionListener(modeListener);
        totalPointsMode.addActionListener(modeListener);
        
        // Initially disable weight fields if in total points mode
        if (cd.gradingMode == ClassData.GradingMode.TOTAL_POINTS) {
            for (JTextField field : weightFields.values()) {
                field.setEnabled(false);
            }
        }
        
        mainPanel.add(modePanel, BorderLayout.NORTH);
        mainPanel.add(weightsPanel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveBtn = new JButton("Save Settings");
        saveBtn.setForeground(Color.BLACK);
        saveBtn.setBackground(BRAND_PRIMARY);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.addActionListener(e -> {
            try {
                // Update grading mode
                ClassData.GradingMode newMode = weightedMode.isSelected() ? 
                    ClassData.GradingMode.WEIGHTED : ClassData.GradingMode.TOTAL_POINTS;
                cd.gradingMode = newMode;
                
                // Update weights (only validate if in weighted mode)
                if (newMode == ClassData.GradingMode.WEIGHTED) {
                    double total = 0;
                    Map<String, Double> newWeights = new HashMap<>();
                    
                    for (String category : weightFields.keySet()) {
                        double weight = Double.parseDouble(weightFields.get(category).getText());
                        if (weight < 0) {
                            JOptionPane.showMessageDialog(dialog, "Weights cannot be negative!");
                            return;
                        }
                        newWeights.put(category, weight);
                        total += weight;
                    }
                    
                    // Warn if weights don't sum to 100% in weighted mode
                    if (Math.abs(total - 100.0) > 0.01) {
                        boolean confirm = showCustomConfirmDialog(dialog,
                            String.format("Weights total %.1f%% instead of 100%%. This may affect grade calculation accuracy.\\n\\nContinue anyway?", total),
                            "Weight Warning");
                        if (!confirm) return;
                    }
                    
                    cd.weights.putAll(newWeights);
                } else {
                    // In total points mode, we still save weights but they won't be used in calculation
                    for (String category : weightFields.keySet()) {
                        try {
                            double weight = Double.parseDouble(weightFields.get(category).getText());
                            if (weight >= 0) {
                                cd.weights.put(category, weight);
                            }
                        } catch (NumberFormatException ex) {
                            // Ignore invalid weights in total points mode
                        }
                    }
                }
                
                pushHistory(cd);
                saveAllUserData();
                
                String modeText = newMode == ClassData.GradingMode.WEIGHTED ? "Weighted" : "Total Points";
                showCustomMessageDialog(dialog, 
                    "Settings saved successfully!\\nGrading Mode: " + modeText, 
                    "Settings Saved");
                dialog.dispose();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for all weights!");
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setForeground(Color.BLACK);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Update pie chart with all available categories
     */
    /**
     * Update the category breakdown display with averages and validation info
     */
    private static void updateCategoryBreakdown(ClassData cd, JPanel categoryPanel, JLabel gradingModeLabel) {
        // Clear existing category labels (keep only the grading mode label)
        Component[] components = categoryPanel.getComponents();
        for (int i = components.length - 1; i >= 1; i--) {
            categoryPanel.remove(i);
        }
        
        // Update grading mode display
        String modeText = cd.gradingMode == ClassData.GradingMode.WEIGHTED ? 
            "Weighted Mode" : "Total Points Mode";
        gradingModeLabel.setText("Mode: " + modeText);
        
        if (cd.assignments.isEmpty()) {
            JLabel noCategories = new JLabel("No categories yet");
            noCategories.setForeground(Color.GRAY);
            noCategories.setFont(new Font("SansSerif", Font.ITALIC, 12));
            categoryPanel.add(noCategories);
            categoryPanel.revalidate();
            categoryPanel.repaint();
            return;
        }
        
        // Add category averages
        for (String category : cd.assignments.keySet()) {
            List<Assignment> assignments = cd.assignments.get(category);
            if (assignments != null && !assignments.isEmpty()) {
                double categoryAvg = cd.getCategoryAverage(category);
                double weight = cd.weights.getOrDefault(category, 0.0);
                
                JLabel categoryLabel = new JLabel();
                categoryLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
                
                String text = String.format("%s: %.1f%%", category, categoryAvg);
                if (cd.gradingMode == ClassData.GradingMode.WEIGHTED) {
                    text += String.format(" (%.1f%% weight)", weight);
                }
                
                categoryLabel.setText(text);
                
                // Color code based on performance
                if (categoryAvg >= 90) {
                    categoryLabel.setForeground(new Color(46, 204, 113)); // Green
                } else if (categoryAvg >= 80) {
                    categoryLabel.setForeground(new Color(243, 156, 18)); // Orange
                } else if (categoryAvg >= 70) {
                    categoryLabel.setForeground(new Color(230, 126, 34)); // Dark orange
                } else {
                    categoryLabel.setForeground(new Color(231, 76, 60)); // Red
                }
                
                categoryPanel.add(categoryLabel);
                
                // Check for extra credit in this category
                boolean hasExtraCredit = assignments.stream().anyMatch(Assignment::hasExtraCredit);
                if (hasExtraCredit) {
                    JLabel extraCreditLabel = new JLabel("  ★ Extra credit detected");
                    extraCreditLabel.setForeground(new Color(155, 89, 182)); // Purple
                    extraCreditLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
                    categoryPanel.add(extraCreditLabel);
                }
            }
        }
        
        // Add validation warnings
        if (cd.gradingMode == ClassData.GradingMode.WEIGHTED) {
            double totalWeight = cd.getTotalWeight();
            if (Math.abs(totalWeight - 100.0) > 0.01) {
                JLabel warningLabel = new JLabel();
                warningLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
                warningLabel.setForeground(Color.RED);
                
                if (totalWeight == 0) {
                    warningLabel.setText("⚠ No weights set!");
                } else {
                    warningLabel.setText(String.format("⚠ Weights total %.1f%%", totalWeight));
                }
                categoryPanel.add(warningLabel);
            }
        }
        
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    private static void updatePieChartWithAllCategories(ClassData cd, PiePanel piePanel) {
        // For now, still use the first 3 categories for the pie chart
        // In the future, this could be enhanced to show all categories
        String[] categories = cd.weights.keySet().toArray(new String[0]);
        
        if (categories.length >= 3) {
            piePanel.setData(
                avgFor(cd, categories[0]),
                avgFor(cd, categories[1]),
                avgFor(cd, categories[2])
            );
        } else if (categories.length == 2) {
            piePanel.setData(
                avgFor(cd, categories[0]),
                avgFor(cd, categories[1]),
                0
            );
        } else if (categories.length == 1) {
            piePanel.setData(avgFor(cd, categories[0]), 0, 0);
        } else {
            piePanel.setData(0, 0, 0);
        }
    }
}
