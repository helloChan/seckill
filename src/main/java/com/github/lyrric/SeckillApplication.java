package com.github.lyrric;

import com.github.lyrric.ui.ConsoleMode;
import com.github.lyrric.ui.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created on 2020-07-21.
 *
 * @author wangxiaodong
 */
public class SeckillApplication {

    private static final Logger log = LoggerFactory.getLogger(SeckillApplication.class);

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        log.info("=================程序运行 {}=================", "start");
        if (args.length > 0 && "-c".equals(args[0].toLowerCase())) {
            new ConsoleMode().start();
        } else {
            new MainFrame();
        }
        log.info("=================程序运行 {}=================", "end");
    }

}
