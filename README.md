# ✨ Regalor Elementals - Minecraft Power Plugin ✨

Welcome to Regalor Elementals! This Spigot/Paper plugin allows server administrators to create unique elemental powers, represented by custom items, that grant players special abilities upon use. Players receive a random power upon joining or can obtain their assigned power via a command.

---

---

## 🚀 Features

* ✨ **Highly Customizable Powers:** Define unique abilities with effects, projectiles, flight, and more via simple YAML files. No coding needed!
* 💎 **Custom Items:** Control the name, material, lore, and even CustomModelData of power items.
* ⏳ **Cooldown System:** Prevent ability spam with configurable cooldowns shown on a Boss Bar.
* ❤️‍🩹 **Resource Costs:** Optionally require XP levels or player health to use abilities.
* 🔊 **Effects & Sounds:** Add particle effects and sounds for ability activation feedback.
* 🎲 **Random Assignment:** Players get a random power using `/rsobtain` if they don't have one.
* 🛡️ **Item Protection:** Power items are protected from dropping, being placed, moved into chests/armor slots, or lost on death (returns on respawn). Prevents duplication.
* 🛠️ **Admin Control:** Reload configs and change player powers easily.

---

## 📦 Installation

1.  Download the latest `RegalorElementals-X.X.X.jar` file from the Releases page.
2.  Stop your server.
3.  Place the `.jar` file into your server's `plugins/` directory.
4.  Start your server.
5.  The plugin will generate `config.yml` and an empty `powers/` folder inside `plugins/RegalorElementals/`.
6.  Start creating your awesome powers inside the `powers/` folder! 🎉

---

## ⚙️ Configuration (`config.yml`)

This is the main configuration file located in `plugins/RegalorElementals/config.yml`.

* `debug-mode: <true|false>`
    * Set to `true` to enable detailed console logs for troubleshooting power loading and usage. Recommended to keep `false` during normal operation.
    ```yaml
    debug-mode: false
    ```
* `registered-powers: [list]`
    * A list of the **filenames** (without `.yml`) of the powers you want the plugin to load from the `powers/` folder. The plugin will only recognize powers listed here.
    ```yaml
    registered-powers:
      - "infernos"
      - "aerol"
      - "shield"
      - "stormbringer"
      # Add more power file names here
    ```
* `received-powers: {map}`
    * ⚠️ **Do not manually edit this section!**
    * The plugin uses this section to store which power has been assigned to each player's UUID.
* **Inventory Full Messages** (`messages:` section within `config.yml`)
    * Customize the title/subtitle shown when a player tries to obtain an item but their inventory is full.
    ```yaml
    messages:
      inventory-full-title: "&cInventory Full!"
      inventory-full-subtitle: "&eClear space to receive item!"
    ```

---

## 🪄 Creating Custom Powers (`powers/` folder)

This is the core of the plugin! Each unique power is defined by its own `.yml` file inside `plugins/RegalorElementals/powers/`. For example, a power named "Infernos" would be configured in `plugins/RegalorElementals/powers/infernos.yml`, and you must add `"infernos"` to the `registered-powers` list in `config.yml`.

Here's a breakdown of all available options within a power's `.yml` file:

<details>
<summary><strong>🏷️ Item Configuration</strong></summary>

> _Defines how the power item looks in-game._

* **`display-name: "<name>"`**
    * The item's name. Use `&` for [Minecraft color codes](https://minecraft.tools/en/color-code.php).
    ```yaml
    display-name: "&6&lInfernos"
    ```
* **`material: <MATERIAL_NAME>`**
    * The item's type. Use standard [Bukkit Material names](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html).
    ```yaml
    material: NETHER_STAR
    ```
* **`custom-model-data: <number>`** (Optional)
    * Sets the `CustomModelData` NBT tag for use with resource packs. Defaults to `0`.
    ```yaml
    custom-model-data: 1001
    ```
* **`lore: [list]`**
    * A list of strings representing the item's description lines below the name. Use `&` for colors. Placeholders like `%cooldown%` can sometimes be used here, but they won't update dynamically.
    ```yaml
    lore:
      - "&eHarness the power of the nether."
      - "&7Right-click to unleash a fireball!"
      - ""
      - "&cCooldown: 5 seconds" # Example static text
    ```

</details>

<details>
<summary><strong>⚙️ Gameplay Configuration</strong></summary>

> _Controls the power's mechanics._

* **`spawn-chance: <number>`**
    * Determines the likelihood of this power being assigned randomly via `/rsobtain`. Higher numbers mean a higher chance relative to other powers. Set to `0` to disable random assignment.
    ```yaml
    spawn-chance: 3
    ```
* **`cooldown: <seconds>`**
    * The time in seconds a player must wait after using the ability before they can use it again. Set to `0` for no cooldown.
    ```yaml
    cooldown: 5
    ```
* **`post-flight-cooldown: <seconds>`**
    * **Only applicable if `ability.type` is `flight`**. This is the cooldown *after* the flight duration ends before the player can activate the flight ability again.
    ```yaml
    post-flight-cooldown: 10
    ```

</details>

<details>
<summary><strong>❤️‍🩹 Resource Costs</strong></summary>

> _Optionally make abilities consume resources._

* **`use-xp-cost: <true|false>`**
    * Set to `true` to require the player to have a certain number of XP levels. Default: `false`.
    ```yaml
    use-xp-cost: true
    ```
* **`xp-cost-levels: <number>`**
    * The number of XP *levels* deducted if `use-xp-cost` is `true`. Default: `1`.
    ```yaml
    xp-cost-levels: 1
    ```
* **`use-health-cost: <true|false>`**
    * Set to `true` to require the player to sacrifice health. Default: `false`.
    ```yaml
    use-health-cost: true
    ```
* **`health-cost: <half-hearts>`**
    * The amount of health (in half-hearts) deducted if `use-health-cost` is `true`. `2.0` equals one full heart. The plugin prevents fatal damage from this cost. Default: `2.0`.
    ```yaml
    health-cost: 4.0 # Costs 2 hearts
    ```

</details>

<details>
<summary><strong>✨ Visuals & Sounds (`effects:` section)</strong></summary>

> _Feedback when the ability activates._

* **`activation-sound: <SOUND_NAME>`**
    * The sound played on activation. Use [Bukkit Sound enum names](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html) (e.g., `ENTITY_PLAYER_LEVELUP`, `ENTITY_GHAST_SHOOT`, `BLOCK_ANVIL_LAND`). Set to `NONE` or leave empty to disable.
    ```yaml
    activation-sound: ENTITY_GHAST_SHOOT
    ```
* **`activation-sound-volume: <number>`**
    * The volume of the sound (e.g., `1.0` is default). Default: `1.0`.
    ```yaml
    activation-sound-volume: 1.0
    ```
* **`activation-sound-pitch: <number>`**
    * The pitch of the sound (e.g., `1.0` is default). <1 is deeper, >1 is sharper. Default: `1.0`.
    ```yaml
    activation-sound-pitch: 0.8
    ```
* **`activation-particle: <PARTICLE_NAME>`**
    * The particle effect spawned on activation. Use [Bukkit Particle enum names](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html) (e.g., `FLAME`, `TOTEM`, `EXPLOSION_NORMAL`, `CLOUD`). Set to `NONE` or leave empty to disable.
    ```yaml
    activation-particle: FLAME
    ```
* **`activation-particle-count: <number>`**
    * How many particles to spawn. Default: `10`.
    ```yaml
    activation-particle-count: 25
    ```
* **`activation-particle-offset-x / y / z: <number>`**
    * Random spread distance on each axis from the spawn point. Default: `0.1`.
    ```yaml
    activation-particle-offset-x: 0.5
    activation-particle-offset-y: 0.5
    activation-particle-offset-z: 0.5
    ```
* **`activation-particle-speed: <number>`**
    * The speed (or "extra" data) for the particles. Interpretation varies by particle type. Default: `0.05`.
    ```yaml
    activation-particle-speed: 0.1
    ```
* **`activation-particle-color: "<data>"`**
    * Used for particles that support extra data, like `REDSTONE` (color), `BLOCK_CRACK` (material), `ITEM_CRACK` (material).
    * For `REDSTONE`: Use `"R,G,B"` format (e.g., `"255,165,0"` for orange).
    * For `BLOCK_CRACK`, `ITEM_CRACK`: Use the Bukkit Material name (e.g., `"DIRT"`).
    * Leave empty `""` otherwise.
    ```yaml
    activation-particle-color: "" # Not needed for FLAME
    ```

</details>

<details>
<summary><strong>⚡ Ability Configuration (`ability:` section)</strong></summary>

> _The core action of the power._

* **`type: <ability_type>`**
    * Determines the kind of ability. Choose **one** from: `effect`, `projectile`, `flight`, `instamine`, `lightning`, `none`.

* **Settings for `type: effect`**
    * `effect`: [PotionEffectType name](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html) (e.g., `SPEED`, `INVISIBILITY`). Also supports `HERO_OF_THE_VILLAGE`.
    * `duration`: Effect duration in seconds.
    * `level`: Effect level (1 = Level I, 2 = Level II, etc.).
    ```yaml
    ability:
      type: effect
      effect: SPEED
      duration: 10
      level: 2
    ```

* **Settings for `type: projectile`**
    * `projectile-type`: [EntityType name](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html) (e.g., `FIREBALL`, `ARROW`, `WITHER_SKULL`).
    * `speed`: Speed multiplier (1.0 = normal).
    * `damage`: Arrow damage or explosion power/yield.
    * `ignite: <true|false>`: Ignites targets (Fireball) or makes Wither Skull charged (blue).
    * `knockback: <number>`: Knockback strength (mainly for arrows).
    * `duration: <seconds>`: (For potion arrows) Effect duration.
    * `level: <number>`: (For potion arrows) Effect level.
    ```yaml
    ability:
      type: projectile
      projectile-type: POISON_ARROW
      speed: 1.2
      damage: 5.0
      ignite: false
      knockback: 0
      duration: 5 # Poison duration
      level: 1 # Poison I
    ```

* **Settings for `type: flight`**
    * `duration: <seconds>`: Flight duration in seconds.
    ```yaml
    ability:
      type: flight
      duration: 15
    ```

* **Settings for `type: instamine`**
    * `duration: <seconds>`: Duration of Haste effect (use large number for 'permanent').
    * `level: <number>`: Haste level (3+ often needed).
    ```yaml
    ability:
      type: instamine
      duration: 30
      level: 3
    ```

* **Settings for `type: lightning`**
    * `range: <blocks>`: Max distance to target the lightning strike. Default: `50`.
    * `damage: <number>`: (Informational) Standard lightning damage is ~5. Not used directly by default strike.
    ```yaml
    ability:
      type: lightning
      range: 60
      damage: 5.0
    ```

* **Settings for `type: none`**
    * No additional ability settings needed. Just triggers effects/sounds.

</details>

<details>
<summary><strong>💬 Messages (`messages:` section)</strong></summary>

> _Customize player feedback text. Use `&` for color codes. Placeholders `%seconds%`, `%power%`, `%required%`, `%duration%` are replaced where applicable._

* **`actionbar-message: "<text>"`**: Shown briefly above hotbar when using.
* **`cooldown-message: "<text>"`**: Shown when ability is on cooldown. Use `%seconds%`.
* **`bossbar-title: "<text>"`**: Text on the cooldown Boss Bar. Use `%power%`.
* **`ability-used-message: "<text>"`**: Shown when ability succeeds. Use `%power%`.
* **`not-enough-xp-message: "<text>"`**: Shown if lacking XP. Use `%required%`.
* **`not-enough-health-message: "<text>"`**: Shown if lacking health. Use `%required%` (shows hearts).
* **`no-target-message: "<text>"`**: Shown for targeted abilities (like `lightning`) if no target found.
* **`flight-start-message: "<text>"`**: (For `flight` type) Shown when flight starts. Use `%duration%`.
* **`flight-end-message: "<text>"`**: (For `flight` type) Shown when flight ends.
* **`post-flight-cooldown-message: "<text>"`**: (For `flight` type) Shown if trying to fly during post-flight cooldown. Use `%seconds%`.

```yaml
messages:
  actionbar-message: "&eUsing &6&l%power%&e..."
  cooldown-message: "&c%power% Cooldown! &7(%seconds%s)"
  bossbar-title: "&6&l%power% &cCooldown"
  ability-used-message: "&aUsed %power%!"
  not-enough-xp-message: "&cNot enough XP Levels! Need %required%."
  not-enough-health-message: "&cNot enough Health! Need %required% hearts."
  no-target-message: "&cNo target in range!"
  flight-start-message: "&aFlight active for %duration% seconds!"
  flight-end-message: "&cFlight ended."
  post-flight-cooldown-message: "&cCannot fly yet! Wait %seconds%s."
