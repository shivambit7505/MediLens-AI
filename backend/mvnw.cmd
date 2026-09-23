@echo off
setlocal
if not defined JAVA_HOME (
    if exist "C:\Program Files\Android\Android Studio\jbr" (
        set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    )
)
if exist "%JAVA_HOME%\bin" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)
if exist "%~dp0..\.tools\apache-maven-3.9.6\bin\mvn.cmd" (
    call "%~dp0..\.tools\apache-maven-3.9.6\bin\mvn.cmd" %*
) else (
    mvn %*
)
endlocal
