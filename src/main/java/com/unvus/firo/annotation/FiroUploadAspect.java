package com.unvus.firo.annotation;

import com.unvus.firo.module.service.domain.AttachContainer;
import com.unvus.firo.module.service.FiroService;
import com.unvus.firo.util.FiroWebUtil;
import com.unvus.util.DateTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
            extractFiroDomainObject(domain, targetList);

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
            FiroDomain parameterAnnotation = null;
            for(Annotation[] methodAnnotations : method.getParameterAnnotations()) {
                for(Annotation methodAnnotation : methodAnnotations) {
                    if(methodAnnotation.annotationType().equals(FiroDomain.class)) {
                        parameterAnnotation = (FiroDomain)methodAnnotation;
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
                extractFiroDomainObject(args[idx], targetList);
                upload(targetList, parameterAnnotation);
            }else {
                for(Object arg: args) {
                    if(arg.getClass().isAnnotationPresent(FiroDomain.class)) {
                        extractFiroDomainObject(arg, targetList);
                    }
                }

                upload(targetList, null);
            }

        } finally {
            return result;
        }

    }


    private void upload(List<Object> targetList, FiroDomain firoDomainForArgs) throws Exception {
        AttachContainer attachContainer = FiroWebUtil.getAttachContainer();
        int index = 0;
        for(Object target: targetList) {
            Class klass = target.getClass();

            FiroDomain firoDomain = (FiroDomain) klass.getAnnotation(FiroDomain.class);

            if(index == 0 && firoDomainForArgs != null) {
                firoDomain = firoDomainForArgs;
            }

            Field domainKeyField = getAnnotatedField(target, FiroDomainKey.class);
            Long refKey;
            if(domainKeyField != null) {
                refKey = (Long)PropertyUtils.getProperty(target, domainKeyField.getName());
            }else {

                refKey = (Long)PropertyUtils.getProperty(target, firoDomain.keyFieldName());
            }

            Field domainDateField = getAnnotatedField(target, FiroDomainDate.class);
            Object date;
            if (domainDateField != null) {
                date = PropertyUtils.getProperty(target, domainDateField.getName());
            } else {
                date = PropertyUtils.getProperty(target, firoDomain.dateFieldName());
            }
            if(date instanceof LocalDate) {
                date = DateTools.convert((LocalDate) date, DateTools.ConvertTo.LOCAL_DATE_TIME);
            }

            firoService.save(refKey, attachContainer.get(firoDomain.value()), (LocalDateTime)date);
            index++;
        }
    }

    private void extractFiroDomainObject(Object source, List<Object> resultList) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if(source != null && source.getClass().isAnnotationPresent(FiroDomain.class)) {
            resultList.add(source);
        }
        for(Field field  : source.getClass().getDeclaredFields()) {
            Class klass = field.getClass();
            if(!klass.isPrimitive() && !klass.getPackage().getName().startsWith("java.")) {
                extractFiroDomainObject(PropertyUtils.getProperty(source, field.getName()), resultList);
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
