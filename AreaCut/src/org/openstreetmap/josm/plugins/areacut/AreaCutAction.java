package org.openstreetmap.josm.plugins.areacut;

import static org.openstreetmap.josm.tools.I18n.*;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

public class AreaCutAction extends JosmAction {

	private static final long serialVersionUID = 3477176320729125522L;

	public AreaCutAction() {
        super(tr("Area cut"),
                "areaCut",
                tr("Cut area into pieces"),
                Shortcut.registerShortcut("tools:AreaCut",
                        tr("Tool: {0}", tr("Area Cut")),
                        KeyEvent.VK_C, Shortcut.GROUP_EDIT,
                        Shortcut.SHIFT_DEFAULT),
                        true);
    }

    public void actionPerformed(ActionEvent e) {
        Collection<Way> selectedWays = Main.main.getCurrentDataSet().getSelectedWays();
        Iterator i=selectedWays.iterator();
        Way line=null;
        Way area=null;
        while(i.hasNext()){
        	Way w=(Way)i.next();
        	if(w.getNode(0)!=w.getNode(w.getNodesCount()-1)){
        		if(line==null)
        			line=w;
        	}        		
        	else{
        		if(area==null)
        			area=w;
        	}
        }
        if(area==null||line==null)return;
        
        List<Node> nodes1=line.getNodes();
        List<Node> nodes2=area.getNodes();        
        
        //looking for node in nodes2==nodes1[0]
        i=nodes2.iterator();
        
        List <Node> newArea1=new ArrayList<Node>();
        List <Node> newArea2=new ArrayList<Node>();
        
        
        
        int g=0;
        while(i.hasNext()){
        	Node n=(Node) i.next();
        	System.out.println("ITERATE");
        	 if((n==nodes1.get(0)||n==nodes1.get(nodes1.size()-1))&&g==0){
        		System.out.println("First cross");
        		g=1;
        	}else if(n==nodes1.get(nodes1.size()-1)&&g==1){
    			System.out.println("Secend cross");
        		for(int j=0;j<nodes1.size()-1;j++){
                	newArea1.add(nodes1.get(j));
                	System.out.println("Added to 1 from way");                	
                }
        		for(int j=nodes1.size()-1;j>=0;j--){
                	newArea2.add(nodes1.get(j));
                	System.out.println("Added to 2 from way");
                }          		
        		g=2;
        	}
    		else if(n==nodes1.get(0)&&g==1){
        		
        		System.out.println("Secend cross");
        		for(int j=nodes1.size()-1;j>0;j--){
                	newArea1.add(nodes1.get(j));
                	System.out.println("Added to 1 from way");                	
                }
        		for(int j=0;j<nodes1.size();j++){
                	newArea2.add(nodes1.get(j));
                	System.out.println("Added to 2 from way");
                }   
        		g=2;
    		}
        	
        		if(g==0||g==2){
        			newArea1.add(n);
            		System.out.println("Added to 1");
        		}else{
        			newArea2.add(n);
            		System.out.println("Added to 2");
        		}
        	
        }
        
        if(newArea1.size()>0)
        	line.setNodes(newArea1);
        if(newArea2.size()>0)
        	area.setNodes(newArea2);
        
        
        Collection<Command> commands = new LinkedList<Command>();
        commands.add(new ChangePropertyCommand(
                line,"",null));
        commands.add(new ChangePropertyCommand(
                area,"",null));
        
        Main.main.undoRedo.add(new SequenceCommand(tr("AreaCut"), commands));        
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getCurrentDataSet() != null);
    }
}
