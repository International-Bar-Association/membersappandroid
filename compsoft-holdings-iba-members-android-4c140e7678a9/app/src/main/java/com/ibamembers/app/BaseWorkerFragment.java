package com.ibamembers.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;

public class BaseWorkerFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
