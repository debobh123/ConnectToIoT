package com.sandvik.databearerdev.gui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sandvik.databearerdev.R;

public class OnboardingFragment extends Fragment {
    private int pageNumber;

    public OnboardingFragment() {
        // Required empty public constructor
    }

    public static OnboardingFragment newInstance(int pageNumber) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt("pageNumber", pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pageNumber = getArguments().getInt("pageNumber");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);

        // Customize the UI based on the page number
        TextView textView = view.findViewById(R.id.textView);


        return view;
    }
}