package dev.authorises.factionalert;

import cc.javajobs.factionsbridge.FactionsBridge;
import cc.javajobs.factionsbridge.bridge.infrastructure.struct.FPlayer;
import cc.javajobs.factionsbridge.bridge.infrastructure.struct.Faction;
import cc.javajobs.factionsbridge.bridge.infrastructure.struct.FactionsAPI;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class FactionAlert extends JavaPlugin implements Listener{

    // FactionsBridge API
    private FactionsAPI api;

    // Utility to format a message, so that you can use & color codes.
    private String format(String s, Player p, Faction f, String message){

        s = s.replace("%PLAYER%", p.getName())
                .replace("%DISPLAY%", p.getDisplayName())
                .replace("%FACTION%", f.getName())
                .replace("%TIME%", new SimpleDateFormat(getConfig().getString("date-format")).format(new Date()))
                .replace("%MESSAGE%", message);

        return ChatColor.translateAlternateColorCodes('&', s);
    }

    // Utility to send an Action Bar to the player.
    private void sendActionBar(Player p, String msg) {
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + msg + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void e(PlayerCommandPreprocessEvent e){
        String x = e.getMessage().toLowerCase(Locale.ROOT);
        if(x.startsWith("/f alert ")){
            e.setCancelled(true);
            String xx = e.getMessage().replace("/f alert ", "");
            Faction fac = api.getFaction(e.getPlayer());
            assert fac != null;
            for(FPlayer fp : fac.getOnlineMembers()){
                Player p = fp.getPlayer();
                assert p != null;
                if(getConfig().getBoolean("chat-message.enabled")) p.sendMessage(format(getConfig().getString("chat-message.format"), p, fac, xx));
                String[] t = new String[2];
                t[0] = t[1] = "";
                if(getConfig().getBoolean("title.main-title.enabled")) t[0] = format(getConfig().getString("title.main-title.format"), p, fac, xx);
                if(getConfig().getBoolean("title.sub-title.enabled")) t[1] = format(getConfig().getString("title.sub-title.format"), p, fac, xx);
                p.sendTitle(t[0], t[1]);
                if(getConfig().getBoolean("action-bar.enabled")) sendActionBar(p, format(getConfig().getString("action-bar.format"),p,fac,xx));
                //p.playSound(p.getLocation(), Sound.BAT_DEATH, 1.0F, 1.0F);
            }
        }else if(x.equals("/f alert")){
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("usage-message")));
            e.setCancelled(true);
        }
    }

    @Override
    public void onEnable() {
        // Set FactionsBridge API
        saveDefaultConfig();
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("FactionsBridge was not found, disabling plugin!");
            getPluginLoader().disablePlugin(this);
        }else {
            api = FactionsBridge.getFactionsAPI();
        }

        Bukkit.getPluginManager().registerEvents(this,this);
    }

}
