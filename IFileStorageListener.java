package com.sandvik.databearerdev.storage;

/**
 * Created by JuhaM on 4.11.2016.
 */
public interface IFileStorageListener {
    void onMachineMetaDataChanged(String machineSerial);
}
