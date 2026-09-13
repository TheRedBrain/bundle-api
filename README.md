# Bundle API

An API for mods that allows easy addition of bundles with custom sizes and filtered content.

## Installation

Add this mod as dependency for your project.

build.gradle

```groovy
repositories {
    maven {
        name = 'Modrinth'
        url = 'https://api.modrinth.com/maven'
        content {
            includeGroup 'maven.modrinth'
        }
    }
}

dependencies {
    modImplementation "maven.modrinth:bundle-api:${project.bundle_api_version}"
}
```

gradle.properties

```
# replace with latest version
bundle_api_version=1.0.0
```

## Usage

- Create your item instance by calling the constructor for the CustomBundleItem: `new CustomBundleItem(contentTag /* nullable */, sizeMultiplier, settings)`.
  A bundle with size multiplier `n` holds `n` full stacks of any accepted item; `contentTag` restricts what can be inserted.
- Register your item instance (Forge: from a `DeferredRegister` / `RegisterEvent`).
- Add model and texture files (taking inspiration from the vanilla bundle is recommended). The `filled` model predicate is registered automatically for every `CustomBundleItem` on client setup.

A simple example can be found on [GitHub](https://github.com/TheRedBrain/bundle-api/blob/1.21.1/src/testmod/java/com/github/theredbrain/bundleapi_test/BundleAPITest.java).
