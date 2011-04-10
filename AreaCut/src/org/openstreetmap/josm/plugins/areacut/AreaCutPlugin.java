package org.openstreetmap.josm.plugins.areacut;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * 
 *
 * @author dotevo
 */
public class AreaCutPlugin extends Plugin {
    public AreaCutPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.toolsMenu, new AreaCutAction());
    }
}
