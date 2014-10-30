open-airplay
============

A collection of libraries for Apple's AirPlay protocol. The Java library also requires [http://jmdns.sourceforge.net/ JMDNS] if you want to support searching/bonjour auto discovery.

Example
=======
The library can be used by another application, but it can also be used for some basic tasks from the command line:
Send a photo:
```
php airplay.php -h hostname[:port] -p file
java -jar airplay.jar -h hostname[:port] [-a password] -p file
```
Steam desktop:
```
php airplay.php -h hostname[:port] -d (mac only)
java -jar airplay.jar -h hostname[:port] [-a password] -d
```



Links
=====
 * [Stream DVDs](http://www.tuaw.com/2010/12/21/dvds-are-playing-back-on-my-apple-tv-and-its-not-really-that/) - Stream DVD's from a Mac using Erica's AirFlick and VLC
 * [Stream Desktop](http://hiddencode.me/blog/2011/07/how-to-stream-mac-desktop-to-apple-tv/) - Stream your Mac's desktop using Erica's AirFlick and VLC (Audio not supported due to limitations in VLC)
 * [lifehacker](http://lifehacker.com/5802958/how-to-make-your-entire-home-airplay+compatible) - Make your entire home AirPlay compatible
