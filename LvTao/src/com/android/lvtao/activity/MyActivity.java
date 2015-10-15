package com.android.lvtao.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.android.lvtao.R;
import com.android.lvtao.fragment.FragmentHome;
import com.android.lvtao.fragment.FragmentMessage;
import com.android.lvtao.fragment.FragmentPersonal;
import com.android.lvtao.fragment.FragmentRent;

public class MyActivity extends FragmentActivity {

    private RadioGroup myTabRg;

    private LinearLayout moreButton;

    private String mCurrentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if(savedInstanceState==null) {
            try {
                mCurrentFragment = FragmentHome.class.getName();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.main_content, new FragmentHome(), mCurrentFragment)
                        .commit();
            } catch (Exception e) {

            }
        }
        initView();
    }

    public void initView() {
        moreButton= (LinearLayout) findViewById(R.id.more_Button);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"center button clicked",Toast.LENGTH_SHORT).show();
            }
        });

        myTabRg = (RadioGroup) findViewById(R.id.tab_menu);
        myTabRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Class<?> fClass=null;
                switch (checkedId) {
                    case R.id.rbHome:
                        fClass = FragmentHome.class;
                        break;
                    case R.id.rbRent:
                        fClass = FragmentRent.class;
                        break;
                    case R.id.rbMessage:
                        fClass = FragmentMessage.class;
                        break;
                    case R.id.rbPersonal:
                        fClass = FragmentPersonal.class;
                        break;
                    default:
                        break;
                }

                if(fClass!=null) {
                    String fTag = fClass.getName();

                    FragmentManager fManager = getSupportFragmentManager();
                    FragmentTransaction fTransaction = fManager.beginTransaction();

                    Fragment oldFragment = fManager.findFragmentByTag(mCurrentFragment);
                    Fragment newFragment = fManager.findFragmentByTag(fTag);

                    if (newFragment == null || !newFragment.isAdded()) {
                        try {
                            newFragment = (Fragment) fClass.newInstance();
                            fTransaction.add(R.id.main_content, newFragment, fTag);
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException ex) {
                            ex.printStackTrace();
                        }
                    }
                    mCurrentFragment = fTag;
                    fTransaction.hide(oldFragment).show(newFragment).commit();
                }
            }
        });
    }
}
