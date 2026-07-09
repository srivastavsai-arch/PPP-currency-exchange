# Keep Retrofit interfaces
-keep,allowobfuscation interface com.ppp.currencyexchange.data.remote.** { *; }
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
