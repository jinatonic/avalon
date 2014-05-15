package com.google.android.avalon.model.messages;

/**
 * Created by jinyan on 5/15/14.
 */
public class GameOverMessage extends AvalonMessage {
    public boolean goodWon;

    public GameOverMessage(boolean g) {
        goodWon = g;
    }

    @Override
    public String toString() {
        return (goodWon) ? "Good wins!" : "Evil wins!";
    }
}
