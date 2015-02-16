@echo off

cd bottlefs

if exist "build/distributions/bottlefs.zip" (
  rm "build/distributions/bottlefs.zip"
)
if errorlevel 1 exit 1

call gradle clean makezip

if not exist "build/distributions/bottlefs.zip" (
  echo "not found file .zip"
  exit;
)

if exist "..\test" (
  rm -rf ../test/*
) else (
  echo mkdir
  mkdir ..\test
)

if exist "../test/bottlefs.zip" (
	rm "../test/bottlefs.zip"
)

copy /Y "build\distributions\bottlefs.zip" "..\test"

cd ..\test

unzip bottlefs.zip
rm bottlefs.zip


