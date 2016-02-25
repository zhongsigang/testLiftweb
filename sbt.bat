set SCRIPT_DIR=%~dp0
"%java_home%\bin\java" -XX:+CMSClassUnloadingEnabled -Xmx1024M -Dinput.encoding=Cp1252 -jar "%SCRIPT_DIR%sbt-launch.jar" %*
