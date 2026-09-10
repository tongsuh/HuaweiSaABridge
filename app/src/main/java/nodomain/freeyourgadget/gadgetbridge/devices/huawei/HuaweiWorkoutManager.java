package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.HuaweiTLVBuilder;

/**
 * 华为手环运动模式控制器
 * 核心黑科技：通过向手环下发“静默自由训练”指令，强制手环常开 PPG 绿光，并以 1Hz 频率向手机推流实时心率与体动
 */
public class HuaweiWorkoutManager {

    public static final byte SERVICE_FITNESS = 0x02;
    public static final byte COMMAND_START_WORKOUT = 0x01;
    public static final byte COMMAND_STOP_WORKOUT = 0x02;

    public static final byte TAG_WORKOUT_TYPE = 0x01;
    public static final byte TAG_GPS_ENABLE = 0x02;
    public static final byte TAG_HEART_RATE_ENABLE = 0x03;
    public static final byte TAG_AUTO_PAUSE_ENABLE = 0x04;

    // 自由训练类型标识符 (Indoor Free Training)
    public static final int ACTIVITY_TYPE_FREE_WORKOUT = 0x07;

    private boolean isTrackingActive = false;

    /**
     * 构建启动自由运动推流的二进制 TLV 指令包
     * 关键配置：关闭 GPS（极度省电）、开启持续高频心率检测、关闭自动暂停（防止静止睡眠被当作暂停运动）
     */
    public byte[] buildStartStreamingPacket() {
        isTrackingActive = true;
        return new HuaweiTLVBuilder(SERVICE_FITNESS, COMMAND_START_WORKOUT)
                .addByte(TAG_WORKOUT_TYPE, ACTIVITY_TYPE_FREE_WORKOUT) // 自由训练
                .addByte(TAG_GPS_ENABLE, 0x00)                        // 关闭 GPS (省电!)
                .addByte(TAG_HEART_RATE_ENABLE, 0x01)                 // 强制开启持续心率
                .addByte(TAG_AUTO_PAUSE_ENABLE, 0x00)                 // 禁止自动暂停
                .build();
    }

    /**
     * 构建停止运动推流指令包，使手环恢复普通待机低功耗模式，熄灭绿光
     */
    public byte[] buildStopStreamingPacket() {
        isTrackingActive = false;
        return new HuaweiTLVBuilder(SERVICE_FITNESS, COMMAND_STOP_WORKOUT)
                .addByte(TAG_WORKOUT_TYPE, ACTIVITY_TYPE_FREE_WORKOUT)
                .build();
    }

    public boolean isTrackingActive() {
        return isTrackingActive;
    }

    public void setTrackingActive(boolean active) {
        this.isTrackingActive = active;
    }
}
