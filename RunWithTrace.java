public class RunWithTrace {
    public static void main(String[] args) {
        // Print any uncaught exceptions from any thread (including EDT)
        Thread.setDefaultUncaughtExceptionHandler((thread, err) -> {
            System.err.println("Uncaught exception in thread " + thread.getName());
            err.printStackTrace();
        });

        try {
            // Call the application's main entrypoint
            CollegeGPATracker.main(args);
        } catch (Throwable t) {
            System.err.println("Exception thrown from main:");
            t.printStackTrace();
        }
    }
}
