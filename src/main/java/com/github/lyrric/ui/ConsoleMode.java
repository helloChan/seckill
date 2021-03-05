package com.github.lyrric.ui;

import com.github.lyrric.conf.Config;
import com.github.lyrric.model.Member;
import com.github.lyrric.model.Vaccine;
import com.github.lyrric.service.YuemiaoService;
import com.github.lyrric.service.SecKillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2020-08-14.
 * 控制台模式
 * @author wangxiaodong
 */
public class ConsoleMode {

    private final Logger log = LoggerFactory.getLogger(ConsoleMode.class);

    private ExecutorService service = Executors.newFixedThreadPool(100);

    private YuemiaoService yuemiaoService = new YuemiaoService();

    private SecKillService secKillService = new SecKillService();

    public void start() throws IOException, ParseException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        log.info("请输入tk：");
        Config.tk = sc.nextLine().trim();
        log.info("请输入Cookie：");
        Config.cookies = sc.nextLine().trim();
        log.info("获取接种人员......");
        List<Member> members = yuemiaoService.getMembers();
        for (int i = 0; i < members.size(); i++) {
            log.info("{}-{}-{}", i, members.get(i).getName(), members.get(i).getIdCardNo());
        }
        log.info("请输入接种人员序号：");
        int no = Integer.parseInt(sc.nextLine());
        Config.memberId = members.get(no).getId();
        Config.idCard = members.get(no).getIdCardNo();

        log.info("获取疫苗列表......");
        List<Vaccine> vaccine = yuemiaoService.getVaccineList();
        for (int i = 0; i < vaccine.size(); i++) {
            Vaccine item = vaccine.get(i);
            log.info("{}-{}-{}-{}-{}", i, item.getName(), item.getVaccineName(), item.getAddress(), item.getStartTime());
        }
        log.info("请输入疫苗序号：");
        no = Integer.parseInt(sc.nextLine());
        int code = vaccine.get(no).getId();
        String startTime = vaccine.get(no).getStartTime();
        log.info("按回车键开始秒杀：");
        sc.nextLine();
        secKillService.startSecKill(code, startTime, null);
    }

}
