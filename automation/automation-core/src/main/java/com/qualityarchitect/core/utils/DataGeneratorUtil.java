package com.qualityarchitect.core.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DataGeneratorUtil {

    private static final Random RANDOM = new Random();
    private static final List<String> FIRST_NAMES = List.of(
            "Alex", "Jordan", "Taylor", "Morgan", "Sam", "Chris", "Pat", "Riley", "Casey", "Avery"
    );
    private static final List<String> LAST_NAMES = List.of(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Anderson", "Taylor"
    );
    private static final List<String> POLICY_TYPES = List.of(
            "auto", "home", "health", "travel", "commercial"
    );
    private static final List<String> INCIDENT_TYPES = List.of(
            "Collision with vehicle", "Windshield rock chip", "Rear-end collision at traffic light",
            "Fender bender in parking lot", "Water damage from pipe burst", "Hail damage on roof"
    );

    private DataGeneratorUtil() {
        // Utility class
    }

    public static String generatePolicyId() {
        return "POL-" + (1000 + RANDOM.nextInt(9000));
    }

    public static String generatePolicyType() {
        return POLICY_TYPES.get(RANDOM.nextInt(POLICY_TYPES.size()));
    }

    public static int generateClaimAmount(int min, int max) {
        if (min >= max) {
            return min;
        }
        return min + RANDOM.nextInt(max - min + 1);
    }

    public static int generateDeductible(int claimAmount) {
        int[] commonDeductibles = {50, 100, 250, 500, 1000};
        int chosen = commonDeductibles[RANDOM.nextInt(commonDeductibles.length)];
        return Math.min(chosen, claimAmount);
    }

    public static String generateFirstName() {
        return FIRST_NAMES.get(RANDOM.nextInt(FIRST_NAMES.size()));
    }

    public static String generateLastName() {
        return LAST_NAMES.get(RANDOM.nextInt(LAST_NAMES.size()));
    }

    public static String generateFullName() {
        return generateFirstName() + " " + generateLastName();
    }

    public static String generateEmail() {
        return "claimant." + System.currentTimeMillis() % 100000 + "@insurance-test.com";
    }

    public static String generatePhoneNumber() {
        return String.format("+1-555-%03d-%04d", RANDOM.nextInt(1000), RANDOM.nextInt(10000));
    }

    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public static String generateIncidentDescription() {
        return INCIDENT_TYPES.get(RANDOM.nextInt(INCIDENT_TYPES.size()));
    }

    public static String generateDate(int daysOffset) {
        return LocalDate.now().plusDays(daysOffset).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String generateRandomAlphaNumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
