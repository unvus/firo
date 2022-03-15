package com.unvus.firo.module.adapter.ftp;


import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class FtpAdapter implements Adapter {

    private final FtpRemoteFileTemplate template;

    public FtpAdapter(FtpRemoteFileTemplate template) {
        this.template = template;
    }

    @Override
    public File read(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getBaseDir(), path), directoryPathPolicy.getSeparator());
        return read(fullPath);
    }


    @Override
    public File readTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), path), directoryPathPolicy.getSeparator());

        return read(fullPath);
    }

    private File read(String fullPath) throws Exception {
        AtomicReference<File> tempFile = new AtomicReference<>(File.createTempFile("ftp_read_", "_tmp"));

        template
            .execute(session -> {
                session.read(fullPath, new FileOutputStream(tempFile.get()));

                return null;
            });
        return tempFile.get();
    }

    @Override
    public File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream in, long size, String contentType) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), path), directoryPathPolicy.getSeparator());

        template
            .execute(session -> {
                createDirectoryIfNotExists(session, directoryPathPolicy.getTempDir());
                session.write(in, fullPath);

                return null;
            });

        return null;
    }

    @Override
    public void write(DirectoryPathPolicy directoryPathPolicy, String fullDir, String path, InputStream in, long size, String contentType) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(fullDir, path), directoryPathPolicy.getSeparator());

        template
            .execute(session -> {
                createDirectoryIfNotExists(session, fullDir);
                session.write(in, fullPath);

                return null;
            });
    }

    @Override
    public void writeFromTemp(DirectoryPathPolicy directoryPathPolicy, String fullDir, String path, String tempFileName, long size, String contentType) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(fullDir, path), directoryPathPolicy.getSeparator());
        String tempFullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), tempFileName), directoryPathPolicy.getSeparator());
        template
            .execute(session -> {
                createDirectoryIfNotExists(session, fullDir);
                session.rename(tempFullPath, fullPath);

                return null;
            });
    }

    @Override
    public void rename(String from, String to) throws Exception {
        template
            .execute(session -> {
                session.rename(from, to);
                return null;
            });
    }

    @Override
    public void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getBaseDir(), path), directoryPathPolicy.getSeparator());
        template
            .execute(session -> {
                session.remove(fullPath);
                return null;
            });
    }

    @Override
    public void deleteTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), path), directoryPathPolicy.getSeparator());
        template
            .execute(session -> {
                session.remove(fullPath);
                return null;
            });
    }

    private void createDirectoryIfNotExists(Session session, String path) throws IOException {
        String[] paths = StringUtils.split(path, '/');
        String fullDir = "/";
        for (String dir : paths) {
            fullDir += dir;
            if (!session.exists(fullDir)) {
                session.mkdir(fullDir);
            }
            fullDir += "/";
        }
    }

    @Override
    public boolean supports(AdapterType adapterType) {
        return AdapterType.FTP == adapterType;
    }

}
