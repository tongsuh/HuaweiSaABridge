/*  Copyright (C) 2024 Gadgetbridge Contributors
    This file is part of Gadgetbridge.
    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
*/
package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import android.content.Context;
import android.content.SharedPreferences;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;

/**
 * 华为手环 8 清醒梦 (REM) 震动参数持久化配置中心
 * 允许用户在手机界面自由调节震动力度、次数与时长，无需重新编译代码
 */
public class HuaweiLucidSettings {

    public static final String KEY_VIBRATE_INTENSITY = "pref_lucid_vibrate_intensity";
    public static final String KEY_VIBRATE_REPEAT = "pref_lucid_vibrate_repeat";
    public static final String KEY_VIBRATE_DURATION = "pref_lucid_vibrate_duration";

    public static final int DEFAULT_INTENSITY = 1;     // 1=微弱(适合手腕), 2=中等, 3=强力
    public static final int DEFAULT_REPEAT = 2;        // 默认双击 2 次
    public static final int DEFAULT_DURATION_MS = 150; // 单次 150 毫秒

    private final SharedPreferences prefs;

    // 内存测试备用字段 (当 context 为空时自动启用内存存储)
    private int memoryIntensity = DEFAULT_INTENSITY;
    private int memoryRepeat = DEFAULT_REPEAT;
    private int memoryDurationMs = DEFAULT_DURATION_MS;

    public HuaweiLucidSettings(Context context) {
        if (context != null) {
            this.prefs = GBApplication.getPrefs().getPreferences();
        } else {
            this.prefs = null;
        }
    }

    public HuaweiLucidSettings() {
        this(GBApplication.getContext());
    }

    public int getVibrateIntensity() {
        if (prefs != null) {
            try {
                String str = prefs.getString(KEY_VIBRATE_INTENSITY, String.valueOf(DEFAULT_INTENSITY));
                return Integer.parseInt(str);
            } catch (Exception ignored) {
                return prefs.getInt(KEY_VIBRATE_INTENSITY, DEFAULT_INTENSITY);
            }
        }
        return memoryIntensity;
    }

    public void setVibrateIntensity(int intensity) {
        int clamped = Math.max(1, Math.min(3, intensity));
        if (prefs != null) {
            prefs.edit().putString(KEY_VIBRATE_INTENSITY, String.valueOf(clamped)).apply();
        } else {
            memoryIntensity = clamped;
        }
    }

    public int getVibrateRepeat() {
        if (prefs != null) {
            try {
                String str = prefs.getString(KEY_VIBRATE_REPEAT, String.valueOf(DEFAULT_REPEAT));
                return Integer.parseInt(str);
            } catch (Exception ignored) {
                return prefs.getInt(KEY_VIBRATE_REPEAT, DEFAULT_REPEAT);
            }
        }
        return memoryRepeat;
    }

    public void setVibrateRepeat(int repeat) {
        int clamped = Math.max(1, Math.min(10, repeat));
        if (prefs != null) {
            prefs.edit().putString(KEY_VIBRATE_REPEAT, String.valueOf(clamped)).apply();
        } else {
            memoryRepeat = clamped;
        }
    }

    public int getVibrateDurationMs() {
        if (prefs != null) {
            try {
                String str = prefs.getString(KEY_VIBRATE_DURATION, String.valueOf(DEFAULT_DURATION_MS));
                return Integer.parseInt(str);
            } catch (Exception ignored) {
                return prefs.getInt(KEY_VIBRATE_DURATION, DEFAULT_DURATION_MS);
            }
        }
        return memoryDurationMs;
    }

    public void setVibrateDurationMs(int durationMs) {
        int clamped = Math.max(50, Math.min(1000, durationMs));
        if (prefs != null) {
            prefs.edit().putString(KEY_VIBRATE_DURATION, String.valueOf(clamped)).apply();
        } else {
            memoryDurationMs = clamped;
        }
    }
}
