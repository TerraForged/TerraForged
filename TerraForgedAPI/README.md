# TerraForgedAPI

### Dependency
```groovy
repositories {
    maven { 
        url "https://io.terraforged.com/repository/maven/" 
    }
}

dependencies {
    implementation "com.terraforged:TerraForgedAPI:1.15.2-0.0.1"
}
```

### Usage

TerraForged fires a number of setup events each time its chunk generator is created. These events expose certain
components of the generator allowing for world-gen content to be configured, modified, or added to.

See the `com.terraforged.api.TerraEvent` class for the available events.

All events are fired on the `FORGE` event bus.