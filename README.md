# TerraForged

[![Build Status](https://ci.dags.me/buildStatus/icon?job=TerraForged)](https://ci.dags.me/job/TerraForged/)

![TerraForged Social](https://terraforged.com/curse/header.jpg)

#### About:
TerraForged is an ambitious new terrain generator mod for Minecraft (Java Edition) attempting to
 create more immersive, inspiring worlds to explore and build in. Featuring an overhaul of the
 vanilla generation system, custom terrain shapes, simulated erosion, better rivers, custom
 decorations, tonnes of configuration options, and more!

#### Website(s):
[https://terraforged.com](https://terraforged.com)  
[https://github.com/TerraForged](https://github.com/TerraForged)

#### Installation:
1. Install forge for the target version of Minecraft (ie 1.15.2)
2. Add the TerraForged mod jar to your profile's mods folder
3. Select the '`TerraForged`' world-type when creating a new world

#### Features:
- Varied and immersive terrain
- Erosion and improved rivers
- Custom features and decoration
- Extensive configuration options & in-game GUI

![TerraForged Gallery](https://terraforged.com/curse/gallery.jpg)

#### FAQ:
1) "Is this compatible with mod xyz?"  
_Probably! (to some degree) - TerraForged is designed to work with many of the same world-gen systems  
that the majority of block & biome providing mods use. Certain biomes' terrain may not always look  
exactly as their author designed but should otherwise be compatible. Feel free to report any other  
compatibility issues on the issue tracker._

2) "How can I use this on my server?"  
_When Forge supports it, you can simply set level-type=terraforged in your server.properties file. In  
the meantime, you will need to create the world in single player and then copy that to your server  
directory. (In both cases, TerraForged must be installed on the client and the server)._

3) "Will I need a super-computer to run this?!"  
_No, not really - while this world generator will be a bit slower than vanilla's (on account of it  
doing more work to make things look nice), it would only be apparent when first generating a chunk  
- they might load in slower when moving to new parts of the world, but game performance should  
otherwise be normal. A 4-core CPU should be able to handle this just fine._  

4) "Can this be ported Fabric/Bukkit/Spigot/Sponge?"  
_If someone would like to take this task on, yes - a large part of the TerraForged codebase is already  
platform independent. There are certain client-side features in the forge-mod that would not translate  
onto server-only APIs, but the core experience could certainly be ported. I don't intend to work on  
this directly but others are very welcome._

5) "Will this be back-ported to older Forge versions?"  
_Not by myself, no - My aim is to keep current with Forge. I'm simply not prolific enough a modder to  
write and maintain for multiple versions (hats off to those who do!). Again though, others are welcome  
to back-port it, if inclined to do so._

[View questions on github](https://github.com/TerraForged/TerraForged/issues?q=label:question)