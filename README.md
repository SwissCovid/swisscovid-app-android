# SwissCovid: DP3T Android App for Switzerland

[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://github.com/DP-3T/dp3t-app-android-ch/blob/master/LICENSE)
![Android Build](https://github.com/DP-3T/dp3t-app-android-ch/workflows/Android%20Build/badge.svg)


## DP3T
The Decentralised Privacy-Preserving Proximity Tracing (DP-3T) project is an open protocol for COVID-19 proximity tracing using Bluetooth Low Energy functionality on mobile devices that ensures personal data and computation stays entirely on an individual's phone. It was produced by a core team of over 25 scientists and academic researchers from across Europe. It has also been scrutinized and improved by the wider community.

DP-3T is a free-standing effort started at EPFL and ETHZ that produced this protocol and that is implementing it in an open-sourced app and server.


## Introduction
This is a COVID-19 tracing client using the [DP3T Android SDK](https://github.com/DP-3T/dp3t-sdk-android). It is based on the previously released demo app, but uses the newest version of the SDK. This project will be released as the official COVID-19 tracing solution for Switzerland, therefore UX, messages and flows are optimized for this specific case. Nevertheless, the source code should be a solid foundation to build a similar app for other countries and demostrate how the SDK can be used in a real app.
The app design, UX and implementation was done by [Ubique](https://www.ubique.ch?app=github).
<p align="center">
<img src="documentation/screenshots/en/screenshot1.png" width="20%">
<img src="documentation/screenshots/en/screenshot2.png" width="20%">
<img src="documentation/screenshots/en/screenshot3.png" width="20%">
<img src="documentation/screenshots/en/screenshot4.png" width="20%">
</p>

## Contribution Guide

The DP3T App is not yet complete. It has not yet been reviewed or audited for security and compatibility. We are both continuing the development and have started a security review. This project is truly open-source and we welcome any feedback on the code regarding both the implementation and security aspects.

Bugs or potential problems should be reported using Github issues. We welcome all pull requests that improve the quality the source code. Please note that the app will be available with approved translations in English, German, French and Italian. Pull requests for additional translations currently won't be merged.

## Repositories
* Android SDK & Calibration app: [dp3t-sdk-android](https://github.com/DP-3T/dp3t-sdk-android)
* iOS SDK & Calibration app: [dp3t-sdk-ios](https://github.com/DP-3T/dp3t-sdk-ios)
* Android App: [dp3t-app-android](https://github.com/DP-3T/dp3t-app-android-ch)
* iOS App: [dp3t-app-ios](https://github.com/DP-3T/dp3t-app-ios-ch)
* Backend SDK: [dp3t-sdk-backend](https://github.com/DP-3T/dp3t-sdk-backend)


## Further Documentation
The full set of documents for DP3T is at https://github.com/DP-3T/documents. Please refer to the technical documents and whitepapers for a description of the implementation.


## Installation and Building

The project can be opened with Android Studio 3.6.1 or later or you can build the project with Gradle using
```sh
$ ./gradlew assembleProdRelease
```
The APK is generated under app/build/outputs/apk/prod/release/package-prod-release.apk

## License
This project is licensed under the terms of the MPL 2 license. See the [LICENSE](LICENSE) file.