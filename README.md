# 水稻叶片病害检查系统

## 环境要求

| 工具 | 版本 |
|------|------|
| Java JDK | 17 |
| Maven | 3.8+ |
| Android SDK | API 34, build-tools 34.0.0 |
| Gradle | wrapper 自动下载 8.5 |

开发阶段使用 H2 内嵌数据库，无需安装 MySQL。

---

## 一、启动后端

```bash
cd rice-leaf-server
mvn spring-boot:run
```

验证：

```bash
curl http://localhost:8088/api/v1/diseases/list
```

服务信息：

| 项目 | 地址 |
|------|------|
| API 基础路径 | `http://localhost:8088/api/v1/` |
| H2 控制台 | `http://localhost:8088/h2-console` |

---

## 二、连接手机（WSL2 USB 透传）

### Windows 侧（首次，PowerShell 管理员）

```powershell
# 安装 usbipd-win
winget install usbipd

# 列出 USB 设备
usbipd wsl list

# 绑定 Android 手机到 WSL2（用上一步查到的 BUSID）
usbipd wsl attach --busid <BUSID>
```

### WSL2 侧

```bash
export ANDROID_HOME=$HOME/Android
export PATH=$ANDROID_HOME/platform-tools:$PATH

# 验证设备连接
adb devices
```

---

## 三、配置服务器地址

真机需要通过 Windows 主机 IP 访问 WSL2 中的后端。
#防火墙放行
netsh advfirewall firewall add rule name="RiceLeaf 8080" dir=in action=allow protocol=tcp localport=8080

查看 Windows 主机 IP：

```bash
# 在 WSL2 中执行
ip route show default | awk '{print $3}'
```

修改 Android 项目中 `RetrofitClient.kt` 的 `BASE_URL`：

```
app/src/main/java/com/riceleaf/app/network/RetrofitClient.kt
const val BASE_URL = "http://<Windows主机IP>:8088/api/v1/"
```

示例：如果 Windows IP 是 `172.25.176.1`，则：
```
const val BASE_URL = "http://172.25.176.1:8088/api/v1/"
```

修改后重新编译。

---

## 四、编译并安装 Android 应用

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=$HOME/Android

cd rice-leaf-android

# 编译
./gradlew assembleDebug

# 安装到手机
adb install app/build/outputs/apk/debug/app-debug.apk
```

或一步完成：
```bash
./gradlew installDebug
```
#启动推理服务
python inference_server.py
#验证推理服务正常
curl http://localhost:5000/health，确认返回 "model_loaded": true。
---

## 五、快捷启动（全流程）

```bash
# 1. 启动后端（后台）
cd rice-leaf-server && mvn spring-boot:run &

# 2. 等待启动完成（约20秒）
sleep 20

# 3. 编译安装 Android（手机需已连接）
cd ../rice-leaf-android && ./gradlew installDebug
```

---

## API 速查

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/diseases/list` | 病害列表 |
| POST | `/api/v1/diseases/detect` | 上传图片识别 |
| GET | `/api/v1/records?page=1&size=20` | 分页查询记录 |
| GET | `/api/v1/records/{id}` | 记录详情 |
| PUT | `/api/v1/records/{id}` | 修改记录 |
| DELETE | `/api/v1/records/{id}` | 删除记录 |
| GET | `/api/v1/records/export?format=xlsx` | 导出 Excel |
