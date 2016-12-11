<img src="assets/logo.png?raw=true" align="left" width="100px" height="100px"/>
<img align="left" width="0" height="100px" hspace="5"/>

> [MyGrades](https://mygrades.de/) Android App

[![Apache2 License](https://img.shields.io/badge/license-APACHE2-blue.svg?style=flat-square)](/LICENSE)
[![MyGrades Version](https://img.shields.io/badge/mygrades-1.2.0-8BC34A.svg?style=flat-square)](https://mygrades.de)
<br><br><br>
MyGrades is an Android App developed by [Tilman Ginzel](https://github.com/tilmanginzel) and [Jonas Theis](https://github.com/jonastheis). Its main purpose is to scrape a students grades from her/his respective university's website. To do so, it simulates a browser which follows a sequence of URLs and extracts the desired information with XPATH (web scraping). The core functionality is based on our Android App [Scrapp](https://github.com/tilmanginzel/scrapp), which we developed during our studies at the RheinMain University of Applied Sciences. Special thanks to [Rahel Habacker](https://github.com/RedHilarious)!

Get it on [Google Play.](https://play.google.com/store/apps/details?id=de.mygrades)

## Screenshots
<img src="assets/screen-overview.jpg?raw=true" width="215px" />
<img src="assets/screen-statistics.jpg?raw=true" width="215px" />
<img src="assets/screen-detail1.jpg?raw=true" width="215px" />
<img src="assets/screen-detail2.jpg?raw=true" width="215px" />
<img src="assets/screen-settings.jpg?raw=true" width="215px" />
<img src="assets/screen-login-filled.jpg?raw=true" width="215px" />
<img src="assets/screen-edit-overview.jpg?raw=true" width="215px" />
<img src="assets/screen-edit-detailed.jpg?raw=true" width="215px" />

## Installation

Clone the project and create a `Config.java` file.

```bash
git clone https://github.com/MyGrades/mygrades-app.git
cd mygrades-app/app/src/main/java/de/mygrades/util
cp ConfigDefault.java Config.java
```

The config must at least implement the method `getServerUrl()`.
Point this url to your running [server](https://github.com/MyGrades/mygrades-server).

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
* [NetCipher: Secured Networking for Android](https://github.com/guardianproject/NetCipher)
* [JTidy](http://jtidy.sourceforge.net/)
* [Groovy](http://www.groovy-lang.org/)
* [Spock Framework](https://github.com/spockframework/spock)
* [Hamcrest](http://hamcrest.org/)
* [FloatingActionButton](https://github.com/makovkastar/FloatingActionButton)

## License

This project is licensed under the [Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

See [`LICENSE`](LICENSE) for more information.

    Copyright 2015-2016 Jonas Theis, Tilman Ginzel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
