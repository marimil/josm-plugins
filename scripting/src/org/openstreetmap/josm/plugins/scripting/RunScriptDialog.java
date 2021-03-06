package org.openstreetmap.josm.plugins.scripting;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;
import org.openstreetmap.josm.plugins.scripting.preferences.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.util.IOUtil;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;

/**
 * <p><strong>RunScriptDialog</strong> provides a modal dialog for selecting and
 * running a script.</p> 
 */
public class RunScriptDialog extends JDialog implements PreferenceKeys{
	static private final Logger logger = Logger.getLogger(RunScriptDialog.class.getName());

	/** the input field for the script file name */
	private HistoryComboBox cbScriptFile;
	private Action actRun;
	
	/**
	 * Constructor
	 * 
	 * @param owner the dialog owner 
	 */
	public RunScriptDialog(Component parent) {
		super(JOptionPane.getFrameForComponent(parent), ModalityType.DOCUMENT_MODAL);
		build();
		HelpUtil.setHelpContext(this.getRootPane(), HelpUtil.ht("/Plugin/Scripting"));
	}
	
	protected JPanel buildInfoPanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		HtmlPanel info = new HtmlPanel();
		info.setText(
				"<html>"
			+  	tr("Select a script file and click on <strong>Run</strong>.")
		    +	"</html>"
		);
		pnl.add(info, BorderLayout.CENTER);
		return pnl;
	}
	
	protected JPanel buildControlButtonPanel() {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton btn;
		
		pnl.add(btn = new SideButton(actRun = new RunAction()));
		btn.setFocusable(true);
		btn.registerKeyboardAction(actRun, KeyStroke.getKeyStroke("ENTER"), JComponent.WHEN_FOCUSED);
		pnl.add(new SideButton(new CancelAction()));
		pnl.add(new SideButton(new ContextSensitiveHelpAction(HelpUtil.ht("/Plugin/Scripting#Run"))));
		return pnl;
	}
	
	protected JPanel buildMacroFileInputPanel() {
		JPanel pnl = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0; 
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.insets = new Insets(3,3,3,3);
		gc.fill = GridBagConstraints.BOTH;
		
		pnl.add(new JLabel(tr("File:")), gc);
		
		cbScriptFile = new HistoryComboBox();		
		SelectAllOnFocusGainedDecorator.decorate((JTextField)cbScriptFile.getEditor().getEditorComponent());
		cbScriptFile.setToolTipText(tr("Enter the name of a script file"));
		gc.gridx = 1; 
		gc.weightx = 1.0;
		gc.insets = new Insets(3,3,3,0 /* no spacing to the right */); 
		gc.fill = GridBagConstraints.BOTH;
		pnl.add(cbScriptFile, gc);
		
		gc.gridx = 2; 
		gc.weightx = 0.0;
		gc.fill = GridBagConstraints.BOTH;
		gc.insets = new Insets(3,0 /* no spacing to the left */,3,3);
		JButton btn;
		pnl.add(btn = new JButton(new SelectMacroFileAction()), gc);
		btn.setFocusable(false);
				
		// just a filler 
		JPanel filler = new JPanel();
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth =3;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		gc.fill = GridBagConstraints.BOTH;
		pnl.add(filler, gc);
		
		return pnl;
	}
	
	protected JPanel buildContentPanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.add(buildInfoPanel(), BorderLayout.NORTH);
		pnl.add(buildMacroFileInputPanel(), BorderLayout.CENTER);
		pnl.add(buildControlButtonPanel(), BorderLayout.SOUTH);
		return pnl;
	}
	
	protected void build() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(buildContentPanel(), BorderLayout.CENTER);
		
		getRootPane().registerKeyboardAction(actRun, KeyStroke.getKeyStroke("ctrl ENTER"), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		setTitle(tr("Run a script"));
		setSize(600, 150);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				cbScriptFile.requestFocusInWindow();
			}			
		});
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			/*
			 * Restore the file history and the last entered script file name
			 * from preferences
			 */
	        List<String> fileHistory = new LinkedList<String>(
	        		Main.pref.getCollection(PREF_KEY_FILE_HISTORY, new LinkedList<String>())
	        );
	        Collections.reverse(fileHistory);
	        cbScriptFile.setPossibleItems(fileHistory);
	        String lastFile = Main.pref.get(PREF_KEY_LAST_FILE);
	        if (lastFile != null && !lastFile.trim().isEmpty()){
	        	cbScriptFile.setText(lastFile.trim());
	        }
	        WindowGeometry.centerInWindow(getParent(),new Dimension(600,150)).applySafe(this);
		} else {
			/*
			 * Persist the file history script file name
			 * in the preferences
			 */
			String currentFile = cbScriptFile.getText();
			Main.pref.put(PREF_KEY_LAST_FILE, currentFile.trim());
			Main.pref.putCollection(PREF_KEY_FILE_HISTORY, cbScriptFile.getHistory());			
		}
		super.setVisible(visible);
	}

	private class CancelAction extends AbstractAction {
		public CancelAction() {
			putValue(NAME, tr("Cancel"));
			putValue(SHORT_DESCRIPTION, tr("Cancel"));
			putValue(SMALL_ICON, ImageProvider.get("cancel"));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			setVisible(false);
		}				
	}
	
	private class RunAction extends AbstractAction {
		public RunAction() {
			putValue(NAME, tr("Run"));
			putValue(SHORT_DESCRIPTION, tr("Run the script"));
			putValue(SMALL_ICON, ImageProvider.get("run"));
		}
		
		protected void warnMacroFileDoesntExist(File f){			
			HelpAwareOptionPane.showOptionDialog(
					null,
					tr("The script file ''{0}'' doesn''t exist.", f.toString()),
					tr("File not found"),
					JOptionPane.ERROR_MESSAGE,
					HelpUtil.ht("/Plugin/Scripting")
			);			
		}
		
		protected void warnEmptyFile(){			
			HelpAwareOptionPane.showOptionDialog(
					cbScriptFile,
					tr("Please enter a file name first."),
					tr("Empty file name"),
					JOptionPane.ERROR_MESSAGE,
					HelpUtil.ht("/Plugin/Scripting")
			);			
		}
		
		protected void warnMacroFileIsntReadable(File f){
			HelpAwareOptionPane.showOptionDialog(
					RunScriptDialog.this,
					tr("The script file ''{0}'' isn''t readable.", f.toString()),
					tr("File not readable"),
					JOptionPane.ERROR_MESSAGE,
					HelpUtil.ht("/Plugin/Scripting")
			);			
		}
		
		protected void warnFailedToOpenMacroFile(File f, Exception e){
			HelpAwareOptionPane.showOptionDialog(
					RunScriptDialog.this,
					tr("Failed to read the script from the file ''{0}''.", f.toString()),
					tr("IO error"),
					JOptionPane.ERROR_MESSAGE,
					HelpUtil.ht("/Plugin/Scripting")
			);			
			System.out.println(tr("Failed to read a macro from the file ''{0}''.", f.toString()));
			e.printStackTrace();			
		}
		
		protected void warnExecutingScriptFailed(ScriptException e){
			HelpAwareOptionPane.showOptionDialog(
					RunScriptDialog.this,
					tr("Script execution has failed."),
					tr("Script Error"),
					JOptionPane.ERROR_MESSAGE,
					HelpUtil.ht("/Plugin/Scripting")
			);			
			System.out.println(tr("Macro execution has failed."));
			e.printStackTrace();
		}
		
		protected void warnNoScriptingEnginesInstalled() {
			HelpAwareOptionPane.showOptionDialog(
					RunScriptDialog.this,
					"<html>"
					+ tr(
						"<p>The script can''t be executed, because there are currently no scripting engines installed.</p>"
						+ "<p>Refer to the online help for information about how to install a scripting engine with JOSM.</p>"						
					)					
					+ "</html>"
					,
					tr("No script engine"),
					JOptionPane.ERROR_MESSAGE,
					HelpUtil.ht("/Plugin/Scripting")
			);
		}
		
		protected ScriptEngine getScriptEngine(File file) {
			ScriptEngine engine = ScriptEngineProvider.getInstance().getEngineForFile(file);
			if (engine != null) return engine;
			
			// let the user select a script engine
			return ScriptEngineSelectionDialog.select(RunScriptDialog.this); 
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			String fileName = cbScriptFile.getText().trim();
			if (fileName.isEmpty()){
				warnEmptyFile();
				return;
			}
			final File f = new File(fileName);
			if (! f.exists() || !f.isFile()) {
				warnMacroFileDoesntExist(f);
				return;
			} else if (!f.canRead()) {
				warnMacroFileIsntReadable(f);
				return;
			}
			
			cbScriptFile.addCurrentItemToHistory();
			
			try {
				new FileReader(f);
			} catch(IOException e){
				warnFailedToOpenMacroFile(f, e);
				return;
			} 			
			final ScriptEngine engine = getScriptEngine(f);
			if (engine == null) return;
			setVisible(false);		

			SwingUtilities.invokeLater(
			    new Runnable() {
			    	public void run() {			
			    		FileReader reader = null;
						try {
							if (engine instanceof Compilable) {
								CompiledScript script = CompiledScriptCache.getInstance().compile((Compilable)engine,f);
								script.eval();
							} else {
								reader = new FileReader(f);								
								engine.eval(reader);
							}
						} catch(ScriptException e){
							warnExecutingScriptFailed(e);
						} catch(IOException e){
							warnFailedToOpenMacroFile(f, e);
						} finally {
							IOUtil.close(reader);
						}
			    	}
			    }
		    );		
		}	
	}
	
	private class SelectMacroFileAction extends AbstractAction {
		public SelectMacroFileAction() {
			putValue(NAME, tr("..."));
			putValue(SHORT_DESCRIPTION, tr("Launch file selection dialog"));
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			String fileName = cbScriptFile.getText().trim();
			File currentFile = null;
			if (! fileName.isEmpty()) {
				currentFile = new File(fileName);
			}
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(tr("Select a script"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			if (currentFile != null){
				chooser.setCurrentDirectory(currentFile);
				chooser.setSelectedFile(currentFile);
			}
			int ret = chooser.showOpenDialog(RunScriptDialog.this);			
			if (ret != JFileChooser.APPROVE_OPTION) return;
			
			currentFile = chooser.getSelectedFile();
			cbScriptFile.setText(currentFile.toString());			
		}	
	}
}
