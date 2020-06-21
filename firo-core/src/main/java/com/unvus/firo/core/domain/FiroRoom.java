package com.unvus.firo.core.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class FiroRoom {
    private String code;

    private Map<String, FiroCabinet> cabinetMap = new HashMap();

}
