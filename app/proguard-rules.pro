# Model
-keep class com.inhealion.generator.networking.api.model.** { *; }

# Strip Timber logging
-assumenosideeffects class timber.log.Timber* {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
