/*  Copyright (C) 2024 Gadgetbridge Contributors
    This file is part of Gadgetbridge.
    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
*/
package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiLucidSettings;

/**
 * 华为手环马达震动报文组装器
 * 负责构建清醒梦微震（REM 提示）与闹钟长震指令，支持动态参数调节
 */
public class HuaweiVibrateCommander {

    public static final byte SERVICE_DEVICE_CONTROL = 0x01;
    public static final byte COMMAND_SET_VIBRATION = 0x08;

    // 震动类型标识
    public static final byte TAG_VIBRATE_PATTERN = 0x01;
    public static final byte TAG_VIBRATE_DURATION = 0x02;
    public static final byte TAG_VIBRATE_REPEAT = 0x03;
    public static final byte TAG_VIBRATE_INTENSITY = 0x04;

    /**
     * 符合华为 BLE 规范的标准震动报文对象，继承自 HuaweiPacket 确保通过会话密钥加密
     */
    public static class VibratePacket extends nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket {
        public VibratePacket(ParamsProvider paramsProvider, int intensity, int repeat, int durationMs) {
            super(paramsProvider);
            this.serviceId = SERVICE_DEVICE_CONTROL;
            this.commandId = COMMAND_SET_VIBRATION;
            this.tlv = new nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV()
                    .put(TAG_VIBRATE_PATTERN, (byte) ((repeat > 1) ? 0x02 : 0x01))
                    .put(TAG_VIBRATE_DURATION, (short) durationMs)
                    .put(TAG_VIBRATE_REPEAT, (byte) repeat)
                    .put(TAG_VIBRATE_INTENSITY, (byte) intensity);
            this.complete = true;
        }
    }

    /**
     * 根据动态传入的自定义参数，构建清醒梦（Lucid Dream）微震报文
     * @param intensity 震动强度 (1=微弱, 2=中等, 3=强力)
     * @param repeat 震动次数 (1~10次)
     * @param durationMs 单次持续时长 (50~1000ms)
     */
    public static byte[] buildLucidDreamCuePacket(int intensity, int repeat, int durationMs) {
        return new HuaweiTLVBuilder(SERVICE_DEVICE_CONTROL, COMMAND_SET_VIBRATION)
                .addByte(TAG_VIBRATE_PATTERN, (repeat > 1) ? 0x02 : 0x01) // 1次为单脉冲，大于1次为连续脉冲
                .addShort(TAG_VIBRATE_DURATION, durationMs)
                .addByte(TAG_VIBRATE_REPEAT, repeat)
                .addByte(TAG_VIBRATE_INTENSITY, intensity)
                .build();
    }

    /**
     * 从用户的 App 设置中自动读取最新参数构建报文
     */
    public static byte[] buildLucidDreamCuePacket(HuaweiLucidSettings settings) {
        if (settings == null) {
            return buildDefaultLucidDreamCuePacket();
        }
        return buildLucidDreamCuePacket(
                settings.getVibrateIntensity(),
                settings.getVibrateRepeat(),
                settings.getVibrateDurationMs()
        );
    }

    /**
     * 默认黄金模板 (微弱双击 150ms)
     */
    public static byte[] buildDefaultLucidDreamCuePacket() {
        return buildLucidDreamCuePacket(
                HuaweiLucidSettings.DEFAULT_INTENSITY,
                HuaweiLucidSettings.DEFAULT_REPEAT,
                HuaweiLucidSettings.DEFAULT_DURATION_MS
        );
    }

    /**
     * 构建智能唤醒闹钟（Smart Alarm）持续强震报文
     */
    public static byte[] buildAlarmVibratePacket() {
        return new HuaweiTLVBuilder(SERVICE_DEVICE_CONTROL, COMMAND_SET_VIBRATION)
                .addByte(TAG_VIBRATE_PATTERN, 0x01)       // 连续震动
                .addShort(TAG_VIBRATE_DURATION, 1000)    // 1000ms
                .addByte(TAG_VIBRATE_REPEAT, 10)         // 重复 10 次
                .addByte(TAG_VIBRATE_INTENSITY, 3)       // 最高强度 (Strong)
                .build();
    }

    /**
     * 构建立即停止震动报文
     */
    public static byte[] buildStopVibratePacket() {
        return new HuaweiTLVBuilder(SERVICE_DEVICE_CONTROL, COMMAND_SET_VIBRATION)
                .addByte(TAG_VIBRATE_PATTERN, 0x00)
                .addByte(TAG_VIBRATE_REPEAT, 0)
                .build();
    }
}
