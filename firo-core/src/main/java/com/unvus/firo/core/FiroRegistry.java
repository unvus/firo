package com.unvus.firo.core;

import com.unvus.firo.core.adapter.Adapter;
import com.unvus.firo.core.adapter.local.LocalAdapter;
import com.unvus.firo.core.domain.FiroCabinet;
import com.unvus.firo.core.domain.FiroRoom;
import com.unvus.firo.core.policy.DirectoryPathPolicy;

import java.util.HashMap;
import java.util.Map;

public class FiroRegistry {

    private final static FiroRegistry INSTANCE = new FiroRegistry();

    public static final String _DEFAULT_CABINET_NAME = "default";

    protected static Map<String, FiroRoom> roomMap = new HashMap();

    protected static Adapter adapter = new LocalAdapter();

    protected static DirectoryPathPolicy directoryPathPolicy;

    protected static String secret = "mExYTViNzQzOTE3YmQ4OWY3NTE4MmRkOTg2YmM2NjAyMjVjZTNjNjFkYzZjRhOTlhYWVhNGRjNT";

    public static FiroRegistry from(DirectoryPathPolicy directoryPathPolicy) {
        FiroRegistry.directoryPathPolicy = directoryPathPolicy;
        return INSTANCE;
    }

    public static FiroRegistry from(DirectoryPathPolicy directoryPathPolicy, Adapter adapter) {
        FiroRegistry.directoryPathPolicy = directoryPathPolicy;
        FiroRegistry.adapter = adapter;
        return INSTANCE;
    }

    public static FiroRegistry secret(String secret) {
        FiroRegistry.secret = secret;
        return INSTANCE;
    }

    public static Adapter getDefaultAdapter() {
        return FiroRegistry.adapter;
    }

    public static DirectoryPathPolicy getDefaultDirectoryPathPolicy() {
        return FiroRegistry.directoryPathPolicy;
    }

    public static FiroRoom get(String roomCode) {
        return roomMap.containsKey(roomCode) ? roomMap.get(roomCode) : createDefaultRoom(roomCode);
    }

    public static FiroCabinet get(String roomCode, String cabinetCode) {
        FiroRoom firoRoom = FiroRegistry.get(roomCode);
        FiroCabinet cabinet = firoRoom.getCabinet(cabinetCode);
        return cabinet == null?firoRoom.getCabinet(_DEFAULT_CABINET_NAME): cabinet;
    }


    public static String getSecret() {
        return FiroRegistry.secret;
    }

    public static FiroRegistry add(FiroRoom firoRoom) {
        if(firoRoom.getAdapter() == null) {
            firoRoom.setAdapter(adapter);
        }

        roomMap.put(firoRoom.getCode(), firoRoom);
        return INSTANCE;
    }

    protected static FiroRoom createDefaultRoom(String roomCode) {
        FiroRoom room = FiroRoom.of(roomCode);

        roomMap.put(roomCode, room);

        return room;
    }

}
