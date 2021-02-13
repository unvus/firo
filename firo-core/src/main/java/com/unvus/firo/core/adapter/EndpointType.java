package com.unvus.firo.core.adapter;


import com.unvus.firo.core.adapter.ftp.FtpProps;
import com.unvus.firo.core.adapter.local.LocalProps;
import com.unvus.firo.core.adapter.s3.S3Props;
import com.unvus.firo.core.adapter.sftp.SFtpProps;

public enum EndpointType {
    LOCAL(LocalProps.class),
    FTP(FtpProps.class),
    SFTP(SFtpProps.class),
    S3(S3Props.class);

    private Class<? extends AdapterProps> propsClass;

    EndpointType(Class propsClass) {
        this.propsClass = propsClass;
    }

    public Class<? extends AdapterProps> getPropsClass() {
        return this.propsClass;
    }
}
