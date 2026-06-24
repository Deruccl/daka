@echo off
REM ====================================
REM 时光印记 TimeMark - Release 构建脚本 (Windows Batch)
REM ====================================
REM 功能：
REM   1. 检查签名配置（keystore.properties）
REM   2. 执行 Gradle assembleRelease
REM   3. 输出 APK 路径
REM 用法：双击运行或在命令行执行 build-and-sign.bat
REM ====================================

setlocal enabledelayedexpansion
chcp 65001 >nul

echo ============================================
echo   时光印记 TimeMark - Release 构建脚本
echo ============================================
echo.

REM 切换到脚本所在目录（项目根目录）
cd /d "%~dp0"

REM ---------- 步骤 1：检查签名配置 ----------
echo [1/3] 检查签名配置...
if not exist "keystore.properties" (
    echo.
    echo [错误] 未找到 keystore.properties 文件！
    echo.
    echo 请按以下步骤操作：
    echo   1. 复制 keystore.properties.example 为 keystore.properties
    echo   2. 填入真实的密钥库信息
    echo   3. 将 .jks 密钥库文件放在项目根目录
    echo.
    echo 示例：
    echo   copy keystore.properties.example keystore.properties
    echo.
    exit /b 1
)
echo   [√] keystore.properties 已就绪

if not exist "*.jks" (
    echo   [!] 警告：未找到 .jks 密钥库文件，请确认 keystore.properties 中 storeFile 路径正确
) else (
    echo   [√] 密钥库文件已就绪
)
echo.

REM ---------- 步骤 2：执行 Gradle 构建 ----------
echo [2/3] 开始执行 Gradle assembleRelease...
echo.

REM 优先使用 gradlew.bat，若不存在则使用全局 gradle
set "GRADLE_CMD=gradlew.bat"
if not exist "%GRADLE_CMD%" (
    echo [!] 未找到 gradlew.bat，尝试使用全局 gradle...
    set "GRADLE_CMD=gradle"
)

call %GRADLE_CMD% assembleRelease --no-daemon
if errorlevel 1 (
    echo.
    echo [错误] 构建失败！请检查上方错误信息。
    exit /b 1
)
echo.
echo   [√] 构建成功
echo.

REM ---------- 步骤 3：输出 APK 路径 ----------
echo [3/3] 查找生成的 APK 文件...
echo.
echo ============================================
echo   构建完成！APK 文件位于：
echo ============================================
set "APK_DIR=app\build\outputs\apk\release"
if exist "%APK_DIR%" (
    dir /b "%APK_DIR%\*.apk"
    echo.
    echo 完整路径：%CD%\%APK_DIR%\
) else (
    echo [警告] 未在 %APK_DIR% 找到 APK，请检查 build\outputs 目录
)
echo.
echo ============================================
pause
