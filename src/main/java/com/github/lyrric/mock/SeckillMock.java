package com.github.lyrric.mock;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 秒杀接口模拟
 */
public class SeckillMock {
    // 请求次数，累计多少次之后请求成功
    private static AtomicInteger requestTimes = new AtomicInteger(0);
//    private static ThreadLocal<AtomicInteger> threadLocal = ThreadLocal.withInitial(new AtomicInteger(0));
    public synchronized String seckillApi(int requestTimes) {
        this.requestTimes.getAndAdd(1);
        System.out.println(this.requestTimes.get());
        if (this.requestTimes.get() < requestTimes) {
            throw new RuntimeException("排队人数较多请等待.........");
        }
        return UUID.randomUUID().toString();
    }
}
