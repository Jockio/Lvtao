package com.android.lvtao.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.android.lvtao.R;
import com.android.lvtao.model.Commodity;
import com.android.lvtao.util.AcquireData;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class SearchActivity extends Activity {
    private ListView listView;
    private List<Commodity> commodityList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.search);

        init();//初始化控件

    }

    public void init(){
        listView= (ListView) findViewById(R.id.listView);

        final ImageView ivDeleteText = (ImageView) findViewById(R.id.ivDeleteText);
        final EditText etSearch = (EditText) findViewById(R.id.etSearch);
        Button btnSearch= (Button) findViewById(R.id.btnSearch);
        Button backButton= (Button) findViewById(R.id.backButton);

        ivDeleteText.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                etSearch.setText("");
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (s.length() == 0) {
                    ivDeleteText.setVisibility(View.GONE);
                } else {
                    ivDeleteText.setVisibility(View.VISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                if (s.length() == 0) {
                    ivDeleteText.setVisibility(View.GONE);
                } else {
                    ivDeleteText.setVisibility(View.VISIBLE);
                }

            }

            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    ivDeleteText.setVisibility(View.GONE);
                } else {
                    ivDeleteText.setVisibility(View.VISIBLE);
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //隐藏输入法
                InputMethodManager imm =(InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

                if(TextUtils.isEmpty(etSearch.getText().toString().trim())){
                    Toast.makeText(getApplicationContext(),"请输入要搜索的商品",Toast.LENGTH_SHORT).show();
                    return;
                }
                etSearch.setSelection(etSearch.getText().length());
                ivDeleteText.setVisibility(View.VISIBLE);
                new MyTask().execute(etSearch.getText().toString().trim());
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.this.finish();
            }
        });
    }

    class MyTask extends AsyncTask<String,Void,List<Commodity>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Commodity> doInBackground(String... params) {
            Log.i("doInBackGround",params[0]);

            return new AcquireData().getCommodityList(params[0]);
        }

        @Override
        protected void onPostExecute(List<Commodity> commodities) {
            commodityList=commodities;
            Log.i("hello",commodityList.size()+"");
            listView.setAdapter(new MyAdapter());
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(List<Commodity> commodities) {
            super.onCancelled(commodities);
        }
    }
    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return commodityList.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null){
                LayoutInflater inflater=getLayoutInflater();
                convertView=inflater.inflate(R.layout.item_list,null);

                holder=new ViewHolder();
                holder.imageView= (ImageView) convertView.findViewById(R.id.imageVIew);
                holder.nameTextView= (TextView) convertView.findViewById(R.id.name_TextView);
                holder.newPriceTextView= (TextView) convertView.findViewById(R.id.newPrice_TextView);
                holder.degreeTextView= (TextView) convertView.findViewById(R.id.degree_TextView);
                holder.availableTextView= (TextView) convertView.findViewById(R.id.available_TextView);
                convertView.setTag(holder);
            }else{
                holder= (ViewHolder) convertView.getTag();
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(SearchActivity.this, CommodityDetailActivity.class);
                    intent.putExtra("commodityUrl",commodityList.get(position).getGoodsUrl());
                    startActivity(intent);
                }
            });


            Commodity commodity=commodityList.get(position);

            Log.i("name", commodity.getName());
            Log.i("newPrice", commodity.getNewPrice());
            Log.i("oldPrice", commodity.getOldPrice());
            Log.i("imageUrl", commodity.getImageUrl());
            Log.i("goodsUrl", commodity.getGoodsUrl());

            holder.nameTextView.setText(commodity.getName());
            holder.newPriceTextView.setText(commodity.getNewPrice());
            holder.degreeTextView.setText(commodity.getOldPrice());
            holder.degreeTextView.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG );//文字上加中横线
            holder.availableTextView.setText("");

            holder.position = position;
            holder.url=commodity.getImageUrl();
            new ThumbnailTask(position, holder)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

            return convertView;
        }
    }

    class ThumbnailTask extends AsyncTask<String,Void,Bitmap> {
        private int mPosition;
        private ViewHolder mHolder;

        public ThumbnailTask(int position, ViewHolder holder) {
            mPosition = position;
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // Download bitmap here
            Bitmap bitmap=null;
            try{
                //String[] urlString=mHolder.url.split(".j");
                String previous=mHolder.url.substring(0, 22);
                String[] last=mHolder.url.substring(22,mHolder.url.length()).split("\\.");
                String lastString=last[0];
                String type=last[1];
                HttpURLConnection conn= (HttpURLConnection) new URL(
                        "http://www.s2sing.com"+previous+"thumb_"+lastString+"_180_180."+type).openConnection();
                bitmap= BitmapFactory.decodeStream(conn.getInputStream());
            }catch(Exception e){

            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mHolder.position == mPosition) {
                mHolder.imageView.setImageBitmap(bitmap);
            }
        }
    }

    class ViewHolder{
        ImageView imageView;
        TextView nameTextView;
        TextView newPriceTextView;
        TextView degreeTextView;
        TextView availableTextView;
        int position;
        String url;
    }
}
