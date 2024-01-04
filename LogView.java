package com.sandvik.databearerdev.gui.log;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import com.sandvik.databearerdev.R;
import com.sandvik.databearerdev.gui.main.MainActivity;

/**
 * TODO: document your custom view class.
 */
public class LogView extends LinearLayout {

    public LogView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public LogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public LogView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);

    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LogView, defStyle, 0);

        a.recycle();

        View.inflate(context, R.layout.log_view, this);
        Toolbar bar = (Toolbar) findViewById(R.id.logViewToolbar);
        bar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.logViewFrame.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
