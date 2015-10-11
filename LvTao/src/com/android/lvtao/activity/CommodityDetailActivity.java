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
        //����WebView���ԣ��ܹ�ִ��Javascript�ű�
        webView.getSettings().setJavaScriptEnabled(true);
        //������Ҫ��ʾ����ҳ
        webView.loadUrl("http://www.s2sing.com"+url);
        //����Web��ͼ
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(webView);
    }
}