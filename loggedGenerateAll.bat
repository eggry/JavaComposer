@echo off
choice /m "是否清空输出文件夹的所有midi文件？（五秒不反应默认不清除）" /t 5 /d n
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
echo 打开生成文件夹？
echo=
pause
explorer.exe .\testfiles\output
goto end

:deleteAll
echo=
del .\testfiles\output\*.mid
echo 清空成功！
goto main

:end
