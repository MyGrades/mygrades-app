package de.mygrades.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import de.mygrades.R;
import de.mygrades.view.adapter.dataprovider.FaqDataProvider;
import de.mygrades.view.widget.ExpandableItemIndicator;

/**
 * FaqExpandableItemAdapter is the adapter for the FAQ recycler view.
 */
public class FaqExpandableItemAdapter extends AbstractExpandableItemAdapter<FaqExpandableItemAdapter.QuestionViewHolder, FaqExpandableItemAdapter.AnswerViewHolder>{

    private FaqDataProvider dataProvider;

    public FaqExpandableItemAdapter(FaqDataProvider dataProvider) {
        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);

        // set data provider
        this.dataProvider = dataProvider;
    }

    @Override
    public int getGroupCount() {
        return dataProvider.getGroupCount();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return dataProvider.getChildCount();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return dataProvider.getGroupItem(groupPosition).getGroupId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return dataProvider.getChildItem(groupPosition).getChildId();
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        return 0; // This method will not be called.
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0; // This method will not be called.
    }

    @Override
    public QuestionViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.item_faq_question, parent, false);
        return new QuestionViewHolder(v);
    }

    @Override
    public AnswerViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.item_faq_answer, parent, false);
        return new AnswerViewHolder(v);
    }

    @Override
    public void onBindGroupViewHolder(QuestionViewHolder holder, int groupPosition, int viewType) {
        FaqDataProvider.QuestionData group = dataProvider.getGroupItem(groupPosition);
        if (group.getSectionTitle() != null) {
            holder.tvFaqSection.setVisibility(View.VISIBLE);
            holder.tvFaqSection.setText(group.getSectionTitle());
        } else {
            holder.tvFaqSection.setText(null);
            holder.tvFaqSection.setVisibility(View.GONE);
        }

        holder.tvFaqHeader.setText(group.getQuestion());
        holder.itemView.setClickable(true);

        // set indicator state / animation
        final int expandState = holder.getExpandStateFlags();
        if ((expandState & ExpandableItemConstants.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;
            boolean isExpanded;
            boolean animateIndicator = ((expandState & ExpandableItemConstants.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED) != 0);

            if ((expandState & ExpandableItemConstants.STATE_FLAG_IS_EXPANDED) != 0) {
                bgResId = R.drawable.faq_question_item_bg_expanded_state;
                isExpanded = true;
            } else {
                bgResId = R.drawable.faq_question_item_bg_normal_state;
                isExpanded = false;
            }

            holder.container.setBackgroundResource(bgResId);
            holder.indicator.setExpandedState(isExpanded, animateIndicator);
        }
    }

    @Override
    public void onBindChildViewHolder(AnswerViewHolder holder, int groupPosition, int childPosition, int viewType) {
        FaqDataProvider.AnswerData child = dataProvider.getChildItem(groupPosition);

        // set answer text
        holder.tvFaqAnswer.setText(child.getAnswer());
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(QuestionViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        // check is enabled
        if (!(holder.itemView.isEnabled() && holder.itemView.isClickable())) {
            return false;
        }

        return true;
    }

    /**
     * ViewHolder for a question.
     */
    public static class QuestionViewHolder extends AbstractExpandableItemViewHolder {
        public TextView tvFaqHeader;
        public ExpandableItemIndicator indicator;
        public LinearLayout container;
        public TextView tvFaqSection;

        public QuestionViewHolder(View itemView) {
            super(itemView);
            container = (LinearLayout) itemView.findViewById(R.id.ll_container);
            tvFaqHeader = (TextView) itemView.findViewById(R.id.tv_faq_question);
            indicator = (ExpandableItemIndicator) itemView.findViewById(R.id.indicator);
            tvFaqSection = (TextView) itemView.findViewById(R.id.tv_faq_section);
        }
    }

    /**
     * ViewHolder for an answer.
     */
    public static class AnswerViewHolder extends AbstractExpandableItemViewHolder {
        public TextView tvFaqAnswer;

        public AnswerViewHolder(View itemView) {
            super(itemView);
            tvFaqAnswer = (TextView) itemView.findViewById(R.id.tv_faq_answer);
        }
    }
}
