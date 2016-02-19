package com.randomname.mrakopedia.ui.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import carbon.Carbon;
import carbon.animation.AnimUtils;
import carbon.animation.AnimatedView;
import carbon.animation.StateAnimator;
import carbon.animation.StateAnimatorView;
import carbon.drawable.EmptyDrawable;
import carbon.drawable.RippleDrawable;
import carbon.drawable.RippleDrawableCompat;
import carbon.drawable.RippleDrawableLollipop;
import carbon.drawable.RippleView;
import carbon.drawable.VectorDrawable;
import carbon.shadow.Shadow;
import carbon.shadow.ShadowGenerator;
import carbon.shadow.ShadowShape;
import carbon.shadow.ShadowView;
import carbon.widget.CornerView;
import carbon.widget.TintedView;
import carbon.widget.TouchMarginView;

/**
 * Created by vgrigoryev on 19.02.2016.
 */
public class RippleImageButton extends android.widget.ImageButton implements ShadowView, RippleView, TouchMarginView, StateAnimatorView, AnimatedView, CornerView, TintedView {
    Paint paint;
    private int cornerRadius;
    private Path cornersMask;
    private static PorterDuffXfermode pdMode;
    private RippleDrawable rippleDrawable;
    private EmptyDrawable emptyBackground;
    private Transformation t;
    private float elevation;
    private float translationZ;
    private Shadow shadow;
    private Rect touchMargin;
    private List<StateAnimator> stateAnimators;
    private AnimUtils.Style inAnim;
    private AnimUtils.Style outAnim;
    private Animator animator;
    ColorStateList tint;

    public RippleImageButton(Context context) {
        this(context, (AttributeSet)null);
    }

    public RippleImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, carbon.R.attr.carbon_imageViewStyle);
    }

    public RippleImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.paint = new Paint(3);
        this.emptyBackground = new EmptyDrawable();
        this.t = new Transformation();
        this.elevation = 0.0F;
        this.translationZ = 0.0F;
        this.stateAnimators = new ArrayList();
        this.init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = this.getContext().obtainStyledAttributes(attrs, carbon.R.styleable.ImageView, defStyleAttr, 0);
        this.setElevation(a.getDimension(carbon.R.styleable.ImageView_carbon_elevation, 0.0F));
        int resId = a.getResourceId(carbon.R.styleable.ImageView_android_src, 0);
        int resId2 = a.getResourceId(carbon.R.styleable.ImageView_carbon_src, 0);
        if(resId == 0) {
            resId = resId2;
        }

        if(resId != 0 && !this.isInEditMode() && this.getContext().getResources().getResourceTypeName(resId).equals("raw")) {
            this.setImageDrawable(new VectorDrawable(this.getResources(), resId));
        }

        this.setEnabled(a.getBoolean(carbon.R.styleable.ImageView_android_enabled, true));
        this.setCornerRadius((int)a.getDimension(carbon.R.styleable.ImageView_carbon_cornerRadius, 0.0F));
        initRippleDrawable(this);
        Carbon.initAnimations(this, attrs, defStyleAttr);
        Carbon.initTouchMargin(this, attrs, defStyleAttr);
        Carbon.initTint(this, attrs, defStyleAttr);
        a.recycle();
    }

    private void initRippleDrawable(RippleView rippleView) {
        View view = (View)rippleView;
        if(!view.isInEditMode()) {
            int color = getResources().getColor(android.support.v7.appcompat.R.color.ripple_material_dark);
            if(color != 0) {
                RippleDrawable.Style style = RippleDrawable.Style.Over;
                boolean useHotspot = true;
                Object rippleDrawable;
                if(Build.VERSION.SDK_INT >= 21) {
                    rippleDrawable = new RippleDrawableLollipop(color, style == RippleDrawable.Style.Background?view.getBackground():null, style);
                } else {
                    rippleDrawable = new RippleDrawableCompat(color, style == RippleDrawable.Style.Background?view.getBackground():null, view.getContext(), style);
                }

                ((RippleDrawable)rippleDrawable).setCallback(view);
                ((RippleDrawable)rippleDrawable).setHotspotEnabled(useHotspot);
                rippleView.setRippleDrawable((RippleDrawable)rippleDrawable);
            }
        }
    }

    public void setImageResource(int resId) {
        if(resId != 0 && this.getContext().getResources().getResourceTypeName(resId).equals("raw")) {
            this.setImageDrawable(new VectorDrawable(this.getResources(), resId));
        } else {
            super.setImageResource(resId);
        }

    }

    public int getCornerRadius() {
        return this.cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        this.invalidateShadow();
        this.initCorners();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(changed) {
            this.invalidateShadow();
            if(this.getWidth() != 0 && this.getHeight() != 0) {
                this.initCorners();
                if(this.rippleDrawable != null) {
                    this.rippleDrawable.setBounds(0, 0, this.getWidth(), this.getHeight());
                }

            }
        }
    }

    private void initCorners() {
        if(this.cornerRadius > 0) {
            if(Build.VERSION.SDK_INT >= 21) {
                this.setClipToOutline(true);
                this.setOutlineProvider(ShadowShape.viewOutlineProvider);
            } else {
                this.cornersMask = new Path();
                this.cornersMask.addRoundRect(new RectF(0.0F, 0.0F, (float)this.getWidth(), (float)this.getHeight()), (float)this.cornerRadius, (float)this.cornerRadius, Path.Direction.CW);
                this.cornersMask.setFillType(Path.FillType.INVERSE_WINDING);
            }
        } else if(Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT >= 21) {
            this.setOutlineProvider(ViewOutlineProvider.BOUNDS);
        }

    }

    public void draw(@NonNull Canvas canvas) {
        if(this.cornerRadius > 0 && this.getWidth() > 0 && this.getHeight() > 0 && Build.VERSION.SDK_INT <= 20) {
            int saveCount = canvas.saveLayer(0.0F, 0.0F, (float)this.getWidth(), (float)this.getHeight(), (Paint)null, Canvas.ALL_SAVE_FLAG);
            super.draw(canvas);
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Over) {
                this.rippleDrawable.draw(canvas);
            }

            this.paint.setXfermode(pdMode);
            canvas.drawPath(this.cornersMask, this.paint);
            canvas.restoreToCount(saveCount);
            this.paint.setXfermode((Xfermode)null);
        } else {
            super.draw(canvas);
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Over) {
                this.rippleDrawable.draw(canvas);
            }
        }

    }

    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        Animation a = this.getAnimation();
        if(a != null && a.hasStarted()) {
            a.getTransformation(this.getDrawingTime(), this.t);
            float[] loc = new float[]{event.getX(), event.getY()};
            loc[0] -= ViewHelper.getTranslationX(this);
            loc[1] -= ViewHelper.getTranslationY(this);
            event.setLocation(loc[0], loc[1]);
        }

        if(this.rippleDrawable != null && event.getAction() == 0) {
            this.rippleDrawable.setHotspot(event.getX(), event.getY());
        }

        return super.dispatchTouchEvent(event);
    }

    public RippleDrawable getRippleDrawable() {
        return this.rippleDrawable;
    }

    public void setRippleDrawable(RippleDrawable newRipple) {
        if(this.rippleDrawable != null) {
            this.rippleDrawable.setCallback((Drawable.Callback)null);
            if(this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Background) {
                super.setBackgroundDrawable((Drawable)(this.rippleDrawable.getBackground() == null?this.emptyBackground:this.rippleDrawable.getBackground()));
            }
        }

        if(newRipple != null) {
            newRipple.setCallback(this);
            if(newRipple.getStyle() == carbon.drawable.RippleDrawable.Style.Background) {
                super.setBackgroundDrawable((Drawable)newRipple);
            }
        }

        this.rippleDrawable = newRipple;
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || this.rippleDrawable == who;
    }

    public void invalidateDrawable(@NonNull Drawable drawable) {
        super.invalidateDrawable(drawable);
        if(this.getParent() != null && this.getParent() instanceof View) {
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Borderless) {
                ((View)this.getParent()).invalidate();
            }

            if(this.getElevation() > 0.0F || this.getCornerRadius() > 0) {
                ((View)this.getParent()).invalidate();
            }

        }
    }

    public void invalidate(@NonNull Rect dirty) {
        super.invalidate(dirty);
        if(this.getParent() != null && this.getParent() instanceof View) {
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Borderless) {
                ((View)this.getParent()).invalidate(dirty);
            }

            if(this.getElevation() > 0.0F || this.getCornerRadius() > 0) {
                ((View)this.getParent()).invalidate(dirty);
            }

        }
    }

    public void invalidate(int l, int t, int r, int b) {
        super.invalidate(l, t, r, b);
        if(this.getParent() != null && this.getParent() instanceof View) {
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Borderless) {
                ((View)this.getParent()).invalidate(l, t, r, b);
            }

            if(this.getElevation() > 0.0F || this.getCornerRadius() > 0) {
                ((View)this.getParent()).invalidate(l, t, r, b);
            }

        }
    }

    public void invalidate() {
        super.invalidate();
        if(this.getParent() != null && this.getParent() instanceof View) {
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Borderless) {
                ((View)this.getParent()).invalidate();
            }

            if(this.getElevation() > 0.0F || this.getCornerRadius() > 0) {
                ((View)this.getParent()).invalidate();
            }

        }
    }

    public void postInvalidateDelayed(long delayMilliseconds) {
        super.postInvalidateDelayed(delayMilliseconds);
        if(this.getParent() != null && this.getParent() instanceof View) {
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Borderless) {
                ((View)this.getParent()).postInvalidateDelayed(delayMilliseconds);
            }

            if(this.getElevation() > 0.0F || this.getCornerRadius() > 0) {
                ((View)this.getParent()).postInvalidateDelayed(delayMilliseconds);
            }

        }
    }

    public void postInvalidateDelayed(long delayMilliseconds, int left, int top, int right, int bottom) {
        super.postInvalidateDelayed(delayMilliseconds, left, top, right, bottom);
        if(this.getParent() != null && this.getParent() instanceof View) {
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Borderless) {
                ((View)this.getParent()).postInvalidateDelayed(delayMilliseconds, left, top, right, bottom);
            }

            if(this.getElevation() > 0.0F || this.getCornerRadius() > 0) {
                ((View)this.getParent()).postInvalidateDelayed(delayMilliseconds, left, top, right, bottom);
            }

        }
    }

    public void postInvalidate() {
        super.postInvalidate();
        if(this.getParent() != null && this.getParent() instanceof View) {
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Borderless) {
                ((View)this.getParent()).postInvalidate();
            }

            if(this.getElevation() > 0.0F || this.getCornerRadius() > 0) {
                ((View)this.getParent()).postInvalidate();
            }

        }
    }

    public void postInvalidate(int left, int top, int right, int bottom) {
        super.postInvalidate(left, top, right, bottom);
        if(this.getParent() != null && this.getParent() instanceof View) {
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Borderless) {
                ((View)this.getParent()).postInvalidate(left, top, right, bottom);
            }

            if(this.getElevation() > 0.0F || this.getCornerRadius() > 0) {
                ((View)this.getParent()).postInvalidate(left, top, right, bottom);
            }

        }
    }

    public void setBackground(Drawable background) {
        this.setBackgroundDrawable(background);
    }

    public void setBackgroundDrawable(Drawable background) {
        if(background instanceof RippleDrawable) {
            this.setRippleDrawable((RippleDrawable)background);
        } else {
            if(this.rippleDrawable != null && this.rippleDrawable.getStyle() == carbon.drawable.RippleDrawable.Style.Background) {
                this.rippleDrawable.setCallback((Drawable.Callback)null);
                this.rippleDrawable = null;
            }

            super.setBackgroundDrawable((Drawable)(background == null?this.emptyBackground:background));
        }
    }

    public float getElevation() {
        return this.elevation;
    }

    public synchronized void setElevation(float elevation) {
        if(elevation != this.elevation) {
            if(Build.VERSION.SDK_INT >= 21) {
                super.setElevation(elevation);
            }

            this.elevation = elevation;
            if(this.getParent() != null) {
                ((View)this.getParent()).postInvalidate();
            }

        }
    }

    public float getTranslationZ() {
        return this.translationZ;
    }

    public synchronized void setTranslationZ(float translationZ) {
        if(translationZ != this.translationZ) {
            if(Build.VERSION.SDK_INT >= 21) {
                super.setTranslationZ(translationZ);
            }

            this.translationZ = translationZ;
            if(this.getParent() != null) {
                ((View)this.getParent()).postInvalidate();
            }

        }
    }

    public ShadowShape getShadowShape() {
        return this.cornerRadius == this.getWidth() / 2 && this.getWidth() == this.getHeight()?ShadowShape.CIRCLE:(this.cornerRadius > 0?ShadowShape.ROUND_RECT:ShadowShape.RECT);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.setTranslationZ(enabled?0.0F:-this.elevation);
    }

    public Shadow getShadow() {
        float elevation = this.getElevation() + this.getTranslationZ();
        if(elevation >= 0.01F && this.getWidth() > 0 && this.getHeight() > 0) {
            if(this.shadow == null || this.shadow.elevation != elevation) {
                this.shadow = ShadowGenerator.generateShadow(this, elevation);
            }

            return this.shadow;
        } else {
            return null;
        }
    }

    public void invalidateShadow() {
        this.shadow = null;
        if(this.getParent() != null && this.getParent() instanceof View) {
            ((View)this.getParent()).postInvalidate();
        }

    }

    public void setTouchMargin(int left, int top, int right, int bottom) {
        this.touchMargin = new Rect(left, top, right, bottom);
    }

    public void setTouchMarginLeft(int margin) {
        this.touchMargin.left = margin;
    }

    public void setTouchMarginTop(int margin) {
        this.touchMargin.top = margin;
    }

    public void setTouchMarginRight(int margin) {
        this.touchMargin.right = margin;
    }

    public void setTouchMarginBottom(int margin) {
        this.touchMargin.bottom = margin;
    }

    public Rect getTouchMargin() {
        return this.touchMargin;
    }

    public void getHitRect(@NonNull Rect outRect) {
        Animation a;
        float[] loc;
        if(this.touchMargin == null) {
            super.getHitRect(outRect);
            a = this.getAnimation();
            if(a != null && a.hasStarted()) {
                a.getTransformation(System.currentTimeMillis(), this.t);
                loc = new float[]{(float)outRect.left, (float)outRect.top, (float)outRect.right, (float)outRect.bottom};
                loc[0] += ViewHelper.getTranslationX(this);
                loc[1] += ViewHelper.getTranslationY(this);
                loc[2] += ViewHelper.getTranslationX(this);
                loc[3] += ViewHelper.getTranslationY(this);
                outRect.set((int)loc[0], (int)loc[1], (int)loc[2], (int)loc[3]);
            }

        } else {
            outRect.set(this.getLeft() - this.touchMargin.left, this.getTop() - this.touchMargin.top, this.getRight() + this.touchMargin.right, this.getBottom() + this.touchMargin.bottom);
            a = this.getAnimation();
            if(a != null && a.hasStarted()) {
                a.getTransformation(System.currentTimeMillis(), this.t);
                loc = new float[]{(float)outRect.left, (float)outRect.top, (float)outRect.right, (float)outRect.bottom};
                loc[0] += ViewHelper.getTranslationX(this);
                loc[1] += ViewHelper.getTranslationY(this);
                loc[2] += ViewHelper.getTranslationX(this);
                loc[3] += ViewHelper.getTranslationY(this);
                outRect.set((int)loc[0], (int)loc[1], (int)loc[2], (int)loc[3]);
            }

        }
    }

    public void removeStateAnimator(StateAnimator animator) {
        this.stateAnimators.remove(animator);
    }

    public void addStateAnimator(StateAnimator animator) {
        this.stateAnimators.add(animator);
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        this.updateTint();
        if(this.rippleDrawable != null && this.rippleDrawable.getStyle() != carbon.drawable.RippleDrawable.Style.Background) {
            this.rippleDrawable.setState(this.getDrawableState());
        }

        if(this.stateAnimators != null) {
            Iterator var1 = this.stateAnimators.iterator();

            while(var1.hasNext()) {
                StateAnimator animator = (StateAnimator)var1.next();
                animator.stateChanged(this.getDrawableState());
            }
        }

    }

    public void setVisibility(final int visibility) {
        if(visibility == VISIBLE && (this.getVisibility() != VISIBLE || this.animator != null)) {
            if(this.animator != null) {
                this.animator.cancel();
            }

            if(this.inAnim != AnimUtils.Style.None) {
                this.animator = AnimUtils.animateIn(this, this.inAnim, new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator a) {
                        RippleImageButton.this.animator = null;
                    }
                });
            }

            super.setVisibility(visibility);
        } else if(visibility != VISIBLE && (this.getVisibility() == VISIBLE || this.animator != null)) {
            if(this.animator != null) {
                this.animator.cancel();
            }

            if(this.outAnim == AnimUtils.Style.None) {
                super.setVisibility(visibility);
                return;
            }

            this.animator = AnimUtils.animateOut(this, this.outAnim, new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator a) {
                    if(((ValueAnimator)a).getAnimatedFraction() == 1.0F) {
                        RippleImageButton.super.setVisibility(visibility);
                    }

                    RippleImageButton.this.animator = null;
                }
            });
        }

    }

    public void setVisibilityImmediate(int visibility) {
        super.setVisibility(visibility);
    }

    public Animator getAnimator() {
        return this.animator;
    }

    public AnimUtils.Style getOutAnimation() {
        return this.outAnim;
    }

    public void setOutAnimation(AnimUtils.Style outAnim) {
        this.outAnim = outAnim;
    }

    public AnimUtils.Style getInAnimation() {
        return this.inAnim;
    }

    public void setInAnimation(AnimUtils.Style inAnim) {
        this.inAnim = inAnim;
    }

    public void setTint(ColorStateList list) {
        this.tint = list;
        this.updateTint();
        this.postInvalidate();
    }

    public void setTint(int color) {
        this.setTint(ColorStateList.valueOf(color));
    }

    public ColorStateList getTint() {
        return this.tint;
    }

    private void updateTint() {
        if(this.tint != null) {
            int color = this.tint.getColorForState(this.getDrawableState(), this.tint.getDefaultColor());
            this.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
            this.setAlpha(Color.alpha(color));
        } else {
            this.setColorFilter((ColorFilter)null);
            this.setAlpha(255);
        }

    }

    static {
        pdMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    }
}
