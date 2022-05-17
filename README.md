
Port with permission from phantamanta44.

# AE2 Fluid Crafting Rework

[![Downloads](https://cf.way2muchnoise.eu/full_623955_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/ae2-fluid-crafting-rework) ![MCVsrsion](https://cf.way2muchnoise.eu/versions/623955.svg)

Put fluids in the pattern!

AE2 autocrafting is amazing and everyone loves it, but it is always becoming painful when dealing with fluids. You have to put fluids in a container or use a dummy item to write patterns with fluids.

That's because AE2 doesn't support fluid as valid crafting ingredients before 1.16, so it can't handle fluids directly.

However, it is changed now! With **AE2 Fluid Crafting** you can write patterns with fluids freely. Your AE system can output and accept fluids like items without worrying about how to handle these fluid cells.

This is a rework and ported version of [ae2-fluid-crafting](https://github.com/phantamanta44/ae2-fluid-crafting)

## Features

 - You can code fluid patterns on fluid terminal directly.
 - Mult/Div/Add/Sub Button also works on fluid.
 - An extended fluid terminal with 16 inputs and 4 outputs.
 - You can add or decrease fluid's amount by clicking with container.
 - Fluid Pattern can display its contents when put on pattern terminal.
 - [1.12.2] Fix the fluid amount display error when removing fluid.
 - [1.12.2] Upgrade cards can be inserted in Dual Interface.
 - [1.12.2] Fluid pattern is searchable in interface terminal.
 - [1.12.2] Fix crash when using block mode, fluid will also be considered as blockable.

## Installation

### 1.7.10
Any version of AE2(Both Official AE2 and GTNH edition AE2 works).

**Extra Cells isn't needed**

### 1.12.2
Unofficial AE2([PAE2](https://www.curseforge.com/minecraft/mc-mods/ae2-extended-life)).

You can directly upgrade origin AE2FC2 in your old save.

Official AE2 isn't supported, you can use origin [AE2FC](https://github.com/phantamanta44/ae2-fluid-crafting) if you are playing with Official AE2.

## Basic Devices

### Fluid Discretizer
The **Fluid Discretizer** is a device that, when attached to ME network, exposes the contents of its fluid storage grid as items, which take the form of "fluid drops".
It does this by functioning as a sort of storage bus: when fluid drops are removed from its storage via the item grid, it extracts the corresponding fluid from the fluid grid.
Conversely, when fluid drops inserted into its storage via the item grid, it injects the corresponding fluid into the fluid grid.
Each fluid drop is equivalent to one mB of its respective fluid, which means a full stack of them is equivalent to 64 mB.
Fluid drops have an important property: when an ME interface attempts to export fluid drops to a machine, it will attempt to convert them to fluid.
This means an interface exporting drops of gelid cryotheum into a fluid transposer will successfully fill the transposer's internal tank rather than inserting the drops as items.
This is the central mechanic that makes fluid autocrafting possible.
Note that the only way to convert between fluids and fluid drops is a discretizer attached to an ME network.
While you could theoretically use this as a very convoluted method of transporting fluids, it is not recomomended to do so.

### Fluid Pattern Encoder
Most crafting recipes involving fluids require far more than 64 mB of a particular fluid, and so the standard pattern terminal will not do for encoding such recipes into patterns.
This problem is solved by the **Fluid Pattern Encoder**, a utility that functions similarly to a pattern terminal.
When a fluid-handling item (e.g. a bucket or tank) is inserted into the crafting ingredient slots, they will be converted into an equivalent stack of the corresponding fluid drops.
Using this, patterns for recipes that require more than a stack of fluid drops can easily be encoded.
AE2 Fluid Crafting also comes with a handy JEI integration module that allows the fluid pattern encoder to encode any JEI recipe involving fluids.
This is the recommended way to play with the mod, since encoding patterns by hand is a little cumbersome.

### Fluid Pattern Terminal

Encoding recipes in a big and bulky workbench separate from the rest of your AE2 equipment can be a little inconvenient.
Luckily, we have the **Fluid Pattern Terminal**, which combines the functionality of the standard pattern terminal and the fluid pattern encoder.
Now, you can encode your fluid recipes using the same familiar interface you know and love!

### Fluid Pattern Interface

The standard ME interface lets you emit items and fluid packets with AE2FC, but it will only accept items, as in vanilla AE2.
This is a little inconvenient when you want to build compact setups for autocrafting with fluid outputs, where you would need to use both an item interface for inputs, and a separate fluid interface for outputs.
To make things easier, we have the **Fluid Pattern Interface**, which functions as a combination of an item interface and a fluid interface!
Its GUI is the same as normal ME interface, but it can emit fluids directly instead of fluid packets! It also accepts fluid and item inputs.
Automating fluid crafting machines has never been this quick and painless!

### Fluid Packets

When putting fluid pattern in a normal ME interface, it will emit fluid packets as fluid.
So if you prefer to deal with item instead of fluids, you can transport these packets and turn them back to fluid with **ME Fluid Packet Decoder**.
Simply connect the decoder to your ME network and insert the fluid packet; the decoder will, if possible, inject the fluid into your fluid storage grid.

## Credited Works

E. Geng(@phantamanta44) and KilaBash (@Yefancy) - Their amazing origin work in 1.12.
