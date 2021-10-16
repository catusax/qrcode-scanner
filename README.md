# qrcode-scanner

## usage

### get the library

refer to:<https://www.jitpack.io/#coolrc136/qrcode-scanner>

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

``` kotlin
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}
```

Step 2. Add the dependency

```	kotlin
dependencies {
    implementation 'com.github.coolrc136:qrcode-scanner:0.5.0'
}
```

### request camera permission

add to your `AndroidManifest.xml`

```xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    ...
    <uses-permission android:name="android.permission.CAMERA" />
    ...
</manifest>
```

### use the fragment

extend `com.github.coolrc136.ScanFragment`

### custom layout

create a custom layout, your custom layout must have `PreviewView` and `ScanOverlay` with
id `previewView` and `overlay`:

```kotlin
// camera-view dependency
implementation "androidx.camera:camera-view:1.0.0-alpha30"
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.camera.view.PreviewView
        android:id="@+id/previewView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
    <!-- display scanning anim -->
    <com.github.coolrc136.overlay.ScanOverlay
        android:id="@+id/overlay"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="20dp"
        android:layout_marginBottom="10dp" />

    <!-- (optional) button to turn on/off torch -->
    <com.github.coolrc136.view.FlashBtn
        android:id="@+id/flash_btn"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:layout_marginBottom="40dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        android:contentDescription="@string/flash_light" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

then load your custom layout in fragment：

```kotlin
override fun getLayoutId(): Int {
    return R.layout.your_custom_layout
}
```