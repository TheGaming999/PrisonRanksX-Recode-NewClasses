package me.prisonranksx.executors;

import com.google.common.collect.Lists;
import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.bukkitutils.ConfigCreator;
import me.prisonranksx.bukkitutils.FireworkColor;
import me.prisonranksx.components.*;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.StorageFields;
import me.prisonranksx.data.UserController;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.ProbabilityCollection;
import me.prisonranksx.utils.Scrif;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class AdminExecutor {

    private PrisonRanksX plugin;

    public AdminExecutor(PrisonRanksX plugin) {
        this.plugin = plugin;
    }

    private String getField(ConfigurationSection section, String... fields) {
        return section == null ? fields[0] : ConfigManager.getPossibleField(section, fields);
    }

    private ConfigurationSection getMainSection() {
        return ConfigManager.getRanksConfig().getConfigurationSection("Ranks");
    }

    public UserController userControl() {
        return plugin.getUserController();
    }

    public User getUser(UUID uniqueId) {
        return userControl().getUser(uniqueId);
    }

    public boolean setPlayerRank(UUID uniqueId, String rankName, String pathName) {
        User user = getUser(uniqueId);
        if (user == null) {
            PrisonRanksX.logWarning("Failed to change '" + uniqueId.toString() + "' rank to '" + rankName + "'.");
            PrisonRanksX.logWarning("No user data found for: " + uniqueId);
            return false;
        }
        if (RankStorage.getRank(rankName, pathName) == null) {
            PrisonRanksX.logWarning("Unable to find a rank named '" + rankName + "'.");
            return false;
        }
        user.setRankName(rankName);
        user.setPathName(pathName);
        plugin.getRankupExecutor().updateGroup(UniqueId.getPlayer(uniqueId));
        return true;
    }

    public boolean setPlayerRank(UUID uniqueId, String rankName) {
        String pathName = getUser(uniqueId).getPathName();
        return setPlayerRank(uniqueId, rankName,
                pathName == null || !RankStorage.pathExists(pathName) ? RankStorage.getDefaultPath() : pathName);
    }

    public boolean setPlayerPrestige(UUID uniqueId, String prestigeName) {
        User user = getUser(uniqueId);
        if (user == null) {
            PrisonRanksX
                    .logWarning("Failed to change '" + uniqueId.toString() + "' prestige to '" + prestigeName + "'.");
            PrisonRanksX.logWarning("No user data found for: " + uniqueId);
            return false;
        }
        if (!PrestigeStorage.prestigeExists(prestigeName)) {
            PrisonRanksX.logWarning("Unable to find a prestige named '" + prestigeName + "'.");
            return false;
        }
        user.setPrestigeName(prestigeName);
        return true;
    }

    public void createRank(String name, double cost, String pathName, String displayName) {
        pathName = pathName == null ? RankStorage.getDefaultPath() : pathName;
        ConfigurationSection pathSection = getMainSection().getConfigurationSection(pathName);
        String lastRankName = RankStorage.getLastRankName(pathName);
        // If it's a new path, then create it
        if (pathSection == null) pathSection = getMainSection().createSection(pathName);
        ConfigurationSection lastRankSection = lastRankName == null ? null
                : pathSection.getConfigurationSection(lastRankName);
        // If the path has at least one rank, then we should change the next rank to the
        // rank we're going to create
        if (lastRankName != null) ConfigManager.setPossible(lastRankSection, StorageFields.NEXT_FIELDS, name);
        ConfigurationSection newRankSection = pathSection.createSection(name);
        newRankSection.set(getField(lastRankSection, StorageFields.COST_FIELDS), cost);
        newRankSection.set(getField(lastRankSection, StorageFields.NEXT_FIELDS), "LASTRANK");
        newRankSection.set(getField(lastRankSection, StorageFields.DISPLAY_FIELDS), displayName);
        ConfigManager.saveConfig("ranks.yml");
        RankStorage.loadRanks();
    }

    public void setRankDisplayName(String name, String pathName, String displayName) {
        ConfigurationSection rankSection = getMainSection().getConfigurationSection(pathName)
                .getConfigurationSection(name);
        if (rankSection == null) return;
        ConfigManager.setPossible(rankSection, StorageFields.DISPLAY_FIELDS, displayName);
        ConfigManager.saveConfig("ranks.yml");
        RankStorage.loadRanks();
    }

    public void setRankCost(String name, String pathName, double cost) {
        ConfigurationSection rankSection = getMainSection().getConfigurationSection(pathName)
                .getConfigurationSection(name);
        if (rankSection == null) return;
        ConfigManager.setPossible(rankSection, StorageFields.COST_FIELDS, cost);
        ConfigManager.saveConfig("ranks.yml");
        RankStorage.loadRanks();
    }

    private Map<String, Object> deleteTemporarily(String name, String oldPathName) {
        ConfigurationSection oldPathSection = getMainSection().getConfigurationSection(oldPathName);
        if (oldPathSection == null) return null;
        ConfigurationSection rankSection = oldPathSection.getConfigurationSection(name);
        if (rankSection == null) return null;
        Map<String, Object> savedRankSection = new LinkedHashMap<>(rankSection.getValues(true));
        List<String> rankNames = Lists.newArrayList(oldPathSection.getValues(false).keySet());
        int specifiedRankIndex = rankNames.indexOf(name);
        String toMoveRankName = rankNames.get(specifiedRankIndex);
        // Account for different scenarios
        if (specifiedRankIndex > 0 && specifiedRankIndex != rankNames.size() - 1) {
            int previousRankIndex = specifiedRankIndex - 1;
            String previousRankName = rankNames.get(previousRankIndex);
            ConfigurationSection previousRankSection = oldPathSection.getConfigurationSection(previousRankName);
            if (rankNames.size() > 2) {
                int nextRankIndex = specifiedRankIndex + 1;
                String nextRankName = rankNames.get(nextRankIndex);
                ConfigurationSection nextRankSection = oldPathSection.getConfigurationSection(nextRankName);
                ConfigManager.setPossible(previousRankSection, StorageFields.NEXT_FIELDS, nextRankName);
                if (rankNames.size() == 3)
                    ConfigManager.setPossible(nextRankSection, StorageFields.NEXT_FIELDS, "LASTRANK");
            } else if (rankNames.size() == 2) {
                ConfigManager.setPossible(previousRankSection, StorageFields.NEXT_FIELDS, "LASTRANK");
            }
        } else if (specifiedRankIndex == rankNames.size() - 1) {
            if (rankNames.size() > 1) {
                int previousRankIndex = specifiedRankIndex - 1;
                String previousRankName = rankNames.get(previousRankIndex);
                ConfigurationSection previousRankSection = oldPathSection.getConfigurationSection(previousRankName);
                ConfigManager.setPossible(previousRankSection, StorageFields.NEXT_FIELDS, "LASTRANK");
            }
        }
        oldPathSection.set(toMoveRankName, null);
        if (oldPathSection.getValues(false).size() == 0) getMainSection().set(oldPathName, null);
        return savedRankSection;
    }

    public void deleteRank(String name, String pathName) {
        deleteTemporarily(name, pathName);
        ConfigManager.saveConfig("ranks.yml");
        RankStorage.loadRanks();
    }

    public void moveRankPath(String name, String oldPathName, String newPathName) {
        Map<String, Object> oldRankValues = deleteTemporarily(name, oldPathName);
        ConfigurationSection newPathSection = getMainSection().getConfigurationSection(newPathName);
        String lastRankName = RankStorage.getLastRankName(newPathName);
        // If it's a new path, then create it
        if (newPathSection == null) newPathSection = getMainSection().createSection(newPathName);
        ConfigurationSection lastRankSection = lastRankName == null ? null
                : newPathSection.getConfigurationSection(lastRankName);
        // If the path has at least one rank, then we should change the next rank to the
        // rank we're going to create
        if (lastRankName != null) ConfigManager.setPossible(lastRankSection, StorageFields.NEXT_FIELDS, name);
        ConfigurationSection newRankSection = newPathSection.createSection(name);
        oldRankValues.entrySet().forEach(entry -> newRankSection.set(entry.getKey(), entry.getValue()));
        newRankSection.set(getField(lastRankSection, StorageFields.NEXT_FIELDS), "LASTRANK");
        ConfigManager.saveConfig("ranks.yml");
        RankStorage.loadRanks();
    }

    public void copyRank(String name, String pathName, String name2) {
        ConfigurationSection rankSection = getMainSection().getConfigurationSection(pathName)
                .getConfigurationSection(name);
        if (rankSection == null) return;
        ConfigurationSection rank2Section = getMainSection().getConfigurationSection(pathName)
                .getConfigurationSection(name2);
        ConfigManager.setPossible(rank2Section, StorageFields.COMMANDS_FIELDS,
                ConfigManager.getPossibleList(rankSection, String.class, StorageFields.COMMANDS_FIELDS));
        ConfigManager.setPossible(rank2Section, StorageFields.ADD_PERMISSIONS_FIELDS,
                ConfigManager.getPossibleList(rankSection, String.class, StorageFields.ADD_PERMISSIONS_FIELDS));
        ConfigManager.setPossible(rank2Section, StorageFields.DEL_PERMISSIONS_FIELDS,
                ConfigManager.getPossibleList(rankSection, String.class, StorageFields.DEL_PERMISSIONS_FIELDS));
        ConfigManager.setPossible(rank2Section, StorageFields.MESSAGE_FIELDS,
                ConfigManager.getPossibleList(rankSection, String.class, StorageFields.MESSAGE_FIELDS));
        ConfigManager.setPossible(rank2Section, StorageFields.FIREWORK_FIELDS,
                ConfigManager.getPossible(rankSection, StorageFields.FIREWORK_FIELDS));
        ConfigManager.setPossible(rank2Section, StorageFields.RANDOM_COMMANDS_FIELDS,
                ConfigManager.getPossible(rankSection, StorageFields.RANDOM_COMMANDS_FIELDS));
        ConfigManager.setPossible(rank2Section, StorageFields.ACTION_BAR_FIELDS,
                ConfigManager.getPossible(rankSection, StorageFields.ACTION_BAR_FIELDS));
        ConfigManager.setPossible(rank2Section, StorageFields.REQUIREMENTS_FIELDS,
                ConfigManager.getPossibleList(rankSection, String.class, StorageFields.REQUIREMENTS_FIELDS));
        ConfigManager.setPossible(rank2Section, StorageFields.REQUIREMENTS_FAIL_MESSAGE_FIELDS,
                ConfigManager.getPossibleList(rankSection, String.class, StorageFields.REQUIREMENTS_FAIL_MESSAGE_FIELDS));
        ConfigManager.saveConfig("ranks.yml");
        RankStorage.loadRanks();
    }

    public void displayRankInfo(CommandSender sender, Rank rank) {
        sendMsg(sender, "-- RANK INFO --");
        long playersCount = Bukkit.getOnlinePlayers().stream().filter(player -> {
            User user = plugin.getUserController().getUser(UniqueId.getUUID(player));
            return Objects.equals(user.getRankName(), rank.getName());
        }).count();
        sendMsg(sender, "&7Amount of Online Players With This Rank: &f" + playersCount);
        sendMsg(sender, "&7Index: &f" + rank.getIndex());
        sendMsg(sender, "&7Name: &f" + rank.getName());
        sendMsg(sender, "&7Path: &f" + RankStorage.findFirstPath(rank.getName()));
        sendMsg(sender, "&7Cost: &f" + rank.getCost() +
                (sender instanceof Player ? " | Your Increased Cost: " + PRXAPI.getRankFinalCost(rank, (Player) sender) : ""));
        sendMsg(sender, "&7Display: &f" + rank.getDisplayName());
        sendMsg(sender, "&7Next Rank: &f" + rank.getNextName());
        sendMsg(sender, "&7Is Allow Prestige: &f" + (rank.isAllowPrestige() || rank.getNextName() == null));

        List<String> broadcastMsgs = rank.getBroadcastMessages();
        sendMsg(sender,
                broadcastMsgs == null ? "&7&mBroadcast Messages:&f none" : "&7Broadcast Messages:");
        if (broadcastMsgs != null) broadcastMsgs.forEach(msg -> sendMsg(sender, "&r" + msg));

        List<String> messages = rank.getMessages();
        sendMsg(sender, messages == null ? "&7&mMessages:&f none" : "&7Messages:");
        if (messages != null) messages.forEach(msg -> sendMsg(sender, "&r" + msg));

        CommandsComponent commandsComponent = rank.getCommandsComponent();
        sendMsg(sender, commandsComponent == null ? "&7&mCommands:&f none" : "&7Commands:");
        if (commandsComponent != null) {
            List<String> console = commandsComponent.getConsoleCommands();
            if (console != null)
                console.forEach(command -> sendMsg(sender, "&f[console] &a" + command));
            List<String> player = commandsComponent.getPlayerCommands();
            if (player != null) player.forEach(command -> sendMsg(sender, "&f[player] &e" + command));
        }

        ActionBarComponent actionBarComponent = rank.getActionBarComponent();
        sendMsg(sender, actionBarComponent == null ? "&7&mAction Bar:&f none" : "&7Action Bar:");
        if (actionBarComponent != null) {
            sendMsg(sender, "&r Interval: &r" + actionBarComponent.getActionBarSender().getInterval());
            actionBarComponent.getActionBarSender().forEachMessage(m -> sendMsg(sender, "&r" + m));
        }

        PermissionsComponent permissionsComponent = rank.getPermissionsComponent();
        sendMsg(sender, permissionsComponent == null ? "&7&mPermissions:&f none" : "&7Permissions:");
        if (permissionsComponent != null) {
            Set<String> add = permissionsComponent.getAddPermissionCollection();
            Set<String> del = permissionsComponent.getDelPermissionCollection();
            if (add != null) add.forEach(permission -> sendMsg(sender, "&7+ &a" + permission));
            if (del != null) del.forEach(permission -> sendMsg(sender, "&7- &c" + permission));
        }
        RandomCommandsComponent randomCommandsComponent = rank.getRandomCommandsComponent();
        sendMsg(sender,
                randomCommandsComponent == null ? "&7&mRandom Commands:&f none" : "&7Random Commands:");
        if (randomCommandsComponent != null) {
            NavigableSet<ProbabilityCollection.ProbabilitySetElement<List<String>>> collection = randomCommandsComponent
                    .getCollection();
            if (collection != null) {
                collection.forEach(probabilitySetElement -> {
                    sendMsg(sender, " &rChance: " + probabilitySetElement.getProbability() + " %");
                    sendMsg(sender, " &rCommands: " + probabilitySetElement.getObject());
                });
            }
        }
        RequirementsComponent requirementsComponent = rank.getRequirementsComponent();
        sendMsg(sender, requirementsComponent == null ? "&7&mRequirements:&f none" : "&7Requirements:");
        if (requirementsComponent != null) {
            Map<String, String> equalRequirements = requirementsComponent.getEqualRequirements();
            if (equalRequirements != null)
                equalRequirements.forEach((k, v) -> sendMsg(sender, "&fEqual: " + k + "&a->&f" + v));
            Map<String, String> notEqualRequirements = requirementsComponent.getNotEqualRequirements();
            if (notEqualRequirements != null) notEqualRequirements
                    .forEach((k, v) -> sendMsg(sender, "&fNot Equal: " + k + "&c<-&f" + v));
            Map<String, Double> greaterThanRequirements = requirementsComponent
                    .getGreaterThanRequirements();
            if (greaterThanRequirements != null) greaterThanRequirements
                    .forEach((k, v) -> sendMsg(sender, "&fGreater Than: " + k + "&a>>&f" + v));
            Map<String, Double> lessThanRequirements = requirementsComponent.getLessThanRequirements();
            if (lessThanRequirements != null) lessThanRequirements
                    .forEach((k, v) -> sendMsg(sender, "&fLess Than: " + k + "&c<<&f" + v));
            Map<Scrif, List<String>> scriptRequirements = requirementsComponent.getScriptRequirements();
            if (scriptRequirements != null) scriptRequirements.forEach(
                    (k, v) -> sendMsg(sender, "&fScript: " + k.getScript() + " Placeholders: " + v));

        }
        List<String> requirementsMessages = rank.getRequirementsMessages();
        sendMsg(sender, requirementsMessages == null ? "&7&mRequirements Messages:&f none"
                : "&7Requirements Messages:");
        if (requirementsMessages != null) {
            requirementsMessages.forEach(msg -> sendMsg(sender, "&r" + msg));
            Messages.sendMessages(sender, requirementsMessages,
                    l -> RequirementsComponent.updateMsg(l, requirementsComponent));
        }
        FireworkComponent fireworkComponent = rank.getFireworkComponent();
        sendMsg(sender, fireworkComponent == null ? "&7&mFirework:&f none" : "&7Firework:");

        if (fireworkComponent != null) {
            sendMsg(sender, " Power: " + fireworkComponent.getPower());
            fireworkComponent.getFireworkEffects().forEach(effect -> {
                sendMsg(sender, " Type: " + effect.getType().name());
                sendMsg(sender, " Colors: " + FireworkColor.stringify(effect.getColors()));
                sendMsg(sender, " FadeColors: " + FireworkColor.stringify(effect.getFadeColors()));
                sendMsg(sender, " Flicker: " + effect.hasFlicker());
                sendMsg(sender, " Trail: " + effect.hasTrail());
            });
        }
    }

    private void sendMsg(CommandSender sender, String s) {
        sender.sendMessage(StringManager.parseColors(s));
    }

    public void reload() {
        ConfigCreator.reloadConfigs("config.yml", "guis.yml", "infinite_prestige.yml", "messages.yml", "prestiges.yml",
                "rebirths.yml", "ranks.yml");
        plugin.initGlobalSettings();

        if (plugin.getGlobalSettings().isRankEnabled()) {
            // plugin.getRankSettings().setup();
            // RankStorage.loadRanks();
            plugin.prepareRanks();
        }
        if (plugin.getGlobalSettings().isPrestigeEnabled()) {
            // plugin.getPrestigeSettings().setup();
            // PrestigeStorage.loadPrestiges();
            plugin.preparePrestiges();
        }
        if (plugin.getGlobalSettings().isRebirthEnabled()) {
            // plugin.getRebirthSettings().setup();
            // RebirthStorage.loadRebirths();
            plugin.prepareRebirths();
        }
        if (plugin.getGlobalSettings().isHologramsPlugin() && (plugin.getHologramSettings().isHologramsEnabled()))
            plugin.getHologramSettings().setup();
        if (plugin.getGlobalSettings().isPlaceholderAPILoaded()) plugin.getPlaceholderAPISettings().setup();
        if (plugin.getGlobalSettings().isGuiRankList()) plugin.initRanksGUIList();
        Messages.reload();
    }

}
