# OpenWaterDetector — 1.21.8 Port

釣魚時顯示是否處於開放水域的 Fabric Mod。

- 🟢 綠色粒子 = 開放水域（可釣到寶藏）
- 🔥 火焰粒子 = 非開放水域
- 按 **O** 鍵可開關偵測

---

## 環境需求

- Java 21+
- Gradle（使用 gradlew 不需另外安裝）

---

## Build 步驟（Windows）

```bat
gradlew.bat build
```

完成後 `.jar` 檔案會在：
```
build/libs/OpenWaterDetector-1.0.0.jar
```

把這個 `.jar` 放進 Minecraft 的 `mods/` 資料夾即可。

> 如果沒有 `gradlew.bat`，請從 Fabric Example Mod 複製：
> https://github.com/FabricMC/fabric-example-mod

---

## 依賴

- Fabric Loader >= 0.16.0
- Fabric API
- Minecraft 1.21.8
- Java 21
