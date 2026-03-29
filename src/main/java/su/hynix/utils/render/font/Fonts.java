package su.hynix.utils.render.font;

public class Fonts {
    public static final String FONT_DIR = "/assets/minecraft/hynix/fonts/";

    public static volatile StyledFont[] sf_medium = new StyledFont[33];
    public static volatile StyledFont[] sf_regular = new StyledFont[28];
    public static volatile StyledFont[] sf_semibold = new StyledFont[38];
    public static volatile StyledFont[] sf_bold = new StyledFont[38];
    public static volatile StyledFont[] icons = new StyledFont[42];
    public static volatile StyledFont[] waypoint_icons = new StyledFont[42];
    public static volatile StyledFont[] hynix_icons = new StyledFont[42];
    public static volatile StyledFont[] hynix = new StyledFont[42];

    public static void init() {
        fill(sf_medium, "sf_medium.ttf", 0);
        fill(sf_bold, "sf_bold.ttf", 0);
        fill(sf_regular, "sf_regular.ttf", -1.01f);
        fill(sf_semibold, "sf_semibold.ttf", 0);
        fill(icons, "icons.ttf", 0);
        fill(hynix_icons, "hynix_icons.ttf", 0);
        fill(hynix, "hynix.ttf", 0);
        fill(waypoint_icons, "waypoint_icons.ttf", 0);
    }

    private static void fill(StyledFont[] target, String fontFileName, float spacing) {
        for (int i = 1; i < target.length; i++) {
            target[i] = new StyledFont(fontFileName, i, spacing, true);
        }
    }
}