package com.example.playwright.utils;

import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSVテンプレートファイルを生成するユーティリティクラス
 */
public class CSVTemplateGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CSVTemplateGenerator.class);
    /**
     * テストシナリオ用のCSVテンプレートを生成
     */
    public static void generateTestScenarioTemplate(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // ヘッダー行
            writer.write("ステップ,アクション,要素,入力値,期待結果,説明\n");
            // サンプル行
            writer.write("1,アクセス,https://example.com,,,指定URLにアクセス\n");
            writer.write("2,入力,#username,testuser,,ユーザー名を入力\n");
            writer.write("3,入力,#password,password123,,パスワードを入力\n");
            writer.write("4,クリック,#loginButton,,,ログインボタンをクリック\n");
            writer.write("5,確認,title,ダッシュボード,ダッシュボード,タイトルを確認\n");
            logger.info("テンプレートファイルを生成しました: {}", fileName);
        } catch (IOException e) {
            logger.error("テンプレートファイルの生成に失敗しました: {}", fileName, e);
        }
    }
    /**
     * アクション一覧を生成
     */
    public static void generateActionReference(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("# テストアクション一覧\n\n");
            writer.write("## 基本アクション\n");
            writer.write("- **アクセス** / navigate / goto: 指定URLにアクセス\n");
            writer.write("- **クリック** / click: 要素をクリック\n");
            writer.write("- **入力** / input / type: テキストを入力\n");
            writer.write("- **待機** / wait: 要素が表示されるまで待機\n");
            writer.write("- **確認** / verify: 要素の内容を確認\n\n");
            writer.write("## 要素の指定方法\n");
            writer.write("- **ID**: #elementId\n");
            writer.write("- **クラス**: .className\n");
            writer.write("- **属性**: [name='elementName']\n");
            writer.write("- **テキスト**: ボタンに表示されているテキスト\n");
            writer.write("- **特殊**: 検索、検索ボタン、検索結果1つ目 など\n\n");
            writer.write("## サンプルシナリオ\n");
            writer.write("1. Google検索:\n");
            writer.write("   - アクセス: www.google.com\n");
            writer.write("   - 入力: 検索窓に「検索キーワード」\n");
            writer.write("   - クリック: 検索ボタン\n");
            writer.write("   - クリック: 検索結果1つ目\n");
            logger.info("アクション一覧ファイルを生成しました: {}", fileName);
        } catch (IOException e) {
            logger.error("アクション一覧ファイルの生成に失敗しました: {}", fileName, e);
        }
    }
    public static void main(String[] args) {
        // テンプレートファイルを生成
        generateTestScenarioTemplate("test_scenario_template.csv");
        generateActionReference("action_reference.md");
    }
}
