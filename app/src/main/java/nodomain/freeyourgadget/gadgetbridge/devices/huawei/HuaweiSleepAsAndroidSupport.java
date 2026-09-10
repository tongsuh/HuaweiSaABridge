package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.HuaweiTelemetryPacketParser;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.HuaweiVibrateCommander;

import java.util.ArrayList;
import java.util.List;

/**
 * 华为手环 8 与 Sleep as Android (SaA) 的核心适配器
 * 
 * 职责：
 * 1. 拦截 SaA 的系统级控制广播 (START_TRACKING, STOP_TRACKING, HINT, ALARM_TRIGGER)
 * 2. 调度 HuaweiWorkoutManager 启停手环的高频推流
 * 3. 接收并平滑手环上报的心率与体动数据，打包成 SaA 官方规范的 DATA_UPDATE 广播发出
 * 4. 响应清醒梦微震（动态读取用户偏好设置）与闹钟唤醒
 */
public class HuaweiSleepAsAndroidSupport {

    public static final String SAA_PACKAGE_NAME = "com.urbandroid.sleep";
    public static final String ACTION_START_TRACKING = "com.urbandroid.sleep.watch.START_TRACKING";
    public static final String ACTION_STOP_TRACKING = "com.urbandroid.sleep.watch.STOP_TRACKING";
    public static final String ACTION_DATA_UPDATE = "com.urbandroid.sleep.watch.DATA_UPDATE";
    public static final String ACTION_HINT = "com.urbandroid.sleep.watch.HINT";
    public static final String ACTION_ALARM_TRIGGER = "com.urbandroid.sleep.watch.ALARM_TRIGGER";
    public static final String ACTION_ALARM_STOP = "com.urbandroid.sleep.watch.ALARM_STOP";
    public static final String ACTION_SET_PAUSE = "com.urbandroid.sleep.watch.SET_PAUSE";

    public static final String EXTRA_MAX_RAW_DATA = "MAX_RAW_DATA";
    public static final String EXTRA_HR_DATA = "HR_DATA";
    public static final String EXTRA_BATTERY_LEVEL = "BATTERY_LEVEL";

    @FunctionalInterface
    public interface BlePacketSender {
        void sendPacketToBand(byte[] packet, String description);
    }

    public interface AndroidBroadcastSender {
        void sendBroadcastToSaA(String action, float[] maxRawData, float heartRate, int batteryLevel);

        default void sendSimpleActionToSaA(String action) {
            sendBroadcastToSaA(action, null, 0, -1);
        }
    }

    private final BlePacketSender bleSender;
    private final AndroidBroadcastSender broadcastSender;
    private final HuaweiWorkoutManager workoutManager;
    private final HuaweiLucidSettings lucidSettings;

    private final List<Float> motionBuffer = new ArrayList<>();
    private long lastBatchSendTime = 0;
    private boolean isPaused = false;

    public HuaweiSleepAsAndroidSupport(BlePacketSender bleSender, AndroidBroadcastSender broadcastSender, HuaweiLucidSettings lucidSettings) {
        this.bleSender = bleSender;
        this.broadcastSender = broadcastSender;
        this.workoutManager = new HuaweiWorkoutManager();
        this.lucidSettings = lucidSettings;
    }

    public void onReceiveSaAIntent(String action) {
        if (action == null) return;

        switch (action) {
            case ACTION_START_TRACKING:
                isPaused = false;
                byte[] startCmd = workoutManager.buildStartStreamingPacket();
                bleSender.sendPacketToBand(startCmd, "启动手环高频心率推流 (伪装自由运动)");
                break;

            case ACTION_STOP_TRACKING:
                byte[] stopCmd = workoutManager.buildStopStreamingPacket();
                bleSender.sendPacketToBand(stopCmd, "停止手环推流，恢复低功耗待机");
                motionBuffer.clear();
                break;

            case ACTION_HINT:
                byte[] lucidVibCmd = HuaweiVibrateCommander.buildLucidDreamCuePacket(lucidSettings);
                bleSender.sendPacketToBand(lucidVibCmd, "【REM期提示】按用户偏好触发手环微震");
                break;

            case ACTION_ALARM_TRIGGER:
                byte[] alarmVibCmd = HuaweiVibrateCommander.buildAlarmVibratePacket();
                bleSender.sendPacketToBand(alarmVibCmd, "【闹钟唤醒】触发手环连续强震");
                break;

            case ACTION_ALARM_STOP:
                byte[] stopVibCmd = HuaweiVibrateCommander.buildStopVibratePacket();
                bleSender.sendPacketToBand(stopVibCmd, "停止手环震动");
                break;
        }
    }

    public void testCurrentVibration() {
        byte[] testCmd = HuaweiVibrateCommander.buildLucidDreamCuePacket(lucidSettings);
        bleSender.sendPacketToBand(testCmd, "【测试震动】执行用户当前设定的清醒梦微震参数");
    }

    public void onReceiveBlePacketFromBand(byte[] rawPacket) {
        if (!workoutManager.isTrackingActive()) {
            return;
        }

        HuaweiTelemetryPacketParser.TelemetryData data = HuaweiTelemetryPacketParser.parse(rawPacket);
        if (data == null) {
            return;
        }

        if (!data.isWorn) {
            if (!isPaused) {
                isPaused = true;
                broadcastSender.sendSimpleActionToSaA(ACTION_SET_PAUSE);
            }
            return;
        } else {
            isPaused = false;
        }

        motionBuffer.add(data.motionIntensity);

        long now = System.currentTimeMillis();
        if (now - lastBatchSendTime >= 10000 || lastBatchSendTime == 0) {
            float[] maxRawData = new float[motionBuffer.size()];
            for (int i = 0; i < motionBuffer.size(); i++) {
                maxRawData[i] = motionBuffer.get(i);
            }
            motionBuffer.clear();
            lastBatchSendTime = now;

            broadcastSender.sendBroadcastToSaA(
                    ACTION_DATA_UPDATE,
                    maxRawData,
                    data.heartRate,
                    data.batteryLevel
            );
        }
    }

    public HuaweiWorkoutManager getWorkoutManager() {
        return workoutManager;
    }

    public HuaweiLucidSettings getLucidSettings() {
        return lucidSettings;
    }
}
