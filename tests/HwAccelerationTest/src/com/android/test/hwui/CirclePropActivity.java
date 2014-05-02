/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.test.hwui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Trace;
import android.view.HardwareCanvas;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class CirclePropActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        ProgressBar spinner = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        layout.addView(spinner, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        layout.addView(new CircleView(this),
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setContentView(layout);
    }

    static class CircleView extends View {
        static final int DURATION = 500;

        private boolean mToggle = false;
        ArrayList<RenderNodeAnimator> mRunningAnimations = new ArrayList<RenderNodeAnimator>();

        CanvasProperty<Float> mX;
        CanvasProperty<Float> mY;
        CanvasProperty<Float> mRadius;
        CanvasProperty<Paint> mPaint;

        CircleView(Context c) {
            super(c);
            setClickable(true);

            mX = CanvasProperty.createFloat(200.0f);
            mY = CanvasProperty.createFloat(200.0f);
            mRadius = CanvasProperty.createFloat(150.0f);

            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setColor(0xFFFF0000);
            p.setStyle(Style.STROKE);
            p.setStrokeWidth(60.0f);
            mPaint = CanvasProperty.createPaint(p);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (canvas.isHardwareAccelerated()) {
                HardwareCanvas hwcanvas = (HardwareCanvas) canvas;
                hwcanvas.drawCircle(mX, mY, mRadius, mPaint);
            }
        }

        @Override
        public boolean performClick() {
            for (int i = 0; i < mRunningAnimations.size(); i++) {
                mRunningAnimations.get(i).cancel();
            }
            mRunningAnimations.clear();

            mToggle = !mToggle;

            mRunningAnimations.add(new RenderNodeAnimator(
                    mX, RenderNodeAnimator.DELTA_TYPE_ABSOLUTE, mToggle ? 400.0f : 200.0f));

            mRunningAnimations.add(new RenderNodeAnimator(
                    mY, RenderNodeAnimator.DELTA_TYPE_ABSOLUTE, mToggle ? 600.0f : 200.0f));

            mRunningAnimations.add(new RenderNodeAnimator(
                    mRadius, RenderNodeAnimator.DELTA_TYPE_ABSOLUTE, mToggle ? 250.0f : 150.0f));

            mRunningAnimations.add(new RenderNodeAnimator(
                    mPaint, RenderNodeAnimator.PAINT_ALPHA,
                    RenderNodeAnimator.DELTA_TYPE_ABSOLUTE, mToggle ? 64.0f : 255.0f));

            mRunningAnimations.add(new RenderNodeAnimator(
                    mPaint, RenderNodeAnimator.PAINT_STROKE_WIDTH,
                    RenderNodeAnimator.DELTA_TYPE_ABSOLUTE, mToggle ? 5.0f : 60.0f));

            for (int i = 0; i < mRunningAnimations.size(); i++) {
                mRunningAnimations.get(i).start(this);
            }

            if (mToggle) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Trace.traceBegin(Trace.TRACE_TAG_VIEW, "pretendBusy");
                        try {
                            Thread.sleep(DURATION);
                        } catch (InterruptedException e) {
                        }
                        Trace.traceEnd(Trace.TRACE_TAG_VIEW);
                    }
                });
            }

            return true;
        }
    }
}
