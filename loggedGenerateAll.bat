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
echo �������ļ��У�
echo=
pause
explorer.exe .\testfiles\output


