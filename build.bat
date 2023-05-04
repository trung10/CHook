./gradlew.bat assembleRelease
adb uninstall com.facepro.camerahook
adb install ./app/build/outputs/apk/release/app-release.apk


