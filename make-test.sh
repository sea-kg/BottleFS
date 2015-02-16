#!/bin/bash

cd bottlefs

if [ -f "build/distributions/bottlefs.zip" ]; then
	rm "build/distributions/bottlefs.zip"
fi


gradle makezip

if [ ! -f "build/distributions/bottlefs.zip" ]; then
	echo "not found file .zip"
	exit;
fi

if [ -d "../test" ]; then
	rm -rf ../test/*
else
	mkdir ../test
fi

if [ -f "../test/bottlefs.zip" ]; then
	rm "../test/bottlefs.zip"
fi

cp "build/distributions/bottlefs.zip" "../test"

cd ../test

unzip bottlefs.zip
rm bottlefs.zip


