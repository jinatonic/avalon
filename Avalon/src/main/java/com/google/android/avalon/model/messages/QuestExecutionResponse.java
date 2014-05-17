package com.google.android.avalon.model.messages;

/**
 * Created by jinyan on 5/15/14.
 */
public class QuestExecutionResponse extends AvalonMessage {
    public PlayerInfo player;
    public boolean pass;
    public int questNum;

    public QuestExecutionResponse(PlayerInfo i, boolean p, int q) {
        player = i;
        pass = p;
        questNum = q;
    }

    @Override
    public String toString() {
        return "[msg execRsp from " + player.name + " with " + pass + " for " + questNum + "]";
    }
}
