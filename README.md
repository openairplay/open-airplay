open-airplay
============

A collection of libraries for Apple's AirPlay protocol. The Java library also requires [JMDNS](http://jmdns.sourceforge.net/) if you want to support searching/bonjour auto discovery.

Examples
========
The library can be used by another application, but it can also be used for some basic tasks from the command line or directly (by double clicking):
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
```
java -jar airplay.jar
```

*Servers (Receivers)*
=====================
| Name | Description | Open Source | Mirroring |
| ---- | ----------- | ----------- | --------- |
| [AirServer](http://www.airserverapp.com/) | The best app for turning your Mac into an AirPlay screen | - | ✔ |
| [Reflector](http://www.airsquirrels.com/reflector/) | Turn your Mac or PC into an AirPlay screen | - | ✔ |
| [Banana TV](http://bananatv.net/) | Another app to turn your Mac into and AirPlay screen | - | - |
| [Casual Share](http://sourceforge.net/projects/casualshare/) | Mac AirPlay receiever | ✔ | - |
| [AirMac](http://code.google.com/p/airmac/) | Turns you Macintosh into an Airplay receiver (Objective C) | ✔ | - |
| [Airstream Media Player](http://code.google.com/p/airstream-media-player/) | C# based AirPlay screen for windows and AirPlay server source code | ✔ | - |
| [Play2Wifi](http://code.google.com/p/play2wifi/) | An AirPlay server written in Python | ✔ | - |
| [Totem Plugin AirPlay](https://github.com/dveeden/totem-plugin-airplay) | Plugin enabling AirPlay video playback in the Totem media player (Python) | ✔ | - |
| [Slave in the Magic Mirror](https://github.com/espes/Slave-in-the-Magic-Mirror) | Open source implementation of AirPlay Mirroring. | ✔ | ✔ |

*Clients (Senders)*
===================
| Name | Description | Open Source |
| ---- | ----------- | ----------- |
| [Beamer](http://beamer-app.com/?mid=5391876) | Send any video to an AppleTV | - |
| [AirParrot](http://www.airparrot.com/) | Send the screen of your Mac to an AppleTV (AirPlay Mirroring) | - |
| [Erica's Air Play Utilities](http://ericasadun.com/ftp/AirPlay/) | A collection of Mac utilities for streaming video and photos | - |
| [doubleTwist AirSync](https://market.android.com/details?id=com.doubleTwist.androidPlayerProKey) | Stream music/videos from your Android phone over AirPlay | - |
| [AP4J](http://www.ioncannon.net/projects/ap4j-player-java-airplay-player/) | Java AirPlay video client | ✔ |
| [PascalWAirplayer](https://github.com/PascalW/Airplayer) | A Python based library | ✔ |
| [elcuervo/airplay](https://github.com/elcuervo/airplay) | Airplay bindings to Ruby | ✔ |
| [xmms2-plugin-airplay](http://packages.debian.org/search?keywords=airplay) | Debian AirPlay library | ✔ |
| [AirPlay NMW](http://code.google.com/p/airplay-nmt/) | AirPlay plugin for Network Media Tank written in C | ✔ |
| [AirMyPC](http://www.airmypc.com/) | AirMyPC Windows AirPlay mirroring client | - |

Links
=====
 * [Stream DVDs](http://www.tuaw.com/2010/12/21/dvds-are-playing-back-on-my-apple-tv-and-its-not-really-that/) - Stream DVD's from a Mac using Erica's AirFlick and VLC
 * [Stream Desktop](http://hiddencode.me/blog/2011/07/how-to-stream-mac-desktop-to-apple-tv/) - Stream your Mac's desktop using Erica's AirFlick and VLC (Audio not supported due to limitations in VLC)
 * [lifehacker](http://lifehacker.com/5802958/how-to-make-your-entire-home-airplay+compatible) - Make your entire home AirPlay compatible
