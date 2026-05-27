package br.com.fiap.restauranteapi.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private static final int SALT_SIZE = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[SALT_SIZE];
        SECURE_RANDOM.nextBytes(salt);
        byte[] digest = digest(salt, password);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(digest);
    }

    public boolean matches(String rawPassword, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedDigest = Base64.getDecoder().decode(parts[1]);
        byte[] actualDigest = digest(salt, rawPassword);
        return MessageDigest.isEqual(expectedDigest, actualDigest);
    }

    private byte[] digest(byte[] salt, String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(salt);
            return messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponivel.", exception);
        }
    }
}
