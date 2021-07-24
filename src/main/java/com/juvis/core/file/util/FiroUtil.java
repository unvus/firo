package com.juvis.core.file.util;

import com.juvis.core.file.annotation.FiroDomain;
import com.juvis.core.file.annotation.FiroDomainDate;
import com.juvis.core.file.annotation.FiroDomainKey;
import com.juvis.core.file.module.service.FiroRegistry;
import com.juvis.core.file.module.service.domain.FiroCategory;
import com.unvus.util.DateTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class FiroUtil {


    public static Field getAnnotatedField(Object obj, Class annotationClass) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                return field;
            }
        }
        return null;
    }

    public static FiroDomain getFiroDomain(Object obj) {
        return obj.getClass().getAnnotation(FiroDomain.class);
    }


    public static Long getFiroDomainKey(Object obj) throws Exception {
        return getFiroDomainKey(obj, getFiroDomain(obj));
    }

    public static Long getFiroDomainKey(Object obj, FiroDomain firoDomain) throws Exception {
        Field domainKeyField = FiroUtil.getAnnotatedField(obj, FiroDomainKey.class);
        Long refKey;
        if (domainKeyField != null) {
            refKey = (Long) PropertyUtils.getProperty(obj, domainKeyField.getName());
        } else {

            refKey = (Long) PropertyUtils.getProperty(obj, firoDomain.keyFieldName());
        }
        return refKey;
    }

    public static LocalDateTime getFiroDomainDt(Object obj) {
        return getFiroDomainDt(obj, getFiroDomain(obj));
    }

    public static LocalDateTime getFiroDomainDt(Object obj, FiroDomain firoDomain) {
        Field domainDateField = FiroUtil.getAnnotatedField(obj, FiroDomainDate.class);
        Object date;
        try {
            if (domainDateField != null) {
                date = PropertyUtils.getProperty(obj, domainDateField.getName());
            } else {
                date = PropertyUtils.getProperty(obj, firoDomain.dateFieldName());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            date = LocalDateTime.now();
        }

        if (date instanceof LocalDate) {
            date = DateTools.convert((LocalDate) date, DateTools.ConvertTo.LOCAL_DATE_TIME);
        }
        return (LocalDateTime) date;
    }

    public static String directUrl(Object obj, String category) throws Exception {
        return directUrl(obj, category, 0);
    }

    public static String directUrl(Object obj, String category, int index) throws Exception {
        FiroDomain firoDomain = FiroUtil.getFiroDomain(obj);
        return directUrl(obj, category, FiroUtil.getFiroDomainKey(obj, firoDomain).toString(), FiroUtil.getFiroDomainDt(obj, firoDomain), index, null);
    }

    public static String directUrl(Object obj, String category, String domainId, LocalDateTime createdDt, Integer index, Object cacheValue) throws Exception {

        FiroDomain firoDomain = FiroUtil.getFiroDomain(obj);
        String domain = firoDomain.value();

        FiroCategory firoCategory = FiroRegistry.get(domain, category);

        if (category == null) {
            category = "default";
        }

        String cache;
        if (cacheValue == null) {
            cacheValue = PropertyUtils.getProperty(obj, "modifiedDt");
        }

        if (cacheValue instanceof LocalDateTime) {
            cache = DateTools.convert((LocalDateTime) cacheValue, DateTools.ConvertTo.LONG).toString();
        } else {
            cache = cacheValue.toString();
        }

        Path path =
            Paths.get(
                FiroRegistry.getDirectUrl(domain, category),
                domain,
                DateTimeFormatter.ofPattern("yyyy/MM").format(createdDt),
                domainId,
                SecureNameUtil.gen(firoCategory, domainId, index)
            );

        return path.toString() + "?cache=" + cache;
    }

    public static String directUrl(String domain, String category, String domainId, LocalDateTime createdDt, Integer index) throws Exception {

        FiroCategory firoCategory = FiroRegistry.get(domain, category);

        if (category == null) {
            category = "default";
        }

        Path path =
            Paths.get(
                domain,
                DateTimeFormatter.ofPattern("yyyy/MM").format(createdDt),
                domainId,
                SecureNameUtil.gen(firoCategory, domainId, index)
            );

        return FiroRegistry.getDirectUrl(domain, category) + path.toString();
    }
}
