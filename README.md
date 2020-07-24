# TerraForged

TerraForged is a small collection of projects centered around procedurally generated worlds.
The primary outlet of which is a Forge mod for the popular game Minecraft (Java Edition).

### Building From Source

Firstly you must recursively git-clone this repository and its submodules (Engine & FeatureManager). To build the
mod jar just use the gradle build task. The jar will output to the `build/libs` directory.

#### Commands
```shell script
git clone --recursive https://github.com/TerraForged/TerraForged.git

./gradlew build
```

### Developing With TerraForged's API

#### Dependency
```groovy
repositories {
    maven { 
        url "https://io.terraforged.com/repository/maven/" 
    }
}

dependencies {
    implementation fg.deobf("com.terraforged:TerraForged:1.15.2-0.1.0")
}
```

#### Usage

TerraForged fires a number of setup events each time its chunk generator is created. These events expose certain
components of the generator allowing for world-gen content to be configured, modified, or added to dynamically.

See the `com.terraforged.api.TerraEvent` class for the available events.

All events are fired on the `FORGE` event bus.
