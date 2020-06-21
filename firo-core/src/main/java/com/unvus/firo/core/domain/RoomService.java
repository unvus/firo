package com.unvus.firo.core.domain;

public class RoomService {

    public static FiroCabinet addCabinet(FiroRoom room, String code) {
        FiroCabinet cabinet = new FiroCabinet();
        cabinet.setCabinetCode(code);
        room.getCabinetMap().put(code, cabinet);
        return cabinet;
    }
}
