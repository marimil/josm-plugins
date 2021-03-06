package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;
import reverter.ChangesetReverter.RevertType;

@SuppressWarnings("serial")
public class ChangesetIdQuery extends ExtendedDialog {
    private JFormattedTextField tcid = new JFormattedTextField(NumberFormat.getInstance());
    private ButtonGroup bgRevertType = new ButtonGroup();
    private JRadioButton rbFull = new JRadioButton(tr("Revert changeset fully"));
    private JRadioButton rbSelection = new JRadioButton(tr("Revert selection only"));
    private JRadioButton rbSelectionUndelete =
        new JRadioButton(tr("Revert selection and restore deleted objects"));

    public int getChangesetId() {
        try {
          return NumberFormat.getInstance().parse(tcid.getText()).intValue();
        } catch (ParseException e) {
          return 0;
        }
    }

    public RevertType getRevertType() {
        if (rbFull.isSelected()) return RevertType.FULL;
        if (rbSelection.isSelected()) return RevertType.SELECTION;
        if (rbSelectionUndelete.isSelected()) return RevertType.SELECTION_WITH_UNDELETE;
        return null;
    }

    public ChangesetIdQuery() {
        super(Main.parent, tr("Revert changeset"), new String[] {tr("Revert"),tr("Cancel")}, true);
        contentInsets = new Insets(10,10,10,5);
        setButtonIcons(new String[] {"ok.png", "cancel.png" });
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Changeset id:")));
        panel.add(tcid, GBC.eol().fill(GBC.HORIZONTAL));

        bgRevertType.add(rbFull);
        bgRevertType.add(rbSelection);
        bgRevertType.add(rbSelectionUndelete);

        rbFull.setSelected(true);
        panel.add(rbFull, GBC.eol().insets(0,10,0,0).fill(GBC.HORIZONTAL));
        panel.add(rbSelection, GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(rbSelectionUndelete, GBC.eol().fill(GBC.HORIZONTAL));

        setContent(panel);
        setupDialog();
    }
}
