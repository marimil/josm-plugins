package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Preference settings for the French Cadastre plugin
 * 
 * @author Pieren <pieren3@gmail.com>
 */
public class CadastrePreferenceSetting implements PreferenceSetting {
    
    static final int TRANS_MIN = 1;
    static final int TRANS_MAX = 10;
    private JSlider sliderTrans = new JSlider(JSlider.HORIZONTAL, TRANS_MIN, TRANS_MAX, TRANS_MAX); 
    
    private JTextField sourcing = new JTextField(20);

    private JCheckBox alterColors = new JCheckBox(tr("Replace original background by JOSM background color."));

    private JCheckBox reversGrey = new JCheckBox(tr("Reverse grey colors (for black backgrounds)."));
    
    private JCheckBox transparency = new JCheckBox(tr("Set background transparent."));
    
    private JCheckBox drawBoundaries = new JCheckBox(tr("Draw boundaries of downloaded data."));

    private JRadioButton grabMultiplier1 = new JRadioButton("", true);

    private JRadioButton grabMultiplier2 = new JRadioButton("", true);

    private JRadioButton grabMultiplier3 = new JRadioButton("", true);
    
    private JRadioButton grabMultiplier4 = new JRadioButton("", true);
    
    private JCheckBox enableCache = new JCheckBox(tr("Enable automatic caching."));

    static final int DEFAULT_CACHE_SIZE = 500;
    JLabel jLabelCacheSize = new JLabel(tr("Max. cache size (in MB)"));
    private JTextField cacheSize = new JTextField(20);

    public void addGui(final PreferenceDialog gui) {

        String description = tr("A special handler of the french cadastre wms at www.cadastre.gouv.fr" + "<BR><BR>"
                + "Please read the Terms and Conditions of Use here (in french): <br>"
                + "<a href=\"http://www.cadastre.gouv.fr/scpc/html/CU_01_ConditionsGenerales_fr.html\"> "
                + "http://www.cadastre.gouv.fr/scpc/html/CU_01_ConditionsGenerales_fr.html</a> <BR>"
                + "before any upload of data created by this plugin.");
        JPanel cadastrewms = gui.createPreferenceTab("cadastrewms.gif", I18n.tr("French cadastre WMS"), description);

        // option to automatically set the source tag when uploading
        sourcing.setText(CadastrePlugin.source);
        sourcing.setToolTipText(tr("<html>Value of key \"source\" when autosourcing is enabled</html>"));
        JLabel jLabelSource = new JLabel(tr("Source"));
        cadastrewms.add(jLabelSource, GBC.eop().insets(0, 0, 0, 0));
        cadastrewms.add(sourcing, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        // option to alter the original colors of the wms images
        alterColors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reversGrey.setEnabled(alterColors.isSelected());
                transparency.setEnabled(alterColors.isSelected());
                sliderTrans.setEnabled(transparency.isSelected() && alterColors.isSelected());
            }
        });
        alterColors.setSelected(Main.pref.getBoolean("cadastrewms.alterColors", false));
        alterColors.setToolTipText(tr("Replace the original white background by the backgound color defined in JOSM preferences."));
        cadastrewms.add(alterColors, GBC.eop().insets(0, 0, 0, 0));

        // option to reverse the grey colors (to see texts background)
        reversGrey.setSelected(Main.pref.getBoolean("cadastrewms.invertGrey", false));
        reversGrey.setToolTipText(tr("Invert the original texts from black to white (and all intermediate greys)."));
        reversGrey.setEnabled(alterColors.isSelected());
        cadastrewms.add(reversGrey, GBC.eop().insets(20, 0, 0, 0));

        // option to enable transparency
        transparency.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sliderTrans.setEnabled(transparency.isSelected());
            }
        });
        transparency.setSelected(Main.pref.getBoolean("cadastrewms.backgroundTransparent", false));
        transparency.setToolTipText(tr("Allows multiple layers stacking"));
        transparency.setEnabled(alterColors.isSelected());
        cadastrewms.add(transparency, GBC.eop().insets(20, 0, 0, 0));

        // slider for transparency level
        sliderTrans.setPreferredSize(new Dimension(20,200));
        sliderTrans.setSnapToTicks(true);
        sliderTrans.setToolTipText(tr("Set WMS layers transparency. Right is opaque, left is transparent."));
        sliderTrans.setMajorTickSpacing(10);
        sliderTrans.setMinorTickSpacing(1);
        sliderTrans.setValue((int)(Float.parseFloat(Main.pref.get("cadastrewms.brightness", "1.0f"))*10));
        sliderTrans.setPaintTicks(true);
        sliderTrans.setPaintLabels(false);
        cadastrewms.add(sliderTrans, GBC.eol().fill(GBC.HORIZONTAL).insets(20, 0, 250, 0));
        
        // option to draw boundaries of downloaded data
        drawBoundaries.setSelected(Main.pref.getBoolean("cadastrewms.drawBoundaries", false));
        drawBoundaries.setToolTipText(tr("Draw a rectangle around downloaded data from WMS server."));
        cadastrewms.add(drawBoundaries, GBC.eop().insets(0, 0, 0, 5));

        // the downloaded images multiplier
        JLabel jLabelScale = new JLabel(tr("Image grab multiplier:"));
        cadastrewms.add(jLabelScale, GBC.std().insets(0, 5, 10, 0));
        ButtonGroup bg = new ButtonGroup();
        grabMultiplier1.setIcon(ImageProvider.get("preferences", "unsel_box_1"));
        grabMultiplier1.setSelectedIcon(ImageProvider.get("preferences", "sel_box_1"));
        grabMultiplier2.setIcon(ImageProvider.get("preferences", "unsel_box_2"));
        grabMultiplier2.setSelectedIcon(ImageProvider.get("preferences", "sel_box_2"));
        grabMultiplier3.setIcon(ImageProvider.get("preferences", "unsel_box_3"));
        grabMultiplier3.setSelectedIcon(ImageProvider.get("preferences", "sel_box_3"));
        grabMultiplier4.setIcon(ImageProvider.get("preferences", "unsel_box_4"));
        grabMultiplier4.setSelectedIcon(ImageProvider.get("preferences", "sel_box_4"));
        String multiplierTooltip = "Grab smaller images (higher quality but use more memory)";
        grabMultiplier3.setToolTipText(multiplierTooltip);
        bg.add(grabMultiplier1);
        bg.add(grabMultiplier2);
        bg.add(grabMultiplier3);
        bg.add(grabMultiplier4);
        if (Main.pref.get("cadastrewms.scale", "1").equals(Scale.X1))
            grabMultiplier1.setSelected(true);
        if (Main.pref.get("cadastrewms.scale", "1").equals(Scale.X2))
            grabMultiplier2.setSelected(true);
        if (Main.pref.get("cadastrewms.scale", "1").equals(Scale.X3))
            grabMultiplier3.setSelected(true);
        if (Main.pref.get("cadastrewms.scale", "1").equals(Scale.SQUARE_100M))
            grabMultiplier4.setSelected(true);
        cadastrewms.add(grabMultiplier1, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(grabMultiplier2, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(grabMultiplier3, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(grabMultiplier4, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 5, 0));
        
        // option to enable automatic caching
        enableCache.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jLabelCacheSize.setEnabled(enableCache.isSelected());
                cacheSize.setEnabled(enableCache.isSelected());
            }
        });
        enableCache.setSelected(Main.pref.getBoolean("cadastrewms.enableCaching", true));
        enableCache.setToolTipText(tr("Replace the original white background by the backgound color defined in JOSM preferences."));
        cadastrewms.add(enableCache, GBC.eop().insets(0, 0, 0, 0));

        // option to fix the cache size(in MB)
        int size;
        try {
            size = Integer.parseInt(Main.pref.get("cadastrewms.cacheSize", String.valueOf(DEFAULT_CACHE_SIZE)));
        } catch (NumberFormatException e) {
            size = DEFAULT_CACHE_SIZE;
        }
        cacheSize.setText(String.valueOf(size));
        cacheSize.setToolTipText(tr("Oldest files are automatically deleted when this size is exceeded"));
        cadastrewms.add(jLabelCacheSize, GBC.std().insets(20, 0, 0, 0));
        cadastrewms.add(cacheSize, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 0, 5));

        cadastrewms.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

    }

    public boolean ok() {
        Main.pref.put("cadastrewms.source", sourcing.getText());
        CadastrePlugin.source = sourcing.getText();
        Main.pref.put("cadastrewms.alterColors", alterColors.isSelected());
        Main.pref.put("cadastrewms.invertGrey", reversGrey.isSelected());
        Main.pref.put("cadastrewms.backgroundTransparent", transparency.isSelected());
        Main.pref.put("cadastrewms.brightness", Float.toString((float)sliderTrans.getValue()/10));
        Main.pref.put("cadastrewms.drawBoundaries", drawBoundaries.isSelected());
        if (grabMultiplier1.isSelected())
            Main.pref.put("cadastrewms.scale", Scale.X1.toString());
        else if (grabMultiplier2.isSelected())
            Main.pref.put("cadastrewms.scale", Scale.X2.toString());
        else if (grabMultiplier3.isSelected())
            Main.pref.put("cadastrewms.scale", Scale.X3.toString());
        else
            Main.pref.put("cadastrewms.scale", Scale.SQUARE_100M.toString());
        Main.pref.put("cadastrewms.enableCaching", enableCache.isSelected());
        
        // spread data into objects instead of restarting the application
        try {
            CacheControl.cacheSize = Integer.parseInt(cacheSize.getText());
            Main.pref.put("cadastrewms.cacheSize", String.valueOf(CacheControl.cacheSize));
        } catch (NumberFormatException e) {
            // ignore the last input
        }
        CacheControl.cacheEnabled = enableCache.isSelected();
        CadastrePlugin.refreshConfiguration();

        return false;
    }

}