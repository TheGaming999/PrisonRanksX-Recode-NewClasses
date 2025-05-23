package me.prisonranksx.lists;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.bukkitutils.NBTEditor;
import me.prisonranksx.bukkitutils.PlayerPagedGUI.GUIItem;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.reflections.UniqueId;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class InfinitePrestigesGUIList extends GUIList implements PrestigesGUIList {

    public InfinitePrestigesGUIList(PrisonRanksX plugin) {
        super(plugin, "Prestiges");
        setup();
    }

    /**
     * Gets limited number of prestiges for a list (4 before current
     * prestige and 4 after) or less if either sides don't have enough prestiges
     * to return. This is to prevent the stress of
     * redundant looping over millions of them
     *
     * @param user to get current prestige from
     * @param max  max for each side (4 normally)
     * @return list of prestiges.
     */
    private List<Long> getPrestigesWithinRange(User user, int max) {
        List<Long> levelRange = new ArrayList<>();
        long currentNumber = user.getPrestige().getNumber();
        long startLevel = currentNumber - max;
        if (startLevel < 1) startLevel = 1;
        long endLevel = startLevel + (max * 2L);
        if (endLevel > PrestigeStorage.getLastPrestigeAsNumber()) endLevel = PrestigeStorage.getLastPrestigeAsNumber();
        for (long level = startLevel; level <= endLevel; level++) levelRange.add(level);
        return levelRange;
    }

    @Override
    public void refreshGUI(Player player) {
        User user = getPlugin().getUserController().getUser(UniqueId.getUUID(player));
        String pathName = user.getPathName();
        long currentPrestigeIndex = PRXAPI.getPlayerPrestigeNumber(player);
        // at least 2 pages of prestiges to list
        getPrestigesWithinRange(user, getPlayerPagedGUI().getSize()).forEach(prestigeIndex -> {
            Prestige prestige = PrestigeStorage.getPrestige(prestigeIndex);
            String prestigeName = prestige.getName();
            if (prestigeIndex < currentPrestigeIndex) {
                GUIItem specialItem = getSpecialCompletedItems().get(prestigeName);
                getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getCompletedItem().clone(),
                        prestigeName, pathName, fun(player, prestige, prestigeName)), player);
            } else if (prestigeIndex == currentPrestigeIndex) {
                GUIItem specialItem = getSpecialCurrentItems().get(prestigeName);
                getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getCurrentItem().clone(),
                        prestigeName, pathName, fun(player, prestige, prestigeName)), player);
            } else {
                GUIItem specialItem = getSpecialOtherItems().get(prestigeName);
                getPlayerPagedGUI().addPagedItem(update(specialItem != null ? specialItem : getOtherItem().clone(),
                        prestigeName, pathName, fun(player, prestige, prestigeName)), player);
            }
        });
    }

    @Override
    public void openGUI(Player player) {
        refreshGUI(player);
        getPlayerPagedGUI().openInventory(player);
    }

    @Override
    public void openGUI(Player player, int page) {
        refreshGUI(player);
        getPlayerPagedGUI().openInventory(player, page);
    }

    protected static Function<String, String> fun(Player player, Prestige prestige, String prestigeName) {
        return str -> str.replace("%prestige%", prestigeName)
                .replace("%prestige_display%", prestige.getDisplayName())
                .replace("%prestige_cost_normal%", String.valueOf(prestige.getCost()))
                .replace("%prestige_cost%", String.valueOf(PRXAPI.getPrestigeFinalCost(prestige, player)))
                .replace("%prestige_cost_comma%", EconomyManager.commaFormat(PRXAPI.getPrestigeFinalCost(prestige, player)))
                .replace("%prestige_cost_comma_decimals%", EconomyManager.commaFormatWithDecimals(PRXAPI.getPrestigeFinalCost(prestige, player)))
                .replace("%prestige_cost_formatted%", EconomyManager.shortcutFormat(PRXAPI.getPrestigeFinalCost(prestige, player)));
    }

    protected GUIItem update(GUIItem guiItem, String prestigeName, String pathName, Function<String, String> function) {
        ItemStack itemStack = guiItem.getItemStack();
        ItemMeta meta = itemStack.getItemMeta();
        String displayName = Objects.requireNonNull(meta).getDisplayName();
        meta.setDisplayName(function.apply(displayName));
        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.clear();
            meta.getLore().forEach(loreLine -> lore.add(function.apply(loreLine)));
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
        itemStack = NBTEditor.set(itemStack, prestigeName, "prx-prestige");
        itemStack = NBTEditor.set(itemStack, pathName, "prx-path");
        guiItem.setItemStack(itemStack);
        return guiItem;
    }

}
