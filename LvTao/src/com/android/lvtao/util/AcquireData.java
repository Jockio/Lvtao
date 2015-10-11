package com.android.lvtao.util;

import android.text.TextUtils;
import android.util.Log;
import com.android.lvtao.model.Commodity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2015/9/22 0022.
 */
public class AcquireData {

    private Document document;

    public List<Commodity> getCommodityList(String keyWords){
        List<Commodity> commodityList=new ArrayList<Commodity>();
        Commodity commodity=null;
        try{
            String url="http://www.s2sing.com/Search/index.html?keywords="+keyWords;

            HttpURLConnection conn= (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");

            //解析html
            document = Jsoup.parse(new URL(url), 5000);

            //获取搜索结果
            Elements elements=document.getElementsByClass("cate-goods-det");

            //Log.i("AcquireData",elements.html());

            for(Element element:elements){
                commodity=new Commodity();

                //Log.i("666 AcquireData",element.html());

                Elements items=element.getElementsByTag("a");
                for(Element item:items){
                    commodity.setGoodsUrl(item.attr("href"));//商品url
                    Elements ws=item.getElementsByTag("img");
                    commodity.setImageUrl(ws.attr("src"));//图片url
                    break;
                }

                items=element.getElementsByTag("p");
                commodity.setName(items.text());//商品名称

                items=element.getElementsByClass("cate-price");
                String[] info=items.text().split(" ");
                commodity.setNewPrice(info[0]);//新价格
                commodity.setOldPrice(info[1]);//旧价格

                /*Log.i("name", commodity.getName());
                Log.i("newPrice", commodity.getNewPrice());
                Log.i("oldPrice", commodity.getOldPrice());
                Log.i("imageUrl", commodity.getImageUrl());
                Log.i("goodsUrl", commodity.getGoodsUrl());*/

                if(!TextUtils.isEmpty(commodity.getName()))
                    commodityList.add(commodity);
            }
        }catch(Exception e){
            Log.i("66666666666666666666", "Sorry, system error.");
        }
        return commodityList;
    }
}
