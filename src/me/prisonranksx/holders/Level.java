package me.prisonranksx.holders;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.components.*;

public interface Level {

	/**
	 *
	 * @return name (section name in config file) of level.
	 */
	@NotNull
	String getName();

	/**
	 * Changes level name to a new one (memory change only).
	 * AdminExecutor should be used for file changes.
	 * 
	 * @param name new name to change to
	 * @return level object for further editing.
	 */
	Level setName(String name);

	/**
	 *
	 * @return display name with parsed colors and symbols.
	 */
	@NotNull
	String getDisplayName();

	/**
	 * Sets a new display name (memory change only).
	 * AdminExecutor should be used for file changes.
	 * 
	 * @param displayName new display name.
	 */
	void setDisplayName(String displayName);

	/**
	 *
	 * @return next name of level or null if it's the last one.
	 */
	@Nullable
	String getNextName();

	void setNextName(String nextName);

	/**
	 *
	 * @return Gets cost of level. Increased cost is not included, just the plain
	 *         cost
	 *         specified in config file.
	 */
	double getCost();

	void setCost(double cost);

	@Nullable
	List<String> getBroadcastMessages();

	void setBroadcastMessages(List<String> broadcastMessages);

	@Nullable
	List<String> getMessages();

	void setMessages(List<String> messages);

	@Nullable
	CommandsComponent getCommandsComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 * 
	 * @param action to perform on component.
	 */
	void useCommandsComponent(Consumer<CommandsComponent> action);

	void setCommandsComponent(CommandsComponent commandsComponent);

	@Nullable
	RequirementsComponent getRequirementsComponent();

	void setRequirementsComponent(RequirementsComponent requirementsComponent);

	@Nullable
	ActionBarComponent getActionBarComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 * 
	 * @param action to perform on component.
	 */
	void useActionBarComponent(Consumer<ActionBarComponent> action);

	void setActionBarComponent(ActionBarComponent actionBarComponent);

	@Nullable
	PermissionsComponent getPermissionsComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 * 
	 * @param action to perform on component.
	 */
	void usePermissionsComponent(Consumer<PermissionsComponent> action);

	void setPermissionsComponent(PermissionsComponent permissionsComponent);

	@Nullable
	FireworkComponent getFireworkComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 * 
	 * @param action to perform on component.
	 */
	void useFireworkComponent(Consumer<FireworkComponent> action);

	void setFireworkComponent(FireworkComponent fireworkComponent);

	@Nullable
	RandomCommandsComponent getRandomCommandsComponent();

	/**
	 * Uses and executes action on component only if it's not null, otherwise does
	 * nothing.
	 * 
	 * @param action to perform on component.
	 */
	void useRandomCommandsComponent(Consumer<RandomCommandsComponent> action);

	void setRandomCommandsComponent(RandomCommandsComponent randomCommandsComponent);

	@Nullable
	List<String> getRequirementsMessages();

	void setRequirementsMessages(List<String> requirementsMessages);

	/**
	 * Index used for organizing lists.
	 * 
	 * @return level number in the ladder of the same type of levels.
	 */
	long getIndex();

	void setIndex(long index);

	default long getNumber() {
		return getIndex();
	}

	default void setNumber(long number) {
		setIndex((int) number);
	}

	default boolean isMultiAccess() {
		return false;
	}

}
