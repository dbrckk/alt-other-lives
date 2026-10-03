# Preserve runtime-visible type metadata used by Android/Compose and third-party libraries
# while still allowing R8 to shrink and obfuscate application code.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod
