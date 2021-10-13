# NightWear

A minimal standalone [Wear OS 2.0](https://wearos.google.com) watch face and complication to follow blood glucose data from [NightScout](http://www.nightscout.info/).

<img src="/assets/photo/7II02989_1024x500.jpg">

<a href='https://play.google.com/store/apps/details?id=im.rah.nightwear&utm_source=github&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_gb/badges/images/generic/en_badge_web_generic.png' width="200"/></a>

<img src="/app/src/main/res/drawable-nodpi/preview_circle.png" width="310" height="310"> <img src="/app/src/main/res/drawable-nodpi/preview_square.png" width="310" height="310">

We already have numerous [CGM watch face options](http://www.nightscout.info/wiki/cgm-watchfaces) across the various smartwatch platforms, including [NightWatch](https://github.com/StephenBlackWasAlreadyTaken/NightWatch) and [xDrip+](https://github.com/NightscoutFoundation/xDrip) on Android that provide featureful Wear OS watch faces when installed alongside their companion Android phone applications.

The motivation for this watch face is to utilise Wear OS 2.0's [standalone app](https://developer.android.com/training/wearables/apps/standalone-apps) support, this is useful because it means:

- the watch can be paired with either an Android phone or iPhone and still be usable as a NightScout display, utilising the phone's data connection when necessary

- the watch can be used as a NightScout display away from its paired phone wherever it has a working internet connection (wifi or integrated cellular)

- the watch face can be installed and configured entirely on the watch itself
