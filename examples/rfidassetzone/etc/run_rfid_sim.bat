@echo off

REM Script to run RFID simulator
REM

call setenv.bat

set MEMORY_OPTIONS=-Xms512m -Xmx512m -server

"%JAVA_HOME%"\bin\java -Dcom.sun.management.jmxremote %MEMORY_OPTIONS% -Dlog4j.configuration=log4j.xml com.espertech.esper.example.rfidassetzone.LRMovingSimMain %1 %2 %3