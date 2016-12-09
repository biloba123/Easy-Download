package com.lvqingyang.onepic.beam;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lvqingyang.onepic.R;

import java.util.List;

/**
 * Author：LvQingYang
 * Date：2016/11/27
 * Email：biloba12345@gamil.com
 * God bless, never bug.
 */

public class CardsDataAdapter extends ArrayAdapter<Picture> {

    private int mResourceId;
    private static final String TAG = "CardsDataAdapter";

    public CardsDataAdapter(Context context, int resource, List<Picture> pictures) {
        super(context, resource,pictures);
        mResourceId=resource;
    }

    @Override
    public View getView(int position, final View contentView, ViewGroup parent){
        Log.d(TAG, "getView: "+position);
        Picture picture = getItem(position);
        ImageView iv= (ImageView) contentView.findViewById(R.id.img);
        Glide.with(getContext())
                .load(picture.getImgUrl())
                .into(iv);
        ((TextView)contentView.findViewById(R.id.title)).setText(picture.getTitle());
        ((TextView)contentView.findViewById(R.id.text)).setText(picture.getText());
        return contentView;
    }

}
