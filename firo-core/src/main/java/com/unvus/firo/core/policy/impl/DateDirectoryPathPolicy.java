package com.unvus.firo.core.policy.impl;


import com.unvus.firo.core.domain.FiroCabinet;
import com.unvus.firo.core.policy.DirectoryPathPolicy;
import com.unvus.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by guava on 16.8.12.
 */
public class DateDirectoryPathPolicy implements DirectoryPathPolicy {
    public static final String FILE_SEP = System.getProperty("file.separator");

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
            dir = DateUtils.format((LocalDateTime) date, this.dateSubDirType.getFormat());
        }else if(date instanceof LocalDate) {
            dir = DateUtils.format((LocalDate) date, this.dateSubDirType.getFormat());
        }else {
            throw new RuntimeException("date parameter must be instance of LocalDate or LocalDateTime.");
        }

        return dir + FILE_SEP;
    }

    @Override
    public String getFullDir(String room, String cabinet) {
        return getBaseDir() + FILE_SEP + room + FILE_SEP + getSubDir();
    }
}
