package de.mygrades.view.adapter.dataprovider;

import android.content.Context;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.R;

/**
 * FaqDataProvider provides access to the underlying FAQ data.
 */
public class FaqDataProvider {
    private List<Pair<QuestionData, AnswerData>> data;

    public static final int GO_TO_GENERAL_ERROR = 100;
    public static final int GO_TO_WHY_NO_GRADING = 200;

    public FaqDataProvider() {
        data = new ArrayList<>();
    }

    /**
     * Reads questions and answers from string-array resources and fills the ArrayList.
     *
     * @param context - context, to access resources
     */
    public void populateData(Context context) {
        data.clear();

        addQuestions(context, R.array.questions_about_mygrades, R.array.answers_about_mygrades, R.string.questions_about_mygrades_section_title);
        addQuestions(context, R.array.questions_error, R.array.answers_error, R.string.questions_error_section_title);
        addQuestions(context, R.array.questions_general, R.array.answers_general, R.string.questions_general_section_title);
    }

    private void addQuestions(Context context, int resIdQuestions, int resIdAnswers, int resIdSectionTitle) {
        CharSequence[] questions = context.getResources().getTextArray(resIdQuestions);
        CharSequence[] answers = context.getResources().getTextArray(resIdAnswers);
        String sectionTitle = context.getResources().getString(resIdSectionTitle);

        for (int i = 0; i < questions.length; i++) {
            String title = (i == 0) ? sectionTitle : null;
            QuestionData question = new QuestionData(data.size() + 1, questions[i], title);
            AnswerData answer = new AnswerData(1, answers[i]);
            data.add(new Pair(question, answer));
        }
    }

    /**
     * Returns the groupId for a given attribute, e.q. GO_TO_GENERAL_ERROR.
     * This is passed as a attribute to the FragmentFaq, to directly jump to a specific question.
     *
     * @param goToQuestion - constant which specifies the desired question
     * @return groupId
     */
    public int getGroupId(int goToQuestion) {
        switch (goToQuestion) {
            case GO_TO_GENERAL_ERROR:
                return 5;
            case GO_TO_WHY_NO_GRADING:
                return 9;
            default:
                return 0;
        }
    }

    public int getGroupCount() {
        return data.size();
    }

    public int getChildCount() {
        return 1; // each question has exactly 1 answer.
    }

    public QuestionData getGroupItem(int groupPosition) {
        return data.get(groupPosition).first;
    }

    public AnswerData getChildItem(int groupPosition) {
        return data.get(groupPosition).second;
    }

    /**
     * Question data.
     */
    public static final class QuestionData {
        private final long id; // unique ids are required
        private final CharSequence question;
        private final String sectionTitle;

        public QuestionData(long id, CharSequence question, String sectionTitle) {
            this.id = id;
            this.question = question;
            this.sectionTitle = sectionTitle;
        }

        public long getGroupId() {
            return id;
        }

        public CharSequence getQuestion() {
            return question;
        }

        public String getSectionTitle() {
            return sectionTitle;
        }
    }

    /**
     * Answer data.
     */
    public static final class AnswerData {
        private final long id; // unique ids are required
        private final CharSequence answer;

        public AnswerData(long id, CharSequence answer) {
            this.id = id;
            this.answer = answer;
        }

        public long getChildId() {
            return id;
        }

        public CharSequence getAnswer() {
            return answer;
        }
    }
}
