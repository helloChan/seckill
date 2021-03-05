package com.github.lyrric.ui;

import com.github.lyrric.conf.Config;
import com.github.lyrric.model.Area;
import com.github.lyrric.model.BusinessException;
import com.github.lyrric.model.TableModel;
import com.github.lyrric.model.Vaccine;
import com.github.lyrric.service.SeckillService;
import com.github.lyrric.service.YuemiaoService;
import com.github.lyrric.util.AreaUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.util.List;

/**
 * Created on 2020-07-21.
 *
 * @author wangxiaodong
 */
public class MainFrame extends JFrame {

    SeckillService service = new SeckillService();
    YuemiaoService yuemiaoService = YuemiaoService.getInstance();
    /**
     * 疫苗列表
     */
    private List<Vaccine> vaccines;

    JButton startButton;
    JButton setCookieButton;
    JButton setMemberButton;
    JButton allCityVaccineListButton;
    JTable vaccineTable;
    JButton comboBoxSureButton;
    JButton defaultCityVaccineListButton;
    DefaultTableModel tableModel;
    JTextArea log;
    JComboBox<Area> provinceBox;
    JComboBox<Area> cityBox;


    public MainFrame() {
        setLayout(null);
        setTitle("Just For Fun");
        setBounds(500, 500, 680, 340);
        initView();
        setLocationRelativeTo(null);
        setVisible(true);
        this.setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public JScrollPane initVaccineTable() {
        String[] columnNames = {"id", "省份", "城市", "疫苗名称", "医院名称", "秒杀时间"};
        tableModel = new TableModel(new String[0][], columnNames);
        vaccineTable = new JTable(tableModel);
        return new JScrollPane(vaccineTable);
    }

    public void initCookieButton() {
        setCookieButton = new JButton("设置Cookie");
        setCookieButton.addActionListener((e) -> {
            ConfigDialog dialog = new ConfigDialog(this);
            dialog.setModal(true);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);
            if (dialog.success()) {
                setMemberButton.setEnabled(true);
                startButton.setEnabled(true);
                defaultCityVaccineListButton.setEnabled(true);
                appendMsg("设置cookie成功");
            }
        });
    }

    public void initAllCityVaccineButton() {
        allCityVaccineListButton = new JButton("可抢列表");
        allCityVaccineListButton.setEnabled(true);
        allCityVaccineListButton.addActionListener((e) -> {
            List<Vaccine> allVaccine = yuemiaoService.getAllVaccine();
            this.vaccineRender(allVaccine);
        });
    }

    public void initMemberButton() {
        setMemberButton = new JButton("选择成员");
        setMemberButton.setEnabled(false);
        setMemberButton.addActionListener((e) -> {
            MemberDialog dialog = new MemberDialog(this);
            dialog.setModal(true);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);
            if (dialog.success()) {
                appendMsg("已设置成员：" + Config.memberName);
            }
        });
    }

    public void initDefaultCityVaccineButton() {
        defaultCityVaccineListButton = new JButton("刷新疫苗列表");
        defaultCityVaccineListButton.setEnabled(false);
        defaultCityVaccineListButton.addActionListener((e) -> {
            try {
                vaccines = yuemiaoService.getVaccineList();
                this.vaccineRender(vaccines);
            } catch (BusinessException e1) {
                appendMsg("错误：" + e1.getErrMsg() + "，errCode" + e1.getCode());
            }
        });
    }

    public void initProvinceAndCityComboBox() {
        List<Area> areas = AreaUtil.getAreas();
        provinceBox = new JComboBox<>(areas.toArray(new Area[0]));
        //itemListener
        ItemListener itemListener = item -> {
            if (ItemEvent.SELECTED == item.getStateChange()) {
                Area selectedItem = (Area) item.getItem();
                cityBox.removeAllItems();
                List<Area> children = AreaUtil.getChildren(selectedItem.getName());
                for (Area child : children) {
                    cityBox.addItem(child);
                }
            }
        };
        provinceBox.addItemListener(itemListener);
        cityBox = new JComboBox<>(AreaUtil.getChildren("直辖市").toArray(new Area[0]));
    }

    public void initComboBoxSureButton() {
        comboBoxSureButton = new JButton("确定");
        comboBoxSureButton.addActionListener(e -> {
            Area city = (Area) cityBox.getSelectedItem();
            Area province = (Area) provinceBox.getSelectedItem();
            Config.regionCode = city.getValue();
            Config.province = province.getName();
            Config.city = city.getName();
            appendMsg("已选择:" + province.getName() + "-" + city.getName() + "-" + city.getValue());
        });
    }

    public void initStartSeckillButton() {
        startButton = new JButton("开始");
        startButton.setEnabled(false);
        startButton.addActionListener(e -> start());
    }

    public JScrollPane initLogTextArea() {
        log = new JTextArea();
        log.append("日记记录：\r\n");
        log.setEditable(false);
        log.setAutoscrolls(true);
        log.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(log);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return scroll;
    }

    private void initView() {
        // 初始化组件
        initStartSeckillButton();
        initCookieButton();
        initMemberButton();
        initDefaultCityVaccineButton();
        JScrollPane textAreaPane = initLogTextArea();
        JScrollPane vaccineTablePane = initVaccineTable();
        initProvinceAndCityComboBox();
        initComboBoxSureButton();
        initAllCityVaccineButton();
        // 布局
        provinceBox.setBounds(20, 275, 100, 20);
        cityBox.setBounds(130, 275, 80, 20);
        comboBoxSureButton.setBounds(220, 270, 80, 30);
        vaccineTablePane.setBounds(10, 10, 460, 200);
        startButton.setBounds(370, 230, 100, 30);
        setCookieButton.setBounds(20, 230, 100, 30);
        setMemberButton.setBounds(130, 230, 100, 30);
        defaultCityVaccineListButton.setBounds(240, 230, 120, 30);
        textAreaPane.setBounds(480, 10, 180, 280);
        allCityVaccineListButton.setBounds(320, 270, 80, 30);
        // 添加组件
        add(vaccineTablePane);
        add(textAreaPane);
        add(startButton);
        add(setCookieButton);
        add(setMemberButton);
        add(defaultCityVaccineListButton);
        add(provinceBox);
        add(allCityVaccineListButton);
        add(cityBox);
        add(comboBoxSureButton);
    }


    /**
     * 渲染疫苗数据
     *
     * @param vaccines
     */
    private void vaccineRender(List<Vaccine> vaccines) {
        //清除表格数据
        //通知模型更新
        ((DefaultTableModel) vaccineTable.getModel()).getDataVector().clear();
        ((DefaultTableModel) vaccineTable.getModel()).fireTableDataChanged();
        vaccineTable.updateUI();//刷新表
        if (vaccines != null && !vaccines.isEmpty()) {
            for (Vaccine t : vaccines) {
                String[] item = {t.getId().toString(), t.getProvince(), t.getCity(), t.getVaccineName(), t.getName(), t.getStartTime()};
                tableModel.addRow(item);
            }
        }
    }

    private void start() {
        if (StringUtils.isEmpty(Config.cookies)) {
            appendMsg("请配置cookie!!!");
            return;
        }
        if (vaccineTable.getSelectedRow() < 0) {
            appendMsg("请选择要抢购的疫苗");
            return;
        }

        int selectedRow = vaccineTable.getSelectedRow();
        Integer id = vaccines.get(selectedRow).getId();
        String startTime = vaccines.get(selectedRow).getStartTime();
        new Thread(() -> {
            try {
                setCookieButton.setEnabled(false);
                startButton.setEnabled(false);
                setMemberButton.setEnabled(false);
                service.startSecKill(id, startTime, this);
            } catch (ParseException | InterruptedException e) {
                appendMsg("解析开始时间失败");
                e.printStackTrace();
            } finally {
                setCookieButton.setEnabled(true);
                startButton.setEnabled(true);
                setMemberButton.setEnabled(true);
            }
        }).start();

    }


    public void appendMsg(String message) {
        log.append(message);
        log.append("\r\n");
    }

    public void setStartBtnEnable() {
        startButton.setEnabled(true);
        setCookieButton.setEnabled(true);
        startButton.setEnabled(true);
    }
}
