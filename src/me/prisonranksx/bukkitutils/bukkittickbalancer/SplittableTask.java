package me.prisonranksx.bukkitutils.bukkittickbalancer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public interface SplittableTask extends Runnable {

	public default BukkitTask init() {
		return init(JavaPlugin.getProvidingPlugin(SplittableTask.class));
	}

	public default BukkitTask initAsync() {
		return initAsync(JavaPlugin.getProvidingPlugin(SplittableTask.class));
	}

	public default BukkitTask init(JavaPlugin plugin) {
		return init(plugin, 0, 0);
	}

	public default BukkitTask init(JavaPlugin plugin, int delay, int period) {
		return Bukkit.getServer().getScheduler().runTaskTimer(plugin, this, delay, period);
	}

	public default BukkitTask initAsync(JavaPlugin plugin) {
		return initAsync(plugin, 0, 0);
	}

	public default BukkitTask initAsync(JavaPlugin plugin, int delay, int period) {
		return Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this, delay, period);
	}

}
