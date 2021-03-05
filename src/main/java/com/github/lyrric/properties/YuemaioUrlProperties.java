package com.github.lyrric.properties;

import java.util.Properties;

public class YuemaioUrlProperties {
    private static YuemaioUrlProperties yueMaioUrlProperties = new YuemaioUrlProperties();
    private YuemaioUrlProperties() {}
    public static YuemaioUrlProperties getInstance() {
        return yueMaioUrlProperties;
    }

    private String seckillList;
    private String subscribe;
    private String checkstock2;
    private String memberList;
    private String host;

    public String getSeckillList() {
        return seckillList;
    }

    public void setSeckillList(String seckillList) {
        this.seckillList = seckillList;
    }

    public String getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(String subscribe) {
        this.subscribe = subscribe;
    }

    public String getCheckstock2() {
        return checkstock2;
    }

    public void setCheckstock2(String checkstock2) {
        this.checkstock2 = checkstock2;
    }

    public String getMemberList() {
        return memberList;
    }

    public void setMemberList(String memberList) {
        this.memberList = memberList;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public static void init(Properties prop) {
        String host = prop.getProperty("yuemiao.url.host");
        yueMaioUrlProperties.setHost(host);
        yueMaioUrlProperties.setCheckstock2(host + prop.getProperty("yuemiao.url.checkstock2"));
        yueMaioUrlProperties.setMemberList(host + prop.getProperty("yuemiao.url.member.list"));
        yueMaioUrlProperties.setSeckillList(host + prop.getProperty("yuemiao.url.seckill.list"));
        yueMaioUrlProperties.setSubscribe(host + prop.getProperty("yuemiao.url.seckill.subscribe"));
    }
}
