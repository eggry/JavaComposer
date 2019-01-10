@echo off
echo Generating All...
call generateAll > .\testfiles\outputLog\generateAll.log
echo=
echo GeneratingOK!
echo=
echo TimeResult:
echo ----------------------------------------
type .\testfiles\outputLog\generateAll.log
echo ----------------------------------------
echo=
echo 打开生成文件夹？
echo=
pause
explorer.exe .\testfiles\output


