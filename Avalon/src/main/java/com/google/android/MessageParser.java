package com.google.android;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by jinyan on 5/12/14.
 */
public class MessageParser {

    public enum AvalonRoles { }

    public static AvalonMessage parse(byte[] input) {
        return null;
    }

    public static byte[] construct(AvalonMessage wrapper) {
        return null;
    }

    public interface AvalonMessage extends Serializable {}

    public static class PlayerName implements AvalonMessage {
        public String name;
    }

    public static class InitialRoleAssignment implements AvalonMessage {
        public AvalonRoles role;
        public Set<String> otherInfo;
    }

    public static class QuestProposal implements AvalonMessage {
        public Set<String> names;
        public String proposer;
        public int propNum;
    }

    public static class QuestRsp implements AvalonMessage {
        public boolean approve;
    }
    
}
