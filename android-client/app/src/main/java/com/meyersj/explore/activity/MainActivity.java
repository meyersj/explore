package com.meyersj.explore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.meyersj.explore.R;
import com.meyersj.explore.communicate.BLECommunicator;
import com.meyersj.explore.communicate.ResponseHandler;
import com.meyersj.explore.chat.ChatFragment;
import com.meyersj.explore.search.SearchFragment;
import com.meyersj.explore.search.NearbyBeacon;
import com.meyersj.explore.utilities.Cons;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getCanonicalName();
    private final Integer TAB_COUNT = 2;
    private SearchFragment searchFragment;
    private ChatFragment chatFragment;
    public NonSwipingViewPager viewPager;
    public BLECommunicator communicator;
    public NearbyBeacon selectedBeacon = null;
    private int backCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchFragment = SearchFragment.newInstance(1);
        chatFragment = ChatFragment.newInstance(2);

        ResponseHandler handler = new ResponseHandler(searchFragment, chatFragment);
        communicator = new BLECommunicator(this, handler);
        communicator.start();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.title_search)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.title_chat)));

        viewPager = (NonSwipingViewPager) findViewById(R.id.pager);
        ExplorePagerAdapter adapter = new ExplorePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                switch(tab.getPosition()) {
                    case 0:
                        // search
                        searchFragment.startScan();
                        chatFragment.leaveChannel();
                        selectedBeacon = null;
                        searchFragment.updateVisibility();
                        break;
                    case 1:
                        // chat
                        searchFragment.stopScan();
                        if (selectedBeacon != null) {
                            chatFragment.joinChannel(selectedBeacon);
                        }
                        chatFragment.updateVisibility();
                        backCounter = 0;
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        communicator.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        communicator.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(Cons.SETTINGS_ACTIVITY);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (++backCounter >= 2) {
            finish();
        }
        else {
            viewPager.setCurrentItem(0);
            backCounter++;
        }
    }

    public class ExplorePagerAdapter extends FragmentPagerAdapter {

        public ExplorePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return searchFragment;
                case 1:
                    return chatFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_search).toUpperCase(l);
                case 1:
                    return getString(R.string.title_chat).toUpperCase(l);
            }
            return null;
        }
    }

    // called by search fragment when registered beacon is selected
    public void startChat(NearbyBeacon beacon) {
        backCounter = 0;
        selectedBeacon = beacon;
        viewPager.setCurrentItem(1);
    }
}