package com.unvus.firo.util;

import com.unvus.firo.annotation.FiroDomainDate;
import com.unvus.firo.annotation.FiroDomainKey;
import com.unvus.firo.module.service.FiroRegistry;
import com.unvus.firo.module.service.domain.AttachBag;
import com.unvus.firo.module.service.domain.FiroCategory;
import com.unvus.firo.module.service.domain.FiroFile;
import com.unvus.util.DateTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;

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

    public static com.unvus.firo.annotation.FiroDomain getFiroDomain(Object obj) {
        Class klass = obj.getClass();
        while (klass != null) {
            Annotation annotation = klass.getAnnotation(com.unvus.firo.annotation.FiroDomain.class);
            if(annotation != null) {
                return (com.unvus.firo.annotation.FiroDomain)annotation;
            }
            klass = klass.getSuperclass();
        }
        return null;
    }

    public static Long getFiroDomainKey(Object obj) throws Exception {
        return getFiroDomainKey(obj, getFiroDomain(obj));
    }

    public static Long getFiroDomainKey(Object obj, com.unvus.firo.annotation.FiroDomain firoDomain) throws Exception {
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

    public static LocalDateTime getFiroDomainDt(Object obj, com.unvus.firo.annotation.FiroDomain firoDomain) {
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

    public static List<FiroFile> injectDirectUrlToFireFile(Object obj, List<FiroFile> fileList) throws Exception {
        int idx = 0;
        if(fileList == null) {
            return null;
        }
        for(FiroFile ff : fileList) {
            try {
                ff.setDirectUrl(directUrl(obj, ff.getRefTargetType(), idx++));
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return fileList;
    }

    public static String directUrl(Object obj, String category) throws Exception {
        return directUrl(obj, category, 0);
    }

    public static String directUrl(Object obj, String category, int index) throws Exception {
        com.unvus.firo.annotation.FiroDomain firoDomain = FiroUtil.getFiroDomain(obj);
        return directUrl(obj, category, FiroUtil.getFiroDomainKey(obj, firoDomain).toString(), FiroUtil.getFiroDomainDt(obj, firoDomain), index, null);
    }

    public static String directUrl(Object obj, String category, String domainId, LocalDateTime createdDt, Integer index, Object cacheValue) throws Exception {

        com.unvus.firo.annotation.FiroDomain firoDomain = FiroUtil.getFiroDomain(obj);
        String domain = firoDomain.value();

        FiroCategory firoCategory = FiroRegistry.get(domain, category);

        if (category == null) {
            category = "default";
        }

        String cache = null;
        if (cacheValue == null) {
            cacheValue = PropertyUtils.getProperty(obj, "modifiedDt");
        }
        if (cacheValue instanceof LocalDateTime) {
            cache = DateTools.convert((LocalDateTime) cacheValue, DateTools.ConvertTo.LONG).toString();
        }else if(cacheValue != null) {
            cache = cacheValue.toString();
        }

        String directUrl = FiroRegistry.getDirectUrl(domain, category);
        if(directUrl != null) {
            Path path =
                Paths.get(
                    directUrl,
                    domain,
                    DateTimeFormatter.ofPattern("yyyy/MM").format(createdDt),
                    domainId,
                    SecureNameUtil.gen(firoCategory, domainId, index)
                );

            return path.toString() + (cache == null?"":"?cache=" + cache);
        }else {
            return "/assets/firo/attach/view/" + domain + "/" + domainId + "/" + category + "/" + index;
        }


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

        String directUrl = FiroRegistry.getDirectUrl(domain, category);
        if(directUrl != null) {
            return directUrl + path.toString();
        }else {
            return "/assets/firo/attach/view/" + domain + "/" + domainId + "/" + category + "/" + index;
        }

    }
}
