package com.github.lyrric.service;

import com.github.lyrric.model.Area;
import com.github.lyrric.model.Vaccine;
import com.github.lyrric.util.AreaUtil;
import com.github.lyrric.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BusinessService {
    private static final Logger log = LoggerFactory.getLogger(BusinessService.class);

    private static HttpService httpService;
    private static BusinessService businessService = new BusinessService();

    private BusinessService() {

    }

    public static BusinessService getInstance() {
        httpService = new HttpService();
        return businessService;
    }

    /**
     * 检测有疫苗的医院
     *
     * @return
     */
    public List<Vaccine> checkVaccine() {
        List<Vaccine> list = new ArrayList<>();
        List<Area> areas = AreaUtil.getAreas();
        for (Area province : areas) {
            for (Area city : province.getChildren()) {
                List<Vaccine> vaccineList = null;
                try {
                    vaccineList = httpService.getVaccineList(city.getValue());
                    // 添加地址信息
                    if (vaccineList != null && vaccineList.size() > 0) {
                        vaccineList.forEach(vaccine -> {
                            vaccine.setProvince(province.getName());
                            vaccine.setCity(city.getName());
                        });
                    }
                    Thread.sleep(200);
                } catch (IOException e) {
                    log.error("{}", e.getMessage(), e);
                    log.info("{}-{} 获取疫苗列表失败，regionCode: {}", province.getName(), city.getName(), city.getValue());
                } catch (InterruptedException e) {
                    log.error("{}", e.getMessage(), e);
                    log.info("{}-{} 获取疫苗列表失败，regionCode: {}", province.getName(), city.getName(), city.getValue());
                }
                list.addAll(vaccineList);
            }
        }
        // 过滤九价疫苗
        list = list.stream().filter(vaccine -> "8803".equals(vaccine.getVaccineCode())).collect(Collectors.toList());
        // 过滤已过期的
        list = list.stream().filter(vaccine ->  DateUtil.parseDate(vaccine.getStartTime(),DateUtil.yyyy_MM_dd_HH_mm_ss).getTime() > System.currentTimeMillis()).collect(Collectors.toList());
        // 时间升序
        list.sort((Vaccine v1, Vaccine v2) -> DateUtil.parseDate(v1.getStartTime(),DateUtil.yyyy_MM_dd_HH_mm_ss).compareTo(DateUtil.parseDate(v2.getStartTime(),DateUtil.yyyy_MM_dd_HH_mm_ss)));
        list.forEach(vaccine -> log.info("{}-{} {} {} {} {}", vaccine.getProvince(), vaccine.getCity(), vaccine.getName(), vaccine.getAddress(), vaccine.getVaccineName(), vaccine.getStartTime()));
        return list;
    }
}
