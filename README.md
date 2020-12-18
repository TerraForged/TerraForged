# TerraForged

TerraForged is a small collection of projects centered around procedurally generated worlds.
The primary outlet of which is a Forge mod for the popular game Minecraft (Java Edition).

### Building From Source

#### Commands
```shell script
git clone https://github.com/TerraForged/TerraForged.git
cd TerraForged
git clone https://github.com/TerraForged/FeatureManager.git
./gradlew runData
./gradlew build
```
Notes:
- The runData task will say it failed despite it having completed successfully (bug in forge gradle)
- You may need to set the '[org.gradle.java.home](https://docs.gradle.org/current/userguide/build_environment.html)' variable in gradle.properties to point to your jdk8 installation if you have more modern java versions installed

### Developing With TerraForged's API

#### Dependency
```groovy
repositories {
    maven { 
        url "https://io.terraforged.com/repository/maven/" 
    }
}

dependencies {
    implementation fg.deobf("com.terraforged:TerraForged:1.15.2-${version}")
}
```

Obtain a `${version}` from the [repository tags](https://github.com/TerraForged/TerraForged/releases) 
(versions 0.0.1 to 0.1.2 inclusive are currently unavailable).

#### Usage

TerraForged fires a number of setup events each time its chunk generator is created. These events expose certain
components of the generator allowing for world-gen content to be configured, modified, or added to dynamically.

See the `com.terraforged.api.TerraEvent` class for the available events.

All events are fired on the `FORGE` event bus.
