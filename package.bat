@echo off
setlocal
set JAVA_HOME=C:\Users\utube\.jdks\ms-17.0.18
set PATH=%JAVA_HOME%\bin;%PATH%
set MVN="C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd"
set PROJECT=C:\Users\utube\IdeaProjects\whmanagement
set TARGET=%PROJECT%\target
set DIST=%PROJECT%\dist

echo ============================================
echo  BUOC 1: Compile va dong goi fat JARs
echo ============================================
cd /d %PROJECT%
call %MVN% clean package -B -q
if errorlevel 1 (
    echo [LOI] Maven build that bai!
    pause
    exit /b 1
)
echo [OK] Maven build thanh cong.

:: Tao thu muc dist
if not exist "%DIST%" mkdir "%DIST%"

:: Xoa output cu
if exist "%DIST%\QuanLyKho"    rmdir /s /q "%DIST%\QuanLyKho"
if exist "%DIST%\GenLicenseKey" rmdir /s /q "%DIST%\GenLicenseKey"

echo.
echo ============================================
echo  BUOC 2: Dong goi QuanLyKho.exe (app chinh)
echo ============================================
%JAVA_HOME%\bin\jpackage.exe ^
    --type app-image ^
    --name "QuanLyKho" ^
    --input "%TARGET%" ^
    --main-jar "QuanLyKho-fat.jar" ^
    --main-class "com.dat.whmanagement.Main" ^
    --dest "%DIST%" ^
    --java-options "--add-opens=java.base/java.lang=ALL-UNNAMED" ^
    --java-options "--add-opens=java.base/java.nio=ALL-UNNAMED" ^
    --app-version "1.0" ^
    --vendor "DAT Software" ^
    --description "Phan mem Quan Ly Kho Hang"

if errorlevel 1 (
    echo [LOI] jpackage QuanLyKho that bai!
    pause
    exit /b 1
)
echo [OK] QuanLyKho.exe da tao thanh cong.

echo.
echo ============================================
echo  BUOC 3: Dong goi GenLicenseKey.exe (tool tao key)
echo ============================================
%JAVA_HOME%\bin\jpackage.exe ^
    --type app-image ^
    --name "GenLicenseKey" ^
    --input "%TARGET%" ^
    --main-jar "GenLicenseKey-fat.jar" ^
    --main-class "com.dat.whmanagement.license.LicenseGenerator" ^
    --dest "%DIST%" ^
    --app-version "1.0" ^
    --vendor "DAT Software" ^
    --description "Tool tao License Key"

if errorlevel 1 (
    echo [LOI] jpackage GenLicenseKey that bai!
    pause
    exit /b 1
)
echo [OK] GenLicenseKey.exe da tao thanh cong.

echo.
echo ============================================
echo  HOAN THANH! Ket qua o thu muc:
echo  %DIST%
echo ============================================
echo.
echo  QuanLyKho\QuanLyKho.exe    - App chinh cho khach hang
echo  GenLicenseKey\GenLicenseKey.exe - Tool tao key (chi danh cho ban)
echo.
explorer "%DIST%"
pause

