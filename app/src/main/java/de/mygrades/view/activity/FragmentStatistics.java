package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.StatisticsEvent;

/**
 * Fragment to show statistics.
 */
public class FragmentStatistics extends Fragment {

    private TextView tvAverage;
    private TextView tvCreditPoints;
    private TextView tvCreditPointsPerSemester;
    private TextView tvStudyProgress;
    private TextView tvGradeCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        tvAverage = (TextView) view.findViewById(R.id.tv_average);
        tvCreditPoints = (TextView) view.findViewById(R.id.tv_credit_points);
        tvCreditPointsPerSemester = (TextView) view.findViewById(R.id.tv_credit_points_per_semester);
        tvStudyProgress = (TextView) view.findViewById(R.id.tv_study_progress);
        tvGradeCount = (TextView) view.findViewById(R.id.tv_grade_count);

        EventBus.getDefault().register(this);

        MainServiceHelper mainServiceHelper = new MainServiceHelper(getContext());
        mainServiceHelper.getStatistics();
    }

    /**
     * Receive StatisticsEvent and update UI.
     *
     * @param statisticsEvent - StatisticsEvent
     */
    public void onEventMainThread(StatisticsEvent statisticsEvent) {
        // TODO: proper formatting
        tvAverage.setText("" + statisticsEvent.getAverage());
        tvCreditPoints.setText("" + statisticsEvent.getCreditPoints());
        tvGradeCount.setText("" + statisticsEvent.getGradeCount());
        tvStudyProgress.setText("" + statisticsEvent.getStudyProgress());
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }
}
