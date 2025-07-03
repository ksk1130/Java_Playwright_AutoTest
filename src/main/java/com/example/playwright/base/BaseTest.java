package com.example.playwright.base;

import com.example.playwright.config.TestConfig;
import com.microsoft.playwright.*;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 全てのテストクラスの基底クラス
 * Playwrightの初期化と終了処理を統一的に管理
 */
public abstract class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected TestConfig config;

    @Before
    public void setUp(String userDirPath) {
        config = TestConfig.getInstance();
        logger.info("=== テストセットアップ開始 ===");

        // Playwrightインスタンスを作成
        playwright = Playwright.create();

        // ブラウザを起動（システムにインストール済みのブラウザを使用）
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless())
                .setSlowMo(config.getSlowMo());

        BrowserType browserType = getBrowserType();
        browserType.launchPersistentContext(Paths.get(userDirPath));

        // システムブラウザを使用する場合はチャンネルを設定
        if (config.useSystemBrowser()) {
            String channel = getSystemBrowserChannel(config.getBrowser());
            if (channel != null) {
                launchOptions.setChannel(channel);
            }
        }

        browser = browserType.launch(launchOptions);

        // ブラウザコンテキストを作成
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080);

        context = browser.newContext(contextOptions);

        // デフォルトタイムアウトを設定
        context.setDefaultTimeout(config.getTimeout());

        // ページを作成
        page = context.newPage();

        // 追加のセットアップがあれば実行
        additionalSetUp();

        logger.info("=== テストセットアップ完了 ===");
    }

    @After
    public void tearDown() {
        logger.info("=== テストクリーンアップ開始 ===");

        // 追加のクリーンアップがあれば実行
        additionalTearDown();

        // リソースを解放
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }

        logger.info("=== テストクリーンアップ完了 ===");
    }

    /**
     * ブラウザタイプを取得
     */
    private BrowserType getBrowserType() {
        String browserName = config.getBrowser().toLowerCase();
        return switch (browserName) {
            case "firefox" -> playwright.firefox();
            case "webkit", "safari" -> playwright.webkit();
            default -> playwright.chromium();
        };
    }

    /**
     * システムにインストール済みのブラウザチャンネルを取得
     */
    private String getSystemBrowserChannel(String browserName) {
        String lowerBrowserName = browserName.toLowerCase();
        return switch (lowerBrowserName) {
            case "chrome", "chromium" -> "chrome"; // Google Chrome
            case "firefox" -> null; // システムFirefox（チャンネル指定なし）
            case "edge" -> "msedge"; // Microsoft Edge
            case "webkit", "safari" -> null; // WebKit（チャンネル指定なし）
            default -> "chrome"; // デフォルトはChrome
        };
    }

    /**
     * 子クラスで追加のセットアップが必要な場合にオーバーライド
     */
    protected void additionalSetUp() {
        // デフォルト実装は空
    }

    /**
     * 子クラスで追加のクリーンアップが必要な場合にオーバーライド
     */
    protected void additionalTearDown() {
        // デフォルト実装は空
    }

    /**
     * 指定されたURLにナビゲート
     */
    protected void navigateTo(String url) {
        if (url.startsWith("http")) {
            page.navigate(url);
        } else {
            page.navigate(config.getBaseUrl() + url);
        }
    }

    /**
     * スクリーンショットを撮影
     */
    protected byte[] takeScreenshot() {
        return page.screenshot();
    }

    /**
     * フルページスクリーンショットを撮影
     */
    protected byte[] takeFullPageScreenshot() {
        return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
    }

    /**
     * スクリーンショットをファイルに保存
     */
    protected void saveScreenshot(String fileName) {
        try {
            Path screenshotPath = Paths.get("screenshots");
            if (!Files.exists(screenshotPath)) {
                Files.createDirectories(screenshotPath);
            }
            Path filePath = screenshotPath.resolve(fileName + ".png");
            byte[] screenshot = takeScreenshot();
            Files.write(filePath, screenshot);
            logger.info("Screenshot saved: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save screenshot: {}", e.getMessage(), e);
        }
    }
}
