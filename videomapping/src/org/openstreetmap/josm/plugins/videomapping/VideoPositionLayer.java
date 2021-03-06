package org.openstreetmap.josm.plugins.videomapping;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.videomapping.video.GPSVideoPlayer;

//Basic rendering and GPS layer interaction
public class VideoPositionLayer extends Layer implements MouseListener,MouseMotionListener {
	private List<WayPoint> gpsTrack;
	private ImageIcon layerIcon;
	private DateFormat gpsTimeFormat;
	private WayPoint iconPosition;
	private final int GPS_INTERVALL=1000;

	public VideoPositionLayer(GpxLayer gpsLayer) {
		super("videolayer");
		layerIcon = new ImageIcon("images/videomapping.png");
		gpsTrack=importGPSLayer(gpsLayer.data);
		gpsTimeFormat= new SimpleDateFormat("HH:mm:ss");
		Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        iconPosition=gpsTrack.get(0);
        Main.main.addLayer(this);        
		
	}

	//make a flat copy
	private List<WayPoint> importGPSLayer(GpxData gps) {
		LinkedList<WayPoint> ls = new LinkedList<WayPoint>();
        for (GpxTrack trk : gps.tracks) {
            for (GpxTrackSegment segment : trk.getSegments()) {
                ls.addAll(segment.getWayPoints());
            }
        }
        Collections.sort(ls); //sort basing upon time
        return ls;
		
	}

	@Override
	public void paint(Graphics2D g, MapView map, Bounds bound) {
		paintGpsTrack(g);
		paintPositionIcon(g);
		paintInterpolatedSegment(g);
	}

	private void paintGpsTrack(Graphics2D g) {
		g.setColor(Color.YELLOW);
        for(WayPoint n: gpsTrack) {
            Point p = Main.map.mapView.getPoint(n.getEastNorth());
            g.drawOval(p.x - 2, p.y - 2, 4, 4);
        }
	}
	
	private void paintPositionIcon(Graphics2D g) {
		Point p=Main.map.mapView.getPoint(iconPosition.getEastNorth());
        layerIcon.paintIcon(null, g, p.x-layerIcon.getIconWidth()/2, p.y-layerIcon.getIconHeight()/2);
        g.drawString(gpsTimeFormat.format(iconPosition.getTime()),p.x-15,p.y-15);
	}
	
	private void paintInterpolatedSegment(Graphics2D g) {
		g.setColor(Color.CYAN);
		List<WayPoint>ls=getInterpolatedSegment(iconPosition,5,5);
        for(WayPoint n: ls) {
            Point p = Main.map.mapView.getPoint(n.getEastNorth());
            g.drawOval(p.x - 2, p.y - 2, 4, 4);
        }
		
	}

	private List<WayPoint> getInterpolatedSegment(WayPoint center, int before, int after) {
		LinkedList<WayPoint> ls = new LinkedList<WayPoint>();
		if(gpsTrack.indexOf(iconPosition)!=0)
		{
			WayPoint prev=gpsTrack.get(gpsTrack.indexOf(iconPosition)-1);
			for(int i=1;i<=before;i++)
			{
				ls.add(interpolate(prev,(float)100f/before*i));
			}
		}
		for(int i=1;i<=after;i++)
		{
			ls.add(interpolate(iconPosition,(float)100f/before*i));
		}
		//test code
		Date test=getFirstWayPoint().getTime();
		test.setHours(14);
		test.setMinutes(50);
		test.setSeconds(33);
		
		ls.add(getWayPointBefore(new Date(test.getTime()+500)));
		ls.add(interpolate(new Date(test.getTime()+500)));
		System.out.print(test);
		return ls;
	}
	
	//creates a waypoint for the corresponding time
	private WayPoint interpolate(Date GPSTime)
	{
		WayPoint before =getWayPointBefore(GPSTime);
		long diff=GPSTime.getTime()-before.getTime().getTime();
		assert diff>=0;
		assert diff<GPS_INTERVALL;
		float perc=((float)diff/(float)GPS_INTERVALL)*100;		
		return interpolate(before,perc);
	}
	
	private WayPoint getWayPointBefore(Date GPSTime)
	{
		assert GPSTime.after(getFirstWayPoint().getTime())==true;
		assert GPSTime.before(getLastWayPoint().getTime())==true;
		
		Date first=getFirstWayPoint().getTime();
		long diff=GPSTime.getTime()-first.getTime();
		//assumes that GPS intervall is constant
		int id=(int) (diff/GPS_INTERVALL);		
		return gpsTrack.get(id);
	}
	
	public WayPoint getFirstWayPoint()
	{
		return gpsTrack.get(0);
	}
	
	public WayPoint getLastWayPoint()
	{
		return gpsTrack.get(gpsTrack.size()-1);
	}

	//interpolates a waypoint between this and the following waypoint at percent
	private WayPoint interpolate(WayPoint first, float percent) {
		assert (percent>0);
		assert (percent<100);
		double dX,dY;
        WayPoint leftP,rightP;
        
        
        WayPoint next=gpsTrack.get(gpsTrack.indexOf(first)+1);       
        //determine which point is what
        leftP=getLeftPoint(first, next);
        rightP=getRightPoint(first,next);
        //calc increment
        percent=percent/100;
        dX=(rightP.getCoor().lon()-leftP.getCoor().lon())*percent;
        dY=(rightP.getCoor().lat()-leftP.getCoor().lat())*percent;
        //move in the right direction
        if (first==leftP)
        {
        	return new WayPoint(new LatLon(leftP.getCoor().lat()+dY,leftP.getCoor().lon()+dX));
        }
        else
        	 return new WayPoint(new LatLon(rightP.getCoor().lat()-dY,rightP.getCoor().lon()-dX));

        
	}
	
	private WayPoint getLeftPoint(WayPoint p1,WayPoint p2)
    {
        if(p1.getCoor().lon()<p2.getCoor().lon()) return p1; else return p2;
    }
    
    private WayPoint getRightPoint(WayPoint p1, WayPoint p2)
    {
        if(p1.getCoor().lon()>p2.getCoor().lon()) return p1; else return p2;
    }

	public void mouseReleased(MouseEvent e) {
		//only leftclicks on our layer
        if(e.getButton() == MouseEvent.BUTTON1) {
        	WayPoint wp = getNearestWayPoint(e.getPoint());            	
            if(wp!=null)
            {
            	iconPosition=wp;
            }
            Main.map.mapView.repaint();
        }
		
	}
	
	//finds the first waypoint that is nearby the given point
    private WayPoint getNearestWayPoint(Point mouse)
    {
        final int MAX=10;
        Point p;
        Rectangle rect = new Rectangle(mouse.x-MAX/2,mouse.y-MAX/2,MAX,MAX);
        //iterate through all possible notes
        for(WayPoint n : gpsTrack)
        {
            p = Main.map.mapView.getPoint(n.getEastNorth());
            if (rect.contains(p))
            {               
                return n;
            }
            
        }
        return null;
        
    }

	@Override
	public Icon getIcon() {
		return layerIcon;
	}

	@Override
	public Object getInfoComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action[] getMenuEntries() {
		return new Action[]{
                LayerListDialog.getInstance().createActivateLayerAction(this),
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this)};
	}


	@Override
	public String getToolTipText() {
		return tr("Shows current position in the video");
	}

	@Override
	public boolean isMergable(Layer arg0) {		
		return false;
	}

	@Override
	public void mergeFrom(Layer arg0) {
		
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
    
}
