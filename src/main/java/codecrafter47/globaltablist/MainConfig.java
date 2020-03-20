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

import java.io.File;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;
import net.md_5.bungee.api.plugin.Plugin;

public class MainConfig extends YamlConfig {

    @Comments({ "true: global tablist", "false: server unique tablist" })
    public boolean useGlobalTablist = true;

    @Comments({ "whether ping is sent to clients", "setting this to false can help you reducing network traffic" })
    public boolean updatePing = false;

    @Comments({ "Whether to send header/footer to the clients or not" })
    public boolean showHeaderFooter = true;

    @Comments({ "This text will be shown above the tablist", " - {player} will be replaced with the name of the player", " - {newline} will insert a linebreak" })
    public String header = "&6Welcome &f{player}";

    @Comments({ "This text will be shown below the tablist", " - {player} will be replaced with the name of the player", " - {newline} will insert a linebreak" })
    public String footer = "&4minecraft.net";

    public MainConfig(Plugin plugin) throws InvalidConfigurationException {
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
        CONFIG_HEADER = new String[] { "", "" };

        this.init();
    }
}
