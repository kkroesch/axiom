![Logo](logo.png)


# Axiom

A powerful, admin-focused Minecraft plugin for building and modifying structures with precision commands. Axiom is designed for server operators who need reliable and flexible tools for structural work.


## Features

- **Area Clearing:** Instantly clear large rectangular areas for new projects.
- **Wall Construction:** Automatically build hollow, multi-block-thick walls with variable dimensions and materials.
- **Tunnel Digging:** Dig fully-equipped 2x2 tunnels in any direction, complete with rails and lighting.
- **Player-Centric:** All commands operate relative to the player's position and direction for intuitive use.

## Installation
Download the latest .jar file from the Releases Page.
Place the Axiom-x.x.jar file into your server's plugins directory.
Restart your PaperMC server.

## Commands
Here is a list of all available commands in the Axiom plugin.

### /cleararea
Clears all blocks in a rectangular box. By default the area is centered on you and 10 blocks high, counted from your feet upwards. With `corner` the box starts at your position (or at `X Y Z`) and grows towards east (+X) and south (+Z), matching a blueprint anchored at that corner.

Large areas are cleared in slices over several ticks (top to bottom, without physics updates), so the server keeps running smoothly.

**Usage:** `/cleararea LENGTH WIDTH [HEIGHT] [corner [X Y Z]]`

**Aliases:** `ca`, `clearrect`

**Arguments:**

`LENGTH`: The size of the area along the X-axis (1-512).

`WIDTH`: The size of the area along the Z-axis (1-512).

`[HEIGHT]` (Optional): Height in blocks, counted from the start Y upwards. Defaults to 10 (max 512).

`[corner]` (Optional): Start at the corner instead of centering the area. The keyword `ecke` works as well.

`[X Y Z]` (Optional, only with `corner`): Explicit start position. This also allows running the command from the console or via RCON, using the main world, e.g. `cleararea 51 51 46 corner -804 65 597`.

Examples: `/cleararea 20 20`, `/cleararea 51 51 46 corner`.

### /buildwall
Constructs a hollow rectangular wall with a fixed thickness of 2 blocks, centered on your location.

**Usage**: `/buildwall LENGTH WIDTH HEIGHT [MATERIAL]`

**Aliases**: `bw`, `wall`

**Arguments**:

`LENGTH`: The outer length of the wall (X-axis).

`WIDTH`: The outer width of the wall (Z-axis).

`HEIGHT`: The height of the wall from your feet upwards.

`[MATERIAL]` (Optional): The Minecraft material name to build the wall with (e.g., STONE_BRICKS, GLASS). Defaults to COBBLESTONE.

### /digtunnel
Digs a 2x2 tunnel in the direction you are facing. It automatically lays down rails on the entire length of the floor and places torches in niches on the left wall every 10 blocks.

**Usage**: `/digtunnel LENGTH`

**Aliases**: `dt`, `tunnel`

**Arguments**:

`LENGTH`: The length of the tunnel in blocks.

## Permissions
To use the commands, you need the appropriate permissions.

`axiom.command.cleararea`: Allows usage of the /cleararea command.

`axiom.command.buildwall`: Allows usage of the /buildwall command.

`axiom.command.digtunnel`: Allows usage of the /digtunnel command.

`axiom.command.*`: Grants access to all commands of the plugin.

`axiom.*`: Grants access to all features of the plugin.

## Building from Source
To compile the plugin yourself, you'll need:

Java Development Kit (JDK) 21 or newer

Apache Maven

Clone the repository and run the following command in the project's root directory:

Bash

`mvn clean package`

The compiled .jar file will be located in the target directory.

## License
This project is licensed under the MIT License. See the LICENSE file for details.