package com.meyersj.explore.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.meyersj.explore.utilities.Cons;
import com.meyersj.explore.R;
import com.meyersj.explore.explore.ExploreFragment;
import com.meyersj.explore.register_client.RegisterClientFragment;
import com.meyersj.explore.register_beacon.RegisterBeaconFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private final String TAG = getClass().getCanonicalName();
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private String currentFragTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        String fragTag = "";
        Fragment newFrag = null;
        Fragment oldFrag;

        switch (position) {
            case 0:
                fragTag = "explore";
                newFrag = ExploreFragment.newInstance(position + 1);
                break;
            case 1:
                fragTag = "register_client";
                newFrag = RegisterClientFragment.newInstance(position + 1);
                break;
            case 2:
                fragTag = "register_beacon";
                newFrag = RegisterBeaconFragment.newInstance(position + 1);
                break;
        }

        if (!fragTag.isEmpty()) {
            if (currentFragTag != null) {
                fragmentManager.saveFragmentInstanceState(fragmentManager.findFragmentByTag(currentFragTag));
            }

            oldFrag = fragmentManager.findFragmentByTag(fragTag);
            if (oldFrag != null) {
                newFrag = oldFrag;
            }
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.addToBackStack(null);
            transaction.replace(R.id.container, newFrag, fragTag).commit();
            currentFragTag = fragTag;
        }
    }



    public void onSectionAttached(int number) {
        switch (number-1) {
            case 0:
                mTitle = getString(R.string.title_explore);
                break;
            case 1:
                mTitle = getString(R.string.title_register) + " Client";
                break;
            case 2:
                mTitle = getString(R.string.title_register) + " Beacon";
                break;

        }
        Log.d(TAG, "on section attached " + mTitle);
        //restoreActionBar();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(Cons.SETTINGS_ACTIVITY);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
