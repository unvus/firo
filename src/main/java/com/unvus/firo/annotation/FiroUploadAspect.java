package com.unvus.firo.annotation;

import com.unvus.firo.module.service.domain.AttachContainer;
import com.unvus.firo.module.service.FiroService;
import com.unvus.firo.util.FiroUtil;
import com.unvus.firo.util.FiroWebUtil;
import com.unvus.util.FieldMap;
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
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FiroUploadAspect {

    private final FiroService firoService;

    @Around("execution(public * *(.., @com.unvus.firo.annotation.FiroUpload (*), ..)) && args(domain, ..)")
    public Object pointcut(ProceedingJoinPoint joinPoint, Object domain) throws Throwable {
        FieldMap bodyMap = FiroWebUtil.getRequestBodyMap();
        Object result = joinPoint.proceed();
        try {
            Map<Object, AttachContainer> targetMap = new HashMap<>();
            extractFiroDomainObject(domain, targetMap, bodyMap);

            upload(targetMap, null);
        } finally {
            return result;
        }
    }

    @Around("@annotation(firoUpload)")
    public Object pointcut(ProceedingJoinPoint joinPoint, FiroUpload firoUpload) throws Throwable {
        FieldMap bodyMap = FiroWebUtil.getRequestBodyMap();

        // execute method
        Object result = joinPoint.proceed();

        if(firoUpload.legacy()) {
            return result;
        }

        try {
            Object[] args = joinPoint.getArgs();

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            // 메소드 파라미터 중 파라미터 레벨에 FiroDomain 어노테이션이 선언되어 있는 파라미터 구하기
            int idx = 0;
            FiroDomain parameterAnnotation = null;
            for (Annotation[] methodAnnotations : method.getParameterAnnotations()) {
                for (Annotation methodAnnotation : methodAnnotations) {
                    if (methodAnnotation.annotationType().equals(FiroDomain.class)) {
                        parameterAnnotation = (FiroDomain) methodAnnotation;
                        break;
                    }
                }
                if (parameterAnnotation != null) {
                    break;
                }
                idx++;
            }

            Map<Object, AttachContainer> targetMap = new HashMap<>();

            if (parameterAnnotation != null) {
                targetMap.put(args[idx], FiroWebUtil.getAttachContainer(bodyMap));
                extractFiroDomainObject(args[idx], targetMap, bodyMap);
            } else {
                // 메소드 파라미터 중 클래스 레벨에 FiroDomain 어노테이션이 선언되어 있는 파라미터 구하기
                for (Object arg : args) {
                    if (arg.getClass().isAnnotationPresent(FiroDomain.class)) {
                        extractFiroDomainObject(arg, targetMap, bodyMap);
                    }
                }
            }

            upload(targetMap, null);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }

    }

    private void upload(Map<Object, AttachContainer> targetMap, FiroDomain firoDomainForArgs) throws Exception {

        int index = 0;
        for (Map.Entry<Object, AttachContainer> entry : targetMap.entrySet()) {
            Object target = entry.getKey();
            AttachContainer attachContainer = entry.getValue();

            FiroDomain firoDomain = FiroUtil.getFiroDomain(target);

            if (index == 0 && firoDomainForArgs != null) {
                firoDomain = firoDomainForArgs;
            }

            // 현재 도메인 객체의 PK 값 얻기
            Long refKey = FiroUtil.getFiroDomainKey(target, firoDomain);

            // 현재 도메인 객체의 생성일시 값 얻기
            LocalDateTime date = FiroUtil.getFiroDomainDt(target, firoDomain);

            firoService.save(refKey, attachContainer.get(firoDomain.value()), date);
            index++;
        }
    }

    private void extractFiroDomainObject(Object source, Map<Object, AttachContainer> targetMap, Object bodyMap) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if (source == null) {
            return;
        }

        if (source instanceof List<?>) {
            // source 가 배열이면 iterate 돌면서 재귀호출
            int idx = 0;
            for (Object item : (Collection<?>) source) {
                extractFiroDomainObject(item, targetMap, ((List<?>)bodyMap).get(idx++));
            }
        } else if (source.getClass().isAnnotationPresent(FiroDomain.class)) {
            // source 개체에 FiroDomain 이 선언되어 있으면 대상에 추가
            if(!targetMap.containsKey(source)) {
                targetMap.put(source, FiroWebUtil.getAttachContainer((Map) bodyMap));
            }

            // 현재 객체의 각 필드별로 FiroDomain 후보 색출 및 재귀호출
            for (Field field : source.getClass().getDeclaredFields()) {
                Class<?> klass = field.getClass();

                if (Collection.class.isAssignableFrom(field.getType())) {
                    // 필드가 배열이면 iterate 돌면서 재귀 호출
                    Collection<?> items = (Collection<?>) PropertyUtils.getProperty(source, field.getName());
                    if (items == null) {
                        continue;
                    }
                    List<?> bodyItemList = (List<?>) ((Map) bodyMap).get(field.getName());

                    if (bodyItemList == null) {
                        continue;
                    }

                    if(items.size() != bodyItemList.size()) {
                        continue;
                    }

                    int idx = 0;
                    for (Object item : items) {
                        extractFiroDomainObject(item, targetMap, bodyItemList.get(idx++));
                    }
                } else if (!klass.isPrimitive() && !klass.getPackage().getName().startsWith("java.")) {
                    // 필드가 자바 기본 타입이 아니라면 해당 값을 가지고 재귀호출
                    extractFiroDomainObject(PropertyUtils.getProperty(source, field.getName()), targetMap, ((Map) bodyMap).get(field.getName()));
                }
            }
        }
    }
}
