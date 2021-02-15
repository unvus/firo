package com.unvus.firo.module.channel.endpoint;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class FtpProps implements AdapterProps {
    private String host;
    private String port;
    private String username;
    private String password;
    private String compression = "Y"; // default: Y

    @Override
    public boolean validate() {
        return StringUtils.isNotBlank(host);
    }
}
