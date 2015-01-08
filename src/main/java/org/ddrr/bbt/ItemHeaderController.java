package org.ddrr.bbt;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class ItemHeaderController implements OnScrollListener {
    private final Set<ItemHeaderViewHolder> mViewHolderSet = Collections
            .newSetFromMap(new WeakHashMap<ItemHeaderViewHolder, Boolean>());
    private OnScrollListener mExternalListener;

    public void registerViewHolder(ItemHeaderViewHolder holder) {
        mViewHolderSet.add(holder);
    }

    public void clear() {
        mViewHolderSet.clear();
    }

    public void setOnScrollListener(OnScrollListener l) {
        this.mExternalListener = l;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (null != mExternalListener) {
            mExternalListener.onScrollStateChanged(view, scrollState);
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        for (ItemHeaderViewHolder holder : mViewHolderSet) {
            holder.updateHeader();
        }
        if (null != mExternalListener) {
            mExternalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

    }

}
