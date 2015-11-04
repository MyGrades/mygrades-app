package de.mygrades.view.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import de.mygrades.view.adapter.model.UniversitySection;
import de.mygrades.view.adapter.model.UniversityItem;

/**
 * Custom recycler view adapter for universities.
 */
public class UniversitiesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_HEADER = 0;
    private final int VIEW_TYPE_SECTION = 1;
    private final int VIEW_TYPE_ITEM = 2;

    private List<UniversityAdapterItem> items;

    public UniversitiesRecyclerViewAdapter() {
        super();
        items = new ArrayList<>();
        items.add(new UniversityHeader());
    }

    /**
     * Add a new university.
     * It will create a relevant section if it does not exist already.
     *
     * @param newUniversity - university to add
     */
    public void add(UniversityItem newUniversity) {
        // update (only if necessary)
        if (!updateUniversity(newUniversity)) {

            // if the university did not exist already, add it
            addUniversity(newUniversity);
        }
    }

    /**
     * Updates an university if it exists already (compared by university id).
     * Its also checked, whether the section will be empty afterwards and deletes it if necessary.
     *
     * @param newUniversity university to delete
     */
    private boolean updateUniversity(UniversityItem newUniversity) {
        int actSectionIndex = 0;
        for(int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof UniversitySection) {
                actSectionIndex = i;
            } else if (items.get(i) instanceof UniversityItem) {
                UniversityItem universityItem = (UniversityItem) items.get(i);
                if (universityItem.getUniversityId() == newUniversity.getUniversityId()) {
                    if (!universityItem.getName().equals(newUniversity.getName())) {
                        // delete old university
                        items.remove(i);
                        notifyItemRemoved(i);

                        // check if section is still required (section could be empty now)
                        if (actSectionIndex == items.size() - 1 || (items.get(actSectionIndex + 1) instanceof UniversitySection)) {
                            // next item is also an section, so the one above can be deleted
                            items.remove(actSectionIndex);
                            notifyItemRemoved(actSectionIndex);
                        }

                        // add new university
                        addUniversity(newUniversity);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds an university to the given section by its index.
     *
     * @param newUniversity - university to add
     */
    private void addUniversity(UniversityItem newUniversity) {
        String desiredSection = newUniversity.getName().substring(0, 1).toUpperCase();

        // get section index
        int sectionIndex = getSectionIndex(desiredSection);

        // add university after section index (lexicographic)
        int newUniversityIndex = items.size(); // add to end, if no index was found
        for(int i = sectionIndex + 1; i < items.size(); i++) {
            if (items.get(i) instanceof UniversityItem) {
                UniversityItem universityItem = (UniversityItem) items.get(i);
                if (newUniversity.getName().compareToIgnoreCase(universityItem.getName()) <= 0) {
                    newUniversityIndex = i;
                    break;
                }
            } else if (items.get(i) instanceof UniversitySection) {
                newUniversityIndex = i;
                break;
            }
        }

        // add new university to list
        items.add(newUniversityIndex, newUniversity);
        notifyItemInserted(newUniversityIndex);
    }

    /**
     * Adds a new university section.
     *
     * @param newSection - section name
     * @return index
     */
    private int addSection(String newSection) {
        int sectionIndex = -1; // -1, if there are no sections so far

        // find index where the section should be added (lexicographic)
        for(int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof UniversitySection) {
                UniversitySection section = (UniversitySection) items.get(i);
                if (newSection.compareToIgnoreCase(section.getSection()) <= 0) {
                    sectionIndex = i;
                    break;
                }
            }
        }

        // if list is empty, use index 0
        sectionIndex = items.size() == 0 ? 0 : sectionIndex;

        // if new section goes to bottom, use item.size()
        sectionIndex = sectionIndex < 0 ? items.size() : sectionIndex;

        // add new section
        UniversitySection newUniversitySection = new UniversitySection(newSection);
        items.add(sectionIndex, newUniversitySection);
        notifyItemInserted(sectionIndex);

        return sectionIndex;
    }

    /**
     * Get the index for a section by its string.
     * It will create a new section, if necessary.
     *
     * @param desiredSection - section as string
     * @return index
     */
    private int getSectionIndex(String desiredSection) {
        int sectionIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof UniversitySection) {
                UniversitySection section = (UniversitySection) items.get(i);
                if (section.getSection().equals(desiredSection)) {
                    sectionIndex = i;
                    break;
                }
            }
        }

        // if no section was found, add a new one
        if (sectionIndex < 0) {
            sectionIndex = addSection(desiredSection);
        }

        return sectionIndex;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_university_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_SECTION) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_university_section, parent, false);
            return new UniversitySectionViewHolder(view);
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
        } else if (holder instanceof UniversitySectionViewHolder) {
            UniversitySectionViewHolder viewHolder = (UniversitySectionViewHolder) holder;
            UniversitySection universitySection = (UniversitySection) items.get(position);

            viewHolder.tvSection.setText(universitySection.getSection());
        } else if (holder instanceof HeaderViewHolder) {
            // TODO: show / hide error wrapper and loading animation
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof UniversityHeader) {
            return VIEW_TYPE_HEADER;
        } else if (items.get(position) instanceof UniversityItem) {
            return VIEW_TYPE_ITEM;
        } else if (items.get(position) instanceof UniversitySection) {
            return VIEW_TYPE_SECTION;
        }

        return -1;
    }

    /**
     * ViewHolder for an university section row.
     */
    public class UniversitySectionViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvSection;

        public UniversitySectionViewHolder(View rootView) {
            super(rootView);
            tvSection = (TextView) rootView.findViewById(R.id.tv_university_section);
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

    /**
     * ViewHolder for the header view.
     */
    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvHeader;

        public HeaderViewHolder(View rootView) {
            super(rootView);
            tvHeader = (TextView) rootView.findViewById(R.id.tv_university_header);
        }
    }
}
