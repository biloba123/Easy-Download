package com.lvqingyang.onepic.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.lvqingyang.easydownload.download.DownloadService;
import com.lvqingyang.onepic.R;
import com.lvqingyang.onepic.tool.ColorArt;
import com.lvqingyang.onepic.tool.HttpUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.lvqingyang.onepic.tool.AppConstants.drawableToBitmap;

public class DetailActivity extends AppCompatActivity implements View.OnTouchListener {

    //手指向右滑动时的最小速度
    private static final int XSPEED_MIN = 200;

    //手指向右滑动时的最小距离
    private static final int XDISTANCE_MIN = 150;
    @BindView(R.id.img)
    ImageView mImageView;
    @BindView(R.id.day)
    TextView mDayTv;
    @BindView(R.id.month)
    TextView mMonthTv;
    @BindView(R.id.titlt)
    TextView mTitleTv;
    @BindView(R.id.photoer)
    TextView mPhotoerTv;
    @BindView(R.id.content)
    TextView mContentTv;
    @BindView(R.id.fristLayout)
    CoordinatorLayout mCoordinatorLayout;
    private static final String TAG = "DetailActivity";
    private String mImgUrl,mTitle;

    //记录手指按下时的横坐标。
    private float xDown;

    //记录手指移动时的横坐标。
    private float xMove;

    //用于计算手指滑动的速度。
    private VelocityTracker mVelocityTracker;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    // 转载请说明出处：http://blog.csdn.net/ff20081528/article/details/17845753
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
                //活动的距离
                int distanceX = (int) (xMove - xDown);
                //获取顺时速度
                int xSpeed = getScrollVelocity();
                //当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity
                if (distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN) {
                    finish();
                }
                break;
            case MotionEvent.ACTION_UP:
                recycleVelocityTracker();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
     *
     * @param event
     */
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    /**
     * 获取手指在content界面滑动的速度。
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    private static final String KEY_URL = "URL";

    private DownloadService.DownloadBinder mDownloadBinder;

    private ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mDownloadBinder= (DownloadService.DownloadBinder) binder;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public static void start(Context context, String url) {
        Intent starter = new Intent(context, DetailActivity.class);
        starter.putExtra(KEY_URL, url);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        initeView();
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    String responce = HttpUtil.sendHttpRequest(getIntent().getStringExtra(KEY_URL));
                    subscriber.onNext(responce);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String responce) {
                        Log.d(TAG, "onNext: " + responce);
                        if (responce != null) {
                            Document doc = Jsoup.parse(responce);
                            Element detail = doc.getElementsByClass("detail_text_main").first();
                            Log.d(TAG, "onNext: " + detail.select("h2").text() + "\n"
                                    + detail.select("li.r_float").text() + "\n"
                                    + detail.select("img").attr("abs:data-cfsrc") + "\n"
                                    + detail.select("div.detail_text").select("div").get(1).text());
                            mImgUrl=detail.select("img").attr("abs:data-cfsrc");
                            Glide.with(DetailActivity.this)
                                    .load(mImgUrl)
                                    .into(new GlideDrawableImageViewTarget(mImageView) {
                                        @Override
                                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                            super.onResourceReady(resource, animation);
                                            ColorArt colorArt = new ColorArt(drawableToBitmap(mImageView.getDrawable()));
                                            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(mCoordinatorLayout, "backgroundColor"
                                                    , colorArt.getBackgroundColor());
                                            objectAnimator.setEvaluator(new ArgbEvaluator());
                                            ObjectAnimator objectAnimator1 = ObjectAnimator.ofInt(mTitleTv, "textColor"
                                                    , colorArt.getDetailColor());
                                            objectAnimator1.setEvaluator(new ArgbEvaluator());
                                            ObjectAnimator objectAnimator2 = ObjectAnimator.ofInt(mMonthTv, "textColor"
                                                    , colorArt.getPrimaryColor());
                                            objectAnimator2.setEvaluator(new ArgbEvaluator());
                                            ObjectAnimator objectAnimator3 = ObjectAnimator.ofInt(mDayTv, "textColor"
                                                    , colorArt.getSecondaryColor());
                                            objectAnimator3.setEvaluator(new ArgbEvaluator());
                                            AnimatorSet set = new AnimatorSet();
                                            set.play(objectAnimator)
                                                    .with(objectAnimator1)
                                                    .with(objectAnimator2)
                                                    .with(objectAnimator3);
                                            set.setDuration(1000);
                                            set.start();
                                        }
                                    });
                            mTitle= detail.select("h2").text().substring(5);
                            mTitleTv.setText(mTitle);
                            StringBuffer sb = new StringBuffer();
                            String date = sb.append(detail.select("li.r_float").text()).delete(0, 5).toString();
                            String[] ymd = date.split("-");
                            mMonthTv.setText(ymd[1]);
                            mDayTv.setText(ymd[2]);
                            mPhotoerTv.setText(detail.select("div.detail_text").select("div").last().text());
                            String content = detail.select("div.detail_text").select("div").text();
                            mContentTv.setText(content.substring(0, content.indexOf("摄影：")));
                        }
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(DetailActivity.this, getString(R.string.fail), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onError: " + e);
                    }
                });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        DownloadService.prepare(this, mConnection);
    }

    private void initeView() {
        mImageView = (AppCompatImageView) findViewById(R.id.img);
        mTitleTv = (TextView) findViewById(R.id.titlt);
        mMonthTv = (TextView) findViewById(R.id.month);
        mDayTv = (TextView) findViewById(R.id.day);
        mPhotoerTv = (TextView) findViewById(R.id.photoer);
        mContentTv = (TextView) findViewById(R.id.content);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.fristLayout);
        mCoordinatorLayout.setOnTouchListener(this);
        CardView cardView = (CardView) findViewById(R.id.td_header);
        cardView.setOnTouchListener(this);
//        mImageView.setOnTouchListener(this);
        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (ContextCompat.checkSelfPermission(DetailActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
                    mDownloadBinder.startDownload(mImgUrl);
                }else {
                    ActivityCompat.requestPermissions(DetailActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    mDownloadBinder.startDownload(mImgUrl);
                }else {
                    Toast.makeText(this, "拒绝权限无法下载", Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Detail Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();
        AppIndex.AppIndexApi.start(mClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mClient, getIndexApiAction());
        mClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService( mConnection);
    }
}
