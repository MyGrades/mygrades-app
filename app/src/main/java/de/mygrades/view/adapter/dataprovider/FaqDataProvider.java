package de.mygrades.view.adapter.dataprovider;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * FaqDataProvider provides access to the underlying FAQ data.
 */
public class FaqDataProvider {
    private List<Pair<QuestionData, AnswerData>> data;

    public FaqDataProvider() {
        data = new ArrayList<>();

        data.add(new Pair(new QuestionData(1, "header 1"), new AnswerData(1, "child 1")));
        data.add(new Pair(new QuestionData(2, "header 2"), new AnswerData(1, "child 2")));
        data.add(new Pair(new QuestionData(3, "header 3"), new AnswerData(1, "child 3")));
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
