package com.google.android.avalon.model.messages;

/**
 * Created by jinyan on 5/15/14.
 */
public class QuestExecution extends AvalonMessage {
    public int questNum;    // 0-based index

    public QuestExecution(int q) {
        questNum = q;
    }

    @Override
    public String toString() {
        return "[msg exec " + questNum + "]";
    }
}
