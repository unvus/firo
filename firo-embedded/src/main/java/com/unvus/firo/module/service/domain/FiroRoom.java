package com.unvus.firo.module.service.domain;

import com.unvus.firo.core.FiroRegistry;
import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FiroRoom {

    private FiroRoom() {
    }

    public static FiroRoom of(String code) {
        FiroRoom room = new FiroRoom();
        room.setCode(code);
        room.setAdapter(FiroRegistry.getDefaultAdapter());
        room.setDirectoryPathPolicy(FiroRegistry.getDefaultDirectoryPathPolicy());
        room.addCabinet(FiroRegistry._DEFAULT_CABINET_NAME);
        return room;
    }

    private String code;

    private Adapter adapter;

    private DirectoryPathPolicy directoryPathPolicy;

    private Map<String, FiroCabinet> cabinetMap = new HashMap();

    public FiroRoom addCabinet(String code) {
        FiroCabinet cabinet = FiroCabinet
            .builder(this)
            .cabinetCode(code)
            .adapter(adapter)
            .directoryPathPolicy(directoryPathPolicy)
            .build();
        cabinetMap.put(code, cabinet);
        return this;
    }
    public FiroCabinet getCabinet(String cabinetCode) {
        if(cabinetCode == null) {
            return null;
        }
        return cabinetMap.get(cabinetCode);
    }

}
