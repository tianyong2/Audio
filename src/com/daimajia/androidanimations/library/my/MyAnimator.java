package com.daimajia.androidanimations.library.my;

import android.content.Context;
import android.view.View;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.nineoldandroids.animation.ObjectAnimator;

public class MyAnimator extends BaseViewAnimator {

    @Override
    protected void prepare(View target) {
	// getAnimatorAgent().play(
	// ObjectAnimator.ofFloat(target, "translationY", 0F,
	// -px2dp(target.getContext(), 200), 0F));
	getAnimatorAgent().playTogether(
		ObjectAnimator.ofFloat(target, "translationY", 0F,
			-px2dp(target.getContext(), 400), 0F));

    }

    /**
     * px转dp
     * 
     * @param context
     * @param pxVal
     * @return
     */
    public float px2dp(Context context, float pxVal) {
	final float scale = context.getResources().getDisplayMetrics().density;
	return (pxVal / scale);
    }

}
