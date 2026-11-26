# د افغان غږ Android APK پروژه

دا د افغان غږ د WordPress مدیریتي اپ لپاره Android WebView shell دی. اپ د WordPress د موجود «Afghan Voice Admin App» پلګین مدیریتي پاڼه په خوندي WebView کې پرانیزي؛ خبرونه، کټګورۍ، مینو، کارنان او نور عملیات د WordPress د هماغه حساب او سروري صلاحیتونو له مخې اجرا کېږي.

## د اپ آیکن

د Android د اپ اصلي نښه د کارن له خوا رالېږل شوي افغان غږ رسمي لوګو ده. همدا نښه د Android launcher او د WordPress PWA (Android/iPhone/کمپیوټر) په icon فایلونو کې کارول شوې ده.

## د Android APK جوړول

### اسانه لاره: GitHub Actions

1. په GitHub کې نوی repository جوړ کړئ (Private هم کېدای شي).
2. د دې ZIP ټول فایلونه او فولډرونه راوباسئ او د پروژې ټول محتویات د repository په root کې پورته کړئ. د `.github/workflows/build-apk.yml` فایل باید هم په همدې مسیر کې وي.
3. د repository د **Actions** برخې ته لاړ شئ.
4. **Build Afghan Voice Admin APK** workflow وټاکئ او **Run workflow** کلیک کړئ. د `main` یا `master` څانګې push هم build پیلوي.
5. د workflow په بریالي اجرا کې د **Artifacts** برخه کې `AfghanVoice-Admin-APK` ښکته کړئ.
6. ZIP خلاص کړئ؛ دننه `app-debug.apk` د Android لپاره نصبېدونکی APK دی.

GitHub Actions د Java 17، Android SDK 36، Build Tools 36.0.0 او Gradle 8.14.8 له لارې APK جوړوي.

### په خپل کمپیوټر کې

- Android Studio نصب کړئ.
- د پروژې فولډر `afghanvoice-android-apk` د Android Studio له لارې خلاص کړئ.
- د Android SDK Platform 36 او Build Tools 36.0.0 نصب کړئ.
- JDK 17 وټاکئ او Gradle 8.14.8 نصب/وکاروئ.
- د پروژې په root کې `gradle assembleDebug` وچلوئ.
- APK دلته پیدا کېږي: `app/build/outputs/apk/debug/app-debug.apk`

## د WordPress اړین نصب

1. په افغان غږ WordPress کې د `wordpress-plugin/afghanvoice-admin-app.zip` پلګین نصب او فعال کړئ.
2. د هغه WordPress کارن په وسیله ننوتل وکړئ چې د خبرونو د سمون اجازه (`edit_posts`) ولري. د ټولو مدیریتي برخو لپاره اړوند WordPress صلاحیتونه هم پکار دي.
3. اپ دا URL پرانیزي:
   `https://afghanvoice.gt.tc/wp-admin/admin.php?page=afghanvoice-admin-app`

## مهم امنیتي معلومات

- دا APK یوازې د افغان غږ د ټاکلي WordPress ډومېن لپاره جوړ شوی دی.
- اپ پاسورډونه په خپله نه ذخیره کوي؛ WordPress د ننوتلو او کوکي مدیریت کوي.
- اپ یوازې HTTPS کاروي او ناامنه HTTP ترافیک بند دی.
- کارن یوازې هغه مدیریتي عملیات ترسره کولای شي چې د WordPress د رول/صلاحیت له مخې ورته اجازه وي.
- APK د Android لپاره دی. د iPhone او کمپیوټر لپاره د WordPress PWA نسخه وکاروئ.

## د APK لاسلیک

GitHub Actions د `assembleDebug` له لارې debug-signed APK جوړوي چې په Android وسیلو کې د sideload له لارې نصبېږي. د Google Play یا د اوږدمهاله عامه خپرونې لپاره باید د مالک په خپل خوندي keystore سره `release` APK لاسلیک شي. Keystore او passwords هېڅکله په عام repository کې مه پورته کوئ.

## د build محدودیت

په هغه چاپېریال کې چې دا پروژه جوړه شوې، Android SDK او Gradle نه وو نصب او بهرنی ډاونلوډ هم بند و؛ له همدې امله دلته APK باینري نه ده Compile شوې. د پورته GitHub workflow فایل د APK د جوړولو لپاره ورسره شامل دی.


## د پیل پرده او GitHub Actions

اپ اوس د افغان غږ رسمي آیکن، «افغان غږ نشراتي اداره» سرلیک او متحرک باروونکي نښه لرونکې پیل پرده لري. د GitHub Actions جوړولو فایل د `ubuntu-latest` له مخکې نصب شوي Android SDK او `sdkmanager` څخه کار اخلي؛ `setup-android@v3` نور نه کارول کېږي.
