# 华为手环 8 × Sleep as Android (SaA) 联动桥梁与清醒梦插件

本项目为 **华为手环 8（Huawei Band 8）** 提供了与 **Sleep as Android（SaA）** 深度联动的双向通信桥梁，旨在实现：
1. **夜间 1Hz 实时心率与体动推流**（为 SaA 提供真实生理数据，大幅提升 REM 快速眼动期识别率）；
2. **REM 阶段清醒梦微震提醒**（SaA 检测到 REM 后，通过手环震动给做梦者提供温和线索，且强度、次数、时长完全可调）；
3. **早晨智能唤醒连续强震**。

代码完全基于 **Gadgetbridge** 开源架构设计，既可独立编译为中继 APK 使用，也可直接合入 Gadgetbridge 官方源码提交 PR。

---

## 一、 文件目录结构

```
d:\antigravity\gadgetbridge-huawei\
├── app\
│   ├── build.gradle.kts
│   └── src\main\
│       ├── AndroidManifest.xml
│       └── java\nodomain\freeyourgadget\gadgetbridge\devices\huawei\
│           ├── HuaweiLucidSettings.java          // 清醒梦震动参数持久化配置中心
│           ├── HuaweiLucidSettingsActivity.java  // 手机端可视化配置与【一键测试震动】界面
│           ├── HuaweiSaABridgeService.java       // 安卓前台保活常驻服务 (监听 SaA 广播)
│           ├── HuaweiSleepAsAndroidSupport.java  // 核心适配器 (打通 SaA 与华为手环双向数据流)
│           ├── HuaweiWorkoutManager.java         // 运动模式伪装推流控制器 (常亮绿光 1Hz 广播)
│           ├── packets\
│           │   ├── HuaweiTLVBuilder.java         // 华为私有 TLV 报文封包与 CRC16 计算
│           │   ├── HuaweiTelemetryPacketParser.java // 遥测数据解包器 (解析 HR、体动、离腕)
│           │   └── HuaweiVibrateCommander.java   // 动态微震/强震指令组装器
│           └── test\
│               └── HuaweiSaABridgeSimulator.java // 全链路仿真模拟器 (自测验证用)
├── .github\workflows\
│   └── build-apk.yml                            // GitHub Actions 自动编译出 APK
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 二、 核心功能说明

### 1. 静默激活运动高频推流
* 当用户在 SaA 点击“开始睡眠追踪”时，发送 `com.urbandroid.sleep.watch.START_TRACKING`。
* 软件通过 TLV 报文伪装启动手环的“自由训练”模式（关闭 GPS 省电），使手环以 1Hz 频率连续向手机广播当前心率与体动。

### 2. 实时数据注入 SaA
* 拦截手环回传的 `0x02` 运动状态包，提取出实时心率（BPM）和 10 秒滑动窗口加速度（$m/s^2$）。
* 组装成官方标准 `com.urbandroid.sleep.watch.DATA_UPDATE` 显式广播（`MAX_RAW_DATA` + `HR_DATA`），直接注入 SaA 的睡眠分期引擎。

### 3. 可视化清醒梦微震调节与【一键测试】
在 App 的 `HuaweiLucidSettingsActivity` 界面中，用户可以自由调节：
* **震动强度：** 1. 微弱（适合手腕佩戴） / 2. 中等（适合大臂佩戴） / 3. 强力
* **震动次数：** 1次、2次（双击）、3次、4次
* **单次时长：** 50ms ~ 500ms 滑块调节
* **★ 一键测试按钮：** 睡前戴上手环，点击“点击测试手环震动”，手环立刻按当前参数震动，无需熬夜就能直观评估力度是否合适。

### 4. 离腕自动挂起保护
* 监测到手环离腕脱落时，自动向 SaA 发送 `SET_PAUSE` 广播，防止桌面杂音导致算法误判。

---

## 三、 使用与配置指南

### 1. 编译生成 APK
* **方式 A（推荐，自动化）：** 将本文件夹代码推送到 GitHub，GitHub Actions 会在几分钟内自动编译生成 `HuaweiSaABridge-debug.apk`，在 Releases/Artifacts 中直接下载安装；
* **方式 B（本地编译）：** 用 Android Studio 打开 `d:\antigravity\gadgetbridge-huawei`，点击 `Build -> Build APK(s)` 即可。

### 2. Sleep as Android (SaA) 端配置
1. 打开 SaA $\to$ `设置` $\to$ `可穿戴设备` $\to$ `使用可穿戴设备` $\to$ 选择 **`DIY`**；
2. 确认包名为：`nodomain.freeyourgadget.gadgetbridge`；
3. 勾选 **`心率监测`**；
4. 进入 `清醒梦（Lucid Dreaming）` $\to$ 勾选 `启用`，勾选 `在可穿戴设备上震动`。

### 3. 日常睡前操作
1. 睡前戴上华为手环 8，打开本 App 确认参数并点击“启动睡眠联动后台服务”；
2. 打开 SaA，点击“开始睡眠追踪”；
3. 观察手环绿光亮起，手机 SaA 界面跳动显示实时心率，即可安心入睡！

---

## 四、 向 Gadgetbridge 官方提交 PR 说明

本项目的代码命名空间完全沿用了 Gadgetbridge 官方主干结构：
* 将 `HuaweiSleepAsAndroidSupport.java`、`HuaweiWorkoutManager.java` 以及 `packets/` 目录拷贝至 Gadgetbridge 源码仓库的 `nodomain.freeyourgadget.gadgetbridge.devices.huawei` 下；
* 在 `HuaweiSupport.java` 中注册并转发生命周期事件；
* 即可向 Codeberg 的 `Freeyourgadget/Gadgetbridge` 仓库发起 Pull Request。
