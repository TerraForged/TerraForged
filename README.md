# TerraForged

TerraForged is a small collection of projects centered around procedurally generated worlds.
The primary outlet of which is a Forge mod for the popular game Minecraft (Java Edition).

### Building From Source

#### Steps
1. Clone the TerraForged repo:
```shell
git clone https://github.com/TerraForged/TerraForged.git
```
2. The buildscript is set up with Engine as a local repo - you can modify this so that it
pulls the Engine dependency from maven instead by commenting/uncommenting the appropriate
lines in the dependencies block
4. Generate the TerraForged resources & then build the mod jar:
```shell script
cd TerraForged
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
        url "https://io.terraforged.com/maven/" 
    }
}

dependencies {
    implementation fg.deobf("com.terraforged:TerraForged:1.16.5-${version}")
}
```

#### Usage

TerraForged fires a number of setup events each time its chunk generator is created. These events expose certain
components of the generator allowing for world-gen content to be configured, modified, or added to dynamically.

See the `com.terraforged.api.TerraEvent` class for the available events.

All events are fired on the `FORGE` event bus.
