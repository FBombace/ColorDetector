> Written with [StackEdit](https://stackedit.io/).

ColorDetector
===================
Color detector is an open source software for the automatic recognition of colors in real time through the main camera of android smartphones/tablets, with manual or automatic picture taking.
The software was developed using Android studio and OpenCV libraries for Android , v2.4.11

User guide
-------------
Simply open the app :D the camera will start to show on the display only the searched color (if set to manual picture taking, that is default setting) or will start to take picture with/without osd (On-Screen Display) countdown if the searched color is found on the actual scene taken from the camera (if set to automatic picture taking).
N.B.: the below pictures don't show the GUI of the software, but only taked images. 

![enter image description here](https://lh3.googleusercontent.com/-pEPHh-bYxBg/Vm1pxKkXZFI/AAAAAAAAJeE/o3tqNRgss98/s240/01_mask_40.jpg "01_mask_40.jpg") ![enter image description here](https://lh3.googleusercontent.com/-Ua8SIHfGR08/Vm1p5gLyMCI/AAAAAAAAJeQ/2PBmsQUIhnc/s240/01_img_40.jpg "01_img_40.jpg")

In this example the searched color was red with manual shoot.

![enter image description here](https://lh3.googleusercontent.com/-YKYNYzPjPrg/Vm1rJgZqiLI/AAAAAAAAJe8/r-oRgB7iGDg/s240/02_mask.jpg "02_mask.jpg") ![enter image description here](https://lh3.googleusercontent.com/UKlizi06u7uJ_jiAr_GwmOBpABfOlzN1qkVQPRo8j2NmWCaBuPihIbrtfiJSAJuiBur4=s240 "02_img.jpg")

In this other example the searched color was blue with manual shoot.

Other colors and parameter are selectable in the option window, that can be opened by clicking on the option button

![enter image description here](https://lh3.googleusercontent.com/-2m7UBkjzJmY/Vm1qVYJyUNI/AAAAAAAAJec/VJnGjQsnnnY/s350/Screenshot_option.png "Screenshot_option.png")

If set to automatic picture taking, the software will shoot automatic picture when recognize the searched color, with an optional osd three second delay, or if set to manual picture taking, the software will let you take picture by pressing camera button.

![enter image description here](https://lh3.googleusercontent.com/-akk8a8mXkUc/Vm1rlVcCe7I/AAAAAAAAJfM/9mYnTbEwqWU/s300/Screenshot_osd.png "Screenshot_osd.png")

The software always ask the user if save or discard the took picture.

![enter image description here](https://lh3.googleusercontent.com/-3ouV2IaQ7Nc/Vm1s39AcTHI/AAAAAAAAJfw/xwQpSYi5E3Y/s350/Screenshot_save.png "Screenshot_save.png")

The picture can be viewed in the integrated gallery (accessible clicking the gallery button), and from them is possible to delete pictures from mass memory.

![enter image description here](https://lh3.googleusercontent.com/-8k7sXc5K6Jw/Vm1r2y7B15I/AAAAAAAAJfc/0LUsY_zc8eQ/s350/Screenshot_gallery.png "Screenshot_gallery.png")

Compiling
---------
Simply import the project in Android Studio and you can run/build/test/compile
Application package
---
the application package (apk) can be found in path colorDetector/build/outputs/apk/

Compatibility
-------------
The software is compatible with version of android >= 2.3 (gingerbread).
It has been tested on android versions 2.3 (CyanogenMod 7.2) and 5.1

Known bugs
---------

 - The software actually suffer from artificial yellow lights, leading to false/miss color recognition in some case

License
-------
Apache License v2 https://www.apache.org/licenses/

Other license
---------------

OpenCV for android v2.4.11, 3-clause BSD License. Can be found in path openCVLibrary2411/LICENSE or http://opencv.org/license.html


