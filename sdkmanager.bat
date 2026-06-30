@echo off
echo [Proxy Helper] Running sdkmanager with hardcoded corporate proxy: baproxy-cloud.baia.baplc.com:8082
call C:\Users\n533750.WW\Documents\Software\Android\cmdline-tools\latest\bin\sdkmanager.bat --proxy=http --proxy_host=baproxy-cloud.baia.baplc.com --proxy_port=8082 %*
