/*
 * GlobalTablist - get the global tablist back
 *
 * Copyright (C) 2014 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package codecrafter47.globaltablist;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.tab.TabList;

/**
 * @author Florian Stober
 */
public class GlobalTablistHandler extends TabList {

    private final GlobalTablist plugin;

    private int lastPing = 0;

    private HashSet<UUID> npcsInTabList;

    public GlobalTablistHandler(ProxiedPlayer player, GlobalTablist plugin) {
        super(player);
        // plugin.getLogger().info(player.getName() + " GlobalTablistHandler " + Thread.currentThread());
        this.plugin = plugin;
        this.npcsInTabList = new HashSet<>();
    }

    protected ProxiedPlayer getPlayer() {
        return player;
    }

    @Override
    public void onServerChange() {
        // cleanup npcs
        if (!npcsInTabList.isEmpty()) {
            final PlayerListItem out = new PlayerListItem();
            out.setAction(Action.REMOVE_PLAYER);
            int l = npcsInTabList.size();
            Item[] items = new Item[l];
            int pos = 0;
            for (UUID npc : npcsInTabList) {
                Item i = new Item();
                i.setUuid(npc);
                items[pos] = i;
                pos += 1;
            }
            out.setItems(items);
            player.unsafe().sendPacket(out);
            npcsInTabList.clear();
        }
    }

    @Override
    public void onUpdate(PlayerListItem pli) {
        for (Item i : pli.getItems()) {
            if (i.getUuid().version() != 4) {
                // NPCs: passthrough
                final PlayerListItem out = new PlayerListItem();
                out.setAction(pli.getAction());
                out.setItems(new Item[] { i });
                if (pli.getAction() == Action.REMOVE_PLAYER) {
                    // delay remove to safely remove npc tablist entries
                    plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                        @Override
                        public void run() {
                            player.unsafe().sendPacket(out);
                        }
                    }, 50, TimeUnit.MILLISECONDS);
                    npcsInTabList.remove(i.getUuid());
                } else {
                    player.unsafe().sendPacket(out);
                    npcsInTabList.add(i.getUuid());
                }
            } else {
                // Real players: update gamemode for players only
                if (pli.getAction() == Action.ADD_PLAYER) {
                    if (i.getUuid().equals(getPlayer().getUniqueId())) {
                        ((UserConnection) player).setGamemode(i.getGamemode());
                        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                            sendPlayerSlot(p, getPlayer());
                        }

                        PlayerListItem packet = new PlayerListItem();
                        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
                        Item i2 = new Item();
                        i2.setUuid(i.getUuid());
                        i2.setGamemode(i.getGamemode());
                        packet.setItems(new Item[] { i2 });
                        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                            p.unsafe().sendPacket(packet);
                        }
                    }
                } else if (pli.getAction() == Action.UPDATE_GAMEMODE) {
                    if (i.getUuid().equals(getPlayer().getUniqueId())) {
                        ((UserConnection) player).setGamemode(i.getGamemode());
                        PlayerListItem packet = new PlayerListItem();
                        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
                        packet.setItems(new Item[] { i });
                        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                            p.unsafe().sendPacket(packet);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPingChange(int newPing) {
        // plugin.getLogger().info(player.getName() + " onPingChange " + Thread.currentThread());
        if (plugin.getConfig().updatePing) {
            if (Math.abs(lastPing - newPing) > 2) {
                lastPing = newPing;
                PlayerListItem pli = new PlayerListItem();
                pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
                Item item = new Item();
                item.setUsername(getPlayer().getName());
                item.setUuid(getPlayer().getUniqueId());
                String text = player.getDisplayName();
                if (text.length() > 16) {
                    text = text.substring(0, 16);
                }
                item.setDisplayName(text);
                item.setPing(newPing);
                pli.setItems(new Item[] { item });
                for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                    p.unsafe().sendPacket(pli);
                }
            }
        }
    }

    @Override
    public void onConnect() {
        //        plugin.getLogger().info(player.getName() + " onConnect " + Thread.currentThread());
        // System.out.println("onConnect " + getPlayer().getName());
        // send players
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            sendPlayerSlot(p, getPlayer());
            if (p == getPlayer()) {
                continue;
            }
            sendPlayerSlot(getPlayer(), p);
        }

        // store ping
        lastPing = getPlayer().getPing();
    }

    @Override
    public void onDisconnect() {
        //        plugin.getLogger().info(player.getName() + " onDisconnect " + Thread.currentThread());
        // System.out.println("onDisconnect " + getPlayer().getName());
        // System.out.println(plugin.getProxy().getPlayer(getPlayer().getUniqueId()) == getPlayer());
        // remove player
        if (plugin.getProxy().getPlayer(getPlayer().getUniqueId()) == getPlayer()) {
            for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                removePlayerSlot(getPlayer(), p);
            }
        } else {
            // the player reconnected and would be invisible if not kicked
            plugin.getProxy().getPlayer(getPlayer().getUniqueId()).disconnect(new ComponentBuilder("Verbindung abgebrochen. Bitte erneut verbinden").color(ChatColor.RED).create());
        }

        //        if (getPlayer() != ProxyServer.getInstance().getPlayer(getPlayer().getUniqueId())) {
        //            // hack to revert changes from https://github.com/SpigotMC/BungeeCord/commit/830f18a35725f637d623594eaaad50b566376e59
        //            Server server = getPlayer().getServer();
        //            if (server != null) {
        //                server.disconnect(new TextComponent("Quitting"));
        //            }
        //            ((UserConnection) getPlayer()).setServer(null);
        //        }
    }

    protected boolean isPremium(ProxiedPlayer player) {
        return player.getPendingConnection().isOnlineMode();
    }

    protected void sendPlayerSlot(ProxiedPlayer player, ProxiedPlayer receiver) {
        // System.out.println("Sending Add " + player.getName() + " to " + receiver.getName());

        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        Item item = new Item();
        item.setPing(player.getPing());

        item.setUsername(player.getName());
        item.setGamemode(((UserConnection) player).getGamemode());
        item.setUuid(player.getUniqueId());
        item.setProperties(new String[0][0]);
        if (isPremium(receiver)) {
            LoginResult loginResult = ((UserConnection) player).getPendingConnection().getLoginProfile();
            if (loginResult != null) {
                Property[] properties = loginResult.getProperties();
                String[][] props = new String[properties.length][];
                for (int i = 0; i < props.length; i++) {
                    Property property = properties[i];

                    //                    byte[] decoded = Base64.getDecoder().decode(property.getValue().getBytes());
                    //                    JsonElement json = new JsonParser().parse(new String(decoded, Charset.forName("utf-8")));
                    //                    String skinURL = json.getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
                    //                    plugin.getLogger().info("SKINURL: " + skinURL);

                    //                    json.getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().addProperty("url",
                    //                            "http://textures.minecraft.net/texture/6fd174b5611b7e6752dca1a5b31ce195aff4a7af58b7b15e36b139e5d5315f2");

                    //                    plugin.getLogger().info(new String(decoded, Charset.forName("utf-8")));
                    //                    plugin.getLogger().info("jss:" + json.toString());
                    // props[i] = new String[] { property.getName(), Base64.getEncoder().encodeToString(json.toString().getBytes()) };
                    if (property.getSignature() == null) {
                        plugin.getLogger().info("Signature is null for " + player.getName() + ": " + property.getName() + " = " + property.getValue());
                        props[i] = new String[] { property.getName(), property.getValue() };
                    } else {
                        props[i] = new String[] { property.getName(), property.getValue(), property.getSignature() };
                    }
                }
                item.setProperties(props);
            } else {
                item.setProperties(new String[0][0]);
            }
        } else {
            item.setProperties(new String[0][0]);
        }

        pli.setItems(new Item[] { item });
        receiver.unsafe().sendPacket(pli);
    }

    private void removePlayerSlot(ProxiedPlayer player, ProxiedPlayer receiver) {
        // System.out.println("Sending Remove " + player.getName() + " to " + receiver.getName());
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        Item item = new Item();
        item.setUsername(player.getName());
        item.setUuid(player.getUniqueId());

        pli.setItems(new Item[] { item });
        receiver.unsafe().sendPacket(pli);
    }
}
