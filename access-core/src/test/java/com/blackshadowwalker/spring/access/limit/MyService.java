package com.blackshadowwalker.spring.access.limit;

import com.blackshadowwalker.spring.access.annotation.AccessLimit;
import com.blackshadowwalker.spring.access.annotation.AccessLimits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Author : LI-JIAN
 * Date   : 2017-09-15
 */
@Component
public class MyService {
    private static Logger log = LoggerFactory.getLogger(MyService.class);

    @AccessLimits({
            @AccessLimit(value = "CONCAT:A_10M", key = "#p0", limit = 2, unitTime = 600, errorMsg = "请求频繁，请10分钟后重试"),
            @AccessLimit(value = "CONCAT:A_1H", key = "#p0", limit = 5, unitTime = 3600, errorMsg = "请求频繁，请1小时后重试"),
            @AccessLimit(value = "CONCAT:AB_24H", key = "#p0 + '_' + #p1", limit = 10, unitTime = 3600 * 24, errorMsg = "请求频繁，请稍后重试[403]"),
            @AccessLimit(value = "CONCAT:AB_24H", key = "#p0 + '_' + #p1", limit = 10, unitTime = 3600 * 24, errorMsg = "请求频繁，请稍后重试[403]",
                    delayExpireOnHit = true, delayExpireStep = 60
            ),
    })
    public String myConcat(String a, Integer b) {
        log.info("call myConcat, a:{} b:{}", a, b);
        return a + "_" + b;
    }

}
