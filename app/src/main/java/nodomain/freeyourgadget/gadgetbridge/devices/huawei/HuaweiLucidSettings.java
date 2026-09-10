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

    public static final String KEY_VIBRATE_MODE = "pref_lucid_vibrate_mode";
    public static final String KEY_CONTINUOUS_DURATION_SEC = "pref_lucid_continuous_sec";
    public static final String KEY_PULSE_INTERVAL_MS = "pref_lucid_pulse_interval";
    public static final String KEY_VIBRATE_INTENSITY = "pref_lucid_vibrate_intensity";
    public static final String KEY_VIBRATE_REPEAT = "pref_lucid_vibrate_repeat";
    public static final String KEY_VIBRATE_DURATION = "pref_lucid_vibrate_duration";

    public static final String MODE_CONTINUOUS = "continuous";
    public static final String MODE_PULSE = "pulse";

    public static final String DEFAULT_MODE = MODE_PULSE;
    public static final int DEFAULT_CONTINUOUS_SEC = 5;       // 默认持续长震 5 秒
    public static final int DEFAULT_PULSE_INTERVAL_MS = 800;   // 默认脉冲间隔 800 毫秒 (防手环消息去重与马达丢震)
    public static final int DEFAULT_INTENSITY = 1;             // 1=微弱(适合手腕), 2=中等, 3=强力
    public static final int DEFAULT_REPEAT = 3;                // 脉冲模式默认 3 次
    public static final int DEFAULT_DURATION_MS = 200;         // 单次脉冲时长 200 毫秒

    private final SharedPreferences prefs;

    // 内存测试备用字段 (当 context 为空时自动启用内存存储)
    private String memoryMode = DEFAULT_MODE;
    private int memoryContinuousSec = DEFAULT_CONTINUOUS_SEC;
    private int memoryPulseIntervalMs = DEFAULT_PULSE_INTERVAL_MS;
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

    public String getVibrateMode() {
        if (prefs != null) {
            return prefs.getString(KEY_VIBRATE_MODE, DEFAULT_MODE);
        }
        return memoryMode;
    }

    public void setVibrateMode(String mode) {
        if (prefs != null) {
            prefs.edit().putString(KEY_VIBRATE_MODE, mode).apply();
        } else {
            memoryMode = mode;
        }
    }

    public int getContinuousDurationSec() {
        if (prefs != null) {
            try {
                String str = prefs.getString(KEY_CONTINUOUS_DURATION_SEC, String.valueOf(DEFAULT_CONTINUOUS_SEC));
                return Integer.parseInt(str);
            } catch (Exception ignored) {
                return prefs.getInt(KEY_CONTINUOUS_DURATION_SEC, DEFAULT_CONTINUOUS_SEC);
            }
        }
        return memoryContinuousSec;
    }

    public void setContinuousDurationSec(int sec) {
        int clamped = Math.max(1, Math.min(60, sec));
        if (prefs != null) {
            prefs.edit().putString(KEY_CONTINUOUS_DURATION_SEC, String.valueOf(clamped)).apply();
        } else {
            memoryContinuousSec = clamped;
        }
    }

    public int getPulseIntervalMs() {
        if (prefs != null) {
            try {
                String str = prefs.getString(KEY_PULSE_INTERVAL_MS, String.valueOf(DEFAULT_PULSE_INTERVAL_MS));
                return Integer.parseInt(str);
            } catch (Exception ignored) {
                return prefs.getInt(KEY_PULSE_INTERVAL_MS, DEFAULT_PULSE_INTERVAL_MS);
            }
        }
        return memoryPulseIntervalMs;
    }

    public void setPulseIntervalMs(int ms) {
        int clamped = Math.max(300, Math.min(3000, ms));
        if (prefs != null) {
            prefs.edit().putString(KEY_PULSE_INTERVAL_MS, String.valueOf(clamped)).apply();
        } else {
            memoryPulseIntervalMs = clamped;
        }
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
        int clamped = Math.max(1, Math.min(20, repeat));
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
