package com.unvus.firo.embedded.annotation;

import com.unvus.firo.embedded.domain.AttachBag;
import com.unvus.firo.embedded.domain.AttachContainer;
import com.unvus.firo.embedded.service.FiroService;
import com.unvus.firo.embedded.util.FiroWebUtil;
import com.unvus.util.DateTools;
import com.unvus.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FiroUploadAspect {

    private final FiroService firoService;

    @Around("execution(public * *(.., @FiroUpload (*), ..)) && args(domain, ..)")
    public Object pointcut(ProceedingJoinPoint joinPoint, Object domain) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            List<Object> targetList = new ArrayList<>();
            extractFiroRoomObject(domain, targetList);

            upload(targetList);
        } finally {
            return result;
        }
    }

    @Around("@annotation(firoUpload)")
    public Object pointcut(ProceedingJoinPoint joinPoint, FiroUpload firoUpload) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            Object[] args = joinPoint.getArgs();
            List<Object> targetList = new ArrayList<>();
            for(Object arg: args) {
                if(arg.getClass().isAnnotationPresent(FiroRoom.class)) {
                    extractFiroRoomObject(arg, targetList);
                }
            }

            upload(targetList);

        } finally {
            return result;
        }

    }

    private void upload(List<Object> targetList) throws Exception {
        AttachContainer attachContainer = getAttachContainer();
        for(Object target: targetList) {
            Class klass = target.getClass();
            FiroRoom firoRoom = (FiroRoom) klass.getAnnotation(FiroRoom.class);
            Field roomKeyField = getAnnotatedField(target, FiroRoomKey.class);
            Long refKey;
            if(roomKeyField != null) {
                refKey = (Long)PropertyUtils.getProperty(target, roomKeyField.getName());
            }else {

                refKey = (Long)PropertyUtils.getProperty(target, firoRoom.keyFieldName());
            }

            Field roomDateField = getAnnotatedField(target, FiroRoomDate.class);
            Object date;
            if (roomKeyField != null) {
                date = PropertyUtils.getProperty(target, roomDateField.getName());
            } else {
                date = PropertyUtils.getProperty(target, firoRoom.dateFieldName());
            }
            if(date instanceof LocalDate) {
                date = DateTools.convert((LocalDate) date, DateTools.ConvertTo.LOCAL_DATE_TIME);
            }

            firoService.save(refKey, attachContainer.get(firoRoom.value()), (LocalDateTime)date);
        }
    }

    private void extractFiroRoomObject(Object source, List<Object> resultList) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if(source != null && source.getClass().isAnnotationPresent(FiroRoom.class)) {
            resultList.add(source);
        }
        for(Field field  : source.getClass().getDeclaredFields()) {
            Class klass = field.getClass();
            if(!klass.isPrimitive() && !klass.getPackage().getName().startsWith("java.")) {
                extractFiroRoomObject(PropertyUtils.getProperty(source, field.getName()), resultList);
            }
        }
    }

    private Field getAnnotatedField(Object obj, Class annotationClass) {
        for(Field field  : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                return field;
            }
        }
        return null;
    }

    private AttachContainer getAttachContainer() throws IOException {
        HttpServletRequest request = FiroWebUtil.request();

        ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;

        String body = new String(requestWrapper.getContentAsByteArray());

//        String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        AttachContainer attachContainer = new AttachContainer();
        Map<String, Map> map = (Map)JsonUtil.toMap(body).get("attachContainer");

        for(Map.Entry<String, Map> entry: map.entrySet()) {
            AttachBag attachBag = JsonUtil.toObject(entry.getValue(), AttachBag.class);
            attachBag.setRoomCode(entry.getKey());
            attachContainer.put(entry.getKey(), attachBag);
        }

        return attachContainer;
    }
}
