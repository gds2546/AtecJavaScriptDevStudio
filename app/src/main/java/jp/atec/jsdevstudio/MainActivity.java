package jp.atec.jsdevstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    //インスタンス保存とリストアの時に参照する現在ビューの情報
    public static final int APP_CURRENTVIEW_RUN = 0;
    public static final int APP_CURRENTVIEW_EDIT = 1;
    public static final int APP_CURRENTVIEW_FILE = 2;
    public static final int APP_CURRENTVIEW_HELP = 3;

    //実行ビューで表示する内容のステート
    public static final int APP_RUNVIEW_SHOW_SCRIPT = 0;
    public static final int APP_RUNVIEW_SHOW_DOCUMENT = 1;

    private Activity activity;
    private int iWhereView;//現在どのビューを表示しているかのステート格納
    private int iRunShow;//実行ビューで表示している内容のステート

    //private AdRequest areq;

    //実行ビューのコントロール群
    private Button btnRUNbackeditor;
    private WebView wvRUNapp;
    private AdView advRUNads;
    //エディタビューのコントロール群
    private Button btnEDITORrun;
    private Button btnEDITORsave;//編集ビュー：セーブボタン
    private Button btnEDITORload;//編集ビュー:ロードボタン
    private Button btnEDITORhelp;//編集ビュー:ヘルプボタン
    private EditText etEDITORsrc;

    //ヘルプビューのコントロール
    private Button btnHELPbacktoeditor;//ヘルプビュー内エディタへ戻るボタン
    private Button btnHELPloadhellosample;
    private Button btnHELPreadme;

    private LinearLayout lyBASEcontainer;

    private String strSrc;//編集されているソースコード
    private String strIntDoc;//ドキュメント表示用文字列

    private AssetManager assetmanager;//ローカルアセットの操作用
    private InputStream inputstream;//ファイル操作用
    private AtecFileMenuClass fileclass;

    //ファイルメニュービューのコントロール
    private TextView tvFILEcwd;//現在ディレクトリの表示
    private EditText etFILEfilename;//選択中のファイル名
    private Button btnFILEopenselected;
    private Button btnFILEinternal;
    private Button btnFILEexternal;
    private Button btnFILEcancel;
    private Button btnFILEprevdir;

    private ListView lvFILEfiles;//ファイル一覧の表示用リストビュー

    private int iDesiredFileViewMode;//ファイルビューのモード

    int iMessageLineNumber;//WebViewのコンソールメッセージ
    String strMessage;//WebViewのコンソールメッセージ本体

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        assetmanager = getResources().getAssets();
        setContentView(R.layout.baselayout);
        lyBASEcontainer = findViewById(R.id.lyBASEcontainer);

        //2022/1/15 AdMob関連を一時無効化　まだ新アプリのためのID取ってないため
        //MobileAds.initialize(this, "ca-app-pub-1914363109829772~1645904228");
        //areq = new AdRequest.Builder().addTestDevice("6402B19FD0A1D78507E9F01DD00DDC3C")
        //        .addTestDevice("3E6BD10F27D5AF4CBFC0943C61B5BA09")
        //        .build();//ZeoFone2 and P028 as test device
        //ファイルメニュー制御クラスの新規生成と初期化処理
        fileclass = new AtecFileMenuClass(this);

        app_V_refreshEditorView();
    }

    //インスタンス保存要求時の処理
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //保存すべき項目のBundleへの投入
        outState.putInt("iwhereview", iWhereView);
        outState.putString("sourcecode", strSrc);
        outState.putString("document", strIntDoc);
        //現在のパス
        outState.putString("cwdabsolute", fileclass.GetAbsoluteCwd());
        switch (iWhereView) {
            case APP_CURRENTVIEW_RUN://実行ビュー
                //ソースコード表示中かアセット内ドキュメント表示中かのステート保存
                outState.putInt("runshow", iRunShow);
                break;
            case APP_CURRENTVIEW_EDIT://エディタビュー
                break;
            case APP_CURRENTVIEW_FILE://ファイル操作ビュー
                outState.putInt("desiredmode", iDesiredFileViewMode);
                //選択中のファイル名
                outState.putString("selectedfile", etFILEfilename.getText().toString());
                break;
            case APP_CURRENTVIEW_HELP://ヘルプビュー
                //2017/12/19 現在のビュー固有書き出し項目なし
                break;
        }

    }

    //インスタンス復帰時の処理
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        int i;
        String s;
        super.onRestoreInstanceState(savedInstanceState);
        //復帰する項目のBundleからの書き戻し
        strSrc = savedInstanceState.getString("sourcecode");
        strIntDoc = savedInstanceState.getString("document");
        iWhereView = savedInstanceState.getInt("iwhereview");
        s = savedInstanceState.getString("cwdabsolute");
        fileclass.SetCWDAbsolutePath(s);
        switch (iWhereView) {
            case APP_CURRENTVIEW_RUN:
                iRunShow = savedInstanceState.getInt("runshow");
                app_V_replaceContainerViews(R.layout.runnerlayout);
                app_V_refreshRunView(iRunShow);
                break;
            case APP_CURRENTVIEW_EDIT:
                app_V_replaceContainerViews(R.layout.editorview);
                app_V_refreshEditorView();//ソースコードのエディットテキストへの書き戻しは関数内で行われる
                break;
            case APP_CURRENTVIEW_FILE:
                i = savedInstanceState.getInt("desiredmode");
                app_V_replaceContainerViews(R.layout.fileopenerview);
                app_V_refreshFileMenuView(i);
                app_V_refreshCWDlist();
                //パスとファイル名の復帰
                tvFILEcwd.setText(fileclass.GetCwd());
                s = savedInstanceState.getString("selectedfile");
                etFILEfilename.setText(s);
                break;
            case APP_CURRENTVIEW_HELP:
                //2017/12/19 現在のビュー固有書き戻し項目なし
                break;
        }
    }

    //ビュー入れ替え処理
    //iIDには新しくinflateするレイアウトリソースIDを渡す
    //処理した場合はtrueを返す
    //lyBASEcontainerがnullなら処理を行わずfalseを返す
    public boolean app_V_replaceContainerViews(int iID) {
        //ビュー入れ替え処理
        if (lyBASEcontainer != null) {
            lyBASEcontainer.removeAllViews();
            getLayoutInflater().inflate(iID, lyBASEcontainer);
            return true;
        }
        return false;
    }

    //ヘルプビュー内のビューの初期化とリスナ設定
    public void app_V_refreshHelpView() {
        btnHELPbacktoeditor = findViewById(R.id.btnHELPbacktoeditor);//エディタに戻る
        btnHELPbacktoeditor.setOnClickListener(this);
        btnHELPloadhellosample = findViewById(R.id.btnHELPloadhellosample);
        btnHELPloadhellosample.setOnClickListener(this);
        btnHELPreadme = findViewById(R.id.btnHELPreadme);
        btnHELPreadme.setOnClickListener(this);
        iWhereView = APP_CURRENTVIEW_HELP;
    }

    @Override
    public void onClick(View v) {
        int iID;
        SpannableStringBuilder sb;
        iID = v.getId();
        switch (iID) {
            case R.id.btnRUNbackeditor:
                //実行モードからエディタに戻るボタン
                if (app_V_replaceContainerViews(R.layout.editorview)) {
                    //エディタービューのイベントリスナ再設定ほか処理
                    app_V_refreshEditorView();
                }
                break;
            //ここから編集ビューのボタン類処理
            case R.id.btnEDITORrunscript:
                //編集中のエディットビューからソースを取得する
                sb = (SpannableStringBuilder) etEDITORsrc.getText();
                strSrc = sb.toString();
                if (app_V_replaceContainerViews(R.layout.runnerlayout)) {
                    //ランナービューに切り替えてから実行
                    app_V_refreshRunView(APP_RUNVIEW_SHOW_SCRIPT);
                }
                break;
            case R.id.btnEDITORsave:
                //編集中のエディットビューからソースを取得する
                sb = (SpannableStringBuilder) etEDITORsrc.getText();
                strSrc = sb.toString();
                //SAVEボタンの処理
                if (app_V_replaceContainerViews(R.layout.fileopenerview)) {
                    app_V_refreshFileMenuView(0);//0指定=SAVEモード
                    //CWDの内容をリストビューに反映
                    app_V_refreshCWDlist();
                }
                break;
            case R.id.btnEDITORload:
                //LOADボタンの処理
                sb = (SpannableStringBuilder) etEDITORsrc.getText();
                strSrc = sb.toString();//読込ビューからキャンセルして戻ってきたときの復帰用
                if (app_V_replaceContainerViews(R.layout.fileopenerview)) {
                    app_V_refreshFileMenuView(1);//1指定=LOADモード
                    app_V_refreshCWDlist();
                }
                break;
            case R.id.btnEDITORhelp:
                sb = (SpannableStringBuilder) etEDITORsrc.getText();
                strSrc = sb.toString();
                if (app_V_replaceContainerViews(R.layout.helpview)) {
                    app_V_refreshHelpView();
                }
                break;
            //ここからファイルメニュービューのボタン処理
            case R.id.btnFILEcancel://ファイルメニュービューのキャンセルボタン
                if (app_V_replaceContainerViews(R.layout.editorview)) {
                    app_V_refreshEditorView();
                }
                break;
            case R.id.btnFILEopenselected://ファイルメニュービューの選択ファイルオープンボタン
                String strName;
                boolean bResult;
                strName = etFILEfilename.getText().toString();
                //ファイル名指定がない場合は処理せず抜ける
                if (strName.isEmpty())
                    break;
                if (iDesiredFileViewMode == 0) { //SAVEモード指定中
                    //ファイル名指定がない場合は処理せず抜ける
                    if (strName.isEmpty())
                        break;
                    try {
                        bResult = fileclass.NewSaveFileToCWD(strName, strSrc);
                        if (!bResult) {//新規作成でないファイルを保存しようとした
                            //上書き保存を実行した場合の処理
                            fileclass.OverWriteFileToCWD(strName, strSrc);
                        }
                    } catch (IOException e) {
                        Toast.makeText(this, "File operation FAILED : " + strName, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    //エディタービューに戻る
                    app_V_replaceContainerViews(R.layout.editorview);
                    app_V_refreshEditorView();
                    etEDITORsrc.setText(strSrc);//ソースコードをエディットテキストに書き戻す
                    //ファイルを保存したToastを出す
                    Toast.makeText(this, "File saved : " + strName, Toast.LENGTH_SHORT).show();
                } else if (iDesiredFileViewMode == 1) {//LOADモード指定中
                    //ファイル名指定がない場合は処理せず抜ける
                    if (strName.isEmpty())
                        break;
                    //注記 存在しないファイルを指定した場合の処理はビューのエディットコントロールの書き込み禁止で対処
                    try {
                        strSrc = fileclass.LoadFileFromCWD(strName);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //エディタビューに戻る
                    app_V_replaceContainerViews(R.layout.editorview);
                    app_V_refreshEditorView();
                    //エディタビューへソースを反映
                    etEDITORsrc.setText(strSrc);//ソースコードをエディットテキストに書き戻す
                    //ファイル読み込んだToast出す
                    Toast.makeText(this, "File loaded : " + strName, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnFILEinternal://ファイルメニュービュー : 内部ストレージのデフォルトディレクトリ選択
                fileclass.SetInternalStorageDirectory(this);
                app_V_refreshCWDlist();
                break;
            case R.id.btnFILEexternal://ファイルメニュービュー　:外部ストレージボタン
                //外部ストレージの状態を判定してマウントされていない場合は処理から抜ける
                if (!fileclass.IsExternalStorageMounted())
                    break;
                if (Build.VERSION.SDK_INT >= 19) {//APIレベル19以降は外部ストレージのドキュメントフォルダを開く
                    fileclass.SetExternalStorageDirectory(this);//外部ストレージの共有ドキュメントフォルダを開く
                } else {
                    fileclass.SetExternalStoragePublicDirectoryOlderThanKitkKat();//Kitkat以前はSDカードのルート/Documentsを開く
                }
                app_V_refreshCWDlist();
                break;
            case R.id.btnFILEprevdir: // ファイルメニュービュー : 親ディレクトリボタン
                fileclass.SetCwdToParent();
                app_V_refreshCWDlist();
                break;
            //ここからヘルプビュー内コントロールでの処理
            case R.id.btnHELPbacktoeditor:
                app_V_replaceContainerViews(R.layout.editorview);
                app_V_refreshEditorView();
                etEDITORsrc.setText(strSrc);//ソースコードをエディットテキストに書き戻す
                break;
            case R.id.btnHELPloadhellosample://サンプルソースHELLO読込処理
                String strSample;
                strSample = APP_I_LoadAssetString("samplecode.html");
                if (strSample != null) {
                    app_V_replaceContainerViews(R.layout.editorview);
                    app_V_refreshEditorView();
                    etEDITORsrc.setText(strSample, TextView.BufferType.NORMAL);
                    Toast.makeText(this, "sample loaded : hello", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnHELPreadme:
                strIntDoc = APP_I_LoadAssetString("readme.html");
                if (strIntDoc != null) {
                    //実行用ビューをドキュメント表示モードに切り替えて読み込んだアセットを表示
                    app_V_replaceContainerViews(R.layout.runnerlayout);
                    app_V_refreshRunView(APP_RUNVIEW_SHOW_DOCUMENT);
                }
                break;
        }
    }

    //アセット読込してStringで返す サンプルソース用
    private String APP_I_LoadAssetString(String strAssetName) {
        StringBuilder strbContent;
        int iSize;
        int r;
        byte[] bFile;

        //アセットからサンプルコードを読み込んでソースコードにセット
        try {
            inputstream = assetmanager.open(strAssetName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (inputstream == null) {
            return null;
        }
        strbContent = new StringBuilder();
        try {
            iSize = inputstream.available();
            bFile = new byte[iSize + 1];
            //大きいサイズのデータを読んだ場合のバッファリング
            r = inputstream.read(bFile);
            while (r != -1) {//リード完了までバッファにアペンド処理繰り返す
                String st = new String(bFile);
                strbContent.append(st);
                iSize = inputstream.available();
                bFile = new byte[iSize + 1];
                r = inputstream.read(bFile);
            }
            inputstream.close();

            return strbContent.toString();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Asset operation FAILED", Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    //現在のファイルクラスのディレクトリ内容をリストビューに反映
    //呼び出し前にファイルクラスのディレクトリ設定済みであること
    public void app_V_refreshCWDlist() {
        int i, j;
        Bitmap bmp = null;
        tvFILEcwd.setText(fileclass.GetCwd());
        j = fileclass.GetFilesCurrentDir();
        // リストビューに表示する要素を設定
        ArrayList<ImageListClass> listItems = new ArrayList<>();
        if (j > 0) {
            //リストビューに取得したファイル一覧をセットする
            for (i = 0; i < fileclass.icwdfilecount; i++) {//現在パスのファイル・フォルダの数だけループ
                if (fileclass.filescwd[i].isFile()) {
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.file70px);  // ファイルアイコン
                } else if (fileclass.filescwd[i].isDirectory()) {
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.folder70pxb);  // フォルダアイコン
                }
                if (bmp == null) { //不明な種類のファイル
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.unknownfile70px);
                }
                ImageListClass item = new ImageListClass(bmp, fileclass.filescwd[i].getName());
                listItems.add(item);
            }

        } else {
            Log.d("files:", "no file");
        }
        // 出力結果をリストビューに表示
        ImageListClassAdapter adapter = new ImageListClassAdapter(this, R.layout.filelistcontent, listItems);
        lvFILEfiles.setAdapter(adapter);
        adapter.notifyDataSetChanged();//リストビューに更新を伝える必要あり
    }


    //ファイルメニュービューのセットアップ
    void app_V_refreshFileMenuView(int iMenuType) {
        tvFILEcwd = findViewById(R.id.tvFILEcwd);
        btnFILEcancel = findViewById(R.id.btnFILEcancel);//キャンセルボタン
        btnFILEcancel.setOnClickListener(this);
        btnFILEinternal = findViewById(R.id.btnFILEinternal);//内部ストレージボタン
        btnFILEinternal.setOnClickListener(this);
        btnFILEexternal = findViewById(R.id.btnFILEexternal);//外部ストレージボタン
        btnFILEexternal.setOnClickListener(this);
        btnFILEprevdir = findViewById(R.id.btnFILEprevdir);
        btnFILEprevdir.setOnClickListener(this);
        etFILEfilename = findViewById(R.id.etFILEfilename);//ファイル名エディットテキスト
        btnFILEopenselected = findViewById(R.id.btnFILEopenselected);
        btnFILEopenselected.setOnClickListener(this);
        lvFILEfiles = findViewById(R.id.lvFILEfiles);
        lvFILEfiles.setOnItemClickListener(this);

        switch (iMenuType) {
            case 0://SAVEモード
                btnFILEopenselected.setText(R.string.strUIsave);
                iDesiredFileViewMode = 0;
                break;
            case 1://LOADモード
                btnFILEopenselected.setText(R.string.strUIload);
                etFILEfilename.setEnabled(false);//書き込み不可にして既存のファイルだけ読ませる
                iDesiredFileViewMode = 1;
                break;
        }
        iWhereView = APP_CURRENTVIEW_FILE;
    }

    //エディタービューのセットアップ
    void app_V_refreshEditorView() {
        btnEDITORrun = findViewById(R.id.btnEDITORrunscript);
        btnEDITORrun.setOnClickListener(this);
        //SAVEボタンのリスナー実装
        btnEDITORsave = findViewById(R.id.btnEDITORsave);
        btnEDITORsave.setOnClickListener(this);
        //LOADボタンのリスナー実装
        btnEDITORload = findViewById(R.id.btnEDITORload);
        btnEDITORload.setOnClickListener(this);
        //HELPボタンのリスナー実装
        btnEDITORhelp = findViewById(R.id.btnEDITORhelp);
        btnEDITORhelp.setOnClickListener(this);
        etEDITORsrc = findViewById(R.id.etEDITORsrc);

        //メモリに入ってるソースコードをエディットテキストに書き戻す
        etEDITORsrc.setText(strSrc, TextView.BufferType.NORMAL);
        iWhereView = APP_CURRENTVIEW_EDIT;
    }


    //リストビューのアイテム取得
    public void onItemClick(AdapterView<?> parent,
                            View view, int pos, long id) {
        // 選択アイテムを取得
        ListView listView = (ListView) parent;

        ImageListClass icobj = (ImageListClass) listView.getItemAtPosition(pos);

        if (fileclass.GetIsDirectory(icobj.getTitle())) {//ディレクトリを選択した
            fileclass.SetCwdFromCurrent(icobj.getTitle());
            app_V_refreshCWDlist();
        } else {//ファイルを選択した
            etFILEfilename.setText(icobj.getTitle());
        }

        // 通知
        //Log.d( "AdapterView" , "OnItemClick");
    }

    //スクリプト実行ビューのセットアップ
    void app_V_refreshRunView(int iShowType) {

        btnRUNbackeditor = findViewById(R.id.btnRUNbackeditor);
        btnRUNbackeditor.setOnClickListener(this);

        //advRUNads = findViewById(R.id.advRUNads);
        //advRUNads.loadAd(areq);
        wvRUNapp = (WebView) findViewById(R.id.wvRUNapp);
        wvRUNapp.setWebViewClient(new WebViewClient() {
            //@Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            //@Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //TODO : スクリプトエラー時にここに到達しない問題を調べる
                Log.d("webview", "error");
                Toast.makeText(MainActivity.this, "JavaScript実行中にエラーが発生しました。", Toast.LENGTH_SHORT).show();
            }
        });

        wvRUNapp.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 1000);
            }

            //スクリプト実行中にコンソールへメッセージ来た場合の処理
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                //ToDo : コンソールメッセージの取得と処理
                iMessageLineNumber = consoleMessage.lineNumber();
                strMessage = consoleMessage.message();
                return true;
            }
        });
        wvRUNapp.getSettings().setJavaScriptEnabled(true);

        //エディットビューから取得したソースをWebViewに投入して実行させる
        //ここに到達するまでにstrSrcにソースコードを格納しておくこと
        iRunShow = iShowType;
        switch (iShowType) {
            case APP_RUNVIEW_SHOW_SCRIPT:
                if (strSrc != null) {
                    //実行するスクリプトのWebViewへの流し込み
                    wvRUNapp.loadData(strSrc, "text/html; charset=utf-8", "UTF-8");//UTF-8指定
                }
                break;
            case APP_RUNVIEW_SHOW_DOCUMENT:
                if (strIntDoc != null) {
                    //webViewに読み込み済みドキュメントをセットする
                    wvRUNapp.loadData(strIntDoc, "text/html; charset=utf-8", "UTF-8");
                }
                break;

        }
        iWhereView = APP_CURRENTVIEW_RUN;
    }
}