package de.mygrades.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.ErrorEvent;
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

    private MainServiceHelper mainServiceHelper;
    private Context context;

    private List<UniversityAdapterItem> items;
    private UniversityHeader header;

    public UniversitiesRecyclerViewAdapter(Context context) {
        super();
        this.context = context.getApplicationContext();
        mainServiceHelper = new MainServiceHelper(this.context);

        header = new UniversityHeader();

        items = new ArrayList<>();
        items.add(header);
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
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;

            // show/hide progress wheel
            viewHolder.progressWheel.setVisibility(header.isLoading() ? View.VISIBLE : View.GONE);

            // show/hide error wrapper
            if (header.getActErrorType() != null) {
                viewHolder.llErrorWrapper.setVisibility(View.VISIBLE);

                String errorMessage;
                switch (header.getActErrorType()) {
                    case NO_NETWORK:
                        errorMessage = context.getResources().getString(R.string.error_no_network);
                        break;
                    case TIMEOUT:
                        errorMessage = context.getResources().getString(R.string.error_server_timeout);
                        break;
                    case GENERAL:
                    default:
                        errorMessage = context.getResources().getString(R.string.error_unknown);
                }

                viewHolder.tvErrorMessage.setText(errorMessage);

            } else {
                viewHolder.llErrorWrapper.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Checks if list contains no universities.
     * It's important to notice that the list always contains the header view.
     *
     * @return true, if list only contains the header view
     */
    public boolean isEmpty() {
        return items.size() == 1;
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
     * Shows or hide the loading animation.
     *
     * @param isLoading true, if animation should be shown.
     */
    public void showLoadingAnimation(boolean isLoading) {
        // ignore if nothing would change to avoid notifyItemChanged
        if (header.isLoading() == isLoading) {
            return;
        }

        header.setIsLoading(isLoading);
        notifyItemChanged(0);
    }

    /**
     * Shows the error wrapper and hides the loading animation.
     *
     * @param errorType ErrorEvent.ErrorType
     */
    public void showError(ErrorEvent.ErrorType errorType) {
        // ignore if nothing would change to avoid notifyItemChanged
        if (header.getActErrorType() == errorType) {
            return;
        }

        header.setActErrorType(errorType);

        if (errorType != null) {
            header.setIsLoading(false);
        }
        notifyItemChanged(0);
    }

    /**
     * The current EventError.ErrorType.
     *
     * @return current shown EventError.ErrorType or null.
     */
    public ErrorEvent.ErrorType getActErrorType() {
        return header.getActErrorType();
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
        public final ProgressWheel progressWheel;
        public final LinearLayout llErrorWrapper;
        public final TextView tvErrorMessage;
        public final Button btnTryAgain;

        public HeaderViewHolder(View rootView) {
            super(rootView);
            tvHeader = (TextView) rootView.findViewById(R.id.tv_university_header);
            progressWheel = (ProgressWheel) rootView.findViewById(R.id.progress_wheel);
            llErrorWrapper = (LinearLayout) rootView.findViewById(R.id.ll_error_wrapper);
            tvErrorMessage = (TextView) rootView.findViewById(R.id.tv_error_message);
            btnTryAgain = (Button) rootView.findViewById(R.id.btn_try_again);
            btnTryAgain.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showError(null);
                    showLoadingAnimation(true);

                    // load universities from server
                    mainServiceHelper.getUniversities(true);
                }
            });
        }
    }
}
