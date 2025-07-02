package com.example.playwright.tests;

import com.example.playwright.base.CSVDataDrivenTest;
import org.junit.Test;

/**
 * Excel/CSV駆動型のGoogle検索テスト
 */
public class GoogleSearchTest extends CSVDataDrivenTest {
    
    @Test
    public void testGoogleSearchScenario() {
        System.out.println("=== Google検索シナリオテスト開始 ===");
        
        // CSVファイルからテストステップを実行
        executeTestStepsFromCsv("google_search_scenario.csv");
        
        System.out.println("=== Google検索シナリオテスト完了 ===");
        
        // 最終的な結果確認
        try {
            Thread.sleep(3000); // 3秒待機してページが完全に読み込まれるのを待つ
            
            // ページタイトルにPlaywrightが含まれているか確認
            String title = page.title();
            if (title.toLowerCase().contains("playwright")) {
                System.out.println("✅ 成功: ページタイトルに 'Playwright' が含まれています: " + title);
            } else {
                System.out.println("⚠️  警告: ページタイトルに 'Playwright' が含まれていません: " + title);
            }
            
            // 現在のURLを表示
            System.out.println("現在のURL: " + page.url());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
