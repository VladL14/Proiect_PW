package com.diceduel.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Salted, iterated password hashing based on PBKDF2WithHmacSHA256.
 *
 * <p>Passwords are never stored in clear text. The persisted value has the
 * shape {@code base64(salt):base64(hash)} so verification is fully
 * self-contained and no extra column is required. Using the JDK primitives
 * keeps the dependency surface small while still providing a modern,
 * brute-force resistant scheme.
 */
@Component
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Hashes a raw password together with a freshly generated random salt.
     *
     * @param rawPassword clear-text password
     * @return encoded {@code salt:hash} value safe to persist
     */
    public String hash(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a raw password against a previously stored encoded value.
     *
     * @param rawPassword clear-text password supplied at login
     * @param encoded     stored {@code salt:hash} value
     * @return true when the password matches
     */
    public boolean matches(String rawPassword, String encoded) {
        if (rawPassword == null || encoded == null || !encoded.contains(":")) {
            return false;
        }
        String[] parts = encoded.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        byte[] actual = pbkdf2(rawPassword.toCharArray(), salt);
        return constantTimeEquals(expected, actual);
    }

    private byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (java.security.NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    private boolean constantTimeEquals(byte[] left, byte[] right) {
        if (left.length != right.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length; i++) {
            result |= left[i] ^ right[i];
        }
        return result == 0;
    }
}
