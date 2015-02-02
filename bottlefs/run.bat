@echo off
call gradle clean build
call java -Dfile.encoding=UTF-8  -cp "build/libs/*" com.seakg.bottlefs.WebServer
