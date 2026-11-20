# Keep Room entities & DAOs
-keep class com.example.data.** { *; }

# Keep Firebase AI Logic & App Check classes
-keep class com.example.ai.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*

# Keep ML Kit Text Recognition
-keep class com.google.mlkit.vision.text.** { *; }
