@echo off

echo void:		%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\void.txt > .\testfiles\outputLog\void.log

echo pureVocaloid:	%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\pureVocaloid.txt > .\testfiles\outputLog\pureVocaloid.log

echo chaosVocaloid:	%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\chaosVocaloid.txt > .\testfiles\outputLog\chaosVocaloid.log

echo allVocaloid:	%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\allVocaloid.txt > .\testfiles\outputLog\allVocaloid.log

echo song:		%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\song.txt > .\testfiles\outputLog\song.log

echo allChaosSong:	%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\allChaosSong.txt > .\testfiles\outputLog\allChaosSong.log

echo vocaloidOrSong:	%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\vocaloidOrSong.txt > .\testfiles\outputLog\vocaloidOrSong.log

echo game:		%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\game.txt > .\testfiles\outputLog\game.log

echo all:		%time%
java -jar .\bin\JavaComposer.jar < .\testfiles\inputTextFile\all.txt > .\testfiles\outputLog\all.log

echo end:		%time%
