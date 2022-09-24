package com.unvus.firo.util;

import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.service.FiroRegistry;
import com.unvus.firo.module.service.domain.FiroCategory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecureNameUtil {
    public static String gen(FiroCategory category, String refTargetKey, int index) {
        return gen(category.getDomain().getCode(), category.getCode(), refTargetKey, index);
    }

    public static String gen(String domain, String category, String refTargetKey, int index) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((
                domain
                    + refTargetKey
                    + category
                    + index
                    + FiroRegistry.getSecret()
            ).getBytes(StandardCharsets.UTF_8));

            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return category + "_" + index + "_" + hexString.toString().substring(10, 10 + 20);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String genDir(DirectoryPathPolicy dpp, String refTargetKey) {
        return refTargetKey + dpp.getSeparator();
    }

    public static void main(String[] args) {
        System.out.println("generated: " + SecureNameUtil.gen("product", "main", "32", 0));
    }
}
