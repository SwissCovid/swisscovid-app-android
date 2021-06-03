#!/bin/bash
set -e

echo "Which app would you like to build (type 'app' or 'instant')?"
read appName

echo Please enter KeystoreFile name:
read keystoreFile

echo Please enter Keystore Password:
read -s keystorePassword

echo Please enter KeyAlias:
read keyAlias

echo Please enter KeyAlias Password:
read -s keyAliasPassword

if [ "$appName" = "app" ] ; then
  echo Please enter Build Timestamp:
  read buildTimestamp
fi

#make sure we have a full clean build
rm -rf $appName/build
rm -rf .gradle

docker build -t swisscovid-builder .
currentPath=`pwd`

if [ "$appName" = "app" ] ; then
  command='assembleProdRelease'
else
  command='bundleProdRelease'
fi
docker run --rm -v $currentPath:/home/swisscovid -w /home/swisscovid swisscovid-builder gradle $appName:$command -PkeystorePassword=$keystorePassword -PkeyAlias=$keyAlias -PkeyAliasPassword=$keyAliasPassword -PkeystoreFile=$keystoreFile -PbuildTimestamp=$buildTimestamp


if [ "$appName" = "app" ] ; then
  cp $appName/build/outputs/apk/prod/release/$appName-prod-release.apk $appName-built.apk

  if [[ $# -eq 1 ]] ; then
    echo Comparing the built APK with the reference:
    python apkdiff.py $appName-built.apk $1
  fi
fi