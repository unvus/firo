package com.unvus.firo.module.service.domain;

import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.filter.FiroFilterChain;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;

public class FiroCabinet {

    @Getter
    private FiroRoom room;

    @Getter
    private String code;

    @Getter
    @Setter
    private Adapter adapter;

    @Getter
    @Setter
    private String directUrl;

    @Getter
    private DirectoryPathPolicy directoryPathPolicy;

    @Getter
    @Setter
    private FiroFilterChain filterChain;

    @Getter
    @Setter
    private SecureAccessFunc secureAccessFunc;

    FiroCabinet(FiroRoom room, String code, String directUrl,
                Adapter adapter, DirectoryPathPolicy directoryPathPolicy,
                FiroFilterChain filterChain, SecureAccessFunc secureAccessFunc) {
        this.room = room;
        this.code = code;
        this.directUrl = directUrl;
        this.adapter = adapter;
        this.directoryPathPolicy = directoryPathPolicy;
        this.filterChain = filterChain;
        this.secureAccessFunc = secureAccessFunc;
    }

    public static FiroCabinetBuilder builder(FiroRoom room, String cabinetCode) {
        return new FiroCabinetBuilder(room, cabinetCode);
    }

    public static class FiroCabinetBuilder {
        private FiroRoom room;
        private String code;
        private String directUrl;
        private Adapter adapter;
        private DirectoryPathPolicy directoryPathPolicy;
        private FiroFilterChain filterChain;
        private SecureAccessFunc secureAccessFunc;

        public FiroCabinetBuilder(FiroRoom room, String code) {
            this.room = room;
            this.code = code;
        }

        public FiroCabinetBuilder directUrl(String directUrl) {
            this.directUrl = directUrl;
            return this;
        }

        public FiroCabinetBuilder adapter(Adapter adapter) {
            this.adapter = adapter;
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

        public FiroCabinetBuilder secureAccessFunc(SecureAccessFunc secureAccessFunc) {
            this.secureAccessFunc = secureAccessFunc;
            return this;
        }

        public FiroCabinet build() {
            if (code == null) {
                code = "default";
            }
            if (directUrl == null) {
                directUrl = room.getDirectUrl();
            }

            if (directoryPathPolicy == null) {
                directoryPathPolicy = room.getDirectoryPathPolicy();
            }

            if (adapter == null) {
                adapter = room.getAdapter();
            }

            if (secureAccessFunc == null) {
                secureAccessFunc = room.getSecureAccessFunc();
            }

            return new FiroCabinet(room, code, directUrl, adapter, directoryPathPolicy, filterChain, secureAccessFunc);
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
        return this.directoryPathPolicy.getFullDir(this.room.getCode(), this.getCode(), date);
    }
}
