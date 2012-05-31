package com.alibaba.qa.diffcoverage.model;

import java.util.Properties;

import lombok.Data;

/**
 * 配置属型
 * @author garcia.wul
 *
 */
@Data
public class ConfigProperty {
    private Properties loggerProperties = new Properties();
    {
        loggerProperties.setProperty("log4j.rootLogger", "INFO,CONSOLE");
        loggerProperties.setProperty("log4j.addivity.org.apache", "true");
        loggerProperties.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        loggerProperties.setProperty("log4j.appender.Threshold", "INFO");
        loggerProperties.setProperty("log4j.appender.CONSOLE.Target", "System.out");
        loggerProperties.setProperty("log4j.appender.CONSOLE.Encoding", "UTF-8");
        loggerProperties.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        loggerProperties.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "[%p][%d] - %m %n");
    }

    public Properties getDebugLoggerProperties() {
        loggerProperties.setProperty("log4j.rootLogger", "DEBUG,CONSOLE");
        loggerProperties.setProperty("log4j.appender.Threshold", "DEBUG");
//        loggerProperties.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "[%p][%d] - <%l> - %m %n");
        return loggerProperties;
    }
    
    private IgnorePattern ignorePattern = new IgnorePattern();
    
    private boolean isAllFile = false;
}
