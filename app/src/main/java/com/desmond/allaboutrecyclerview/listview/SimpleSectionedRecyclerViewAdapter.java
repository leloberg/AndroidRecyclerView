package com.desmond.allaboutrecyclerview.listview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Credit: https://gist.github.com/gabrielemariotti/4c189fb1124df4556058
 * The RecyclerView should use a LinearLayoutManager.
 */
public class SimpleSectionedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private static final int SECTION_TYPE = 0;

    private boolean mValid = true;
    private int mSectionResourceId;
    private int mTextResourceId;
    private LayoutInflater mLayoutInflater;
    private RecyclerView.Adapter mBaseAdapter;
    private SparseArray<Section> mSections;

    public SimpleSectionedRecyclerViewAdapter(Context context, int sectionResourceId,
                                              int textResourceId, RecyclerView.Adapter baseAdapter) {
        super();
        mLayoutInflater = LayoutInflater.from(context);
        mSectionResourceId = sectionResourceId;
        mTextResourceId = textResourceId;
        mContext = context;
        mSections = new SparseArray<>();

        // Register a data observer to be notified of data changes from within SimpleSectionedRecyclerViewAdapter
        mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == SECTION_TYPE) {
            final View view = LayoutInflater.from(mContext).inflate(mSectionResourceId, viewGroup, false);
            return new SectionViewHolder(view,mTextResourceId);
        }
        else {
            return mBaseAdapter.onCreateViewHolder(viewGroup, viewType -1);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder sectionViewHolder, int position) {
        if (isSectionHeaderPosition(position)) {
            ((SectionViewHolder) sectionViewHolder).title.setText(mSections.get(position).title);
        }
        else {
            mBaseAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position));
        }
    }

    public void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(Section lhs, Section rhs) {
                return (lhs.firstPosition == rhs.firstPosition) ?
                        0 : (lhs.firstPosition < rhs.firstPosition ? -1 : 1);
            }
        });
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {break;}
            ++offset;
        }

        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > sectionedPosition) {break;}
            --offset;
        }

        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }

    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position) ?
                Integer.MAX_VALUE - mSections.indexOfKey(position)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemCount() {
        return (mValid ? mBaseAdapter.getItemCount() + mSections.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position) ?
                SECTION_TYPE : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1;
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public SectionViewHolder(View view, int mTextResourceId) {
            super(view);
            title = (TextView) view.findViewById(mTextResourceId);
        }
    }

    public static class Section {
        int firstPosition;
        int sectionedPosition;
        CharSequence title;

        public Section(int firstPosition, CharSequence title) {
            this.firstPosition = firstPosition;
            this.title = title;
        }

        public CharSequence getTitle() {
            return title;
        }
    }
}
