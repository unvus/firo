package com.unvus.firo.module.service.domain;

import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.service.FiroRegistry;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FiroRoom {

    private String code;

    private String directUrl;

    private Adapter adapter;

    private DirectoryPathPolicy directoryPathPolicy;

    private Map<String, FiroCabinet> cabinetMap = new HashMap();

    private SecureAccessFunc secureAccessFunc;

    private FiroRoom(String code) {
        this.code = code;
    }

    private FiroRoom(String code, String directUrl, Adapter adapter, DirectoryPathPolicy directoryPathPolicy, SecureAccessFunc secureAccessFunc) {
        this.code = code;
        this.directUrl = directUrl;
        this.adapter = adapter;
        this.directoryPathPolicy = directoryPathPolicy;
        this.secureAccessFunc = secureAccessFunc;
    }

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
        cabinetMap.put(cabinet.getCode(), cabinet);
        return this;
    }

    public FiroCabinet getCabinet(String cabinetCode) {
        if(cabinetCode == null) {
            return null;
        }
        return cabinetMap.get(cabinetCode);
    }

    public Map<String, FiroCabinet> getAllCabinet() {
        return cabinetMap;
    }

    public static FiroRoomBuilder builder(String code) {
        return new FiroRoomBuilder(code);
    }

    public static class FiroRoomBuilder {
        private String code;

        private String directUrl;

        private Adapter adapter;

        private DirectoryPathPolicy directoryPathPolicy;

        private SecureAccessFunc secureAccessFunc;

        public FiroRoomBuilder(String code) {
            this.code = code;
        }

        public FiroRoomBuilder directUrl(String directUrl) {
            this.directUrl = directUrl;
            return this;
        }

        public FiroRoomBuilder adapter(Adapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public FiroRoomBuilder directoryPathPolicy(DirectoryPathPolicy directoryPathPolicy) {
            this.directoryPathPolicy = directoryPathPolicy;
            return this;
        }

        public FiroRoomBuilder secureAccessFunc(SecureAccessFunc secureAccessFunc) {
            this.secureAccessFunc = secureAccessFunc;
            return this;
        }

        public FiroRoom build() {
            if (directUrl == null) {
                directUrl = FiroRegistry.getDefaultDirectUrl();
            }

            if (directoryPathPolicy == null) {
                directoryPathPolicy = FiroRegistry.getDefaultDirectoryPathPolicy();
            }

            if (adapter == null) {
                adapter = FiroRegistry.getDefaultAdapter();
            }

            if (secureAccessFunc == null) {
                secureAccessFunc = FiroRegistry.getDefaultSecureAccessFunc();
            }

            FiroRoom room = new FiroRoom(code, directUrl, adapter, directoryPathPolicy, secureAccessFunc);
            room.addCabinet(FiroRegistry._DEFAULT_CABINET_NAME);
            return room;
        }
    }

}
