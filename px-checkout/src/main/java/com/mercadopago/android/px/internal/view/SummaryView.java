package com.mercadopago.android.px.internal.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import com.mercadopago.android.px.R;
import java.util.ArrayList;
import java.util.List;

public class SummaryView extends LinearLayout {

    @NonNull private final VerticalElementDescriptorView verticalHeaderDescriptor;
    @NonNull private final HorizontalElementDescriptorView horizontalHeaderDescriptor;
    @NonNull private ElementDescriptorView currentHeaderDescriptor;
    @NonNull private final AmountDescriptorView totalAmountDescriptor;
    @NonNull private final View itemsMaxSize;
    /* default */ final DetailAdapter detailAdapter;

    /* default */ final RecyclerView detailRecyclerView;
    @Nullable private OnMeasureListener measureListener;

    private final Animation listAppearAnimation;

    /* default */ boolean animating = false;
    private int maxElementsToShow;
    private boolean shouldAnimateReturnFromCardForm = false;

    public SummaryView(final Context context) {
        this(context, null);
    }

    public SummaryView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SummaryView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        setBackgroundResource(R.color.px_checkout_summary_background);
        inflate(getContext(), R.layout.px_view_express_summary, this);
        itemsMaxSize = findViewById(R.id.itemsMaxSize);
        totalAmountDescriptor = findViewById(R.id.total);
        detailRecyclerView = findViewById(R.id.recycler);
        detailRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        detailAdapter = new DetailAdapter();
        detailRecyclerView.setAdapter(detailAdapter);

        verticalHeaderDescriptor = findViewById(R.id.bigElementDescriptor);
        verticalHeaderDescriptor.setVisibility(INVISIBLE);
        horizontalHeaderDescriptor = findViewById(R.id.element_descriptor_toolbar);
        currentHeaderDescriptor = horizontalHeaderDescriptor;

        listAppearAnimation = AnimationUtils.loadAnimation(context, R.anim.px_summary_list_appear);
        listAppearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
                detailRecyclerView.setAlpha(1.0f);
                animating = true;
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                animating = false;
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {

            }
        });
    }

    public void setMaxElementsToShow(final int maxElementsToShow) {
        this.maxElementsToShow = maxElementsToShow;
    }

    public void setOnLogoClickListener(@NonNull final OnClickListener listener) {
        verticalHeaderDescriptor.setOnClickListener(listener);
        horizontalHeaderDescriptor.setOnClickListener(listener);
    }

    public void setMeasureListener(@Nullable final OnMeasureListener measureListener) {
        this.measureListener = measureListener;
    }

    private boolean isViewOverlapping(final View firstView, final View secondView) {
        final int yFirstViewEnd = firstView.getTop() + firstView.getHeight();
        final int ySecondViewInit = secondView.getTop();

        return yFirstViewEnd >= ySecondViewInit;
    }

    public void showToolbarElementDescriptor(@NonNull final ElementDescriptorView.Model elementDescriptorModel) {
        horizontalHeaderDescriptor.update(elementDescriptorModel);
        horizontalHeaderDescriptor.setVisibility(VISIBLE);
    }

    public void animateEnter(final long duration) {
        shouldAnimateReturnFromCardForm = true;
        detailAdapter.customAnimation = true;

        startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.px_summary_translate_in));

        final Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.px_fade_in);
        fadeIn.setStartOffset(duration);
        fadeIn.setDuration(duration);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {

            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                detailAdapter.customAnimation = false;
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {

            }
        });
        findViewById(R.id.separator).startAnimation(fadeIn);

        totalAmountDescriptor.animateEnter();
    }

    public void animateExit(final long duration) {
        final Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.px_fade_out);
        fadeOut.setDuration(duration);

        if (showingVerticalHeader()) {
            verticalHeaderDescriptor.startAnimation(fadeOut);
        } else {
            horizontalHeaderDescriptor.startAnimation(
                AnimationUtils.loadAnimation(getContext(), R.anim.px_summary_slide_up_out));
        }

        detailRecyclerView.startAnimation(fadeOut);

        findViewById(R.id.separator).startAnimation(fadeOut);

        startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.px_summary_translate_out));

        totalAmountDescriptor.startAnimation(fadeOut);
    }

    public void animateElementList(final float positionOffset) {
        if (!animating) {
            detailRecyclerView.setAlpha(1.0f - positionOffset);
        }
    }

    public void configureToolbar(@NonNull final AppCompatActivity activity,
        @NonNull final View.OnClickListener listener) {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeActionContentDescription(R.string.px_label_back);
        }
        toolbar.setNavigationOnClickListener(listener);
    }

    public void update(@NonNull final Model model) {
        if (model.headerDescriptor != null) {
            verticalHeaderDescriptor.update(model.headerDescriptor);
        } else {
            verticalHeaderDescriptor.setVisibility(GONE);
        }
        totalAmountDescriptor.update(model.total);
        detailAdapter.updateItems(model.elements);
        detailRecyclerView.startAnimation(listAppearAnimation);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        super.onLayout(changed, l, t, r, b);
        if (isViewOverlapping(verticalHeaderDescriptor, detailRecyclerView)) {
            if (showingVerticalHeader()) {
                swapHeaderTo(horizontalHeaderDescriptor);
            }
        } else if (!showingVerticalHeader()) {
            swapHeaderTo(verticalHeaderDescriptor);
        }
        if (measureListener != null) {
            final int availableSummaryHeight = itemsMaxSize.getMeasuredHeight();
            final float singleItemHeight = AmountDescriptorView.getDesiredHeight(getContext());
            final int expectedItemsHeight = Math.round(singleItemHeight * maxElementsToShow);
            measureListener.onSummaryMeasured(expectedItemsHeight > availableSummaryHeight);
        }
    }

    private boolean showingVerticalHeader() {
        return currentHeaderDescriptor == verticalHeaderDescriptor;
    }

    private void swapHeaderTo(ElementDescriptorView newHeader) {
        currentHeaderDescriptor.animateExit();
        currentHeaderDescriptor = newHeader;
        currentHeaderDescriptor.animateEnter(shouldAnimateReturnFromCardForm);
        shouldAnimateReturnFromCardForm = false;
    }

    public interface OnMeasureListener {
        void onSummaryMeasured(boolean itemsClipped);
    }

    public static final class Model {

        /* default */ @NonNull final List<AmountDescriptorView.Model> elements;

        /* default */ @Nullable final ElementDescriptorView.Model headerDescriptor;

        /* default */ @NonNull final AmountDescriptorView.Model total;

        public Model(@Nullable final ElementDescriptorView.Model headerDescriptor,
            @NonNull final List<AmountDescriptorView.Model> elements,
            @NonNull final AmountDescriptorView.Model total) {
            this.elements = elements;
            this.headerDescriptor = headerDescriptor;
            this.total = total;
        }

        public int getElementsSize() {
            return elements.size();
        }
    }

    /* default */ static final class DetailAdapter extends RecyclerView.Adapter<AmountViewHolder> {
        @NonNull private List<AmountDescriptorView.Model> items;

        /* default */ boolean customAnimation = false;

        /* default */ DetailAdapter() {
            items = new ArrayList<>();
        }

        @Override
        public AmountViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final AmountDescriptorView view = (AmountDescriptorView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.px_viewholder_amountdescription, parent, false);
            if (customAnimation) {
                view.animateEnter();
            }
            return new AmountViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final AmountViewHolder holder, final int position) {
            holder.populate(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        /* default */ void updateItems(@NonNull final List<AmountDescriptorView.Model> items) {
            this.items = items;
            notifyDataSetChanged();
        }
    }

    /* default */ static class AmountViewHolder extends RecyclerView.ViewHolder {

        private final AmountDescriptorView amountDescView;

        /* default */ AmountViewHolder(@NonNull final AmountDescriptorView amountDescView) {
            super(amountDescView);
            this.amountDescView = amountDescView;
        }

        /* default */ void populate(@NonNull final AmountDescriptorView.Model model) {
            amountDescView.update(model);
        }
    }
}