package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 华为手环 8 清醒梦 (REM) 震动参数持久化配置中心
 * 允许用户在手机界面自由调节震动力度、次数与时长，无需重新编译代码
 */
public class HuaweiLucidSettings {

    private static final String PREF_NAME = "huawei_band8_saa_settings";

    public static final String KEY_VIBRATE_INTENSITY = "vibrate_intensity";
    public static final String KEY_VIBRATE_REPEAT = "vibrate_repeat";
    public static final String KEY_VIBRATE_DURATION = "vibrate_duration_ms";

    public static final int DEFAULT_INTENSITY = 1;     // 1=微弱(适合手腕), 2=中等(适合大臂), 3=强力
    public static final int DEFAULT_REPEAT = 2;        // 默认双击 2 次
    public static final int DEFAULT_DURATION_MS = 150; // 单次 150 毫秒

    private final SharedPreferences prefs;

    // 内存测试备用字段 (当 context 为空时自动启用内存存储)
    private int memoryIntensity = DEFAULT_INTENSITY;
    private int memoryRepeat = DEFAULT_REPEAT;
    private int memoryDurationMs = DEFAULT_DURATION_MS;

    public HuaweiLucidSettings(Context context) {
        if (context != null) {
            this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        } else {
            this.prefs = null;
        }
    }

    public HuaweiLucidSettings() {
        this(null);
    }

    public int getVibrateIntensity() {
        if (prefs != null) {
            return prefs.getInt(KEY_VIBRATE_INTENSITY, DEFAULT_INTENSITY);
        }
        return memoryIntensity;
    }

    public void setVibrateIntensity(int intensity) {
        int clamped = Math.max(1, Math.min(3, intensity));
        if (prefs != null) {
            prefs.edit().putInt(KEY_VIBRATE_INTENSITY, clamped).apply();
        } else {
            memoryIntensity = clamped;
        }
    }

    public int getVibrateRepeat() {
        if (prefs != null) {
            return prefs.getInt(KEY_VIBRATE_REPEAT, DEFAULT_REPEAT);
        }
        return memoryRepeat;
    }

    public void setVibrateRepeat(int repeat) {
        int clamped = Math.max(1, Math.min(10, repeat));
        if (prefs != null) {
            prefs.edit().putInt(KEY_VIBRATE_REPEAT, clamped).apply();
        } else {
            memoryRepeat = clamped;
        }
    }

    public int getVibrateDurationMs() {
        if (prefs != null) {
            return prefs.getInt(KEY_VIBRATE_DURATION, DEFAULT_DURATION_MS);
        }
        return memoryDurationMs;
    }

    public void setVibrateDurationMs(int durationMs) {
        int clamped = Math.max(50, Math.min(1000, durationMs));
        if (prefs != null) {
            prefs.edit().putInt(KEY_VIBRATE_DURATION, clamped).apply();
        } else {
            memoryDurationMs = clamped;
        }
    }
}
