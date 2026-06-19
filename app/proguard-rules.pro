# 1. AndroidX နှင့် Material Component များအတွက် (ပုံမှန်အားဖြင့် မလိုအပ်သော်လည်း ထည့်ထားခြင်းက လုံခြုံသည်)
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }

# 2. Gesture Views (သင်အသုံးပြုထားသည့် Library) အတွက်
-keep class com.alexvasilkov.gestures.** { *; }

# 3. Navigation Tab Strip အတွက်
-keep class com.devlight.navigationtabstrip.** { *; }

# 4. နောက်ဆုံးအနေဖြင့် - တခြား Library များအတွက်
-dontwarn com.alexvasilkov.gestures.**
-dontwarn com.devlight.navigationtabstrip.**
