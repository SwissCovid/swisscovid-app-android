
# Reproducible Builds

Note: reproducible builds work starting version 1.0.3

## Install Docker

Download and install [Docker](https://www.docker.com/).

## Check your SwissCovid app version and build timestamp

1. Open the SwissCovid app
2. Click on the `i` button in the top-right corner
3. Check the app version in the top right corner, which is the text between 'Version' and the comma (e.g., 1.0.8), and record its value to be used later
4. Check the build timestamp in the bottom right corner, which is the number before the slash (e.g., 1591722151141), and record its value to be used later

## Download the App open-source code

1. Make sure you have `git` installed
2. Clone the Github repository
3. Checkout the Tag that corresponds to the version of your SwissCovid app (e.g., 1.0.8)

```shell
git clone https://github.com/DP-3T/dp3t-app-android-ch.git ~/dp3t-app-android-ch
cd ~/dp3t-app-android-ch
git checkout 1.0.8
```

## Build the project using Docker

1. Build a Docker Image with the required Android Tools
2. Build the App in the Docker Container while specifying the build timestamp that was recorded earlier (e.g., 1595936711208)
3. Copy the freshly-built APK

```shell
cd ~/dp3t-app-android-ch
docker build -t swisscovid-builder .
docker run --rm -v ~/dp3t-app-android-ch:/home/swisscovid -w /home/swisscovid swisscovid-builder gradle assembleProdRelease -PkeystorePassword=securePassword -PkeyAliasPassword=securePassword -PkeystoreFile=build.keystore -PbuildTimestamp=1595936711208
cp app/build/outputs/apk/prod/release/app-prod-release.apk swisscovid-built.apk
```

## Extract the Play Store APK from your phone

1. Make sure you have `adb` installed
2. Connect your phone to your computer
3. Extract the APK from the phone

```shell
cd ~/dp3t-app-android-ch
adb pull `adb shell pm path ch.admin.bag.dp3t | cut -d':' -f2` swisscovid-store.apk
```

If you want to check the version of the APK you are pulling from your device:

```shell
adb shell dumpsys package ch.admin.bag.dp3t | grep versionName=| cut -d '=' -f 2
```

## Compare the two files

1. Make sure you have `python` installed
2. Use the `apkdiff` script to compare the APKs

```shell
cd ~/dp3t-app-android-ch
python apkdiff.py swisscovid-built.apk swisscovid-store.apk
```
