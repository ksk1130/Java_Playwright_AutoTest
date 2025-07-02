package com.example.playwright.tests;

import com.example.playwright.base.CSVDataDrivenTest;
import org.junit.Test;

/**
 * 簡単なGoogle検索テスト（デバッグ用）
 */
public class SimpleGoogleTest extends CSVDataDrivenTest {
    
    @Test
    public void testSimpleGoogleSearch() {
        System.out.println("=== 簡単なGoogle検索テスト開始 ===");
        
        // CSVファイルからテストステップを実行
        executeTestStepsFromCsv("simple_google_test.csv");
        
        System.out.println("=== 簡単なGoogle検索テスト完了 ===");
        
        // 結果確認
        try {
            Thread.sleep(3000); // 3秒待機
            
            // ページタイトルを確認
            String title = page.title();
            System.out.println("ページタイトル: " + title);
            
            // 現在のURLを表示
            System.out.println("現在のURL: " + page.url());
            
            // 検索結果があるかチェック
            int searchResults = page.locator("h3").count();
            System.out.println("検索結果数: " + searchResults);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
