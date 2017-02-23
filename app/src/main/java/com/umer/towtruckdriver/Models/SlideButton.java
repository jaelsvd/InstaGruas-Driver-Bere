package com.umer.towtruckdriver.Models;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.umer.towtruckdriver.interfaces.SlideButtonListener;

/**
 * Created by umer on 1/1/17.
 */

public class SlideButton extends SeekBar {

    private Drawable thumb;
    private SlideButtonListener listener;

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        this.thumb = thumb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thumb.getBounds().contains((int) event.getX(), (int) event.getY())) {
                super.onTouchEvent(event);
            } else
                return false;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getProgress() > 0)
                handleSlide(getProgress());

            setProgress(0);
        } else
            super.onTouchEvent(event);

        return true;
    }


    private void handleSlide(int slide) {
        listener.handleSlide(slide);
    }

    public void setSlideButtonListener(SlideButtonListener listener) {
        this.listener = listener;
    }

}

