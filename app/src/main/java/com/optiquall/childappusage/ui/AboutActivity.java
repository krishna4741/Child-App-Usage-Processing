package com.optiquall.childappusage.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.optiquall.childappusage.R;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.running_apps);
        }


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(android.R.id.content, new ProcessListFragment()).commit();
        }

    }
}
