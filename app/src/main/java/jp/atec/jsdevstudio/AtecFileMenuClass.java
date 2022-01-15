package jp.atec.jsdevstudio;

import android.content.Context;
import android.os.Environment;
import android.service.autofill.FillEventHistory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Created by user on 2017/11/28.
 * 2018/2/12 readline関数で読み込みしたテキストの行末改行コードが反映されない問題を修正
 */

//TODO ファイルが存在するものか確認する関数を実装
public class AtecFileMenuClass {
    private File filedir;//ディレクトリを格納するクラス
    public File[] filescwd;//取得した現在ディレクトリのファイル一覧
    public int icwdfilecount;
    public File fileselected;

    //デフォルトディレクトリは内部ストレージのアプリケーションデータディレクトリとする
    AtecFileMenuClass( Context context ){
        fileselected = null;
        //SetCwdRoot();
        SetInternalStorageDirectory( context );
    }

    boolean IsExternalStorageMounted() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //現在ディレクトリに指定のファイル名で新規にファイルを書き込む
    //この関数は新規に作成したファイルのみ書き込みを行う
    //新規作成したファイルに書き込んだ場合はtrueを返す
    //そうでない場合はfalseを返す
    boolean NewSaveFileToCWD( String strFileName , String strContent ) throws IOException {
        String strPath;
        FileWriter fWriter;
        strPath = filedir.getPath();
        strPath = strPath + "/" + strFileName;
        fileselected = new File(strPath);
        if( fileselected.createNewFile() == true ) {//新規作成の場合の処理
            fWriter = new FileWriter(fileselected);
            fWriter.write(strContent);
            fWriter.flush();
            fWriter.close();
            return true;
        }
        return false;
    }

    //現在パスの既存のファイルへ上書き保存
    boolean OverWriteFileToCWD( String strFileName , String strContent ) throws IOException {
        String strPath;
        FileWriter fWriter;
        //BufferedWriter bWriter;
        PrintWriter pWriter;
        strPath = filedir.getPath();
        strPath = strPath + "/" + strFileName;
        fWriter = new FileWriter( strPath );
        if( fWriter == null )
            return false;
        //bWriter = new BufferedWriter( fWriter );
        //if( bWriter == null )
        //    return false;
        fWriter.write( strContent );
        //bWriter.write(strContent );
        //bWriter.flush();
        //bWriter.close();
        fWriter.flush();
        fWriter.close();
        return true;
    }

    String LoadFileFromCWD( String strFileName ) throws FileNotFoundException {
        String strpath;
        String strLine = null;
        StringBuilder strbContent;
        FileReader freader;
        BufferedReader breader;

        strpath = filedir.getPath();
        strpath = strpath + "/" + strFileName;

        fileselected = new File( strpath );
        freader = new FileReader(fileselected);
        breader = new BufferedReader( freader );
        strbContent = new StringBuilder();
        try {
            strLine = breader.readLine();//最初の1行読む
        } catch (IOException e) {
            e.printStackTrace();
        }
        while( strLine != null ) {//空のファイルでなかったら終了までバッファリング
            strbContent.append( strLine );//読み込んだ内容をバッファに結合
            strbContent.append("\n");//2018/2/12 改行コード省かれる対策
            try {
                strLine = breader.readLine();//次の行読み込み
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            breader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            freader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strbContent.toString();
    }

    void SetCwdRoot(){
        filedir = Environment.getRootDirectory();
    }

    //内部ストレージのアプリケーション固有ディレクトリのパスに現在ディレクトリを変更する
    void SetInternalStorageDirectory( Context context ) {
        filedir = context.getFilesDir(); //内部ストレージのアプリケーション固有のデータディレクトリを返すようにする Androidトレーニングガイド準拠の動作
    }

    //現在のディレクトリを取得
    String GetCwd(){
        return filedir.getPath();
    }

    //現在ディレクトリを絶対パスで返す
    String GetAbsoluteCwd() {
        return filedir.getAbsolutePath();
    }

    //絶対ディレクトリで現在パスを変更
    void SetCWDAbsolutePath( String strAbsolutePath ){
        filedir = new File( strAbsolutePath );
    }

    //現在のディレクトリの子ディレクトリへ移動
    //成功したらtrue 失敗したらfalseを返す
    //失敗の場合は移動前のディレクトリに現在ディレクトリ位置を戻す
    boolean SetCwdFromCurrent( String  strChildDir) {
        String strtmp;
        String strfallback;

        strtmp = this.GetCwd();
        strfallback = String.copyValueOf( strtmp.toCharArray() );
        strtmp = strtmp + "/" + strChildDir;
        filedir = new File( strtmp );
        if( filedir == null ) {
            filedir = new File( strfallback );
            return false;
        } else {
            return true;
        }
    }

    //現在のディレクトリの一つ上の階層へ移動
    //成功したらtrue 失敗したらfalse
    //失敗の場合は移動前のディレクトリに現在のディレクトリ位置を戻す
    boolean SetCwdToParent() {
        String strtmp;
        String strfallaback;
        strfallaback = this.GetCwd();
        strtmp = filedir.getParent();

        if( strtmp == null ){
            filedir = new File( strfallaback );
            return false;
        }

        filedir = new File( strtmp );
        if( filedir == null ){
            filedir = new File( strfallaback );
            return false;
        } else {
            return true;
        }
    }

    //ディレクトリと文字列で絶対パス生成してディレクトリかどうか返す
    boolean GetIsDirectory( String strSelectedPath ) {
        String strTmp;
        //String StrTmpTarg;
        File fileTmp;

        strTmp =this.GetCwd();
        strTmp = strTmp + "/" + strSelectedPath;

        fileTmp = new File( strTmp );
        if( fileTmp == null) {
            return false;
        }
        if( fileTmp.isDirectory() == true ) {
            return true ;
        }
        return false;
    }

    //現在ディレクトリから指定のファイルを開く
    //成功ならtrue 失敗ならfalse を返す
    boolean OpenFile( String strfileName ){
        String  strpath;
        strpath = filedir.getAbsolutePath();
        strpath = strpath + "/" + strfileName;
        fileselected = new File(strpath);
        if( fileselected == null ) {
            return false;
        }
        return true;
    }

    int GetFilesCurrentDir() {

        filescwd = filedir.listFiles();
        if( filescwd == null ) {
            return 0;
        }
        icwdfilecount = filescwd.length;
        return icwdfilecount;
    }

    //現在ディレクトリを外部ストレージのパブリックドキュメントディレクトリに変更
    void SetExternalStorageDirectory( Context context ) {
        //filedir = Environment.getExternalStorageDirectory();
        filedir = context.getExternalFilesDir( Environment.DIRECTORY_DOCUMENTS );
    }

    void SetExternalStoragePublicDirectoryOlderThanKitkKat( ) {
        String strpath;
        File f;
        f = Environment.getExternalStorageDirectory();
        strpath = f.getAbsolutePath();
        filedir = new File( strpath + "/Documents" );
    }
}
