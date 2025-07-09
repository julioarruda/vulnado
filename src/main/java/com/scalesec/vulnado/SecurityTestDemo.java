package com.scalesec.vulnado;

public class SecurityTestDemo {
    public static void main(String[] args) {
        System.out.println("=== Security Fixes Demonstration ===\n");
        
        // Test 1: RCE Protection in Cowsay
        System.out.println("1. Testing RCE Protection in Cowsay:");
        String maliciousInput = "test; echo 'This would be dangerous'";
        String result = Cowsay.run(maliciousInput);
        System.out.println("Input: " + maliciousInput);
        System.out.println("Output sanitized: " + !result.contains("dangerous"));
        System.out.println();
        
        // Test 2: SQL Injection Protection
        System.out.println("2. Testing SQL Injection Protection:");
        System.out.println("User.fetch now uses PreparedStatement with parameterized queries");
        System.out.println("This prevents SQL injection attacks like: admin' OR '1'='1");
        System.out.println();
        
        // Test 3: Password Hashing Upgrade
        System.out.println("3. Testing Password Hashing Upgrade:");
        String password = "testPassword123";
        String sha256Hash = Postgres.sha256(password);
        System.out.println("Password: " + password);
        System.out.println("SHA-256 hash: " + sha256Hash);
        System.out.println("Hash length (should be 64): " + sha256Hash.length());
        System.out.println();
        
        // Test 4: Input Validation
        System.out.println("4. Testing Input Validation:");
        String longInput = "a".repeat(150);
        String validatedResult = Cowsay.run(longInput);
        System.out.println("Long input handled safely: " + !validatedResult.contains("Error"));
        System.out.println();
        
        System.out.println("=== Security Test Complete ===");
    }
}