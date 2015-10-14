package com.android.lvtao.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.*;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
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

    public static String IMAGE_CACHE_PATH="Lvtao/Cache";//ͼƬ����·��
    private ViewPager adViewPager;
    private List<ImageView> imageViews;// ������ͼƬ����

    private PullToRefreshScrollView mPullScrollView;
    private ScrollView mScrollView;

    // ��ʱ����
    private ScheduledExecutorService mScheduledService;

    // �첽����ͼƬ
    private ImageLoader mImageLoader;
    private DisplayImageOptions options;

    // �ֲ�banner������
    private List<Ad> adList;

    //СԲ��
    private ImageView[] dots;

    private boolean runTaskFlag = true;

    private int previousPage=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        init();//��ʼ���ؼ�

        initImageLoader();//��ʼ��ImageLoader

        //��ȡͼƬ����ʵ��
        mImageLoader=ImageLoader.getInstance();
        options=new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.banner_empty)//����ͼƬ�������ǰ��ʾ��ͼƬ
                .showImageForEmptyUri(R.drawable.banner_empty)
                .showImageOnFail(R.drawable.banner_empty)
                        //.cacheInMemory(true)//�Ż�oom �þ��ȥ��
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)//�Ƿ���JPEGͼ��EXIF��������ת����ת��
                .build();

        initData();//��ʼ������
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

    //��ʼ������
    public void initData() {
        adList = getAdList();
        imageViews = new ArrayList<ImageView>();

        adViewPager = (ViewPager) findViewById(R.id.viewpager);
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

        mPullScrollView = (PullToRefreshScrollView) findViewById(R.id.pullToRefresh);
        mPullScrollView.setOnRefreshListener(this);
        mScrollView = mPullScrollView.getRefreshableView();

        //��̬���ͼƬ��СԲ��
        LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        dots = new ImageView[adList.size()];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.rightMargin = 10;
            dots[i].setLayoutParams(params);
            if (i != 0) {
                dots[i].setBackgroundResource(R.drawable.welcome_dot);
            } else {
                dots[i].setBackgroundResource(R.drawable.dot_focused);
            }
            // ����Ϊ��ɫ
            dots[i].setEnabled(true);
            mLinearLayout.addView(dots[i]);

            //�첽����ͼƬ
            ImageView imageView = new ImageView(this);

            mImageLoader.displayImage(adList.get(i).getImageUrl(), imageView, options);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews.add(imageView);
        }

        // ��ʼ�����������ǰ����Integer.MAX_VALUE��һ��
        int index = (Integer.MAX_VALUE / 2) - (Integer.MAX_VALUE / 2 % adList.size());
        // ���õ�ǰѡ�е�Page���ᴥ��onPageChangListener.onPageSelected����
        adViewPager.setCurrentItem(index);
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
            } catch (Exception e) {
                Toast.makeText(getApplication(), "ˢ��ʧ��", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
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
                //�л�����һ�ţ�������Ч��
                adViewPager.setCurrentItem(pos + 1, true);
            }
        }
    };

    public void initImageLoader() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(
                getApplicationContext(), IMAGE_CACHE_PATH);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.item1)//����ͼƬ�������ǰ��ʾ��ͼƬ
                .showImageForEmptyUri(R.drawable.item1)
                .showImageOnFail(R.drawable.item1)
                        //.cacheInMemory(true)//�Ż�oom�쳣 �þ��ȥ��
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions)
                //.memoryCache(new LruMemoryCache(12 * 1024 * 1024))
                //.memoryCacheSize(12 * 1024 * 1024)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .diskCacheSize(32 * 1024 * 1024)
                .diskCacheFileCount(100)
                .threadPriority(Thread.NORM_PRIORITY)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config);
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
                    //ͼƬ����¼�
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
        ��ҳ���ڻ�����ʱ�����ô˷������ڻ�����ֹ֮ͣǰ���˷�����һֱ�õ����á��������������ĺ���ֱ�Ϊ��
        arg0 :��ǰҳ�棬������������ҳ��
        arg1:��ǰҳ��ƫ�Ƶİٷֱ�
        arg2:��ǰҳ��ƫ�Ƶ�����λ��
         */
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        //ҳ����ת��ɺ����
        @Override
        public void onPageSelected(int position) {
            /*int n=adList.size();
            dots[(position+n-1)%n].setBackgroundResource(R.drawable.dot_unfocused);
            dots[(position)%n].setBackgroundResource(R.drawable.dot_focused);
            dots[(position+1)%n].setBackgroundResource(R.drawable.dot_unfocused);*/

            // ��ȡ�µ�λ��
            int newPosition = position % adList.size();
            // ������һ�ε�״̬��
            dots[previousPage].setBackgroundResource(R.drawable.dot_unfocused);
            // ���õ�ǰ��״̬�㡰�㡱
            dots[newPosition].setBackgroundResource(R.drawable.dot_focused);

            // ��¼λ��
            previousPage = newPosition;

            /*currentItem = position;
            dots[oldPosition].setBackgroundResource(R.drawable.dot_unfocused);
            dots[position].setBackgroundResource(R.drawable.dot_focused);
            oldPosition = position;*/
        }

        //״̬�ı�ʱ����
        @Override
        public void onPageScrollStateChanged(int arg) {
            //arg 0��Ĭ��ʲô��û�� 1�����ڻ��� 2���������
        }
    }

    @Override
    protected void onResume() {
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
    protected void onStop() {
        super.onStop();
        runTaskFlag=false;
        if(mScheduledService != null) {
            mScheduledService.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        // activity����ʱ�򣬹ر�ѭ������
        runTaskFlag = false;
        if(mScheduledService != null) {
            mScheduledService.shutdown();
        }
        super.onDestroy();
    }

    //��ȡ�����Ϣ
    public List<Ad> getAdList() {
        List<Ad> list = new ArrayList<Ad>();

        Ad ad = new Ad();
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
        list.add(ad5);

        return list;
    }

}