package com.unvus.firo.core.adapter.local;

import com.unvus.firo.core.adapter.Adapter;
import com.unvus.firo.core.adapter.AdapterProps;
import com.unvus.firo.core.adapter.EndpointType;
import com.unvus.firo.core.domain.FiroCabinet;
import com.unvus.firo.core.policy.DirectoryPathPolicy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalAdapter implements Adapter {

    @Override
    public File readTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        Path fullPath = Paths.get(directoryPathPolicy.getTempDir(), path);
        if (!Files.exists(fullPath)) {
            return null;
        }

        return fullPath.toFile();
    }

    @Override
    public File read(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        Path fullPath = Paths.get(directoryPathPolicy.getBaseDir(), path);
        if (!Files.exists(fullPath)) {
            return null;
        }

        return fullPath.toFile();
    }

    @Override
    public File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream in) throws Exception {
        createDirectoryIfNotExists(directoryPathPolicy.getTempDir());
        Path fullPath = Paths.get(directoryPathPolicy.getTempDir(), path);
        Files.copy(in, fullPath);
        return fullPath.toFile();
    }

    @Override
    public void write(FiroCabinet cabinet, String path, InputStream in) throws Exception {
        DirectoryPathPolicy policy = cabinet.getDirectoryPathPolicy();

        String fullDir = policy.getFullDir(cabinet.getRoom().getCode(), cabinet.getCabinetCode());
        createDirectoryIfNotExists(fullDir);

        Files.copy(in, Paths.get(fullDir, path));
    }

    @Override
    public void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        File t = Paths.get(directoryPathPolicy.getBaseDir(), path).toFile();
        if (t.exists()) {
            t.delete();
        }
    }


    private void createDirectoryIfNotExists(String saveDir) throws IOException {
        Path path = Paths.get(saveDir);
        //if directory exists?
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    @Override
    public boolean supports(EndpointType endpointType) {
        return false;
    }
}
