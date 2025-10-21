public class SendTest {
    public static void main(String[] args) {
        try {
            MailSender.sendEmail("malik.g.jones0415@gmail.com", "GPA Tracker — SMTP test", "This is a test email from your GPA Tracker app. If you received this, SMTP works.");
            System.out.println("Mail send call completed.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Send failed: " + e.getMessage());
        }
    }
}
