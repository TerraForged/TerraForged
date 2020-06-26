# TerraForged

TerraForged is a small collection of projects centered around procedurally generated worlds.
The primary outlet of which is a Forge mod for the popular game Minecraft (Java Edition).

### Building From Source

The project relies on Gradle for build and dependency management. In order to compile you need
to use the maven-publish task, with the mod binary being located under the 'TerraForgedMod'
sub-project.

Command:
```shell script
./gradlew publish
```

// TODO - more

### Developing With TerraForged's API

##### Dependency
```groovy
repositories {
    maven { 
        url "https://io.terraforged.com/repository/maven/" 
    }
}

dependencies {
    implementation "com.terraforged:TerraForged:1.15.2-0.1.0:api"
}
```

##### Usage

TerraForged fires a number of setup events each time its chunk generator is created. These events expose certain
components of the generator allowing for world-gen content to be configured, modified, or added to dynamically.

See the `com.terraforged.api.TerraEvent` class for the available events.

All events are fired on the `FORGE` event bus.
