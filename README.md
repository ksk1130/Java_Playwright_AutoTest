# Playwright for Java E2E Testing Tool

このプロジェクトは、**Playwright for Java** を使ったノーコードE2E自動テストフレームワークです。

## 特徴

- **CSV/Excelでテストシナリオ作成**: Javaスキル不要、表形式で直感的にE2Eテストを記述
- **汎用的なアクション対応**: クリック/入力/選択/検証/iframe/ウィンドウ/Shadow DOM/プルダウン/スクリーンショット等
- **Page Objectパターン**: メンテナンス性の高いテストコードも併用可能
- **設定管理**: プロパティファイル・システムプロパティで柔軟に設定
- **テストデータ管理**: JSONやCSVで外部化
- **自動スクリーンショット**: テスト失敗時に自動保存
- **複数ブラウザ対応**: Chrome/Edge/Firefox（システムインストール版）
- **iframe/ウィンドウ/Shadow DOM/プルダウン**: 複雑なUIもノーコードで自動化

## 環境要件

- Java 21+
- Gradle 7.0+
- システムにChrome/Edge/Firefoxのいずれかがインストールされていること

## セットアップ

```bash
./gradlew build
```

## テスト実行方法

```bash
# 全テストを実行
./gradlew test

# Chrome/Edge/Firefoxを指定
./gradlew runChrome
./gradlew runEdge
./gradlew runFirefox

# ヘッドレス実行
./gradlew runHeadless
```

## CSV/Excelでのシナリオ記述例

```csv
ステップ,アクション,要素,入力値,期待結果,説明
1,アクセス,https://example.com,,,サイトにアクセス
2,入力,#username,testuser,,ユーザー名を入力
3,入力,#password,password123,,パスワードを入力
4,選択,#country,Japan,,国を選択
5,クリック,#loginButton,,,ログインボタンをクリック
6,確認,title,Welcome,,タイトルにWelcomeが含まれることを確認
7,スクリーンショット,login_success,,,ログイン成功画面をキャプチャ
```

- **アクション**: アクセス, クリック, 入力, 選択, 確認, スクリーンショット, iframe, main, newwindow, window, closewindow など
- **要素**: CSSセレクタ/ラベル/テキスト/特殊キーワード（検索ボタン, 検索, 検索結果1つ目 など）
- **入力値**: 入力や選択時の値

詳細は `ACTION_REFERENCE.md` を参照してください。

## よくある操作例

### 1. プルダウン選択
```csv
4,選択,#country,Japan,,国を選択
5,選択,select[name='prefecture'],東京都,,都道府県を選択
```

### 2. iframe内の要素操作
```csv
2,iframe,#iframeResult iframe,,,iframeに切り替え
3,入力,input[name='fname'],太郎,,名前を入力
6,main,,,,,メインフレームに戻る
```

### 3. 新しいウィンドウ（ポップアップ）操作
```csv
6,クリック,#openHelp,,,ヘルプウィンドウを開く
7,newwindow,,,,,新しく開いたウィンドウに切り替え
8,確認,title,Help,,ヘルプページであることを確認
9,closewindow,,,,,ウィンドウを閉じる
10,window,,0,,元のウィンドウに戻る
```

### 4. Shadow DOM要素の操作
```csv
2,入力,css=custom-element >> css=#shadowInput,テスト値,,Shadow DOM内のinputに入力
```

- Playwrightの複合セレクタ（`>>`）でShadow DOM横断が可能

## サンプルシナリオ
- `src/main/resources/testdata/` 配下に多数のサンプルCSVあり
  - `google_search_scenario.csv`（Google検索）
  - `sample_generic_scenario.csv`（汎用フォーム）
  - `iframe_test_scenario.csv`（iframe/ウィンドウ/決済例）
  - `dropdown_test_scenario.csv`（プルダウン）

## 主要ファイル構成

```
src/
├── main/java/com/example/playwright/
│   ├── base/         # テスト基底クラス
│   ├── config/       # 設定管理
│   ├── pages/        # Page Object
│   └── utils/        # 汎用ユーティリティ
├── test/java/com/example/playwright/
│   ├── tests/        # テストクラス
│   └── utils/        # テスト用ユーティリティ
└── main/resources/
    ├── test.properties      # 設定
    └── testdata/           # テストデータ（CSV/JSON）
```

## よく使う設定例

`src/main/resources/test.properties`:
```properties
playwright.browser=chrome
playwright.headless=false
playwright.slowMo=0
playwright.timeout=30000
playwright.useSystemBrowser=true
test.baseUrl=http://localhost:3000
test.environment=local
screenshot.onFailure=true
screenshot.directory=screenshots
```

## よくある質問

### Q. iframeやウィンドウ、Shadow DOMの深い要素も操作できる？
A. できます！
- `iframe`アクションでフレーム切り替え、`main`で戻る
- `newwindow`/`window`でウィンドウ切り替え
- Shadow DOMは `css=custom-element >> css=#target` のようにPlaywright複合セレクタで指定

### Q. CSVでどんなアクションが使える？
A. 主要アクション・記述例は `ACTION_REFERENCE.md` を参照してください。

### Q. Excel（.xlsx）も使える？
A. 現状はCSV推奨ですが、拡張でExcel対応も可能です。

### Q. CSVシナリオをJavaテストクラスから実行する方法は？
A. `CSVDataDrivenTest` を継承したテストクラスで、`executeTestStepsFromCsv("ファイル名.csv")` を呼び出すだけで、CSVシナリオを実行できます。

### サンプルテストクラス
```java
package com.example.playwright.tests;

import com.example.playwright.base.CSVDataDrivenTest;
import org.junit.Test;

public class SampleCsvTest extends CSVDataDrivenTest {
    @Test
    public void testScenarioFromCsv() {
        // src/main/resources/testdata/sample_generic_scenario.csv を実行
        executeTestStepsFromCsv("sample_generic_scenario.csv");
    }
}
```

- CSVファイルは `src/main/resources/testdata/` 配下に配置してください。
- 複数のシナリオファイルを使い分けることも可能です。

## 拡張性
- Page Object追加、カスタムアクション追加、CI/CD連携、並列実行など柔軟に拡張可能

## 参考
- [Playwright for Java公式](https://playwright.dev/java/)
- [ACTION_REFERENCE.md](./ACTION_REFERENCE.md)

---
MIT License
