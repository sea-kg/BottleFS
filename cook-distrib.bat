@echo off

rem build bottlefs
pushd bottlefs
call gradle build
xcopy /Y build\libs\bottlefs.jar ..\distrib\libs\
popd

pushd distrib

if not exist libs/org.json-20120521.jar (
  curl https://org-json-java.googlecode.com/files/org.json-20120521.jar > libs/org.json-20120521.jar
)

popd




exit;

if not exist  (
  echo * git cloning source files into "%ProjectRoot%"...
  git clone git@startext.tomsk.ru:actapro/actapro.git "%ProjectRoot%"
) else (
  echo * git updating source files in "%ProjectRoot%"...
  pushd "%ProjectRoot%"
  call _Delete_temp_files.cmd
  git checkout -f
  git clean -d -f
  git pull
  popd
)
if errorlevel 1 exit 1

