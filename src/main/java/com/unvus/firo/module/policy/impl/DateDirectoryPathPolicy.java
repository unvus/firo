package com.unvus.firo.module.policy.impl;

import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.util.DateTools;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by guava on 16.8.12.
 */
public class DateDirectoryPathPolicy implements DirectoryPathPolicy {
    public static String FILE_SEP = System.getProperty("file.separator");

    public enum DATE_SUBDIR_TYPE {
        YYYY("yyyy"), YYYY_MM("yyyy" + FILE_SEP + "MM"), YYYY_MM_DD("yyyy" + FILE_SEP + "MM" + FILE_SEP + "dd");

        private String format;

        DATE_SUBDIR_TYPE(String format) {
            this.format = format;
        }

        public String getFormat() {
            return format;
        }
    }

    private DATE_SUBDIR_TYPE dateSubDirType;

    private String baseDir = "";
    private String tempDir = "";


    public DateDirectoryPathPolicy(DATE_SUBDIR_TYPE dateSubDirType, String baseDir, String tempDir) {
        this.dateSubDirType = dateSubDirType;
        if(baseDir.endsWith(FILE_SEP)) {
            baseDir = baseDir.substring(0, baseDir.length() -1);
        }
        this.baseDir = baseDir;

        this.tempDir = tempDir;
    }

    @Override
    public String getTempDir() {
        return this.tempDir;
    }

    @Override
    public String getBaseDir() {
        return this.baseDir;
    }

    @Override
    public String getSubDir() {
        LocalDateTime localDateTime = LocalDateTime.now();

        return getSubDir(localDateTime);
    }

    @Override
    public String getSubDir(Object date) {

        String dir;

        if(date instanceof LocalDateTime) {
            dir = DateTools.format((LocalDateTime) date, this.dateSubDirType.getFormat());
        }else if(date instanceof LocalDate) {
            dir = DateTools.format((LocalDate) date, this.dateSubDirType.getFormat());
        }else {
            throw new RuntimeException("date parameter must be instance of LocalDate or LocalDateTime.");
        }

        return dir + FILE_SEP;
    }

    @Override
    public String getFullDir(String room, String cabinet, LocalDateTime date) {
        return getBaseDir() + FILE_SEP + room + FILE_SEP + getSubDir(date);
    }
}