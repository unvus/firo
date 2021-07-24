package com.juvis.core.file.module.policy.impl;

import com.juvis.core.file.module.policy.DirectoryPathPolicy;
import com.unvus.util.DateTools;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by guava on 16.8.12.
 */
public class DateDirectoryPathPolicy implements DirectoryPathPolicy {
    private String separator;

    public enum DATE_SUBDIR_TYPE {
        YYYY("yyyy"), YYYY_MM("yyyy_MM"), YYYY_MM_DD("yyyy_MM_dd");

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
        this(dateSubDirType, baseDir, tempDir, System.getProperty("file.separator"));

    }

    public DateDirectoryPathPolicy(DATE_SUBDIR_TYPE dateSubDirType, String baseDir, String tempDir, String separator) {
        this.separator = separator;
        this.dateSubDirType = dateSubDirType;
        if (baseDir.endsWith(separator)) {
            baseDir = baseDir.substring(0, baseDir.length() - 1);
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
            dir = DateTools.format((LocalDateTime) date, StringUtils.replace(this.dateSubDirType.getFormat(), "_", separator));
        }else if(date instanceof LocalDate) {
            dir = DateTools.format((LocalDate) date, StringUtils.replace(this.dateSubDirType.getFormat(), "_", separator));
        }else {
            throw new RuntimeException("date parameter must be instance of LocalDate or LocalDateTime.");
        }

        return dir + separator;
    }

    @Override
    public String getFullDir(String domain, String category, LocalDateTime date) {
        return getBaseDir() + separator + domain + separator + getSubDir(date);
    }

    @Override
    public String getSeparator() {
        return this.separator;
    }
}
