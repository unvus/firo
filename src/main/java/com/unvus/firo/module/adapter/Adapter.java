package com.unvus.firo.module.adapter;


import com.unvus.firo.module.policy.DirectoryPathPolicy;
import org.springframework.plugin.core.Plugin;

import java.io.File;
import java.io.InputStream;

public interface Adapter extends Plugin<AdapterType> {

    File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream out) throws Exception;

    void write(String fullDir, String path, InputStream out) throws Exception;

    void rename(String from, String to) throws Exception;

    File readTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    File read(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    void deleteTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;
}
