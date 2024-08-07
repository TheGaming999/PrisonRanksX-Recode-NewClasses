package me.prisonranksx.lists;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.UniversalPrestige;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.CollectionUtils;
import me.prisonranksx.utils.NumParser;

public class InfinitePrestigesTextList implements PrestigesTextList {

	private PrisonRanksX plugin;
	private String prestigeCurrentFormat;
	private String prestigeCompletedFormat;
	private String prestigeOtherFormat;
	private boolean enablePages;
	private int prestigePerPage;
	private List<String> prestigeWithPagesListFormat;
	private List<String> prestigeListFormat;
	boolean isCustomList;
	private List<String> prestigeListFormatHeader;
	private List<String> prestigeListFormatFooter;

	public InfinitePrestigesTextList(PrisonRanksX plugin) {
		this.plugin = plugin;
		setup();
	}

	@Override
	public void setup() {
		prestigeCurrentFormat = plugin.getPrestigesListSettings().getPrestigeCurrentFormat();
		prestigeCompletedFormat = plugin.getPrestigesListSettings().getPrestigeCompletedFormat();
		prestigeOtherFormat = plugin.getPrestigesListSettings().getPrestigeOtherFormat();
		enablePages = plugin.getPrestigesListSettings().isEnablePages();
		prestigePerPage = plugin.getPrestigesListSettings().getPrestigePerPage();
		prestigeWithPagesListFormat = plugin.getPrestigesListSettings().getPrestigeWithPagesListFormat();
		prestigeListFormat = plugin.getPrestigesListSettings().getPrestigeListFormat();
		prestigeListFormatHeader = new ArrayList<>();
		prestigeListFormatFooter = new ArrayList<>();
		int ignoreIndex = prestigeWithPagesListFormat.indexOf("[prestigeslist]");
		if (prestigeListFormatHeader.isEmpty() && prestigeListFormatFooter.isEmpty() && prestigeListFormat.size() > 1) {
			for (int i = 0; i < prestigeWithPagesListFormat.size(); i++) {
				if (ignoreIndex > i)
					prestigeListFormatHeader.add(prestigeWithPagesListFormat.get(i));
				else if (ignoreIndex < i) prestigeListFormatFooter.add(prestigeWithPagesListFormat.get(i));
			}
		}
		if (enablePages) {
			if (!prestigeWithPagesListFormat.contains("[prestigeslist]")) isCustomList = true;
		} else {
			if (!prestigeListFormat.contains("[prestigeslist]")) isCustomList = true;
		}
	}

	/**
	 * Sends prestiges list as configured in config file, if pageNumber is null or
	 * enable pages is false, then a non-paged list will be sent.
	 * 
	 * @param sender     to send to
	 * @param pageNumber number of page
	 */
	@Override
	public void send(CommandSender sender, @Nullable String pageNumber) {
		if (enablePages && pageNumber != null)
			sendPagedList(sender, pageNumber);
		else
			sendList(sender);
	}

	/**
	 * Gets limited number of prestige names for a list (4 before current
	 * prestige and 4 after) or less if either sides don't have enough prestiges
	 * to return. This is to prevent the stress of
	 * redundant looping over millions of them
	 *
	 * @param user to get current prestige from
	 * @param max  max for each side (4 normally)
	 * @return list of prestige names
	 */
	private List<String> getPrestigeNamesWithinRange(User user, int max) {
		List<String> levelRange = new ArrayList<>();
		long currentNumber = user.getPrestige().getNumber();
		long startLevel = currentNumber - max;
		if (startLevel < 1) startLevel = 1;
		long endLevel = startLevel + (max * 2L);
		if (endLevel > PrestigeStorage.getLastPrestigeAsNumber()) endLevel = PrestigeStorage.getLastPrestigeAsNumber();
		for (long level = startLevel; level <= endLevel; level++) levelRange.add(String.valueOf(level));
		return levelRange;
	}

	@Override
	public void sendList(CommandSender sender) {
		if (isCustomList) {
			List<String> customList = prestigeListFormat;
			if (sender instanceof Player) customList = StringManager.parsePlaceholders(customList, (Player) sender);
			customList.forEach(sender::sendMessage);
		}
		Player p = (Player) sender;
		User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
		String prestigeName = user.getPrestigeName();
		List<String> prestigesCollection = new ArrayList<>(getPrestigeNamesWithinRange(user, 4));
		// Header Setup
		List<String> header = new ArrayList<>(prestigeListFormatHeader);

		// Prestiges List Organization
		List<String> currentPrestiges = new ArrayList<>(), completedPrestiges = new ArrayList<>(),
				otherPrestiges = new ArrayList<>(), nonPagedPrestiges = new ArrayList<>();
		int currentPrestigeIndex = prestigesCollection.indexOf(prestigeName);
		for (String cyclingPrestigeName : prestigesCollection) {
			Prestige prestige = PrestigeStorage.getPrestige(cyclingPrestigeName);
			// Current Prestige Format
			if (currentPrestigeIndex == prestigesCollection.indexOf(cyclingPrestigeName)) {
				if (prestige.getNextName() != null) {
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextName());
					String format = StringManager
							.parseAll(prestigeCurrentFormat.replace("%prestige_name%", cyclingPrestigeName)
									.replace("%prestige_displayname%", prestige.getDisplayName())
									.replace("%nextprestige_name%", prestige.getNextName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);

					currentPrestiges.add(format);
				}
			}
			// Completed Prestige Format
			if (currentPrestigeIndex > prestigesCollection.indexOf(cyclingPrestigeName)) {
				if (prestige.getNextName() != null) {
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextName());
					String format = StringManager
							.parseAll(prestigeCompletedFormat.replace("%prestige_name%", cyclingPrestigeName)
									.replace("%prestige_displayname%", prestige.getDisplayName())
									.replace("%nextprestige_name%", prestige.getNextName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);
					completedPrestiges.add(format);
				}
			}
			// Other Prestige Format
			if (currentPrestigeIndex < prestigesCollection.indexOf(cyclingPrestigeName)) {
				if (prestige.getNextName() != null) {
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextName());
					String format = StringManager
							.parseAll(prestigeOtherFormat.replace("%prestige_name%", cyclingPrestigeName)
									.replace("%prestige_displayname%", prestige.getDisplayName())
									.replace("%nextprestige_name%", prestige.getNextName())
									.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
									.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
									.replace("%nextprestige_cost_us_format%",
											EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
									.replace("%nextprestige_cost_formatted%",
											EconomyManager.shortcutFormat(nextPrestige.getCost())),
									p);
					otherPrestiges.add(format);
				}
			}
		}
		nonPagedPrestiges.addAll(completedPrestiges);
		nonPagedPrestiges.addAll(currentPrestiges);
		nonPagedPrestiges.addAll(otherPrestiges);

		List<String> footer = new ArrayList<>(prestigeListFormatFooter);

		header.forEach(sender::sendMessage);
		nonPagedPrestiges.forEach(sender::sendMessage);
		footer.forEach(sender::sendMessage);
	}

	public long getPageWherePrestigeIsFound(long prestigeNum) {
		if (prestigeNum > 0) return (long) (Math.floor(prestigeNum - 1) / prestigePerPage) + 1;
		return 1;
	}

	@Override
	public void sendPagedList(CommandSender sender, String pageNumber) {
		if (isCustomList) {
			List<String> customList = CollectionUtils.paginateList(prestigeWithPagesListFormat, prestigePerPage,
					NumParser.asInt(pageNumber, 1));
			if (sender instanceof Player) customList = StringManager.parsePlaceholders(customList, (Player) sender);
			customList.forEach(sender::sendMessage);
			return;
		}
		Player p = (Player) sender;
		int counter = 0;
		User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
		long size = PrestigeStorage.getLastPrestigeAsNumber();
		long finalPage = CollectionUtils.getFinalPage(size - 1, prestigePerPage);
		boolean findCurrentPage = pageNumber.equals("-1") || pageNumber.equals("0");
		long currentPage = findCurrentPage ? getPageWherePrestigeIsFound(user.getPrestige().getNumber())
				: NumParser.asLong(pageNumber, 1);
		if (findCurrentPage) pageNumber = String.valueOf(currentPage);
		if (currentPage > finalPage) {
			Messages.sendMessage(p, Messages.getPrestigeListLastPageReached(),
					s -> s.replace("%page%", String.valueOf(finalPage)));
			return;
		}

		// Prestiges List Organization
		List<String> currentPrestiges = new ArrayList<>(), completedPrestiges = new ArrayList<>(),
				otherPrestiges = new ArrayList<>(), pagedPrestiges = new ArrayList<>();
		Prestige currentPrestige = user.hasPrestige() ? user.getPrestige() : null;
		long currentPrestigeNumber = currentPrestige == null ? 0 : currentPrestige.getNumber();
		for (int i = 0; i < prestigePerPage; i++) {
			int elementIndex = CollectionUtils.paginateIndex(counter, prestigePerPage, (int) currentPage);
			if (elementIndex < 0 || elementIndex >= size) break;
			long prestigeNumber = (elementIndex) + 1;
			if (currentPrestigeNumber == 0 && prestigeNumber == 1 && pageNumber.equals("1")) {
				// First Prestige
				Prestige prestige = PrestigeStorage.getPrestige(0);
				if (!prestige.isLast()) {
					String format = prestigeCurrentFormat.replace("%prestige_name%", prestige.getName())
							.replace("%prestige_displayname%", prestige.getDisplayName());
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextPrestigeName());
					format = format.replace("%nextprestige_name%", prestige.getNextName())
							.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
							.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
							.replace("%nextprestige_cost_us_format%",
									EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
							.replace("%nextprestige_cost_formatted%",
									EconomyManager.shortcutFormat(nextPrestige.getCost()));
					currentPrestiges.add(format);
				}
			}
			if (currentPrestigeNumber == prestigeNumber) {
				// Prestige Current:
				Prestige prestige = PrestigeStorage.getPrestige(prestigeNumber);
				if (!prestige.isLast()) {
					String format = prestigeCurrentFormat.replace("%prestige_name%", prestige.getName())
							.replace("%prestige_displayname%", prestige.getDisplayName());
					Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextPrestigeName());
					format = format.replace("%nextprestige_name%", prestige.getNextName())
							.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
							.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
							.replace("%nextprestige_cost_us_format%",
									EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
							.replace("%nextprestige_cost_formatted%",
									EconomyManager.shortcutFormat(nextPrestige.getCost()));
					currentPrestiges.add(format);
				}
			} else if (currentPrestigeNumber > prestigeNumber) {
				// Prestige Completed:
				Prestige prestige = PrestigeStorage.getPrestige(prestigeNumber - 1);
				Prestige nextPrestige = PrestigeStorage.getPrestige(prestige.getNextPrestigeName());
				if (!prestige.isLast()) {
					String format = prestigeCompletedFormat.replace("%prestige_name%", prestige.getName())
							.replace("%prestige_displayname%", prestige.getDisplayName());

					format = format.replace("%nextprestige_name%", prestige.getNextName())
							.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
							.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
							.replace("%nextprestige_cost_us_format%",
									EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
							.replace("%nextprestige_cost_formatted%",
									EconomyManager.shortcutFormat(nextPrestige.getCost()));
					completedPrestiges.add(format);
				}
			} else {
				// Prestige Other:
				UniversalPrestige prestige = (UniversalPrestige) PrestigeStorage.getPrestige(prestigeNumber);
				if (!prestige.isLast()) {
					String format = prestigeOtherFormat.replace("%prestige_name%", prestige.getName())
							.replace("%prestige_displayname%", prestige.getDisplayName());
					UniversalPrestige nextPrestige = (UniversalPrestige) prestige.getNextPrestige();
					format = format.replace("%nextprestige_name%", nextPrestige.getName())
							.replace("%nextprestige_displayname%", nextPrestige.getDisplayName())
							.replace("%nextprestige_cost%", String.valueOf(nextPrestige.getCost()))
							.replace("%nextprestige_cost_us_format%",
									EconomyManager.commaFormatWithDecimals(nextPrestige.getCost()))
							.replace("%nextprestige_cost_formatted%",
									EconomyManager.shortcutFormat(nextPrestige.getCost()));
					otherPrestiges.add(format);
				}
			}
			counter++;
			if (PrestigeStorage.getPrestige(prestigeNumber).isLast()) break;
		}
		// Header Setup
		List<String> header = new ArrayList<>();
		for (String headerLine : prestigeListFormatHeader) header.add(StringManager.parsePlaceholders(
				headerLine.replace("%currentpage%", pageNumber).replace("%totalpages%", String.valueOf(finalPage)),
				user.getPlayer()));

		pagedPrestiges.addAll(completedPrestiges);
		pagedPrestiges.addAll(currentPrestiges);
		pagedPrestiges.addAll(otherPrestiges);

		List<String> footer = new ArrayList<>();
		for (String footerLine : prestigeListFormatFooter) footer.add(StringManager.parsePlaceholders(
				footerLine.replace("%currentpage%", pageNumber).replace("%totalpages%", String.valueOf(finalPage)),
				user.getPlayer()));

		header.forEach(sender::sendMessage);
		pagedPrestiges.forEach(sender::sendMessage);
		footer.forEach(sender::sendMessage);
	}

}
