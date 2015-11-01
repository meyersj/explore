package com.meyersj.explore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.meyersj.explore.ExploreApplication;
import com.meyersj.explore.R;
import com.meyersj.explore.background.ScannerService;
import com.meyersj.explore.explore.ExploreFragment;
import com.meyersj.explore.map.LocationMapFragment;
import com.meyersj.explore.utilities.Cons;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getCanonicalName();
    private ExploreFragment exploreFragment;
    private LocationMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exploreFragment = ExploreFragment.newInstance(1);
        mapFragment = LocationMapFragment.newInstance(2);

        Intent intent = getIntent();
        if (intent.getBooleanExtra(Cons.NOTIFICATION, false)) {
            exploreFragment.setRestoreBundle(intent.getExtras());

        }

        //NewRelic.withApplicationToken(Utils.getNewRelicToken(getApplicationContext()))
        //        .withLogLevel(AgentLog.DEBUG)
        //        .start(this.getApplication());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.title_explore)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.title_map)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final NonSwipeableViewPager viewPager = (NonSwipeableViewPager) findViewById(R.id.pager);
        ExplorePagerAdapter adapter = new ExplorePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                switch(tab.getPosition()) {
                    case 0:
                        break;
                    case 1:
                        mapFragment.fetchBeaconLocations();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
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

    public class ExplorePagerAdapter extends FragmentPagerAdapter {

        public ExplorePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return exploreFragment;
                default:
                    return mapFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_explore).toUpperCase(l);
                case 1:
                    return getString(R.string.title_map).toUpperCase(l);
            }
            return null;
        }
    }
}