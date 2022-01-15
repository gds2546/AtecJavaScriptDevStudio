package jp.atec.jsdevstudio;

import android.graphics.Bitmap;

/**
 * Created by user on 2017/11/30.
 */

public class ImageListClass {
    private Bitmap  bpImage = null;
    private String  strText = null;

    public ImageListClass() {

    }
    /**
     * コンストラクタ
     * @param thumbnail サムネイル画像
     * @param title タイトル
     */
    public ImageListClass(Bitmap thumbnail, String title) {
        bpImage = thumbnail;
        strText = title;
    }

    /**
     * サムネイル画像を設定
     * @param thumbnail サムネイル画像
     */
    public void setThumbnail(Bitmap thumbnail) {
        bpImage = thumbnail;
    }

    /**
     * タイトルを設定
     * @param title タイトル
     */
    public void setmTitle(String title) {
        strText = title;
    }

    /**
     * サムネイル画像を取得
     * @return サムネイル画像
     */
    public Bitmap getThumbnail() {
        return bpImage;
    }

    /**
     * タイトルを取得
     * @return タイトル
     */
    public String getTitle() {
        return strText;
    }
}
