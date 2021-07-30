@echo off
@set LIBRARY=SmartInspect.JDK.jar
@set SOURCE=com\gurock\smartinspect\jdk
@echo Creating %LIBRARY%

mkdir dist
javac -g:none -d dist %SOURCE%\*.java -classpath "..\SmartInspect.jar"
jar cf %LIBRARY% -C dist .
