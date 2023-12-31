package com.unvus.firo.module.service;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.adapter.Adapter;
import com.unvus.firo.module.adapter.AdapterType;
import com.unvus.firo.module.policy.DirectoryPathPolicy;
import com.unvus.firo.module.policy.impl.DateDirectoryPathPolicy;
import com.unvus.firo.module.service.domain.FiroCategory;
import com.unvus.firo.module.service.domain.FiroDomain;
import com.unvus.firo.module.service.domain.SecureAccessFunc;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

@Component
public class FiroRegistry {

    private final static FiroRegistry INSTANCE = new FiroRegistry();

    public static final String _DEFAULT_CATEGORY_NAME = "default";

    protected static PluginRegistry<Adapter, AdapterType> adapterPluginRegistry;

    protected static Map<String, FiroDomain> domainMap = new HashMap();

    protected static String defaultDirectUrl;

    protected static AdapterType defaultAdapterType = AdapterType.LOCAL;

    protected static Adapter defaultAdapter;

    protected static DirectoryPathPolicy directoryPathPolicy;

    protected static DirectoryPathPolicy localDirectoryPathPolicy;

    protected static SecureAccessFunc defaultSecureAccessFunc = null;

    protected static FiroProperties firoProperties;

    protected static String secret = "mExYTViNzQzOTE3YmQ4OWY3NTE4MmRkOTg2YmM2NjAyMjVjZTNjNjFkYzZjRhOTlhYWVhNGRjNT";

    @Inject
    @Qualifier("adapterRegistry")
    public void setAdapterPluginRegistry(PluginRegistry pluginRegistry) {
        this.adapterPluginRegistry = pluginRegistry;
    }

    @Inject
    public void setFiroProperties(FiroProperties firoProperties) {
        this.firoProperties = firoProperties;
    }

    public static FiroRegistry defaults(FiroProperties props, DirectoryPathPolicy directoryPathPolicy) {
        FiroRegistry.defaultDirectUrl = props.getDirectUrl();
        FiroRegistry.directoryPathPolicy = directoryPathPolicy;
        FiroRegistry.defaultAdapter = getAdapter(defaultAdapterType);

        return INSTANCE;
    }

    public static FiroRegistry defaults(FiroProperties props, AdapterType adapterType) {
        FiroRegistry.defaultDirectUrl = props.getDirectUrl();
        FiroRegistry.directoryPathPolicy = buildDirectoryPathPolicy(props.getDirectory());
        FiroRegistry.defaultAdapter = getAdapter(adapterType);

        FiroRegistry.localDirectoryPathPolicy = buildDirectoryPathPolicy(props.getLocal().getDirectory());
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

    public static String getDirectUrl(String domainCode, String categoryCode) {
        String result = defaultDirectUrl;
        if (StringUtils.isBlank(domainCode)) {
            return result;
        }

        FiroDomain domain = get(domainCode);
        if (domain != null) {
            result = domain.getDirectUrl();
            if (StringUtils.isBlank(categoryCode)) {
                return result;
            }
            FiroCategory category = domain.getCategory(categoryCode);
            if (category != null) {
                result = category.getDirectUrl();
            }
            if(result == null) {
                result = category.getAdapter().getDirectUrl();
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

    public static DirectoryPathPolicy getLocalDirectoryPathPolicy() {
        return FiroRegistry.localDirectoryPathPolicy;
    }

    public static Map<String, FiroDomain> getAllDomain() {
        return domainMap;
    }

    public static FiroDomain get(String domainCode) {
        return domainMap.containsKey(domainCode) ? domainMap.get(domainCode) : createDefaultDomain(domainCode);
    }

    public static FiroCategory get(String domainCode, String categoryCode) {
        FiroDomain firoDomain = FiroRegistry.get(domainCode);
        FiroCategory category = firoDomain.getCategory(categoryCode);
        if (category == null) {
            category = FiroCategory.builder(firoDomain, categoryCode).build();
            firoDomain.addCategory(category);
        }
        return category;
    }


    public static String getSecret() {
        return FiroRegistry.secret;
    }

    public static FiroRegistry add(FiroDomain firoDomain) {
        if (firoDomain.getAdapter() == null) {
            firoDomain.setAdapter(getDefaultAdapter());
        }

        domainMap.put(firoDomain.getCode(), firoDomain);
        return INSTANCE;
    }

    protected static FiroDomain createDefaultDomain(String domainCode) {
        FiroDomain domain;
        if(defaultAdapter.getAdapterType() == AdapterType.LOCAL) {
            domain = FiroDomain.builder(domainCode).build();
        }else {
            FiroProperties.AdapterProp prop = firoProperties.getAdapterProp(defaultAdapter.getAdapterType());

            domain = FiroDomain.builder(domainCode).directoryPathPolicy(new DateDirectoryPathPolicy(
                DateDirectoryPathPolicy.DATE_SUBDIR_TYPE.YYYY_MM,
                prop.getDirectory().getBaseDir(),
                prop.getDirectory().getTmpDir()
            )).build();
        }

        domainMap.put(domainCode, domain);

        return domain;
    }

    public static DirectoryPathPolicy buildDirectoryPathPolicy(FiroProperties.Directory directory) {
        return new DateDirectoryPathPolicy(
            DateDirectoryPathPolicy.DATE_SUBDIR_TYPE.YYYY_MM,
            directory.getBaseDir(),
            directory.getTmpDir(),
            directory.getSeparator()
        );
    }

}
