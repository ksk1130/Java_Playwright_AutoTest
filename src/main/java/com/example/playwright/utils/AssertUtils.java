package com.example.playwright.utils;

import com.microsoft.playwright.Page;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 汎用的なアサーションユーティリティクラス
 */
public class AssertUtils {
    private static final Logger logger = LoggerFactory.getLogger(AssertUtils.class);
    /**
     * ページタイトルをアサート
     */
    public static void assertTitle(Page page, String expectedTitle) {
        String actualTitle = page.title();
        logger.info("タイトル検証: actual='{}', expected='{}'", actualTitle, expectedTitle);
        assertThat("ページタイトルが期待値と異なります", actualTitle, equalTo(expectedTitle));
    }
    
    /**
     * ページタイトルに指定の文字列が含まれることをアサート
     */
    public static void assertTitleContains(Page page, String expectedText) {
        String actualTitle = page.title();
        logger.info("タイトル部分一致検証: actual='{}', contains='{}'", actualTitle, expectedText);
        assertThat("ページタイトルに期待される文字列が含まれていません", actualTitle, containsString(expectedText));
    }
    
    /**
     * URLをアサート
     */
    public static void assertUrl(Page page, String expectedUrl) {
        String actualUrl = page.url();
        logger.info("URL検証: actual='{}', expected='{}'", actualUrl, expectedUrl);
        assertThat("URLが期待値と異なります", actualUrl, equalTo(expectedUrl));
    }
    
    /**
     * URLに指定の文字列が含まれることをアサート
     */
    public static void assertUrlContains(Page page, String expectedText) {
        String actualUrl = page.url();
        logger.info("URL部分一致検証: actual='{}', contains='{}'", actualUrl, expectedText);
        assertThat("URLに期待される文字列が含まれていません", actualUrl, containsString(expectedText));
    }
    
    /**
     * 要素の表示をアサート
     */
    public static void assertElementVisible(Page page, String selector) {
        boolean isVisible = page.locator(selector).isVisible();
        logger.info("要素表示検証: selector='{}', isVisible={}", selector, isVisible);
        Assert.assertTrue("要素が表示されていません: " + selector, isVisible);
    }
    
    /**
     * 要素の非表示をアサート
     */
    public static void assertElementNotVisible(Page page, String selector) {
        boolean isVisible = page.locator(selector).isVisible();
        logger.info("要素非表示検証: selector='{}', isVisible={}", selector, isVisible);
        Assert.assertFalse("要素が表示されています: " + selector, isVisible);
    }
    
    /**
     * 要素の存在をアサート
     */
    public static void assertElementExists(Page page, String selector) {
        int count = page.locator(selector).count();
        logger.info("要素存在検証: selector='{}', count={}", selector, count);
        Assert.assertTrue("要素が存在しません: " + selector, count > 0);
    }
    
    /**
     * 要素の非存在をアサート
     */
    public static void assertElementNotExists(Page page, String selector) {
        int count = page.locator(selector).count();
        logger.info("要素非存在検証: selector='{}', count={}", selector, count);
        Assert.assertEquals("要素が存在します: " + selector, 0, count);
    }
    
    /**
     * 要素のテキストをアサート
     */
    public static void assertElementText(Page page, String selector, String expectedText) {
        String actualText = page.locator(selector).textContent();
        logger.info("要素テキスト検証: selector='{}', actualText='{}', expectedText='{}'", selector, actualText, expectedText);
        assertThat("要素のテキストが期待値と異なります", actualText, equalTo(expectedText));
    }
    
    /**
     * 要素のテキストに指定の文字列が含まれることをアサート
     */
    public static void assertElementTextContains(Page page, String selector, String expectedText) {
        String actualText = page.locator(selector).textContent();
        logger.info("要素テキスト部分一致検証: selector='{}', actualText='{}', contains='{}'", selector, actualText, expectedText);
        assertThat("要素のテキストに期待される文字列が含まれていません", actualText, containsString(expectedText));
    }
    
    /**
     * 要素の属性値をアサート
     */
    public static void assertElementAttribute(Page page, String selector, String attribute, String expectedValue) {
        String actualValue = page.locator(selector).getAttribute(attribute);
        logger.info("要素属性値検証: selector='{}', attribute='{}', actualValue='{}', expectedValue='{}'", selector, attribute, actualValue, expectedValue);
        assertThat("要素の属性値が期待値と異なります", actualValue, equalTo(expectedValue));
    }
    
    /**
     * 要素の有効性をアサート
     */
    public static void assertElementEnabled(Page page, String selector) {
        boolean isEnabled = page.locator(selector).isEnabled();
        logger.info("要素有効性検証: selector='{}', isEnabled={}", selector, isEnabled);
        Assert.assertTrue("要素が有効ではありません: " + selector, isEnabled);
    }
    
    /**
     * 要素の無効性をアサート
     */
    public static void assertElementDisabled(Page page, String selector) {
        boolean isEnabled = page.locator(selector).isEnabled();
        logger.info("要素無効性検証: selector='{}', isEnabled={}", selector, isEnabled);
        Assert.assertFalse("要素が有効です: " + selector, isEnabled);
    }
    
    /**
     * 要素数をアサート
     */
    public static void assertElementCount(Page page, String selector, int expectedCount) {
        int actualCount = page.locator(selector).count();
        logger.info("要素数検証: selector='{}', actualCount={}, expectedCount={}", selector, actualCount, expectedCount);
        Assert.assertEquals("要素数が期待値と異なります", expectedCount, actualCount);
    }
    
    /**
     * チェックボックスのチェック状態をアサート
     */
    public static void assertCheckboxChecked(Page page, String selector) {
        boolean isChecked = page.locator(selector).isChecked();
        logger.info("チェックボックスチェック状態検証: selector='{}', isChecked={}", selector, isChecked);
        Assert.assertTrue("チェックボックスがチェックされていません: " + selector, isChecked);
    }
    
    /**
     * チェックボックスの非チェック状態をアサート
     */
    public static void assertCheckboxUnchecked(Page page, String selector) {
        boolean isChecked = page.locator(selector).isChecked();
        logger.info("チェックボックス非チェック状態検証: selector='{}', isChecked={}", selector, isChecked);
        Assert.assertFalse("チェックボックスがチェックされています: " + selector, isChecked);
    }
    
    /**
     * 入力フィールドの値をアサート
     */
    public static void assertInputValue(Page page, String selector, String expectedValue) {
        String actualValue = page.locator(selector).inputValue();
        logger.info("入力フィールド値検証: selector='{}', actualValue='{}', expectedValue='{}'", selector, actualValue, expectedValue);
        assertThat("入力フィールドの値が期待値と異なります", actualValue, equalTo(expectedValue));
    }
}
