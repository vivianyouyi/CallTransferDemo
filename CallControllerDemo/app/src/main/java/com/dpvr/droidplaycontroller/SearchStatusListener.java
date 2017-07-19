package com.dpvr.droidplaycontroller;

import java.util.ArrayList;

/**
 * Created by liweiwei on 2017/6/6 0022.
 */
public interface SearchStatusListener {

    void searchSuccess(ArrayList<String> deviceList);

    void searchFailed(String err);
}
