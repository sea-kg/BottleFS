@ECHO OFF
SET "JAVA_HOME=C:\java\jdk7-32\jdk1.7.0_45"
echo JAVA_HOME: %JAVA_HOME%
C:\java\jdk7-32\jdk1.7.0_45\bin\java.exe -version
call gradle --refresh-dependencies
call gradle clean build
call gradle build

pushd bottlefs
 call gradle build
popd