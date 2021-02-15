package com.unvus.firo.module.service.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class AttachBag extends HashMap<String, List<FiroFile>> implements Serializable {

    private String roomCode;


    public AttachBag() {
    }

    public AttachBag(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public FiroFile one(String mapCode) {
        List<FiroFile> attachList = get(mapCode);
        if(attachList != null && attachList.size() > 0) {
            return attachList.get(0);
        }
        return null;
    }
}
