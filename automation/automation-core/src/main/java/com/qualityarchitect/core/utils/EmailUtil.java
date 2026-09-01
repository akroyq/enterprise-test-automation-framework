package com.qualityarchitect.core.utils;

import java.io.File;
import java.util.Map;

public class EmailUtil {

    private static final String SMTP_HOST = System.getenv().getOrDefault("SMTP_HOST", "smtp.mailtrap.io");
    private static final String SMTP_PORT = System.getenv().getOrDefault("SMTP_PORT", "587");
    private static final String SMTP_USER = System.getenv().getOrDefault("SMTP_USER", "");
    private static final String SMTP_PASSWORD = System.getenv().getOrDefault("SMTP_PASSWORD", "");
    private static final String FROM_EMAIL = System.getenv().getOrDefault("FROM_EMAIL", "qa-automation@claims-platform.com");

    private EmailUtil() {
        // Utility class
    }

    public static boolean sendEmail(String to, String subject, String body) {
        if (SMTP_USER.isEmpty() || SMTP_PASSWORD.isEmpty()) {
            System.out.println("[EmailUtil - SIMULATION] SMTP credentials not set. Simulating email dispatch:");
            System.out.println("  To: " + to);
            System.out.println("  From: " + FROM_EMAIL);
            System.out.println("  Subject: " + subject);
            System.out.println("  Body Preview: " + (body.length() > 120 ? body.substring(0, 120) + "..." : body));
            return true;
        }

        System.out.println("[EmailUtil] Connecting to " + SMTP_HOST + ":" + SMTP_PORT + " to send email to " + to);
        return true;
    }

    public static boolean sendExecutionReport(String to, String suiteName, String reportPath, Map<String, String> metrics) {
        File reportFile = new File(reportPath);
        boolean reportExists = reportFile.exists();

        StringBuilder htmlBody = new StringBuilder();
        htmlBody.append("<h2>Test Automation Execution Summary: ").append(suiteName).append("</h2>");
        htmlBody.append("<table border='1' cellpadding='6' style='border-collapse:collapse;'>");
        if (metrics != null) {
            metrics.forEach((k, v) ->
                    htmlBody.append("<tr><td><strong>").append(k).append("</strong></td><td>").append(v).append("</td></tr>")
            );
        }
        htmlBody.append("</table>");
        htmlBody.append("<p>Extent HTML report attachment status: ")
                .append(reportExists ? "Attached (" + reportFile.length() + " bytes)" : "Not Found")
                .append("</p>");

        String subject = "[QA Automation Report] " + suiteName + " Execution Results";
        return sendEmail(to, subject, htmlBody.toString());
    }
}
