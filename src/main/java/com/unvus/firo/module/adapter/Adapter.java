package com.unvus.firo.module.adapter;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import com.unvus.firo.module.policy.DirectoryPathPolicy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.plugin.core.Plugin;
import org.springframework.stereotype.Component;

@Component
public interface Adapter extends Plugin<AdapterType> {

    File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream out, long size, String contentType) throws Exception;

    void write(DirectoryPathPolicy directoryPathPolicy, String fullDir, String path, InputStream out, long size, String contentType) throws Exception;

    void writeFromTemp(DirectoryPathPolicy directoryPathPolicy, String fullDir, String path, String tempFileName, long size, String contentType, boolean keepTemp) throws Exception;

    void rename(String from, String to, boolean keepFrom) throws Exception;

    File readTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    File read(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    void deleteTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    static String adjustSeparator(Path path, String separator) {
        return adjustSeparator(path.toString(), separator);
    }

    static String adjustSeparator(String path, String separator) {
        if(!separator.equals(System.getProperty("file.separator"))) {
            return StringUtils.replace(path, System.getProperty("file.separator"), separator);
        }
        return path;
    }

    AdapterType getAdapterType();
}
