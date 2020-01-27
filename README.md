<img src="assets/icon/app_icon_512x512.png?raw=true" align="left" width="100px" height="100px"/>
<img align="left" width="0" height="100px" hspace="5"/>

> [MyGrades](https://mygrades.aquiver.de/) Android App

[![Apache2 License](https://img.shields.io/badge/license-APACHE2-blue.svg?style=flat-square)](/LICENSE)
[![MyGrades Version](https://img.shields.io/badge/mygrades-0.1.0-8BC34A.svg?style=flat-square)](https://mygrades.aquiver.de)
<br><br><br>
MyGrades is an Android App to help you stay up to date with your university grades - it automatically fetches your grades and informs you about new marks. To do so, it simulates a browser which logs into your university's website and extracts the desired information with XPATH (web scraping). It is currenty maintained by [Daniel Habenicht](https://github.com/DanielHabenicht) and was mainly developed by [Tilman Ginzel](https://github.com/tilmanginzel) and [Jonas Theis](https://github.com/jonastheis), with the help of [Rahel Habacker](https://github.com/RedHilarious). 

Get it here: 
[Google Play](https://play.google.com/store/apps/details?id=dh.mygrades) | 
[Direct Download](https://dev.azure.com/MyGradesReloaded/MyGrades-App/_build?definitionId=2) | 
~~[F-Droid Repository](https://apt.izzysoft.de/fdroid/index/apk/dh.mygrades) (maintained by [IzzySoft](https://github.com/IzzySoft))~~

## Screenshots
<span><img src="assets/screenshots/screen-overview.jpg?raw=true" width="215px" /></span>
<span><img src="assets/screenshots/screen-statistics.jpg?raw=true" width="215px" /></span>
<span><img src="assets/screenshots/screen-detail1.jpg?raw=true" width="215px" /></span>
<span><img src="assets/screenshots/screen-detail2.jpg?raw=true" width="215px" /></span>
<span><img src="assets/screenshots/screen-settings.jpg?raw=true" width="215px" /></span>
<span><img src="assets/screenshots/screen-login-filled.jpg?raw=true" width="215px" /></span>
<span><img src="assets/screenshots/screen-edit-overview.jpg?raw=true" width="215px" /></span>
<span><img src="assets/screenshots/screen-edit-detailed.jpg?raw=true" width="215px" /></span>

## Installation

1. Clone the project `git clone https://github.com/MyGradesReloaded/mygrades-app.git`.

2. Edit the config must so the string implemented in `getServerUrl()` point to your running [server](https://github.com/MyGradesReloaded/mygrades-server).

## Used third-party libraries
* [jsoup](http://jsoup.org/)
* [Retrofit](http://square.github.io/retrofit/)
* [greenDAO](https://github.com/greenrobot/greenDAO)
* [EventBus](https://github.com/greenrobot/EventBus)
* [secure-preferences](https://github.com/scottyab/secure-preferences)
* [android-advancedrecyclerview](https://github.com/h6ah4i/android-advancedrecyclerview)
* [vector-compat](https://github.com/wnafee/vector-compat)
* [materialish-progress](https://github.com/pnikosis/materialish-progress)
* [android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh)
* [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
* [android-support-preference](https://github.com/consp1racy/android-support-preference)
* [JTidy](http://jtidy.sourceforge.net/)
* [Groovy](http://www.groovy-lang.org/)
* [Spock Framework](https://github.com/spockframework/spock)
* [Hamcrest](http://hamcrest.org/)
* [FloatingActionButton](https://github.com/makovkastar/FloatingActionButton)

## License

This project is licensed under the [Apache Software License, Version 2.0](LICENSE).