package com.unvus.firo.module.service;

import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.service.domain.FiroCabinet;
import com.unvus.firo.module.service.domain.FiroRoom;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Component
public class FiroRegistry {

    private final static FiroRegistry INSTANCE = new FiroRegistry();

    public static final String _DEFAULT_CABINET_NAME = "default";

    protected static PluginRegistry<Adapter, AdapterType> adapterPluginRegistry;

    protected static Map<String, FiroRoom> roomMap = new HashMap();

    protected static AdapterType adapterType = AdapterType.LOCAL;

    protected static DirectoryPathPolicy directoryPathPolicy;

    protected static String secret = "mExYTViNzQzOTE3YmQ4OWY3NTE4MmRkOTg2YmM2NjAyMjVjZTNjNjFkYzZjRhOTlhYWVhNGRjNT";

    @Inject
    public void setAdapterPluginRegistry(PluginRegistry pluginRegistry) {
        this.adapterPluginRegistry = pluginRegistry;
    }

    public static FiroRegistry from(DirectoryPathPolicy directoryPathPolicy) {
        FiroRegistry.directoryPathPolicy = directoryPathPolicy;
        return INSTANCE;
    }

    public static FiroRegistry from(DirectoryPathPolicy directoryPathPolicy, AdapterType adapterType) {
        FiroRegistry.directoryPathPolicy = directoryPathPolicy;
        FiroRegistry.adapterType = adapterType;
        return INSTANCE;
    }

    public static FiroRegistry secret(String secret) {
        FiroRegistry.secret = secret;
        return INSTANCE;
    }

    public static Adapter getDefaultAdapter() {
        return adapterPluginRegistry.getPluginFor(adapterType).get();
    }

    public static AdapterType getDefaultAdapterType() {
        return adapterType;
    }

    public static Adapter getAdapter(AdapterType adapterType) {
        return adapterPluginRegistry.getPluginFor(adapterType).get();
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
        if(cabinet == null) {
            cabinet = FiroCabinet.builder(firoRoom, cabinetCode).build();
            firoRoom.addCabinet(cabinet);
        }
        return cabinet;
    }


    public static String getSecret() {
        return FiroRegistry.secret;
    }

    public static FiroRegistry add(FiroRoom firoRoom) {
        if(firoRoom.getAdapter() == null) {
            firoRoom.setAdapter(getDefaultAdapter());
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
