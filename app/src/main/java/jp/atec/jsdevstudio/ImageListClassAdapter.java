package jp.atec.jsdevstudio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by user on 2017/11/30.
 */

public class ImageListClassAdapter extends ArrayAdapter<ImageListClass> {
    private int mResource;
    private List<ImageListClass> mItems;
    private LayoutInflater mInflater;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param resource リソースID
     * @param items リストビューの要素
     */
    public ImageListClassAdapter(Context context, int resource, List<ImageListClass> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            view = mInflater.inflate(mResource, null);
        }

        // リストビューに表示する要素を取得
        ImageListClass item = mItems.get(position);

        // サムネイル画像を設定
        ImageView thumbnail = view.findViewById(R.id.ivThumb);
        thumbnail.setImageBitmap(item.getThumbnail());

        // タイトルを設定
        TextView title = view.findViewById(R.id.tvText);
        title.setText(item.getTitle());

        return view;
    }
}
