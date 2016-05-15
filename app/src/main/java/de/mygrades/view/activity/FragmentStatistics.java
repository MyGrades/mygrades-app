package de.mygrades.view.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.StatisticsEvent;
import de.mygrades.view.adapter.model.SemesterItem;

/**
 * Fragment to show statistics.
 */
public class FragmentStatistics extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private TextView tvAverage;
    private TextView tvCreditPoints;
    private TextView tvCreditPointsPerSemester;
    private TextView tvStudyProgress;
    private TextView tvGradeCount;

    private LineChart chartCreditPoints;
    private LineChart chartCreditPointsPerSemester;
    private LineChart chartAverageGradePerSemester;
    private BarChart chartGradeDistribution;

    private MainServiceHelper mainServiceHelper;
    private Map<String, Integer> semesterNumberMap;
    private String actualFirstSemester;

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
        chartGradeDistribution = (BarChart) view.findViewById(R.id.chart_grade_distribution);

        EventBus.getDefault().register(this);

        mainServiceHelper = new MainServiceHelper(getContext());
        mainServiceHelper.getStatistics();

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Receive StatisticsEvent and update UI.
     *
     * @param statisticsEvent - StatisticsEvent
     */
    public void onEventMainThread(StatisticsEvent statisticsEvent) {
        semesterNumberMap = statisticsEvent.getSemesterToSemesterNumberMap();
        actualFirstSemester = statisticsEvent.getActualFirstSemester();

        tvAverage.setText("Ø " + String.format("%.2f", statisticsEvent.getAverage()));
        tvCreditPoints.setText("Σ " + String.format("%.1f", statisticsEvent.getCreditPoints()) + " CP");
        tvCreditPointsPerSemester.setText("Ø " + String.format("%.1f", statisticsEvent.getCreditPointsPerSemester()) + " CP");
        tvStudyProgress.setText(String.format("%.1f", statisticsEvent.getStudyProgress()) + "%");
        tvGradeCount.setText("" + statisticsEvent.getGradeCount());

        initCreditPointsChart(statisticsEvent.getSemesterItems());
        initCreditPointsPerSemesterChart(statisticsEvent.getSemesterItems(), statisticsEvent.getCreditPointsPerSemester());
        initAverageGradePerSemesterChart(statisticsEvent.getSemesterItems(), statisticsEvent.getAverage());
        initGradeDistributionChart(statisticsEvent.getGradeDistribution());
    }

    /**
     * Initializes the credit points line chart.
     *
     * @param semesterItems list of semester items
     */
    private void initCreditPointsChart(List<SemesterItem> semesterItems) {
        List<String> xVals = new ArrayList<>();
        for(SemesterItem item : semesterItems) {
            xVals.add("" + getCorrectSemesterNumber(item));
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

        setChartStyle(chartCreditPoints);
        setLineDataSetStyle(dataset);
        chartCreditPoints.animateY(800);
    }

    /**
     * Initializes the credit points per semester line chart.
     *
     * @param semesterItems list of semester items
     */
    private void initCreditPointsPerSemesterChart(List<SemesterItem> semesterItems, float creditPointsPerSemester) {
        List<String> xVals = new ArrayList<>();
        for(SemesterItem item : semesterItems) {
            xVals.add(""  + getCorrectSemesterNumber(item));
        }

        List<Entry> yVals = new ArrayList<>();
        for(int i = 0; i < semesterItems.size(); i++) {
            yVals.add(new Entry(semesterItems.get(i).getCreditPoints(), i));
        }

        // add limit line
        addLimitLine(chartCreditPointsPerSemester, creditPointsPerSemester, "%.1f");

        LineDataSet dataset = new LineDataSet(yVals, "");
        LineData data = new LineData(xVals, dataset);
        chartCreditPointsPerSemester.setData(data);

        setChartStyle(chartCreditPointsPerSemester);
        setLineDataSetStyle(dataset);
        chartCreditPointsPerSemester.invalidate();
    }

    /**
     * Initializes the average grade per semester line chart.
     *
     * @param semesterItems list of semester items
     */
    private void initAverageGradePerSemesterChart(List<SemesterItem> semesterItems, float averageGrade) {
        List<String> xVals = new ArrayList<>();
        for(SemesterItem item : semesterItems) {
            xVals.add("" + getCorrectSemesterNumber(item));
        }

        List<Entry> yVals = new ArrayList<>();
        for(int i = 0; i < semesterItems.size(); i++) {
            yVals.add(new Entry(semesterItems.get(i).getAverage(), i));
        }

        // add limit line
        addLimitLine(chartAverageGradePerSemester, averageGrade, "%.2f");

        LineDataSet dataset = new LineDataSet(yVals, "");
        LineData data = new LineData(xVals, dataset);
        data.setValueFormatter(new DecimalValueFormatter("#.00"));
        chartAverageGradePerSemester.setData(data);

        setChartStyle(chartAverageGradePerSemester);
        setLineDataSetStyle(dataset);
        chartAverageGradePerSemester.invalidate();
    }

    /**
     * Initializes the grade distribution bar chart.
     *
     * @param gradeDistribution array with grade distributions
     */
    private void initGradeDistributionChart(int[] gradeDistribution) {
        // y-values
        ArrayList<BarEntry> yValues = new ArrayList<>();
        for(int i = 0; i < gradeDistribution.length; i++) {
            yValues.add(new BarEntry(gradeDistribution[i], i));
        }

        // x-values
        ArrayList<String> xValues = new ArrayList<>();
        xValues.add("1,0 - 1,3");
        xValues.add("1,7 - 2,3");
        xValues.add("2,7 - 3,3");
        xValues.add("3,7 - 4,0");
        xValues.add("4,3 - 5,0");
        xValues.add("Andere");

        BarDataSet dataSet = new BarDataSet(yValues, "");
        dataSet.setValueTextSize(12);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.text87));
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dataSet.setDrawValues(true);
        dataSet.setBarSpacePercent(35);

        BarData barData = new BarData(xValues, dataSet);
        barData.setValueFormatter(new DecimalValueFormatter("#"));
        barData.setHighlightEnabled(false);
        chartGradeDistribution.setData(barData);

        setChartStyle(chartGradeDistribution);
        chartGradeDistribution.getXAxis().setLabelsToSkip(0);
        chartGradeDistribution.invalidate();
    }

    /**
     * Sets basic chart styles.
     *
     * @param chart LineChart where basic styles will be applied.
     */
    private void setChartStyle(BarLineChartBase<?> chart) {
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
        leftYAxis.setLabelCount(6, true);

        // hide right y-axis
        YAxis rightYAxis = chart.getAxisRight();
        rightYAxis.setDrawGridLines(false);
        rightYAxis.setDrawAxisLine(false);
        rightYAxis.setDrawLabels(false);
    }

    /**
     * Get the correct semester number for a given semester item.
     *
     * @param item semester item
     * @return correct semester number
     */
    private int getCorrectSemesterNumber(SemesterItem item) {
        int semesterNumber = semesterNumberMap.get(item.getSemester());
        int actualFirstSemesterNumber = semesterNumberMap.get(actualFirstSemester);
        return (semesterNumber - actualFirstSemesterNumber) + 1;
    }

    /**
     * Sets basic LineDataSet styles.
     *
     * @param dataSet LineDataSet
     */
    private void setLineDataSetStyle(LineDataSet dataSet) {
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleSize(5);
    }

    /**
     * Adds a LimitLine to the given chart.
     *
     * @param chart LineChart
     * @param value value for the LimitLine
     * @param format decimal format
     */
    private void addLimitLine(LineChart chart, float value, String format) {
        LimitLine ll = new LimitLine(value);
        ll.setLabel(String.format(format, value));
        ll.setLineColor(ContextCompat.getColor(getContext(), R.color.text87));
        ll.setLineWidth(0.2f);
        ll.setTextSize(11);
        ll.setTextColor(ContextCompat.getColor(getContext(), R.color.text87));
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.addLimitLine(ll);
        leftAxis.setDrawLimitLinesBehindData(true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getContext().getString(R.string.pref_key_simple_weighting))) {
            mainServiceHelper.getStatistics();
        }
    }

    /**
     * Custom DecimalValueFormatter.
     */
    private class DecimalValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        public DecimalValueFormatter(String format) {
            mFormat = new DecimalFormat(format);
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }
}
