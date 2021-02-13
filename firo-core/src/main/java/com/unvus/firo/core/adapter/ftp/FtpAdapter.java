package com.unvus.firo.core.adapter.ftp;


import com.unvus.firo.core.adapter.Adapter;
import com.unvus.firo.core.adapter.AdapterProps;
import com.unvus.firo.core.adapter.EndpointType;
import com.unvus.firo.core.domain.FiroCabinet;
import com.unvus.firo.core.policy.DirectoryPathPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class FtpAdapter implements Adapter {

    private final FtpRemoteFileTemplate template;

    public FtpAdapter(FtpRemoteFileTemplate template) {
        this.template = template;
    }

    @Override
    public File read(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        return read(directoryPathPolicy.getBaseDir(), path);
    }


    @Override
    public File readTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        return read(directoryPathPolicy.getTempDir(), path);
    }

    private File read(String dir, String path) throws Exception {
        AtomicReference<File> tempFile = new AtomicReference<>(File.createTempFile("ftp_read_", "_tmp"));

        template
            .execute(session -> {
                try {
                    session.read(Paths.get(dir, path).toString(), new FileOutputStream(tempFile.get()));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    tempFile.set(null);
                }

                return null;
            });
        return tempFile.get();
    }

    @Override
    public File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream in) throws Exception {
        Path fullPath = Paths.get(directoryPathPolicy.getTempDir(), path);
        template
            .execute(session -> {
                try {
                    createDirectoryIfNotExists(session, directoryPathPolicy.getTempDir());
                    session.write(in, fullPath.toString());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

                return null;
            });
        File temp = File.createTempFile("ftp_", ".tmp");
        FileUtils.copyInputStreamToFile(in, File.createTempFile("ftp_", ".tmp"));
        return temp;
    }

    @Override
    public void write(FiroCabinet cabinet, String path, InputStream in) throws Exception {
        DirectoryPathPolicy policy = cabinet.getDirectoryPathPolicy();
        String fullDir = policy.getFullDir(cabinet.getRoom().getCode(), cabinet.getCabinetCode());
        Path fullPath = Paths.get(fullDir, path);
        template
            .execute(session -> {
                try {
                    createDirectoryIfNotExists(session, fullDir);
                    session.write(in, fullPath.toString());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

                return null;
            });
    }

    @Override
    public void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        template
            .execute(session -> {
                try {
                    Path fullPath = Paths.get(directoryPathPolicy.getBaseDir(), path);
                    session.remove(fullPath.toString());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

                return null;
            });
    }


    @Override
    public boolean supports(EndpointType endpointType) {
        return EndpointType.FTP == endpointType;
    }

    private void createDirectoryIfNotExists(Session session, String path) throws IOException {
        String[] paths = StringUtils.split(path, '/');
        String fullDir = "/";
        for(String dir : paths) {
            fullDir += dir;
            if(!session.exists(fullDir)) {
                session.mkdir(fullDir);
            }
            fullDir += "/";
        }

    }
}
