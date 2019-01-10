@echo off
echo song.txt:%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\song.txt > .\testfiles\outputLog\song.log
echo vocaloidOrSong.txt:%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\vocaloidOrSong.txt > .\testfiles\outputLog\vocaloidOrSong.log
echo allChaosSong.txt:%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\allChaosSong.txt > .\testfiles\outputLog\allChaosSong.log
echo allVocaloid.txt:%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\allVocaloid.txt > .\testfiles\outputLog\allVocaloid.log
echo chaosVocaloid.txt:%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\chaosVocaloid.txt > .\testfiles\outputLog\chaosVocaloid.log
echo game.txt:%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\game.txt > .\testfiles\outputLog\game.log
echo pureVocaloid.txt:%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\pureVocaloid.txt > .\testfiles\outputLog\pureVocaloid.log
echo void.txt:%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\void.txt > .\testfiles\outputLog\void.log
echo all.txt:%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\all.txt > .\testfiles\outputLog\all.log
echo end:%time%
explorer.exe .\testfiles\outputLog
explorer.exe .\testfiles
pause