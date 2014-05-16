package com.google.android.avalon.model;

import com.google.android.avalon.rules.IllegalConfigurationException;

/**
 * Created by jinyan on 5/15/14.
 */
public class BoardCampaignInfo {

    public final int[] numPeopleOnQuests;
    public final int[] numPeopleNeedToFail;

    public BoardCampaignInfo(int numPlayers) throws IllegalConfigurationException {
        switch (numPlayers) {
            case 5:
                numPeopleOnQuests = new int[] {2, 3, 2, 3, 3};
                numPeopleNeedToFail = new int[] {1, 1, 1, 1, 1};
                break;
            case 6:
                numPeopleOnQuests = new int[] {2, 3, 4, 3, 4};
                numPeopleNeedToFail = new int[] {1, 1, 1, 1, 1};
                break;
            case 7:
                numPeopleOnQuests = new int[] {2, 3, 3, 4, 4};
                numPeopleNeedToFail = new int[] {1, 1, 1, 2, 1};
                break;
            default:
                // Number of players is not supported
                throw new IllegalConfigurationException();
        }
    }
}
