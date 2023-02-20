# PowerStones
Minecraft mod with overlapping wires of Redstone, Bluestone, Greenstone and Yellowstone

## Dependencies

Mods needed to be installed in order for PowerStones to function correctly:

| Mod           | Version | Reason |
| ------------- | ------- | ------ |
| [FerriteCore] | >=5.1.0 | Enables Minecraft to load with the default 4GB of RAM, instead of 12GB which requires the addition of an argument (-Xmx12G). |

[FerriteCore]: https://modrinth.com/mod/ferrite-core

## Features

### Blocks

| Block             | Crafting recipe |
| ----------------- | --------------- |
| Bluestone         | 
| Greenstone        |
| Yellowstone       |
| Bluestone Block   |
| Greenstone Block  |
| Yellowstone Block |
| Bluestone Torch   |
| Greenstone Torch  |
| Yellowstone Torch |

### Functionalities

 - Powerstone wires are overlappable by pairs. Redstone and Bluestone wires can overlap each other. Similarly, Greenstone and Yellowstone wires can overlap each other as well.
 - Hitting powerstone wires while holding a powerstone wire item will only break equally coloured powerstone wires than the one held in the main hand.
 - Powerstone blocks and powerstone torches only give power to powerstone wires of the same colour.

## Limitations

The initial idea was to be able to place all four powerstone wires on a single block. This meant that the redstone wire block would have had more than 5 308 416 states (3x3x3x3x16x16x16x16) compared to its unmoded version of 1 296 states (3x3x3x3x16). As all states of all the game's blocks are computed on startup, this would have taken more computation time and memory than a computer can handle.

In order to keep the initial idea, an attempt was made to limit the powerstone's power to two states (0-1), instead of sixteen (0-15). This lowered the number of states of the redstone wire block down to its unmoded version of 1 296 states (3x3x3x3x2x2x2x2). However, this number of states would not have been final as more would have been added to display all possible connections. Eventually, the implementation proved itself too complicated and unreasonable to continue, as it required an overhaul of the redstone wire block's power updates and removed all vanilla redstone functionalities related to redstone power levels.

<TODO - redstone & bluestone - 3x3x3x3x17x17>

<TODO - greenstone & yellowstone - 3x3x3x3x17x17x2>

<TODO - FerriteCore>

## Colour choices

<TODO - blend - colour dodge>
