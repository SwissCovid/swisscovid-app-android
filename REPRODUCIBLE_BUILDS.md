
# Reproducible Builds

## Install Docker

Download and install [Docker](https://www.docker.com/).

## Check your SwissCovid app version

1. Open the SwissCovid app
2. Click on the `i` button in the top-right corner
3. Check the version number, e.g., 1.0.1-pilot

## Download the App open-source code

1. Make sure you have `git` installed
2. Clone the Github repository
3. Checkout the Tag that corresponds to the version of your SwissCovid app, e.g., 1.0.1-pilot

```shell
git clone https://github.com/DP-3T/dp3t-app-android-ch.git ~/dp3t-app-android-ch
cd ~/dp3t-app-android-ch
git checkout 1.0.1-pilot
```

## Build the project using Docker

1. Build a Docker Image with the required Android Tools
2. Build the App in a Docker Container
3. Copy the freshly-built APK

```shell
cd ~/dp3t-app-android-ch
docker build -t swisscovid-builder .
docker run --rm -v ~/dp3t-app-android-ch:/home/swisscovid -w /home/swisscovid swisscovid-builder gradle assembleProdRelease -PkeystorePassword=securePassword -PkeyAliasPassword=securePassword -PkeystoreFile=build.keystore
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

## Compare the two files

1. Make sure you have `python` installed
2. Use the `apkdiff` script to compare the APKs

```shell
cd ~/dp3t-app-android-ch
python apkdiff.py swisscovid-built.apk swisscovid-store.apk
```
