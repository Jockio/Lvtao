package com.android.lvtao.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.view.LayoutInflater;
import android.widget.*;
import com.android.lvtao.R;
import com.android.lvtao.activity.MainActivity;
import com.android.lvtao.activity.SearchActivity;
import com.android.lvtao.model.Ad;
import com.android.lvtao.view.DisableScrollListview;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import me.widget.pulltorefresh.library.PullToRefreshBase;
import me.widget.pulltorefresh.library.PullToRefreshScrollView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import static com.android.lvtao.R.drawable.ic_launcher;

/**
 * Created by john on 2015/10/14 0014.
 */
public class FragmentHome extends Fragment implements PullToRefreshBase.OnRefreshListener<ScrollView> {
    public static String IMAGE_CACHE_PATH = "Lvtao/Cache";//图片缓存路径
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

    private boolean runTaskFlag = true;

    private int previousPage = 0;

    private ListView schoolListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adList = getAdList();
        imageViews = new ArrayList<ImageView>();

        initImageLoader();//初始化ImageLoader

        //获取图片加载实例
        mImageLoader=ImageLoader.getInstance();
        options=new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.banner_empty)//网络图片加载完成前显示的图片
                .showImageForEmptyUri(R.drawable.banner_empty)
                .showImageOnFail(R.drawable.banner_empty)
                .cacheInMemory(true)//优化oom 该句可去掉
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)//是否考虑JPEG图像EXIF参数（旋转，翻转）
                .build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home, container, false);

        Button btnSearch = (Button) rootView.findViewById(R.id.searchButton);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        adViewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        adViewPager.setAdapter(new MyAdapter());
        adViewPager.setOnPageChangeListener(new MyOnPageChangeListener());

        adViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        runTaskFlag = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        runTaskFlag = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        runTaskFlag = true;
                        break;
                    default:
                        runTaskFlag = true;
                        break;
                }
                return false;
            }
        });

        mPullScrollView = (PullToRefreshScrollView) rootView.findViewById(R.id.pullToRefresh);
        mPullScrollView.setOnRefreshListener(this);
        mScrollView = mPullScrollView.getRefreshableView();

        //动态添加图片及小圆点
        LinearLayout mLinearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
        dots = new ImageView[adList.size()];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(getActivity().getApplicationContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.rightMargin = 10;
            dots[i].setLayoutParams(params);
            if (i != 0) {
                dots[i].setBackgroundResource(R.drawable.welcome_dot);
            } else {
                dots[i].setBackgroundResource(R.drawable.dot_focused);
            }
            // 设置为灰色
            dots[i].setEnabled(true);
            mLinearLayout.addView(dots[i]);

            //异步加载图片
            ImageView imageView = new ImageView(getActivity().getApplicationContext());

            mImageLoader.displayImage(adList.get(i).getImageUrl(), imageView, options);
            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);//拉伸图片填满控件
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews.add(imageView);
        }

        // 初始化广告条，当前索引Integer.MAX_VALUE的一半
        int index = (Integer.MAX_VALUE / 2) - (Integer.MAX_VALUE / 2 % adList.size());
        // 设置当前选中的Page，会触发onPageChangListener.onPageSelected方法
        adViewPager.setCurrentItem(index);

        schoolListView= (ListView) rootView.findViewById(R.id.listView);
        schoolListView.setAdapter(new MyListViewAdapter());
        /*schoolListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return true;
                }

                return false;
            }
        });*/
        return rootView;
    }

    public void initImageLoader() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(
                getActivity().getApplicationContext(), IMAGE_CACHE_PATH);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.item1)//网络图片加载完成前显示的图片
                .showImageForEmptyUri(R.drawable.item1)
                .showImageOnFail(R.drawable.item1)
                .cacheInMemory(true)//优化oom异常 该句可去掉
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getActivity().getApplicationContext()).defaultDisplayImageOptions(defaultOptions)
                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                .diskCacheExtraOptions(480, 800, null)
                .memoryCache(new LruMemoryCache(12 * 1024 * 1024))
                .memoryCacheSize(12 * 1024 * 1024)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .diskCacheSize(32 * 1024 * 1024)
                .diskCacheFileCount(100)
                .threadPriority(Thread.NORM_PRIORITY)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config);
    }

    private class ScrollTask implements Runnable {

        @Override
        public void run() {
            synchronized (MainActivity.class) {
                if (runTaskFlag) {
                    handler.obtainMessage().sendToTarget();
                }
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if(runTaskFlag) {
                int pos = adViewPager.getCurrentItem();
                //切换到下一张，带动画效果
                adViewPager.setCurrentItem(pos + 1, true);
            }
        }
    };

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
            } catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "刷新失败", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    private class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            //return adList.size();
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view.equals(obj);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = imageViews.get(position%adList.size());
            container.addView(imageView,0);
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
            container.removeView(imageViews.get(position%imageViews.size()));
        }
    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        private int oldPosition = 0;

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
            // 获取新的位置
            int newPosition = position % adList.size();
            // 消除上一次的状态点
            dots[previousPage].setBackgroundResource(R.drawable.dot_unfocused);
            // 设置当前的状态点“点”
            dots[newPosition].setBackgroundResource(R.drawable.dot_focused);
            // 记录位置
            previousPage = newPosition;
        }

        //状态改变时调用
        @Override
        public void onPageScrollStateChanged(int arg) {
            //arg 0：默认什么都没做 1：正在滑动 2：滑动完毕
        }
    }

    private class MyListViewAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;

            if(convertView==null){
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView=inflater.inflate(R.layout.school_item_list,null);
                holder=new ViewHolder();
                holder.titleTextView= (TextView) convertView.findViewById(R.id.schoolTitle_TextView);
                holder.dynamicTextView= (TextView) convertView.findViewById(R.id.schoolDynamic_TextView);
                holder.imageView1= (ImageView) convertView.findViewById(R.id.imageView1);
                holder.imageView2= (ImageView) convertView.findViewById(R.id.imageView2);
                holder.imageView3= (ImageView) convertView.findViewById(R.id.imageView3);
                convertView.setTag(holder);
            }else{
                holder= (ViewHolder) convertView.getTag();
            }
            return convertView;
        }

        public class ViewHolder{
            TextView titleTextView;
            TextView dynamicTextView;
            ImageView imageView1;
            ImageView imageView2;
            ImageView imageView3;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        runTaskFlag=true;
        mScheduledService = Executors.newSingleThreadScheduledExecutor();
        mScheduledService.scheduleWithFixedDelay(new ScrollTask(), 3, 2, TimeUnit.SECONDS);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mScheduledService != null) {
            mScheduledService.shutdown();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        runTaskFlag=false;
        if(mScheduledService != null) {
            mScheduledService.shutdown();
        }
    }

    @Override
    public void onDestroy() {
        // activity销毁时候，关闭循环播放
        runTaskFlag = false;
        if(mScheduledService != null) {
            mScheduledService.shutdown();
        }
        super.onDestroy();
    }

    //获取广告信息
    public List<Ad> getAdList() {
        List<Ad> list = new ArrayList<Ad>();

        /*Ad ad = new Ad();
        ad.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/325.jpg");
        ad.setTargetUrl("");
        ad.setTitle("");
        list.add(ad);

        Ad ad1 = new Ad();
        ad1.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/326.jpg");
        ad1.setTargetUrl("");
        ad1.setTitle("");
        list.add(ad1);

        Ad ad2 = new Ad();
        ad2.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/327.jpg");
        ad2.setTargetUrl("");
        ad2.setTitle("");
        list.add(ad2);

        Ad ad3 = new Ad();
        ad3.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/328.jpg");
        ad3.setTargetUrl("");
        ad3.setTitle("");
        list.add(ad3);

        Ad ad4 = new Ad();
        ad4.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/329.jpg");
        ad4.setTargetUrl("");
        ad4.setTitle("");
        list.add(ad4);

        Ad ad5 = new Ad();
        ad5.setImageUrl("https://ss2.bdstatic.com/lfoZeXSm1A5BphGlnYG/skin/330.jpg");
        ad5.setTargetUrl("");
        ad5.setTitle("");
        list.add(ad5);*/

        Ad ad6 = new Ad();
        ad6.setImageUrl("http://www.s2sing.com/Public/Home/Images/zlbanner.jpg");
        ad6.setTargetUrl("http://www.s2sing.com/Goods/category/cat_id/17/school_id/all/user_id/all/is_rent/1/is_market/0/plow/-1/phigh/-1/old_degree/-1.html");
        ad6.setTitle("");
        list.add(ad6);

        Ad ad7 = new Ad();
        ad7.setImageUrl("http://www.s2sing.com/Public/Home/Images/index_banner2.jpg");
        ad7.setTargetUrl("http://www.s2sing.com/Goods/category/cat_id/17.html");
        ad7.setTitle("");
        list.add(ad7);

        Ad ad8 = new Ad();
        ad8.setImageUrl("http://www.s2sing.com/Public/Home/Images/index_banner3.jpg");
        ad8.setTargetUrl("");
        ad8.setTitle("");
        list.add(ad8);

        Ad ad9 = new Ad();
        ad9.setImageUrl("http://www.s2sing.com/Public/Home/Images/index_banner4.jpg");
        ad9.setTargetUrl("");
        ad9.setTitle("");
        list.add(ad9);

        return list;
    }

    @Override
    public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
        new PullDownRefresh().execute();
    }
}
