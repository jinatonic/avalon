package com.google.android.avalon.model;

import android.util.Log;

import com.google.android.avalon.model.messages.AvalonMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by jinyan on 5/13/14.
 */
public class MessageParser {
    private static final String TAG = MessageParser.class.getSimpleName();

    public static final int MAX_NUM_BYTES = 4096;

    public static AvalonMessage parse(byte[] input) {
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            AvalonMessage o = (AvalonMessage) in.readObject();
            return o;
        } catch (IOException e) {
            Log.e(TAG, "Error deserializing object", e);
            return null;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Error deserializing object", e);
            return null;
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public static byte[] construct(AvalonMessage wrapper) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(wrapper);
            byte[] yourBytes = bos.toByteArray();
            if (yourBytes.length > MAX_NUM_BYTES) {
                Log.e(TAG, "Constructed byte array is too long");
                return null;
            }
            return yourBytes;
        } catch (IOException e) {
            Log.e(TAG, "Error serializing " + wrapper, e);
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }
}
