package de.mygrades.view.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.R;
import de.mygrades.view.activity.LoginActivity;
import de.mygrades.view.adapter.model.UniversityAdapterItem;
import de.mygrades.view.adapter.model.UniversityHeader;
import de.mygrades.view.adapter.model.UniversityItem;

/**
 * Custom recycler view adapter for universities.
 */
public class UniversitiesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_HEADER = 0;
    private final int VIEW_TYPE_ITEM = 1;

    private List<UniversityAdapterItem> items;

    public UniversitiesRecyclerViewAdapter() {
        super();
        items = new ArrayList<>();
    }

    /**
     * Add a new university.
     * It will create a relevant header if it does not exist already.
     *
     * @param newUniversity - university to add
     */
    public void add(UniversityItem newUniversity) {
        // check if header already exists for this university
        String desiredHeader = newUniversity.getName().substring(0, 1).toUpperCase();
        int headerIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof UniversityHeader) {
                UniversityHeader header = (UniversityHeader) items.get(i);
                if (header.getHeader().equals(desiredHeader)) {
                    headerIndex = i;
                    break;
                }
            }
        }

        // if no header was found, add a new one
        if (headerIndex < 0) {
            headerIndex = addHeader(desiredHeader);
        }

        // add university after header index (lexicographic)
        int newUniversityIndex = items.size(); // add to end, if no index was found
        for(int i = headerIndex + 1; i < items.size(); i++) {
            if (items.get(i) instanceof UniversityItem) {
                UniversityItem universityItem = (UniversityItem) items.get(i);
                if (newUniversity.getName().compareToIgnoreCase(universityItem.getName()) <= 0) {
                    newUniversityIndex = i;
                    break;
                }
            } else if (items.get(i) instanceof UniversityHeader) {
                newUniversityIndex = i;
                break;
            }
        }

        // add new university to list
        items.add(newUniversityIndex, newUniversity);
        notifyItemInserted(newUniversityIndex);
    }

    /**
     * Adds a new university header.
     *
     * @param newHeader - header name
     * @return index
     */
    private int addHeader(String newHeader) {
        int headerIndex = -1; // -1, if there are no headers so far

        // find index where the header should be added (lexicographic)
        for(int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof UniversityHeader) {
                UniversityHeader header = (UniversityHeader) items.get(i);
                if (newHeader.compareToIgnoreCase(header.getHeader()) <= 0) {
                    headerIndex = i;
                    break;
                }
            }
        }

        // if list is empty, use index 0
        headerIndex = items.size() == 0 ? 0 : headerIndex;

        // if new header goes to bottom, use item.size()
        headerIndex = headerIndex < 0 ? items.size() : headerIndex;

        // add new header
        UniversityHeader newUniversityHeader = new UniversityHeader(newHeader);
        items.add(headerIndex, newUniversityHeader);
        notifyItemInserted(headerIndex);

        return headerIndex;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_university_header, parent, false);
            return new UniversityHeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_university, parent, false);
            return new UniversityItemViewHolder(view);
        }

        return null;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UniversityItemViewHolder) {
            UniversityItemViewHolder viewHolder = (UniversityItemViewHolder) holder;
            UniversityItem universityItem = (UniversityItem) items.get(position);

            viewHolder.tvUniversityName.setText(universityItem.getName());
        } else if (holder instanceof UniversityHeaderViewHolder) {
            UniversityHeaderViewHolder viewHolder = (UniversityHeaderViewHolder) holder;
            UniversityHeader universityHeader = (UniversityHeader) items.get(position);

            viewHolder.tvHeader.setText(universityHeader.getHeader());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof UniversityItem) {
            return VIEW_TYPE_ITEM;
        } else if (items.get(position) instanceof UniversityHeader) {
            return VIEW_TYPE_HEADER;
        }

        return -1;
    }

    /**
     * ViewHolder for an university header row.
     */
    public class UniversityHeaderViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvHeader;

        public UniversityHeaderViewHolder(View rootView) {
            super(rootView);
            tvHeader = (TextView) rootView.findViewById(R.id.tv_university_header);
        }
    }

    /**
     * ViewHolder for an university row.
     */
    public class UniversityItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView tvUniversityName;

        public UniversityItemViewHolder(View rootView) {
            super(rootView);
            tvUniversityName = (TextView) rootView.findViewById(R.id.tv_university_name);
            rootView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Intent intent = new Intent(v.getContext(), LoginActivity.class);
            UniversityItem universityItem = (UniversityItem) items.get(getAdapterPosition());

            intent.putExtra(LoginActivity.EXTRA_UNIVERSITY_NAME, universityItem.getName());
            intent.putExtra(LoginActivity.EXTRA_UNIVERSITY_ID, universityItem.getUniversityId());
            v.getContext().startActivity(intent);
        }
    }
}
