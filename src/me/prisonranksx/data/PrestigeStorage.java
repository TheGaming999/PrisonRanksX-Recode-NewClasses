package me.prisonranksx.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.components.ActionBarComponent;
import me.prisonranksx.components.CommandsComponent;
import me.prisonranksx.components.ComponentsHolder;
import me.prisonranksx.components.FireworkComponent;
import me.prisonranksx.components.PermissionsComponent;
import me.prisonranksx.components.RandomCommandsComponent;
import me.prisonranksx.components.RequirementsComponent;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.UniversalPrestige;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.utils.HashedLongRange;
import me.prisonranksx.utils.IntParser;
import me.prisonranksx.utils.ModuloLongRange;

public class PrestigeStorage {

	private static final PrestigeStorageHandler PRESTIGE_STORAGE_HANDLER = new PrestigeStorageHandler();

	public static PrestigeStorageHandler getHandler() {
		return PRESTIGE_STORAGE_HANDLER;
	}

	public static void loadPrestiges() {
		PRESTIGE_STORAGE_HANDLER.loadPrestiges();
	}

	public static PrestigeStorageHandler init(boolean infinite) {
		PRESTIGE_STORAGE_HANDLER.create(infinite);
		return PRESTIGE_STORAGE_HANDLER;
	}

	public static PrestigeStorageHandler initAndLoad(boolean infinite) {
		init(infinite).loadPrestiges();
		return PRESTIGE_STORAGE_HANDLER;
	}

	public static boolean isCreated() {
		return PRESTIGE_STORAGE_HANDLER.isCreated();
	}

	public static boolean prestigeExists(String name) {
		return PRESTIGE_STORAGE_HANDLER.prestigeExists(name);
	}

	public static boolean prestigeExists(long number) {
		return PRESTIGE_STORAGE_HANDLER.prestigeExists(number);
	}

	public static Prestige getPrestige(String name) {
		return PRESTIGE_STORAGE_HANDLER.getPrestige(name);
	}

	public static Prestige getPrestige(long number) {
		return PRESTIGE_STORAGE_HANDLER.getPrestige(number);
	}

	public static String getFirstPrestigeName() {
		return PRESTIGE_STORAGE_HANDLER.getFirstPrestigeName();
	}

	public static long getFirstPrestigeAsNumber() {
		return PRESTIGE_STORAGE_HANDLER.getFirstPrestigeAsNumber();
	}

	public static String getLastPrestigeName() {
		return PRESTIGE_STORAGE_HANDLER.getLastPrestigeName();
	}

	public static long getLastPrestigeAsNumber() {
		return PRESTIGE_STORAGE_HANDLER.getLastPrestigeAsNumber();
	}

	/**
	 * 
	 * @return a hash set of all registered prestiges names, or null if
	 *         infinite prestige is enabled
	 */
	public static Set<String> getPrestigeNames() {
		return PRESTIGE_STORAGE_HANDLER.getPrestigeNames();
	}

	/**
	 * 
	 * @return a hash set of all registered prestiges, or null if infinite
	 *         prestige is enabled
	 */
	public static Collection<Prestige> getPrestiges() {
		return PRESTIGE_STORAGE_HANDLER.getPrestiges();
	}

	public static String matchPrestigeName(String name) {
		return PRESTIGE_STORAGE_HANDLER.matchPrestigeName(name);
	}

	public static Prestige matchPrestige(String name) {
		return PRESTIGE_STORAGE_HANDLER.matchPrestige(name);
	}

	public static String getRangedDisplay(long prestige) {
		return PRESTIGE_STORAGE_HANDLER.getRangedDisplay(prestige);
	}

	public static void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction) {
		PRESTIGE_STORAGE_HANDLER.useContinuousComponents(prestige, componentsAction);
	}

	public static CommandsComponent getCommandsComponent() {
		return PRESTIGE_STORAGE_HANDLER.getCommandsComponent();
	}

	public static void useCommandsComponent(Consumer<CommandsComponent> action) {
		PRESTIGE_STORAGE_HANDLER.useCommandsComponent(action);
	}

	public static String getCostExpression() {
		return PRESTIGE_STORAGE_HANDLER.getCostExpression();
	}

	public static class PrestigeStorageHandler {

		private IPrestigeStorage prestigeStorage;

		public void create(boolean infinite) {
			prestigeStorage = infinite ? new InfinitePrestigeStorage() : new RegularPrestigeStorage();
		}

		public boolean isCreated() {
			return prestigeStorage != null;
		}

		public void loadPrestiges() {
			prestigeStorage.loadPrestiges();
		}

		public IPrestigeStorage getStorage() {
			return prestigeStorage;
		}

		public boolean prestigeExists(String name) {
			return prestigeStorage.prestigeExists(name);
		}

		public boolean prestigeExists(long number) {
			return prestigeStorage.prestigeExists(number);
		}

		public Prestige getPrestige(String name) {
			return prestigeStorage.getPrestige(name);
		}

		public Prestige getPrestige(long number) {
			return prestigeStorage.getPrestige(number);
		}

		public String getFirstPrestigeName() {
			return prestigeStorage.getFirstPrestigeName();
		}

		public long getFirstPrestigeAsNumber() {
			return prestigeStorage.getFirstPrestigeAsNumber();
		}

		public String getLastPrestigeName() {
			return prestigeStorage.getLastPrestigeName();
		}

		public long getLastPrestigeAsNumber() {
			return prestigeStorage.getLastPrestigeAsNumber();
		}

		public Set<String> getPrestigeNames() {
			return prestigeStorage.getPrestigeNames();
		}

		public Collection<Prestige> getPrestiges() {
			return prestigeStorage.getPrestiges();
		}

		public String matchPrestigeName(String name) {
			return prestigeStorage.matchPrestigeName(name);
		}

		public Prestige matchPrestige(String name) {
			return prestigeStorage.matchPrestige(name);
		}

		public boolean isInfinite() {
			return prestigeStorage.isInfinite();
		}

		public long getPrestigeNumber(String name) {
			return prestigeStorage.getPrestigeNumber(name);
		}

		public String getRangedDisplay(long prestige) {
			return prestigeStorage.getRangedDisplay(prestige);
		}

		public void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction) {
			prestigeStorage.useContinuousComponents(prestige, componentsAction);
		}

		public CommandsComponent getCommandsComponent() {
			return prestigeStorage.getCommandsComponent();
		}

		public void useCommandsComponent(Consumer<CommandsComponent> action) {
			prestigeStorage.useCommandsComponent(action);
		}

		public String getCostExpression() {
			return prestigeStorage.getCostExpression();
		}

	}

	private static interface IPrestigeStorage {

		void loadPrestiges();

		boolean prestigeExists(String name);

		boolean prestigeExists(long number);

		Prestige getPrestige(String name);

		Prestige getPrestige(long number);

		String getFirstPrestigeName();

		long getFirstPrestigeAsNumber();

		String getLastPrestigeName();

		long getLastPrestigeAsNumber();

		Set<String> getPrestigeNames();

		Collection<Prestige> getPrestiges();

		String matchPrestigeName(String name);

		Prestige matchPrestige(String name);

		boolean isInfinite();

		long getPrestigeNumber(String name);

		void useCommandsComponent(Consumer<CommandsComponent> action);

		public String getRangedDisplay(long prestige);

		public void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction);

		public CommandsComponent getCommandsComponent();

		public String getCostExpression();

	}

	private static class RegularPrestigeStorage implements IPrestigeStorage {

		private Map<String, Prestige> prestiges = new HashMap<>();
		private Map<String, String> alternativeNames = new HashMap<>();
		private List<String> prestigeNames = new ArrayList<>();
		private String firstPrestigeName;
		private String lastPrestigeName;
		private long lastPrestigeNumber;
		private CommandsComponent prestigeCommands;

		@SuppressWarnings("unchecked")
		@Override
		public void loadPrestiges() {
			prestiges.clear();
			alternativeNames.clear();
			prestigeNames.clear();
			firstPrestigeName = null;
			lastPrestigeName = null;
			lastPrestigeNumber = 0;
			FileConfiguration prestigesConfig = ConfigManager.getPrestigesConfig();
			ConfigurationSection prestigeSection = prestigesConfig.getConfigurationSection("Prestiges");
			for (String prestigeName : prestigeSection.getKeys(false)) {
				ConfigurationSection current = prestigeSection.getConfigurationSection(prestigeName);
				Prestige prestige = new Prestige(prestigeName,
						StringManager.parseColorsAndSymbols(
								ConfigManager.getOrElse(current, String.class, "display-name", "display", "prefix")),
						ConfigManager.getOrElse(current, "next-prestige", "nextprestige"),
						ConfigManager.getDoubleOrElse(current, "cost", "price"),
						StringManager.parseColorsAndSymbols(prestigeSection.getStringList("broadcast")),
						StringManager.parseColorsAndSymbols(
								ConfigManager.getOrElse(current, List.class, "message", "msg", "messages")),
						CommandsComponent.parseCommands(
								ConfigManager.getOrElse(current, "commands", "executecmds", "command", "cmd")),
						RequirementsComponent.parseRequirements(
								ConfigManager.getOrElse(current, "requirements", "requirement", "require", "requires")),
						ActionBarComponent.parseActionBar(ConfigManager.getOrElse(current, "action-bar", "actionbar")),
						PermissionsComponent.parsePermissions(
								ConfigManager.getOrElse(current, "add-permissions", "addpermission", "add-permission",
										"addperm", "add-perm", "add-perms"),
								ConfigManager.getOrElse(current, "delete-permissions", "delpermission",
										"del-permission", "delete-permission", "remove-permissions",
										"remove-permission", "del-perms")),
						FireworkComponent.parseFirework(ConfigManager.getOrElse(current, "firework", "firework-builder",
								"fireworks", "fire-work")),
						RandomCommandsComponent.parseRandomCommands(ConfigManager.getOrElse(current, "random-commands",
								"randomcmds", "random-command", "randomcmd", "random-cmds", "random-cmd")),
						StringManager.parseColorsAndSymbols(ConfigManager.getOrElse(current, List.class,
								"requirements-fail-message", "custom-requirement-message",
								"custom-requirements-message", "requirement-fail-message", "requirements-fail-messages",
								"requirements-message", "requirement-message")),
						ConfigManager.getDoubleOrElse(current, "cost-increase", "rankup_cost_increase_percentage",
								"cost-increase-percentage", "cost_increase", "rankup-cost-increase-percentage"));
				lastPrestigeNumber += 1;
				prestiges.put(prestigeName, prestige);
				alternativeNames.put(prestigeName.toLowerCase(), prestigeName);
				prestigeNames.add(prestigeName);
				if (firstPrestigeName == null) firstPrestigeName = prestigeName;
			}
			lastPrestigeName = prestigeNames.get((int) (lastPrestigeNumber - 1));
			prestigeCommands = PrisonRanksX.getInstance().getPrestigeSettings().getPrestigeCommands();
		}

		@Override
		public boolean prestigeExists(String name) {
			return getPrestige(name) != null;
		}

		@Override
		public boolean prestigeExists(long number) {
			return number > 0 && number <= lastPrestigeNumber;
		}

		@Override
		public Prestige getPrestige(String name) {
			return prestiges.get(name);
		}

		@Override
		public Prestige getPrestige(long number) {
			return getPrestige(prestigeNames.get((int) (number - 1)));
		}

		@Override
		public String getFirstPrestigeName() {
			return firstPrestigeName;
		}

		@Override
		public long getFirstPrestigeAsNumber() {
			return prestigeNames.indexOf(firstPrestigeName) + 1;
		}

		@Override
		public String getLastPrestigeName() {
			return lastPrestigeName;
		}

		@Override
		public long getLastPrestigeAsNumber() {
			return lastPrestigeNumber;
		}

		@Override
		public Set<String> getPrestigeNames() {
			return prestiges.keySet();
		}

		@Override
		public Collection<Prestige> getPrestiges() {
			return prestiges.values();
		}

		@Override
		public String matchPrestigeName(String name) {
			String altName = alternativeNames.get(name.toLowerCase());
			if (altName != null) return altName;
			int intName = IntParser.asInt(name, -1);
			if (intName == -1) return null;
			return prestigeNames.get(intName + 1);
		}

		@Override
		public Prestige matchPrestige(String name) {
			return prestiges.get(matchPrestigeName(name));
		}

		@Override
		public boolean isInfinite() {
			return false;
		}

		@Override
		public long getPrestigeNumber(String name) {
			return prestiges.get(name).getNumber();
		}

		@Override
		public String getRangedDisplay(long prestige) {
			return getPrestige(prestige).getDisplayName();
		}

		@Override
		public void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction) {
			// Does nothing...
		}

		@Override
		public CommandsComponent getCommandsComponent() {
			return prestigeCommands;
		}

		@Override
		public void useCommandsComponent(Consumer<CommandsComponent> action) {
			if (prestigeCommands == null) return;
			action.accept(prestigeCommands);
		}

		@Override
		public String getCostExpression() {
			return null;
		}

	}

	public static class InfinitePrestigeStorage implements IPrestigeStorage {

		/**
		 * For infinite prestige, we don't store all prestiges, instead we use one
		 * universal prestige object for prestiges that don't have unique settings.
		 */
		private Map<Long, Prestige> prestiges = new HashMap<>();
		private final String firstPrestigeName = "1";
		private final long firstPrestigeNumber = 1;
		private String lastPrestigeName;
		private long lastPrestigeNumber;
		private Prestige universalPrestige;
		private Map<HashedLongRange, String> constantSettings = new HashMap<>();
		private Map<ModuloLongRange, ComponentsHolder> continuousSettings = new HashMap<>();
		private CommandsComponent maxPrestigeCommands;
		private String costExpression;

		@SuppressWarnings("unchecked")
		@Override
		public void loadPrestiges() {
			prestiges.clear();
			constantSettings.clear();
			continuousSettings.clear();
			lastPrestigeName = null;
			lastPrestigeNumber = 0;
			FileConfiguration infinitePrestigeConfig = ConfigManager.getInfinitePrestigeConfig();
			ConfigurationSection prestigeSection = infinitePrestigeConfig.getConfigurationSection("Prestiges-Settings");
			ConfigurationSection globalSection = infinitePrestigeConfig.getConfigurationSection("Global-Settings");
			ConfigurationSection constantSection = infinitePrestigeConfig
					.getConfigurationSection("Constant-Prestiges-Settings");
			ConfigurationSection continuousSection = infinitePrestigeConfig
					.getConfigurationSection("Continuous-Prestiges-Settings");
			for (String prestigeName : prestigeSection.getKeys(false)) {
				ConfigurationSection current = prestigeSection.getConfigurationSection(prestigeName);
				Prestige prestige = new UniversalPrestige(prestigeName,
						StringManager.parseColorsAndSymbols(
								ConfigManager.getOrElse(globalSection, String.class, StorageFields.DISPLAY_FIELDS)),
						null, 0.0, StringManager.parseColorsAndSymbols(current.getStringList("broadcast")),
						StringManager.parseColorsAndSymbols(
								ConfigManager.getOrElse(current, List.class, StorageFields.MESSAGE_FIELDS)),
						CommandsComponent
								.parseCommands(ConfigManager.getOrElse(current, StorageFields.COMMANDS_FIELDS)),
						null, null, null, null, null, null, 0.0);
				prestiges.put(Long.parseLong(prestigeName), prestige);
			}
			for (String prestigeName : constantSection.getKeys(false)) {
				long maxRange = ConfigManager.getLongOrElse(constantSection.getConfigurationSection(prestigeName),
						StorageFields.NEXT_FIELDS);
				long minRange = IntParser.readLong(prestigeName);
				String display = StringManager.parseColorsAndSymbols(
						ConfigManager.getOrElse(constantSection.getConfigurationSection(prestigeName), String.class,
								StorageFields.DISPLAY_FIELDS));
				constantSettings.put(HashedLongRange.newRange(minRange, maxRange), display);
			}
			for (String prestigeName : continuousSection.getKeys(false)) {
				ConfigurationSection rangeSection = continuousSection.getConfigurationSection(prestigeName);
				ModuloLongRange range = ModuloLongRange.newRange(Long.parseLong(prestigeName));
				ComponentsHolder componentsHolder = ComponentsHolder.hold()
						.commands(CommandsComponent
								.parseCommands(ConfigManager.getOrElse(rangeSection, StorageFields.COMMANDS_FIELDS)))
						.messages(StringManager.parseColorsAndSymbols(
								ConfigManager.getOrElse(rangeSection, List.class, StorageFields.MESSAGE_FIELDS)))
						.broadcastMessages(StringManager
								.parseColorsAndSymbols(ConfigManager.getOrElse(rangeSection, List.class, "broadcast")));
				continuousSettings.put(range, componentsHolder);
			}
			maxPrestigeCommands = CommandsComponent.parseCommands(globalSection.getStringList("max-prestige-commands"));
			lastPrestigeNumber = ConfigManager.getLongOrElse(globalSection, "last-prestige", "final-prestige");
			lastPrestigeName = String.valueOf(lastPrestigeNumber);
			universalPrestige = new UniversalPrestige("0",
					StringManager.parseColorsAndSymbols(
							ConfigManager.getOrElse(globalSection, String.class, StorageFields.DISPLAY_FIELDS)),
					null, 0.0d, globalSection.getStringList("broadcast"),
					ConfigManager.getOrElse(globalSection, List.class, StorageFields.MESSAGE_FIELDS),
					CommandsComponent.parseCommands(
							ConfigManager.getOrElse(globalSection, List.class, StorageFields.COMMANDS_FIELDS)),
					RequirementsComponent.parseRequirements(
							ConfigManager.getOrElse(globalSection, StorageFields.REQUIREMENTS_FIELDS)),
					null, null, null, null, null, firstPrestigeNumber);
			costExpression = globalSection.getString("cost-expression");
		}

		@Override
		public boolean prestigeExists(String name) {
			return prestigeExists(Long.parseLong(name));
		}

		@Override
		public boolean prestigeExists(long number) {
			return number > 0 && number <= lastPrestigeNumber;
		}

		@Override
		public Prestige getPrestige(String name) {
			Prestige regPrestige = prestiges.get(Long.parseLong(name));
			if (regPrestige == null) {
				regPrestige = universalPrestige;
				return regPrestige.setName(name);
			}
			return regPrestige;
		}

		@Override
		public Prestige getPrestige(long number) {
			return getPrestige(String.valueOf(number));
		}

		@Override
		public String getFirstPrestigeName() {
			return firstPrestigeName;
		}

		@Override
		public long getFirstPrestigeAsNumber() {
			return firstPrestigeNumber;
		}

		@Override
		public String getLastPrestigeName() {
			return lastPrestigeName;
		}

		@Override
		public long getLastPrestigeAsNumber() {
			return lastPrestigeNumber;
		}

		@Override
		public Set<String> getPrestigeNames() {
			return null;
		}

		@Override
		public Collection<Prestige> getPrestiges() {
			return null;
		}

		@Override
		public String matchPrestigeName(String name) {
			return String.valueOf(IntParser.readLong(name));
		}

		@Override
		public Prestige matchPrestige(String name) {
			return prestiges.get(Long.parseLong(name));
		}

		@Override
		public boolean isInfinite() {
			return true;
		}

		@Override
		public long getPrestigeNumber(String name) {
			return Long.parseLong(name);
		}

		@Override
		public String getRangedDisplay(long prestige) {
			return constantSettings.get(HashedLongRange.matchingHash(prestige));
		}

		@Override
		public void useContinuousComponents(long prestige, Consumer<ComponentsHolder> componentsAction) {
			ModuloLongRange.forEachMatchingHash(continuousSettings, prestige, componentsAction);
		}

		@Override
		public CommandsComponent getCommandsComponent() {
			return maxPrestigeCommands;
		}

		@Override
		public void useCommandsComponent(Consumer<CommandsComponent> action) {
			if (maxPrestigeCommands == null) return;
			action.accept(maxPrestigeCommands);
		}

		@Override
		public String getCostExpression() {
			return costExpression;
		}

	}

}
