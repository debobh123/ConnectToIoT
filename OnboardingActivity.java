package com.sandvik.databearerdev.gui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.sandvik.databearerdev.R;
import com.sandvik.databearerdev.gui.main.OnboardingAdapter;

public class OnboardingActivity extends AppCompatActivity {

    private Button btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);


        ViewPager2 viewPager = findViewById(R.id.viewPager);
        OnboardingAdapter adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);
        btnSkip = findViewById(R.id.btnSkip);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });
    }

    private void startMainActivity() {
        // Start your main activity after skipping the onboarding
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Finish the OnboardingActivity
    }
}
