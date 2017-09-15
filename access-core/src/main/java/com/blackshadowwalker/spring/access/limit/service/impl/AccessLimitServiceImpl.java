package com.blackshadowwalker.spring.access.limit.service.impl;

import com.blackshadowwalker.spring.access.annotation.AccessLimit;
import com.blackshadowwalker.spring.access.annotation.AccessLimits;
import com.blackshadowwalker.spring.access.bean.AccessLimitOperation;
import com.blackshadowwalker.spring.access.exp.AccessLimitException;
import com.blackshadowwalker.spring.access.limit.advice.AccessLimitExpressionEvaluator;
import com.blackshadowwalker.spring.access.limit.service.AccessLimitService;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author : LI-JIAN
 * Date   : 2017-07-17
 */
@Component
public class AccessLimitServiceImpl implements AccessLimitService {

    private static final DateFormat yyyy_MM_dd_HH_mm_ss_SSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private AccessLimitExpressionEvaluator evaluator = new AccessLimitExpressionEvaluator();

    @Autowired
    RedisTemplate<String, Object> redis;

    @Value("${access.limit.prefix}")
    private String accessLimitKeyPrefix;

    private String accessHitLimitCache = "HitCache";//命中后的结果缓存，不用每次都计算了

    @Override
    public List<AccessLimitOperation> parseLimit(Object target, Method method, Object[] args) {
        Class<?> targetClass = getTargetClass(target);
        return this.parseAnnotations(targetClass, target, method, method, args);
    }

    @Override
    public void checkHitLimit(List<AccessLimitOperation> limitList) {
        if (CollectionUtils.isEmpty(limitList)) {
            return;
        }
        long timestamp = System.currentTimeMillis();
        for (AccessLimitOperation op : limitList) {
            if (op.getLimit() == -1) {
                continue;
            }
            //命中访问控制缓存检查
            String hitLimitKey = accessHitLimitCache + op.getValue() + ":" + op.getKey();
            if (op.getDelayExpireOnHit() && redis.hasKey(hitLimitKey)) {
                Long ttl = redis.getExpire(hitLimitKey);
                ttl = op.getDelayExpireStep() + ((ttl == null || ttl < 0) ? 1 : ttl);
                redis.expire(hitLimitKey, ttl, TimeUnit.SECONDS);
                throw new AccessLimitException(op.getErrorMsg().isEmpty() ? "请求频繁，请稍后重试" : op.getErrorMsg());
            }

            //访问记录分析
            String key = accessLimitKeyPrefix + op.getValue() + ":" + op.getKey();
            long timeoutEndTime = timestamp - op.getUnitTime() * 1000;
            Long size = redis.opsForZSet().count(key, timeoutEndTime, timestamp);
            if (size != null && size >= op.getLimit()) {
                if (op.getDelayExpireOnHit()) {
                    String value = yyyy_MM_dd_HH_mm_ss_SSS.format(new Date());
                    redis.opsForValue().set(hitLimitKey, value, op.getDelayExpireStep(), TimeUnit.SECONDS);
                }
                throw new AccessLimitException(op.getErrorMsg().isEmpty() ? "请求频繁，请稍后重试" : op.getErrorMsg(), op);
            }

        }
    }

    /**
     * 校验并记录访问
     *
     * @param limitList
     */
    @Override
    public void checkHitLimitAndRecord(List<AccessLimitOperation> limitList) {
        this.checkHitLimit(limitList);
        this.recordAccess(limitList);
    }

    /**
     * 记录访问
     *
     * @param limitList
     */
    @Override
    public void recordAccess(List<AccessLimitOperation> limitList) {
        if (CollectionUtils.isEmpty(limitList)) {
            return;
        }
        long timestamp = System.currentTimeMillis();
        String value = yyyy_MM_dd_HH_mm_ss_SSS.format(new Date());
        for (AccessLimitOperation op : limitList) {
            String key = accessLimitKeyPrefix + op.getValue() + ":" + op.getKey();
            long timeoutEndTime = timestamp - op.getUnitTime() * 1000;
            redis.opsForZSet().add(key, value, timestamp);//add new
            redis.opsForZSet().removeRangeByScore(key, Long.MIN_VALUE, timeoutEndTime);//remove older
            redis.expire(key, op.getUnitTime(), TimeUnit.SECONDS);
        }
    }

    //====================================================================
    private Class<?> getTargetClass(Object target) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        if (targetClass == null) {
            targetClass = target.getClass();
        }
        return targetClass;
    }

    private Object[] extractArgs(Method method, Object[] args) {
        if (!method.isVarArgs()) {
            return args;
        }
        Object[] varArgs = ObjectUtils.toObjectArray(args[args.length - 1]);
        Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
        System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
        System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
        return combinedArgs;
    }

    private List<AccessLimitOperation> parseAnnotations(Class<?> targetClass, Object target, AnnotatedElement ae, Method method, Object[] _args) {
        List<AccessLimitOperation> list = new ArrayList<AccessLimitOperation>();
        AccessLimit limit = ae.getAnnotation(AccessLimit.class);
        AccessLimits limits = ae.getAnnotation(AccessLimits.class);

        if (limit == null && (limits == null || limits.value().length == 0)) {
            return null;
        }
        List<AccessLimit> limitList = new ArrayList<>();
        if (limit != null) {
            limitList.add(limit);
        }
        if (limits != null && limits.value().length > 0) {
            limitList.addAll(Arrays.asList(limits.value()));
        }
        Object[] args = this.extractArgs(method, _args);
        for (AccessLimit item : limitList) {
            EvaluationContext evaluationContext = evaluator.createEvaluationContext(new ArrayList<AccessLimitOperation>(), target, targetClass, method, args);
            String limitKey = String.valueOf(evaluator.key(item.key(), new AnnotatedElementKey(method, targetClass), evaluationContext));
            AccessLimitOperation op = new AccessLimitOperation(item.value(), limitKey, item.limit(), item.unitTime(), item.errorMsg());
            list.add(op);
        }
        return list;
    }

}
