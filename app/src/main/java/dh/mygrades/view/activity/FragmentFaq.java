package dh.mygrades.view.activity;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import dh.mygrades.R;
import dh.mygrades.view.adapter.FaqExpandableItemAdapter;
import dh.mygrades.view.adapter.dataprovider.FaqDataProvider;

/**
 * Fragment to show Frequently Asked Questions in an expandable RecyclerView.
 */
public class FragmentFaq extends Fragment {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewExpandableItemManager recyclerViewExpandableItemManager;

    // if this is set, the value (an integer) will be used
    // to determine which question should be expanded
    public static final String ARGUMENT_GO_TO_QUESTION = "attribute_go_to_question";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_faq, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // restore state if necessary
        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        recyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        // create data
        FaqDataProvider faqDataProvider = new FaqDataProvider();
        faqDataProvider.populateData(getContext());

        // create adapter
        FaqExpandableItemAdapter itemAdapter = new FaqExpandableItemAdapter(faqDataProvider);
        wrappedAdapter = recyclerViewExpandableItemManager.createWrappedAdapter(itemAdapter);

        // set animation stuff
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);

        // init recycler view
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(wrappedAdapter);  // requires *wrapped* adapter
        recyclerView.setItemAnimator(animator);
        recyclerView.setHasFixedSize(false);

        // set divider
        // recyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.grade_divider), true));

        // attach recycler view to item manager, necessary for touch listeners
        recyclerViewExpandableItemManager.attachRecyclerView(recyclerView);

        // jump to specific question if ARGUMENT_GO_TO_QUESTION is provided
        if (getArguments() != null) {
            int argumentQuestionId = getArguments().getInt(ARGUMENT_GO_TO_QUESTION);
            int groupId = faqDataProvider.getGroupId(argumentQuestionId);

            recyclerViewExpandableItemManager.expandGroup(groupId);
            recyclerView.scrollToPosition(groupId);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save state
        if (recyclerViewExpandableItemManager != null) {
            outState.putParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER, recyclerViewExpandableItemManager.getSavedState());
        }
    }

    @Override
    public void onDestroyView() {
        if (recyclerViewExpandableItemManager != null) {
            recyclerViewExpandableItemManager.release();
            recyclerViewExpandableItemManager = null;
        }

        if (recyclerView != null) {
            recyclerView.setItemAnimator(null);
            recyclerView.setAdapter(null);
            recyclerView = null;
        }

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
        }

        layoutManager = null;

        super.onDestroyView();
    }
}
