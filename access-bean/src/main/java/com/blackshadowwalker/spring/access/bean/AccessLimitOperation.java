package com.blackshadowwalker.spring.access.bean;

/**
 * Author : LI-JIAN
 * Date   : 2017-07-17
 * 访问限制
 */
public class AccessLimitOperation {

    String value;//key prefix
    String key;//key(SpEL)
    long limit;//限制次数([-1, max)),-1 : 无限制
    long unitTime;//计算频率限制单位时间(秒)
    String errorMsg;//抛出异常msg
    boolean delayExpireOnHit = false;//命中规则后是否继续延期过期时间
    int delayExpireStep = 0;//延期过期时间步长(秒)

    public AccessLimitOperation() {
    }

    public AccessLimitOperation(String value, String key, long limit, long unitTime, String errorMsg) {
        this.value = value;
        this.key = key;
        this.limit = limit;
        this.unitTime = unitTime;
        this.errorMsg = errorMsg;
    }

    public AccessLimitOperation(String value, String key, long limit, long unitTime, String errorMsg, boolean delayExpireOnHit, int delayExpireStep) {
        this.value = value;
        this.key = key;
        this.limit = limit;
        this.unitTime = unitTime;
        this.errorMsg = errorMsg;
        this.delayExpireOnHit = delayExpireOnHit;
        this.delayExpireStep = delayExpireStep;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getUnitTime() {
        return unitTime;
    }

    public void setUnitTime(long unitTime) {
        this.unitTime = unitTime;
    }

    public boolean getDelayExpireOnHit() {
        return delayExpireOnHit;
    }

    public int getDelayExpireStep() {
        return delayExpireStep;
    }
}
