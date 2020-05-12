package com.kristian.czepluch.snoozification.Interfaces;

import android.app.job.JobParameters;

public interface AsyncUser {
    boolean onFinish();
    boolean onFinishJob(JobParameters params);
}
