package com.unvus.firo.core.adapter;


import com.unvus.firo.core.domain.FiroCabinet;
import com.unvus.firo.core.policy.DirectoryPathPolicy;
import org.springframework.plugin.core.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface Adapter extends Plugin<EndpointType>  {

    File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream out) throws Exception;

    void write(FiroCabinet cabinet, String path, InputStream out) throws Exception;

    File readTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    File read(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;

    void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception;
}
