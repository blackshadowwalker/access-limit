package com.blackshadowwalker.spring.access.limit.service;

import com.blackshadowwalker.spring.access.bean.AccessLimitOperation;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Author : LI-JIAN
 * Date   : 2017-07-17
 * 访问限制
 */
public interface AccessLimitService {

    /**
     * 解析limit
     * @param target
     * @param method
     * @param args
     * @return
     */
    List<AccessLimitOperation> parseLimit(Object target, Method method, Object[] args);

    /**
     * 验证是否命中限制规则
     * @param limitList
     * @return
     */
    void checkHitLimit(List<AccessLimitOperation> limitList);

    /**
     * 校验并记录访问
     * @param limitList
     */
    void checkHitLimitAndRecord(List<AccessLimitOperation> limitList);

    /**
     * 记录访问
     * @param limitList
     */
    void recordAccess(List<AccessLimitOperation> limitList);

}
