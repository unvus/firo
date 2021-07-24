package com.juvis.core.file.util;

import com.juvis.core.file.module.policy.DirectoryPathPolicy;
import com.juvis.core.file.module.service.FiroRegistry;
import com.juvis.core.file.module.service.domain.FiroCategory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecureNameUtil {
    public static String gen(FiroCategory category, String refTargetKey, int index) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((
                category.getDomain().getCode()
                    + refTargetKey
                    + category.getCode()
                    + index
                    + FiroRegistry.getSecret()
            ).getBytes(StandardCharsets.UTF_8));

            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return category.getCode() + "_" + index + "_" + hexString.toString().substring(10, 10 + 20);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String genDir(DirectoryPathPolicy dpp, String refTargetKey) {
        return refTargetKey + dpp.getSeparator();
    }
}