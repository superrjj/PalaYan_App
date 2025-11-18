package com.example.palayan.Helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Custom RecyclerView that measures all items when used inside a ScrollView.
 * This ensures all items are displayed even when the RecyclerView has wrap_content height.
 */
public class NonScrollableRecyclerView extends RecyclerView {

    public NonScrollableRecyclerView(@NonNull Context context) {
        super(context);
    }

    public NonScrollableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // If height is wrap_content (AT_MOST or UNSPECIFIED), measure all items
        if (View.MeasureSpec.getMode(heightSpec) == View.MeasureSpec.AT_MOST ||
            View.MeasureSpec.getMode(heightSpec) == View.MeasureSpec.UNSPECIFIED) {
            
            final Adapter adapter = getAdapter();
            final LayoutManager layoutManager = getLayoutManager();
            
            if (adapter != null && layoutManager instanceof LinearLayoutManager) {
                int itemCount = adapter.getItemCount();
                if (itemCount > 0) {
                    int totalHeight = getPaddingTop() + getPaddingBottom();
                    int width = View.MeasureSpec.getSize(widthSpec);
                    int availableWidth = width - getPaddingLeft() - getPaddingRight();
                    int childWidthSpec = View.MeasureSpec.makeMeasureSpec(
                        availableWidth, 
                        View.MeasureSpec.EXACTLY
                    );
                    
                    // Measure each item by creating temporary ViewHolders
                    for (int i = 0; i < itemCount; i++) {
                        try {
                            RecyclerView.ViewHolder holder = adapter.createViewHolder(this, adapter.getItemViewType(i));
                            adapter.onBindViewHolder(holder, i);
                            View child = holder.itemView;
                            
                            // Get layout params to account for margins
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                            if (params == null) {
                                params = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                );
                                child.setLayoutParams(params);
                            }
                            
                            int childHeightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                            child.measure(childWidthSpec, childHeightSpec);
                            
                            // Add item height plus top and bottom margins
                            totalHeight += child.getMeasuredHeight();
                            totalHeight += params.topMargin + params.bottomMargin;
                        } catch (Exception e) {
                            // If measurement fails for one item, continue with others
                            android.util.Log.e("NonScrollableRecyclerView", "Error measuring item " + i, e);
                        }
                    }
                    
                    setMeasuredDimension(width, totalHeight);
                    return;
                }
            }
        }
        
        // Default measurement
        super.onMeasure(widthSpec, heightSpec);
    }
    
    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        // Force remeasurement after adapter is set
        if (adapter != null) {
            post(() -> {
                requestLayout();
                invalidate();
            });
        }
    }
    
    /**
     * Force remeasurement after adapter data changes
     */
    public void forceMeasure() {
        post(() -> {
            requestLayout();
            invalidate();
        });
    }
}

