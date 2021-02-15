package com.unvus.firo.module.channel.endpoint.enums;


import com.unvus.firo.module.channel.endpoint.AdapterProps;
import com.unvus.firo.module.channel.endpoint.FtpProps;
import com.unvus.firo.module.channel.endpoint.S3Props;

public enum EndpointType {
    LOCAL("BS", FtpProps.class), // 싱글파일을 처리하기 위한 것으로, 이 타입은 DB에 저장하지 않는다.
    FTP("FT", FtpProps.class),
    SFTP("ST", FtpProps.class),
    S3("S3", S3Props.class);

    private String code;
    private Class<? extends AdapterProps> propsClass;

    EndpointType(String code, Class propsClass) {
        this.code = code;
        this.propsClass = propsClass;
    }

    public String getCode() {
        return code;
    }

    public Class<? extends AdapterProps> getPropsClass() {
        return this.propsClass;
    }

    public static EndpointType getByCode(String code) {
        for(EndpointType c : values()) {
            if(c.code.equals(code)) {
                return c;
            }
        }
        return null;
    }
}
