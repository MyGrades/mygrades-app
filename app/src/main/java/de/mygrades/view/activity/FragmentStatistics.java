package de.mygrades.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.StatisticsEvent;
import de.mygrades.view.adapter.model.SemesterItem;

/**
 * Fragment to show statistics.
 */
public class FragmentStatistics extends Fragment {

    private TextView tvAverage;
    private TextView tvCreditPoints;
    private TextView tvCreditPointsPerSemester;
    private TextView tvStudyProgress;
    private TextView tvGradeCount;

    private LineChart chartCreditPoints;
    private LineChart chartCreditPointsPerSemester;
    private LineChart chartAverageGradePerSemester;

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

        chartCreditPoints = (LineChart) view.findViewById(R.id.chart_credit_points);
        chartCreditPointsPerSemester = (LineChart) view.findViewById(R.id.chart_credit_points_per_semester);
        chartAverageGradePerSemester = (LineChart) view.findViewById(R.id.chart_average_grade_per_semester);

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
        tvAverage.setText("Ø " + String.format("%.2f", statisticsEvent.getAverage()));
        tvCreditPoints.setText("Σ " + String.format("%.1f", statisticsEvent.getCreditPoints()) + " CP");
        tvCreditPointsPerSemester.setText("Ø " + String.format("%.1f", statisticsEvent.getCreditPointsPerSemester()) + " CP");
        tvStudyProgress.setText(String.format("%.1f", statisticsEvent.getStudyProgress()) + "%");
        tvGradeCount.setText("" + statisticsEvent.getGradeCount());

        initCreditPointsChart(statisticsEvent.getSemesterItems());
        initCreditPointsPerSemesterChart(statisticsEvent.getSemesterItems());
        initAverageGradePerSemesterChart(statisticsEvent.getSemesterItems());
    }

    /**
     * Initializes the credit points line chart.
     *
     * @param semesterItems list of semester items
     */
    private void initCreditPointsChart(List<SemesterItem> semesterItems) {
        List<String> xVals = new ArrayList<>();
        for(SemesterItem item : semesterItems) {
            xVals.add("" + item.getSemesterNumber());
        }

        List<Entry> yVals = new ArrayList<>();
        float sum = 0;
        for(int i = 0; i < semesterItems.size(); i++) {
            sum += semesterItems.get(i).getCreditPoints();
            yVals.add(new Entry(sum, i));
        }

        LineDataSet dataset = new LineDataSet(yVals, "");
        LineData data = new LineData(xVals, dataset);
        chartCreditPoints.setData(data);

        setLineChartStyle(chartCreditPoints, dataset);
        chartCreditPoints.invalidate();
    }

    /**
     * Initializes the credit points per semester line chart.
     *
     * @param semesterItems list of semester items
     */
    private void initCreditPointsPerSemesterChart(List<SemesterItem> semesterItems) {
        List<String> xVals = new ArrayList<>();
        for(SemesterItem item : semesterItems) {
            xVals.add("" + item.getSemesterNumber());
        }

        List<Entry> yVals = new ArrayList<>();
        for(int i = 0; i < semesterItems.size(); i++) {
            yVals.add(new Entry(semesterItems.get(i).getCreditPoints(), i));
        }

        LineDataSet dataset = new LineDataSet(yVals, "");
        LineData data = new LineData(xVals, dataset);
        chartCreditPointsPerSemester.setData(data);

        setLineChartStyle(chartCreditPointsPerSemester, dataset);
        chartCreditPointsPerSemester.invalidate();
    }

    /**
     * Initializes the average grade per semester line chart.
     *
     * @param semesterItems list of semester items
     */
    private void initAverageGradePerSemesterChart(List<SemesterItem> semesterItems) {
        List<String> xVals = new ArrayList<>();
        for(SemesterItem item : semesterItems) {
            xVals.add("" + item.getSemesterNumber());
        }

        List<Entry> yVals = new ArrayList<>();
        for(int i = 0; i < semesterItems.size(); i++) {
            yVals.add(new Entry(semesterItems.get(i).getAverage(), i));
        }

        LineDataSet dataset = new LineDataSet(yVals, "");
        LineData data = new LineData(xVals, dataset);
        chartAverageGradePerSemester.setData(data);

        setLineChartStyle(chartAverageGradePerSemester, dataset);
        chartAverageGradePerSemester.invalidate();
    }

    /**
     * Sets basic lLineChart and DataSet styles.
     *
     * @param chart LineChart where basic styles will be applied.
     * @param dataSet DataSet where basic styles will be applied.
     */
    private void setLineChartStyle(LineChart chart, LineDataSet dataSet) {
        // general style
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDescription("");
        chart.setDrawBorders(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        chart.setExtraLeftOffset(16);
        chart.setExtraRightOffset(16);

        // set x-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.text87));

        // show left y-axis
        YAxis leftYAxis = chart.getAxisLeft();
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.divider));
        leftYAxis.setDrawAxisLine(false);
        leftYAxis.setDrawLabels(false);

        // hide right y-axis
        YAxis rightYAxis = chart.getAxisRight();
        rightYAxis.setDrawGridLines(false);
        rightYAxis.setDrawAxisLine(false);
        rightYAxis.setDrawLabels(false);

        // style dataSet
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleSize(5);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }
}
