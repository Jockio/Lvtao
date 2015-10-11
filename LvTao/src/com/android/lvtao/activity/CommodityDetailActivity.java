package com.android.lvtao.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

/**
 * Created by Jockio on 2015/9/24 0024.
 */
public class CommodityDetailActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        String url=getIntent().getStringExtra("commodityUrl");
        WebView webView=new WebView(this);

        webView = new WebView(this);
        //设置WebView属性，能够执行Javascript脚本
        webView.getSettings().setJavaScriptEnabled(true);
        //加载需要显示的网页
        webView.loadUrl("http://www.s2sing.com"+url);
        //设置Web视图
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(webView);
    }
}