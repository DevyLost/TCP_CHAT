package de.DevyLost;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import java.security.SecureRandom;
import java.util.Base64;


public class PasswordHasher {
    private static final SecureRandom generator = new SecureRandom();

    public static final String ALLOWEDCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!=()&+-*_:;,.#{}[]$§";

    protected static String secureSalt(int length){
        if(length > 0) {
            byte[] salt = new byte[length];
            generator.nextBytes(salt);
            return Base64.getEncoder().encodeToString(salt);
        }else {
            return "";
        }
    }

    protected static String secureHash(char[] password, String csalt, int iter){
        byte[] saltbytes = csalt.getBytes();
        String hash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] passbytes = new byte[password.length];
            for (int n = 0; n < password.length; n++) {
                passbytes[n] = ((byte) password[n]);
            }
            Arrays.fill(password, Character.MIN_VALUE);
            for(int i = 0; i < iter; i++) {
                md.update(saltbytes);
                passbytes = md.digest(passbytes);
                md.reset();
            }
            hash = Base64.getEncoder().encodeToString(passbytes);
            Arrays.fill(passbytes, Byte.MIN_VALUE);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
