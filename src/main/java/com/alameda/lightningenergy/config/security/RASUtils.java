package com.alameda.lightningenergy.config.security;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

@NoArgsConstructor
@Component
public class RASUtils {













    public String encrypt(String content) throws IllegalBlockSizeException, BadPaddingException,  InvalidKeyException {

        return content;
    }

    public String decrypt(String encrypted) throws  InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        return encrypted;
    }
}