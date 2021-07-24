package com.juvis.core.file.module.adapter;


import com.juvis.core.file.module.policy.DirectoryPathPolicy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.plugin.core.Plugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public interface Adapter extends Plugin<AdapterType> {

    File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream out, long size, String contentType) throws Exception;

    void write(DirectoryPathPolicy directoryPathPolicy, String fullDir, String path, InputStream out, long size, String contentType) throws Exception;

    void rename(String from, String to) throws Exception;

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
}
