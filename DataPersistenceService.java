import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * DataPersistenceService - Handles all file I/O operations and data persistence
 * Part of the refactored architecture for CollegeGPATracker
 */
public class DataPersistenceService {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.json";
    private static final String USERDATA_FILE = DATA_DIR + File.separator + "user_data.json";
    private static final String USERNAME_CHANGES_FILE = DATA_DIR + File.separator + "username_changes.json";
    private static final String SEMESTER_ORDER_FILE = DATA_DIR + File.separator + "semester_order.json";
    private static final String USER_PREFS_FILE = DATA_DIR + File.separator + "user_prefs.json";
    // Session file handled by SecurityManager
    // private static final String SESSION_FILE = DATA_DIR + File.separator + "session.json";
    
    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    
    /**
     * Initialize data directory and required files
     */
    public static void initialize() {
        ensureDataDirectory();
    }
    
    /**
     * Ensure data directory exists
     */
    private static void ensureDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (!created) {
                System.err.println("Warning: Could not create data directory: " + DATA_DIR);
            }
        }
    }
    
    // ===== USER DATA PERSISTENCE =====
    
    /**
     * Save all users to file
     */
    public static boolean saveUsers(Map<String, User> users) {
        try {
            // Convert User objects to legacy format for compatibility
            Map<String, String[]> legacyUsers = new HashMap<>();
            for (Map.Entry<String, User> entry : users.entrySet()) {
                User user = entry.getValue();
                // Legacy format: [password, email] - password handled by SecurityManager
                legacyUsers.put(entry.getKey(), new String[]{"", user.getEmail()});
            }
            
            return writeToFile(USERS_FILE, legacyUsers);
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load all users from file
     */
    public static Map<String, User> loadUsers() {
        Map<String, User> users = new HashMap<>();
        
        try {
            Map<String, String[]> legacyUsers = readFromFile(USERS_FILE, 
                new TypeToken<Map<String, String[]>>(){}.getType());
            
            if (legacyUsers != null) {
                for (Map.Entry<String, String[]> entry : legacyUsers.entrySet()) {
                    String username = entry.getKey();
                    String email = entry.getValue().length > 1 ? entry.getValue()[1] : "";
                    users.put(username, new User(username, email));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Save user academic data
     */
    public static boolean saveUserData(Map<String, User> users) {
        try {
            // Convert User objects to nested structure for academic data
            Map<String, Map<String, Map<String, CourseData>>> userData = new HashMap<>();
            
            for (Map.Entry<String, User> userEntry : users.entrySet()) {
                String username = userEntry.getKey();
                User user = userEntry.getValue();
                
                Map<String, Map<String, CourseData>> userSemesters = new HashMap<>();
                for (Map.Entry<String, Semester> semesterEntry : user.getSemesters().entrySet()) {
                    String semesterName = semesterEntry.getKey();
                    Semester semester = semesterEntry.getValue();
                    
                    Map<String, CourseData> semesterCourses = new HashMap<>();
                    for (Map.Entry<String, Course> courseEntry : semester.getCourses().entrySet()) {
                        String courseName = courseEntry.getKey();
                        Course course = courseEntry.getValue();
                        
                        // Convert Course to CourseData for JSON serialization
                        CourseData courseData = new CourseData(course);
                        semesterCourses.put(courseName, courseData);
                    }
                    userSemesters.put(semesterName, semesterCourses);
                }
                userData.put(username, userSemesters);
            }
            
            return writeToFile(USERDATA_FILE, userData);
        } catch (Exception e) {
            System.err.println("Error saving user data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load user academic data
     */
    public static void loadUserData(Map<String, User> users) {
        try {
            Map<String, Map<String, Map<String, CourseData>>> userData = readFromFile(USERDATA_FILE,
                new TypeToken<Map<String, Map<String, Map<String, CourseData>>>>(){}.getType());
            
            if (userData != null) {
                for (Map.Entry<String, Map<String, Map<String, CourseData>>> userEntry : userData.entrySet()) {
                    String username = userEntry.getKey();
                    User user = users.get(username);
                    
                    if (user != null) {
                        for (Map.Entry<String, Map<String, CourseData>> semesterEntry : userEntry.getValue().entrySet()) {
                            String semesterName = semesterEntry.getKey();
                            
                            // Create or get semester
                            Semester semester = user.getSemester(semesterName);
                            if (semester == null) {
                                semester = new Semester(semesterName);
                                user.addSemester(semester);
                            }
                            
                            // Load courses into semester
                            for (Map.Entry<String, CourseData> courseEntry : semesterEntry.getValue().entrySet()) {
                                String courseName = courseEntry.getKey();
                                CourseData courseData = courseEntry.getValue();
                                
                                // Convert CourseData to Course
                                Course course = courseData.toCourse();
                                course.setName(courseName);
                                semester.addCourse(course);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user data: " + e.getMessage());
        }
    }
    
    /**
     * Save username change timestamps
     */
    public static boolean saveUsernameChanges(Map<String, Long> usernameChanges) {
        return writeToFile(USERNAME_CHANGES_FILE, usernameChanges);
    }
    
    /**
     * Load username change timestamps
     */
    public static Map<String, Long> loadUsernameChanges() {
        Map<String, Long> changes = readFromFile(USERNAME_CHANGES_FILE,
            new TypeToken<Map<String, Long>>(){}.getType());
        return changes != null ? changes : new HashMap<>();
    }
    
    /**
     * Save semester order data
     */
    public static boolean saveSemesterOrder(Map<String, Map<String, Integer>> semesterOrder) {
        return writeToFile(SEMESTER_ORDER_FILE, semesterOrder);
    }
    
    /**
     * Load semester order data
     */
    public static Map<String, Map<String, Integer>> loadSemesterOrder() {
        Map<String, Map<String, Integer>> order = readFromFile(SEMESTER_ORDER_FILE,
            new TypeToken<Map<String, Map<String, Integer>>>(){}.getType());
        return order != null ? order : new HashMap<>();
    }
    
    /**
     * Save user preferences (dark mode, etc.)
     */
    public static boolean saveUserPreferences(Map<String, UserPreferences> preferences) {
        return writeToFile(USER_PREFS_FILE, preferences);
    }
    
    /**
     * Load user preferences
     */
    public static Map<String, UserPreferences> loadUserPreferences() {
        Map<String, UserPreferences> prefs = readFromFile(USER_PREFS_FILE,
            new TypeToken<Map<String, UserPreferences>>(){}.getType());
        return prefs != null ? prefs : new HashMap<>();
    }
    
    // ===== GENERIC FILE OPERATIONS =====
    
    /**
     * Write object to JSON file
     */
    private static boolean writeToFile(String filePath, Object data) {
        try {
            ensureDataDirectory();
            try (FileWriter writer = new FileWriter(filePath)) {
                gson.toJson(data, writer);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Read object from JSON file
     */
    private static <T> T readFromFile(String filePath, Type type) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.err.println("Error reading from file " + filePath + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if a data file exists
     */
    public static boolean dataFileExists(String fileName) {
        return new File(DATA_DIR + File.separator + fileName).exists();
    }
    
    /**
     * Get data directory path
     */
    public static String getDataDirectory() {
        return DATA_DIR;
    }
    
    /**
     * Backup all data files
     */
    public static boolean backupData(String backupDir) {
        try {
            File backup = new File(backupDir);
            if (!backup.exists() && !backup.mkdirs()) {
                return false;
            }
            
            File dataDirectory = new File(DATA_DIR);
            if (!dataDirectory.exists()) {
                return true; // Nothing to backup
            }
            
            File[] files = dataDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        copyFile(file, new File(backup, file.getName()));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error backing up data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Copy a file
     */
    private static void copyFile(File source, File destination) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(destination)) {
            
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
    
    // ===== DATA TRANSFER OBJECTS =====
    
    /**
     * CourseData - JSON serializable representation of Course
     */
    public static class CourseData {
        private Map<String, List<Assignment>> assignments;
        private Map<String, Integer> categoryWeights;
        private List<Double> gradeHistory;
        private int credits;
        
        public CourseData() {
            this.assignments = new HashMap<>();
            this.categoryWeights = new HashMap<>();
            this.gradeHistory = new ArrayList<>();
            this.credits = 3;
        }
        
        public CourseData(Course course) {
            this.assignments = course.getAssignments();
            this.categoryWeights = course.getCategoryWeights();
            this.gradeHistory = course.getGradeHistory();
            this.credits = course.getCredits();
        }
        
        public Course toCourse() {
            Course course = new Course();
            course.setCredits(credits);
            course.setCategoryWeights(categoryWeights);
            
            // Add assignments
            for (Map.Entry<String, List<Assignment>> entry : assignments.entrySet()) {
                for (Assignment assignment : entry.getValue()) {
                    course.addAssignment(assignment);
                }
            }
            
            // Set grade history
            for (Double grade : gradeHistory) {
                course.getGradeHistory().add(grade);
            }
            
            return course;
        }
        
        // Getters and setters for JSON serialization
        public Map<String, List<Assignment>> getAssignments() { return assignments; }
        public void setAssignments(Map<String, List<Assignment>> assignments) { this.assignments = assignments; }
        
        public Map<String, Integer> getCategoryWeights() { return categoryWeights; }
        public void setCategoryWeights(Map<String, Integer> categoryWeights) { this.categoryWeights = categoryWeights; }
        
        public List<Double> getGradeHistory() { return gradeHistory; }
        public void setGradeHistory(List<Double> gradeHistory) { this.gradeHistory = gradeHistory; }
        
        public int getCredits() { return credits; }
        public void setCredits(int credits) { this.credits = credits; }
    }
    
    /**
     * UserPreferences - User settings and preferences
     */
    public static class UserPreferences {
        private boolean darkMode;
        private String defaultSemester;
        private Map<String, Object> customSettings;
        
        public UserPreferences() {
            this.darkMode = false;
            this.defaultSemester = "Semester 1";
            this.customSettings = new HashMap<>();
        }
        
        // Getters and setters
        public boolean isDarkMode() { return darkMode; }
        public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }
        
        public String getDefaultSemester() { return defaultSemester; }
        public void setDefaultSemester(String defaultSemester) { this.defaultSemester = defaultSemester; }
        
        public Map<String, Object> getCustomSettings() { return customSettings; }
        public void setCustomSettings(Map<String, Object> customSettings) { this.customSettings = customSettings; }
    }
    
    /**
     * Get file statistics
     */
    public static Map<String, Object> getFileStatistics() {
        Map<String, Object> stats = new HashMap<>();
        File dataDir = new File(DATA_DIR);
        
        if (dataDir.exists() && dataDir.isDirectory()) {
            File[] files = dataDir.listFiles();
            if (files != null) {
                stats.put("totalFiles", files.length);
                long totalSize = 0;
                Map<String, Long> fileSizes = new HashMap<>();
                
                for (File file : files) {
                    if (file.isFile()) {
                        long size = file.length();
                        totalSize += size;
                        fileSizes.put(file.getName(), size);
                    }
                }
                
                stats.put("totalSizeBytes", totalSize);
                stats.put("fileSizes", fileSizes);
                stats.put("dataDirectory", dataDir.getAbsolutePath());
            }
        } else {
            stats.put("totalFiles", 0);
            stats.put("totalSizeBytes", 0L);
            stats.put("dataDirectory", "Not found");
        }
        
        return stats;
    }
}