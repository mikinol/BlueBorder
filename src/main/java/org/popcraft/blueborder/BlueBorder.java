package org.popcraft.blueborder;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public final class BlueBorder extends JavaPlugin {
    private static final String MARKER_SET_ID = "worldborder";
    private static final String DEFAULT_LABEL = "World border";
    private static final String DEFAULT_COLOR = "FF0000";
    private Color color;
    private String label;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadOptions();
        BlueMapAPI.onEnable(this::addWorldBorders);
        BlueMapAPI.onDisable(this::removeWorldBorders);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        reloadOptions();
        BlueMapAPI.getInstance().ifPresent(this::removeWorldBorders);
        BlueMapAPI.getInstance().ifPresent(this::addWorldBorders);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private void reloadOptions() {
        reloadConfig();
        color = new Color(Integer.parseInt(getConfig().getString("color", DEFAULT_COLOR).toLowerCase(), 16), 1f);
        label = getConfig().getString("label", DEFAULT_LABEL);
    }

    private void addWorldBorders(BlueMapAPI blueMapAPI) {
        for (final World world : getServer().getWorlds()) {
            final MarkerSet markerSet = MarkerSet.builder().label(label).build();
            final WorldBorder worldBorder = world.getWorldBorder();
            final double centerX = worldBorder.getCenter().getX();
            final double centerZ = worldBorder.getCenter().getZ();
            final double radius = worldBorder.getSize() / 2d;
            final Vector2d pos1 = new Vector2d(centerX - radius, centerZ - radius);
            final Vector2d pos2 = new Vector2d(centerX + radius, centerZ + radius);
            final Shape border = Shape.createRect(pos1, pos2);
            final ShapeMarker marker = ShapeMarker.builder()
                    .label(label)
                    .shape(border, world.getSeaLevel())
                    .lineColor(color)
                    .fillColor(new Color(0))
                    .lineWidth(3)
                    .depthTestEnabled(false)
                    .build();
            markerSet.getMarkers().put(world.getName(), marker);
            blueMapAPI.getWorld(world.getName())
                    .map(BlueMapWorld::getMaps)
                    .ifPresent(maps -> maps.forEach(map -> map.getMarkerSets().put(MARKER_SET_ID, markerSet)));
        }
    }

    private void removeWorldBorders(BlueMapAPI blueMapAPI) {
        blueMapAPI.getMaps().forEach(map -> map.getMarkerSets().remove(MARKER_SET_ID));
    }
}
