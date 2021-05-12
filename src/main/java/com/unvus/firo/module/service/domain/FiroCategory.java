package com.unvus.firo.module.service.domain;

import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.filter.FiroFilterChain;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;

public class FiroCategory {

    @Getter
    private FiroDomain domain;

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

    FiroCategory(FiroDomain domain, String code, String directUrl,
                 Adapter adapter, DirectoryPathPolicy directoryPathPolicy,
                 FiroFilterChain filterChain, SecureAccessFunc secureAccessFunc) {
        this.domain = domain;
        this.code = code;
        this.directUrl = directUrl;
        this.adapter = adapter;
        this.directoryPathPolicy = directoryPathPolicy;
        this.filterChain = filterChain;
        this.secureAccessFunc = secureAccessFunc;
    }

    public static FiroCategoryBuilder builder(FiroDomain domain, String categoryCode) {
        return new FiroCategoryBuilder(domain, categoryCode);
    }

    public static class FiroCategoryBuilder {
        private FiroDomain domain;
        private String code;
        private String directUrl;
        private Adapter adapter;
        private DirectoryPathPolicy directoryPathPolicy;
        private FiroFilterChain filterChain;
        private SecureAccessFunc secureAccessFunc;

        public FiroCategoryBuilder(FiroDomain domain, String code) {
            this.domain = domain;
            this.code = code;
        }

        public FiroCategoryBuilder directUrl(String directUrl) {
            this.directUrl = directUrl;
            return this;
        }

        public FiroCategoryBuilder adapter(Adapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public FiroCategoryBuilder directoryPathPolicy(DirectoryPathPolicy directoryPathPolicy) {
            this.directoryPathPolicy = directoryPathPolicy;
            return this;
        }

        public FiroCategoryBuilder filterChain(FiroFilterChain filterChain) {
            this.filterChain = filterChain;
            return this;
        }

        public FiroCategoryBuilder secureAccessFunc(SecureAccessFunc secureAccessFunc) {
            this.secureAccessFunc = secureAccessFunc;
            return this;
        }

        public FiroCategory build() {
            if (code == null) {
                code = "default";
            }
            if (directUrl == null) {
                directUrl = domain.getDirectUrl();
            }

            if (directoryPathPolicy == null) {
                directoryPathPolicy = domain.getDirectoryPathPolicy();
            }

            if (adapter == null) {
                adapter = domain.getAdapter();
            }

            if (secureAccessFunc == null) {
                secureAccessFunc = domain.getSecureAccessFunc();
            }

            return new FiroCategory(domain, code, directUrl, adapter, directoryPathPolicy, filterChain, secureAccessFunc);
        }
    }

    public File readTemp(String path) throws Exception {
        return adapter.readTemp(this.directoryPathPolicy, path);
    }

    public File read(String path) throws Exception {
        return adapter.read(this.directoryPathPolicy, path);
    }

    public File writeTemp(String path, InputStream in, long size, String contentType) throws Exception {
        return adapter.writeTemp(this.directoryPathPolicy, path, in, size, contentType);
    }

    public void write(String fullDir, String path, InputStream in, long size, String contentType) throws Exception {
        adapter.write(this.directoryPathPolicy, fullDir, path, in, size, contentType);
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
        return this.directoryPathPolicy.getFullDir(this.domain.getCode(), this.getCode(), date);
    }
}
