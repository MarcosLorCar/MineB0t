# MineB0t

A Discord bot that lets you play a 2D Terraria-like sandbox game directly within Discord messages.

<img width="502" height="522" alt="Screenshot 2026-06-25 230632" src="https://github.com/user-attachments/assets/eb1de7de-3cba-4a3a-b6de-b5639f780178" />

> **Status:** This was a personal project to see how far a Discord-native sandbox game could go. It's functional and fun to poke at, but it's no longer under active development. Feel free to fork it, learn from it, or build on top of it.

## Features

- **Procedural World Generation:** Infinite-feeling worlds with terrain, caves, and ores.
- **Mining & Placing:** Interact with tiles using buttons.
- **Crafting System:** Craft new items and stations (e.g., Furnaces, Crafting Tables).
- **Inventory Management:** Manage your items with a dedicated inventory view and hotbar.
- **Persistence:** Your player data and world state are saved (per guild).
- **Customizable:** Change your head and body emojis, and other preferences.

## Getting Started

### Prerequisites

- JDK 21
- A Discord Bot Token (with `applications.commands` and `guilds` scopes)

### Environment Variables

You can configure the bot by setting the following environment variables (either in your system environment or in a `.env` file at the root of the project):

| Variable | Description | Default |
| --- | --- | --- |
| `DISCORD_BOT_TOKEN` | **Required.** The authentication token for your Discord Bot. | None |
| `DEV_MODE` | If set to `true`, the bot runs in development mode. Players will receive starting kits upon joining/reconnecting. | `false` |
| `PERSISTENCE_ENABLED` | Determines whether player data and world states are saved. | `true` |

### Installation

1. Clone the repository.
2. Create a `.env` file in the root directory and define `DISCORD_BOT_TOKEN` (and optionally `DEV_MODE` or `PERSISTENCE_ENABLED`).
3. Run with `./gradlew run`.

## How to Play

### Commands

- `/play`: Start or resume the game in the current channel.
- `/settings`: Open the preferences menu.
- `/set-head <emoji>`: Change your player's head emoji.
- `/set-body <emoji>`: Change your player's body emoji.

### Gameplay

- **Movement:** Use the arrow buttons to move your character.
- **Actions:** The center grid of buttons allows you to interact with the world. 
    - In **Break Mode** (Red), buttons break the tiles in that direction.
    - In **Place Mode** (Green), buttons place the selected item.
- **Inventory:** Click the backpack icon to open your inventory. Use arrows to navigate and select items.
- **Crafting:** Use a Crafting Table or Furnace by standing near them and clicking the crafting icon.

## Development

MineB0t is built with Kotlin, JDA (Java Discord API), and uses Coroutines for its game loop.

### Project Structure

- `src/main/kotlin/me/orange/bot`: Bot lifecycle and configuration.
- `src/main/kotlin/me/orange/game`: Core game logic (World, Player, Inventory, Crafting).
- `src/main/kotlin/me/orange/bot/events`: Discord interaction handling.

### Building

```bash
./gradlew build
```

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

This project is licensed under the [MIT License](LICENSE).
