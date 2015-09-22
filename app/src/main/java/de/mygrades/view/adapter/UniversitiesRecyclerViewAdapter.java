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

/**
 * Custom recycler view adapter for universities.
 */
public class UniversitiesRecyclerViewAdapter extends RecyclerView.Adapter<UniversityItemViewHolder> {

    private Context context;
    private List<University> universities;

    public UniversitiesRecyclerViewAdapter(Context context, List<University> data) {
        this.context = context;
        if (data == null) {
            universities = new ArrayList<>();
        } else {
            universities = new ArrayList<>(data);
        }
    }

    public void add(University u, int position) {
        universities.add(position, u);
        notifyItemInserted(position);
    }

    @Override
    public UniversityItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_university, parent, false);
        return new UniversityItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UniversityItemViewHolder holder, int position) {
        // set text view content
        University university = universities.get(position);
        holder.tvUniversityName.setText(university.getName());

        // show or hide section header
        if (university.isSection()) {
            holder.llSectionHeader.setVisibility(View.VISIBLE);
            holder.tvSectionHeader.setText(university.getName().substring(0, 1).toUpperCase());
        } else {
            holder.llSectionHeader.setVisibility(View.GONE);
        }

        // create intent and add extra data
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_UNIVERSITY_NAME, university.getName());
        intent.putExtra(LoginActivity.EXTRA_UNIVERSITY_ID, university.getUniversityId());

        // set click listener
        holder.tvUniversityName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return universities.size();
    }
}
