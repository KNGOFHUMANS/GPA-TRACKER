import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * PasswordResetStore - Manages password reset tokens and persistence
 * Part of the refactored architecture for CollegeGPATracker
 */
public class PasswordResetStore {
    private static Map<String, String> tokenToUser = new HashMap<>();
    private static String filePath = "";
    private static final Gson gson = new Gson();
    
    /**
     * Initialize the password reset store with file path
     */
    public static void init(String resetTokensFile) {
        filePath = resetTokensFile;
        loadTokensFromFile();
    }
    
    /**
     * Generate and immediately persist a token for a user
     */
    public static String issueTokenFor(String username) {
        String token = generateCode();
        tokenToUser.put(token, username);
        persistTokens();
        return token;
    }
    
    /**
     * Generate a token in memory but do not persist it yet
     * Used for email-first workflow where token is only saved after successful email send
     */
    public static String generateTokenFor(String username) {
        return generateCode();
    }
    
    /**
     * Persist a previously generated token mapped to user
     * Called after successful email send in email-first workflow
     */
    public static void persistToken(String token, String username) {
        tokenToUser.put(token, username);
        persistTokens();
    }
    
    /**
     * Consume (use and remove) a reset token
     * @param token The reset token to consume
     * @return Username associated with token, or null if token is invalid/expired
     */
    public static String consume(String token) {
        String username = tokenToUser.remove(token);
        if (username != null) {
            persistTokens(); // Save the removal
        }
        return username;
    }
    
    /**
     * Check if a token exists without consuming it
     */
    public static boolean isValidToken(String token) {
        return tokenToUser.containsKey(token);
    }
    
    /**
     * Get username for a token without consuming it
     */
    public static String getUserForToken(String token) {
        return tokenToUser.get(token);
    }
    
    /**
     * Remove all tokens for a specific user
     */
    public static void invalidateTokensForUser(String username) {
        tokenToUser.entrySet().removeIf(entry -> entry.getValue().equals(username));
        persistTokens();
    }
    
    /**
     * Get count of active tokens
     */
    public static int getTokenCount() {
        return tokenToUser.size();
    }
    
    /**
     * Clear all tokens (for testing/cleanup)
     */
    public static void clearAllTokens() {
        tokenToUser.clear();
        persistTokens();
    }
    
    /**
     * Generate a 6-digit numeric reset code
     */
    private static String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit number
        return String.valueOf(code);
    }
    
    /**
     * Save tokens to file
     */
    private static void persistTokens() {
        if (filePath.isEmpty()) {
            return; // Not initialized
        }
        
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(tokenToUser, writer);
        } catch (IOException e) {
            System.err.println("Error saving password reset tokens: " + e.getMessage());
        }
    }
    
    /**
     * Load tokens from file
     */
    private static void loadTokensFromFile() {
        if (filePath.isEmpty()) {
            return; // Not initialized
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            // Create empty file
            persistTokens();
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            Map<String, String> loadedTokens = gson.fromJson(reader, 
                new TypeToken<Map<String, String>>(){}.getType());
            
            if (loadedTokens != null) {
                tokenToUser = loadedTokens;
            } else {
                tokenToUser = new HashMap<>();
            }
        } catch (IOException e) {
            System.err.println("Error loading password reset tokens: " + e.getMessage());
            tokenToUser = new HashMap<>();
        }
    }
    
    /**
     * Get statistics about reset tokens
     */
    public static Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTokens", tokenToUser.size());
        stats.put("filePath", filePath);
        stats.put("isInitialized", !filePath.isEmpty());
        
        // Count tokens per user
        Map<String, Integer> tokensPerUser = new HashMap<>();
        for (String username : tokenToUser.values()) {
            tokensPerUser.put(username, tokensPerUser.getOrDefault(username, 0) + 1);
        }
        stats.put("tokensPerUser", tokensPerUser);
        
        return stats;
    }
    
    @Override
    public String toString() {
        return String.format("PasswordResetStore{tokens=%d, initialized=%b}", 
            tokenToUser.size(), !filePath.isEmpty());
    }
}