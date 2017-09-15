package com.blackshadowwalker.spring.access.limit;

import com.blackshadowwalker.spring.access.exp.AccessLimitException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Author : LI-JIAN
 * Date   : 2017-09-15
 */
@ContextConfiguration(locations = {"classpath:spring.xml"})
public class AccessLimitTest extends AbstractJUnit4SpringContextTests {
    private static Logger log = LoggerFactory.getLogger(MyService.class);

    @Autowired
    MyService myService;

    @Value("${access.limit.prefix}")
    private String accessLimitKeyPrefix;

    @Autowired
    private RedisTemplate<String, Object> redis;

    @Before
    public void clearRedis() {
        redis.delete(accessLimitKeyPrefix + "CONCAT:A_10M:a");
        redis.delete(accessLimitKeyPrefix + "CONCAT:A_10M:b");
        redis.delete(accessLimitKeyPrefix + "CONCAT:A_1H:a");
        redis.delete(accessLimitKeyPrefix + "CONCAT:A_1H:b");
        redis.delete(accessLimitKeyPrefix + "CONCAT:AB_24H:a_1");
        redis.delete(accessLimitKeyPrefix + "CONCAT:AB_24H:b_1");
    }

    @Test
    public void testNotHitLimit() {
        int limit = 2;
        for (int i = 0; i < limit; i++) {
            myService.myConcat("a", 1);
        }
        myService.myConcat("b", 1);
    }

    @Test(expected = AccessLimitException.class)
    public void testHitLimitA10M() {
        int limit = 3;
        for (int i = 0; i < limit; i++) {
            myService.myConcat("a", 1);
        }
    }

    @Test(expected = AccessLimitException.class)
    public void testHitLimitAB24H() {
        int limit = 11;
        for (int i = 0; i < limit; i++) {
            redis.delete(accessLimitKeyPrefix + "CONCAT:A_10M:b");
            redis.delete(accessLimitKeyPrefix + "CONCAT:A_1H:b");
            myService.myConcat("b", 1);
        }
    }

}
