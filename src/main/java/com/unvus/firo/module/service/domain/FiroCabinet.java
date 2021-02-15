package com.unvus.firo.module.service.domain;

import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import com.unvus.firo.module.filter.FiroFilterChain;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.service.FiroRegistry;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;

public class FiroCabinet {

    @Getter
    private FiroRoom room;

    @Getter
    private String cabinetCode;

    @Getter
    @Setter
    private Adapter adapter;

    @Getter
    private DirectoryPathPolicy directoryPathPolicy;

    @Getter
    @Setter
    private FiroFilterChain filterChain;

    FiroCabinet(FiroRoom room, String cabinetCode, Adapter adapter, DirectoryPathPolicy directoryPathPolicy, FiroFilterChain filterChain) {
        this.room = room;
        this.cabinetCode = cabinetCode;
        this.adapter = adapter;
        this.directoryPathPolicy = directoryPathPolicy;
        this.filterChain = filterChain;
    }

    public static FiroCabinetBuilder builder(FiroRoom room, String cabinetCode) {
        return new FiroCabinetBuilder(room, cabinetCode);
    }

    public static class FiroCabinetBuilder {
        private FiroRoom room;
        private String cabinetCode;
        private AdapterType adapterType;
        private DirectoryPathPolicy directoryPathPolicy;
        private FiroFilterChain filterChain;

        public FiroCabinetBuilder(FiroRoom room, String cabinetCode) {
            this.room = room;
            this.cabinetCode = cabinetCode;
        }

        public FiroCabinetBuilder adapter(AdapterType adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public FiroCabinetBuilder directoryPathPolicy(DirectoryPathPolicy directoryPathPolicy) {
            this.directoryPathPolicy = directoryPathPolicy;
            return this;
        }

        public FiroCabinetBuilder filterChain(FiroFilterChain filterChain) {
            this.filterChain = filterChain;
            return this;
        }

        public FiroCabinet build() {
            if(directoryPathPolicy == null) {
                directoryPathPolicy = room.getDirectoryPathPolicy();
            }

            Adapter adapter;
            if(adapterType == null) {
                adapter = room.getAdapter();
            }else {
                adapter = FiroRegistry.getAdapter(adapterType);
            }

            return new FiroCabinet(room, cabinetCode, adapter, directoryPathPolicy, filterChain);
        }
    }

    public File readTemp(String path) throws Exception {
        return adapter.readTemp(this.directoryPathPolicy, path);
    }

    public File read(String path) throws Exception {
        return adapter.read(this.directoryPathPolicy, path);
    }

    public File writeTemp(String path, InputStream in) throws Exception {
        return adapter.writeTemp(this.directoryPathPolicy, path, in);
    }

    public void write(String fullDir, String path, InputStream in) throws Exception {
        adapter.write(fullDir, path, in);
    }

    public void rename(String from, String to) throws Exception {
        adapter.rename(from, to);
    }

    public void delete(String path) throws Exception {
        adapter.delete(this.directoryPathPolicy, path);
    }

    public void deleteTemp(String path) throws Exception {
        adapter.deleteTemp(this.directoryPathPolicy, path);
    }

    public String getFullDir(LocalDateTime date) {
        return this.directoryPathPolicy.getFullDir(this.room.getCode(), this.getCabinetCode(), date);
    }
}
