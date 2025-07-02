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

/**
 * CSV駆動型テストの基底クラス
 */
public abstract class CSVDataDrivenTest extends BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(CSVDataDrivenTest.class);
    protected TestActionExecutor actionExecutor;
    
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
                Thread.sleep(1000); // 各ステップ間で1秒待機
            } catch (Exception e) {
                logger.error("ステップ {} でエラー: {}", stepNo, e.getMessage(), e);
                throw new RuntimeException("Test step failed: " + stepNo, e);
            }
        }
    }
    
    /**
     * CSVファイルからデータを読み込む
     */
    private List<Map<String, String>> loadCsvData(String csvFileName) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("testdata/" + csvFileName);
            if (is == null) {
                throw new RuntimeException("CSV file not found: " + csvFileName);
            }
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
