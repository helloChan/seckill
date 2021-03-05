package com.github.lyrric.service;

import com.github.lyrric.conf.Config;
import com.github.lyrric.model.BusinessException;
import com.github.lyrric.ui.MainFrame;
import com.github.lyrric.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 2020-07-22.
 *
 * @author wangxiaodong
 */
public class SeckillService {
    private final Logger logger = LoggerFactory.getLogger(SeckillService.class);

    private YuemiaoService yuemiaoService = YuemiaoService.getInstance();
    public SeckillService() {}

    private ExecutorService service = Executors.newFixedThreadPool(200);
    /**
     * 多线程秒杀开启
     */
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void startSeckill(Integer vaccineId, String startDateStr, MainFrame mainFrame) throws ParseException, InterruptedException {
        long startDate = DateUtil.parseDate(startDateStr, DateUtil.yyyy_MM_dd_HH_mm_ss).getTime();
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> orderId = new AtomicReference<>(null);
        Runnable task = ()-> {
            do {
                try {
                    //1.直接秒杀、获取秒杀资格
                    long id = Thread.currentThread().getId();
                    logger.info("Thread ID：{}，发送请求", id);
                    orderId.set(yuemiaoService.seckill(vaccineId.toString(), "1", Config.memberId.toString(), Config.idCard));
                    success.set(true);
                    logger.info("Thread ID：{}，抢购成功", id);
                } catch (BusinessException e) {
                    logger.info("Thread ID: {}, 抢购失败: {}",Thread.currentThread().getId(), e.getErrMsg());
                    //如果离开始时间120秒后，或者已经成功抢到则不再继续
                    if(System.currentTimeMillis() > startDate+1000*60*2 || success.get()){
                        return;
                    }
//                    if("操作过于频繁,请稍后再试!".equals(e.getErrMsg()) && new Random().nextBoolean()){
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException ex) {
//                            ex.printStackTrace();
//                        }
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn("Thread ID: {}，未知异常", Thread.currentThread().getId());
                }
            } while (orderId.get() == null);
        };
        long now = System.currentTimeMillis();
        if(now + 2000 < startDate){
            logger.info("还未到开始时间，等待中......");
            Thread.sleep(startDate - now - 2000);
        }
        //如何保证能在秒杀时间点瞬间并发？
        //提前2000毫秒开始秒杀
        logger.info("###########提前2秒 开始秒杀###########");
        for (int i = 0; i < 20; i++) {
            service.submit(task);
        }
        //提前1000毫秒开始秒杀
        do {
            now = System.currentTimeMillis();
        }while (now + 1000 < startDate);
        logger.info("###########第一波 开始秒杀###########");
        for (int i = 0; i < 20; i++) {
            service.submit(task);
        }
        //提前500毫秒开始秒杀
        do {
            now = System.currentTimeMillis();
        }while (now + 500 < startDate);
        logger.info("###########第二波 开始秒杀###########");
        for (int i = 0; i < 20; i++) {
            service.submit(task);
        }
        //提前200毫秒开始秒杀
        do {
            now = System.currentTimeMillis();
        }while (now + 200 < startDate);
        logger.info("###########第三波 开始秒杀###########");
        for (int i = 0; i < 40; i++) {
            service.submit(task);
        }
        //准点（提前20毫秒）秒杀
        do {
            now = System.currentTimeMillis();
        }while (now + 20 < startDate);
        logger.info("###########第四波 开始秒杀###########");
        for (int i = 0; i < 40; i++) {
            service.submit(task);
        }

        service.shutdown();
        if(mainFrame != null){
            mainFrame.setStartBtnEnable();
        }
        //等待线程结束
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            if(success.get()){
                if(mainFrame != null){
                    mainFrame.appendMsg("抢购成功，请登录约苗小程序查看");
                }
                logger.info("抢购成功，请登录约苗小程序查看");
            }else{
                if(mainFrame != null){
                    mainFrame.appendMsg("抢购失败");
                }
            }
        } catch (InterruptedException e) {
            if(mainFrame != null){
                mainFrame.appendMsg("未知异常");
            }
            logger.info("抢购失败:",e.getCause());
        }

    }
}
