package com.github.lyrric;

import com.github.lyrric.properties.YuemaioUrlProperties;
import com.github.lyrric.ui.ConsoleMode;
import com.github.lyrric.ui.MainFrame;
import com.github.lyrric.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

/**
 * Created on 2020-07-21.
 *
 * @author wangxiaodong
 */
public class SeckillApplication {

    private static final Logger log = LoggerFactory.getLogger(SeckillApplication.class);
    public static Properties boostrap ;

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        initProperties();
        if (args.length > 0 && "-c".equals(args[0].toLowerCase())) {
            new ConsoleMode().start();
        } else {
            new MainFrame();
        }
    }

    /**
     * 初始化properties
     */
    public static void initProperties() {
        boostrap = PropertiesUtil.getProperties("bootstrap.properties");
        YuemaioUrlProperties.init(boostrap);
    }
}
