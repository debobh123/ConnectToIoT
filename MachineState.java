package com.sandvik.databearerdev.gui.main;

import java.io.Serializable;

/**
 * Created by JuhaM on 28.10.2016.
 */
public class MachineState implements Serializable {
    public enum ConnectionState
    {
        Disconnected,
        Idle,
        Sending,
        Receiving
    };
    private ConnectionState connState = ConnectionState.Disconnected;
    private String name;
    private int outgoingFileCount;
    private int receivedFileCount;
    private Object lock = new Object();

    public ConnectionState getConnectionState() {
        synchronized (lock) {
            return connState;
        }
    }

    public void setConnectionState(ConnectionState state) {
        synchronized (lock) {
            connState = state;
        }
    }

    public String getName() {
        synchronized (lock) {
            return name;
        }
    }

    public void setName(String name) {
        synchronized (lock) {
            this.name = name;
        }
    }

    public int getOutgoingFileCount() {
        synchronized (lock) {
            return outgoingFileCount;
        }
    }

    public void setOutgoingFileCount(int count)
    {
        synchronized (lock) {
            outgoingFileCount = count;
        }
    }

    public int getReceivedFileCount() {
        synchronized (lock) {
            return receivedFileCount;
        }
    }

    public void setReceivedFileCount(int count) {
        synchronized (lock) {
            receivedFileCount = count;
        }
    }
}
