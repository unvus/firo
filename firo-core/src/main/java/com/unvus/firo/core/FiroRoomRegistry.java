package com.unvus.firo.core;

import com.unvus.firo.core.domain.FiroRoom;
import com.unvus.firo.core.domain.RoomService;

import java.util.HashMap;
import java.util.Map;

public class FiroRoomRegistry {
    public static final String _DEFAULT_CABINET_NAME = "default";

    protected static Map<String, FiroRoom> roomMap = new HashMap();

    public static FiroRoom get(String type) {
        return roomMap.containsKey(type)?roomMap.get(type):createDefaultRoom(type);
    }

    public static void add(String type, FiroRoom referenceType) {
        roomMap.put(type, referenceType);
    }

    protected static FiroRoom createDefaultRoom(String roomCode) {
        FiroRoom room = new FiroRoom();
        room.setCode(roomCode);

        RoomService.addCabinet(room, _DEFAULT_CABINET_NAME);

        roomMap.put(roomCode, room);

        return room;
    }
}
