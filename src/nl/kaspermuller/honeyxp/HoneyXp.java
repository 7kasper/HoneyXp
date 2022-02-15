package nl.kaspermuller.honeyxp;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class HoneyXp extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		registerGlow();
		getServer().getConsoleSender().sendMessage("Enabled §6HoneyXp§r plugin :D");
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (
			e.getAction() == Action.RIGHT_CLICK_BLOCK && 
			e.getClickedBlock().getType() == Material.LIGHTNING_ROD && 
			e.getClickedBlock().getRelative(BlockFace.DOWN).getType() == Material.LAPIS_BLOCK
		) {
			if (e.getItem().getType() == Material.HONEY_BOTTLE && !(e.getItem().hasItemMeta() || e.getItem().getItemMeta().hasLore())) {
				ExperienceManager expMan = new ExperienceManager(e.getPlayer());
				if (e.getPlayer().hasPermission("honeyxp.bottle") && expMan.hasExp(7)) {
					expMan.changeExp(-7);
					ItemStack hb = e.getItem();
					ItemStack newB = hb.clone();
					// Remove one normal honey bottle.
					hb.setAmount(hb.getAmount() - 1);
					// Prepare new Honey Xp Bottle
					newB.setAmount(1);
					ItemMeta m = newB.getItemMeta();
					m.setLore(Arrays.asList("Stored XP: 7")); //TODO add more level possibility?
					m.setDisplayName(ChatColor.RESET + "Honey Xp Bottle");
					// Add item glow effect.
					NamespacedKey key = new NamespacedKey(this, getDescription().getName());
					GlowEnchant glow = new GlowEnchant(key);
					m.addEnchant(glow, 1, true);
//					m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					// Give the item.
					newB.setItemMeta(m);
					e.getPlayer().getInventory().addItem(newB);
					e.getPlayer().updateInventory();
					e.getPlayer().getWorld().playSound(e.getPlayer(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0f, 1.0f);
				} else {
					e.getPlayer().getWorld().playSound(e.getPlayer(), Sound.BLOCK_LAVA_POP, 1.0f, 1.0f);
				}
			}
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
					} catch (Exception ex) { ex.printStackTrace();}
				} else {
					e.getPlayer().getWorld().playSound(e.getPlayer(), Sound.BLOCK_LAVA_POP, 1.0f, 1.0f);
					e.setCancelled(true);
				}
			}
		}
	}
	
	// UTIL
    public void registerGlow() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
         NamespacedKey key = new NamespacedKey(this, getDescription().getName());
           
            GlowEnchant glow = new GlowEnchant(key);
            Enchantment.registerEnchantment(glow);
        }
        catch (IllegalArgumentException e){
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
