# Minecraft DragonLib 2
<p align="center">
  <img src="https://i.imgur.com/neOFoxn.png" alt="Logo">
</p>

<p align="center">
  <img src="https://i.imgur.com/YnDoeHs.png" href="https://discord.gg/AeSbNgvc7f" alt="Discord">
  <img src="https://i.imgur.com/uLIB4gb.png" href="https://modrinth.com/mod/dragonlib" alt="CurseForge">
  <img src="https://i.imgur.com/XZYlGVF.png" href="https://www.curseforge.com/minecraft/mc-mods/dragonlib" alt="Modrinth">
</p>

DragonLib is a small and simple Multiloader Minecraft Library Mod which uses architectury and adds useful tools for all my mods. This mod adds nothing special on its own. New functionality may be added at any time when needed by my mods.

If you are developer and find the features useful, you can use it if you want. However, I cannot guarantee that everything will work as expected and will be supported in newer versions! Below there is a small tutorial on how to add this library as a dependency to your Forge Mod.

## Versions
| Minecraft Version | Latest DragonLib Version|
| - | - |
| 1.18.2 | 2.0.0 |

You can find all available major versions of DragonLib on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dragonlib/files) or [Modrinth](https://www.curseforge.com/minecraft/mc-mods/dragonlib).

## Forge project dependency
#### 1. Add the following content to your `build.gradle`:

```groovy
repositories {
    maven {
        name = "DragonLib"
        url = "https://raw.githubusercontent.com/MisterJulsen/modsrepo/main/maven"
    }
}

dependencies {
    implementation("de.mrjulsen.mcdragonlib:dragonlib-forge:<MINECRAFT_VERSION>-<DRAGONLIB_VERSION>")
}
```

#### 2. Add the following lines to your `mods.toml`:
```toml
[[dependencies.<YOUR_MODID>]]
    modId="dragonlib"
    mandatory=true
    versionRange="[<MINECRAFT_VERSION>,<NAXT_MAJOR_MINECRAFT_VERSION>)"
    ordering="NONE"
    side="BOTH"
```

## Mixin Refmap Remapping
If you encounter errors when trying to start Minecraft from your development environment, it is necessary to remap the mixin refmap. Add the following code to each run configuration block.

```groovy
property 'mixin.env.remapRefMap', 'true'
property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
```

For example: Your client run configuration should look something like this:
```groovy
minecraft {
    runs {
        client {
            // ...
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
            // ...
        }
    }
}
```
