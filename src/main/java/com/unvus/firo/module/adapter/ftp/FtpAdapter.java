package com.unvus.firo.module.adapter.ftp;


import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
                session.read(Paths.get(dir, path).toString(), new FileOutputStream(tempFile.get()));

                return null;
            });
        return tempFile.get();
    }

    @Override
    public File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream in) throws Exception {
        Path fullPath = Paths.get(directoryPathPolicy.getTempDir(), path);
        template
            .execute(session -> {
                createDirectoryIfNotExists(session, directoryPathPolicy.getTempDir());
                session.write(in, fullPath.toString());

                return null;
            });
        File temp = File.createTempFile("ftp_", ".tmp");
        FileUtils.copyInputStreamToFile(in, File.createTempFile("ftp_", ".tmp"));
        return temp;
    }

    @Override
    public void write(String fullDir, String path, InputStream in) throws Exception {
        Path fullPath = Paths.get(fullDir, path);
        template
            .execute(session -> {
                createDirectoryIfNotExists(session, fullDir);
                session.write(in, fullPath.toString());

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
        template
            .execute(session -> {
                Path fullPath = Paths.get(directoryPathPolicy.getBaseDir(), path);
                session.remove(fullPath.toString());
                return null;
            });
    }

    @Override
    public void deleteTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        template
            .execute(session -> {
                Path fullPath = Paths.get(directoryPathPolicy.getTempDir(), path);
                session.remove(fullPath.toString());
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
