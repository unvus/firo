package com.unvus.firo.module.adapter.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

@Slf4j
public class S3Adapter implements Adapter {

    private final TransferManager tm;
    private final FiroProperties.S3 props;

    @Getter
    @Setter
    private String directUrl;

    public S3Adapter(FiroProperties.S3 props) throws Exception {
        this.tm = createManager(props);
        this.props = props;
        this.directUrl = props.getDirectUrl();
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
        GetObjectRequest request = new GetObjectRequest(props.getBucket(), fullPath);
        File tempFile = File.createTempFile("s3_read_", "_tmp");

        Download download = tm.download(request, tempFile);

        download.waitForCompletion();

        return tempFile;
    }

    @Override
    public File writeTemp(DirectoryPathPolicy directoryPathPolicy, String path, InputStream in, long size, String contentType) throws Exception {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), path), directoryPathPolicy.getSeparator());
//        Path fullPath = Paths.get(directoryPathPolicy.getTempDir(), path);
        PutObjectRequest request = new PutObjectRequest(props.getBucket(), fullPath, in, metadata);
//        request.setCannedAcl(CannedAccessControlList.PublicRead);
        Upload upload = tm.upload(request);

        upload.waitForCompletion();

        return null;
    }

    @Override
    public void write(DirectoryPathPolicy directoryPathPolicy, String fullDir, String path, InputStream in, long size, String contentType) throws Exception {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        metadata.setContentType(contentType);

        String fullPath = Adapter.adjustSeparator(Paths.get(fullDir, path), directoryPathPolicy.getSeparator());

        PutObjectRequest request = new PutObjectRequest(props.getBucket(), fullPath, in, metadata);
//        request.setCannedAcl(CannedAccessControlList.PublicRead);
        Upload upload = tm.upload(request);

        upload.waitForCompletion();
    }

    @Override
    public void writeFromTemp(DirectoryPathPolicy directoryPathPolicy, String fullDir, String path, String tempFileName, long size, String contentType, boolean keepTemp) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(fullDir, path), directoryPathPolicy.getSeparator());
        String tempFullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), tempFileName), directoryPathPolicy.getSeparator());

        rename(tempFullPath, fullPath, keepTemp);
    }

    @Override
    public void rename(String from, String to, boolean keepFrom) throws Exception {
        CopyObjectRequest copyObjRequest = new CopyObjectRequest(props.getBucket(), from, props.getBucket(), to);
        tm.getAmazonS3Client().copyObject(copyObjRequest);
        if(!keepFrom) {
            tm.getAmazonS3Client().deleteObject(props.getBucket(), from);
        }
    }

    @Override
    public void delete(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getBaseDir(), path), directoryPathPolicy.getSeparator());

        tm.getAmazonS3Client().deleteObject(props.getBucket(), fullPath);
    }

    @Override
    public void deleteTemp(DirectoryPathPolicy directoryPathPolicy, String path) throws Exception {
        String fullPath = Adapter.adjustSeparator(Paths.get(directoryPathPolicy.getTempDir(), path), directoryPathPolicy.getSeparator());
        tm.getAmazonS3Client().deleteObject(props.getBucket(), fullPath);
    }

    @Override
    public boolean supports(AdapterType adapterType) {
        return AdapterType.S3 == adapterType;
    }

    private TransferManager createManager(FiroProperties.S3 props) throws Exception {
        try {
            if (StringUtils.isNotEmpty(props.getRoleArn())) {
                return createManagerArn(props);
            } else {
                return createManagerDefault(props);
            }
        } catch (Exception e) {
            log.error("AWS S3 커넥션 오류");
            log.error("AWS S3 Properties---");
            log.error("AWS S3 Props.accessKey: {}", props.getAccessKey());
            log.error("AWS S3 Props.secretKey: {}", props.getSecretKey());
            log.error("AWS S3 Props.bucket: {}", props.getBucket());
            log.error("AWS S3 Props.region: {}", props.getRegion());
            log.error("AWS S3 Props.roleArn: {}", props.getRoleArn());
            throw e;
        }
    }

    private TransferManager createManagerDefault(FiroProperties.S3 props) throws Exception {

        AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(props.getRegion()));
        if (props.getAccessKey() != null && !"".equals(props.getAccessKey()) && props.getSecretKey() != null && !"".equals(props.getSecretKey())) {
            clientBuilder.withCredentials(
                new AWSStaticCredentialsProvider(new BasicAWSCredentials(props.getAccessKey(), props.getSecretKey()))
            );
        }

//        AmazonS3 s3 = clientBuilder.build();
//        ListObjectsV2Result result = s3.listObjectsV2(props.getBucket());
//        List<S3ObjectSummary> objects = result.getObjectSummaries();
//        for (S3ObjectSummary os : objects) {
//            System.out.println("* " + os.getKey());
//            System.out.println("* " + os.getSize());
//        }
//        AccessControlList acl = s3.getBucketAcl(props.getBucket());
//        List<Grant> grants = acl.getGrantsAsList();
//        for (Grant grant : grants) {
//            System.out.format("  %s: %s\n", grant.getGrantee().getIdentifier(),
//                grant.getPermission().toString());
//        }

        TransferManager transferManager = TransferManagerBuilder.standard()
            .withS3Client(
                clientBuilder.build()
            )
            .build();

        return transferManager;
    }


    private TransferManager createManagerArn(FiroProperties.S3 props) throws Exception {
//        private String accessKey;
//        private String secretKey;
//        private String bucket;
//        private String region;
//        private String roleArn;
        AWSCredentials credentials = new BasicAWSCredentials(props.getAccessKey(), props.getSecretKey());
        // Creating the STS client is part of your trusted code. It has
        // the security credentials you use to obtain temporary security credentials.
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(props.getRegion())
            .build();
        String randomSessionName = RandomStringUtils.randomAlphabetic(20); // ??
        log.info("random RoleSessionName: {}", randomSessionName);

        // Obtain credentials for the IAM role. Note that you cannot assume the role of an AWS root account;
        // Amazon S3 will deny access. You must use credentials for an IAM user or an IAM role.
        AssumeRoleRequest roleRequest = new AssumeRoleRequest()
            .withRoleArn(props.getRoleArn())
            .withRoleSessionName(randomSessionName);
        AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
        Credentials sessionCredentials = roleResponse.getCredentials();

        // Create a BasicSessionCredentials object that contains the credentials you just retrieved.
        BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
            sessionCredentials.getAccessKeyId(),
            sessionCredentials.getSecretAccessKey(),
            sessionCredentials.getSessionToken());

        log.info("awsCredentials.accessKey: {}", sessionCredentials.getAccessKeyId());
        log.info("awsCredentials.secretAccessKey: {}", sessionCredentials.getSecretAccessKey());
        log.info("awsCredentials.sessionToken: {}", sessionCredentials.getSessionToken());

        AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(props.getRegion()));
        //if (props.getAccessKey() != null && !"".equals(props.getAccessKey()) && props.getSecretKey() != null && !"".equals(props.getSecretKey())) {
        clientBuilder.withCredentials(
            new AWSStaticCredentialsProvider(awsCredentials)
        );
        //}
        // Verify that assuming the role worked and the permissions are set correctly
        // by getting a set of object keys from the bucket.
        TransferManager transferManager = TransferManagerBuilder.standard()
            .withS3Client(
                clientBuilder.build()
            )
            .build();

        return transferManager;
    }

    public AdapterType getAdapterType() {
        return AdapterType.S3;
    }
}
