package nl.kaspermuller.honeyxp;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class HoneyXp extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getConsoleSender().sendMessage("Enabled §6HoneyXp§r plugin :D");
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (
			e.getAction() == Action.RIGHT_CLICK_BLOCK && 
			e.hasItem() && e.getItem().getType() == Material.HONEY_BOTTLE &&
			e.getClickedBlock().getType() == Material.LIGHTNING_ROD && 
			e.getClickedBlock().getRelative(BlockFace.DOWN).getType() == Material.LAPIS_BLOCK
		) {
			ExperienceManager expMan = new ExperienceManager(e.getPlayer());
			if (e.getPlayer().hasPermission("honeyxp.bottle")) {
				int points = 7;
				int onPoints = 0;
				if (e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasLore()) {
					try {
						onPoints = Integer.parseInt(e.getItem().getItemMeta().getLore().get(0).split(": ")[1]);
						points = expMan.getXpNeededToLevelUp(expMan.getLevelForExp(onPoints) + 1);
						if (points > 36) { // don't go further than 21 levels (from 0) stored in a bottle.
							e.getPlayer().getWorld().playSound(e.getPlayer(), Sound.BLOCK_LAVA_POP, 1.0f, 1.0f);
							return;
						}
					} catch (Exception ex) { ex.printStackTrace();}
				}
				expMan.changeExp(-points);
				ItemStack hb = e.getItem();
				ItemStack newB = hb.clone();
				// Remove one normal honey bottle.
				hb.setAmount(hb.getAmount() - 1);
				// Prepare new Honey Xp Bottle
				newB.setAmount(1);
				ItemMeta m = newB.getItemMeta();
				m.setLore(Arrays.asList("Stored XP: " + (onPoints + points)));
				m.setDisplayName(ChatColor.RESET + "Honey Xp Bottle");
				// TODO no hack? For now we just add random enchantment and remove its visisbility:
				m.addEnchant(Enchantment.THORNS, 1, true);
				m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				// Give the item.
				newB.setItemMeta(m);
				e.getPlayer().getInventory().addItem(newB);
				e.getPlayer().updateInventory();
				e.getPlayer().getWorld().playSound(e.getPlayer(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0f, 1.0f);
				return;
			}
			e.getPlayer().getWorld().playSound(e.getPlayer(), Sound.BLOCK_LAVA_POP, 1.0f, 1.0f);
		}
	}
	
	@EventHandler
	public void onPlayerConsume(PlayerItemConsumeEvent e) {
		if (e.getItem().getType() == Material.HONEY_BOTTLE) {
			if (e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasLore()) {
				if (e.getPlayer().hasPermission("honeyxp.drink")) {
					try {
						ExperienceManager expMan = new ExperienceManager(e.getPlayer());
						int points = Integer.parseInt(e.getItem().getItemMeta().getLore().get(0).split(": ")[1]);
						expMan.changeExp(points);
						e.getPlayer().getWorld().playSound(e.getPlayer(), Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1.0f, 1.0f);
					} catch (Exception ex) { ex.printStackTrace(); }
				} else {
					e.getPlayer().getWorld().playSound(e.getPlayer(), Sound.BLOCK_LAVA_POP, 1.0f, 1.0f);
					e.setCancelled(true);
				}
			}
		}
	}

}
