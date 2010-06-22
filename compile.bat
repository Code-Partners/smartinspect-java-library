@echo off
@set LIBRARY=SmartInspect.jar
@set SOURCE=com\gurock\smartinspect
@echo Creating %LIBRARY%

mkdir dist
javac -g:none -d dist %SOURCE%\*.java
jar cf %LIBRARY% -C dist .
