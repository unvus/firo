package com.unvus.firo.core.domain;

import com.unvus.firo.core.adapter.Adapter;
import com.unvus.firo.core.policy.DirectoryPathPolicy;

public class RoomService {

    public static FiroCabinet addCabinet(FiroRoom room, String cabinetCode, Adapter adapter, DirectoryPathPolicy directoryPathPolicy) {
        FiroCabinet cabinet =
            FiroCabinet
                .builder(room)
                .cabinetCode(cabinetCode)
                .adapter(adapter == null?room.getAdapter():adapter)
                .directoryPathPolicy(directoryPathPolicy == null?room.getDirectoryPathPolicy():directoryPathPolicy)
                .build();
        room.getCabinetMap().put(cabinetCode, cabinet);
        return cabinet;
    }
}
