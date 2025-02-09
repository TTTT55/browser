package com.studio.browser.misc;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.studio.browser.R;

public class PreferenceFrameLayout extends FrameLayout {
    private static final int DEFAULT_BORDER_TOP = 0;
    private static final int DEFAULT_BORDER_BOTTOM = 0;
    private static final int DEFAULT_BORDER_LEFT = 0;
    private static final int DEFAULT_BORDER_RIGHT = 0;
    private final int mBorderTop;
    private final int mBorderBottom;
    private final int mBorderLeft;
    private final int mBorderRight;
    private boolean mPaddingApplied;
    public PreferenceFrameLayout(Context context) {
        this(context, null);
    }
    public PreferenceFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0/*R.attr.preferenceFrameLayoutStyle*/);
    }
    public PreferenceFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public PreferenceFrameLayout(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray a = context.obtainStyledAttributes(attrs,
                null/*R.styleable.PreferenceFrameLayout*/, defStyleAttr, defStyleRes);
        float density = context.getResources().getDisplayMetrics().density;
        int defaultBorderTop = (int) (density * DEFAULT_BORDER_TOP + 0.5f);
        int defaultBottomPadding = (int) (density * DEFAULT_BORDER_BOTTOM + 0.5f);
        int defaultLeftPadding = (int) (density * DEFAULT_BORDER_LEFT + 0.5f);
        int defaultRightPadding = (int) (density * DEFAULT_BORDER_RIGHT + 0.5f);
        mBorderTop = a.getDimensionPixelSize(
                0/*R.styleable.PreferenceFrameLayout_borderTop*/,
                defaultBorderTop);
        mBorderBottom = a.getDimensionPixelSize(
                0/*R.styleable.PreferenceFrameLayout_borderBottom*/,
                defaultBottomPadding);
        mBorderLeft = a.getDimensionPixelSize(
                0/*R.styleable.PreferenceFrameLayout_borderLeft*/,
                defaultLeftPadding);
        mBorderRight = a.getDimensionPixelSize(
                0/*R.styleable.PreferenceFrameLayout_borderRight*/,
                defaultRightPadding);
        a.recycle();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }
    @Override
    public void addView(View child) {
        int borderTop = getPaddingTop();
        int borderBottom = getPaddingBottom();
        int borderLeft = getPaddingLeft();
        int borderRight = getPaddingRight();
        android.view.ViewGroup.LayoutParams params = child.getLayoutParams();
        LayoutParams layoutParams = params instanceof PreferenceFrameLayout.LayoutParams
                ? (PreferenceFrameLayout.LayoutParams) child.getLayoutParams() : null;
        // Check on the id of the child before adding it.
        if (layoutParams != null && layoutParams.removeBorders) {
            if (mPaddingApplied) {
                borderTop -= mBorderTop;
                borderBottom -= mBorderBottom;
                borderLeft -= mBorderLeft;
                borderRight -= mBorderRight;
                mPaddingApplied = false;
            }
        } else {
            // Add the padding to the view group after determining if the
            // padding already exists.
            if (!mPaddingApplied) {
                borderTop += mBorderTop;
                borderBottom += mBorderBottom;
                borderLeft += mBorderLeft;
                borderRight += mBorderRight;
                mPaddingApplied = true;
            }
        }
        int previousTop = getPaddingTop();
        int previousBottom = getPaddingBottom();
        int previousLeft = getPaddingLeft();
        int previousRight = getPaddingRight();
        if (previousTop != borderTop || previousBottom != borderBottom
                || previousLeft != borderLeft || previousRight != borderRight) {
            setPadding(borderLeft, borderTop, borderRight, borderBottom);
        }
        super.addView(child);
    }
    public static class LayoutParams extends FrameLayout.LayoutParams {
        public boolean removeBorders = false;
        /**
         * {@inheritDoc}
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs,
                    null/*R.styleable.PreferenceFrameLayout_Layout*/);
            removeBorders = a.getBoolean(
                    0/*R.styleable.PreferenceFrameLayout_Layout_layout_removeBorders*/,
                    false);
            a.recycle();
        }
        /**
         * {@inheritDoc}
         */
        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
