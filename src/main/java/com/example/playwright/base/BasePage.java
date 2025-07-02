package com.example.playwright.base;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Objectパターンの基底クラス
 * 汎用的なページ操作メソッドを提供
 */
public abstract class BasePage {
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    protected Page page;
    
    public BasePage(Page page) {
        this.page = page;
    }
    
    /**
     * 要素をクリック
     */
    protected void click(String selector) {
        logger.info("Click: {}", selector);
        page.locator(selector).click();
    }
    
    /**
     * 要素をクリック（要素が見えるまで待機）
     */
    protected void clickWhenVisible(String selector) {
        logger.info("ClickWhenVisible: {}", selector);
        page.locator(selector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.locator(selector).click();
    }
    
    /**
     * テキストを入力
     */
    protected void fill(String selector, String text) {
        logger.info("Fill: {} = {}", selector, text);
        page.locator(selector).fill(text);
    }
    
    /**
     * テキストを入力（既存のテキストをクリア）
     */
    protected void type(String selector, String text) {
        logger.info("Type: {} = {}", selector, text);
        Locator locator = page.locator(selector);
        locator.clear();
        locator.type(text);
    }
    
    /**
     * 要素のテキストを取得
     */
    protected String getText(String selector) {
        return page.locator(selector).textContent();
    }
    
    /**
     * 要素の属性値を取得
     */
    protected String getAttribute(String selector, String attribute) {
        return page.locator(selector).getAttribute(attribute);
    }
    
    /**
     * 要素が表示されているかチェック
     */
    protected boolean isVisible(String selector) {
        return page.locator(selector).isVisible();
    }
    
    /**
     * 要素が有効かチェック
     */
    protected boolean isEnabled(String selector) {
        return page.locator(selector).isEnabled();
    }
    
    /**
     * 要素が存在するまで待機
     */
    protected void waitForSelector(String selector) {
        page.locator(selector).waitFor();
    }
    
    /**
     * 要素が表示されるまで待機
     */
    protected void waitForVisible(String selector) {
        page.locator(selector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }
    
    /**
     * 要素が隠れるまで待機
     */
    protected void waitForHidden(String selector) {
        page.locator(selector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
    
    /**
     * 指定された時間待機
     */
    protected void wait(int milliseconds) {
        page.waitForTimeout(milliseconds);
    }
    
    /**
     * ページのタイトルを取得
     */
    protected String getTitle() {
        return page.title();
    }
    
    /**
     * 現在のURLを取得
     */
    protected String getCurrentUrl() {
        return page.url();
    }
    
    /**
     * ページをリロード
     */
    protected void reload() {
        page.reload();
    }
    
    /**
     * ブラウザの戻るボタン
     */
    protected void goBack() {
        page.goBack();
    }
    
    /**
     * ブラウザの進むボタン
     */
    protected void goForward() {
        page.goForward();
    }
    
    /**
     * セレクトボックスから値を選択
     */
    protected void selectOption(String selector, String value) {
        page.locator(selector).selectOption(value);
    }
    
    /**
     * チェックボックスをチェック
     */
    protected void check(String selector) {
        page.locator(selector).check();
    }
    
    /**
     * チェックボックスのチェックを外す
     */
    protected void uncheck(String selector) {
        page.locator(selector).uncheck();
    }
    
    /**
     * 要素の数を取得
     */
    protected int getElementCount(String selector) {
        return page.locator(selector).count();
    }
    
    /**
     * JavaScriptを実行
     */
    protected Object executeScript(String script) {
        return page.evaluate(script);
    }
    
    /**
     * 要素にフォーカス
     */
    protected void focus(String selector) {
        page.locator(selector).focus();
    }
    
    /**
     * 要素をホバー
     */
    protected void hover(String selector) {
        page.locator(selector).hover();
    }
}
