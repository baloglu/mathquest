# Math Quest

A Paper plugin that teaches math inside Minecraft: short lessons, adaptive quizzes, and in-game rewards.
Built for Bedrock players (via Geyser/Floodgate) with pop-up forms. Java players get clickable chat buttons.

## Topics

| Topic | Levels |
|---|---|
| **Fractions** | naming fractions → equivalent fractions → comparing → adding & subtracting → whole number × fraction |
| **Tenths & Hundredths** | tenths → hundredths (100-square grid) → tenths = hundredths → comparing decimals |
| **Multiplication** | ×1 ×2 ×5 ×10 → ×3 ×4 → ×6–9 → up to 12 × 12 → 2-digit × 1-digit & missing factors |

- 5 right in a row → level up; 3 wrong in a row → level down.
- About 30% of questions review earlier levels.
- Wrong answers show the correct one with a step-by-step explanation or trick.
- Rewards: an item and XP per correct answer, a bonus every 5-answer streak, and a daily cap.

## Playing

- Hold the **Math Quest book** (given on join) and tap the ground or a block, or
- type `/math` (aliases `/fractions`, `/mathquest`).

## Commands

| Command | Who | What |
|---|---|---|
| `/math` | everyone | open the menu |
| `/math learn <fractions\|decimals\|multiplication>` | everyone | open a lesson |
| `/math practice <topic\|mixed>` | everyone | start practicing |
| `/math progress` | everyone | your levels and accuracy |
| `/math book` | everyone | get a new Math Quest book |
| `/math report [player]` | op / console | progress report |
| `/math open <player>` | op / console | open the menu on a player's screen |
| `/math givebook <player>` | op / console | give a player the book |
| `/math setlevel <player> <topic> <level>` | op / console | set a starting level |
| `/math reload` | op / console | reload config.yml |

## Configuration

`plugins/MathQuest/config.yml` sets the reward items, XP, bonus streak, daily limit, leveling thresholds,
join message, and whether players get the book. Progress is stored in `plugins/MathQuest/progress.yml`.

## Building

Requires JDK 21+. The build compiles against the Paper API and Floodgate jars from a Paper server:

```bash
./build.sh                          # uses /opt/minecraft/server
SERVER=/path/to/server ./build.sh   # or another server
```

The output is `dist/MathQuest.jar`. Copy it into the server's `plugins/` folder and restart.

Tested on Paper 26.2 with Geyser 2.11 and Floodgate 2.2.5.

## Troubleshooting

If Bedrock players freeze or time out when typing `/`, set `command-suggestions: false` in
`plugins/Geyser-Spigot/config.yml` and restart. Players can use the book in the meantime.
