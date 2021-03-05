package com.github.lyrric.service;

import com.alibaba.fastjson.JSONObject;
import com.github.lyrric.conf.Config;
import com.github.lyrric.model.*;
import com.github.lyrric.properties.YuemaioUrlProperties;
import com.github.lyrric.util.AreaUtil;
import com.github.lyrric.util.DateUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 2020-07-22.
 *
 * @author wangxiaodong
 */
public class YuemiaoService {
    private String baseUrl = "https://miaomiao.scmttec.com";
    private final Logger log = LoggerFactory.getLogger(YuemiaoService.class);

    private static YuemiaoService yuemiaoService = new YuemiaoService();
    private YuemiaoService() {}
    public static YuemiaoService getInstance() {
        return yuemiaoService;
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
                    vaccineList = this.getVaccineList(city.getValue());
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



    /***
     * 获取秒杀资格
     * @param seckillId 疫苗ID
     * @param vaccineIndex 固定1
     * @param linkmanId 接种人ID
     * @param idCard 接种人身份证号码
     * @return 返回订单ID
     * @throws IOException
     * @throws BusinessException
     */
    public String secKill(String seckillId, String vaccineIndex, String linkmanId, String idCard) throws IOException, BusinessException {
        Map<String, String> params = new HashMap<>();
        params.put("seckillId", seckillId);
        params.put("vaccineIndex", vaccineIndex);
        params.put("linkmanId", linkmanId);
        params.put("idCardNo", idCard);
        //后面替换成接口返回的st
        //目前发现接口返回的st就是当前时间，后面可能会固定为一个加密参数
        long st = System.currentTimeMillis();
        Header header = new BasicHeader("ecc-hs", eccHs(seckillId, st));
        return get(YuemaioUrlProperties.getInstance().getSubscribe(), params, header);
    }

    /**
     * 获取疫苗列表
     *
     * @return
     * @throws BusinessException
     */
    public List<Vaccine> getVaccineList() throws BusinessException, IOException {
        return this.getVaccineList(Config.regionCode);
    }

    /**
     * 获取疫苗列表
     *
     * @return
     * @throws BusinessException
     */
    public List<Vaccine> getVaccineList(String regionCode) throws BusinessException, IOException {
        hasAvailableConfig();
        Map<String, String> param = new HashMap<>();
        //九价疫苗的code
        param.put("offset", "0");
        param.put("limit", "100");
        //这个应该是成都的行政区划前四位
        param.put("regionCode", regionCode);
        String json = get(YuemaioUrlProperties.getInstance().getSeckillList(), param, null);
        return JSONObject.parseArray(json).toJavaList(Vaccine.class);
    }

    /**
     * 获取接种人信息
     *
     * @return
     */
    public List<Member> getMembers() throws IOException, BusinessException {
        String json = get(YuemaioUrlProperties.getInstance().getMemberList(), null, null);
        return JSONObject.parseArray(json, Member.class);
    }

    /***
     * 获取加密参数st
     * @param vaccineId 疫苗ID
     */
    public String getSt(String vaccineId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("id", vaccineId);
        String json = get(YuemaioUrlProperties.getInstance().getCheckstock2(), params, null);
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getJSONObject("data").getString("st");
    }

    /***
     * 获取接种日期
     * @param vaccineId 疫苗ID
     * @param orderId 订单ID
     */
    public List<SubDate> getSkSubDays(String vaccineId, String orderId) throws IOException, BusinessException {
        String path = baseUrl + "/seckill/seckill/subscribeDays.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", vaccineId);
        params.put("sid", orderId);
        String json = get(path, params, null);
        log.info("日期格式:{}", json);
        return JSONObject.parseArray(json).toJavaList(SubDate.class);
    }

    /**
     * 根据接种日期，获取接种时间段
     *
     * @param vaccineId
     * @param orderId
     * @param day       接种日期 YYYY-MM-DD
     * @return
     * @throws IOException
     * @throws BusinessException
     */
    public List<SubDateTime> getSkSubDayTime(String vaccineId, String orderId, String day) throws IOException, BusinessException {
        String path = baseUrl + "/seckill/seckill/dayTimes.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", vaccineId);
        params.put("sid", orderId);
        params.put("day", day);
        String json = get(path, params, null);
        log.info("根据选择的日期，获取的时间格式" + json);
        return JSONObject.parseArray(json).toJavaList(SubDateTime.class);
    }

    /**
     * 提交接种时间
     *
     * @param vaccineId
     * @param orderId
     * @param day       接种日期 YYYY-MM-DD
     * @return
     * @throws IOException
     * @throws BusinessException
     */
    public void subDayTime(String vaccineId, String orderId, String day, String wid) throws IOException, BusinessException {
        String path = baseUrl + "/seckill/seckill/submitDateTime.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", vaccineId);
        params.put("sid", orderId);
        params.put("day", day);
        params.put("wid", wid);
        String json = get(path, params, null);
        log.info("提交接种时间，返回数据: {}", json);
    }

    private void hasAvailableConfig() throws BusinessException {
        if (StringUtils.isEmpty(Config.cookies)) {
            throw new BusinessException("0", "请先配置cookie");
        }
    }

    private String get(String path, Map<String, String> params, Header extHeader) throws IOException, BusinessException {
        if (params != null && params.size() != 0) {
            StringBuilder paramStr = new StringBuilder("?");
            params.forEach((key, value) -> {
                paramStr.append(key).append("=").append(value).append("&");
            });
            String t = paramStr.toString();
            if (t.endsWith("&")) {
                t = t.substring(0, t.length() - 1);
            }
            path += t;
        }
        HttpGet get = new HttpGet(path);
        List<Header> headers = getCommonHeader();
        if (extHeader != null) {
            headers.add(extHeader);
        }
        get.setHeaders(headers.toArray(new Header[0]));
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpEntity httpEntity = httpClient.execute(get).getEntity();
        String json = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        JSONObject jsonObject = JSONObject.parseObject(json);
        if ("0000".equals(jsonObject.get("code"))) {
            return jsonObject.getString("data");
        } else {
            throw new BusinessException(jsonObject.getString("code"), jsonObject.getString("msg"));
        }
    }

    private List<Header> getCommonHeader() {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; SM-N960F Build/JLS36C; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36 MMWEBID/1042 MicroMessenger/7.0.15.1680(0x27000F34) Process/appbrand0 WeChat/arm32 NetType/WIFI Language/zh_CN ABI/arm32"));
        headers.add(new BasicHeader("Referer", "https://servicewechat.com/wxff8cad2e9bf18719/2/page-frame.html"));
        headers.add(new BasicHeader("tk", Config.tk));
        headers.add(new BasicHeader("Accept", "application/json, text/plain, */*"));
        headers.add(new BasicHeader("Host", "miaomiao.scmttec.com"));
        headers.add(new BasicHeader("Cookie", Config.cookies));
        return headers;
    }

    private String eccHs(String seckillId, Long st) {
        String salt = "ux$ad70*b";
        final Integer memberId = Config.memberId;
        String md5 = DigestUtils.md5Hex(seckillId + st + memberId);
        return DigestUtils.md5Hex(md5 + salt);
    }
}
