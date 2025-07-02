package com.example.playwright.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 汎用的なテスト設定管理クラス
 */
public class TestConfig {
    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);
    private static TestConfig instance;
    private Properties properties;
    
    private TestConfig() {
        loadProperties();
    }
    
    public static TestConfig getInstance() {
        if (instance == null) {
            instance = new TestConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        
        // デフォルト設定を読み込み
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            logger.error("Failed to load test.properties: {}", e.getMessage(), e);
        }
        
        // システムプロパティで上書き
        properties.putAll(System.getProperties());
    }
    
    public String getBrowser() {
        return properties.getProperty("playwright.browser", "chrome");
    }
    
    public boolean isHeadless() {
        return Boolean.parseBoolean(properties.getProperty("playwright.headless", "false"));
    }
    
    public boolean useSystemBrowser() {
        return Boolean.parseBoolean(properties.getProperty("playwright.useSystemBrowser", "true"));
    }
    
    public int getSlowMo() {
        return Integer.parseInt(properties.getProperty("playwright.slowMo", "0"));
    }
    
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("playwright.timeout", "30000"));
    }
    
    public String getBaseUrl() {
        return properties.getProperty("test.baseUrl", "http://localhost:3000");
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
