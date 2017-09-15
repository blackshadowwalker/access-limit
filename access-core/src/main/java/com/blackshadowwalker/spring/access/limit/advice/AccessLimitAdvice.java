package com.blackshadowwalker.spring.access.limit.advice;

import com.blackshadowwalker.spring.access.bean.AccessLimitOperation;
import com.blackshadowwalker.spring.access.limit.service.AccessLimitService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Author : LI-JIAN
 * Date   : 2017-07-17
 */
@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AccessLimitAdvice {

    @Pointcut("@annotation(com.blackshadowwalker.spring.access.annotation.AccessLimit)")
    private void point() {
    }

    @Pointcut("@annotation(com.blackshadowwalker.spring.access.annotation.AccessLimits)")
    private void points() {
    }

    @Resource
    private AccessLimitService accessLimitService;

    @Around("point() || points()")
    public Object around(final ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        List<AccessLimitOperation> ops = accessLimitService.parseLimit(pjp.getTarget(), method, pjp.getArgs());

        accessLimitService.checkHitLimit(ops);
        Object ret = pjp.proceed();
        accessLimitService.recordAccess(ops);
        return ret;
    }

}
