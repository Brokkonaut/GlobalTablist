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

import java.util.logging.Level;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Main Class of BungeeTabListPlus
 *
 * @author Florian Stober
 */
public class GlobalTablist extends Plugin {

    private CustomizationHandler customizationHandler;

    /**
     * provides access to the configuration
     */
    private MainConfig config;

    public MainConfig getConfig() {
        return config;
    }

    private final TabListListener listener = new TabListListener(this);

    /**
     * Called when the plugin is enabled
     */
    @Override
    public void onEnable() {
        // check whether bungee version is supported
        try {
            Class.forName("net.md_5.bungee.api.Title");
        } catch (ClassNotFoundException ex) {
            getLogger().severe("This plugin does not support your BungeeCord version");
            getLogger().severe("Please use build #996 or above");
            return;
        }

        try {
            config = new MainConfig(this);
        } catch (InvalidConfigurationException ex) {
            getLogger().warning("Unable to load Config");
            getLogger().log(Level.WARNING, null, ex);
            getLogger().info("Disabling Plugin");
            return;
        }

        if (config.showHeaderFooter) {
            customizationHandler = new CustomizationHandler(this);
        }

        ProxyServer.getInstance().getPluginManager().registerListener(this, listener);
    }

    /**
     * called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        // let the proxy do this
    }

    public void reportError(Throwable th) {
        getLogger().log(Level.WARNING, ChatColor.RED + "An internal error occured! Please send the " + "following stacktrace to the developer in order to help" + " resolving the problem", th);
    }
}
