open-airplay
============

A collection of libraries for Apple's AirPlay protocol. The Java library also requires [JmDNS](https://github.com/jmdns/jmdns) if you want to support searching/bonjour auto discovery.

Examples
========
The library can be used by another application, but it can also be used for some basic tasks from the command line or directly (by double clicking):

Build:
```
cd Java && ant
```
Send a photo:
```
php airplay.php -h hostname[:port] -p file
java -jar airplay.jar -h hostname[:port] [-a password] -p file
```
Stream desktop:
```
php airplay.php -h hostname[:port] -d (mac only)
java -jar airplay.jar -h hostname[:port] [-a password] -d
```

Stream desktop - GUI dialog for selecting available apple tvs (bonjour discovery) and resolution:

Download supplementary library jmdns.jar (stored in folder Java/lib)

Execute from folder with downloaded jars by 
```
java -cp "airplay.jar:jmdns.jar" com.jameslow.AirPlay
```

*Servers (Receivers)*
=====================
These third-party projects are a mix of current public options and historical references. Some are archived or unmaintained.

| Name | Description | Open Source | Mirroring |
| ---- | ----------- | ----------- | --------- |
| [UxPlay](https://github.com/FDH2/UxPlay) | A maintained open source AirPlay receiver for Linux, macOS, Windows, and BSD systems | ✔ | ✔ |
| [Shairport Sync](https://github.com/mikebrady/shairport-sync) | A maintained open source AirPlay audio receiver and multi-room player | ✔ | - |
| [AirServer](https://www.airserver.com/) | The best app for turning your Mac into an AirPlay screen | - | ✔ |
| [Reflector](https://www.airsquirrels.com/reflector) | Turn your Mac or PC into an AirPlay screen | - | ✔ |
| [Banana TV (archived)](https://web.archive.org/web/*/http://bananatv.net/) | Another app to turn your Mac into and AirPlay screen | - | - |
| [Casual Share](https://sourceforge.net/projects/casualshare/) | Mac AirPlay receiver | ✔ | - |
| [AirMac (archived)](https://code.google.com/archive/p/airmac) | Turns your Macintosh into an AirPlay receiver (Objective C) | ✔ | - |
| [Airstream Media Player (archived)](https://code.google.com/archive/p/airstream-media-player) | C# based AirPlay screen for Windows and AirPlay server source code | ✔ | - |
| [Play2Wifi (archived)](https://code.google.com/archive/p/play2wifi) | An AirPlay server written in Python | ✔ | - |
| [Totem Plugin AirPlay (archived)](https://web.archive.org/web/*/https://github.com/dveeden/totem-plugin-airplay) | Plugin enabling AirPlay video playback in the Totem media player (Python) | ✔ | - |
| [Slave in the Magic Mirror](https://github.com/espes/Slave-in-the-Magic-Mirror) | Open source implementation of AirPlay Mirroring. | ✔ | ✔ |

*Clients (Senders)*
===================
| Name | Description | Open Source |
| ---- | ----------- | ----------- |
| [Beamer](https://softorino.com/beamer) | Send any video to an AppleTV | - |
| [AirParrot](https://www.airsquirrels.com/airparrot/) | Send the screen of your Mac to an AppleTV (AirPlay Mirroring) | - |
| [Erica's Air Play Utilities (archived)](https://web.archive.org/web/*/http://ericasadun.com/ftp/AirPlay/) | A collection of Mac utilities for streaming video and photos | - |
| [doubleTwist AirSync](https://play.google.com/store/apps/details?id=com.doubleTwist.androidPlayerProKey) | Stream music/videos from your Android phone over AirPlay | - |
| [AP4J (archived)](https://web.archive.org/web/*/http://www.ioncannon.net/projects/ap4j-player-java-airplay-player/) | Java AirPlay video client | ✔ |
| [PascalWAirplayer](https://github.com/PascalW/Airplayer) | A Python based library | ✔ |
| [elcuervo/airplay](https://github.com/elcuervo/airplay) | AirPlay bindings for Ruby | ✔ |
| [xmms2-plugin-airplay](https://packages.debian.org/search?keywords=airplay) | Debian AirPlay library | ✔ |
| [AirPlay NMW (archived)](https://code.google.com/archive/p/airplay-nmt) | AirPlay plugin for Network Media Tank written in C | ✔ |
| [AirMyPC](https://www.airmypc.com/) | AirMyPC Windows AirPlay mirroring client | - |

Links
=====
 * [Stream DVDs (archived)](https://web.archive.org/web/*/http://www.tuaw.com/2010/12/21/dvds-are-playing-back-on-my-apple-tv-and-its-not-really-that/) - Stream DVD's from a Mac using Erica's AirFlick and VLC
 * [Stream Desktop (archived)](https://web.archive.org/web/*/http://www.hiddencode.me/blog/2011/07/how-to-stream-mac-desktop-to-apple-tv/) - Stream your Mac's desktop using Erica's AirFlick and VLC (Audio not supported due to limitations in VLC)
 * [Lifehacker](https://lifehacker.com/how-to-make-your-entire-home-airplay-compatible-5802958) - Make your entire home AirPlay compatible
