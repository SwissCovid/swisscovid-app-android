<h1 align="center">SwissCovid Android App</h1>
<br />
<div align="center">
  <img width="180" height="180" src="app/src/main/ic_launcher-playstore.png" />
  <br />
  <div>
    <!-- App Store -->
    <a href="https://play.google.com/store/apps/details?id=ch.admin.bag.dp3t">
      <img height="40" src="https://bag-coronavirus.ch/wp-content/uploads/2020/04/play-store.png" alt="Download on the PlayStore" />
    </a>
  </div>
</div>

[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://github.com/SwissCovid/swisscovid-app-android/blob/master/LICENSE)
![Android Build](https://github.com/SwissCovid/swisscovid-app-android/workflows/Android%20Build/badge.svg)

SwissCovid is the official contact tracing app of Switzerland. The app can be installed from the [Google Play Store](https://play.google.com/store/apps/details?id=ch.admin.bag.dp3t). The SwissCovid 2.0 app uses two types of contact tracing to prevent the spread of COVID-19.

With proximity tracing close contacts are detected using the bluetooth technology. For this the [DP3T Android SDK](https://github.com/DP-3T/dp3t-sdk-android) is used that builds on top of the Google & Apple Exposure Notifications. This feature is called SwissCovid encounters.

With presence tracing people that are at the same venue at the same time are detected. For this the [CrowdNotifier Android SDK](https://github.com/CrowdNotifier/crowdnotifier-sdk-android) is used that provides a secure, decentralized, privacy-preserving presence tracing system. This feature is called SwissCovid Check-in.

Please see the [SwissCovid documentation repository](https://github.com/SwissCovid/swisscovid-doc) for more details.

## Contribution Guide

This project is truly open-source and we welcome any feedback on the code regarding both the implementation and security aspects.

Bugs or potential problems should be reported using Github issues. We welcome all pull requests that improve the quality of the source code. Please note that the app will be available with approved translations in English, German, French, Italian, Romansh, Albanian, Bosnian, Croatian, Portuguese, Serbian and Spanish. Pull requests for additional translations currently won't be merged.

Platform independent UX and design discussions should be reported in [dp3t-ux-screenflows-ch](https://github.com/DP-3T/dp3t-ux-screenflows-ch)

## Repositories
* Android App: [swisscovid-app-android](https://github.com/SwissCovid/swisscovid-app-android)
* iOS App: [swisscovid-app-ios](https://github.com/SwissCovid/swisscovid-app-ios)
* CovidCode Web-App: [CovidCode-UI](https://github.com/admin-ch/CovidCode-UI)
* CovidCode Backend: [CovidCode-Service](https://github.com/admin-ch/CovidCode-service)
* Config Backend: [swisscovid-config-backend](https://github.com/SwissCovid/swisscovid-config-backend)
* Additional Info Backend: [swisscovid-additionalinfo-backend](https://github.com/SwissCovid/swisscovid-additionalinfo-backend)
* QR Code Landingpage: [swisscovid-qr-landingpage](https://github.com/SwissCovid/swisscovid-qr-landingpage)
* DP3T Android SDK & Calibration app: [dp3t-sdk-android](https://github.com/DP-3T/dp3t-sdk-android)
* DP3T iOS SDK & Calibration app: [dp3t-sdk-ios](https://github.com/DP-3T/dp3t-sdk-ios)
* DP3T Backend SDK: [dp3t-sdk-backend](https://github.com/DP-3T/dp3t-sdk-backend)
* CrowdNotifier Android SDK: [crowdnotifier-sdk-android](https://github.com/CrowdNotifier/crowdnotifier-sdk-android)
* CrowdNotifier iOS SDK: [crowdnotifier-sdk-ios](https://github.com/CrowdNotifier/crowdnotifier-sdk-ios)
* CrowdNotifier Backend: [swisscovid-cn-backend](https://github.com/SwissCovid/swisscovid-cn-backend)

## Installation and Building

The project can be opened with Android Studio 3.6.1 or later or you can build the project with Gradle using
```sh
$ ./gradlew assembleProdRelease
```
The APK is generated under app/build/outputs/apk/prod/release/package-prod-release.apk

The app will not be functional unless your Google account is whitelisted for ExposureNotification testing. Please install the app from the [Google PlayStore](https://play.google.com/store/apps/details?id=ch.admin.bag.dp3t).

## Reproducible builds

To verify that the app distributed on the PlayStore was built by the source code published here, please see the instructions in [REPRODUCIBLE_BUILDS.md](REPRODUCIBLE_BUILDS.md).

## License
This project is licensed under the terms of the MPL 2 license. See the [LICENSE](LICENSE) file.
