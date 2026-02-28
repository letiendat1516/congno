@echo off
set JAVA_HOME=C:\Users\utube\.jdks\ms-17.0.18
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\Users\utube\IdeaProjects\whmanagement
call "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd" compile -B

