# Newer Style HitSplats

A client-side RuneLite plugin that redraws combat hitsplats in a compact newer-era style: a small context-aware icon beside a beveled rectangular damage counter.

## Features

- Replaces the circular native appearance with compact rectangular hitsplats.
- Uses RuneLite's built-in skill and item sprites; no downloaded assets or external services.
- Shows Attack, Strength, Defence, Ranged, or Magic for normal combat styles.
- Detects damage spells cast directly from the spellbook as Magic, even when the equipped weapon remains on a melee style.
- Shows every relevant icon for shared-XP styles:
  - Controlled: Attack, Strength, and Defence
  - Longrange: Ranged and Defence
  - Defensive Casting: Magic and Defence
- Uses Antipoison and Anti-venom potion icons for poison and venom damage.
- Uses appropriate icons and colors for healing, disease, prayer drain, burn, bleed, and other special hits.
- Supports configurable scale, opacity, smooth fading, visibility filters, and optional 2010-style x10 damage numbers.

## Privacy and game behavior

This plugin is cosmetic only. It does not automate input, alter game state, access the network, collect data, or write files.

## Development

The project requires Java 11. Open it in IntelliJ IDEA, select an Eclipse Temurin 11 Gradle JVM, and run the `test` Gradle task. Use the `run` task to launch a RuneLite development client.

## License

Newer Style HitSplats is available under the BSD 2-Clause License. See [LICENSE](LICENSE).
