package me.lssupportteam.regalorElementals;

import com.google.common.base.Preconditions;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RegalorElementals extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private static final String CONFIG_DEBUG_MODE = "debug-mode";
    private static final String CONFIG_REGISTERED_POWERS = "registered-powers";
    private static final String CONFIG_RECEIVED_POWERS = "received-powers";
    private static final String POWER_CONFIG_DISPLAY_NAME = "display-name";
    private static final String POWER_CONFIG_LORE = "lore";
    private static final String POWER_CONFIG_MATERIAL = "material";
    private static final String POWER_CONFIG_CUSTOM_MODEL_DATA = "custom-model-data";
    private static final String POWER_CONFIG_SPAWN_CHANCE = "spawn-chance";
    private static final String POWER_CONFIG_COOLDOWN = "cooldown";
    private static final String POWER_CONFIG_POST_FLIGHT_COOLDOWN = "post-flight-cooldown";
    private static final String POWER_CONFIG_USE_XP_COST = "use-xp-cost";
    private static final String POWER_CONFIG_XP_COST_LEVELS = "xp-cost-levels";
    private static final String POWER_CONFIG_USE_HEALTH_COST = "use-health-cost";
    private static final String POWER_CONFIG_HEALTH_COST = "health-cost";
    private static final String POWER_CONFIG_BOSSBAR_COLOR = "bossbar-color";
    private static final String POWER_CONFIG_MESSAGES = "messages";
    private static final String POWER_CONFIG_MSG_COOLDOWN = "cooldown-message";
    private static final String POWER_CONFIG_MSG_ACTIONBAR = "actionbar-message";
    private static final String POWER_CONFIG_MSG_BOSSBAR_TITLE = "bossbar-title";
    private static final String POWER_CONFIG_MSG_FLIGHT_START = "flight-start-message";
    private static final String POWER_CONFIG_MSG_FLIGHT_END = "flight-end-message";
    private static final String POWER_CONFIG_MSG_POST_FLIGHT_COOLDOWN = "post-flight-cooldown-message";
    private static final String POWER_CONFIG_MSG_NOT_ENOUGH_XP = "not-enough-xp-message";
    private static final String POWER_CONFIG_MSG_NOT_ENOUGH_HEALTH = "not-enough-health-message";
    private static final String POWER_CONFIG_MSG_ABILITY_USED = "ability-used-message";
    private static final String POWER_CONFIG_MSG_NO_TARGET = "no-target-message";
    private static final String POWER_CONFIG_MSG_INV_FULL_TITLE = "inventory-full-title";
    private static final String POWER_CONFIG_MSG_INV_FULL_SUBTITLE = "inventory-full-subtitle";
    private static final String POWER_CONFIG_EFFECTS_SOUND = "effects.activation-sound";
    private static final String POWER_CONFIG_EFFECTS_SOUND_VOLUME = "effects.activation-sound-volume";
    private static final String POWER_CONFIG_EFFECTS_SOUND_PITCH = "effects.activation-sound-pitch";
    private static final String POWER_CONFIG_EFFECTS_PARTICLE = "effects.activation-particle";
    private static final String POWER_CONFIG_EFFECTS_PARTICLE_COUNT = "effects.activation-particle-count";
    private static final String POWER_CONFIG_EFFECTS_PARTICLE_OFFSET_X = "effects.activation-particle-offset-x";
    private static final String POWER_CONFIG_EFFECTS_PARTICLE_OFFSET_Y = "effects.activation-particle-offset-y";
    private static final String POWER_CONFIG_EFFECTS_PARTICLE_OFFSET_Z = "effects.activation-particle-offset-z";
    private static final String POWER_CONFIG_EFFECTS_PARTICLE_SPEED = "effects.activation-particle-speed";
    private static final String POWER_CONFIG_EFFECTS_PARTICLE_COLOR = "effects.activation-particle-color";
    private static final String POWER_CONFIG_ABILITY_TYPE = "ability.type";
    private static final String POWER_CONFIG_ABILITY_EFFECT = "ability.effect";
    private static final String POWER_CONFIG_ABILITY_DURATION = "ability.duration";
    private static final String POWER_CONFIG_ABILITY_LEVEL = "ability.level";
    private static final String POWER_CONFIG_ABILITY_RANGE = "ability.range";
    private static final String POWER_CONFIG_ABILITY_PROJECTILE_TYPE = "ability.projectile-type";
    private static final String POWER_CONFIG_ABILITY_SPEED = "ability.speed";
    private static final String POWER_CONFIG_ABILITY_DAMAGE = "ability.damage";
    private static final String POWER_CONFIG_ABILITY_IGNITE = "ability.ignite";
    private static final String POWER_CONFIG_ABILITY_KNOCKBACK = "ability.knockback";


    private final Map<UUID, PowerData> playerPowers = new ConcurrentHashMap<>();
    private final Map<String, PowerConfig> powerConfigs = new ConcurrentHashMap<>();
    private final Map<UUID, CasterData> casterMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> receivedPowers = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> cooldownTasks = new ConcurrentHashMap<>();

    private File powersFolder;
    private File casterFolder;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        powersFolder = new File(getDataFolder(), "powers");
        casterFolder = new File(getDataFolder(), "caster");
        if (!powersFolder.exists()) powersFolder.mkdirs();
        if (!casterFolder.exists()) casterFolder.mkdirs();

        loadAllPowerConfigs();
        loadCasterData();
        loadReceivedPlayers();

        logServerVersion();

        getServer().getPluginManager().registerEvents(this, this);
        registerCommand("rshelp");
        registerCommand("rsobtain");
        registerCommand("rsrebirth");
        registerCommand("rsreload");

        resumeCooldownsOnLoad();

        getLogger().info("RegalorElementals enabled successfully.");
    }

    @Override
    public void onDisable() {
        cancelAllTasks();
        clearAllBossBars();
        saveAllCasterData();
        saveReceivedPlayers();
        getLogger().info("RegalorElementals disabled.");
    }

    private void registerCommand(String commandName) {
        if (getCommand(commandName) != null) {
            getCommand(commandName).setExecutor(this);
            getCommand(commandName).setTabCompleter(this);
        } else {
            getLogger().severe("Command '" + commandName + "' is not registered in plugin.yml!");
        }
    }

    private void loadAllPowerConfigs() {
        powerConfigs.clear();
        List<String> registeredPowers = getConfig().getStringList(CONFIG_REGISTERED_POWERS);

        if (registeredPowers.isEmpty()) {
            getLogger().warning("No powers listed under 'registered-powers' in config.yml!");
            return;
        }

        if (!powersFolder.exists() || !powersFolder.isDirectory()) {
            getLogger().severe("Powers folder is missing or not a directory: " + powersFolder.getPath());
            return;
        }

        for (String powerName : registeredPowers) {
            File powerFile = new File(powersFolder, powerName + ".yml");
            if (!powerFile.exists()) {
                getLogger().warning("Missing power config file: " + powerName + ".yml (referenced in config.yml)");
                continue;
            }

            YamlConfiguration powerYaml = YamlConfiguration.loadConfiguration(powerFile);
            try {
                PowerConfig config = new PowerConfig(powerName, powerYaml, this);
                powerConfigs.put(powerName.toLowerCase(), config);
                if (isDebugMode()) {
                    getLogger().info("Loaded power config: " + powerName);
                }
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to load power configuration for '" + powerName + "' from " + powerFile.getName(), e);
            }
        }
        getLogger().info("Loaded " + powerConfigs.size() + " power configurations.");
    }

    private void loadCasterData() {
        casterMap.clear();
        if (!casterFolder.exists() || !casterFolder.isDirectory()) return;

        File[] files = casterFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String powerName = config.getString("powerName");
                long cooldownEnd = config.getLong("cooldownEnd", 0);
                int postFlight = config.getInt("postFlightCooldownTicks", 0);

                if (powerName != null && !powerName.isEmpty()) {
                    if (System.currentTimeMillis() < cooldownEnd || postFlight > 0) {
                        casterMap.put(uuid, new CasterData(powerName, cooldownEnd, postFlight));
                        if (isDebugMode()) {
                            getLogger().info("Loaded active caster data for " + uuid);
                        }
                    } else {
                        if (!file.delete()) {
                            getLogger().warning("Could not delete expired caster data file: " + file.getName());
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID format in filename: " + file.getName());
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Error loading caster data from " + file.getName(), e);
            }
        }
        getLogger().info("Loaded caster data for " + casterMap.size() + " players.");
    }


    private void saveCasterData(UUID uuid, CasterData data) {
        File file = new File(casterFolder, uuid + ".yml");
        if (data.needsSaving()) {
            YamlConfiguration config = new YamlConfiguration();
            config.set("powerName", data.powerName);
            config.set("cooldownEnd", data.cooldownEnd);
            config.set("postFlightCooldownTicks", data.postFlightCooldownTicks);

            try {
                config.save(file);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to save caster data for " + uuid, e);
            }
        } else {
            if (file.exists()) {
                if (!file.delete()) {
                    getLogger().warning("Could not delete caster data file upon expiry: " + file.getName());
                }
            }
        }
    }

    private void saveAllCasterData() {
        getLogger().info("Saving caster data...");
        casterMap.forEach(this::saveCasterData);

        if (casterFolder.exists() && casterFolder.isDirectory()) {
            File[] files = casterFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    try {
                        UUID fileUuid = UUID.fromString(file.getName().replace(".yml", ""));
                        if (!casterMap.containsKey(fileUuid) || !casterMap.get(fileUuid).needsSaving()) {
                            if (!file.delete()) {
                                getLogger().warning("Could not delete obsolete caster file: " + file.getName());
                            }
                        }
                    } catch (IllegalArgumentException ignore) { }
                }
            }
        }
        getLogger().info("Caster data saved.");
    }


    private void loadReceivedPlayers() {
        receivedPowers.clear();
        ConfigurationSection receivedSection = getConfig().getConfigurationSection(CONFIG_RECEIVED_POWERS);
        if (receivedSection != null) {
            for (String uuidString : receivedSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String power = receivedSection.getString(uuidString);
                    if (power != null && !power.isEmpty() && powerConfigs.containsKey(power.toLowerCase())) {
                        receivedPowers.put(uuid, power);
                    } else {
                        getLogger().warning("Invalid or unknown power '" + power + "' listed for UUID " + uuidString + " in config.yml");
                    }
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid UUID format '" + uuidString + "' found in received-powers section of config.yml");
                }
            }
        }
        getLogger().info("Loaded received power data for " + receivedPowers.size() + " players.");
    }

    private void saveReceivedPlayers() {
        Map<String, String> dataToSave = new HashMap<>();
        receivedPowers.forEach((uuid, powerName) -> {
            if (powerName != null && !powerName.isEmpty()) {
                dataToSave.put(uuid.toString(), powerName);
            }
        });
        getConfig().set(CONFIG_RECEIVED_POWERS, dataToSave);
        saveConfig();
        if (isDebugMode()) {
            getLogger().info("Saved received powers data.");
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (receivedPowers.containsKey(uuid)) {
            String powerName = receivedPowers.get(uuid);
            if (powerName != null && powerConfigs.containsKey(powerName.toLowerCase())) {
                playerPowers.put(uuid, new PowerData(powerName));
                if (!hasPowerItem(player, powerName)) {
                    getLogger().info("Player " + player.getName() + " joined without their power item ("+powerName+"). Use /rsobtain to get it back.");
                }
            } else {
                getLogger().warning("Player " + player.getName() + " had an invalid power (" + powerName + ") saved. They need to use /rsobtain.");
                receivedPowers.remove(uuid);
                playerPowers.remove(uuid);
                saveReceivedPlayers();
            }
        }

        resumeCooldown(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (casterMap.containsKey(uuid)) {
            saveCasterData(uuid, casterMap.get(uuid));
        }
        playerPowers.remove(uuid);
        clearBossBar(event.getPlayer());
        cancelCooldownTask(uuid);
    }

    private void resumeCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        CasterData data = casterMap.get(uuid);
        if (data != null) {
            PowerConfig config = powerConfigs.get(data.powerName.toLowerCase());
            if (config != null) {
                if (data.isOnCooldown()) {
                    startCooldownBar(player, config, data.getRemainingCooldownMillis());
                }
                if (data.postFlightCooldownTicks > 0) {
                    startPostFlightCooldown(player, config, data.postFlightCooldownTicks);
                }
            } else {
                getLogger().warning("Player " + player.getName() + " had saved caster data for an unknown power: " + data.powerName);
                casterMap.remove(uuid);
                File file = new File(casterFolder, uuid + ".yml");
                if(file.exists() && !file.delete()){
                    getLogger().warning("Could not delete invalid caster file: " + file.getName());
                }
            }
        }
    }

    private void resumeCooldownsOnLoad() {
        getLogger().info("Resuming cooldowns for online players...");
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (casterMap.containsKey(player.getUniqueId())) {
                resumeCooldown(player);
                count++;
            }
        }
        getLogger().info("Resumed cooldowns for " + count + " players.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ItemStack item = event.getItem();

        PowerConfig powerConfig = getPowerConfigFromItem(item);
        if (powerConfig == null) {
            if (isPowerItem(item)) {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
            }
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);


        PowerData powerData = playerPowers.get(uuid);
        if (powerData == null || !powerData.powerName.equalsIgnoreCase(powerConfig.name)) {
            player.sendMessage(ChatColor.RED + "Error: This item does not match your assigned power. Use /rsobtain if needed.");
            return;
        }


        CasterData casterData = casterMap.get(uuid);
        if (casterData != null && casterData.isOnCooldown()) {
            String cooldownMsg = powerConfig.getMessage(POWER_CONFIG_MSG_COOLDOWN, "&cAbility on cooldown! &7(%seconds%s remaining)");
            sendActionBar(player, cooldownMsg.replace("%seconds%", String.valueOf(casterData.getRemainingCooldownSeconds())));
            return;
        }
        if (casterData != null && casterData.postFlightCooldownTicks > 0 && "flight".equalsIgnoreCase(powerConfig.abilityType)) {
            String postFlightMsg = powerConfig.getMessage(POWER_CONFIG_MSG_POST_FLIGHT_COOLDOWN, "&cCannot fly yet! Wait &e%seconds%s");
            sendActionBar(player, postFlightMsg.replace("%seconds%", String.valueOf(casterData.postFlightCooldownTicks / 20 + 1)));
            return;
        }


        if (powerConfig.useXpCost && !checkAndDeductExperience(player, powerConfig.xpCostLevels, powerConfig)) {
            return;
        }
        if (powerConfig.useHealthCost && !checkAndDeductHealth(player, powerConfig.healthCost, powerConfig)) {
            return;
        }


        if (usePower(player, powerConfig)) {
            String usedMsg = powerConfig.getMessage(POWER_CONFIG_MSG_ABILITY_USED, "&aUsed %power%!");
            sendActionBar(player, usedMsg.replace("%power%", powerConfig.displayName));

            if (powerConfig.cooldown > 0) {
                CasterData newCasterData = casterMap.computeIfAbsent(uuid, k -> new CasterData(powerConfig.name, 0, 0));
                newCasterData.startCooldown(powerConfig.cooldown);
                startCooldownBar(player, powerConfig, powerConfig.cooldown * 1000L);
            }

            playSound(player, powerConfig);
            spawnParticles(player.getEyeLocation(), powerConfig);
        }
    }


    private boolean usePower(Player player, PowerConfig config) {
        String abilityType = config.abilityType.toLowerCase();
        boolean success = false;

        switch (abilityType) {
            case "effect":
                success = applyEffect(player, config);
                break;
            case "instamine":
                success = applyInstamine(player, config);
                break;
            case "flight":
            case "fly":
                success = enableFlight(player, config);
                break;
            case "projectile":
                success = launchProjectile(player, config);
                break;
            case "lightning":
                success = strikeLightning(player, config);
                break;
            case "none":
                player.sendMessage(ChatColor.YELLOW + "This power item currently has no active ability.");
                success = false;
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown ability type configured: " + config.abilityType);
                getLogger().warning("Player " + player.getName() + " tried to use power '" + config.name + "' with unknown ability type: " + config.abilityType);
                success = false;
                break;
        }

        if (success && isDebugMode()) {
            getLogger().info("Player " + player.getName() + " used power: " + config.name + " (Type: " + abilityType + ")");
        }
        return success;
    }


    private boolean applyEffect(Player player, PowerConfig config) {
        PotionEffectType effectType = PotionEffectType.getByName(config.effectType);

        if (effectType == null) {
            if ("hero-village".equalsIgnoreCase(config.effectType) || "HERO_OF_THE_VILLAGE".equalsIgnoreCase(config.effectType)) {
                try {
                    effectType = PotionEffectType.HERO_OF_THE_VILLAGE;
                } catch (NoSuchFieldError e) {
                    player.sendMessage(ChatColor.RED + "Hero of the Village effect not supported on this server version.");
                    return false;
                }
            }
            if (effectType == null) {
                player.sendMessage(ChatColor.RED + "Invalid or unsupported effect type: " + config.effectType);
                getLogger().warning("Power '" + config.name + "' configured with invalid effect type: " + config.effectType);
                return false;
            }
        }

        try {
            player.addPotionEffect(new PotionEffect(
                    effectType,
                    config.abilityDuration * 20,
                    Math.max(0, config.abilityLevel - 1)
            ), true);
            return true;
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error applying effect: " + config.effectType);
            getLogger().log(Level.SEVERE, "Exception applying effect " + config.effectType + " for power " + config.name, e);
            return false;
        }
    }

    private boolean applyInstamine(Player player, PowerConfig config) {
        try {
            int durationTicks = (config.abilityDuration > 0) ? config.abilityDuration * 20 : 999999;
            int level = (config.abilityLevel > 0) ? config.abilityLevel : 3;

            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.FAST_DIGGING,
                    durationTicks,
                    Math.max(0, level - 1)
            ), true);
            return true;
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error applying Instamine effect.");
            getLogger().log(Level.SEVERE, "Exception applying instamine for power " + config.name, e);
            return false;
        }
    }

    private boolean enableFlight(Player player, PowerConfig config) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            player.sendMessage(ChatColor.YELLOW + "Flight ability has no effect in your current game mode.");
            return false;
        }

        player.setAllowFlight(true);
        player.setFlying(true);
        String startMsg = config.getMessage(POWER_CONFIG_MSG_FLIGHT_START, "&aYou take to the skies for &e%duration% &aseconds!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', startMsg.replace("%duration%", String.valueOf(config.abilityDuration))));


        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    String endMsg = config.getMessage(POWER_CONFIG_MSG_FLIGHT_END, "&cYour flight power wears off.");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', endMsg));

                    if (config.postFlightCooldown > 0) {
                        startPostFlightCooldown(player, config, config.postFlightCooldown * 20);
                    }
                }
            }
        }.runTaskLater(this, config.abilityDuration * 20L);

        return true;
    }

    private void startPostFlightCooldown(Player player, PowerConfig config, int ticks) {
        UUID uuid = player.getUniqueId();
        CasterData data = casterMap.computeIfAbsent(uuid, k -> new CasterData(config.name, 0, 0));
        data.startPostFlightCooldown(ticks);

        String postFlightMsg = config.getMessage(POWER_CONFIG_MSG_POST_FLIGHT_COOLDOWN, "&cYou must wait &e%seconds%s &cto fly again.");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', postFlightMsg.replace("%seconds%", String.valueOf(ticks / 20))));

        BossBar postFlightBar = activeBossBars.computeIfAbsent(uuid, k -> Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID));
        postFlightBar.setColor(BarColor.BLUE);
        postFlightBar.setTitle(ChatColor.translateAlternateColorCodes('&', "&bFlight Cooldown"));
        postFlightBar.setProgress(1.0);
        postFlightBar.addPlayer(player);
        postFlightBar.setVisible(true);

        cancelCooldownTask(uuid);
        BukkitTask task = new BukkitRunnable() {
            int remainingTicks = ticks;

            @Override
            public void run() {
                CasterData currentData = casterMap.get(uuid);
                if (!player.isOnline() || remainingTicks <= 0 || currentData == null) {
                    clearBossBar(player);
                    if (currentData != null) {
                        currentData.postFlightCooldownTicks = 0;
                        if (!currentData.needsSaving()) casterMap.remove(uuid);
                    }
                    cooldownTasks.remove(uuid);
                    this.cancel();
                    return;
                }

                postFlightBar.setProgress(Math.max(0.0, (double) remainingTicks / ticks));
                currentData.postFlightCooldownTicks = remainingTicks;
                remainingTicks--;
            }
        }.runTaskTimer(this, 0L, 1L);
        cooldownTasks.put(uuid, task);
    }

    private boolean launchProjectile(Player player, PowerConfig config) {
        String projectileType = config.projectileType.toLowerCase();
        Location launchLoc = player.getEyeLocation();
        Vector direction = launchLoc.getDirection();
        double speed = config.projectileSpeed;

        try {
            Projectile projectile;
            switch (projectileType) {
                case "fireball":
                    projectile = player.launchProjectile(Fireball.class, direction.multiply(speed));
                    ((Fireball) projectile).setYield((float) config.projectileDamage);
                    ((Fireball) projectile).setIsIncendiary(config.projectileIgnite);
                    break;

                case "small_fireball":
                    projectile = player.launchProjectile(SmallFireball.class, direction.multiply(speed));
                    ((SmallFireball) projectile).setYield((float) config.projectileDamage);
                    ((SmallFireball) projectile).setIsIncendiary(config.projectileIgnite);
                    break;

                case "arrow":
                    projectile = player.launchProjectile(Arrow.class, direction.multiply(speed));
                    ((Arrow) projectile).setDamage(config.projectileDamage);
                    ((Arrow) projectile).setKnockbackStrength(config.abilityKnockback);
                    break;

                case "poison_arrow":
                    projectile = player.launchProjectile(Arrow.class, direction.multiply(speed));
                    ((Arrow) projectile).setDamage(config.projectileDamage);
                    ((Arrow) projectile).setKnockbackStrength(config.abilityKnockback);
                    ((Arrow) projectile).addCustomEffect(new PotionEffect(
                            PotionEffectType.POISON,
                            config.abilityDuration * 20,
                            Math.max(0, config.abilityLevel - 1)
                    ), true);
                    break;

                case "slowness_arrow":
                    projectile = player.launchProjectile(Arrow.class, direction.multiply(speed));
                    ((Arrow) projectile).setDamage(config.projectileDamage);
                    ((Arrow) projectile).setKnockbackStrength(config.abilityKnockback);
                    ((Arrow) projectile).addCustomEffect(new PotionEffect(
                            PotionEffectType.SLOW,
                            config.abilityDuration * 20,
                            Math.max(0, config.abilityLevel - 1)
                    ), true);
                    break;


                case "blindness_arrow":
                    projectile = player.launchProjectile(Arrow.class, direction.multiply(speed));
                    ((Arrow) projectile).setDamage(config.projectileDamage);
                    ((Arrow) projectile).setKnockbackStrength(config.abilityKnockback);
                    ((Arrow) projectile).addCustomEffect(new PotionEffect(
                            PotionEffectType.BLINDNESS,
                            config.abilityDuration > 0 ? config.abilityDuration * 20 : 100,
                            Math.max(0, config.abilityLevel - 1)
                    ), true);
                    break;

                case "wither_skull":
                    if (isWitherSkullSupported()) {
                        projectile = player.launchProjectile(WitherSkull.class, direction.multiply(speed));
                        ((WitherSkull) projectile).setYield((float) config.projectileDamage);
                        ((WitherSkull) projectile).setCharged(config.projectileIgnite);
                    } else {
                        player.sendMessage(ChatColor.RED + "Wither Skulls are not supported on this server version.");
                        return false;
                    }
                    break;

                case "snowball":
                    projectile = player.launchProjectile(Snowball.class, direction.multiply(speed));
                    break;

                case "egg":
                    projectile = player.launchProjectile(Egg.class, direction.multiply(speed));
                    break;

                case "ender_pearl":
                    projectile = player.launchProjectile(EnderPearl.class, direction.multiply(speed));
                    ((EnderPearl) projectile).setShooter((ProjectileSource) player);
                    break;

                case "trident":
                    projectile = player.launchProjectile(Trident.class, direction.multiply(speed));
                    ((Trident) projectile).setDamage(config.projectileDamage);
                    ((Trident) projectile).setKnockbackStrength(config.abilityKnockback);
                    break;


                default:
                    player.sendMessage(ChatColor.RED + "Unknown projectile type configured: " + config.projectileType);
                    getLogger().warning("Power '" + config.name + "' tried to launch unknown projectile: " + config.projectileType);
                    return false;
            }

            if(projectile != null && projectile instanceof AbstractArrow) {
                ((AbstractArrow) projectile).setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            }

            return true;

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error launching projectile!");
            getLogger().log(Level.SEVERE, "Exception launching projectile type " + projectileType + " for power " + config.name, e);
            return false;
        }
    }

    private boolean strikeLightning(Player player, PowerConfig config) {
        int range = config.abilityRange;
        World world = player.getWorld();

        RayTraceResult rayTraceResult = world.rayTraceBlocks(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                FluidCollisionMode.NEVER,
                true
        );

        Location targetLocation;

        if (rayTraceResult != null && rayTraceResult.getHitBlock() != null) {
            targetLocation = rayTraceResult.getHitBlock().getLocation();
        } else {
            Location endPoint = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(range));
            Block highestBlock = world.getHighestBlockAt(endPoint);
            if (highestBlock.getY() > world.getMinHeight() -1 ) {
                targetLocation = highestBlock.getLocation();
            } else {
                targetLocation = endPoint;
            }
        }

        if (targetLocation != null) {
            world.strikeLightning(targetLocation.add(0, 1, 0));
            return true;
        } else {
            String noTargetMsg = config.getMessage(POWER_CONFIG_MSG_NO_TARGET, "&cNo target in range!");
            sendActionBar(player, noTargetMsg);
            return false;
        }
    }


    private void startCooldownBar(Player player, PowerConfig config, long durationMillis) {
        UUID uuid = player.getUniqueId();
        CasterData casterData = casterMap.get(uuid);
        if (casterData == null) {
            getLogger().severe("Attempted to start cooldown bar for " + player.getName() + " but CasterData was null!");
            return;
        }

        BossBar bar = activeBossBars.computeIfAbsent(uuid, k -> {
            String title = config.getMessage(POWER_CONFIG_MSG_BOSSBAR_TITLE, "&c%power% Cooldown");
            title = title.replace("%power%", config.displayName);
            BarColor color = parseBarColor(config.bossbarColor, BarColor.RED);
            return Bukkit.createBossBar(ChatColor.translateAlternateColorCodes('&', title), color, BarStyle.SOLID);
        });

        String title = config.getMessage(POWER_CONFIG_MSG_BOSSBAR_TITLE, "&c%power% Cooldown");
        title = title.replace("%power%", config.displayName);
        bar.setTitle(ChatColor.translateAlternateColorCodes('&', title));
        bar.setColor(parseBarColor(config.bossbarColor, BarColor.RED));
        bar.setProgress(1.0);
        bar.addPlayer(player);
        bar.setVisible(true);

        cancelCooldownTask(uuid);

        long totalTicks = durationMillis / 50;
        if (totalTicks <= 0) {
            clearBossBar(player);
            casterData.cooldownEnd = 0;
            if (!casterData.needsSaving()) casterMap.remove(uuid);
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            long initialTotalTicks = totalTicks;

            @Override
            public void run() {
                CasterData currentData = casterMap.get(uuid);
                if (!player.isOnline() || currentData == null) {
                    cancelCooldownTask(uuid);
                    clearBossBar(player);
                    this.cancel();
                    return;
                }

                if (!currentData.isOnCooldown()) {
                    clearBossBar(player);
                    if (!currentData.needsSaving()) {
                        casterMap.remove(uuid);
                    }
                    cooldownTasks.remove(uuid);
                    this.cancel();
                    return;
                }

                long remainingMillis = currentData.getRemainingCooldownMillis();
                bar.setProgress(Math.max(0.0, Math.min(1.0, (double) remainingMillis / durationMillis)));
            }
        }.runTaskTimer(this, 0L, 1L);

        cooldownTasks.put(uuid, task);
    }

    private void cancelCooldownTask(UUID uuid) {
        BukkitTask task = cooldownTasks.remove(uuid);
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignore) {}
        }
    }

    private void clearBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bar = activeBossBars.remove(uuid);
        if (bar != null) {
            try {
                bar.removePlayer(player);
                bar.setVisible(false);
            } catch (Exception ignore) {}
        }
    }

    private void clearAllBossBars() {
        getLogger().info("Clearing active boss bars...");
        activeBossBars.forEach((uuid, bar) -> {
            try {
                bar.removeAll();
                bar.setVisible(false);
            } catch (Exception ignore) {}
        });
        activeBossBars.clear();
        getLogger().info("Active boss bars cleared.");
    }

    private void cancelAllTasks() {
        getLogger().info("Cancelling plugin tasks...");
        int count = cooldownTasks.size();
        cooldownTasks.values().forEach(task -> {
            try { task.cancel(); } catch (Exception ignore) {}
        });
        cooldownTasks.clear();

        Bukkit.getScheduler().cancelTasks(this);

        getLogger().info("Cancelled " + count + " tracked tasks (and potentially others registered by this plugin).");
    }


    private boolean checkAndDeductExperience(Player player, int levelsRequired, PowerConfig config) {
        if (levelsRequired <= 0) return true;

        if (player.getLevel() >= levelsRequired) {
            player.setLevel(player.getLevel() - levelsRequired);
            return true;
        } else {
            String noXpMsg = config.getMessage(POWER_CONFIG_MSG_NOT_ENOUGH_XP, "&cNot enough experience levels! Required: %required%");
            sendActionBar(player, noXpMsg.replace("%required%", String.valueOf(levelsRequired)));
            return false;
        }
    }


    private boolean checkAndDeductHealth(Player player, double healthCost, PowerConfig config) {
        if (healthCost <= 0) return true;

        double currentHealth = player.getHealth();
        if (currentHealth > healthCost) {
            player.setHealth(Math.max(0.1, currentHealth - healthCost));
            return true;
        } else {
            String noHealthMsg = config.getMessage(POWER_CONFIG_MSG_NOT_ENOUGH_HEALTH, "&cNot enough health! Required: %required% hearts");
            sendActionBar(player, noHealthMsg.replace("%required%", String.format("%.1f", healthCost / 2.0)));
            return false;
        }
    }


    private void sendActionBar(Player player, String message) {
        if (message == null || message.isEmpty()) return;
        try {
            player.sendActionBar(ChatColor.translateAlternateColorCodes('&', message));
        } catch (NoSuchMethodError e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            if (isDebugMode()) {
                getLogger().warning("sendActionBar method not found, falling back to chat message. Server version might be old.");
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error sending action bar message to " + player.getName(), ex);
        }
    }

    private void sendTitleMessage(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title == null && subtitle == null) return;
        String finalTitle = title != null ? ChatColor.translateAlternateColorCodes('&', title) : "";
        String finalSubtitle = subtitle != null ? ChatColor.translateAlternateColorCodes('&', subtitle) : "";
        try {
            player.sendTitle(finalTitle, finalSubtitle, fadeIn, stay, fadeOut);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error sending title message to " + player.getName(), ex);
            if (!finalTitle.isEmpty()) player.sendMessage(finalTitle);
            if (!finalSubtitle.isEmpty()) player.sendMessage(finalSubtitle);
        }
    }

    private void playSound(Player player, PowerConfig config) {
        String soundKey = config.activationSound;
        if (soundKey == null || soundKey.isEmpty() || "NONE".equalsIgnoreCase(soundKey)) return;
        try {
            Sound sound = Sound.valueOf(soundKey.toUpperCase().replace('-', '_'));
            player.playSound(player.getLocation(), sound, config.soundVolume, config.soundPitch);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Error: Invalid sound configured: " + soundKey);
            getLogger().warning("Invalid sound key '" + soundKey + "' in power configuration for '" + config.name + "'.");
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error playing sound " + soundKey + " for " + player.getName(), ex);
        }
    }

    private void spawnParticles(Location location, PowerConfig config) {
        String particleName = config.activationParticle;
        int count = config.activationParticleCount;
        if (particleName == null || particleName.isEmpty() || "NONE".equalsIgnoreCase(particleName) || count <= 0) return;

        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase().replace('-', '_'));
            World world = location.getWorld();
            if (world == null) return;

            double offsetX = config.particleOffsetX;
            double offsetY = config.particleOffsetY;
            double offsetZ = config.particleOffsetZ;
            double speed = config.particleSpeed;

            Object particleData = null;
            Class<?> dataType = particle.getDataType();

            if (dataType == Particle.DustOptions.class) {
                Color color = parseColor(config.activationParticleColor);
                if (color != null) {
                    particleData = new Particle.DustOptions(color, 1.0F);
                } else {
                    if (isDebugMode() && !config.activationParticleColor.isEmpty()) {
                        getLogger().warning("Invalid color string '" + config.activationParticleColor + "' for particle " + particleName + " in power '" + config.name + "'. Using default.");
                    }
                }
            } else if (dataType.getName().equals("org.bukkit.Particle$DustColorTransitionOptions")) {
                if (isDebugMode()) {
                    getLogger().warning("Particle '" + particleName + "' uses DustColorTransitionOptions which is not supported on this server version. Falling back to default color.");
                }
                Color color = parseColor(config.activationParticleColor.split("->")[0]);
                particleData = new Particle.DustOptions(color != null ? color : Color.RED, 1.0F);

            } else if (dataType == ItemStack.class) {
                try {
                    Material mat = Material.matchMaterial(config.activationParticleColor);
                    if (mat != null && mat.isItem()) {
                        particleData = new ItemStack(mat);
                    } else if(isDebugMode() && !config.activationParticleColor.isEmpty()){
                        getLogger().warning("Invalid item material specified for particle data: " + config.activationParticleColor);
                    }
                } catch (Exception e) {getLogger().warning("Error parsing item material for particle data: " + config.activationParticleColor);}
            } else if (dataType == BlockData.class) {
                try {
                    Material mat = Material.matchMaterial(config.activationParticleColor);
                    if (mat != null && mat.isBlock()) {
                        particleData = mat.createBlockData();
                    } else if(isDebugMode() && !config.activationParticleColor.isEmpty()){
                        getLogger().warning("Invalid block material specified for particle data: " + config.activationParticleColor);
                    }
                } catch (Exception e) {getLogger().warning("Error parsing block material for particle data: " + config.activationParticleColor);}
            }


            world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, particleData, true);

        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid particle type configured: " + particleName + " in power '" + config.name + "'.");
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error spawning particle " + particleName + " for power '" + config.name + "'", ex);
        }
    }

    private Color parseColor(String colorString) {
        if (colorString == null || colorString.isEmpty()) return null;
        String[] rgb = colorString.split(",");
        if (rgb.length == 3) {
            try {
                int r = Integer.parseInt(rgb[0].trim());
                int g = Integer.parseInt(rgb[1].trim());
                int b = Integer.parseInt(rgb[2].trim());
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                return Color.fromRGB(r, g, b);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private BarColor parseBarColor(String colorName, BarColor defaultColor) {
        if (colorName == null || colorName.isEmpty()) return defaultColor;
        try {
            return BarColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid BossBar color specified: '" + colorName + "'. Using default: " + defaultColor.name());
            return defaultColor;
        }
    }

    private boolean isWitherSkullSupported() {
        try {
            EntityType.valueOf("WITHER_SKULL");
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    private void logServerVersion() {
        if (isDebugMode()) {
            getLogger().info("Server Version: " + Bukkit.getVersion());
            getLogger().info("Bukkit Version: " + Bukkit.getBukkitVersion());
        }
    }

    private boolean isDebugMode() {
        return getConfig().getBoolean(CONFIG_DEBUG_MODE, false);
    }

    private PowerConfig getPowerConfigFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return null;

        String displayName = meta.getDisplayName();
        Material type = item.getType();
        int customModelData = meta.hasCustomModelData() ? meta.getCustomModelData() : 0;

        for (PowerConfig config : powerConfigs.values()) {
            if (config.material == type && config.displayName.equals(displayName)) {
                if (config.customModelData == 0 || config.customModelData == customModelData) {
                    return config;
                }
            }
        }
        return null;
    }

    private boolean isPowerItem(ItemStack item) {
        return getPowerConfigFromItem(item) != null;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        switch (cmdName) {
            case "rshelp":
                sendHelpMessage(sender);
                return true;
            case "rsreload":
                return handleReloadCommand(sender);
            case "rsobtain":
                return handleObtainCommand(sender);
            case "rsrebirth":
                return handleRebirthCommand(sender, args);
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        String cmdName = cmd.getName().toLowerCase();
        if (cmdName.equals("rsrebirth")) {
            if (args.length == 1 && sender.hasPermission("regalor.admin.rebirth")) {
                String input = args[0].toLowerCase();
                return powerConfigs.keySet().stream()
                        .filter(name -> name.startsWith(input))
                        .sorted()
                        .collect(Collectors.toList());
            }
        } else if (cmdName.equals("rsreload") || cmdName.equals("rsobtain") || cmdName.equals("rshelp")) {
            return Collections.emptyList();
        }
        return null;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("regalor.admin.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Reloading RegalorElementals configuration...");

        cancelAllTasks();
        clearAllBossBars();
        reloadConfig();

        powerConfigs.clear();
        loadAllPowerConfigs();

        casterMap.clear();
        loadCasterData();

        receivedPowers.clear();
        loadReceivedPlayers();

        resumeCooldownsOnLoad();

        sender.sendMessage(ChatColor.GREEN + "RegalorElementals configuration reloaded successfully!");
        getLogger().info("Configuration reloaded by " + sender.getName());
        return true;
    }

    private boolean handleObtainCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (receivedPowers.containsKey(uuid)) {
            String powerName = receivedPowers.get(uuid);
            PowerConfig config = powerConfigs.get(powerName.toLowerCase());

            if (config == null) {
                player.sendMessage(ChatColor.YELLOW + "Your previously assigned power was invalid. Receiving a new one...");
                clearPlayerPowerState(player);
                giveRandomPower(player);
                PowerData newData = playerPowers.get(uuid);
                if (newData != null) {
                    PowerConfig newConfig = powerConfigs.get(newData.powerName.toLowerCase());
                    if (newConfig != null) {
                        player.sendMessage(ChatColor.GREEN + "You have received the power: " + newConfig.displayName);
                    }
                }

            } else {
                if (!hasPowerItem(player, powerName)) {
                    if(!givePowerItem(player, powerName)) {
                        sendTitleMessage(player,
                                getConfigString(POWER_CONFIG_MSG_INV_FULL_TITLE, "&cInventory Full!"),
                                getConfigString(POWER_CONFIG_MSG_INV_FULL_SUBTITLE,"&eClear space to receive your power."),
                                10, 70, 20);
                    } else {
                        player.sendMessage(ChatColor.GREEN + "You received your power item: " + config.displayName);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You already have your power item!");
                }
            }
        } else {
            giveRandomPower(player);
            PowerData assignedData = playerPowers.get(uuid);
            if (assignedData != null) {
                PowerConfig assignedConfig = powerConfigs.get(assignedData.powerName.toLowerCase());
                if (assignedConfig != null) {
                    player.sendMessage(ChatColor.GREEN + "You have been granted the elemental power: " + assignedConfig.displayName);
                } else {
                    player.sendMessage(ChatColor.RED + "Error: Assigned power configuration is missing!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Error: Could not assign a power. No powers available?");
                getLogger().severe("Failed to assign random power to " + player.getName() + " - check power configs and spawn chances.");
            }
        }
        return true;
    }

    private boolean handleRebirthCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("regalor.admin.rebirth")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /rsrebirth <power_name>");
            player.sendMessage(ChatColor.YELLOW + "Available powers: " + String.join(", ", powerConfigs.keySet()));
            return true;
        }

        String requestedPower = args[0];
        PowerConfig newConfig = powerConfigs.get(requestedPower.toLowerCase());

        if (newConfig == null) {
            player.sendMessage(ChatColor.RED + "Invalid power name: " + requestedPower);
            player.sendMessage(ChatColor.YELLOW + "Available powers: " + String.join(", ", powerConfigs.keySet()));
            return true;
        }

        UUID uuid = player.getUniqueId();
        CasterData currentCasterData = casterMap.get(uuid);
        if (currentCasterData != null && (currentCasterData.isOnCooldown() || currentCasterData.postFlightCooldownTicks > 0)) {
            player.sendMessage(ChatColor.RED + "You cannot rebirth while your current power ability is active or cooling down.");
            return true;
        }

        clearPlayerPowerState(player);

        playerPowers.put(uuid, new PowerData(newConfig.name));
        receivedPowers.put(uuid, newConfig.name);
        saveReceivedPlayers();

        if(!givePowerItem(player, newConfig.name)){
            sendTitleMessage(player,
                    getConfigString(POWER_CONFIG_MSG_INV_FULL_TITLE, "&cInventory Full!"),
                    getConfigString(POWER_CONFIG_MSG_INV_FULL_SUBTITLE,"&eClear space to receive your power."),
                    10, 70, 20);
            player.sendMessage(ChatColor.YELLOW + "(Power assigned, use /rsobtain when inventory has space)");
        }

        player.sendMessage(ChatColor.GREEN + "You have been reborn with the power of " + newConfig.displayName + ChatColor.GREEN + "!");
        getLogger().info("Player " + player.getName() + " used rebirth to change power to " + newConfig.name);
        return true;
    }

    private void clearPlayerPowerState(Player player) {
        UUID uuid = player.getUniqueId();

        PowerData oldData = playerPowers.get(uuid);
        if (oldData != null && oldData.powerName != null) {
            removePowerItem(player, oldData.powerName);
        }

        playerPowers.remove(uuid);
        if (casterMap.containsKey(uuid)) {
            casterMap.remove(uuid);
            File file = new File(casterFolder, uuid + ".yml");
            if (file.exists() && !file.delete()) {
                getLogger().warning("Could not delete old caster file during state clear: " + file.getName());
            }
        }

        clearBossBar(player);
        cancelCooldownTask(uuid);

        if (player.getAllowFlight() && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
    }


    private void giveRandomPower(Player player) {
        List<PowerConfig> availableForSpawn = new ArrayList<>();
        powerConfigs.values().forEach(config -> {
            if (config.spawnChance > 0) {
                for (int i = 0; i < config.spawnChance; i++) {
                    availableForSpawn.add(config);
                }
            }
        });

        if (availableForSpawn.isEmpty()) {
            getLogger().severe("Cannot assign random power: No powers available for spawning (check spawn-chance > 0 in power configs)!");
            player.sendMessage(ChatColor.RED+"Error: No powers available to be assigned.");
            return;
        }

        PowerConfig selected = availableForSpawn.get(ThreadLocalRandom.current().nextInt(availableForSpawn.size()));
        UUID uuid = player.getUniqueId();

        clearPlayerPowerState(player);

        playerPowers.put(uuid, new PowerData(selected.name));
        receivedPowers.put(uuid, selected.name);
        saveReceivedPlayers();

        if (!givePowerItem(player, selected.name)) {
            sendTitleMessage(player,
                    getConfigString(POWER_CONFIG_MSG_INV_FULL_TITLE, "&cInventory Full!"),
                    getConfigString(POWER_CONFIG_MSG_INV_FULL_SUBTITLE,"&eClear space to receive your power."),
                    10, 70, 20);
            player.sendMessage(ChatColor.YELLOW + "(Power assigned, use /rsobtain when inventory has space)");
        }

        if (isDebugMode()) {
            getLogger().info("Assigned random power '" + selected.name + "' to player " + player.getName());
        }
    }

    private boolean hasPowerItem(Player player, String powerName) {
        if (powerName == null || powerName.isEmpty()) return false;
        PowerConfig config = powerConfigs.get(powerName.toLowerCase());
        if (config == null) return false;

        for (ItemStack item : player.getInventory().getContents()) {
            if (isPowerItem(item) && getPowerConfigFromItem(item).name.equalsIgnoreCase(powerName)) {
                return true;
            }
        }
        if (isPowerItem(player.getInventory().getItemInOffHand()) && getPowerConfigFromItem(player.getInventory().getItemInOffHand()).name.equalsIgnoreCase(powerName)) {
            return true;
        }

        return false;
    }

    private boolean givePowerItem(Player player, String powerName) {
        PowerConfig config = powerConfigs.get(powerName.toLowerCase());
        if (config == null) {
            getLogger().warning("Attempted to give item for unknown power: " + powerName);
            return false;
        }

        ItemStack item = config.createItem();
        if (item == null || item.getType() == Material.AIR) {
            getLogger().warning("Power '" + powerName + "' is configured with AIR or invalid material, cannot give item.");
            return false;
        }

        PlayerInventory inventory = player.getInventory();
        int firstEmpty = -1;
        for (int i = 0; i <= 35; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                firstEmpty = i;
                break;
            }
        }

        if (firstEmpty != -1) {
            inventory.setItem(firstEmpty, item);
            return true;
        } else {
            return false;
        }
    }

    private void removePowerItem(Player player, String powerName) {
        PowerConfig config = powerConfigs.get(powerName.toLowerCase());
        if (config == null) return;

        ItemStack powerItemMatcher = config.createItem();
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.isSimilar(powerItemMatcher)) {
                if(getPowerConfigFromItem(item).name.equalsIgnoreCase(powerName)){
                    inventory.setItem(i, null);
                }
            }
        }
        player.updateInventory();

        if (isDebugMode()) {
            getLogger().info("Attempted removal of power item '" + powerName + "' from " + player.getName());
        }
    }


    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e--- &6Regalor Elementals Help &e---"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rshelp &7- Shows this help message."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rsobtain &7- Get your elemental power item."));
        if (sender.hasPermission("regalor.admin.rebirth")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rsrebirth <power_name> &7- Change your power (Admin)."));
        }
        if (sender.hasPermission("regalor.admin.reload")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rsreload &7- Reload the plugin configuration (Admin)."));
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isPowerItem(event.getItemDrop().getItemStack())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop your elemental power item!");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        List<ItemStack> toKeep = new ArrayList<>();
        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while(iterator.hasNext()) {
            ItemStack item = iterator.next();
            if(isPowerItem(item)) {
                toKeep.add(item.clone());
                iterator.remove();
            }
        }
        if (!toKeep.isEmpty()) {
            event.getItemsToKeep().addAll(toKeep);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && receivedPowers.containsKey(uuid)) {
                    String powerName = receivedPowers.get(uuid);
                    PowerConfig config = powerConfigs.get(powerName.toLowerCase());
                    if (config != null && !hasPowerItem(player, powerName)) {
                        if(givePowerItem(player, powerName)){
                            player.sendMessage(ChatColor.GREEN + "Your elemental power item has been returned to you.");
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "Could not return your power item (Inventory Full?). Use /rsobtain later.");
                        }
                    }
                }
            }
        }.runTaskLater(this, 5L);
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (isPowerItem(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (isPowerItem(event.getItem().getItemStack())) {
            Player player = event.getPlayer();
            PowerConfig itemPower = getPowerConfigFromItem(event.getItem().getItemStack());
            PowerData playerData = playerPowers.get(player.getUniqueId());

            boolean isTheirPower = itemPower != null && playerData != null && itemPower.name.equalsIgnoreCase(playerData.powerName);

            if (isTheirPower) {
                if (hasPowerItem(player, itemPower.name)) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }

            if (event.isCancelled()) {
                event.getItem().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlaceAttempt(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasItem() && isPowerItem(event.getItem())) {
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack cursorItem = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInv = event.getClickedInventory();
        Inventory topInv = event.getView().getTopInventory();
        Inventory bottomInv = event.getView().getBottomInventory();

        boolean powerOnCursor = isPowerItem(cursorItem);
        boolean powerInSlot = isPowerItem(currentItem);

        if (!powerOnCursor && !powerInSlot) {
            return;
        }

        if (clickedInv == null) {
            return;
        }

        InventoryAction action = event.getAction();
        int rawSlot = event.getRawSlot();
        int slot = event.getSlot();


        boolean clickedPlayerInventory = clickedInv.getType() == InventoryType.PLAYER;


        if (powerOnCursor) {
            if (!clickedPlayerInventory) {
                event.setResult(Event.Result.DENY);
                event.setCancelled(true);
                sendActionBar(player, "&cPower items cannot be placed here.");
                return;
            }
            if (clickedPlayerInventory) {
                if (slot >= 36 && slot <= 39) {
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                    sendActionBar(player, "&cPower items cannot be placed in armor slots.");
                    return;
                }
            }
        }


        if (powerInSlot) {
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                Inventory targetInv = (topInv.equals(clickedInv)) ? bottomInv : topInv;
                if (!(targetInv instanceof PlayerInventory)) {
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                    sendActionBar(player, "&cPower items cannot leave your inventory.");
                    return;
                }
            }

            if (event.getClick() == ClickType.NUMBER_KEY) {
                int hotbarButton = event.getHotbarButton();
                ItemStack swapTargetItem = player.getInventory().getItem(hotbarButton);
                if (clickedPlayerInventory && slot >= 36 && slot <= 40) {
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                    sendActionBar(player, "&cCannot swap power item here.");
                    return;
                }
            }

            if (clickedPlayerInventory && action == InventoryAction.HOTBAR_SWAP) {
                int hotbarButton = event.getHotbarButton();
                if (hotbarButton >= 0 && hotbarButton <= 8) {
                    ItemStack itemInHotbar = player.getInventory().getItem(hotbarButton);
                    if(isPowerItem(itemInHotbar) && (slot >= 36 && slot <= 39)) {
                        event.setResult(Event.Result.DENY);
                        event.setCancelled(true);
                        sendActionBar(player, "&cCannot swap power item into armor slots.");
                        return;
                    }
                }
            }

        }
    }

    private String getConfigString(String path, String defaultValue) {
        return getConfig().getString(path, defaultValue);
    }


    public static class PowerConfig {
        private final RegalorElementals plugin;
        final String name;
        final YamlConfiguration config;

        final String displayName;
        final List<String> lore;
        final Material material;
        final int customModelData;
        final int spawnChance;
        final int cooldown;
        final int postFlightCooldown;
        final boolean useXpCost;
        final int xpCostLevels;
        final boolean useHealthCost;
        final double healthCost;
        final String bossbarColor;
        final Map<String, String> messages;
        final String activationSound;
        final float soundVolume;
        final float soundPitch;
        final String activationParticle;
        final int activationParticleCount;
        final double particleOffsetX;
        final double particleOffsetY;
        final double particleOffsetZ;
        final double particleSpeed;
        final String activationParticleColor;
        final String abilityType;
        final String effectType;
        final int abilityDuration;
        final int abilityLevel;
        final int abilityRange;
        final String projectileType;
        final double projectileSpeed;
        final double projectileDamage;
        final boolean projectileIgnite;
        final int abilityKnockback;


        public PowerConfig(String name, YamlConfiguration yamlConfig, RegalorElementals plugin) {
            Preconditions.checkNotNull(name, "Power name cannot be null");
            Preconditions.checkNotNull(yamlConfig, "YamlConfiguration cannot be null for power " + name);
            Preconditions.checkNotNull(plugin, "Plugin instance cannot be null");

            this.plugin = plugin;
            this.name = name;
            this.config = yamlConfig;

            this.displayName = ChatColor.translateAlternateColorCodes('&',
                    yamlConfig.getString(POWER_CONFIG_DISPLAY_NAME, "&cUnnamed Power"));

            this.lore = translateLore(yamlConfig.getStringList(POWER_CONFIG_LORE));

            this.material = parseMaterial(yamlConfig.getString(POWER_CONFIG_MATERIAL, "BARRIER"), Material.BARRIER);
            this.customModelData = yamlConfig.getInt(POWER_CONFIG_CUSTOM_MODEL_DATA, 0);

            this.spawnChance = yamlConfig.getInt(POWER_CONFIG_SPAWN_CHANCE, 0);
            this.cooldown = yamlConfig.getInt(POWER_CONFIG_COOLDOWN, 5);
            this.postFlightCooldown = yamlConfig.getInt(POWER_CONFIG_POST_FLIGHT_COOLDOWN, 10);

            this.useXpCost = yamlConfig.getBoolean(POWER_CONFIG_USE_XP_COST, false);
            this.xpCostLevels = yamlConfig.getInt(POWER_CONFIG_XP_COST_LEVELS, 1);
            this.useHealthCost = yamlConfig.getBoolean(POWER_CONFIG_USE_HEALTH_COST, false);
            this.healthCost = yamlConfig.getDouble(POWER_CONFIG_HEALTH_COST, 2.0);

            this.bossbarColor = yamlConfig.getString(POWER_CONFIG_BOSSBAR_COLOR, "RED").toUpperCase();

            this.messages = new HashMap<>();
            ConfigurationSection msgSection = yamlConfig.getConfigurationSection(POWER_CONFIG_MESSAGES);
            if (msgSection != null) {
                for (String key : msgSection.getKeys(false)) {
                    messages.put(key, msgSection.getString(key));
                }
            }

            this.activationSound = yamlConfig.getString(POWER_CONFIG_EFFECTS_SOUND, "ENTITY_PLAYER_LEVELUP");
            this.soundVolume = (float) yamlConfig.getDouble(POWER_CONFIG_EFFECTS_SOUND_VOLUME, 1.0);
            this.soundPitch = (float) yamlConfig.getDouble(POWER_CONFIG_EFFECTS_SOUND_PITCH, 1.0);

            this.activationParticle = yamlConfig.getString(POWER_CONFIG_EFFECTS_PARTICLE, "NONE");
            this.activationParticleCount = yamlConfig.getInt(POWER_CONFIG_EFFECTS_PARTICLE_COUNT, 10);
            this.particleOffsetX = yamlConfig.getDouble(POWER_CONFIG_EFFECTS_PARTICLE_OFFSET_X, 0.1);
            this.particleOffsetY = yamlConfig.getDouble(POWER_CONFIG_EFFECTS_PARTICLE_OFFSET_Y, 0.1);
            this.particleOffsetZ = yamlConfig.getDouble(POWER_CONFIG_EFFECTS_PARTICLE_OFFSET_Z, 0.1);
            this.particleSpeed = yamlConfig.getDouble(POWER_CONFIG_EFFECTS_PARTICLE_SPEED, 0.05);
            this.activationParticleColor = yamlConfig.getString(POWER_CONFIG_EFFECTS_PARTICLE_COLOR, "");

            this.abilityType = yamlConfig.getString(POWER_CONFIG_ABILITY_TYPE, "none").toLowerCase();
            this.effectType = yamlConfig.getString(POWER_CONFIG_ABILITY_EFFECT, "");
            this.abilityDuration = yamlConfig.getInt(POWER_CONFIG_ABILITY_DURATION, 5);
            this.abilityLevel = yamlConfig.getInt(POWER_CONFIG_ABILITY_LEVEL, 1);
            this.abilityRange = yamlConfig.getInt(POWER_CONFIG_ABILITY_RANGE, 50);
            this.projectileType = yamlConfig.getString(POWER_CONFIG_ABILITY_PROJECTILE_TYPE, "");
            this.projectileSpeed = yamlConfig.getDouble(POWER_CONFIG_ABILITY_SPEED, 1.0);
            this.projectileDamage = yamlConfig.getDouble(POWER_CONFIG_ABILITY_DAMAGE, 1.0);
            this.projectileIgnite = yamlConfig.getBoolean(POWER_CONFIG_ABILITY_IGNITE, false);
            this.abilityKnockback = yamlConfig.getInt(POWER_CONFIG_ABILITY_KNOCKBACK, 0);
        }


        public String getMessage(String key, String defaultValue) {
            String msg = messages.get(key);
            return ChatColor.translateAlternateColorCodes('&', (msg != null) ? msg : defaultValue);
        }


        private List<String> translateLore(List<String> originalLore) {
            List<String> translated = new ArrayList<>();
            if (originalLore == null) return translated;
            originalLore.forEach(line -> {
                if (line != null) translated.add(ChatColor.translateAlternateColorCodes('&', line));
            });
            return translated;
        }


        public ItemStack createItem() {
            Material mat = (material == null || material == Material.AIR) ? Material.BARRIER : material;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(this.displayName);
                if (this.lore != null && !this.lore.isEmpty()) {
                    meta.setLore(this.lore);
                }
                if (this.customModelData > 0) {
                    meta.setCustomModelData(this.customModelData);
                }

                item.setItemMeta(meta);
            } else {
                plugin.getLogger().warning("Could not get ItemMeta for material " + mat.name() + " for power '" + name + "'.");
            }
            return item;
        }


        private static Material parseMaterial(String materialName, Material defaultMaterial) {
            if (materialName == null || materialName.isEmpty()) return defaultMaterial;
            try {
                Material mat = Material.matchMaterial(materialName);
                return mat != null ? mat : defaultMaterial;
            } catch (Exception e) {
                Bukkit.getLogger().warning("Invalid material specified: '" + materialName + "'. Using default: " + defaultMaterial.name());
                return defaultMaterial;
            }
        }
    }


    public static class CasterData {
        final String powerName;
        long cooldownEnd;
        int postFlightCooldownTicks;

        public CasterData(String powerName, long cooldownEndTimeMillis, int postFlightTicks) {
            this.powerName = powerName;
            this.cooldownEnd = cooldownEndTimeMillis;
            this.postFlightCooldownTicks = postFlightTicks;
        }

        public boolean isOnCooldown() {
            return System.currentTimeMillis() < cooldownEnd;
        }

        public void startCooldown(int seconds) {
            this.cooldownEnd = (seconds > 0) ? System.currentTimeMillis() + (seconds * 1000L) : 0;
        }

        public long getRemainingCooldownMillis() {
            return isOnCooldown() ? cooldownEnd - System.currentTimeMillis() : 0;
        }

        public int getRemainingCooldownSeconds() {
            return (int) Math.ceil(getRemainingCooldownMillis() / 1000.0);
        }

        public void startPostFlightCooldown(int ticks) {
            this.postFlightCooldownTicks = ticks;
        }

        public boolean needsSaving() {
            return isOnCooldown() || postFlightCooldownTicks > 0;
        }
    }

    public static class PowerData {
        final String powerName;

        public PowerData(String powerName) {
            this.powerName = powerName;
        }
    }
}