@echo off
setlocal enabledelayedexpansion

set PROXY_HOST=
set PROXY_PORT=

set INPUT_FILE=%~dp0input.txt

if not exist "%INPUT_FILE%" (
    echo Error: input.txt not found at %INPUT_FILE%
    exit /b 1
)

for /f "usebackq tokens=1,2 delims==" %%A in ("%INPUT_FILE%") do (
    set "key=%%A"
    set "value=%%B"
    
    rem Strip spaces from key and value
    set "key=!key: =!"
    set "value=!value: =!"
    
    if "!key!"=="systemProp.http.proxyHost" set "PROXY_HOST=!value!"
    if "!key!"=="systemProp.http.proxyPort" set "PROXY_PORT=!value!"
)

if not "%PROXY_HOST%"=="" (
    echo [Proxy Helper] Found proxy settings in input.txt: %PROXY_HOST%:%PROXY_PORT%
    call C:\Users\n533750.WW\Documents\Software\cmdline-tools\latest\bin\sdkmanager.bat --proxy=http --proxy_host=%PROXY_HOST% --proxy_port=%PROXY_PORT% %*
) else (
    echo [Proxy Helper] No proxy settings found in input.txt, running directly.
    call C:\Users\n533750.WW\Documents\Software\cmdline-tools\latest\bin\sdkmanager.bat %*
)
