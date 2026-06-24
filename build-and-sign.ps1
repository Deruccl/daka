# ====================================
# 时光印记 TimeMark - Release 构建脚本 (PowerShell)
# ====================================
# 功能：
#   1. 检查签名配置（keystore.properties）
#   2. 执行 Gradle assembleRelease
#   3. 输出 APK 路径与构建摘要
# 用法：在 PowerShell 中执行 .\build-and-sign.ps1
# ====================================

#Requires -Version 5.1

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# 切换到脚本所在目录（项目根目录）
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

# 辅助函数：带颜色输出
function Write-Step { param([string]$msg) Write-Host "`n>>> $msg" -ForegroundColor Cyan }
function Write-Ok   { param([string]$msg) Write-Host "  [√] $msg" -ForegroundColor Green }
function Write-Warn { param([string]$msg) Write-Host "  [!] $msg" -ForegroundColor Yellow }
function Write-Err  { param([string]$msg) Write-Host "  [X] $msg" -ForegroundColor Red }

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   时光印记 TimeMark - Release 构建脚本"      -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# ---------- 步骤 1：检查签名配置 ----------
Write-Step "[1/3] 检查签名配置..."

$keystoreProps = Join-Path $ScriptDir "keystore.properties"
if (-not (Test-Path $keystoreProps)) {
    Write-Err "未找到 keystore.properties 文件！"
    Write-Host ""
    Write-Host "请按以下步骤操作：" -ForegroundColor Yellow
    Write-Host "  1. 复制 keystore.properties.example 为 keystore.properties"
    Write-Host "  2. 填入真实的密钥库信息"
    Write-Host "  3. 将 .jks 密钥库文件放在项目根目录"
    Write-Host ""
    Write-Host "示例命令：" -ForegroundColor Yellow
    Write-Host "  Copy-Item keystore.properties.example keystore.properties"
    Write-Host ""
    exit 1
}
Write-Ok "keystore.properties 已就绪"

$jksFiles = Get-ChildItem -Path $ScriptDir -Filter "*.jks" -ErrorAction SilentlyContinue
if ($jksFiles) {
    Write-Ok "密钥库文件已就绪：$($jksFiles.Name)"
} else {
    Write-Warn "未找到 .jks 密钥库文件，请确认 keystore.properties 中 storeFile 路径正确"
}

# ---------- 步骤 2：执行 Gradle 构建 ----------
Write-Step "[2/3] 开始执行 Gradle assembleRelease..."

# 优先使用 gradlew.bat，若不存在则使用全局 gradle
$gradleCmd = $null
if (Test-Path "gradlew.bat") {
    $gradleCmd = ".\gradlew.bat"
} elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
    $gradleCmd = "gradle"
    Write-Warn "未找到 gradlew.bat，使用全局 gradle"
} else {
    Write-Err "未找到 Gradle，请确保 gradlew.bat 存在或已安装全局 Gradle"
    exit 1
}

Write-Host "  执行命令：$gradleCmd assembleRelease --no-daemon" -ForegroundColor DarkGray
& $gradleCmd assembleRelease --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Err "构建失败！请检查上方错误信息。"
    exit 1
}
Write-Ok "构建成功"

# ---------- 步骤 3：输出 APK 路径与摘要 ----------
Write-Step "[3/3] 查找生成的 APK 文件..."

$apkDir = Join-Path $ScriptDir "app\build\outputs\apk\release"
if (Test-Path $apkDir) {
    $apkFiles = Get-ChildItem -Path $apkDir -Filter "*.apk" -ErrorAction SilentlyContinue
    if ($apkFiles) {
        Write-Host ""
        Write-Host "============================================" -ForegroundColor Green
        Write-Host "  构建完成！APK 文件列表：" -ForegroundColor Green
        Write-Host "============================================" -ForegroundColor Green
        foreach ($apk in $apkFiles) {
            $sizeMB = [math]::Round($apk.Length / 1MB, 2)
            Write-Host ("  {0,-50} {1,8} MB" -f $apk.Name, $sizeMB) -ForegroundColor White
        }
        Write-Host ""
        Write-Host "  输出目录：$apkDir" -ForegroundColor DarkGray
        Write-Host ""
    } else {
        Write-Warn "在 $apkDir 未找到 APK 文件"
    }
} else {
    Write-Warn "输出目录不存在：$apkDir"
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  构建脚本执行完毕" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
