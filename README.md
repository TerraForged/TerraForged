# TerraForged

TerraForged is a small collection of projects centered around procedurally generated worlds.
The primary outlet of which is a Forge mod for the popular game Minecraft (Java Edition).

### Building From Source

Firstly you must recursively git-clone this repository and its submodules (Engine & FeatureManager). To build the
mod jar just use the gradle build task. The jar will output to the `build/libs` directory.

Commands:
```shell script
git clone --recursive https://github.com/TerraForged/TerraForged.git

./gradlew build
```
