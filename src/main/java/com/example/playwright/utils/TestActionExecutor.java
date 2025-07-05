package com.example.playwright.utils;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.AriaRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 汎用的なE2Eテストアクション実行エンジン
 * 
 * - CSV/Excel等のシナリオ記述から呼び出されるアクションを一元管理
 * - iframe/ウィンドウ/Shadow DOM/複合セレクタ/プルダウン等の多様な操作に対応
 * - ロギング・エラーハンドリング・拡張性を重視
 */
public class TestActionExecutor {
    // ロガー（全アクションの実行・エラーを記録）
    private static final Logger logger = LoggerFactory.getLogger(TestActionExecutor.class);
    // PlaywrightのPageインスタンス
    private Page page;
    // 現在のiframeセレクタ（nullならメインフレーム）
    private String currentIframeSelector;
    // ウィンドウハンドルのリスト
    private java.util.List<Page> windowHandles;
    // 現在のウィンドウインデックス
    private int currentWindowIndex;

    /**
     * コンストラクタ
     * 
     * @param page PlaywrightのPageインスタンス
     */
    public TestActionExecutor(Page page) {
        this.page = page;
        this.currentIframeSelector = null; // デフォルトはメインフレーム
        this.windowHandles = new java.util.ArrayList<>();
        this.windowHandles.add(page); // 最初のページを追加
        this.currentWindowIndex = 0;
    }

    /**
     * アクションを実行（シナリオCSVの1行に対応）
     * 
     * @param action     アクション名（例: クリック, 入力, 遷移, ...）
     * @param element    対象要素（セレクタやラベル等）
     * @param inputValue 入力値（必要な場合のみ）
     */
    public void executeAction(String action, String element, String inputValue) {
        try {
            switch (action.toLowerCase().trim()) {
                case "navigate", "goto", "アクセス", "移動" -> navigate(element); // URL遷移
                case "click", "クリック", "押下" -> click(element); // 要素クリック
                case "input", "type", "入力", "タイプ" -> input(element, inputValue); // テキスト入力
                case "wait", "待機", "wait for" -> waitForElement(element); // 要素の表示待機
                case "verify", "確認", "検証", "assertion" -> verify(element, inputValue); // 値検証
                case "select", "選択", "dropdown" -> selectOption(element, inputValue); // プルダウン選択
                case "check", "チェック", "checkbox" -> checkElement(element); // チェックボックスON
                case "uncheck", "チェック解除", "uncheckbox" -> uncheckElement(element); // チェックボックスOFF
                case "hover", "ホバー", "マウスオーバー" -> hoverElement(element); // ホバー
                case "scroll", "スクロール" -> scrollTo(element); // スクロール
                case "refresh", "reload", "リロード", "更新" -> refresh(); // リロード
                case "back", "戻る", "前のページ" -> goBack(); // 戻る
                case "forward", "進む", "次のページ" -> goForward(); // 進む
                case "screenshot", "スクリーンショット", "画面キャプチャ" -> takeScreenshot(element); // スクショ
                case "iframe", "アイフレーム", "フレーム" -> switchToIframe(element); // iframe切替
                case "main", "メイン", "親フレーム" -> switchToMainFrame(); // メインフレーム復帰
                case "newwindow", "新しいウィンドウ", "ポップアップ" -> switchToNewWindow(element); // 新規ウィンドウ切替
                case "closewindow", "ウィンドウを閉じる", "ポップアップを閉じる" -> closeWindow(element); // ウィンドウ閉じる
                case "window", "ウィンドウ", "ウィンドウ切り替え" -> switchToWindow(element != null && !element.trim().isEmpty() ? element : inputValue); // 指定ウィンドウ切替
                default -> logger.warn("Unknown action: " + action);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute action: " + action + " on element: " + element, e);
        }
    }

    /**
     * セレクトボックスから値を選択
     * 
     * @param element セレクタまたはラベル
     * @param value   選択する値
     */
    private void selectOption(String element, String value) {
        if (element.startsWith("#") || element.startsWith(".") || element.contains("[")) {
            getLocator(element).selectOption(value);
        } else {
            // ラベルで検索
            if (currentIframeSelector != null) {
                // iframe内では直接locatorを使用
                getLocator("[aria-label='" + element + "'], label:has-text('" + element + "') + select")
                        .selectOption(value);
            } else {
                page.getByLabel(element).selectOption(value);
            }
        }
        logger.info("Selected '{}' from: {}", value, element);
    }

    /**
     * チェックボックスをON
     */
    private void checkElement(String element) {
        if (element.startsWith("#") || element.startsWith(".") || element.contains("[")) {
            page.locator(element).check();
        } else {
            page.getByLabel(element).check();
        }
        logger.info("Checked: {}", element);
    }

    /**
     * チェックボックスをOFF
     */
    private void uncheckElement(String element) {
        if (element.startsWith("#") || element.startsWith(".") || element.contains("[")) {
            page.locator(element).uncheck();
        } else {
            page.getByLabel(element).uncheck();
        }
        logger.info("Unchecked: {}", element);
    }

    /**
     * 要素にマウスオーバー
     */
    private void hoverElement(String element) {
        if (element.startsWith("#") || element.startsWith(".") || element.contains("[")) {
            page.locator(element).hover();
        } else {
            page.getByText(element).hover();
        }
        logger.info("Hovered: {}", element);
    }

    /**
     * 要素までスクロール
     */
    private void scrollTo(String element) {
        if (element.startsWith("#") || element.startsWith(".") || element.contains("[")) {
            page.locator(element).scrollIntoViewIfNeeded();
        } else {
            page.getByText(element).scrollIntoViewIfNeeded();
        }
        logger.info("Scrolled to: {}", element);
    }

    /**
     * ページをリロード
     */
    private void refresh() {
        page.reload();
        logger.info("Page refreshed");
    }

    /**
     * 前のページに戻る
     */
    private void goBack() {
        page.goBack();
        logger.info("Navigated back");
    }

    /**
     * 次のページに進む
     */
    private void goForward() {
        page.goForward();
        logger.info("Navigated forward");
    }

    /**
     * スクリーンショットを保存
     * 
     * @param fileName ファイル名（null可）
     */
    private void takeScreenshot(String fileName) {
        try {
            java.nio.file.Path screenshotPath = java.nio.file.Paths.get("screenshots");
            if (!java.nio.file.Files.exists(screenshotPath)) {
                java.nio.file.Files.createDirectories(screenshotPath);
            }
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFileName = (fileName != null && !fileName.isEmpty()) ? fileName + "_" + timestamp + ".png"
                    : "screenshot_" + timestamp + ".png";
            java.nio.file.Path filePath = screenshotPath.resolve(fullFileName);
            byte[] screenshot = page.screenshot();
            java.nio.file.Files.write(filePath, screenshot);
            logger.info("Screenshot saved: {}", filePath.toAbsolutePath());
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", fileName, e);
        }
    }

    /**
     * 指定URLへナビゲート
     */
    public void navigate(String url) {
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }

        // ページの読み込み完了を待機
        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // ページの読み込み完了後、少し待機
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 一般的な同意ボタンがある場合は自動的にクリック
        handleCommonConsentButtons();

        logger.info("Navigated to: " + url);
    }

    /**
     * 一般的な同意ボタンを処理
     */
    private void handleCommonConsentButtons() {
        try {
            // 一般的な同意ボタンのパターン
            String[] consentSelectors = {
                    "button:has-text('すべて同意')",
                    "button:has-text('Accept all')",
                    "button:has-text('Accept All')",
                    "button:has-text('同意')",
                    "button:has-text('Accept')",
                    "button:has-text('OK')",
                    "button[id*='accept']",
                    "button[id*='agree']",
                    "button[class*='accept']",
                    "button[class*='agree']"
            };

            for (String selector : consentSelectors) {
                if (page.locator(selector).count() > 0 && page.locator(selector).isVisible()) {
                    page.locator(selector).first().click();
                    logger.info("Clicked consent button: " + selector);
                    Thread.sleep(1000); // 同意後の処理を待機
                    break;
                }
            }
        } catch (Exception e) {
            // 同意ボタンの処理でエラーが発生しても継続
            logger.warn("Consent button handling failed, continuing...");
        }
    }

    /**
     * 要素をクリック
     */
    public void click(String element) {
        logger.info("Attempting to click element: '" + element + "'");

        try {
            // 1. CSS セレクタまたは属性セレクタの場合
            if (element.startsWith("#") || element.startsWith(".") || element.contains("[") || element.contains(":")) {
                logger.info("Identified as CSS selector");
                getLocator(element).click();
                logger.info("Successfully clicked using CSS selector: " + element);
                return;
            }

            // 2. 特定のパターンマッチング
            if (isSearchButton(element)) {
                logger.info("Identified as search button");
                clickSearchButton();
                return;
            } else if (isFirstSearchResult(element)) {
                logger.info("Identified as first search result");
                clickFirstSearchResult();
                return;
            }

            // 3. 汎用的な要素検索（複数手法を試行）
            if (clickByMultipleMethods(element)) {
                logger.info("Successfully clicked: " + element);
                return;
            }

            throw new RuntimeException("Could not find or click element: " + element);

        } catch (Exception e) {
            throw new RuntimeException("Failed to click element: " + element, e);
        }
    }

    /**
     * 検索ボタンかどうか判定
     */
    private boolean isSearchButton(String element) {
        String lower = element.toLowerCase();
        return (lower.contains("検索") && lower.contains("ボタン")) ||
                (lower.contains("search") && lower.contains("button")) ||
                lower.equals("検索ボタン") || lower.equals("search button");
    }

    /**
     * 検索結果の最初の項目かどうか判定
     */
    private boolean isFirstSearchResult(String element) {
        String lower = element.toLowerCase();
        return (lower.contains("結果") && lower.contains("1")) ||
                (lower.contains("result") && lower.contains("first")) ||
                lower.contains("検索結果1つ目") || lower.contains("first result");
    }

    /**
     * 複数の手法で要素をクリック
     */
    private boolean clickByMultipleMethods(String element) {
        // 試行する手法のリスト
        String[][] clickMethods = {
                { "text", element }, // テキストで検索
                { "placeholder", element }, // プレースホルダーで検索
                { "label", element }, // ラベルで検索
                { "title", element }, // title属性で検索
                { "alt", element }, // alt属性で検索
                { "value", element }, // value属性で検索
                { "name", element }, // name属性で検索
                { "id", element }, // id属性で検索（#なしの場合）
                { "class", element }, // class属性で検索（.なしの場合）
                { "partial-text", element }, // 部分テキストマッチ
                { "role-button", element } // ボタンロールで検索
        };

        for (String[] method : clickMethods) {
            try {
                if (tryClickMethod(method[0], method[1])) {
                    logger.info("Successfully clicked using method: " + method[0] + " with value: " + method[1]);
                    return true;
                }
            } catch (Exception e) {
                // 次の手法を試行
                continue;
            }
        }

        return false;
    }

    /**
     * 指定された手法で要素のクリックを試行
     */
    private boolean tryClickMethod(String method, String value) {
        try {
            switch (method) {
                case "text":
                    if (page.getByText(value).count() > 0) {
                        page.getByText(value).first().click();
                        return true;
                    }
                    break;
                case "placeholder":
                    if (page.getByPlaceholder(value).count() > 0) {
                        page.getByPlaceholder(value).click();
                        return true;
                    }
                    break;
                case "label":
                    if (page.getByLabel(value).count() > 0) {
                        page.getByLabel(value).click();
                        return true;
                    }
                    break;
                case "title":
                    if (page.locator("[title='" + value + "']").count() > 0) {
                        page.locator("[title='" + value + "']").click();
                        return true;
                    }
                    break;
                case "alt":
                    if (page.locator("[alt='" + value + "']").count() > 0) {
                        page.locator("[alt='" + value + "']").click();
                        return true;
                    }
                    break;
                case "value":
                    if (page.locator("[value='" + value + "']").count() > 0) {
                        page.locator("[value='" + value + "']").click();
                        return true;
                    }
                    break;
                case "name":
                    if (page.locator("[name='" + value + "']").count() > 0) {
                        page.locator("[name='" + value + "']").click();
                        return true;
                    }
                    break;
                case "id":
                    if (page.locator("#" + value).count() > 0) {
                        page.locator("#" + value).click();
                        return true;
                    }
                    break;
                case "class":
                    if (page.locator("." + value).count() > 0) {
                        page.locator("." + value).first().click();
                        return true;
                    }
                    break;
                case "partial-text":
                    if (page.locator(":has-text('" + value + "')").count() > 0) {
                        page.locator(":has-text('" + value + "')").first().click();
                        return true;
                    }
                    break;
                case "role-button":
                    try {
                        if (page.getByRole(AriaRole.BUTTON,
                                new Page.GetByRoleOptions().setName(value)).count() > 0) {
                            page.getByRole(AriaRole.BUTTON,
                                    new Page.GetByRoleOptions().setName(value)).click();
                            return true;
                        }
                    } catch (Exception e) {
                        // AriaRoleが使えない場合はスキップ
                    }
                    break;
            }
        } catch (Exception e) {
            // この手法では見つからない
        }

        return false;
    }

    /**
     * 検索ボタンをクリック
     */
    public void clickSearchButton() {
        // Googleの検索ボタンを探す（複数の方法で試行）
        String[] buttonSelectors = {
                "input[type='submit'][value*='検索']",
                "input[type='submit'][value*='Google']",
                "button:has-text('Google 検索')",
                "button:has-text('検索')",
                "input[name='btnK']",
                "input[value='Google Search']",
                "center input[type='submit']"
        };

        boolean clickSuccess = false;

        // まずボタンを探してクリックを試行
        for (String selector : buttonSelectors) {
            try {
                if (page.locator(selector).count() > 0 && page.locator(selector).isVisible()) {
                    page.locator(selector).click();
                    clickSuccess = true;
                    logger.info("Clicked search button using selector: " + selector);
                    break;
                }
            } catch (Exception e) {
                continue;
            }
        }

        // ボタンが見つからない場合はEnterキーを使用
        if (!clickSuccess) {
            try {
                String[] searchBoxSelectors = {
                        "input[name='q']",
                        "textarea[name='q']",
                        "input[title='検索']"
                };

                for (String selector : searchBoxSelectors) {
                    if (page.locator(selector).count() > 0) {
                        page.locator(selector).press("Enter");
                        logger.info("Pressed Enter on search box: " + selector);
                        clickSuccess = true;
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not click search button or press Enter", e);
            }
        }

        if (!clickSuccess) {
            throw new RuntimeException("Could not find or click search button");
        }
    }

    /**
     * 検索結果の1つ目をクリック
     */
    public void clickFirstSearchResult() {
        try {
            logger.info("Waiting for search results to appear...");

            // 検索結果が表示されるまで待機（複数の条件で試行）
            boolean resultsFound = false;
            String[] waitSelectors = {
                    "div#search",
                    "#rso",
                    ".g",
                    "div[data-ved]",
                    "#search .g",
                    "h3"
            };

            for (String selector : waitSelectors) {
                try {
                    page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(10000));
                    logger.info("Found search results with selector: " + selector);
                    resultsFound = true;
                    break;
                } catch (Exception e) {
                    logger.info("Selector not found: " + selector);
                    continue;
                }
            }

            if (!resultsFound) {
                logger.info("No search results found with standard selectors, checking page content...");
                String pageContent = page.textContent("body");
                if (pageContent.contains("playwright") || pageContent.contains("Playwright")) {
                    logger.info("Page contains 'playwright' content, assuming search was successful");
                } else {
                    throw new RuntimeException("Search results not found on page");
                }
            }

            // 少し待機してページを安定させる
            Thread.sleep(2000);

            // 複数のセレクタで検索結果を探す
            String[] resultSelectors = {
                    "#search .g:first-child a[href]:not([href*='googleadservices']):not([href*='doubleclick'])",
                    "#rso .g:first-child a[href]",
                    ".g:first-child h3 a",
                    ".rc:first-child h3 a",
                    "#search a[href]:not([href*='googleadservices']):not([href*='doubleclick'])",
                    "div[data-ved]:first-child a[href]:not([href*='googleadservices'])",
                    "h3 a[href]",
                    "a[href]:not([href*='googleadservices']):not([href*='doubleclick'])"
            };

            boolean clickSuccess = false;

            for (String selector : resultSelectors) {
                try {
                    int count = page.locator(selector).count();
                    logger.info("Selector '" + selector + "' found " + count + " elements");

                    if (count > 0) {
                        // 最初の要素が表示されているかチェック
                        if (page.locator(selector).first().isVisible()) {
                            logger.info("Clicking element with selector: " + selector);
                            page.locator(selector).first().click();
                            logger.info("Successfully clicked first search result");
                            clickSuccess = true;
                            break;
                        } else {
                            logger.info("Element not visible with selector: " + selector);
                        }
                    }
                } catch (Exception e) {
                    logger.info("Failed with selector '" + selector + "': " + e.getMessage());
                    continue;
                }
            }

            if (!clickSuccess) {
                // 最後の手段として h3 要素をクリック
                logger.info("Trying fallback: clicking h3 element");
                if (page.locator("h3").count() > 0) {
                    page.locator("h3").first().click();
                    logger.info("Clicked first h3 element as fallback");
                    clickSuccess = true;
                } else {
                    // デバッグ情報を出力
                    logger.info("Current URL: " + page.url());
                    logger.info("Page title: " + page.title());

                    // ページ内の主要な要素をチェック
                    logger.info("Links found: " + page.locator("a[href]").count());
                    logger.info("H3 elements found: " + page.locator("h3").count());
                    logger.info("Div elements found: " + page.locator("div").count());

                    throw new RuntimeException("Could not find any search results to click");
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to click first search result", e);
        }
    }

    /**
     * テキストを入力
     */
    public void input(String element, String value) {
        logger.info("Attempting to input '" + value + "' into element: '" + element + "'");

        try {
            // 1. CSS セレクタの場合
            if (element.startsWith("#") || element.startsWith(".") || element.contains("[") || element.contains(":")) {
                logger.info("Identified as CSS selector");
                getLocator(element).fill(value);
                logger.info("Successfully input using CSS selector: " + element);
                return;
            }

            // 2. 特定のパターンマッチング（検索ボックスなど）
            if (isSearchBox(element)) {
                logger.info("Identified as search box");
                inputToSearchBox(value);
                return;
            }

            // 3. 汎用的な入力フィールド検索
            if (inputByMultipleMethods(element, value)) {
                logger.info("Successfully input '" + value + "' into: " + element);
                return;
            }

            throw new RuntimeException("Could not find input element: " + element);

        } catch (Exception e) {
            throw new RuntimeException("Failed to input into element: " + element, e);
        }
    }

    /**
     * 検索ボックスかどうか判定
     */
    private boolean isSearchBox(String element) {
        String lower = element.toLowerCase();
        return lower.contains("検索") || lower.contains("search") ||
                lower.contains("サーチ") || lower.equals("検索窓");
    }

    /**
     * 複数の手法で入力を試行
     */
    private boolean inputByMultipleMethods(String element, String value) {
        // 試行する手法のリスト
        String[][] inputMethods = {
                { "placeholder", element }, // プレースホルダーで検索
                { "label", element }, // ラベルで検索
                { "name", element }, // name属性で検索
                { "id", element }, // id属性で検索（#なしの場合）
                { "title", element }, // title属性で検索
                { "aria-label", element }, // aria-label属性で検索
                { "class", element }, // class属性で検索（.なしの場合）
                { "type-text", "" }, // type="text"の入力フィールド
                { "type-email", "" }, // type="email"の入力フィールド
                { "type-password", "" }, // type="password"の入力フィールド
                { "textarea", "" } // textarea要素
        };

        for (String[] method : inputMethods) {
            try {
                if (tryInputMethod(method[0], method[1], value)) {
                    logger.info("Successfully input using method: " + method[0] + " with selector: " + method[1]);
                    return true;
                }
            } catch (Exception e) {
                // 次の手法を試行
                continue;
            }
        }

        return false;
    }

    /**
     * 指定された手法で入力を試行
     */
    private boolean tryInputMethod(String method, String selector, String value) {
        try {
            switch (method) {
                case "placeholder":
                    if (page.getByPlaceholder(selector).count() > 0) {
                        page.getByPlaceholder(selector).fill(value);
                        return true;
                    }
                    break;
                case "label":
                    if (page.getByLabel(selector).count() > 0) {
                        page.getByLabel(selector).fill(value);
                        return true;
                    }
                    break;
                case "name":
                    if (page.locator("[name='" + selector + "']").count() > 0) {
                        page.locator("[name='" + selector + "']").fill(value);
                        return true;
                    }
                    break;
                case "id":
                    if (page.locator("#" + selector).count() > 0) {
                        page.locator("#" + selector).fill(value);
                        return true;
                    }
                    break;
                case "title":
                    if (page.locator("[title='" + selector + "']").count() > 0) {
                        page.locator("[title='" + selector + "']").fill(value);
                        return true;
                    }
                    break;
                case "aria-label":
                    if (page.locator("[aria-label='" + selector + "']").count() > 0) {
                        page.locator("[aria-label='" + selector + "']").fill(value);
                        return true;
                    }
                    break;
                case "class":
                    if (page.locator("." + selector).count() > 0) {
                        page.locator("." + selector).first().fill(value);
                        return true;
                    }
                    break;
                case "type-text":
                    if (page.locator("input[type='text']").count() > 0) {
                        page.locator("input[type='text']").first().fill(value);
                        return true;
                    }
                    break;
                case "type-email":
                    if (page.locator("input[type='email']").count() > 0) {
                        page.locator("input[type='email']").first().fill(value);
                        return true;
                    }
                    break;
                case "type-password":
                    if (page.locator("input[type='password']").count() > 0) {
                        page.locator("input[type='password']").first().fill(value);
                        return true;
                    }
                    break;
                case "textarea":
                    if (page.locator("textarea").count() > 0) {
                        page.locator("textarea").first().fill(value);
                        return true;
                    }
                    break;
            }
        } catch (Exception e) {
            // この手法では見つからない
        }

        return false;
    }

    /**
     * 検索ボックスに入力（汎用的）
     */
    private void inputToSearchBox(String value) {
        // 一般的な検索ボックスのセレクタ
        String[] selectors = {
                "input[name='q']", // Google等
                "input[name='search']", // 一般的
                "input[type='search']", // HTML5 search type
                "input[placeholder*='検索']", // 日本語プレースホルダー
                "input[placeholder*='Search']", // 英語プレースホルダー
                "input[placeholder*='search']", // 小文字
                "input[aria-label*='検索']", // 日本語aria-label
                "input[aria-label*='Search']", // 英語aria-label
                "input[title*='検索']", // 日本語title
                "input[title*='Search']", // 英語title
                "textarea[name='q']", // Googleのテキストエリア版
                "input[class*='search']", // クラス名にsearchを含む
                "#search input", // searchというIDの下の入力
                ".search input", // searchというクラスの下の入力
                "input[type='text']:visible" // 表示されているテキスト入力（最後の手段）
        };

        boolean inputSuccess = false;

        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0 && page.locator(selector).isVisible()) {
                    page.locator(selector).fill(value);
                    inputSuccess = true;
                    logger.info("Successfully input using search box selector: " + selector);
                    break;
                }
            } catch (Exception e) {
                // 次のセレクタを試行
                continue;
            }
        }

        if (!inputSuccess) {
            throw new RuntimeException("Could not find search box with any known selector");
        }
    }

    /**
     * 要素の表示を待機
     */
    public void waitForElement(String element) {
        page.locator(element).waitFor();
        logger.info("Waited for element: " + element);
    }

    /**
     * 要素の内容を確認
     */
    public void verify(String element, String expectedValue) {
        String actualValue;
        if (element.toLowerCase().contains("title")) {
            actualValue = page.title();
        } else if (element.toLowerCase().contains("url")) {
            actualValue = page.url();
        } else {
            actualValue = page.locator(element).textContent();
        }

        if (actualValue != null && actualValue.contains(expectedValue)) {
            logger.info("Verification passed: " + element + " contains '" + expectedValue + "'");
        } else {
            throw new AssertionError("Verification failed: " + element + " does not contain '" + expectedValue + "'");
        }
    }

    /**
     * iframeに切り替える
     */
    private void switchToIframe(String iframeSelector) {
        try {
            com.microsoft.playwright.Locator iframeLocator = page.locator(iframeSelector);
            if (iframeLocator.count() == 0) {
                throw new RuntimeException("Iframe not found: " + iframeSelector);
            }
            currentIframeSelector = iframeSelector;
            logger.info("Switched to iframe: {}", iframeSelector);
        } catch (Exception e) {
            logger.error("Failed to switch to iframe: {}", iframeSelector, e);
            throw new RuntimeException("Failed to switch to iframe: " + iframeSelector, e);
        }
    }

    /**
     * メインフレームに戻る
     */
    private void switchToMainFrame() {
        currentIframeSelector = null;
        logger.info("Switched back to main frame");
    }

    /**
     * 現在のページまたはフレームでロケータを取得
     */
    private com.microsoft.playwright.Locator getLocator(String selector) {
        if (currentIframeSelector != null) {
            // iframe内の要素にアクセス
            return page.frameLocator(currentIframeSelector).locator(selector);
        }
        return page.locator(selector);
    }

    /**
     * 新しく開いたウィンドウに切り替える
     * 
     * @param expectedWindowTitle 期待するウィンドウタイトル（省略可）
     */
    private void switchToNewWindow(String expectedWindowTitle) {
        try {
            // 新しいページ（ウィンドウ）が開かれるまで待機
            Page newPage = page.context().waitForPage(() -> {
                // この中では特に何もしない（新しいページが開かれるのを待つだけ）
            });

            if (newPage != null) {
                // ウィンドウタイトルが指定されている場合は、そのタイトルを待機
                if (expectedWindowTitle != null && !expectedWindowTitle.trim().isEmpty()) {
                    waitForWindowTitle(newPage, expectedWindowTitle);
                }
                
                windowHandles.add(newPage);
                this.page = newPage;
                currentWindowIndex = windowHandles.size() - 1;
                currentIframeSelector = null; // 新しいウィンドウではiframeをリセット
                logger.info("Switched to new window (index: " + currentWindowIndex + ", title: " + newPage.title() + ")");
            } else {
                throw new RuntimeException("No new window was opened");
            }
        } catch (Exception e) {
            // 既に開いているウィンドウがある場合の処理
            java.util.List<Page> allPages = page.context().pages();
            if (allPages.size() > windowHandles.size()) {
                // 新しいページが見つかった
                Page newPage = allPages.get(allPages.size() - 1);
                
                // ウィンドウタイトルが指定されている場合は、そのタイトルを待機
                if (expectedWindowTitle != null && !expectedWindowTitle.trim().isEmpty()) {
                    waitForWindowTitle(newPage, expectedWindowTitle);
                }
                
                windowHandles.add(newPage);
                this.page = newPage;
                currentWindowIndex = windowHandles.size() - 1;
                currentIframeSelector = null;
                logger.info("Switched to new window (index: " + currentWindowIndex + ")");
            } else {
                throw new RuntimeException("Failed to switch to new window", e);
            }
        }
    }

    /**
     * 指定されたインデックスまたはタイトルのウィンドウに切り替える
     */
    private void switchToWindow(String windowIdentifier) {
        try {
            if (windowIdentifier == null || windowIdentifier.trim().isEmpty()) {
                logger.info("Window identifier is empty, staying on current window");
                return;
            }

            // 数字の場合はインデックスとして処理
            try {
                int index = Integer.parseInt(windowIdentifier.trim());
                if (index >= 0 && index < windowHandles.size()) {
                    this.page = windowHandles.get(index);
                    currentWindowIndex = index;
                    currentIframeSelector = null;
                    logger.info("Switched to window index: " + index);
                    return;
                }
            } catch (NumberFormatException e) {
                // 数字ではない場合はタイトルとして処理
            }

            // タイトルで検索
            for (int i = 0; i < windowHandles.size(); i++) {
                Page windowPage = windowHandles.get(i);
                String title = windowPage.title();
                if (title != null && title.toLowerCase().contains(windowIdentifier.toLowerCase())) {
                    this.page = windowPage;
                    currentWindowIndex = i;
                    currentIframeSelector = null;
                    logger.info("Switched to window with title containing: " + windowIdentifier);
                    return;
                }
            }

            throw new RuntimeException("Window not found: " + windowIdentifier);

        } catch (Exception e) {
            throw new RuntimeException("Failed to switch to window: " + windowIdentifier, e);
        }
    }

    /**
     * 指定されたウィンドウを閉じる
     * 
     * @param windowIdentifier ウィンドウ識別子（タイトルまたはインデックス、nullの場合は現在のウィンドウ）
     */
    private void closeWindow(String windowIdentifier) {
        try {
            if (windowHandles.size() <= 1) {
                logger.info("Cannot close the last window");
                return;
            }

            int windowIndexToClose = currentWindowIndex; // デフォルトは現在のウィンドウ

            // ウィンドウ識別子が指定されている場合は、そのウィンドウを検索
            if (windowIdentifier != null && !windowIdentifier.trim().isEmpty()) {
                windowIndexToClose = findWindowIndex(windowIdentifier);
                if (windowIndexToClose == -1) {
                    throw new RuntimeException("Window not found: " + windowIdentifier);
                }
            }

            // 指定されたウィンドウを閉じる
            Page windowToClose = windowHandles.get(windowIndexToClose);
            windowToClose.close();

            // ウィンドウリストから削除
            windowHandles.remove(windowIndexToClose);

            // 現在のウィンドウが閉じられた場合は、前のウィンドウに切り替え
            if (windowIndexToClose == currentWindowIndex) {
                if (currentWindowIndex > 0) {
                    currentWindowIndex--;
                } else {
                    currentWindowIndex = 0;
                }
                this.page = windowHandles.get(currentWindowIndex);
                currentIframeSelector = null;
            } else if (windowIndexToClose < currentWindowIndex) {
                // 現在のウィンドウより前のウィンドウが閉じられた場合はインデックスを調整
                currentWindowIndex--;
            }

            logger.info("Closed window: " + windowIdentifier + ", current window index: " + currentWindowIndex);

        } catch (Exception e) {
            throw new RuntimeException("Failed to close window: " + windowIdentifier, e);
        }
    }

    /**
     * ウィンドウ識別子からウィンドウインデックスを取得
     * 
     * @param windowIdentifier ウィンドウ識別子（インデックスまたはタイトル）
     * @return ウィンドウインデックス（見つからない場合は-1）
     */
    private int findWindowIndex(String windowIdentifier) {
        // 数字の場合はインデックスとして処理
        try {
            int index = Integer.parseInt(windowIdentifier.trim());
            if (index >= 0 && index < windowHandles.size()) {
                return index;
            }
        } catch (NumberFormatException e) {
            // 数字ではない場合はタイトルとして処理
        }

        // タイトルで検索
        for (int i = 0; i < windowHandles.size(); i++) {
            Page windowPage = windowHandles.get(i);
            String title = windowPage.title();
            if (title != null && title.toLowerCase().contains(windowIdentifier.toLowerCase())) {
                return i;
            }
        }

        return -1; // 見つからない
    }

    /**
     * 現在のウィンドウを閉じて前のウィンドウに戻る（下位互換性のため）
     */
    private void closeCurrentWindow() {
        closeWindow(null);
    }

    /**
     * 指定されたウィンドウタイトルを待機する
     * 
     * @param targetPage 対象のページ
     * @param expectedTitle 期待するタイトル（部分一致）
     */
    private void waitForWindowTitle(Page targetPage, String expectedTitle) {
        try {
            // 最大10秒間、ウィンドウタイトルを待機
            for (int i = 0; i < 100; i++) {
                String currentTitle = targetPage.title();
                if (currentTitle != null && currentTitle.toLowerCase().contains(expectedTitle.toLowerCase())) {
                    logger.info("Window title matched: " + currentTitle);
                    return;
                }
                Thread.sleep(100); // 100ms待機
            }
            logger.warn("Window title did not match expected: " + expectedTitle + ", actual: " + targetPage.title());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for window title", e);
        }
    }
}
