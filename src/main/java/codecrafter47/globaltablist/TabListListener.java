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

import java.lang.reflect.Field;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabListListener implements Listener {

    private final GlobalTablist plugin;
    private Field tabListHandler;

    public TabListListener(GlobalTablist plugin) {
        this.plugin = plugin;

        try {
            Class<UserConnection> cplayer = UserConnection.class;
            tabListHandler = cplayer.getDeclaredField("tabListHandler");
            tabListHandler.setAccessible(true);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e) throws IllegalArgumentException, IllegalAccessException {
        if (plugin.getConfig().useGlobalTablist) {
            tabListHandler.set(e.getPlayer(), new GlobalTablistHandler(e.getPlayer(), plugin));
        }
    }
}
