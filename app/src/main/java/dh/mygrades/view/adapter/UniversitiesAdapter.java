package dh.mygrades.view.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import dh.mygrades.R;
import dh.mygrades.main.MainServiceHelper;
import dh.mygrades.main.events.ErrorEvent;
import dh.mygrades.view.adapter.dataprovider.UniversitiesDataProvider;
import dh.mygrades.view.adapter.model.RuleItem;
import dh.mygrades.view.adapter.model.UniversityFooter;
import dh.mygrades.view.adapter.model.UniversityHeader;
import dh.mygrades.view.adapter.model.UniversityItem;

/**
 * Expandable UniversitiesAdapter for the university recycler view.
 */
public class UniversitiesAdapter extends AbstractExpandableItemAdapter<UniversitiesAdapter.GroupViewHolder, UniversitiesAdapter.RuleViewHolder> {

    private Context context;
    private UniversitiesDataProvider dataProvider;
    private RecyclerViewExpandableItemManager expandableItemManager;

    private final int GROUP_VIEW_TYPE_UNIVERSITY = 1;
    private final int GROUP_VIEW_TYPE_HEADER = 2;
    private final int GROUP_VIEW_TYPE_FOOTER = 3;

    /**
     * General click listener for group and child items.
     */
    private View.OnClickListener itemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickItemView(v);
        }
    };

    public UniversitiesAdapter(Context context, UniversitiesDataProvider dataProvider, RecyclerViewExpandableItemManager recyclerViewExpandableItemManager) {
        this.context = context.getApplicationContext();
        this.expandableItemManager = recyclerViewExpandableItemManager;

        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);

        // set data provider
        this.dataProvider = dataProvider;
    }

    /**
     * Adds a list of UniversityItem objects to the underlying data provider.
     *
     * @param universityItems List of UniversityItem objects.
     */
    public void addUniversities(List<UniversityItem> universityItems) {
        if (universityItems.size() > 0) {
            dataProvider.addUniversities(expandableItemManager, universityItems);
        }
    }

    @Override
    public int getGroupCount() {
        return dataProvider.getGroupCount();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return dataProvider.getChildCount(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return dataProvider.getGroupItem(groupPosition).getGroupId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return dataProvider.getChildItem(groupPosition, childPosition).getChildId();
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        if (groupPosition > 0 && groupPosition < getGroupCount() - 1) {
            return GROUP_VIEW_TYPE_UNIVERSITY;
        } else if (groupPosition == getGroupCount() - 1) {
            return GROUP_VIEW_TYPE_FOOTER;
        } else {
            return GROUP_VIEW_TYPE_HEADER;
        }
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case GROUP_VIEW_TYPE_UNIVERSITY:
                final View universityView = inflater.inflate(R.layout.item_university, parent, false);
                return new UniversityViewHolder(universityView, itemOnClickListener);
            case GROUP_VIEW_TYPE_HEADER:
                final View headerView = inflater.inflate(R.layout.item_university_header, parent, false);
                return new HeaderViewHolder(headerView, tryAgainListener);
            case GROUP_VIEW_TYPE_FOOTER:
                final View footerView = inflater.inflate(R.layout.item_university_footer, parent, false);
                return new FooterViewHolder(footerView, itemOnClickListener);
        }
        return null;
    }

    @Override
    public RuleViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.item_rule, parent, false);
        return new RuleViewHolder(v, itemOnClickListener);
    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder holder, int groupPosition, int viewType) {
        switch (viewType) {
            case GROUP_VIEW_TYPE_UNIVERSITY:
                UniversityViewHolder uvh = (UniversityViewHolder) holder;

                UniversityItem universityData = (UniversityItem) dataProvider.getGroupItem(groupPosition);
                universityData.setName(universityData.getName());

                uvh.tvUniversityName.setText(universityData.getName());
                if (universityData.isSectionHeader()) {
                    uvh.llSectionWrapper.setVisibility(View.VISIBLE);
                    uvh.tvSection.setText(universityData.getSectionTitle());
                } else {
                    uvh.llSectionWrapper.setVisibility(View.GONE);
                }

                uvh.tvSelectRuleHint.setVisibility(universityData.showRuleHint() ? View.VISIBLE : View.GONE);
                break;
            case GROUP_VIEW_TYPE_HEADER:
                HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
                UniversityHeader header = dataProvider.getHeader();

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
                break;
            case GROUP_VIEW_TYPE_FOOTER:
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                UniversityFooter footer = dataProvider.getFooter();

                if (footer.isVisible()) {
                    footerHolder.llContainer.setVisibility(View.VISIBLE);
                } else {
                    footerHolder.llContainer.setVisibility(View.GONE);
                }
        }
    }

    @Override
    public void onBindChildViewHolder(RuleViewHolder holder, int groupPosition, int childPosition, int viewType) {
        RuleItem ruleData = dataProvider.getChildItem(groupPosition, childPosition);
        holder.tvRuleName.setText(ruleData.getName());
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        // NOTE: click events are handled manually in onClickItemView(View v)
        return false;
    }

    /**
     * Handle click events on group and child items.
     *
     * @param v view clicked on
     */
    private void onClickItemView(View v) {
        RecyclerView.ViewHolder vh = RecyclerViewAdapterUtils.getViewHolder(v);
        int flatPosition = vh.getAdapterPosition();

        if (flatPosition == RecyclerView.NO_POSITION) {
            return;
        }

        long expandablePosition = expandableItemManager.getExpandablePosition(flatPosition);
        int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition);
        int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition);

        switch (v.getId()) {
            case R.id.container:
                if (childPosition == RecyclerView.NO_POSITION) {
                    handleOnClickGroupItemContainerView(v, groupPosition);
                } else {
                    handleOnClickChildItemContainerView(v, groupPosition, childPosition);
                }
                break;
        }
    }

    /**
     * Handle clicks on group items. If it contains only one child, directly go to login screen.
     * Otherwise expand/collapse the group.
     *
     * @param v view clicked on
     * @param groupPosition group position
     */
    private void handleOnClickGroupItemContainerView(View v, int groupPosition) {
        if (getGroupItemViewType(groupPosition) == GROUP_VIEW_TYPE_UNIVERSITY) {
            UniversityItem universityData = (UniversityItem) dataProvider.getGroupItem(groupPosition);
            if (universityData.getRules().size() > 1) {
                if (expandableItemManager.isGroupExpanded(groupPosition)) {
                    expandableItemManager.collapseGroup(groupPosition);
                    universityData.setShowRuleHint(false);
                } else {
                    expandableItemManager.expandGroup(groupPosition);
                    universityData.setShowRuleHint(true);

                }
            } else if (universityData.getRules().size() == 1) {
                universityData.getRules().get(0).goToLogin(v.getContext(), universityData);
            }
        } else if (getGroupItemViewType(groupPosition) == GROUP_VIEW_TYPE_FOOTER) {
            dataProvider.getFooter().goToSubmitWish(v.getContext());
        }
    }

    /**
     * Handle clicks on child items and go to LoginActivity.
     *
     * @param v view clicked on
     * @param groupPosition group position
     * @param childPosition child position
     */
    private void handleOnClickChildItemContainerView(View v, int groupPosition, int childPosition) {
        UniversityItem universityData = (UniversityItem) dataProvider.getGroupItem(groupPosition);
        RuleItem ruleData = dataProvider.getChildItem(groupPosition, childPosition);
        ruleData.goToLogin(v.getContext(), universityData);
    }

    /**
     * ViewHolder for selected universities (group item).
     */
    public static class UniversityViewHolder extends GroupViewHolder {
        public final TextView tvUniversityName;
        public final TextView tvSection;
        public final LinearLayout llSectionWrapper;
        public final TextView tvSelectRuleHint;

        public UniversityViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            tvUniversityName = (TextView) itemView.findViewById(R.id.tv_university_name);
            tvSection = (TextView) itemView.findViewById(R.id.tv_university_section);
            llSectionWrapper = (LinearLayout) itemView.findViewById(R.id.ll_university_section_wrapper);
            tvSelectRuleHint = (TextView) itemView.findViewById(R.id.tv_select_rule_hint);
            itemView.setOnClickListener(clickListener);
        }
    }

    /**
     * Click listener to retry getUniversities request.
     */
    private Button.OnClickListener tryAgainListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            showError(null);
            showLoadingAnimation(true);

            // load universities from server
            MainServiceHelper mainServiceHelper = new MainServiceHelper(v.getContext());
            mainServiceHelper.getUniversities(true);
        }
    };

    /**
     * ViewHolder for the header view.
     */
    public static class HeaderViewHolder extends GroupViewHolder{
        public final TextView tvHeader;
        public final ProgressWheel progressWheel;
        public final LinearLayout llErrorWrapper;
        public final TextView tvErrorMessage;
        public final Button btnTryAgain;

        public HeaderViewHolder(View rootView, Button.OnClickListener tryAgainListener) {
            super(rootView);
            tvHeader = (TextView) rootView.findViewById(R.id.tv_university_header);
            progressWheel = (ProgressWheel) rootView.findViewById(R.id.progress_wheel);
            llErrorWrapper = (LinearLayout) rootView.findViewById(R.id.ll_error_wrapper);
            tvErrorMessage = (TextView) rootView.findViewById(R.id.tv_error_message);
            btnTryAgain = (Button) rootView.findViewById(R.id.btn_try_again);
            btnTryAgain.setOnClickListener(tryAgainListener);
        }
    }

    /**
     * ViewHolder for selectable rules (child item).
     */
    public static class RuleViewHolder extends AbstractExpandableItemViewHolder  {
        public final TextView tvRuleName;

        public RuleViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            tvRuleName = (TextView) itemView.findViewById(R.id.tv_rule_name);
            itemView.setOnClickListener(clickListener);
        }
    }

    /**
     * ViewHolder for footer. 'university not found?'
     */
    public static class FooterViewHolder extends GroupViewHolder {
        public final LinearLayout llContainer;

        public FooterViewHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);
            llContainer = (LinearLayout) itemView.findViewById(R.id.container);
            itemView.setOnClickListener(clickListener);
        }
    }

    public static abstract class GroupViewHolder extends AbstractExpandableItemViewHolder {
        public GroupViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * Shows or hide the loading animation.
     *
     * @param isLoading true, if animation should be shown.
     */
    public void showLoadingAnimation(boolean isLoading) {
        // ignore if nothing would change to avoid notifyItemChanged
        if (dataProvider.getHeader().isLoading() == isLoading) {
            return;
        }

        dataProvider.getHeader().setIsLoading(isLoading);
        expandableItemManager.notifyGroupItemChanged(0);
    }

    /**
     * Shows the error wrapper and hides the loading animation.
     *
     * @param errorType ErrorEvent.ErrorType
     */
    public void showError(ErrorEvent.ErrorType errorType) {
        // ignore if nothing would change to avoid notifyItemChanged
        if (dataProvider.getHeader().getActErrorType() == errorType) {
            return;
        }

        dataProvider.getHeader().setActErrorType(errorType);

        if (errorType != null) {
            dataProvider.getHeader().setIsLoading(false);
        }
        expandableItemManager.notifyGroupItemChanged(0);
    }

    /**
     * The current EventError.ErrorType.
     *
     * @return current shown EventError.ErrorType or null.
     */
    public ErrorEvent.ErrorType getActErrorType() {
        return dataProvider.getHeader().getActErrorType();
    }

    public void showFooter() {
        dataProvider.getFooter().setVisible(true);
    }

    /**
     * Checks if list contains no universities.
     * It's important to notice that the list always contains the header and footer view.
     *
     * @return true, if list only contains the header view
     */
    public boolean isEmpty() {
        return dataProvider.getGroupCount() == 2;
    }
}
