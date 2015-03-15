package org.gestern.bukkitmigration;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class UUIDCache implements Listener {
	private static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	private Map<String, UUID> cache = new ConcurrentHashMap();
	private JavaPlugin plugin;

	public UUIDCache(JavaPlugin plugin) {
		Validate.notNull(plugin);
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public UUID getIdOptimistic(String name) {
		Validate.notEmpty(name);
		UUID uuid = cache.get(name);
		if (uuid == null) {
			ensurePlayerUUID(name);
			return null;
		}
		return uuid;
	}

	public UUID getId(String name) {
		Validate.notEmpty(name);
		UUID uuid = cache.get(name);
		if (uuid == null) {
			syncFetch(nameList(name));
			return cache.get(name);
		}
		if (uuid.equals(ZERO_UUID)) {
			uuid = null;
		}
		return uuid;
	}

	public void shutdown() {
		HandlerList.unregisterAll(this);
		plugin = null;
	}

	public void ensurePlayerUUID(String name) {
		if (cache.containsKey(name))
			return;
		cache.put(name, ZERO_UUID);
		asyncFetch(nameList(name));
	}

	private void asyncFetch(final ArrayList<String> names) {
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				UUIDCache.this.syncFetch(names);
			}
		});
	}

	private void syncFetch(ArrayList<String> names) {
		UUIDFetcher fetcher = new UUIDFetcher(names);
		try {
			cache.putAll(fetcher.call());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<String> nameList(String name) {
		ArrayList names = new ArrayList();
		names.add(name);
		return names;
	}

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {
		ensurePlayerUUID(event.getPlayer().getName());
	}

	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {
		cache.remove(event.getPlayer().getName());
	}
}