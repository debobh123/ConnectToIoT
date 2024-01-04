package com.sandvik.databearerdev.gui.main;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

// OnboardingAdapter.java
public class OnboardingAdapter extends FragmentStateAdapter {

    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OnboardingFragment();
            case 1:
                return new OnboardingFragment_1();
            case 2:
                return new OnboardingFragment_2();
            case 3:
                return new OnboardingFragment_3();
            case 4:
                return new OnboardingFragment_4();
            case 5:
                return new OnboardingFragment_5();
            case 6:
                return new OnboardingFragment_6();
            case 7:
                return new OnboardingFragment_7();
            case 8:
                return new OnboardingFragment_8();
            case 9:
                return new OnboardingFragment_9();
            default:
                return new OnboardingFragment(); // Default to the first fragment
        }
    }

    @Override
    public int getItemCount() {
        return 10; // Number of pages/slides
    }
}

