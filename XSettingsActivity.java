package com.sandvik.databearerdev.gui.settings;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.sandvik.databearerdev.R;

public class XSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xsettings);
        setSupportActionBar(findViewById(R.id.settingsToolbar));
        Toolbar bar = findViewById(R.id.settingsToolbar);

        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new SettingsFragment());
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }



}
