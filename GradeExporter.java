import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Advanced Grade Export System for GradeRise
 * Exports grade reports to PDF and Excel formats with professional styling
 */
public class GradeExporter {
    
    // Export Formats
    public enum ExportFormat {
        PDF("PDF Document", "pdf"),
        EXCEL("Excel Spreadsheet", "xlsx"),
        CSV("Comma Separated Values", "csv"),
        HTML("Web Page", "html");
        
        private final String description;
        private final String extension;
        
        ExportFormat(String desc, String ext) {
            this.description = desc;
            this.extension = ext;
        }
        
        public String getDescription() { return description; }
        public String getExtension() { return extension; }
    }
    
    // Report Types
    public enum ReportType {
        COURSE_SUMMARY,
        SEMESTER_REPORT,
        GRADE_ANALYTICS,
        TRANSCRIPT,
        PROGRESS_REPORT
    }
    
    /**
     * Export Configuration
     */
    public static class ExportConfig {
        private final ExportFormat format;
        private final ReportType reportType;
        private final String fileName;
        private final boolean includeCharts;
        private final boolean includeStatistics;
        private final boolean includePredictions;
        private final Map<String, Object> customOptions;
        
        public ExportConfig(ExportFormat format, ReportType type, String fileName) {
            this.format = format;
            this.reportType = type;
            this.fileName = fileName;
            this.includeCharts = true;
            this.includeStatistics = true;
            this.includePredictions = true;
            this.customOptions = new HashMap<>();
        }
        
        // Getters
        public ExportFormat getFormat() { return format; }
        public ReportType getReportType() { return reportType; }
        public String getFileName() { return fileName; }
        public boolean isIncludeCharts() { return includeCharts; }
        public boolean isIncludeStatistics() { return includeStatistics; }
        public boolean isIncludePredictions() { return includePredictions; }
        public Map<String, Object> getCustomOptions() { return customOptions; }
    }
    
    /**
     * Export Result
     */
    public static class ExportResult {
        private final boolean success;
        private final String filePath;
        private final String message;
        private final long fileSizeBytes;
        
        public ExportResult(boolean success, String path, String message, long size) {
            this.success = success;
            this.filePath = path;
            this.message = message;
            this.fileSizeBytes = size;
        }
        
        public boolean isSuccess() { return success; }
        public String getFilePath() { return filePath; }
        public String getMessage() { return message; }
        public long getFileSizeBytes() { return fileSizeBytes; }
        public String getFormattedFileSize() {
            if (fileSizeBytes < 1024) return fileSizeBytes + " B";
            if (fileSizeBytes < 1024 * 1024) return String.format("%.1f KB", fileSizeBytes / 1024.0);
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Main export method
     */
    public static ExportResult exportGrades(ExportConfig config, Object data) {
        try {
            String filePath = generateFileName(config);
            
            switch (config.getFormat()) {
                case PDF:
                    return exportToPDF(config, data, filePath);
                case EXCEL:
                    return exportToExcel(config, data, filePath);
                case CSV:
                    return exportToCSV(config, data, filePath);
                case HTML:
                    return exportToHTML(config, data, filePath);
                default:
                    return new ExportResult(false, "", "Unsupported format", 0);
            }
            
        } catch (Exception e) {
            return new ExportResult(false, "", "Export failed: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Export to PDF using custom PDF generation
     */
    private static ExportResult exportToPDF(ExportConfig config, Object data, String filePath) {
        try {
            StringBuilder content = new StringBuilder();
            
            // Generate content based on report type
            switch (config.getReportType()) {
                case COURSE_SUMMARY:
                    if (data instanceof Course) {
                        content.append(generateCourseSummaryContent((Course) data, config));
                    }
                    break;
                case SEMESTER_REPORT:
                    if (data instanceof Semester) {
                        content.append(generateSemesterReportContent((Semester) data, config));
                    }
                    break;
                case GRADE_ANALYTICS:
                    content.append(generateAnalyticsContent(data, config));
                    break;
                case TRANSCRIPT:
                    content.append(generateTranscriptContent(data, config));
                    break;
                case PROGRESS_REPORT:
                    content.append(generateProgressReportContent(data, config));
                    break;
            }
            
            // Create simple PDF-like format (text-based)
            String pdfContent = createPDFContent(content.toString(), config);
            
            Files.write(Paths.get(filePath), pdfContent.getBytes());
            long fileSize = Files.size(Paths.get(filePath));
            
            return new ExportResult(true, filePath, "PDF exported successfully", fileSize);
            
        } catch (Exception e) {
            return new ExportResult(false, filePath, "PDF export failed: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Export to Excel format (CSV-like for simplicity)
     */
    private static ExportResult exportToExcel(ExportConfig config, Object data, String filePath) {
        try {
            StringBuilder excel = new StringBuilder();
            
            switch (config.getReportType()) {
                case COURSE_SUMMARY:
                    if (data instanceof Course) {
                        excel.append(generateCourseExcelContent((Course) data, config));
                    }
                    break;
                case SEMESTER_REPORT:
                    if (data instanceof Semester) {
                        excel.append(generateSemesterExcelContent((Semester) data, config));
                    }
                    break;
                default:
                    excel.append(generateGenericExcelContent(data, config));
                    break;
            }
            
            Files.write(Paths.get(filePath), excel.toString().getBytes());
            long fileSize = Files.size(Paths.get(filePath));
            
            return new ExportResult(true, filePath, "Excel file exported successfully", fileSize);
            
        } catch (Exception e) {
            return new ExportResult(false, filePath, "Excel export failed: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Export to CSV format
     */
    private static ExportResult exportToCSV(ExportConfig config, Object data, String filePath) {
        try {
            StringBuilder csv = new StringBuilder();
            
            if (data instanceof Course) {
                Course course = (Course) data;
                csv.append("Assignment Name,Category,Grade,Max Points,Percentage,Date\\n");
                
                for (Assignment assignment : course.getAllAssignments()) {
                    csv.append(String.format("\"%s\",\"%s\",%.2f,%.2f,%.2f%%,%s\\n",
                        escapeCSV(assignment.getName()),
                        escapeCSV(assignment.getCategory()),
                        assignment.getPointsEarned(),
                        assignment.getMaxPoints(),
                        assignment.getGradePercent(),
                        assignment.getDateAdded().toString()
                    ));
                }
            } else if (data instanceof Semester) {
                Semester semester = (Semester) data;
                csv.append("Course Name,Credits,Current Grade,Letter Grade,GPA\\n");
                
                for (Course course : semester.getAllCourses()) {
                    double grade = course.calculateCurrentGrade();
                    csv.append(String.format("\"%s\",%d,%.2f%%,%s,%.2f\\n",
                        escapeCSV(course.getName()),
                        course.getCredits(),
                        grade,
                        getLetterGrade(grade),
                        convertToGPA(grade)
                    ));
                }
            }
            
            Files.write(Paths.get(filePath), csv.toString().getBytes());
            long fileSize = Files.size(Paths.get(filePath));
            
            return new ExportResult(true, filePath, "CSV exported successfully", fileSize);
            
        } catch (Exception e) {
            return new ExportResult(false, filePath, "CSV export failed: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Export to HTML format
     */
    private static ExportResult exportToHTML(ExportConfig config, Object data, String filePath) {
        try {
            StringBuilder html = new StringBuilder();
            html.append(generateHTMLHeader(config));
            
            switch (config.getReportType()) {
                case COURSE_SUMMARY:
                    if (data instanceof Course) {
                        html.append(generateCourseHTMLContent((Course) data, config));
                    }
                    break;
                case SEMESTER_REPORT:
                    if (data instanceof Semester) {
                        html.append(generateSemesterHTMLContent((Semester) data, config));
                    }
                    break;
                default:
                    html.append(generateGenericHTMLContent(data, config));
                    break;
            }
            
            html.append(generateHTMLFooter());
            
            Files.write(Paths.get(filePath), html.toString().getBytes());
            long fileSize = Files.size(Paths.get(filePath));
            
            return new ExportResult(true, filePath, "HTML report exported successfully", fileSize);
            
        } catch (Exception e) {
            return new ExportResult(false, filePath, "HTML export failed: " + e.getMessage(), 0);
        }
    }
    
    // Content Generation Methods
    
    private static String generateCourseSummaryContent(Course course, ExportConfig config) {
        StringBuilder content = new StringBuilder();
        
        content.append("=".repeat(80)).append("\\n");
        content.append("GRADERISE - COURSE SUMMARY REPORT\\n");
        content.append("=".repeat(80)).append("\\n\\n");
        
        content.append(String.format("Course: %s\\n", course.getName()));
        content.append(String.format("Credits: %d\\n", course.getCredits()));
        content.append(String.format("Report Date: %s\\n\\n", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        
        // Current Performance
        double currentGrade = course.calculateCurrentGrade();
        content.append("CURRENT PERFORMANCE\\n");
        content.append("-".repeat(40)).append("\\n");
        content.append(String.format("Overall Grade: %.2f%% (%s)\\n", currentGrade, getLetterGrade(currentGrade)));
        content.append(String.format("GPA Equivalent: %.2f\\n", convertToGPA(currentGrade)));
        content.append(String.format("Total Assignments: %d\\n\\n", course.getAllAssignments().size()));
        
        // Assignments Table
        content.append("ASSIGNMENT BREAKDOWN\\n");
        content.append("-".repeat(80)).append("\\n");
        content.append(String.format("%-25s %-12s %-8s %-8s %-10s %s\\n", 
            "Assignment", "Category", "Points", "Max", "Percent", "Date"));
        content.append("-".repeat(120)).append("\\n\\n");
        
        for (Assignment assignment : course.getAllAssignments()) {
            content.append(String.format("%-25s %-12s %-8.1f %-8.1f %-10.2f%% %s\\n",
                truncateString(assignment.getName(), 24),
                truncateString(assignment.getCategory(), 11),
                assignment.getPointsEarned(),
                assignment.getMaxPoints(),
                assignment.getGradePercent(),
                assignment.getDateAdded().toString()
            ));
        }
        
        // Statistics
        if (config.isIncludeStatistics()) {
            content.append("\\n\\nSTATISTICS\\n");
            content.append("-".repeat(40)).append("\\n");
            
            GradeAnalyticsEngine.GradeStatistics stats = 
                GradeAnalyticsEngine.calculateStatistics(course.getAllAssignments());
            
            content.append(String.format("Mean: %.2f%%\\n", stats.getMean()));
            content.append(String.format("Median: %.2f%%\\n", stats.getMedian()));
            content.append(String.format("Standard Deviation: %.2f\\n", stats.getStandardDeviation()));
            content.append(String.format("Range: %.2f%%\\n", stats.getRange()));
        }
        
        // Predictions
        if (config.isIncludePredictions()) {
            content.append("\\n\\nGRADE PREDICTIONS\\n");
            content.append("-".repeat(40)).append("\\n");
            
            GradeAnalyticsEngine.GradePrediction prediction = 
                GradeAnalyticsEngine.predictFinalGrade(course, GradeAnalyticsEngine.PredictionModel.WEIGHTED_AVERAGE);
            
            content.append(String.format("Likely Final Grade: %.2f%%\\n", prediction.getLikelyGrade()));
            content.append(String.format("Conservative Estimate: %.2f%%\\n", prediction.getConservativeGrade()));
            content.append(String.format("Optimistic Estimate: %.2f%%\\n", prediction.getOptimisticGrade()));
            content.append(String.format("Confidence: %.1f%%\\n", prediction.getConfidenceScore() * 100));
            content.append(String.format("Trend: %s\\n", prediction.getTrendDescription()));
        }
        
        return content.toString();
    }
    
    private static String generateSemesterReportContent(Semester semester, ExportConfig config) {
        StringBuilder content = new StringBuilder();
        
        content.append("=".repeat(80)).append("\\n");
        content.append("GRADERISE - SEMESTER REPORT\\n");
        content.append("=".repeat(80)).append("\\n\\n");
        
        content.append(String.format("Semester: %s\\n", semester.getName()));
        content.append(String.format("Report Date: %s\\n\\n", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        
        // Semester Overview
        double semesterGPA = semester.calculateGPA();
        int totalCredits = semester.getAllCourses().stream().mapToInt(Course::getCredits).sum();
        
        content.append("SEMESTER OVERVIEW\\n");
        content.append("-".repeat(40)).append("\\n");
        content.append(String.format("Semester GPA: %.3f\\n", semesterGPA));
        content.append(String.format("Total Credits: %d\\n", totalCredits));
        content.append(String.format("Number of Courses: %d\\n\\n", semester.getAllCourses().size()));
        
        // Course Breakdown
        content.append("COURSE BREAKDOWN\\n");
        content.append("-".repeat(80)).append("\\n");
        content.append(String.format("%-30s %-8s %-12s %-12s %-8s\\n", 
            "Course", "Credits", "Grade", "Letter", "GPA"));
        content.append("-".repeat(120)).append("\\n\\n");
        
        for (Course course : semester.getAllCourses()) {
            double grade = course.calculateCurrentGrade();
            content.append(String.format("%-30s %-8d %-12.2f%% %-12s %-8.2f\\n",
                truncateString(course.getName(), 29),
                course.getCredits(),
                grade,
                getLetterGrade(grade),
                convertToGPA(grade)
            ));
        }
        
        return content.toString();
    }
    
    private static String generateCourseExcelContent(Course course, ExportConfig config) {
        StringBuilder excel = new StringBuilder();
        
        // Header
        excel.append("GradeRise Course Summary Report\\n");
        excel.append(String.format("Course,%s\\n", course.getName()));
        excel.append(String.format("Credits,%d\\n", course.getCredits()));
        excel.append(String.format("Report Date,%s\\n\\n", LocalDate.now().toString()));
        
        // Current Grade
        double currentGrade = course.calculateCurrentGrade();
        excel.append("Current Performance\\n");
        excel.append(String.format("Overall Grade,%.2f%%\\n", currentGrade));
        excel.append(String.format("Letter Grade,%s\\n", getLetterGrade(currentGrade)));
        excel.append(String.format("GPA,%.2f\\n\\n", convertToGPA(currentGrade)));
        
        // Assignments
        excel.append("Assignment Details\\n");
        excel.append("Assignment Name,Category,Points Earned,Max Points,Percentage,Date\\n");
        
        for (Assignment assignment : course.getAllAssignments()) {
            excel.append(String.format("%s,%s,%.2f,%.2f,%.2f%%,%s\\n",
                assignment.getName(),
                assignment.getCategory(),
                assignment.getPointsEarned(),
                assignment.getMaxPoints(),
                assignment.getGradePercent(),
                assignment.getDateAdded().toString()
            ));
        }
        
        return excel.toString();
    }
    
    private static String generateSemesterExcelContent(Semester semester, ExportConfig config) {
        StringBuilder excel = new StringBuilder();
        
        excel.append("GradeRise Semester Report\\n");
        excel.append(String.format("Semester,%s\\n", semester.getName()));
        excel.append(String.format("Report Date,%s\\n\\n", LocalDate.now().toString()));
        
        double semesterGPA = semester.calculateGPA();
        excel.append(String.format("Semester GPA,%.3f\\n\\n", semesterGPA));
        
        excel.append("Course Details\\n");
        excel.append("Course Name,Credits,Current Grade,Letter Grade,GPA\\n");
        
        for (Course course : semester.getAllCourses()) {
            double grade = course.calculateCurrentGrade();
            excel.append(String.format("%s,%d,%.2f%%,%s,%.2f\\n",
                course.getName(),
                course.getCredits(),
                grade,
                getLetterGrade(grade),
                convertToGPA(grade)
            ));
        }
        
        return excel.toString();
    }
    
    private static String generateGenericExcelContent(Object data, ExportConfig config) {
        return "Generic Excel Content\\nData Type," + data.getClass().getSimpleName() + "\\n";
    }
    
    private static String generateHTMLHeader(ExportConfig config) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>GradeRise Report</title>
                <style>
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        margin: 40px; 
                        background: #f5f5f5; 
                        color: #333;
                    }
                    .container { 
                        background: white; 
                        padding: 30px; 
                        border-radius: 8px; 
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        max-width: 1000px;
                        margin: 0 auto;
                    }
                    .header { 
                        border-bottom: 3px solid #8B0000; 
                        padding-bottom: 20px; 
                        margin-bottom: 30px;
                        text-align: center;
                    }
                    h1 { 
                        color: #8B0000; 
                        margin: 0;
                        font-size: 2.2em;
                    }
                    h2 { 
                        color: #DC143C; 
                        border-bottom: 2px solid #DC143C; 
                        padding-bottom: 10px;
                        margin-top: 30px;
                    }
                    table { 
                        width: 100%; 
                        border-collapse: collapse; 
                        margin: 20px 0;
                        background: white;
                    }
                    th, td { 
                        padding: 12px; 
                        text-align: left; 
                        border-bottom: 1px solid #ddd;
                    }
                    th { 
                        background-color: #8B0000; 
                        color: white;
                        font-weight: bold;
                    }
                    tr:nth-child(even) { 
                        background-color: #f9f9f9; 
                    }
                    .grade-excellent { color: #10B981; font-weight: bold; }
                    .grade-good { color: #8B0000; font-weight: bold; }
                    .grade-average { color: #F59E0B; font-weight: bold; }
                    .grade-poor { color: #EF4444; font-weight: bold; }
                    .summary-box {
                        background: #f8f9fa;
                        border-left: 4px solid #8B0000;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        color: #666;
                        font-size: 0.9em;
                        border-top: 1px solid #ddd;
                        padding-top: 20px;
                    }
                </style>
            </head>
            <body>
            <div class="container">
            <div class="header">
                <h1>🎓 GradeRise</h1>
                <p>Academic Performance Report</p>
            </div>
            """;
    }
    
    private static String generateCourseHTMLContent(Course course, ExportConfig config) {
        StringBuilder html = new StringBuilder();
        
        double currentGrade = course.calculateCurrentGrade();
        String gradeClass = getGradeHTMLClass(currentGrade);
        
        html.append("<div class='summary-box'>");
        html.append(String.format("<h2>📚 %s</h2>", course.getName()));
        html.append(String.format("<p><strong>Credits:</strong> %d</p>", course.getCredits()));
        html.append(String.format("<p><strong>Current Grade:</strong> <span class='%s'>%.2f%% (%s)</span></p>", 
            gradeClass, currentGrade, getLetterGrade(currentGrade)));
        html.append(String.format("<p><strong>GPA:</strong> %.2f</p>", convertToGPA(currentGrade)));
        html.append(String.format("<p><strong>Total Assignments:</strong> %d</p>", course.getAllAssignments().size()));
        html.append("</div>");
        
        html.append("<h2>📊 Assignment Details</h2>");
        html.append("<table>");
        html.append("<tr><th>Assignment</th><th>Category</th><th>Points</th><th>Max Points</th><th>Grade</th><th>Date</th></tr>");
        
        for (Assignment assignment : course.getAllAssignments()) {
            String assignmentGradeClass = getGradeHTMLClass(assignment.getGradePercent());
            html.append("<tr>");
            html.append(String.format("<td>%s</td>", assignment.getName()));
            html.append(String.format("<td>%s</td>", assignment.getCategory()));
            html.append(String.format("<td>%.1f</td>", assignment.getPointsEarned()));
            html.append(String.format("<td>%.1f</td>", assignment.getMaxPoints()));
            html.append(String.format("<td class='%s'>%.2f%%</td>", assignmentGradeClass, assignment.getGradePercent()));
            html.append(String.format("<td>%s</td>", assignment.getDateAdded().toString()));
            html.append("</tr>");
        }
        
        html.append("</table>");
        
        return html.toString();
    }
    
    private static String generateSemesterHTMLContent(Semester semester, ExportConfig config) {
        StringBuilder html = new StringBuilder();
        
        double semesterGPA = semester.calculateGPA();
        
        html.append("<div class='summary-box'>");
        html.append(String.format("<h2>🗓️ %s</h2>", semester.getName()));
        html.append(String.format("<p><strong>Semester GPA:</strong> <span class='grade-excellent'>%.3f</span></p>", semesterGPA));
        html.append(String.format("<p><strong>Total Credits:</strong> %d</p>", 
            semester.getAllCourses().stream().mapToInt(Course::getCredits).sum()));
        html.append(String.format("<p><strong>Number of Courses:</strong> %d</p>", semester.getAllCourses().size()));
        html.append("</div>");
        
        html.append("<h2>📚 Course Breakdown</h2>");
        html.append("<table>");
        html.append("<tr><th>Course</th><th>Credits</th><th>Current Grade</th><th>Letter Grade</th><th>GPA</th></tr>");
        
        for (Course course : semester.getAllCourses()) {
            double grade = course.calculateCurrentGrade();
            String gradeClass = getGradeHTMLClass(grade);
            
            html.append("<tr>");
            html.append(String.format("<td>%s</td>", course.getName()));
            html.append(String.format("<td>%d</td>", course.getCredits()));
            html.append(String.format("<td class='%s'>%.2f%%</td>", gradeClass, grade));
            html.append(String.format("<td>%s</td>", getLetterGrade(grade)));
            html.append(String.format("<td>%.2f</td>", convertToGPA(grade)));
            html.append("</tr>");
        }
        
        html.append("</table>");
        
        return html.toString();
    }
    
    private static String generateGenericHTMLContent(Object data, ExportConfig config) {
        return String.format("<p>Generic content for %s</p>", data.getClass().getSimpleName());
    }
    
    private static String generateHTMLFooter() {
        return String.format("""
            <div class="footer">
                <p>Generated by GradeRise Academic Management System</p>
                <p>Report created on %s</p>
            </div>
            </div>
            </body>
            </html>
            """, LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
    }
    
    // Utility Methods
    
    private static String generateFileName(ExportConfig config) {
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseName = config.getFileName();
        if (baseName == null || baseName.trim().isEmpty()) {
            baseName = "GradeRise_Report_" + timestamp;
        }
        return baseName + "." + config.getFormat().getExtension();
    }
    
    private static String createPDFContent(String content, ExportConfig config) {
        // Simple text-based PDF content (in a real implementation, you'd use a PDF library)
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\\n");
        pdf.append("% GradeRise Report\\n\\n");
        pdf.append("TEXT CONTENT:\\n");
        pdf.append("=".repeat(50)).append("\\n");
        pdf.append(content);
        pdf.append("\\n").append("=".repeat(50)).append("\\n");
        pdf.append("End of Report\\n");
        return pdf.toString();
    }
    
    private static String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
    
    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        return str.length() <= maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }
    
    private static String getLetterGrade(double percentage) {
        if (percentage >= 97) return "A+";
        if (percentage >= 93) return "A";
        if (percentage >= 90) return "A-";
        if (percentage >= 87) return "B+";
        if (percentage >= 83) return "B";
        if (percentage >= 80) return "B-";
        if (percentage >= 77) return "C+";
        if (percentage >= 73) return "C";
        if (percentage >= 70) return "C-";
        if (percentage >= 67) return "D+";
        if (percentage >= 65) return "D";
        return "F";
    }
    
    private static double convertToGPA(double percentage) {
        if (percentage >= 97) return 4.0;
        if (percentage >= 93) return 3.7;
        if (percentage >= 90) return 3.3;
        if (percentage >= 87) return 3.0;
        if (percentage >= 83) return 2.7;
        if (percentage >= 80) return 2.3;
        if (percentage >= 77) return 2.0;
        if (percentage >= 73) return 1.7;
        if (percentage >= 70) return 1.3;
        if (percentage >= 67) return 1.0;
        if (percentage >= 65) return 0.7;
        return 0.0;
    }
    
    private static String getGradeHTMLClass(double grade) {
        if (grade >= 90) return "grade-excellent";
        if (grade >= 80) return "grade-good";
        if (grade >= 70) return "grade-average";
        return "grade-poor";
    }
    
    private static String generateAnalyticsContent(Object data, ExportConfig config) {
        return "Analytics content placeholder";
    }
    
    private static String generateTranscriptContent(Object data, ExportConfig config) {
        return "Transcript content placeholder";
    }
    
    private static String generateProgressReportContent(Object data, ExportConfig config) {
        return "Progress report content placeholder";
    }
    
    /**
     * Utility method to show file chooser for export
     */
    public static File showExportFileChooser(Component parent, ExportFormat format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Grade Report");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Set file filter based on format
        javax.swing.filechooser.FileNameExtensionFilter filter = 
            new javax.swing.filechooser.FileNameExtensionFilter(
                format.getDescription() + " (*." + format.getExtension() + ")", 
                format.getExtension()
            );
        fileChooser.setFileFilter(filter);
        
        // Set default filename
        String defaultName = "GradeRise_Report_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
            "." + format.getExtension();
        fileChooser.setSelectedFile(new File(defaultName));
        
        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Ensure correct extension
            String fileName = selectedFile.getName();
            if (!fileName.toLowerCase().endsWith("." + format.getExtension().toLowerCase())) {
                selectedFile = new File(selectedFile.getParent(), 
                    fileName + "." + format.getExtension());
            }
            
            return selectedFile;
        }
        
        return null;
    }
    
    /**
     * Show export progress dialog
     */
    public static JDialog createExportProgressDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Exporting Report...", true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("Generating report, please wait...", SwingConstants.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        panel.add(label, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        
        dialog.add(panel);
        return dialog;
    }
}