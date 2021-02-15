package com.unvus.firo.util;

import com.unvus.firo.core.FiroRegistry;
import com.unvus.firo.core.domain.FiroCabinet;
import com.unvus.firo.module.policy.impl.DateDirectoryPathPolicy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecureNameUtil {
    public static String gen(FiroCabinet cabinet, String refTargetKey, int index) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((
                cabinet.getRoom().getCode()
                    + refTargetKey
                    + cabinet.getCabinetCode()
                    + index
                    + FiroRegistry.getSecret()
            ).getBytes(StandardCharsets.UTF_8));

            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return index + "_" + hexString.toString().substring(10, 10 + 20);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String genDir(FiroCabinet cabinet, String refTargetKey) {
        return refTargetKey + "_" + cabinet.getCabinetCode() + DateDirectoryPathPolicy.FILE_SEP;
    }
}
