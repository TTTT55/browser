/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.studio.browser;

import com.studio.browser.misc.WebAddress;
import android.os.Debug;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

/**
 * Performance analysis
 */
public class Performance {

    private static final String LOGTAG = "browser";

    private final static boolean LOGD_ENABLED =
            Browser.LOGD_ENABLED;

    private static boolean mInTrace;

    /** @hide */
    public static final int PROC_TERM_MASK = 0xff;
    /** @hide */
    public static final int PROC_ZERO_TERM = 0;
    /** @hide */
    public static final int PROC_SPACE_TERM = (int)' ';
    /** @hide */
    public static final int PROC_TAB_TERM = (int)'\t';
    /** @hide */
    public static final int PROC_COMBINE = 0x100;
    /** @hide */
    public static final int PROC_PARENS = 0x200;
    /** @hide */
    public static final int PROC_QUOTES = 0x400;
    /** @hide */
    public static final int PROC_OUT_STRING = 0x1000;
    /** @hide */
    public static final int PROC_OUT_LONG = 0x2000;
    /** @hide */
    public static final int PROC_OUT_FLOAT = 0x4000;

    // Performance probe
    private static final int[] SYSTEM_CPU_FORMAT = new int[] {
            PROC_SPACE_TERM | PROC_COMBINE,
            PROC_SPACE_TERM | PROC_OUT_LONG, // 1: user time
            PROC_SPACE_TERM | PROC_OUT_LONG, // 2: nice time
            PROC_SPACE_TERM | PROC_OUT_LONG, // 3: sys time
            PROC_SPACE_TERM | PROC_OUT_LONG, // 4: idle time
            PROC_SPACE_TERM | PROC_OUT_LONG, // 5: iowait time
            PROC_SPACE_TERM | PROC_OUT_LONG, // 6: irq time
            PROC_SPACE_TERM | PROC_OUT_LONG  // 7: softirq time
    };

    private static long mStart;
    private static long mProcessStart;
    private static long mUserStart;
    private static long mSystemStart;
    private static long mIdleStart;
    private static long mIrqStart;

    private static long mUiStart;

    static void tracePageStart(String url) {
        if (BrowserSettings.getInstance().isTracing()) {
            String host;
            try {
                WebAddress uri = new WebAddress(url);
                host = uri.getHost();
            } catch (android.net.ParseException ex) {
                host = "browser";
            }
            host = host.replace('.', '_');
            host += ".trace";
            mInTrace = true;
            Debug.startMethodTracing(host, 20 * 1024 * 1024);
        }
    }

    static void tracePageFinished() {
        if (mInTrace) {
            mInTrace = false;
            Debug.stopMethodTracing();
        }
    }

    public static final native boolean readProcFile(String file, int[] format,
                                                    String[] outStrings, long[] outLongs, float[] outFloats);

    static void onPageStarted() {
        mStart = SystemClock.uptimeMillis();
        mProcessStart = Process.getElapsedCpuTime();
        long[] sysCpu = new long[7];
        if (readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            mUserStart = sysCpu[0] + sysCpu[1];
            mSystemStart = sysCpu[2];
            mIdleStart = sysCpu[3];
            mIrqStart = sysCpu[4] + sysCpu[5] + sysCpu[6];
        }
        mUiStart = SystemClock.currentThreadTimeMillis();
    }

    static void onPageFinished(String url) {
        long[] sysCpu = new long[7];
        if (readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            String uiInfo =
                    "UI thread used " + (SystemClock.currentThreadTimeMillis() - mUiStart) + " ms";
            if (LOGD_ENABLED) {
                Log.d(LOGTAG, uiInfo);
            }
            // The string that gets written to the log
            String performanceString =
                    "It took total " + (SystemClock.uptimeMillis() - mStart)
                            + " ms clock time to load the page." + "\nbrowser process used "
                            + (Process.getElapsedCpuTime() - mProcessStart)
                            + " ms, user processes used " + (sysCpu[0] + sysCpu[1] - mUserStart)
                            * 10 + " ms, kernel used " + (sysCpu[2] - mSystemStart) * 10
                            + " ms, idle took " + (sysCpu[3] - mIdleStart) * 10
                            + " ms and irq took " + (sysCpu[4] + sysCpu[5] + sysCpu[6] - mIrqStart)
                            * 10 + " ms, " + uiInfo;
            if (LOGD_ENABLED) {
                Log.d(LOGTAG, performanceString + "\nWebpage: " + url);
            }
            if (url != null) {
                // strip the url to maintain consistency
                String newUrl = new String(url);
                if (newUrl.startsWith("http://www.")) {
                    newUrl = newUrl.substring(11);
                } else if (newUrl.startsWith("http://")) {
                    newUrl = newUrl.substring(7);
                } else if (newUrl.startsWith("https://www.")) {
                    newUrl = newUrl.substring(12);
                } else if (newUrl.startsWith("https://")) {
                    newUrl = newUrl.substring(8);
                }
                if (LOGD_ENABLED) {
                    Log.d(LOGTAG, newUrl + " loaded");
                }
            }
        }
    }
}
