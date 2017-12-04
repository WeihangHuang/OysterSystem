package com.tfl.billing;

import java.time.LocalTime;

/**
 * Created by apple on 12/4/17.
 */
public class SystemClock implements Clock {
    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }
}
