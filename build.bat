@echo off
chcp 65001 > nul
echo =========================================
echo Kompilacja i budowanie pliku EXE...
echo =========================================

if exist out rmdir /s /q out
if exist out_jar rmdir /s /q out_jar
if exist dist rmdir /s /q dist

mkdir out
mkdir out_jar

echo [1/3] Kompilacja plikow zrodlowych (UTF-8)...
javac -encoding UTF-8 -d out src\*.java
if %errorlevel% neq 0 (
    echo Blad kompilacji!
    exit /b %errorlevel%
)

copy src\jumpscare1.png out\ > nul

echo [2/3] Tworzenie pliku JAR...
jar --create --file=out_jar\GraWASD.jar --main-class=Main -C out .
if %errorlevel% neq 0 (
    echo Blad podczas tworzenia JAR!
    exit /b %errorlevel%
)

echo [3/3] Budowanie aplikacji EXE za pomoca jpackage...
jpackage --type app-image --name "Welcome to Guts" --input out_jar --dest dist --main-jar GraWASD.jar --main-class Main
if %errorlevel% neq 0 (
    echo Blad podczas jpackage!
    exit /b %errorlevel%
)

echo =========================================
echo Gotowe! Aplikacja EXE znajduje sie w folderze:
echo dist\Welcome to Guts\Welcome to Guts.exe
echo =========================================
