package com.unvus.firo.core.adapter;


import com.unvus.firo.core.policy.DirectoryPathPolicy;

import java.io.File;
import java.io.InputStream;

public interface Adapter {

    File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream out) throws Exception;

    void write(String fullDir, String path, InputStream out) throws Exception;

    void rename(String from, String to) throws Exception;

    File readTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    File read(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    void deleteTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;
}
