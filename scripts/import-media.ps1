#requires -Version 5.1
<#
.SYNOPSIS
    导入完整曲库：解压媒体包 -> 补全扩展名 -> 导入数据库。

.EXAMPLE
    pwsh scripts/import-media.ps1 -Zip "D:\BaiduNetdiskDownload\vibe-music-data.zip" -Target "D:\linyuan-media"

.NOTES
    1) 需要本机已安装 MySQL 客户端（默认取 D:\MySQL\MySQL Server 8.0\bin\mysql.exe，可用 -MySQL 指定）；
    2) 会先 DROP 再 CREATE 数据库 linyuan_music，请确认无重要数据；
    3) 导入完成后用 STORAGE_DIR=<Target> 启动后端。
#>
param(
    [Parameter(Mandatory = $true)][string]$Zip,
    [Parameter(Mandatory = $true)][string]$Target,
    [string]$MySQL = "D:\MySQL\MySQL Server 8.0\bin\mysql.exe",
    [string]$DbUser = "root",
    [string]$DbPassword = "123456",
    [string]$DbName = "linyuan_music"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $Zip)) { throw "找不到媒体包：$Zip" }
if (-not (Test-Path $MySQL)) { throw "找不到 mysql 客户端：$MySQL（可用 -MySQL 指定路径）" }

Write-Host "[1/4] 解压到 $Target ..."
if (Test-Path $Target) { Remove-Item $Target -Recurse -Force }
New-Item -ItemType Directory -Path $Target | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory($Zip, $Target)

Write-Host "[2/4] 为 -blob 文件补全扩展名 ..."
$blobFiles = Get-ChildItem $Target -Recurse -File | Where-Object { $_.Name -like "*-blob" }
foreach ($f in $blobFiles) {
    $fs = [System.IO.File]::OpenRead($f.FullName)
    $buf = New-Object byte[] 12
    $n = $fs.Read($buf, 0, 12)
    $fs.Close()
    $ext = ".bin"
    if ($n -ge 8 -and $buf[0] -eq 0x89 -and $buf[1] -eq 0x50 -and $buf[2] -eq 0x4E -and $buf[3] -eq 0x47) { $ext = ".png" }
    elseif ($n -ge 3 -and $buf[0] -eq 0xFF -and $buf[1] -eq 0xD8 -and $buf[2] -eq 0xFF) { $ext = ".jpg" }
    elseif ($n -ge 12 -and $buf[8] -eq 0x57 -and $buf[9] -eq 0x45 -and $buf[10] -eq 0x42 -and $buf[11] -eq 0x50) { $ext = ".webp" }
    elseif ($n -ge 3 -and $buf[0] -eq 0x47 -and $buf[1] -eq 0x49 -and $buf[2] -eq 0x46) { $ext = ".gif" }
    Rename-Item $f.FullName ($f.Name + $ext)
}
Write-Host "      处理 $($blobFiles.Count) 个文件"

Write-Host "[3/4] 建库并导入 sql/linyuan_music_full.sql ..."
$sqlFile = Join-Path $PSScriptRoot "..\sql\linyuan_music_full.sql"
if (-not (Test-Path $sqlFile)) { throw "找不到 $sqlFile" }

$create = "DROP DATABASE IF EXISTS $DbName; CREATE DATABASE $DbName CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
& cmd /c "`"$MySQL`" --user=$DbUser --password=$DbPassword --host=127.0.0.1 --port=3306 --default-character-set=utf8mb4 -e `"$create`""
& cmd /c "`"$MySQL`" --user=$DbUser --password=$DbPassword --host=127.0.0.1 --port=3306 --default-character-set=utf8mb4 $DbName < `"$sqlFile`""

Write-Host "[4/4] 完成。启动方式："
Write-Host "      `$env:STORAGE_DIR=`"$($Target -replace '\\','/')`""
Write-Host "      java -jar linyuan-music-backend/linyuan-music-server/target/linyuan-music-server-0.0.1-SNAPSHOT.jar"
