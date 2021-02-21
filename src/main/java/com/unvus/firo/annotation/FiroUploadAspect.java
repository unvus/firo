package com.unvus.firo.annotation;

import com.unvus.firo.module.service.domain.AttachBag;
import com.unvus.firo.module.service.domain.AttachContainer;
import com.unvus.firo.module.service.FiroService;
import com.unvus.firo.util.FiroWebUtil;
import com.unvus.util.DateTools;
import com.unvus.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            upload(targetList, null);
        } finally {
            return result;
        }
    }

    @Around("@annotation(firoUpload)")
    public Object pointcut(ProceedingJoinPoint joinPoint, FiroUpload firoUpload) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            Object[] args = joinPoint.getArgs();

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            int idx = 0;
            FiroRoom parameterAnnotation = null;
            for(Annotation[] methodAnnotations : method.getParameterAnnotations()) {
                for(Annotation methodAnnotation : methodAnnotations) {
                    if(methodAnnotation.annotationType().equals(FiroRoom.class)) {
                        parameterAnnotation = (FiroRoom)methodAnnotation;
                        break;
                    }
                }
                if(parameterAnnotation != null) {
                    break;
                }
                idx++;
            }

            List<Object> targetList = new ArrayList<>();

            if(parameterAnnotation != null) {
                targetList.add(args[idx]);
                extractFiroRoomObject(args[idx], targetList);
                upload(targetList, parameterAnnotation);
            }else {
                for(Object arg: args) {
                    if(arg.getClass().isAnnotationPresent(FiroRoom.class)) {
                        extractFiroRoomObject(arg, targetList);
                    }
                }

                upload(targetList, null);
            }

        } finally {
            return result;
        }

    }


    private void upload(List<Object> targetList, FiroRoom firoRoomForArgs) throws Exception {
        AttachContainer attachContainer = FiroWebUtil.getAttachContainer();
        int index = 0;
        for(Object target: targetList) {
            Class klass = target.getClass();

            FiroRoom firoRoom = (FiroRoom) klass.getAnnotation(FiroRoom.class);

            if(index == 0 && firoRoomForArgs != null) {
                firoRoom = firoRoomForArgs;
            }

            Field roomKeyField = getAnnotatedField(target, FiroRoomKey.class);
            Long refKey;
            if(roomKeyField != null) {
                refKey = (Long)PropertyUtils.getProperty(target, roomKeyField.getName());
            }else {

                refKey = (Long)PropertyUtils.getProperty(target, firoRoom.keyFieldName());
            }

            Field roomDateField = getAnnotatedField(target, FiroRoomDate.class);
            Object date;
            if (roomDateField != null) {
                date = PropertyUtils.getProperty(target, roomDateField.getName());
            } else {
                date = PropertyUtils.getProperty(target, firoRoom.dateFieldName());
            }
            if(date instanceof LocalDate) {
                date = DateTools.convert((LocalDate) date, DateTools.ConvertTo.LOCAL_DATE_TIME);
            }

            firoService.save(refKey, attachContainer.get(firoRoom.value()), (LocalDateTime)date);
            index++;
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

}
