package dh.mygrades.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dh.mygrades.R;
import dh.mygrades.database.dao.GradeEntry;
import dh.mygrades.main.MainServiceHelper;
import dh.mygrades.util.SemesterMapper;

/**
 * Helper class for the grade detail edit mode. It is directly tied to the GradeDetailedActivity.
 */
public class GradeDetailedActivityEditHelper {
    private static final String TAG = GradeDetailedActivityEditHelper.class.getSimpleName();

    // instance state
    public static final String ET_EXAM_ID_TEXT = "et_exam_id_text";
    public static final String ET_STATE_TEXT = "et_state_text";
    public static final String ET_CREDIT_POINTS_TEXT = "et_credit_points_text";
    public static final String ET_GRADE_TEXT = "et_grade_text";
    public static final String ET_ANNOTATION_TEXT = "et_annotation_text";
    public static final String ET_EXAM_DATE_TEXT = "et_exam_date_text";
    public static final String ET_TESTER_TEXT = "et_tester_text";
    public static final String ET_WEIGHT_TEXT = "et_weight_text";
    public static final String ET_NAME_TEXT = "et_name_text";
    public static final String SP_ATTEMPT_POSITION = "sp_attempt_position";
    public static final String SP_SEMESTER_POSITION = "sp_semester_position";

    private Activity activity;
    private MainServiceHelper mainServiceHelper;
    private boolean editModeEnabled;
    private GradeEntry gradeEntry;
    private Map<String, Integer> semesterToNumberMap;
    private SemesterMapper semesterMapper;

    // editable views
    private EditText etExamId;
    private EditText etState;
    private EditText etCreditPoints;
    private EditText etGrade;
    private EditText etAnnotation;
    private EditText etExamDate;
    private EditText etTester;
    private EditText etWeight;
    private EditText etName;
    private ImageView ivIncreaseWeight;
    private ImageView ivDecreaseWeight;

    private Spinner spAttempt;
    private ArrayAdapter<CharSequence> spAttemptAdapter;

    private Spinner spSemester;
    private ArrayAdapter<String> spSemesterAdapter;

    // other views
    private LinearLayout llModifiedHint;

    public GradeDetailedActivityEditHelper(Activity activity) {
        this.activity = activity;
        mainServiceHelper = new MainServiceHelper(activity);
        semesterMapper = new SemesterMapper();
    }

    public void init() {
        initViews();
        initListener();
    }

    /**
     * Initialize all views.
     */
    private void initViews() {
        llModifiedHint = (LinearLayout) activity.findViewById(R.id.ll_modified_hint);

        // init edit texts
        etExamId = (EditText) activity.findViewById(R.id.et_grade_detail_exam_id);
        etTester = (EditText) activity.findViewById(R.id.et_grade_detail_tester);
        etState = (EditText) activity.findViewById(R.id.et_grade_detail_state);
        etCreditPoints = (EditText) activity.findViewById(R.id.et_grade_detail_credit_points);
        etGrade = (EditText) activity.findViewById(R.id.et_grade_detail_grade);
        etAnnotation = (EditText) activity.findViewById(R.id.et_grade_detail_annotation);
        etExamDate = (EditText) activity.findViewById(R.id.et_grade_detail_exam_date);
        etName = (EditText) activity.findViewById(R.id.et_grade_detail_name);
        etWeight = (EditText) activity.findViewById(R.id.et_grade_detail_weight);
        ivIncreaseWeight = (ImageView) activity.findViewById(R.id.iv_increase_weight);
        ivDecreaseWeight = (ImageView) activity.findViewById(R.id.iv_decrease_weight);

        // init attempt spinner
        spAttempt = (Spinner) activity.findViewById(R.id.sp_grade_detail_attempt);
        spAttemptAdapter = ArrayAdapter.createFromResource(activity, R.array.grade_attempt_spinner, R.layout.grade_detail_spinner_item);
        spAttemptAdapter.setDropDownViewResource(R.layout.grade_detail_spinner_dropdown_item);
        spAttempt.setAdapter(spAttemptAdapter);

        // init semester spinner
        spSemester = (Spinner) activity.findViewById(R.id.sp_grade_detail_semester);
        List<String> semesterList = new ArrayList<>(semesterToNumberMap.keySet());
        semesterMapper.sortSemesterList(semesterList, false);
        spSemesterAdapter = new ArrayAdapter<>(activity, R.layout.grade_detail_spinner_item, semesterList);
        spSemesterAdapter.setDropDownViewResource(R.layout.grade_detail_spinner_dropdown_item);
        spSemester.setAdapter(spSemesterAdapter);
    }

    /**
     * Initialize listener.
     */
    private void initListener() {
        View.OnFocusChangeListener etOnFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (v instanceof EditText) {
                        final EditText editText = (EditText) v;
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                editText.setSelection(editText.getText().length());
                            }
                        }, 10);
                    }
                }
            }
        };

        etExamId.setOnFocusChangeListener(etOnFocusChangeListener);
        etTester.setOnFocusChangeListener(etOnFocusChangeListener);
        etState.setOnFocusChangeListener(etOnFocusChangeListener);
        etWeight.setOnFocusChangeListener(etOnFocusChangeListener);
        etCreditPoints.setOnFocusChangeListener(etOnFocusChangeListener);
        etGrade.setOnFocusChangeListener(etOnFocusChangeListener);
        etAnnotation.setOnFocusChangeListener(etOnFocusChangeListener);
        etExamDate.setOnFocusChangeListener(etOnFocusChangeListener);

        ivIncreaseWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseWeight();
            }
        });

        ivDecreaseWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseWeight();
            }
        });
    }

    /**
     * Enables or disables the edit mode. If enabled, all views will be shown.
     *
     * @param enable enable edit mode
     */
    public void enableEditMode(boolean enable) {
        editModeEnabled = enable;

        // enable / disable all views
        etExamId.setEnabled(editModeEnabled);
        etTester.setEnabled(editModeEnabled);
        etState.setEnabled(editModeEnabled);
        etWeight.setEnabled(editModeEnabled);
        etCreditPoints.setEnabled(editModeEnabled);
        etCreditPoints.setError(null);
        etGrade.setEnabled(editModeEnabled);
        etGrade.setError(null);
        etAnnotation.setEnabled(editModeEnabled);
        etName.setEnabled(editModeEnabled);
        etExamDate.setEnabled(editModeEnabled);
        spAttempt.setEnabled(editModeEnabled);
        spSemester.setEnabled(editModeEnabled);
        ivIncreaseWeight.setVisibility(editModeEnabled ? View.VISIBLE : View.GONE);
        ivDecreaseWeight.setVisibility(editModeEnabled ? View.VISIBLE : View.GONE);

        // show all views if edit mode is enabled
        if (editModeEnabled) {
            ((View) etExamId.getParent()).setVisibility(View.VISIBLE);
            ((View) etTester.getParent()).setVisibility(View.VISIBLE);
            ((View) etState.getParent()).setVisibility(View.VISIBLE);
            ((View) etWeight.getParent()).setVisibility(View.VISIBLE);
            ((View) etCreditPoints.getParent()).setVisibility(View.VISIBLE);
            ((View) etGrade.getParent()).setVisibility(View.VISIBLE);
            ((View) etAnnotation.getParent()).setVisibility(View.VISIBLE);
            ((View) etExamDate.getParent()).setVisibility(View.VISIBLE);
            ((View) spAttempt.getParent()).setVisibility(View.VISIBLE);
            ((View) spSemester.getParent()).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates the text value for each edit text, spinner etc.
     * If a modified value is used anywhere, a hint will be shown.
     */
    public void updateValues() {
        boolean modified;

        modified = updateEditText(etExamId, gradeEntry.getExamId(), gradeEntry.getModifiedExamId(), R.id.modified_badge_exam_id, false);
        modified = updateEditText(etTester, gradeEntry.getTester(), gradeEntry.getModifiedTester(), R.id.modified_badge_tester, false) || modified;
        modified = updateEditText(etState, gradeEntry.getState(), gradeEntry.getModifiedState(), R.id.modified_badge_state, false) || modified;
        modified = updateEditText(etCreditPoints, gradeEntry.getCreditPoints(), gradeEntry.getModifiedCreditPoints(), R.id.modified_badge_credit_points, true) || modified;
        modified = updateEditText(etGrade, gradeEntry.getGrade(), gradeEntry.getModifiedGrade(), R.id.modified_badge_grade, true) || modified;
        modified = updateEditText(etAnnotation, gradeEntry.getAnnotation(), gradeEntry.getModifiedAnnotation(), R.id.modified_badge_annotation, false) || modified;
        modified = updateEditText(etExamDate, gradeEntry.getExamDate(), gradeEntry.getModifiedExamDate(), R.id.modified_badge_exam_date, false) || modified;
        modified = updateEditText(etName, gradeEntry.getName(), gradeEntry.getModifiedName(), -1, true) || modified;
        modified = updateWeightEditText() || modified;
        modified = updateAttemptSpinner() || modified;
        modified = updateSemesterSpinner() || modified;

        // show or hide modified hint only if this entry is not a custom one
        if (!gradeEntry.isCustomGradeEntry()) {
            llModifiedHint.setVisibility(modified ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Updates the value of a given edit text. If the modified value is used, a badge will be shown.
     * If the value and modified value are null, the parent views visibility is set to GONE.
     *
     * @param et edit text
     * @param value string value
     * @param modifiedValue modified string value
     * @param resIdModifiedBadge resource id for 'modified badge'
     * @return true, if the modified value is used
     */
    private boolean updateEditText(EditText et, String value, String modifiedValue, int resIdModifiedBadge, boolean forcedVisible) {
        boolean modified = false;
        if (modifiedValue != null) {
            value = modifiedValue;
            modified = true;
        }

        ViewGroup parent = (ViewGroup)et.getParent();
        if (value != null || forcedVisible) {
            et.setText(value);
            parent.setVisibility(View.VISIBLE);
        } else {
            parent.setVisibility(View.GONE);
        }

        showBadge(parent, modified, resIdModifiedBadge);

        return modified;
    }

    /**
     * Updates the value of a given edit text. If the modified value is used, a badge will be shown.
     * If the value and modified value are null, the parent views visibility is set to GONE.
     *
     * @param et edit text
     * @param value double value
     * @param modifiedValue modified double value
     * @param resIdModifiedBadge resource id for 'modified bagde'
     * @param forcedVisible force parent view to be visible, despite null value
     * @return true, if the modified value is used
     */
    private boolean updateEditText(EditText et, Double value, Double modifiedValue, int resIdModifiedBadge, boolean forcedVisible) {
        boolean modified = false;
        if (modifiedValue != null) {
            value = modifiedValue;
            modified = true;
        }

        ViewGroup parent = (ViewGroup)et.getParent();
        if (value != null || forcedVisible) {
            writeDoubleToEditText(et, value);
            parent.setVisibility(View.VISIBLE);
        } else {
            parent.setVisibility(View.GONE);
        }

        showBadge(parent, modified, resIdModifiedBadge);

        return modified;
    }

    /**
     * Updates the value of the weight edit text. If weight != 1, it is assumed that the value
     * is modified.
     *
     * @return true, if the weight was modified
     */
    private boolean updateWeightEditText() {
        double weight = gradeEntry.getWeight() == null ? 1 : gradeEntry.getWeight();
        boolean modified = weight != 1;

        etWeight.setText(String.format("%.1f", weight));
        ViewGroup parent = (ViewGroup)etWeight.getParent().getParent();
        parent.setVisibility(View.VISIBLE);

        showBadge(parent, modified, R.id.modified_badge_weight);

        return modified;
    }

    /**
     * Updates the selected value of the attempt spinner.
     *
     * @return true, if the attempt was modified
     */
    private boolean updateAttemptSpinner() {
        boolean modified = false;
        String attempt = gradeEntry.getAttempt();
        String modifiedAttempt = gradeEntry.getModifiedAttempt();
        if (modifiedAttempt != null) {
            attempt = modifiedAttempt;
            modified = true;
        }

        ViewGroup parent = (ViewGroup) spAttempt.getParent();
        if (attempt == null) {
            parent.setVisibility(View.GONE);
        } else {
            int position = spAttemptAdapter.getPosition(attempt);
            spAttempt.setSelection(position);
            parent.setVisibility(View.VISIBLE);
        }

        showBadge(parent, modified, R.id.modified_badge_attempt);

        return modified;
    }

    /**
     * Updates the selected value of the semester spinner.
     *
     * @return true, if the semester was modified
     */
    private boolean updateSemesterSpinner() {
        boolean modified = false;
        String semester = gradeEntry.getSemester();
        String modifiedSemester = gradeEntry.getModifiedSemester();
        if (modifiedSemester != null) {
            semester = modifiedSemester;
            modified = true;
        }

        ViewGroup parent = (ViewGroup) spSemester.getParent();
        if (semester == null) {
            parent.setVisibility(View.GONE);
        } else {
            int position = spSemesterAdapter.getPosition(semester);
            spSemester.setSelection(position);
            parent.setVisibility(View.VISIBLE);
        }

        showBadge(parent, modified, R.id.modified_badge_semester);

        return modified;
    }

    /**
     * Writes a double to an edit text. If the value is null, "-" will be shown.
     * (only necessary for views with forced visibility)
     *
     * @param et edit text
     * @param value double value
     */
    private void writeDoubleToEditText(EditText et, Double value) {
        String valueAsString = value == null ? "-" : String.format("%.1f", value);
        et.setText(valueAsString);
    }

    /**
     * Shows a badge, if modified = true.
     *
     * @param parent parent view containing the badge view
     * @param modified modified
     * @param resIdModifiedBadge resource id for badge view inside parent view
     */
    private void showBadge(ViewGroup parent, boolean modified, int resIdModifiedBadge) {
        if (resIdModifiedBadge < 0) return;
        if (gradeEntry.isCustomGradeEntry()) return;

        View badge = parent.findViewById(resIdModifiedBadge);
        if (badge != null) {
            badge.setVisibility(modified ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Checks each input whether the value differs from the original one.
     * If one or more values were modified, update the GradeEntry in th database.
     */
    public void saveEdits() {
        boolean modified;

        modified = updateExamId();
        modified = updateWeight() || modified;
        modified = updateTester() || modified;
        modified = updateState() || modified;
        modified = updateAnnotation() || modified;
        modified = updateExamDate() || modified;
        modified = updateGrade() || modified;
        modified = updateCreditPoints() || modified;
        modified = updateAttempt() || modified;
        modified = updateSemester() || modified;
        modified = updateName() || modified;

        if (modified) {
            mainServiceHelper.updateGradeEntry(gradeEntry);
        }

        // update ui to hide empty properties
        updateValues();
    }

    /**
     * Checks if the entered examId differs from the original value and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateExamId() {
        String examId = gradeEntry.getExamId();
        String modifiedExamId = etExamId.getText().toString().trim();
        modifiedExamId = modifiedExamId.length() == 0 ? null : modifiedExamId;
        if (examId == null || !examId.equals(modifiedExamId)) {
            gradeEntry.setModifiedExamId(modifiedExamId);
            return true;
        } else if (examId.equals(modifiedExamId)) {
            gradeEntry.setModifiedExamId(null);
            return true;
        }

        return false;
    }

    /**
     * Checks if the entered weight differs from the original weight and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateWeight() {
        Double weight = gradeEntry.getWeight();
        String weightInput = etWeight.getText().toString().trim();
        Double modifiedWeight = weightInput.length() == 0 ? 1 : stringToDouble(weightInput);
        if (weight == null || !weight.equals(modifiedWeight)) {
            gradeEntry.setWeight(modifiedWeight);
            return true;
        }
        return false;
    }

    /**
     * Checks if the entered tester differs from the original one and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateTester() {
        String tester = gradeEntry.getTester();
        String modifiedTester = etTester.getText().toString().trim();
        modifiedTester = modifiedTester.length() == 0 ? null : modifiedTester;
        if (tester == null || !tester.equals(modifiedTester)) {
            gradeEntry.setModifiedTester(modifiedTester);
            return true;
        } else if (tester.equals(modifiedTester)) {
            gradeEntry.setModifiedTester(null);
            return true;
        }
        return false;
    }

    /**
     * Checks if the entered state differs from the original one and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateState() {
        String state = gradeEntry.getState();
        String modifiedState = etState.getText().toString().trim();
        modifiedState = modifiedState.length() == 0 ? null : modifiedState;
        if (state == null || !state.equals(modifiedState)) {
            gradeEntry.setModifiedState(modifiedState);
            return true;
        } else if (state.equals(modifiedState)) {
            gradeEntry.setModifiedState(null);
            return true;
        }
        return false;
    }

    /**
     * Checks if the entered annotation differs from the original one and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateAnnotation() {
        String annotation = gradeEntry.getAnnotation();
        String modifiedAnnotation = etAnnotation.getText().toString().trim();
        modifiedAnnotation = modifiedAnnotation.length() == 0 ? null : modifiedAnnotation;
        if (annotation == null || !annotation.equals(modifiedAnnotation)) {
            gradeEntry.setModifiedAnnotation(modifiedAnnotation);
            return true;
        } else if (annotation.equals(modifiedAnnotation)) {
            gradeEntry.setModifiedAnnotation(null);
            return true;
        }
        return false;
    }

    /**
     * Checks if the entered exam date differs from the original one and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateExamDate() {
        String examDate = gradeEntry.getExamDate();
        String modifiedExamDate = etExamDate.getText().toString().trim();
        modifiedExamDate = modifiedExamDate.length() == 0 ? null : modifiedExamDate;
        if (examDate == null || !examDate.equals(modifiedExamDate)) {
            gradeEntry.setModifiedExamDate(modifiedExamDate);
            return true;
        } else if (examDate.equals(modifiedExamDate)) {
            gradeEntry.setModifiedExamDate(null);
            return true;
        }
        return false;
    }

    /**
     * Checks if the entered grade differs from the original one and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateGrade() {
        Double grade = gradeEntry.getGrade();
        String modifiedGradeAsString = etGrade.getText().toString().trim();
        modifiedGradeAsString = modifiedGradeAsString.equals("-") ? null : modifiedGradeAsString;
        if (modifiedGradeAsString != null) {
            modifiedGradeAsString = modifiedGradeAsString.length() == 0 ? null : modifiedGradeAsString;
        }

        if (modifiedGradeAsString == null) {
            gradeEntry.setModifiedGrade(null);
            return true;
        } else {
            Double modifiedGrade = stringToDouble(modifiedGradeAsString);
            if (modifiedGrade == null || modifiedGrade < 0 || modifiedGrade > 5) {
                etGrade.setError(activity.getString(R.string.et_grade_invalid));
            } else if (grade == null || !grade.equals(modifiedGrade)) {
                gradeEntry.setModifiedGrade(modifiedGrade);
                return true;
            } else if (grade.equals(modifiedGrade)) {
                gradeEntry.setModifiedGrade(null);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the entered credit points differs from the original one and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateCreditPoints() {
        Double creditPoints = gradeEntry.getCreditPoints();
        String modifiedCreditPointsAsString = etCreditPoints.getText().toString().trim();
        modifiedCreditPointsAsString = modifiedCreditPointsAsString.equals("-") ? null : modifiedCreditPointsAsString;
        if (modifiedCreditPointsAsString != null) {
            modifiedCreditPointsAsString = modifiedCreditPointsAsString.length() == 0 ? null : modifiedCreditPointsAsString;
        }

        if (modifiedCreditPointsAsString == null) {
            gradeEntry.setModifiedCreditPoints(null);
            return true;
        } else {
            Double modifiedCreditPoints = stringToDouble(modifiedCreditPointsAsString);
            if (modifiedCreditPoints == null || modifiedCreditPoints < 0) {
                etCreditPoints.setError(activity.getString(R.string.et_credit_points_invalid));
            } else if (creditPoints == null || !creditPoints.equals(modifiedCreditPoints)) {
                gradeEntry.setModifiedCreditPoints(modifiedCreditPoints);
                return true;
            } else if (creditPoints.equals(modifiedCreditPoints)) {
                gradeEntry.setModifiedCreditPoints(null);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the selected attempt differs from the original one and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateAttempt() {
        String attempt = gradeEntry.getAttempt();
        String modifiedAttempt = (String) spAttempt.getSelectedItem();
        modifiedAttempt = modifiedAttempt.equals("0") ? null : modifiedAttempt;
        if (modifiedAttempt != null) {
            modifiedAttempt = modifiedAttempt.length() == 0 ? null : modifiedAttempt;
        }

        if (attempt == null || !attempt.equals(modifiedAttempt)) {
            gradeEntry.setModifiedAttempt(modifiedAttempt);
            return true;
        } else if (attempt.equals(modifiedAttempt)) {
            gradeEntry.setModifiedAttempt(null);
            return true;
        }

        return false;
    }

    /**
     * Checks if the selected semester differs from the original one and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateSemester() {
        String semester = gradeEntry.getSemester();
        semester = semester == null ? "" : semester;
        String modifiedSemester = (String) spSemester.getSelectedItem();

        if (semester.equals(modifiedSemester)) {
            gradeEntry.setModifiedSemester(null);
            return true;
        } else if (!semester.equals(modifiedSemester)) {
            gradeEntry.setModifiedSemester(modifiedSemester);
            return true;
        }

        return false;
    }

    /**
     * Checks if the entered name differs from the original one and updates the gradeEntry.
     *
     * @return true, if gradeEntry must be updated in the database
     */
    private boolean updateName() {
        String name = gradeEntry.getName();
        String modifiedName = etName.getText().toString().trim();
        modifiedName = modifiedName.length() == 0 ? null : modifiedName;
        if (name == null || !name.equals(modifiedName)) {
            gradeEntry.setModifiedName(modifiedName);
            return true;
        } else if (name.equals(modifiedName)) {
            gradeEntry.setModifiedName(null);
            return true;
        }

        return false;
    }

    /**
     * Resets all modified properties to restores the original one.
     */
    public void restore() {
        gradeEntry.setModifiedCreditPoints(null);
        gradeEntry.setModifiedAttempt(null);
        gradeEntry.setModifiedGrade(null);
        gradeEntry.setModifiedSemester(null);
        gradeEntry.setModifiedTester(null);
        gradeEntry.setModifiedAnnotation(null);
        gradeEntry.setModifiedExamDate(null);
        gradeEntry.setModifiedExamId(null);
        gradeEntry.setModifiedName(null);
        gradeEntry.setModifiedState(null);
        gradeEntry.setWeight(1.0);

        mainServiceHelper.updateGradeEntry(gradeEntry);
        updateValues();

        enableEditMode(false);
    }

    /**
     * Deletes this grade entry.
     */
    public void delete() {
        mainServiceHelper.deleteGradeEntry(gradeEntry.getHash());
    }

    /**
     * Increases the current weight by 1.
     */
    private void increaseWeight() {
        Double actWeight = gradeEntry.getWeight();

        if (actWeight != null) {
            actWeight += 1.0;
            gradeEntry.setWeight(actWeight);
            updateWeightEditText();
        }
    }

    /**
     * Decreases the current weight by 1.
     */
    private void decreaseWeight() {
        Double actWeight = gradeEntry.getWeight();

        if (actWeight != null && actWeight >= 1.0) {
            actWeight -= 1.0;
            gradeEntry.setWeight(actWeight);
            updateWeightEditText();
        }
    }

    /**
     * Converts a string to double.
     * If the string is empty or the parsing fails, null will be returned.
     *
     * @param s - string to convert
     * @return Double or null
     */
    private Double stringToDouble(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }

        s = s.replace(',', '.');

        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    /**
     * Get the instance state for all edit texts and spinner.
     *
     * @return instance state as Bundle
     */
    public Bundle getInstanceState() {
        Bundle state = new Bundle();
        state.putString(ET_EXAM_ID_TEXT, etExamId.getText().toString());
        state.putString(ET_STATE_TEXT, etState.getText().toString());
        state.putString(ET_CREDIT_POINTS_TEXT, etCreditPoints.getText().toString());
        state.putString(ET_GRADE_TEXT, etGrade.getText().toString());
        state.putString(ET_ANNOTATION_TEXT, etAnnotation.getText().toString());
        state.putString(ET_EXAM_DATE_TEXT, etExamDate.getText().toString());
        state.putString(ET_TESTER_TEXT, etTester.getText().toString());
        state.putString(ET_WEIGHT_TEXT, etWeight.getText().toString());
        state.putString(ET_NAME_TEXT, etName.getText().toString());
        state.putInt(SP_ATTEMPT_POSITION, spAttempt.getSelectedItemPosition());
        state.putInt(SP_SEMESTER_POSITION, spSemester.getSelectedItemPosition());

        return state;
    }

    /**
     * Restore the instance state for all edit texts and spinner.
     *
     * @param savedState Bundle
     */
    public void restoreInstanceState(Bundle savedState) {
        if (savedState == null) return;

        etExamId.setText(savedState.getString(ET_EXAM_ID_TEXT));
        etState.setText(savedState.getString(ET_STATE_TEXT));
        etCreditPoints.setText(savedState.getString(ET_CREDIT_POINTS_TEXT));
        etGrade.setText(savedState.getString(ET_GRADE_TEXT));
        etAnnotation.setText(savedState.getString(ET_ANNOTATION_TEXT));
        etExamDate.setText(savedState.getString(ET_EXAM_DATE_TEXT));
        etTester.setText(savedState.getString(ET_TESTER_TEXT));
        etWeight.setText(savedState.getString(ET_WEIGHT_TEXT));
        etName.setText(savedState.getString(ET_NAME_TEXT));
        spAttempt.setSelection(savedState.getInt(SP_ATTEMPT_POSITION));
        spSemester.setSelection(savedState.getInt(SP_SEMESTER_POSITION));
    }

    public void setGradeEntry(GradeEntry gradeEntry) {
        this.gradeEntry = gradeEntry;
    }

    public boolean isEditModeEnabled() {
        return editModeEnabled;
    }

    public void setSemesterToNumberMap(Map<String, Integer> semesterToNumberMap) {
        this.semesterToNumberMap = semesterToNumberMap;
    }

    public boolean isCustomGradeEntry() {
        return gradeEntry.getGeneratedId() != null;
    }
}
