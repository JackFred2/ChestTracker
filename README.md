# Chest Tracker

![Modrinth Downloads](https://img.shields.io/modrinth/dt/ni4SrKmq?style=flat-square&label=Modrinth&color=%2316AF54)
![CurseForge Downloads](https://img.shields.io/curseforge/dt/397217?style=flat-square&label=CurseForge&color=%23E04E14)
[![Crowdin](https://badges.crowdin.net/chest-tracker/localized.svg)](https://crowdin.com/project/chest-tracker)

An client-sided mod to remember where you've put items. Press **Y** to search for items, and the
**GRAVE** key ``` ` ``` to open the GUI.

![An example image of Chest Tracker highlighting results, and showing names above chests](https://i.imgur.com/jfAfFDh.png)

![The main Chest Tracker GUI](https://i.imgur.com/45pBNFJ.png)

![A GIF recording on the inventory button to access the Chest Tracker GUI. Can now be moved around by users.](https://i.imgur.com/66sTTRg.gif)

[Other Images](https://modrinth.com/mod/chest-tracker/gallery)

## 📥 Requirements (2.x) (≥ 1.20.2)

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [YACL](https://modrinth.com/mod/yacl)

Chest Tracker embeds [Where Is It](https://modrinth.com/mod/where-is-it), [JackFredLib](https://github.com/JackFred2/JackFredLib) and [Searchables](https://github.com/jaredlll08/searchables).

## 🚧 Requirements (1.x) (< 1.20.2)

Note: 1.x won't be recieving any new updates.

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config)

## ⭐ Features

- Saving of items on the client, allowing it to work on Realms and multiplayer servers.
- Integration with:
  - [REI](https://modrinth.com/mod/rei), [JEI](https://modrinth.com/mod/jei), and [EMI](https://modrinth.com/mod/emi) via Where Is It; see [it's usage section](https://github.com/JackFred2/WhereIsIt#usage) 
  - [Shulker Box Tooltip](https://modrinth.com/mod/shulkerboxtooltip) - Show Ender Chest contents on the client
  - [WTHIT](https://modrinth.com/mod/wthit) & [Jade](https://modrinth.com/mod/jade) - Show contents of container you're looking at. Contains it's own plugin settings.
- Custom handling for:
  - Hypixel Skyblock (private island + ender chest).
  - Hypixel SMP

## 📖 Usage

Press Y to search by an Item Stack; this uses Where Is It's keybind.

Press GRAVE ``` [ ` ] ``` to open the main GUI. In the GUI, click an item to search for it in your current dimension. Use the search bar and it's various filters
to narrow down your search.

For more details on using the mod, see [the wiki](https://github.com/JackFred2/ChestTracker/wiki).

## 🌍 Translations

Chest Tracker 2.0 is available on [Crowdin](https://crowdin.com/project/chest-tracker). All languages are accepted; DM me on Crowdin to add yours if needed.

## ✏️ License

Chest Tracker 2.x a ground-up rewrite and is licensed under LGPL-3.0-only, You have permission to use the textures,
modified or not, in your resource pack.

Legacy Chest Tracker 1.x is licensed under MIT (with some EPL rendering code), and is available on [the legacy branch](https://github.com/JackFred2/ChestTracker/tree/legacy-1.x).

## 📟 Planned Features

To see what's planned with Chest Tracker, see the [Trello Board](https://trello.com/b/cMzr1g9P/chest-tracker). To suggest something,
[open an issue!](https://github.com/JackFred2/ChestTracker/issues)