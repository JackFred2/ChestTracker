# Chest Tracker 1.x

**Chest Tracker 1.x** is for 1.20.1 and earlier, is licensed under the MIT license, and is now
deprecated in favour of version **2.x**.

## Description

A [Fabric](https://fabricmc.net) mod for remembering items and recalling where on the client.

Original Forge idea by [HenneGamer](https://www.curseforge.com/minecraft/mc-mods/chestcounter).

## Translations

You can contribute at https://poeditor.com/join/project?hash=FpMp8wEAKO. You will be credited under the name in POEditor,
unless you wish to have it under a different name - in this case please leave an issue or contact me.

Current translation targets (as of 2021/08/19):
- Chinese (90%)
- English (100%) ✅
- French (65%)
- German (36%)
- Italian (100%) ✅
- Polish (92%)
- Portuguese (0%)
- Russian (100%) ✅
- Spanish (100%) ✅
- Turkish (26%)
- Welsh (0%)

If your language isn't listed, feel free to make an issue requesting it.

## Button Positions

Button positions can be overriden on a per-screen basis using resource packs (either player made or included with mods).
This can be done by creating a .json file under `<root>/assets/chesttracker/button_positions` named anything, with the following format:

```json5
{
  // A list of GUI class names to override. T
  // These can be found by enabling "Print GUI Class Names" under Misc Options in the config, and opening the desired GUI.
  "classNames": [
    "KitchenCupboardScreen",
    "DrawerScreen",
    "TradingStationScreen"
  ],
  "horizontalAlignment": "RIGHT", // Which side of the GUI to anchor to horizontally - can be LEFT or RIGHT.
  "horizontalOffset": -6, // Pixel horizontal offset - positive is right, negative is left.
  "verticalAlignment": "TOP", // Which side of the GUI to anchor to vertically - can be TOP or BOTTOM.
  "verticalOffset": -3 // Pixel vertical offset - positive is down, negative is up.
}
```

More examples can be found [here](src/main/resources/assets/chesttracker/button_positions); I recommend creating a dummy resource pack while editing these to take advantage of MC's Reload Resource Pack feature (F3-T).

## Credits

See the [fabric.mod.json](src/main/resources/fabric.mod.json)
