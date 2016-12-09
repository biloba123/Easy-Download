package com.lvqingyang.onepic.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lvqingyang.onepic.R;
import com.lvqingyang.onepic.beam.CardsDataAdapter;
import com.lvqingyang.onepic.beam.Picture;
import com.lvqingyang.onepic.tool.AppConstants;
import com.lvqingyang.onepic.tool.ColorArt;
import com.lvqingyang.onepic.tool.HttpUtil;
import com.wenchao.cardstack.CardStack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.lvqingyang.onepic.tool.AppConstants.drawableToBitmap;

public class CardActivity extends AppCompatActivity {

    private CardStack mCardStack;
    private CardsDataAdapter mCardAdapter;
    private static final String TAG = "CardActivity";
    private List<Picture> mPictures=new ArrayList<>();
    private RelativeLayout mLayout;
    private  int mCurrentColor=Color.BLACK;
    private static int mPage=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        hideStatusBar();
        setContentView(R.layout.activity_card);

        initView();
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String > subscriber) {
                try {
                    String responce= HttpUtil.sendHttpRequest(AppConstants.add);
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
                    public void onNext(String  responce) {
                        Log.d(TAG, "onNext: "+responce);
                        if (responce != null) {
                            Document doc = Jsoup.parseBodyFragment(responce);
                            Element body = doc.body();
                            Elements pics=body.getElementsByClass("ajax_list");
                            for (Element pic : pics) {
                                Picture picture=new Picture();
                                picture.setImgUrl(pic.select("img").attr("abs:src"));
                                picture.setTitle(pic.select("a[href]").text());
                                picture.setText(pic.select("dd.ajax_dd_text").text());
                                picture.setDetailUrl("http://m.nationalgeographic.com.cn"+
                                        pic.getElementsByTag("dt").select("a").attr("href"));
                                mPictures.add(picture);
                                Log.d(TAG, "onNext: "+"http://m.nationalgeographic.com.cn"+pic.getElementsByTag("dt").select("a").attr("href"));
                            }
                        }
                    }

                    @Override
                    public void onCompleted() {
                        mCardAdapter = new CardsDataAdapter(CardActivity.this, R.layout.card_layout,mPictures);
                        mCardStack.setAdapter(mCardAdapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(mCardStack,getString(R.string.fail), Snackbar.LENGTH_SHORT).show();
                        Log.e(TAG, "onError: "+e );
                    }
                });
    }

    private void initView(){
        mCardStack = (CardStack)findViewById(R.id.container);
        mCardStack.setContentResource(R.layout.card_layout);
        mCardStack.setStackMargin(20);
        mLayout = (RelativeLayout) findViewById(R.id.activity_card);
        mLayout.setBackgroundColor(Color.DKGRAY);
        mCardStack.setListener(new CardStack.CardEventListener() {
            @Override
            public boolean swipeEnd(int i, float v) {
                return (v>300)? true : false;
            }

            @Override
            public boolean swipeStart(int i, float v) {
                return true;
            }

            @Override
            public boolean swipeContinue(int i, float v, float v1) {
                return true;
            }

            @Override
            public void discarded(int i, int i1) {
               setLayoutBg();
                int count=mPictures.size()-mCardStack.getCurrIndex();
                if (count<4){
                    Observable.create(new Observable.OnSubscribe<String>() {
                        @Override
                        public void call(Subscriber<? super String > subscriber) {
                            try {
                                String responce= HttpUtil.sendHttpRequest(AppConstants.add+(++mPage)+".html");
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
                                public void onNext(String  responce) {
                                    Log.d(TAG, "onNext: "+responce);
                                    if (responce != null) {
                                        Document doc = Jsoup.parseBodyFragment(responce);
                                        Element body = doc.body();
                                        Elements pics=body.getElementsByClass("ajax_list");
                                        for (Element pic : pics) {
                                            Picture picture=new Picture();
                                            picture.setImgUrl(pic.select("img").attr("abs:src"));
                                            picture.setTitle(pic.select("a[href]").text());
                                            picture.setText(pic.select("dd.ajax_dd_text").text());
                                            picture.setDetailUrl("http://m.nationalgeographic.com.cn"+
                                                    pic.getElementsByTag("dt").select("a").attr("href"));
                                            mPictures.add(picture);
                                            Log.d(TAG, "onNext: "+"http://m.nationalgeographic.com.cn"+pic.getElementsByTag("dt").select("a").attr("href"));
                                        }
                                    }
                                }

                                @Override
                                public void onCompleted() {
                                    mCardAdapter = new CardsDataAdapter(CardActivity.this, R.layout.card_layout,mPictures);
                                    mCardStack.setAdapter(mCardAdapter);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Snackbar.make(mCardStack,getString(R.string.fail), Snackbar.LENGTH_SHORT).show();
                                    Log.e(TAG, "onError: "+e );
                                }
                            });
                }
            }

            @Override
            public void topCardTapped() {
                DetailActivity.start(CardActivity.this, mPictures.get(mCardStack.getCurrIndex()).getDetailUrl());
            }
        });
    }

    private void setLayoutBg(){
        ImageView iv= (ImageView) mCardStack.getTopView().findViewById(R.id.img);
        ColorArt colorArt=new ColorArt(drawableToBitmap(iv.getDrawable()));
//        mLayout.setBackgroundColor(colorArt.getBackgroundColor());
//        ((TextView)mCardStack.getTopView().findViewById(R.id.title)).setTextColor(colorArt.getPrimaryColor());
        ObjectAnimator objectAnimator=ObjectAnimator.ofInt(mLayout,"backgroundColor",
               mCurrentColor,colorArt.getBackgroundColor())
                .setDuration(1000);
        objectAnimator.setEvaluator(new ArgbEvaluator());
        objectAnimator.start();
        mCurrentColor=colorArt.getBackgroundColor();
    }



}
