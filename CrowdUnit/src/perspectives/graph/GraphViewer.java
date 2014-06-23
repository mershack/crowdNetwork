package perspectives.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import perspectives.base.Property;
import perspectives.base.PropertyManager;
import perspectives.base.Task;
import perspectives.two_d.Viewer2D;
import perspectives.properties.PBoolean;
import perspectives.properties.PColor;
import perspectives.properties.PDouble;
import perspectives.properties.PInteger;
import perspectives.properties.PString;
import perspectives.properties.PFileInput;
import perspectives.properties.PFileOutput;
import perspectives.base.PropertyType;
import perspectives.base.Animation;
import perspectives.base.DataSource;
import perspectives.util.BubbleSets;
import perspectives.util.Label;
import perspectives.util.Oval;
import perspectives.util.Rectangle;
import perspectives.util.Util;

import perspectives.base.ObjectInteraction;

public class GraphViewer extends Viewer2D {

    protected Graph graph;
    protected GraphDrawer drawer;
    int[] edgeSources;
    int[] edgeTargets;
    ObjectInteraction ovalInteraction;
    protected ArrayList<Rectangle> ovals;
    protected Task initTask;
    String dataFilePath = "";
    boolean firstOutputUpdate = true;
    protected Color nodeColor = Color.blue;
    String dataSourceName;
    protected int nodeSize = 22;
    boolean isUserStudy = false;
    boolean isFirstRender = true;

    public void updatePropertiesOutputFile(String type, String name, String value) {

        String fileName = this.getName();

//         String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();

        File outputFile = new File("data/" + fileName + ".txt");

        try {
            if (!isUserStudy) {
                if (firstOutputUpdate) {
                    //delete the file if it exists and write the value to it

                    if (outputFile.exists()) {
                        outputFile.delete();
                    }

                    //create new File
                    outputFile.createNewFile();

                    firstOutputUpdate = false;
                }

                FileWriter fileWritter = new FileWriter(outputFile, true);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

                PrintWriter pw = new PrintWriter(bufferWritter);

                pw.println(type + "," + name + "," + value);

                pw.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void updateDefaultPropertiesToOutputFile() {
        updatePropertiesOutputFile("PInteger", "Appearance.Node Size", "22");
        updatePropertiesOutputFile("PColor", "Appearance.Node Color", "0-0-255-255");
    }

    public String getDataFilePath() {
        return dataFilePath;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public GraphViewer(String name, GraphData g) {
        super(name);

        graph = g.graph;
        dataSourceName = g.getName();
        dataFilePath = g.getFilePath();
        final GraphViewer gv = this;

        this.addGraphProperties(gv);

        initTask = new Task("Initializing") {
            public void task() {
                ovals = new ArrayList<Rectangle>();
                ovalInteraction = new ObjectInteraction() {
                    @Override
                    protected void mouseIn(int obj) {
                        gv.setTooltipContent(obj);
                        // System.out.println(" ----- scheduling node animation");
                        final int o = obj;
                        //gv.createAnimation(new Animation.IntegerAnimation(22, 30, 500) {
                        gv.createAnimation(new Animation.IntegerAnimation(nodeSize, nodeSize + 8, 500) {
                            public void step(int v) {
                                //	System.out.println(" ----- node animation step + " + v);
                                Rectangle l = ((RectangleItem) ovalInteraction.getItem(o)).r;
                                l.w = v;
                                l.h = v;
                                requestRender();
                            }
                        });
                    }

                    protected void mouseOut(int obj) {
                        gv.setToolTipText("");
                        final Rectangle l = ((RectangleItem) ovalInteraction.getItem(obj)).r;
                        // gv.createAnimation(new Animation.IntegerAnimation(30, nodeSize, 300) {
                        gv.createAnimation(new Animation.IntegerAnimation(nodeSize + 8, nodeSize, 300) {
                            public void step(int v) {
                                l.w = v;
                                l.h = v;
                                gv.requestRender();
                            }
                        });
                    }

                    protected void itemDragged(int item, Point2D delta) {
                        gv.drawer.setX(item, gv.drawer.getX(item) + (int) delta.getX());
                        gv.drawer.setY(item, gv.drawer.getY(item) + (int) delta.getY());
                        gv.requestRender();
                    }

                    @Override
                    protected void itemsSelected(int[] obj) {
                        gv.requestRender();
                    }

                    @Override
                    protected void itemsDeselected(int[] obj) {
                        gv.requestRender();
                    }
                };

                ArrayList<String> nodes = graph.getNodes();
                for (int i = 0; i < nodes.size(); i++) {
                    Oval o = new Oval(0, 0, 22, 22);
                    ovals.add(o);
                    ovalInteraction.addItem(ovalInteraction.new RectangleItem(o));
                }

                ArrayList<Integer> e1 = new ArrayList<Integer>();
                ArrayList<Integer> e2 = new ArrayList<Integer>();
                graph.getEdgesAsIndeces(e1, e2);

                edgeSources = new int[e1.size()];
                edgeTargets = new int[e2.size()];
                for (int i = 0; i < e1.size(); i++) {
                    edgeSources[i] = e1.get(i);
                    edgeTargets[i] = e2.get(i);
                }

                drawer = new BarnesHutGraphDrawer(graph);
                ((BarnesHutGraphDrawer) drawer).setSpringLength(300.);
                ((BarnesHutGraphDrawer) drawer).max_step = 200.;

                done();
                requestRender();
            }
        };

        initTask.indeterminate = true;
        initTask.blocking = true;
        this.startTask(initTask);

    }

    public void addGraphProperties(final GraphViewer gv) {
        try {
            Property<PFileInput> p1 = new Property<PFileInput>("Load Positions", new PFileInput()) {
                @Override
                public boolean updating(PFileInput newvalue) {
                    loadPositions(newvalue.path);

                    updatePropertiesOutputFile("PFileInput", "Load Positions", newvalue.serialize());
                    return true;
                }
            };
            gv.addProperty(p1);

            Property<PDouble> p2 = new Property<PDouble>("Simulation.SPRING_LENGTH", new PDouble(300.)) {
                @Override
                public boolean updating(PDouble newvalue) {
                    ((BarnesHutGraphDrawer) drawer).setSpringLength(newvalue.doubleValue());
                    updatePropertiesOutputFile("PDouble", "Simulation.SPRING_LENGTH", newvalue.serialize());
                    return true;
                }
            };
            gv.addProperty(p2);

            Property<PDouble> p3 = new Property<PDouble>("Simulation.MAX_STEP", new PDouble(100.)) {
                @Override
                public boolean updating(PDouble newvalue) {
                    ((BarnesHutGraphDrawer) drawer).max_step = newvalue.doubleValue();
                    updatePropertiesOutputFile("PDouble", "Simulation.SPRING_LENGTH", newvalue.serialize());
                    return true;
                }
            };

            gv.addProperty(p3);

            Property<PBoolean> p4 = new Property<PBoolean>("Simulation.Simulate", new PBoolean(false)) {
                public boolean updating(PBoolean newvalue) {
                    if (newvalue.boolValue()) {
                        gv.startSimulation(50);
                    } else {
                        gv.stopSimulation();
                    }

                    updatePropertiesOutputFile("PBoolean", "Simulation.Simulate", newvalue.serialize());
                    return true;
                }
            };
            gv.addProperty(p4);

            Property<PFileOutput> p5 = new Property<PFileOutput>("Save", new PFileOutput()) {
            };
            gv.addProperty(p5);

            PFileOutput f = new PFileOutput();
            Property<PFileOutput> p6 = new Property<PFileOutput>("Save Positions", f) {
                @Override
                public boolean updating(PFileOutput newvalue) {
                    savePositions(newvalue.path);
                    return true;
                }
            };
            gv.addProperty(p6);

            Property<PInteger> p7 = new Property<PInteger>("Appearance.Node Size", new PInteger(22)) {
                @Override
                public boolean updating(PInteger newvalue) {
                    int s = newvalue.intValue();
                    //setNodeSize(s);
                    nodeSize = s;
                    for (int i = 0; i < ovals.size(); i++) {
                        ovals.get(i).h = s;
                        ovals.get(i).w = s;
                    }
                    gv.requestRender();
                    updatePropertiesOutputFile("PInteger", "Appearance.Node Size", newvalue.serialize());

                    return true;
                }

                @Override
                protected void receivedBroadcast(PInteger newvalue, PropertyManager sender) {
                    this.setValue(newvalue);
                }
            };
            p7.setPublic(true);
            addProperty(p7);

            Property<PColor> p8 = new Property<PColor>("Appearance.Node Color", new PColor(Color.blue)) {
                @Override
                protected void receivedBroadcast(PColor newvalue, PropertyManager sender) {
                    this.setValue(newvalue);

                }

                @Override
                public boolean updating(PColor newvalue) {
                    nodeColor = ((PColor) newvalue).colorValue();
                    gv.requestRender();

                    updatePropertiesOutputFile("PColor", "Appearance.Node Color", newvalue.serialize());
                    return true;
                }
            };
            p8.setPublic(true);
            addProperty(p8);


            Property<PString> p99 = new Property<PString>("Selected", new PString("")) {
                @Override
                protected boolean updating(PString newvalue) {

                    //System.out.println("selecting a node " + newvalue);
                    ArrayList<String> nodes = graph.getNodes();
                    int index = nodes.indexOf(newvalue.stringValue());
                    ovalInteraction.getItem(index).selected = true;
                    return true;
                }
            };
            gv.addProperty(p99);
            p99.setPublic(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetOvalInteraction() {
        ArrayList<String> nodes = graph.getNodes();

        for (int i = 0; i < ovals.size(); i++) {
            //Oval o = new Oval(0, 0, 22, 22);
            //ovals.add(o);
            ovalInteraction.addItem(ovalInteraction.new RectangleItem(ovals.get(i)));
        }
    }

    public void simulate() {

        System.out.println("simulate");

        if (!initTask.done) {
            return;
        }

        long t = new Date().getTime();
        for (int i = 0; i < 2; i++) {
            drawer.iteration();
        }
        for (int i = 0; i < ovals.size(); i++) {
            ovals.get(i).x = this.drawer.getX(i);
            ovals.get(i).y = this.drawer.getY(i);
        }
        this.requestRender();
    }
    ArrayList<Integer> pairs1 = new ArrayList<Integer>();
    ArrayList<Integer> pairs2 = new ArrayList<Integer>();

    @Override
    public void render(Graphics2D g) {
        if (!initTask.done) {
            return;
        }
        
        if(isFirstRender){
            updateDefaultPropertiesToOutputFile();
            isFirstRender = false;
        }
        long t1 = new Date().getTime();

        ArrayList<String> nodes = graph.getNodes();
        boolean[] hov = new boolean[nodes.size()];
        boolean[] sel = new boolean[nodes.size()];

        //   Color nodeColor = ((PColor) (getProperty("Appearance.Node Color").getValue())).colorValue();
        ArrayList<Integer> selindex = new ArrayList<Integer>();
        for (int i = 0; i < nodes.size(); i++) {
            ObjectInteraction.VisualItem item = ovalInteraction.getItem(i);

            if (item.selected) {
                ovals.get(i).setColor(Color.gray);
                sel[i] = true;
                selindex.add(i);
            } else if (item.hovered) {
                ovals.get(i).setColor(Color.lightGray);
                hov[i] = true;
            } else {
                ovals.get(i).setColor(nodeColor);
            }

        }

        this.renderNodes(g, sel, hov);

        long t2 = new Date().getTime();

        renderEdges(g, sel, hov);

        long t3 = new Date().getTime();

        //	System.out.println("render times " + (t2-t1) + " " + (t3-t2));
        if (selindex.size() == 2) {
            boolean found = false;
            for (int i = 0; i < pairs1.size(); i++) {
                if ((pairs1.get(i).intValue() == selindex.get(0).intValue() && pairs2.get(i).intValue() == selindex.get(1).intValue())
                        || (pairs1.get(i).intValue() == selindex.get(0).intValue() && pairs1.get(i).intValue() == selindex.get(1).intValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                pairs1.add(selindex.get(0));
                pairs2.add(selindex.get(1));
                System.out.println(selindex.get(0) + " \t" + selindex.get(1));
            }
        }

    }

    public void renderNodes(Graphics2D g, boolean sel[], boolean[] hov) {
        for (int i = 0; i < sel.length; i++) {
            renderNode(i, sel[i], hov[i], g);
        }
    }

    public void renderNode(int i, boolean selected, boolean hovered, Graphics2D g) {
        if (selected) {
            ovals.get(i).setColor(Color.gray);
        } else if (hovered) {
            ovals.get(i).setColor(Color.lightGray);
        }
        ovals.get(i).render(g);

    }

    public void renderEdges(Graphics2D g, boolean[] sel, boolean[] hov) {
        ArrayList<Integer> e1 = new ArrayList<Integer>();
        ArrayList<Integer> e2 = new ArrayList<Integer>();
        graph.getEdgesAsIndeces(e1, e2);
        for (int i = 0; i < e1.size(); i++) {
            int index1 = e1.get(i);
            int index2 = e2.get(i);

            renderEdge(index1, index2, i, sel[index1] || sel[index2], hov[index1] || hov[index2], g);

        }
    }

    protected void setTooltipContent(int index) {
        // TODO Auto-generated method stub
        this.setToolTipText("" + index);
    }

    public void renderEdge(int p1, int p2, int edgeIndex, boolean selected, boolean hovered, Graphics2D g) {
        int x1 = (int) drawer.getX(p1);
        int y1 = (int) drawer.getY(p1);
        int x2 = (int) drawer.getX(p2);
        int y2 = (int) drawer.getY(p2);

        //NB: commented below to prevent highlighting of edges. Comment can be removed
        //      to reverse it later.
        if (selected) {
            g.setColor(new Color(255, 0, 0, 200));
        } else if (hovered) {
            g.setColor(new Color(255, 100, 100, 200));
        } else {
            g.setColor(new Color(150, 150, 150, 200));
        }

        // g.setColor(new Color(150, 150, 150, 200));
        g.setStroke(new BasicStroke(2));

        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public boolean mousepressed(int x, int y, int button) {
        ovalInteraction.mousePress(x, y);
        return false;
    }

    @Override
    public boolean mousereleased(int x, int y, int button) {
        ovalInteraction.mouseRelease(x, y);
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean mousemoved(int x, int y) {
        boolean ret = ovalInteraction.mouseMove(x, y);

        return ret;
        // TODO Auto-generated method stub

    }

    @Override
    public boolean mousedragged(int x, int y, int px, int py) {
        boolean ret;
        ret = ovalInteraction.mouseMove(x, y);
        return ret;
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(String key, String mod) {
        System.out.println("graph viewer keypress: " + key + " , " + mod);
        ovalInteraction.ctrlPress();
        // TODO Auto-generated method stub
        super.keyPressed(key, mod);
    }

    @Override
    public void keyReleased(String key, String mod) {
        ovalInteraction.ctrlRelease();
        // TODO Auto-generated method stub
        super.keyReleased(key, mod);
    }

    public void loadPositions(String path) {
        ArrayList<String> nodes = graph.getNodes();

        try {
            FileInputStream fstream = new FileInputStream(path);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String s;
            while ((s = br.readLine()) != null) {
                s = s.trim();

                String[] split = s.split("\t");

                if (split.length < 2) {
                    continue;
                }

                int index = nodes.indexOf(split[0].trim());
                if (index < 0) {
                    String s2 = split[0].trim().toLowerCase();
                    for (int i = 0; i < nodes.size(); i++) {
                        String s1 = nodes.get(i).toLowerCase();
                        if (s1.equals(s2)) {
                            index = i;
                            break;
                        }
                    }
                    if (index < 0) {
                        continue;
                    }
                }

                String[] poss = split[1].split(",");
                double x = Double.parseDouble(poss[0]);
                double y = Double.parseDouble(poss[1]);

                //x = x*1.335;
                //y = -y*1.335;
                if (drawer != null) {
                    drawer.setX(index, (int) (x));
                    drawer.setY(index, (int) (y));

                    ovals.get(index).x = (int) (x);
                    ovals.get(index).y = (int) (y);
                    ovals.get(index).setAnchor(-5, -5);

                }
            }

            in.close();

            this.requestRender();

        } catch (Exception e) {
        }

    }

    public void savePositions(String path) {
        ArrayList<String> nodes = graph.getNodes();
        try {
            FileWriter fstream;

            fstream = new FileWriter(new File(path));

            BufferedWriter br = new BufferedWriter(fstream);

            for (int i = 0; i < nodes.size(); i++) {
                br.write(nodes.get(i) + "\t" + drawer.getX(i) + "," + drawer.getY(i));
                br.newLine();
            }

            br.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void save(File f) {
        ArrayList<String> nodes = graph.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            int x = (int) drawer.getX(i);
            int y = (int) drawer.getY(i);

            Property<PInteger> px = graph.nodeProperty(nodes.get(i), "x");
            if (px == null) {
                px = new Property<PInteger>("x", new PInteger(x));
                graph.addNodeProperty(nodes.get(i), px);
            }
            px.setValue(new PInteger(x));

            Property<PInteger> py = graph.nodeProperty(nodes.get(i), "y");
            if (py == null) {
                py = new Property<PInteger>("y", new PInteger(y));
                graph.addNodeProperty(nodes.get(i), py);
            }
            py.setValue(new PInteger(y));

        }
        graph.toGraphML(f);
    }

    public void load(File f) {
        graph.fromGraphML(f);

        ArrayList<String> nodes = graph.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            Property<PInteger> p = graph.nodeProperty(nodes.get(i), "x");
            if (p != null) {
                drawer.setX(i, p.getValue().intValue());
            } else {
                drawer.setX(i, 0);
            }

            p = graph.nodeProperty(nodes.get(i), "y");
            if (p != null) {
                drawer.setY(i, p.getValue().intValue());
            } else {
                drawer.setY(i, 0);
            }
        }
    }

    public int[] getNodeCoordinates(int index) {
        return new int[]{(int) drawer.getX(index), (int) drawer.getY(index)};
    }

    public void setNodeCoordinates(int index, int x, int y) {
        drawer.setX(index, x);
        drawer.setY(index, y);
        ovals.get(index).x = x;
        ovals.get(index).y = y;
    }

    public boolean isIsUserStudy() {
        return isUserStudy;
    }

    public void setIsUserStudy(boolean isUserStudy) {
        this.isUserStudy = isUserStudy;
    }
}
