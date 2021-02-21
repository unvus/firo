package com.unvus.firo.module.service;

import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.service.domain.FiroCabinet;
import com.unvus.firo.module.service.domain.FiroRoom;
import com.unvus.firo.module.service.domain.SecureAccessFunc;
import org.apache.commons.lang3.StringUtils;
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

    protected static String defaultDirectUrl;

    protected static AdapterType defaultAdapterType = AdapterType.LOCAL;

    protected static Adapter defaultAdapter;

    protected static DirectoryPathPolicy directoryPathPolicy;

    protected static SecureAccessFunc defaultSecureAccessFunc = null;

    protected static String secret = "mExYTViNzQzOTE3YmQ4OWY3NTE4MmRkOTg2YmM2NjAyMjVjZTNjNjFkYzZjRhOTlhYWVhNGRjNT";

    @Inject
    public void setAdapterPluginRegistry(PluginRegistry pluginRegistry) {
        this.adapterPluginRegistry = pluginRegistry;
    }

    public static FiroRegistry from(FiroProperties props, DirectoryPathPolicy directoryPathPolicy) {
        FiroRegistry.defaultDirectUrl = props.getDirectUrl();
        FiroRegistry.directoryPathPolicy = directoryPathPolicy;
        FiroRegistry.defaultAdapter = getAdapter(defaultAdapterType);

        return INSTANCE;
    }

    public static FiroRegistry from(FiroProperties props, DirectoryPathPolicy directoryPathPolicy, AdapterType adapterType) {
        FiroRegistry.defaultDirectUrl = props.getDirectUrl();
        FiroRegistry.directoryPathPolicy = directoryPathPolicy;
        FiroRegistry.defaultAdapter = getAdapter(adapterType);
        return INSTANCE;
    }

    public static FiroRegistry from(FiroProperties props, DirectoryPathPolicy directoryPathPolicy, Adapter adapter) {
        FiroRegistry.defaultDirectUrl = props.getDirectUrl();
        FiroRegistry.directoryPathPolicy = directoryPathPolicy;
        FiroRegistry.defaultAdapter = adapter;
        return INSTANCE;
    }

    public static FiroRegistry secret(String secret) {
        FiroRegistry.secret = secret;
        return INSTANCE;
    }

    public static FiroRegistry secureAccessFunc(SecureAccessFunc defaultSecureAccessFunc) {
        FiroRegistry.defaultSecureAccessFunc = defaultSecureAccessFunc;
        return INSTANCE;
    }

    public static SecureAccessFunc getDefaultSecureAccessFunc() {
        return FiroRegistry.defaultSecureAccessFunc;
    }

    public static String getDefaultDirectUrl() {
        return defaultDirectUrl;
    }

    public static String getDirectUrl(String roomCode, String cabinetCode) {
        String result = defaultDirectUrl;
        if(StringUtils.isBlank(roomCode)) {
            return result;
        }

        FiroRoom room = get(roomCode);
        if(room != null) {
            result = room.getDirectUrl();
            if (StringUtils.isBlank(cabinetCode)) {
                return result;
            }
            FiroCabinet cabinet = room.getCabinet(cabinetCode);
            if(cabinet != null) {
                result = cabinet.getDirectUrl();
            }
        }
        return result;
    }

    public static Adapter getDefaultAdapter() {
        return FiroRegistry.defaultAdapter;
    }

    public static Adapter getAdapter(AdapterType adapterType) {
        return adapterPluginRegistry.getPluginFor(adapterType).get();
    }

    public static DirectoryPathPolicy getDefaultDirectoryPathPolicy() {
        return FiroRegistry.directoryPathPolicy;
    }

    public static Map<String, FiroRoom> getAllRoom() {
        return roomMap;
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
        FiroRoom room = FiroRoom.builder(roomCode).build();

        roomMap.put(roomCode, room);

        return room;
    }

}
