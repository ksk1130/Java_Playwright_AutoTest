package com.example.playwright.base;

import com.example.playwright.utils.TestActionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.io.FileInputStream;

/**
 * CSV駆動型テストの基底クラス
 */
public class CSVDataDrivenTest extends BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(CSVDataDrivenTest.class);
    protected TestActionExecutor actionExecutor;

    public static void main(String[] args) {

        // コマンドラインからCSVファイルを指定して単体実行可能
        logger.info("CSVDataDrivenTest main method called. コマンドライン実行モード");
        if (args.length == 0) {
            logger.error("CSVファイル名（フルパス）を指定してください。");
            System.exit(1);
        }
        String csvFileName = args[0];
        CSVDataDrivenTest runner = new CSVDataDrivenTest() {
            @Override
            protected void additionalSetUp() {
                actionExecutor = new TestActionExecutor(page);
            }
        };
        try {
            runner.setUp(args[1]);
            runner.executeTestStepsFromCsv(csvFileName);
        } catch (Exception e) {
            logger.error("テスト実行中にエラー: {}", e.getMessage(), e);
            System.exit(2);
        } finally {
            runner.tearDown();
        }
    }

    @Override
    protected void additionalSetUp() {
        actionExecutor = new TestActionExecutor(page);
    }

    /**
     * CSVファイルからテストステップを実行
     */
    protected void executeTestStepsFromCsv(String csvFileName) {
        logger.info("CSVシナリオ実行: {}", csvFileName);
        List<Map<String, String>> steps = loadCsvData(csvFileName);
        for (Map<String, String> step : steps) {
            String stepNo = step.get("ステップ");
            String action = step.get("アクション");
            String element = step.get("要素");
            String inputValue = step.get("入力値");
            String description = step.get("説明");
            // ステップ番号が空の場合はスキップ
            if (stepNo == null || stepNo.trim().isEmpty()) {
                continue;
            }
            logger.info("実行中: ステップ {} - {}", stepNo, description);
            try {
                actionExecutor.executeAction(action, element, inputValue);
                Thread.sleep(10000); // 各ステップ間で10秒待機
            } catch (Exception e) {
                logger.error("ステップ {} でエラー: {}", stepNo, e.getMessage(), e);
                throw new RuntimeException("Test step failed: " + stepNo, e);
            }
        }
    }

    /**
     * CSVファイルからデータを読み込む（フルパス対応）
     * 
     * @param csvFileName フルパスのCSVファイル名
     * @return 各行をMap化したリスト
     */
    private List<Map<String, String>> loadCsvData(String csvFileName) {
        try {
            InputStream is = new FileInputStream(csvFileName); // フルパスでファイルを開く
            List<Map<String, String>> data = new ArrayList<>();
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            String[] headers = null;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                String[] values = line.split(",", -1); // -1 to include empty strings
                if (headers == null) {
                    headers = values; // 最初の行はヘッダー
                    logger.info("CSV Headers: {}", Arrays.toString(headers));
                } else {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                        String key = headers[i].trim();
                        String value = values[i].trim();
                        row.put(key, value);
                        logger.debug("  {} = {}", key, value);
                    }
                    data.add(row);
                }
            }
            bufferedReader.close();
            return data;
        } catch (Exception e) {
            logger.error("Failed to load CSV file: {}", csvFileName, e);
            throw new RuntimeException("Failed to load CSV file: " + csvFileName, e);
        }
    }
}
