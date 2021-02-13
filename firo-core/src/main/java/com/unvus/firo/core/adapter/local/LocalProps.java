package com.unvus.firo.core.adapter.local;

import com.unvus.firo.core.adapter.AdapterProps;
import lombok.Data;

@Data
public class LocalProps implements AdapterProps {

    private String compression = "N";

    @Override
    public boolean validate() {
        return true;
    }
}
