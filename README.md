# Chest Tracker

![Modrinth Downloads](https://img.shields.io/modrinth/dt/ni4SrKmq?style=flat-square&label=Modrinth&color=%2316AF54)
![CurseForge Downloads](https://img.shields.io/curseforge/dt/397217?style=flat-square&label=CurseForge&color=%23E04E14)

An client-sided mod to remember where you've put items. Press **Y** to search for items, and the
**GRAVE** key ``` ` ``` to open the GUI.

![An example image of Chest Tracker highlighting results, and showing names above chests](https://i.imgur.com/jfAfFDh.png)

![The main Chest Tracker GUI](https://i.imgur.com/45pBNFJ.png)

[Other Images](https://imgur.com/a/mDTAACo)

## Requirements (2.x) (≥ 1.20.2)

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [YACL](https://modrinth.com/mod/yacl)

Chest Tracker embeds [Where Is It](https://modrinth.com/mod/where-is-it), [JackFredLib](https://github.com/JackFred2/JackFredLib) and [MixinExtras](https://github.com/LlamaLad7/MixinExtras).

## Requirements (1.x) (< 1.20.2)

Note: 1.x won't be recieving any new updates.

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config)

## Features

- Saving of items on the client, allowing it to work on Realms and multiplayer servers.
- Integration with:
  - [REI](https://modrinth.com/mod/rei), [JEI](https://modrinth.com/mod/jei), and [EMI](https://modrinth.com/mod/emi) via Where Is It; see [it's usage section](https://github.com/JackFred2/WhereIsIt#usage) 
  - [Shulker Box Tooltip](https://modrinth.com/mod/shulkerboxtooltip) - Show Ender Chest contents on the client
  - [WTHIT](https://modrinth.com/mod/wthit) & [Jade](https://modrinth.com/mod/jade) - Show contents of container you're looking at. Contains it's own plugin settings.
- Custom handling for:
  - Hypixel Skyblock (private island + ender chest).

## Usage

Press Y to search by an Item Stack; this uses Where Is It's keybind.

Press GRAVE ``` ` ``` to open the main GUI. It's the button above tab, to the left of the number row, also known as the
Source Engine console key.

In the GUI, click an item to search for it in your current dimension. Use the search bar and it's various filters
to narrow down your search.

### Memory Bank Settings

Press the "Change Memory Bank Settings" to change how your memory bank functions. This includes integrity checks, which
try to keep your data valid.

## License

Chest Tracker 2.x a ground-up rewrite and is licensed under LGPL-3.0-only, You have permission to use the textures,
modified or not, in your resource pack.

Legacy Chest Tracker 1.x is licensed under MIT (with some EPL rendering code), and is available on [the legacy branch](https://github.com/JackFred2/ChestTracker/tree/legacy-1.x).

# TODO

## Core Mod

Possibly move IO to a separate thread? not a problem right now

Bring inventory icon back? maybe stretch goal, want to make a JFLib module for nice
snapping around inventory menus

## Compat

[Inventory Tabs](https://modrinth.com/mod/inventory-tabs-updated) - When on 1.20.2
