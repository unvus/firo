package com.unvus.firo.module.channel.endpoint;

import lombok.Data;

@Data
public class LocalProps implements AdapterProps {

    private String compression = "N";

    @Override
    public boolean validate() {
        return true;
    }
}
