package com.sandvik.databearerdev.services.fdm;

import android.os.Binder;

public class FDMServiceBinder extends Binder {
    FDMService service;

    public FDMServiceBinder(FDMService service) {
        this.service = service;
    }
}
