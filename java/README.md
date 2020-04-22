## Overview

CBL-Java supports three platforms including linux, windows, and macos (only 64 bits arch supported). CBL-Java contains code for a native shared library for its JNI that are built separately on each platform.  Once built, the three are merged into a single jar file along with the compiled java classes. The distribution package for the linux platform also includes library dependencies required by CBL-Java's JNI including libc++, libicu, and libz.

The sections below will outline the steps necessary to build CBL-Java on each platform and merge them together. The linux platform will need to be the main one as it also needs to package the additional dependecies.


## How to build couchbase-lite-java-ee

#### 1 Create local.properties required by build.gradle

While this file is necessary, unlike it's Android analog, in need not contain any configuration information

```
$ touch local.properties
```


#### 2 Build couchbase-lite-core

The -d option causes the tool to build a DEBUG version of LiteCOre

**MacOS / Linux**

```
$ ../../common/tools/build_litecore.sh -e CE [-d] 
```

**Windows**

```
$ ..\couchbase-lite-java\scripts\build_litecore.bat 2019 CE [d]
```
** Assuming that the Visual Studio 2019 with C++ development libraries was installed.

### 3. Build couchbase-lite-java

#### 3.1 Build and Test

```
$ ./gradlew build 
```

#### 3.2 Create distribution zip file

```
$ ./gradlew distZip 
```

The generated zip file will located at `build/distribution` directory.
