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
        String[] questions = context.getResources().getStringArray(R.array.questions);
        String[] answers = context.getResources().getStringArray(R.array.answers);

        for (int i = 0; i < questions.length && i < answers.length; i++) {
            QuestionData question = new QuestionData(i + 1, questions[i]);
            AnswerData answer = new AnswerData(1, answers[i]);
            data.add(new Pair(question, answer));
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
        private final String question;

        public QuestionData(long id, String question) {
            this.id = id;
            this.question = question;
        }

        public long getGroupId() {
            return id;
        }

        public String getQuestion() {
            return question;
        }
    }

    /**
     * Answer data.
     */
    public static final class AnswerData {
        private final long id; // unique ids are required
        private final String answer;

        public AnswerData(long id, String answer) {
            this.id = id;
            this.answer = answer;
        }

        public long getChildId() {
            return id;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
