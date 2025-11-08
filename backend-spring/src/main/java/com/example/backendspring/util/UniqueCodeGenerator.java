package com.example.backendspring.util;

import java.security.SecureRandom;

/**
 * 유저 고유 코드 생성기
 * a-z, A-Z, 0-9 조합으로 10자리 랜덤 코드 생성
 */
public class UniqueCodeGenerator {
    
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * 유저 고유 코드 생성
     * @return 10자리 랜덤 코드 (예: "aB3xK9pQr2")
     */
    public static String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }
}

