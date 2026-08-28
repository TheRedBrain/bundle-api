# Bundle API

An API for mods that allows easy addition of bundles with custom sizes and filtered content.

This repository is the **RPG Series fork** of [TheRedBrain's Bundle API](https://github.com/TheRedBrain/bundle-api),
maintained because upstream stopped at Minecraft 1.21.1. The mod id (`bundleapi`), maven group
(`com.github.TheRedBrain`) and artifact name (`bundle-api`) are kept unchanged so it stays a drop-in replacement.

Current line: **Minecraft 26.1.x, Java 25**, Fabric + NeoForge (Architectury).

## Installation

The fork is built and consumed locally — build it with `./gradlew build publishToMavenLocal`, then depend on it
from `mavenLocal()`:

build.gradle

```groovy
repositories {
    mavenLocal()
}

dependencies {
    // pick the artifact matching your platform module
    modImplementation "com.github.TheRedBrain:bundle-api-fabric:${project.bundle_api_version}"
    // or
    modImplementation "com.github.TheRedBrain:bundle-api-neoforge:${project.bundle_api_version}"
}
```

gradle.properties

```
# replace with latest version
bundle_api_version=1.1.1+26.1.2
```

## Usage

- Create your item instance by calling the constructor for the CustomBundleItem.
- Register your item instance.
- Add model and texture files (taking inspiration from the vanilla bundle is recommended)

A simple example can be found in this repository at
`fabric/src/testmod/java/com/github/theredbrain/bundleapi_test/BundleAPITest.java`.
