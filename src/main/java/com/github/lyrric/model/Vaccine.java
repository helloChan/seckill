package com.github.lyrric.model;

/**
 * Created on 2020-07-23.
 * 疫苗列表
 * @author wangxiaodong
 */
public class Vaccine {

    private Integer id;
    /**
     * 医院名称
     */
    private String name;
    /**
     * 医院地址
     */
    private String address;
    /**
     * 疫苗代码
     * 九价-8803
     * 四价-8802
     */
    private String vaccineCode;
    /**
     * 疫苗名称
     */
    private String vaccineName;
    /**
     * 秒杀时间
     */
    private String startTime;

    private String province;

    private String city;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVaccineCode() {
        return vaccineCode;
    }

    public void setVaccineCode(String vaccineCode) {
        this.vaccineCode = vaccineCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
