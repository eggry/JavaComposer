@echo off
choice /m "�Ƿ��������ļ��е�����midi�ļ��������벻��ӦĬ�ϲ������" /t 5 /d n
if %errorlevel%==1 goto deleteAll

:main
echo=
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
goto end

:deleteAll
echo=
del .\testfiles\output\*.mid
echo ��ճɹ���
goto main

:end
