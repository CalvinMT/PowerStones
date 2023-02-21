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

## Implementation process and limitations

The initial idea was to be able to place all four powerstone wires on a single block. This meant that the redstone wire block would have had more than 5 308 416 states (3x3x3x3x16x16x16x16) compared to its unmoded version of 1 296 states (3x3x3x3x16). As all states of all the game's blocks are computed on startup, this would have taken more computation time and memory than a computer can handle.

In order to keep the initial idea, an effort was made to limit the powerstone's power to two states (0-1), instead of sixteen (0-15). This lowered the number of states of the redstone wire block down to its unmoded version of 1 296 states (3x3x3x3x2x2x2x2). However, this number of states would not have been final as more would have been added to display all possible connections. Eventually, the implementation proved itself too complicated and unreasonable to continue, as it required an overhaul of the redstone wire block's power updates and removed all vanilla redstone functionalities related to redstone power levels.

The idea of having overlappable powerstones still viewed as a goal, an attempt to first add bluestone ont top of redstone was made with success through the addition of another power property and the incrementation of both power properties by one. Thus, the number of states of the redstone wire block was of 23 409 (3x3x3x3x17x17), which only added a couple of seconds more to the loading time.

The inclusion of greenstone and yellowstone wires could not have been achieved through the addition of two more power properties for the same reason as stated in the first paragraph (too many states). The choice had then been made to make wires overlappable by pairs. Redstone wires would be overlappable with bluestone wires, while greenstone wires would be overlappable with yellowstone wires. This was easily achievable through the addition of a single property with two states (one state per pair of powerstones), ending with a number of states for the redstone wire block of 46 818 (3x3x3x3x17x17x2). However, adding this basic property rendered the game's startup slower (~1mn30s). But, with higher concern, the number of memory (RAM) needed in order for the game to load every state approximated 12GB. Even if asking users to add a simple Java argument in the Minecraft's launcher to extend the maximum amount of allocated memory to the game is done for other mods, 12GB of memory is huge in comparison to the small addition this mod would bring to the game.

As the powerstone pair property seemed like a good arrangement to bring the project as close to the initial goal as it could in under six weeks of time, the decision was taken to find a mod which could considerably reduce the amount of memory allocated to run the game with the PowerStones mod. After a quick search, the FerriteCore mod seemed adequate and, against all expectations, made it possible for the game to run with the PowerStones mod wihtout changing the maximum amount of allocated memory (<4GB). Although PowerStones has become dependent of a performance mod, users can benefit from it without worrying about memory consumption.

## Colour choices

<TODO - blend - colour dodge>
