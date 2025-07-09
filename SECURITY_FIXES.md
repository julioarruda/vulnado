# Security Vulnerabilities Fixed

This document summarizes the critical security vulnerabilities that were identified and fixed in the Vulnado application.

## 1. SQL Injection (CVE-2017-5645 class)

**Location**: `src/main/java/com/scalesec/vulnado/User.java`

**Issue**: The `User.fetch()` method used string concatenation to build SQL queries, making it vulnerable to SQL injection attacks.

**Original vulnerable code**:
```java
String query = "select * from users where username = '" + un + "' limit 1";
```

**Fix**: Replaced with parameterized PreparedStatement:
```java
String query = "select * from users where username = ? limit 1";
pstmt = cxn.prepareStatement(query);
pstmt.setString(1, un.trim());
```

## 2. Remote Code Execution (RCE)

**Location**: `src/main/java/com/scalesec/vulnado/Cowsay.java`

**Issue**: The cowsay function directly concatenated user input into shell commands without sanitization.

**Original vulnerable code**:
```java
String cmd = "/usr/games/cowsay '" + input + "'";
processBuilder.command("bash", "-c", cmd);
```

**Fix**: 
- Added input sanitization to remove dangerous characters
- Implemented input length limits
- Used ProcessBuilder with separate arguments instead of shell commands:
```java
String sanitizedInput = input.replaceAll("[;&|`$()\\\\<>\"']", "");
processBuilder.command("/usr/games/cowsay", sanitizedInput);
```

## 3. Server-Side Request Forgery (SSRF)

**Location**: `src/main/java/com/scalesec/vulnado/LinkLister.java`

**Issue**: Insufficient validation of URLs allowed attackers to access internal services.

**Fix**: Enhanced URL validation with:
- Protocol restrictions (only HTTP/HTTPS)
- Comprehensive private IP blocking (10.x, 172.x, 192.168.x, 169.254.x)
- Localhost blocking
- Internal hostname blocking

## 4. Missing Authentication

**Location**: `src/main/java/com/scalesec/vulnado/CommentsController.java`

**Issue**: Comment creation and deletion endpoints didn't verify authentication tokens.

**Fix**: Added `User.assertAuth(secret, token);` calls to both endpoints.

## 5. Cross-Site Scripting (XSS)

**Location**: `client/js/index.js`

**Issue**: Comment body was only filtered for `<script>` tags, allowing other XSS vectors.

**Fix**: Implemented proper HTML encoding:
```javascript
var safeComment = {
  username: $('<div>').text(comment.username).html(),
  body: $('<div>').text(comment.body).html(),
  // ...
};
```

## 6. Weak Password Hashing

**Location**: `src/main/java/com/scalesec/vulnado/Postgres.java`

**Issue**: Used MD5 hashing which is cryptographically broken.

**Fix**: Upgraded to SHA-256:
```java
MessageDigest md = MessageDigest.getInstance("SHA-256");
```

## Summary

All major security vulnerabilities from the OWASP Top 10 that were present in this intentionally vulnerable application have been addressed:

- **A03:2021 - Injection**: Fixed SQL injection
- **A05:2021 - Security Misconfiguration**: Added proper authentication checks  
- **A07:2021 - Cross-Site Scripting**: Improved XSS protection
- **A10:2021 - Server-Side Request Forgery**: Enhanced SSRF protection
- **A02:2021 - Cryptographic Failures**: Upgraded from MD5 to SHA-256

The application now follows security best practices including input validation, parameterized queries, proper authentication, and secure cryptographic algorithms.