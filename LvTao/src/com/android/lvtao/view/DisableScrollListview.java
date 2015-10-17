package com.android.lvtao.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Jockio on 2015/10/16 0016.
 */
public class DisableScrollListview extends ListView {

    public DisableScrollListview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * ÉèÖÃ²»¹ö¶¯
     */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }

}
