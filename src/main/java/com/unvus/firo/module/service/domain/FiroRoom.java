package com.unvus.firo.module.service.domain;

import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.service.FiroRegistry;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FiroRoom {

    private FiroRoom() {
    }

    private FiroRoom(String code, Adapter adapter, DirectoryPathPolicy directoryPathPolicy) {

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
            .builder(this, code)
            .directoryPathPolicy(directoryPathPolicy)
            .build();

        cabinetMap.put(code, cabinet);
        return this;
    }

    public FiroRoom addCabinet(FiroCabinet cabinet) {
        if(!this.code.equals(cabinet.getRoom().getCode())) {
            throw new RuntimeException("does not match for this room : invalid room code");
        }
        cabinetMap.put(cabinet.getCabinetCode(), cabinet);
        return this;
    }
    public FiroCabinet getCabinet(String cabinetCode) {
        if(cabinetCode == null) {
            return null;
        }
        return cabinetMap.get(cabinetCode);
    }

    public static class FiroRoomBuilder {
        private String code;

        private AdapterType adapterType;

        private DirectoryPathPolicy directoryPathPolicy;

        public FiroRoomBuilder(String code) {
            this.code = code;
        }

        public FiroRoomBuilder adapter(AdapterType adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public FiroRoomBuilder directoryPathPolicy(DirectoryPathPolicy directoryPathPolicy) {
            this.directoryPathPolicy = directoryPathPolicy;
            return this;
        }

        public FiroRoom build() {
            if (directoryPathPolicy == null) {
                directoryPathPolicy = FiroRegistry.getDefaultDirectoryPathPolicy();
            }

            Adapter adapter;
            if (adapterType == null) {
                adapter = FiroRegistry.getDefaultAdapter();
            } else {
                adapter = FiroRegistry.getAdapter(adapterType);
            }

            return new FiroRoom(code, adapter, directoryPathPolicy);
        }
    }

}
