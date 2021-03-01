# Chest Tracker
A [Fabric](https://fabricmc.net) mod for remembering items and recalling where on the client.

Original Forge idea by [HenneGamer](https://www.curseforge.com/minecraft/mc-mods/chestcounter).

## Translations

You can now contribute translations at https://poeditor.com/join/project?hash=FpMp8wEAKO

If your language isn't listed, feel free to make an issue and I'll add it.

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
