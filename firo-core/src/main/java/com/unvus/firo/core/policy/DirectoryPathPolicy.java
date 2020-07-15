package com.unvus.firo.core.policy;

/**
 * Created by guava on 16.8.12.
 */
public interface DirectoryPathPolicy {

    String getTempDir();
    String getBaseDir();
    String getSubDir();
    String getSubDir(Object param);
    String getFullDir(String room, String cabinet);
}
