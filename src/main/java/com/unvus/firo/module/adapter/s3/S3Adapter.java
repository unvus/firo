package com.unvus.firo.module.adapter.s3;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class S3Adapter implements Adapter {

    private final TransferManager tm;
    private final FiroProperties.S3 props;

    public S3Adapter(TransferManager tm, FiroProperties.S3 props) {
        this.tm = tm;
        this.props = props;
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
        GetObjectRequest request = new GetObjectRequest(props.getBucket(), Paths.get(dir, path).toString());
        File tempFile = File.createTempFile("s3_read_", "_tmp");

        Download download = tm.download(request, tempFile);

        download.waitForCompletion();

        return tempFile;
    }

    @Override
    public File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream in, long size) throws Exception {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        Path fullPath = Paths.get(directoryPathPolicy.getTempDir(), path);
        PutObjectRequest request = new PutObjectRequest(props.getBucket(), fullPath.toString(), in, metadata);
        Upload upload = tm.upload(request);

        upload.waitForCompletion();

        File temp = File.createTempFile("s3_", ".tmp");
        FileUtils.copyInputStreamToFile(in, File.createTempFile("s3_", ".tmp"));
        return temp;
    }

    @Override
    public void write(String fullDir, String path, InputStream in, long size) throws Exception {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        Path fullPath = Paths.get(fullDir, path);
        PutObjectRequest request = new PutObjectRequest(props.getBucket(), fullPath.toString(), in, metadata);
        Upload upload = tm.upload(request);

        upload.waitForCompletion();
    }

    @Override
    public void rename(String from, String to) throws Exception {

    }

    @Override
    public void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {

    }

    @Override
    public void deleteTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {

    }

    @Override
    public boolean supports(AdapterType adapterType) {
        return AdapterType.S3 == adapterType;
    }
}
