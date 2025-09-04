# Axiom

A powerful, admin-focused Minecraft plugin for building and modifying structures with precision commands. Axiom is designed for server operators who need reliable and flexible tools for structural work.

(Note: Replace DEIN_USERNAME and DEIN_REPO in the badge links above with your GitHub details.)

## Features

Area Clearing: Instantly clear large rectangular areas for new projects.

Wall Construction: Automatically build hollow, multi-block-thick walls with variable dimensions and materials.

Tunnel Digging: Dig fully-equipped 2x2 tunnels in any direction, complete with rails and lighting.

Player-Centric: All commands operate relative to the player's position and direction for intuitive use.

## Installation
Download the latest .jar file from the Releases Page.

Place the Axiom-x.x.jar file into your server's plugins directory.

Restart your PaperMC server.

## Commands
Here is a list of all available commands in the Axiom plugin.

### /cleararea
Clears all blocks in a rectangular area centered on you, up to a fixed height of 10 blocks.

Usage: /cleararea LENGTH WIDTH

Aliases: ca, clearrect

Arguments:

LENGTH: The size of the area along the X-axis.

WIDTH: The size of the area along the Z-axis.

### /buildwall
Constructs a hollow rectangular wall with a fixed thickness of 2 blocks, centered on your location.

Usage: /buildwall LENGTH WIDTH HEIGHT [MATERIAL]

Aliases: bw, wall

Arguments:

LENGTH: The outer length of the wall (X-axis).

WIDTH: The outer width of the wall (Z-axis).

HEIGHT: The height of the wall from your feet upwards.

[MATERIAL] (Optional): The Minecraft material name to build the wall with (e.g., STONE_BRICKS, GLASS). Defaults to COBBLESTONE.

### /digtunnel
Digs a 2x2 tunnel in the direction you are facing. It automatically lays down rails on the entire length of the floor and places torches in niches on the left wall every 10 blocks.

Usage: /digtunnel LENGTH

Aliases: dt, tunnel

Arguments:

LENGTH: The length of the tunnel in blocks.

## Permissions
To use the commands, you need the appropriate permissions.

axiom.command.cleararea: Allows usage of the /cleararea command.

axiom.command.buildwall: Allows usage of the /buildwall command.

axiom.command.digtunnel: Allows usage of the /digtunnel command.

axiom.command.*: Grants access to all commands of the plugin.

axiom.*: Grants access to all features of the plugin.

## Building from Source
To compile the plugin yourself, you'll need:

Java Development Kit (JDK) 21 or newer

Apache Maven

Clone the repository and run the following command in the project's root directory:

Bash

mvn clean package
The compiled .jar file will be located in the target directory.

## License
This project is licensed under the MIT License. See the LICENSE file for details.