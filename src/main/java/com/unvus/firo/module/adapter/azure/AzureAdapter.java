package com.unvus.firo.module.adapter.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class AzureAdapter implements Adapter {

    private BlobServiceClient serviceClient;

    private BlobContainerClient containerClient;
    private final FiroProperties.Azure props;

    @Getter
    @Setter
    private String directUrl;

    public AzureAdapter(FiroProperties.Azure props) throws Exception {
        init(props);
        this.props = props;
        this.directUrl = props.getDirectUrl();
    }

    private void init(FiroProperties.Azure props) {
        // Create a BlobServiceClient object which will be used to create a container client
        serviceClient = new BlobServiceClientBuilder().connectionString(props.getConnectionString()).buildClient();

        // return a container client object
        containerClient = serviceClient.getBlobContainerClient(props.getContainer());

//        containerClient.listBlobs()
//            .forEach(blobItem -> System.out.println("Blob name: " + blobItem.getName() + ", Snapshot: " + blobItem.getSnapshot()));
    }

    @Override
    public File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream out, long size, String contentType) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), path), directoryPathPolicy.getSeparator());
        BlobClient blobClient = containerClient.getBlobClient(adjustPath(fullPath));
//        upload(blobClient, out, size, contentType);
        blobClient.upload(out, size);
        return null;
    }

    @Override
    public void write(DirectoryPathPolicy directoryPathPolicy, String fullDir, String path, InputStream out, long size, String contentType) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(fullDir, path), directoryPathPolicy.getSeparator());
        BlobClient blobClient = containerClient.getBlobClient(adjustPath(fullPath));
//        upload(blobClient, out, size, contentType);
        blobClient.upload(out, size);
    }

    @Override
    public void writeFromTemp(DirectoryPathPolicy directoryPathPolicy, String fullDir, String path, String tempFileName, long size, String contentType, boolean keepTemp) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(fullDir, path), directoryPathPolicy.getSeparator());
        String tempFullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), tempFileName), directoryPathPolicy.getSeparator());

        rename(tempFullPath, fullPath, keepTemp);
    }

    @Override
    public void rename(String from, String to, boolean keepFrom) throws Exception {
        BlobClient sourceBlob = containerClient.getBlobClient(adjustPath(from));
        BlobClient destBlob = containerClient.getBlobClient(adjustPath(to));
        destBlob.copyFromUrl(sourceBlob.getBlobUrl());
        if (destBlob.exists() && !keepFrom) {
            sourceBlob.delete();
        }
    }

    public void rename(String from, String from2, String to, boolean keepFrom) throws Exception {
        BlobClient sourceBlob = containerClient.getBlobClient(adjustPath(from));
        if(!sourceBlob.exists()) {
            sourceBlob = containerClient.getBlobClient(adjustPath(from2));
        }
        BlobClient destBlob = containerClient.getBlobClient(adjustPath(to));
        destBlob.copyFromUrl(sourceBlob.getBlobUrl());
        if (destBlob.exists() && !keepFrom) {
            sourceBlob.delete();
        }
    }

    public void copyIfNotExists(String from, String to) {
        BlobClient destBlob = containerClient.getBlobClient(adjustPath(to));
        if (destBlob.exists()) {
            return;
        }
        BlobClient sourceBlob = containerClient.getBlobClient(adjustPath(from));
        destBlob.copyFromUrl(sourceBlob.getBlobUrl());
    }

    @Override
    public File readTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), path), directoryPathPolicy.getSeparator());
        return read(fullPath);
    }

    @Override
    public File read(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getBaseDir(), path), directoryPathPolicy.getSeparator());
        return read(fullPath);
    }

    @Override
    public void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getBaseDir(), path), directoryPathPolicy.getSeparator());
        BlobClient destBlob = containerClient.getBlobClient(adjustPath(fullPath));
        destBlob.delete();
    }

    @Override
    public void deleteTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), path), directoryPathPolicy.getSeparator());
        BlobClient destBlob = containerClient.getBlobClient(adjustPath(fullPath));
        destBlob.delete();
    }

    @Override
    public boolean supports(AdapterType adapterType) {
        return AdapterType.AZURE == adapterType;
    }

    private File read(String fullPath) throws Exception {
        fullPath = adjustPath(fullPath);
        BlobClient blobClient = containerClient.getBlobClient(fullPath);
        File tempFile = File.createTempFile("azure_read_", "_tmp");
        try {
            blobClient.download(Files.newOutputStream(tempFile.toPath()));
        }catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }

        return tempFile;
    }

    private String adjustPath(String path) {
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

//    private void upload(BlobClient blobClient, InputStream data, long size, String contentType) {
//        BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
//        BlobHttpHeaders headers = new BlobHttpHeaders();
//        headers.setCacheControl("no-cache");
//        if(StringUtils.isNotBlank(contentType)) {
//            headers.setContentType(contentType);
//        }
//
//        blobClient.uploadWithResponse(data, size, (ParallelTransferOptions)null, headers, (Map)null, (AccessTier)null, new BlobRequestConditions(), (Duration)null, Context.NONE);
//    }

    public AdapterType getAdapterType() {
        return AdapterType.AZURE;
    }
}
