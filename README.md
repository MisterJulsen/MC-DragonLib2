# Minecraft DragonLib 2
<p align="center">
  <img src="https://i.imgur.com/neOFoxn.png" alt="Logo">
</p>

<p align="center">
  <a href="https://discord.gg/AeSbNgvc7f"><img src="https://i.imgur.com/YnDoeHs.png" alt="Discord"></a>
  <a href="https://modrinth.com/mod/dragonlib"><img src="https://i.imgur.com/uLIB4gb.png" alt="CurseForge"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/dragonlib"><img src="https://i.imgur.com/XZYlGVF.png" alt="Modrinth"></a>
</p>

DragonLib is a small and simple Multiloader Minecraft Library Mod which uses architectury and adds useful tools for all my mods. This mod adds nothing special on its own. New functionality may be added at any time when needed by my mods.

If you are developer and find the features useful, you can use it if you want. However, I cannot guarantee that everything will work as expected and will be supported in newer versions! Below there is a small tutorial on how to add this library as a dependency to your Forge Mod.

## Versions
| Minecraft Version | Latest DragonLib Version|
| - | - |
| 1.18.2 | 2.1.8 |

You can find all available major versions of DragonLib on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dragonlib/files) or [Modrinth](https://www.curseforge.com/minecraft/mc-mods/dragonlib).

## ForgeGradle project setup
#### 1. Add the following content to your `build.gradle`:

```groovy
repositories {
    maven { // DragonLib
        name = "MrJulsen's Mod Resources"
        url = "https://raw.githubusercontent.com/MisterJulsen/modsrepo/main/maven"
    }
    maven {
        name = "Architectury API"
        url "https://maven.architectury.dev/"
    }
}

dependencies {
    implementation("de.mrjulsen.mcdragonlib:dragonlib-forge:<MINECRAFT_VERSION>-<DRAGONLIB_VERSION>")
}
```
As the project is based on [Architectury API](https://docs.architectury.dev/), you also need its repository.

#### 2. Add the following lines to your `mods.toml`:
```toml
[[dependencies.<YOUR_MODID>]]
    modId="dragonlib"
    mandatory=true
    versionRange="[<MINECRAFT_VERSION>,<NAXT_MAJOR_MINECRAFT_VERSION>)"
    ordering="NONE"
    side="BOTH"
```

## Architectury Loom Multiloader project setup
#### 1. First download an [Architecury Template](https://github.com/architectury/architectury-templates/releases/) and import it as gradle project.
#### 2. Add the following content to the `build.gradle` of your root project:

```groovy
allprojects {
    repositories {
        maven { // DragonLib
            name = "MrJulsen's Mod Resources"
            url = "https://raw.githubusercontent.com/MisterJulsen/modsrepo/main/maven"
        }
        maven { // Forge Config Api (required for fabric version of DragonLib)
            name = "Fuzs Mod Resources"
            url = "https://raw.githubusercontent.com/Fuzss/modresources/main/maven/"
        }
    }
}
```

#### 3. Add the following line to all `build.gradle` files of all your sub-projects (forge, fabric, common).
Replace `<LOADER>` with the specific loader (e.g. `forge`) and use `fabric` in your common project.
```groovy
dependencies {
    modApi("de.mrjulsen.mcdragonlib:dragonlib-<LOADER>:<MINECRAFT_VERSION>-<DRAGONLIB_VERSION>")
}
```

#### 4. Add the following lines to your `mods.toml`:
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
