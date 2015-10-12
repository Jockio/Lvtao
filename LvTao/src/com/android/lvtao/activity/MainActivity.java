package com.android.lvtao.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.android.lvtao.R;
import com.android.lvtao.model.Ad;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import me.widget.pulltorefresh.library.PullToRefreshBase;
import me.widget.pulltorefresh.library.PullToRefreshScrollView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jockio on 2015/10/8 0008.
 */
public class MainActivity extends Activity implements PullToRefreshBase.OnRefreshListener<ScrollView>{

    public static String IMAGE_CACHE_PATH="Lvtao/Cache";//图片缓存路径
    private ViewPager adViewPager;
    private List<ImageView> imageViews;// 滑动的图片集合

    private PullToRefreshScrollView mPullScrollView;
    private ScrollView mScrollView;

    // 定时任务
    private ScheduledExecutorService mScheduledService;

    // 异步加载图片
    private ImageLoader mImageLoader;
    private DisplayImageOptions options;

    // 轮播banner的数据
    private List<Ad> adList;

    //小圆点
    private ImageView[] dots;

    private int currentItem=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        init();//初始化控件

        initImageLoader();//初始化ImageLoader

        //获取图片加载实例
        mImageLoader=ImageLoader.getInstance();
        options=new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.banner_empty)//网络图片加载完成前显示的图片
                .showImageForEmptyUri(R.drawable.banner_empty)
                .showImageOnFail(R.drawable.banner_empty)
                        //.cacheInMemory(true)//优化oom 该句可去掉
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)//是否考虑JPEG图像EXIF参数（旋转，翻转）
                .build();

        initData();//初始化数据
    }

    public void init(){
        Button btnSearch= (Button) findViewById(R.id.searchButton);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    //初始化数据
    public void initData(){
        adList=getAdList();
        imageViews=new ArrayList<ImageView>();

        adViewPager= (ViewPager) findViewById(R.id.viewpager);
        adViewPager.setAdapter(new MyAdapter());
        adViewPager.setOnPageChangeListener(new MyOnPageChangeListener());

        mPullScrollView= (PullToRefreshScrollView) findViewById(R.id.pullToRefresh);
        mPullScrollView.setOnRefreshListener(this);
        mScrollView = mPullScrollView.getRefreshableView();

        //动态添加图片及小圆点
        LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        dots = new ImageView[adList.size()];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.rightMargin = 10;
            dots[i].setLayoutParams(params);
            if(i!=0){
                dots[i].setBackgroundResource(R.drawable.welcome_dot);
            }else {
                dots[i].setBackgroundResource(R.drawable.dot_focused);
            }
            // 设置为灰色
            dots[i].setEnabled(true);
            mLinearLayout.addView(dots[i]);

            //异步加载图片
            ImageView imageView=new ImageView(this);
            mImageLoader.displayImage(adList.get(i).getImageUrl(),imageView,options);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews.add(imageView);
        }
    }

    @Override
    public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
        new PullDownRefresh().execute();
    }

    private class PullDownRefresh extends AsyncTask<Void, Void, String[]> {

        @Override
        protected void onPostExecute(String[] result) {
            mPullScrollView.onRefreshComplete();
            super.onPostExecute(result);
        }

        @Override
        protected String[] doInBackground(Void... arg0) {
            try {
                new Thread().sleep(2000);
            }catch(Exception e){
                Toast.makeText(getApplication(), "刷新失败", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    private Handler handler=new Handler() {
        public void handleMessage(Message msg){
            //切换到下一张，带动画效果
            adViewPager.setCurrentItem(currentItem, true);
        }
    };

    public void initImageLoader(){
        File cacheDir= StorageUtils.getOwnCacheDirectory(
                getApplicationContext(), IMAGE_CACHE_PATH);
        DisplayImageOptions defaultOptions=new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.item1)//网络图片加载完成前显示的图片
                .showImageForEmptyUri(R.drawable.item1)
                .showImageOnFail(R.drawable.item1)
                        //.cacheInMemory(true)//优化oom异常 该句可去掉
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config=new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions)
                //.memoryCache(new LruMemoryCache(12 * 1024 * 1024))
                //.memoryCacheSize(12 * 1024 * 1024)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .diskCacheSize(32 * 1024 * 1024).diskCacheFileCount(100)
                .threadPriority(Thread.NORM_PRIORITY)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config);
    }



    private class ScrollTask implements Runnable{

        @Override
        public void run() {
            synchronized (MainActivity.class){
                currentItem = (currentItem + 1) % imageViews.size();
                handler.obtainMessage().sendToTarget();
            }
        }
    }

    private class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return adList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view.equals(obj);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView=imageViews.get(position);
            container.addView(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //图片点击事件
                }
            });
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public void notifyDataSetChanged() {
        }

        @Override
        public void finishUpdate(ViewGroup container) {
        }

        @Override
        public void startUpdate(ViewGroup container) {
        }
    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener{

        private int oldPosition=0;

        /*
        当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。其中三个参数的含义分别为：
        arg0 :当前页面，及你点击滑动的页面
        arg1:当前页面偏移的百分比
        arg2:当前页面偏移的像素位置
         */
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        //页面跳转完成后调用
        @Override
        public void onPageSelected(int position) {
            /*int n=adList.size();
            dots[(position+n-1)%n].setBackgroundResource(R.drawable.dot_unfocused);
            dots[(position)%n].setBackgroundResource(R.drawable.dot_focused);
            dots[(position+1)%n].setBackgroundResource(R.drawable.dot_unfocused);*/

            currentItem=position;
            dots[oldPosition].setBackgroundResource(R.drawable.dot_unfocused);
            dots[position].setBackgroundResource(R.drawable.dot_focused);
            oldPosition=position;
        }

        //状态改变时调用
        @Override
        public void onPageScrollStateChanged(int arg) {
            //arg 0：默认什么都没做 1：正在滑动 2：滑动完毕
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScheduledService = Executors.newSingleThreadScheduledExecutor();
        // 当Activity显示出来后，每两秒切换一次图片显示
        // command：执行线程 initialDelay：初始化延时 period：两次开始执行最小间隔时间 unit：计时单位
        mScheduledService.scheduleAtFixedRate(new ScrollTask(), 2, 3,
                TimeUnit.SECONDS);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mScheduledService != null) {
            mScheduledService.shutdown();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 当Activity不可见的时候停止切换
        if(mScheduledService != null) {
            mScheduledService.shutdown();
        }
    }

    //获取广告信息
    public List<Ad> getAdList(){
        List<Ad> list=new ArrayList<Ad>();

        Ad ad=new Ad();
        ad.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/412.jpg");
        ad.setTargetUrl("");
        ad.setTitle("");
        list.add(ad);

        Ad ad1=new Ad();
        ad1.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/413.jpg");
        ad1.setTargetUrl("");
        ad1.setTitle("");
        list.add(ad1);

        Ad ad2=new Ad();
        ad2.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/414.jpg");
        ad2.setTargetUrl("");
        ad2.setTitle("");
        list.add(ad2);

        Ad ad3=new Ad();
        ad3.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/415.jpg");
        ad3.setTargetUrl("");
        ad3.setTitle("");
        list.add(ad3);

        Ad ad4=new Ad();
        ad4.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/416.jpg");
        ad4.setTargetUrl("");
        ad4.setTitle("");
        list.add(ad4);

        Ad ad5=new Ad();
        ad5.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/417.jpg");
        ad5.setTargetUrl("");
        ad5.setTitle("");
        list.add(ad5);

        return list;
    }

}