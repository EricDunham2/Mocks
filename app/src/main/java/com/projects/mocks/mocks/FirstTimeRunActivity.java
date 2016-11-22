package com.projects.mocks.mocks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.projects.mocks.classes.ThreadLeaderboard;
import com.projects.mocks.fragments.BalanceIntroFragment;
import com.projects.mocks.fragments.FinalIntroFragment;
import com.projects.mocks.fragments.ScreenSlidePageFragment;

public class FirstTimeRunActivity extends FragmentActivity
{

    ViewPager mImageViewPager;
    PagerAdapter mPagerAdapter;
    private static final int NUM_PAGES =5;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_run);

        mImageViewPager = (ViewPager)findViewById(R.id.pager);
        mImageViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mImageViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mImageViewPager, true);

    }

    @Override
    public void onBackPressed() {
        if (mImageViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mImageViewPager.setCurrentItem(mImageViewPager.getCurrentItem() - 1);
        }
    }

    public void onClickGo(View view)
    {
        //will have to do stuff like name checking and stuff here before we set the "firstRun" preference to false;
        TextView tv = (TextView)findViewById(R.id.etName);
        TextView tvError = (TextView)findViewById(R.id.tvErrorFinalIntro);

        if(tv != null)
        {
            if(tv.getText().toString().equals(""))
            {
                tvError.setText("Please enter a name");
            }
            else
            {
                ThreadLeaderboard tl = new ThreadLeaderboard();
                tl.username =  tv.getText().toString(); //newUser.getText().toString();
                tl.method = "ADD";
                Thread t = new Thread(tl);
                t.start();
                try
                {
                    t.join();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                int response =  tl.responseCode;

                if(response == 0)
                    tvError.setText("Sorry, that user already exists.");
                else if(response == 2)
                    tvError.setText("Something went wrong. Please try again.");
                else if(response == 1)
                {
                    settings = getSharedPreferences("settings", CONTEXT_RESTRICTED);
                    editor = settings.edit();
                    editor.putBoolean("firstRun", false);
                    editor.commit();

                    editor.putString("username", tv.getText().toString());

                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                }
            }
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
    {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            if(position < 3)
                return new ScreenSlidePageFragment();
            else if(position == 3)
                return new BalanceIntroFragment();
            return new FinalIntroFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
