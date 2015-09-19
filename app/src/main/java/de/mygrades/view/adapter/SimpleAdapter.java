package de.mygrades.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.R;
import de.mygrades.database.dao.University;
import de.mygrades.view.activity.LoginActivity;
import de.mygrades.view.adapter.viewholder.UniversityItemViewHolder;

public class SimpleAdapter extends RecyclerView.Adapter<UniversityItemViewHolder> {

    private final Context mContext;
    private List<University> mData;

    public void add(University s,int position) {
        position = position == -1 ? getItemCount()  : position;
        mData.add(position,s);
        notifyItemInserted(position);
    }

    public void remove(int position){
        if (position < getItemCount()  ) {
            mData.remove(position);
            notifyItemRemoved(position);
        }
    }

    public SimpleAdapter(Context context, List<University> data) {
        mContext = context;
        if (data != null)
            mData = new ArrayList<University>(data);
        else mData = new ArrayList<University>();
    }

    public UniversityItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.item_university, parent, false);
        return new UniversityItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UniversityItemViewHolder holder, final int position) {
        // set text view content
        University university = mData.get(position);
        holder.tvUniversityName.setText(university.getName());

        // create intent and add extra data
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_UNIVERSITY_NAME, university.getName());
        intent.putExtra(LoginActivity.EXTRA_UNIVERSITY_ID, university.getUniversityId());

        // set click listener
        holder.tvUniversityName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
