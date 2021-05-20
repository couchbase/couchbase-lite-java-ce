
# Couchbase Lite Android

# Issues
Please file any issues concerning the Couchbase Lite Android product in the root project, couchbase-lite-android-ce-root,
[here](https://github.com/couchbase/couchbase-lite-android-ce-root)

## Requirements

- Android 5.1.1+ (API 22+)
- Supported architectures: armeabi-v7a, arm64-v8a, x86 and x86_64
- Android Studio 3.6+

## Installation

Download the latest AAR or grab via Maven

### Gradle
Add the following in the dependencies section of the application's build.gradle (the one in the app folder).

```
dependencies {
    implementation 'com.couchbase.lite:couchbase-lite-android:2.8.0'
}
```

### Maven
```
<dependency>
  <groupId>com.couchbase.lite</groupId>
  <artifactId>couchbase-lite-android</artifactId>
  <version>2.8.0</version>
</dependency>
```

### Download
- https://www.couchbase.com/downloads

## Documentation

- [Developer Guide](https://docs.couchbase.com/couchbase-lite/2.8/java-android.html)

## How to build from source

You will first need a local properties file that provides the locations for an Android
SDK, NDK, and CMake.  It will probably look something like this:
```
## This file must *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.
#
# Location of the SDK. This is only used by Gradle.
# For customization when using a Version Control System, please read the
# header note.
#Tue Dec 17 10:21:05 PST 2019
sdk.dir=/Users/ladygaga/Library/Android/sdk
ndk.dir=/Users/ladygaga/Library/Android/sdk/ndk/21.3.6528147
cmake.dir=/Users/ladygaga/Library/Android/sdk/cmake/3.10.2.4988404
```

Once local.properties is properly configured, a normal gradle build should work:

`./gradlew assemble`

Most normal gradle targets should work.  There are a couple of special ones at the bottom of `lib/build.gradle`

## Using the Library

If you minify (Proguard) an application that uses CouchbaseLite you will need to add a few rules to your
Proguard configuration.  The rules are listed [here](https://docs.couchbase.com/couchbase-lite/2.7/java-android.html#ruleset)

## Sample Apps

- [Todo](https://github.com/couchbaselabs/mobile-training-todo/tree/feature/2.0)

## License

Apache 2 [license](https://info.couchbase.com/rs/302-GJY-034/images/2017-10-30_License_Agreement.pdf).

