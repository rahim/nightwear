# NightWear

A minimal standalone [Wear OS 2.0](https://wearos.google.com) watch face to follow blood glucose data from [NightScout](http://www.nightscout.info/).

![](/app/src/main/res/drawable-nodpi/preview_circle.png)

![](/app/src/main/res/drawable-nodpi/preview_square.png)

We already have numerous [CGM watch face options](http://www.nightscout.info/wiki/cgm-watchfaces) across the various smartwatch platforms, including [NightWatch](https://github.com/StephenBlackWasAlreadyTaken/NightWatch) and [xDrip+](https://github.com/NightscoutFoundation/xDrip) on Android that provide featureful Wear OS watch faces when installed alongside their companion Android phone applications.

The main reason this project exists is to utilise Wear 2.0's [standalone app](https://developer.android.com/training/wearables/apps/standalone-apps) support.

This is useful because it means:
- a Wear 2.0 watch can be paired with an iPhone and still be usable as a NightScout display, utilising the phone's data connection when necessary
- a Wear 2.0 watch can be used as a NightScout display away from its paired phone (Android or iPhone) wherever it has a working internet connection (wifi or integrated cellular)

The watch face can be installed and configured entirely on the watch itself.
