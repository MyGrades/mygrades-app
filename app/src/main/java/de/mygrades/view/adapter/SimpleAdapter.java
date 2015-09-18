package de.mygrades.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.R;
import de.mygrades.database.dao.University;
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
        holder.universityName.setText(mData.get(position).getName());
        holder.universityName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext,"Position ="+position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
