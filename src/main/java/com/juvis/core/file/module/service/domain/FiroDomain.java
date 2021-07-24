package com.juvis.core.file.module.service.domain;

import com.juvis.core.file.module.policy.DirectoryPathPolicy;
import com.juvis.core.file.module.adapter.Adapter;
import com.juvis.core.file.module.service.FiroRegistry;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FiroDomain {

    private String code;

    private String directUrl;

    private Adapter adapter;

    private DirectoryPathPolicy directoryPathPolicy;

    private Map<String, FiroCategory> categoryMap = new HashMap();

    private SecureAccessFunc secureAccessFunc;

    private FiroDomain(String code) {
        this.code = code;
    }

    private FiroDomain(String code, String directUrl, Adapter adapter, DirectoryPathPolicy directoryPathPolicy, SecureAccessFunc secureAccessFunc) {
        this.code = code;
        this.directUrl = directUrl;
        this.adapter = adapter;
        this.directoryPathPolicy = directoryPathPolicy;
        this.secureAccessFunc = secureAccessFunc;
    }

    public FiroDomain addCategory(String code) {
        FiroCategory category = FiroCategory
            .builder(this, code)
            .directoryPathPolicy(directoryPathPolicy)
            .build();

        categoryMap.put(code, category);
        return this;
    }

    public FiroDomain addCategory(FiroCategory category) {
        if(!this.code.equals(category.getDomain().getCode())) {
            throw new RuntimeException("does not match for this domain : invalid domain code");
        }
        categoryMap.put(category.getCode(), category);
        return this;
    }

    public FiroCategory getCategory(String code) {
        if(code == null) {
            return null;
        }
        return categoryMap.get(code);
    }

    public Map<String, FiroCategory> getAllCategory() {
        return categoryMap;
    }

    public static FiroDomainBuilder builder(String code) {
        return new FiroDomainBuilder(code);
    }

    public static class FiroDomainBuilder {
        private String code;

        private String directUrl;

        private Adapter adapter;

        private DirectoryPathPolicy directoryPathPolicy;

        private SecureAccessFunc secureAccessFunc;

        public FiroDomainBuilder(String code) {
            this.code = code;
        }

        public FiroDomainBuilder directUrl(String directUrl) {
            this.directUrl = directUrl;
            return this;
        }

        public FiroDomainBuilder adapter(Adapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public FiroDomainBuilder directoryPathPolicy(DirectoryPathPolicy directoryPathPolicy) {
            this.directoryPathPolicy = directoryPathPolicy;
            return this;
        }

        public FiroDomainBuilder secureAccessFunc(SecureAccessFunc secureAccessFunc) {
            this.secureAccessFunc = secureAccessFunc;
            return this;
        }

        public FiroDomain build() {
            if (directUrl == null) {
                directUrl = FiroRegistry.getDefaultDirectUrl();
            }

            if (directoryPathPolicy == null) {
                directoryPathPolicy = FiroRegistry.getDefaultDirectoryPathPolicy();
            }

            if (adapter == null) {
                adapter = FiroRegistry.getDefaultAdapter();
            }

            if (secureAccessFunc == null) {
                secureAccessFunc = FiroRegistry.getDefaultSecureAccessFunc();
            }

            FiroDomain domain = new FiroDomain(code, directUrl, adapter, directoryPathPolicy, secureAccessFunc);
            domain.addCategory(FiroRegistry._DEFAULT_CATEGORY_NAME);
            return domain;
        }
    }

}
