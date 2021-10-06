package com.example.logitudetimezone;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

public class Utils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Instant calculateActualTime(long timeDifference) {

        Instant instant = Instant.now();
        if (TimeZone.getDefault().inDaylightTime(new Date())) {
            timeDifference += 3600;
        }

        long seconds = instant.getEpochSecond() + timeDifference;
        Instant currentTime = Instant.ofEpochSecond(seconds);
        return currentTime;
    }

    public static long calculateTimeDifference(double longitude) {

        int degree = (int) longitude;
        int minute = (int) ((longitude) * 60) % 60;
        double second = ((longitude) * 3600) % 60;

        double timeDifference = ((degree * 3600L) + (minute * 60) + second);

        return Math.round((timeDifference / 15));
    }
}
