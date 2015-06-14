package com.lastsoft.plog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.lastsoft.plog.calculation.VerticalScrollBoundsProvider;
import com.lastsoft.plog.calculation.position.VerticalScreenPositionCalculator;
import com.lastsoft.plog.calculation.progress.VerticalLinearLayoutManagerScrollProgressCalculator;
import com.lastsoft.plog.calculation.progress.VerticalScrollProgressCalculator;

import static android.support.v7.widget.RecyclerView.OnScrollListener;

public class FastScroller extends LinearLayout {
    private static final int HANDLE_HIDE_DELAY = 1000;
    private static final int HANDLE_ANIMATION_DURATION = 100;
    private static final int TRACK_SNAP_RANGE = 5;
    private static final String SCALE_X = "scaleX";
    private static final String SCALE_Y = "scaleY";
    private static final String ALPHA = "alpha";

    private View bubble;
    private View handle;

    private RecyclerView recyclerView;

    private SwipeRefreshLayout pullToRefresh;
    private final HandleHider handleHider = new HandleHider();
    private final ScrollListener scrollListener = new ScrollListener();
    private int height;

    private VerticalScrollProgressCalculator mScrollProgressCalculator;
    private VerticalScreenPositionCalculator mScreenPositionCalculator;

    private AnimatorSet currentAnimator = null;

    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context);
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context);
    }

    private void initialise(Context context) {
        setOrientation(HORIZONTAL);
        setClipChildren(false);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.fastscroller, this);
        bubble = findViewById(R.id.fastscroller_bubble);
        handle = findViewById(R.id.fastscroller_handle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
    }

    private void setUpCalculator(){
        VerticalScrollBoundsProvider boundsProvider =
                new VerticalScrollBoundsProvider(0, height - bubble.getHeight());
        mScrollProgressCalculator = new VerticalLinearLayoutManagerScrollProgressCalculator(boundsProvider);
        mScreenPositionCalculator = new VerticalScreenPositionCalculator(boundsProvider);
        // synchronize the handle position to the RecyclerView
        float scrollProgress = mScrollProgressCalculator.calculateScrollProgress(recyclerView);
        setPosition(scrollProgress);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed) {
            setUpCalculator();
        }

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            setPosition(event.getY());
            if (currentAnimator != null) {
                currentAnimator.cancel();
            }
            getHandler().removeCallbacks(handleHider);
            if (handle.getVisibility() == INVISIBLE) {
                //showHandle();
            }
            setRecyclerViewPosition(event.getY());
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            getHandler().postDelayed(handleHider, HANDLE_HIDE_DELAY);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setRecyclerView(RecyclerView recyclerView, SwipeRefreshLayout pullToRefresh) {
        this.recyclerView = recyclerView;
        this.pullToRefresh = pullToRefresh;
        recyclerView.setOnScrollListener(scrollListener);

    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            int itemCount = recyclerView.getAdapter().getItemCount();
            float proportion;
            if (bubble.getY() == 0) {
                proportion = 0f;
            } else if (bubble.getY() + bubble.getHeight() >= height - TRACK_SNAP_RANGE) {
                proportion = 1f;
            } else {
                proportion = y / (float) height;
            }
            int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            recyclerView.scrollToPosition(targetPos);
            if(pullToRefresh != null){
                try {
                    boolean enable = false;
                    boolean firstItemVisiblePull = targetPos == 0;
                    boolean topOfFirstItemVisiblePull = recyclerView.getChildAt(0).getTop() == 18;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisiblePull && topOfFirstItemVisiblePull;
                    //enable = firstItemVisiblePull;
                    pullToRefresh.setEnabled(enable);
                }catch (Exception ignored){}
            }
        }
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    public void setPosition(float y) {
        float position = y / height;
        int bubbleHeight = bubble.getHeight();
        bubble.setY(getValueInRange(0, height - bubbleHeight, (int) ((height - bubbleHeight) * position)));
        int handleHeight = handle.getHeight();
        handle.setY(getValueInRange(0, height - handleHeight, (int) ((height - handleHeight) * position)));

    }

    public void moveHandleToPosition(float scrollProgress) {
        if (mScreenPositionCalculator == null) {
            return;
        }
        //Log.d("V1", "scroll progress = " + scrollProgress);
        //Log.d("V1", "screen position calc = " + mScreenPositionCalculator.getYPositionFromScrollProgress(scrollProgress));
        bubble.setY(mScreenPositionCalculator.getYPositionFromScrollProgress(scrollProgress));
    }

    private void showHandle() {
        AnimatorSet animatorSet = new AnimatorSet();
        handle.setPivotX(handle.getWidth());
        handle.setPivotY(handle.getHeight());
        handle.setVisibility(VISIBLE);
        Animator growerX = ObjectAnimator.ofFloat(handle, SCALE_X, 0f, 1f).setDuration(HANDLE_ANIMATION_DURATION);
        Animator growerY = ObjectAnimator.ofFloat(handle, SCALE_Y, 0f, 1f).setDuration(HANDLE_ANIMATION_DURATION);
        Animator alpha = ObjectAnimator.ofFloat(handle, ALPHA, 0f, 1f).setDuration(HANDLE_ANIMATION_DURATION);
        animatorSet.playTogether(growerX, growerY, alpha);
        animatorSet.start();
    }

    private void hideHandle() {
        currentAnimator = new AnimatorSet();
        handle.setPivotX(handle.getWidth());
        handle.setPivotY(handle.getHeight());
        Animator shrinkerX = ObjectAnimator.ofFloat(handle, SCALE_X, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION);
        Animator shrinkerY = ObjectAnimator.ofFloat(handle, SCALE_Y, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION);
        Animator alpha = ObjectAnimator.ofFloat(handle, ALPHA, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION);
        currentAnimator.playTogether(shrinkerX, shrinkerY, alpha);
        currentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                handle.setVisibility(INVISIBLE);
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                handle.setVisibility(INVISIBLE);
                currentAnimator = null;
            }
        });
        currentAnimator.start();
    }

    private class HandleHider implements Runnable {
        @Override
        public void run() {
            hideHandle();
        }
    }

    private class ScrollListener extends OnScrollListener {
        @Override
        public void onScrolled(RecyclerView rv, int dx, int dy) {
            setUpCalculator();
            /*View firstVisibleView = recyclerView.getChildAt(0);
            int firstVisiblePosition = recyclerView.getChildPosition(firstVisibleView);
            int visibleRange = recyclerView.getChildCount();
            int lastVisiblePosition = firstVisiblePosition + visibleRange;
            int itemCount = recyclerView.getAdapter().getItemCount();
            Log.d("V1", "firstVisiblePositio = " + firstVisiblePosition+"");
            Log.d("V1", "visibleRange = " + visibleRange+"");
            Log.d("V1", "lastVisiblePosition = " + lastVisiblePosition+"");
            Log.d("V1", "itemCount = " + itemCount+"");


            int position;
            if (firstVisiblePosition == 0) {
                position = 0;
            //} else if (lastVisiblePosition == itemCount - 1) {
            //    position = itemCount - 1;
            } else {
                position = firstVisiblePosition;
            }
            float proportion = (float) position / (float) itemCount;

            Log.d("V1", "position  = " + position + "");
            Log.d("V1", "position being set = " + (height * proportion));

            setPosition(height * proportion);*/
            float scrollProgress = 0;
            if (mScrollProgressCalculator != null) {
                scrollProgress = mScrollProgressCalculator.calculateScrollProgress(recyclerView);

            }
            moveHandleToPosition(scrollProgress);

            if(pullToRefresh != null && recyclerView != null && recyclerView.getChildCount() > 0){
                try {
                    boolean enable = false;
                    boolean firstItemVisiblePull = recyclerView.getChildPosition(recyclerView.getChildAt(0)) == 0;
                    boolean topOfFirstItemVisiblePull = recyclerView.getChildAt(0).getTop() == 18;
                    enable = firstItemVisiblePull && topOfFirstItemVisiblePull;
                    pullToRefresh.setEnabled(enable);
                }catch (Exception ignored){}
            }

        }
    }
}