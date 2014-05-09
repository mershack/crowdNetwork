package graphEvaluation;

import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import perspectives.base.Property;
import perspectives.base.PropertyType;
import perspectives.properties.PBoolean;
import perspectives.properties.PFileInput;
import perspectives.graph.GraphData;
import perspectives.graph.GraphViewer;
import perspectives.properties.PButton;
import perspectives.properties.PFileOutput;
import perspectives.properties.PString;
import perspectives.properties.PText;
import userstudy.UserStudyUtility;
//import util.Points2DViewer.PointAspectType;

public class GraphUserStudyViewer extends GraphViewer {

    ArrayList<Integer> pointA;
    ArrayList<Integer> pointB;
    //final String TASKFILE = "tasks.txt";
    final String TASKFILE = "tasks2.txt";
    private String propChangesFile = "viewer11.txt";

    int testCounter = 0;
    String answer;
    Property<PBoolean> panswer_no;
    Property<PBoolean> panswer_yes;
    Property<PText> ptask;
    ArrayList<GraphTask> allTasks = new ArrayList<GraphTask>();

    int showedNode1, showedNode2;

    boolean firstRender = true;
    boolean secondRender = true;
    int cnt = 0;
    int testNodeA = -1;
    int testNodeB = -1;
    Color testNodeColor = Color.red;
    Color OtherNodesColor = Color.lightGray;

    Property<PString> ptask_status;

    /////////////////////////////                 Constructor             //////////////////////////////
    public GraphUserStudyViewer(String name, GraphData g) {

        super(name, g);
        //readPropChangesFileAndUpdate();

        Property<PButton> pstart = new Property<PButton>("Start", new PButton()) {
            @Override
            public boolean updating(PButton newvalue) {
                startStudy();
                return true;
            }
        };
        this.addProperty(pstart);

        loadTasks();
    }

    public void readPropChangesFileAndUpdate() {
        try {

            System.out.println("The file name is : "+ propChangesFile);
            File propFile = new File(propChangesFile);

            if (propFile.exists()) {//Do the property changes if the file exists

                BufferedReader br = new BufferedReader(new FileReader(propFile));
                String line = " ";
                String split[];

                Property prop = null;
                PropertyType propValue = null;
                while ((line = br.readLine()) != null) {

                    split = line.split(",");
                    //NB: each line in the file is of the format PropertyType, PropertyName, PropertyValue
                    //E.g: PInteger,Appearance.Node Size,50
                    prop = this.getProperty(split[1]);
                    propValue = this.deserialize(split[0], split[2]);
                    if (prop != null) {
                        //set the new value given from the file
                        this.getProperty(split[1]).setValue(propValue);
                    }

                    //remove default graph properties.
                    // this.requestRender();
                }
            }
            
            removeDefaultGraphProps();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void removeDefaultGraphProps() {
        //this.getProperty("Load Positions").setVisible(false);
        this.removeProperty("Load Positions");
        this.removeProperty("Simulation.SPRING_LENGTH");
        this.removeProperty("Simulation.MAX_STEP");
        this.removeProperty("Simulation.Simulate");
        this.removeProperty("Save");
        this.removeProperty("Save Positions");
        this.removeProperty("Appearance.Node Size");
        this.removeProperty("Appearance.Node Color");
        this.removeProperty("Selected");

    }

    public void startStudy() {

        //remove the start button property
        this.removeProperty("Start");

        addTestProperties();
        advanceStudy();
    }

    public void loadTasks() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(TASKFILE)));
            String line = " ";
            String split[];
            pointA = new ArrayList<Integer>();
            pointB = new ArrayList<Integer>();
            GraphTaskEnum graphTaskType = null;
            while ((line = br.readLine()) != null) {
                split = line.split(":");
                if (split[0].startsWith("#task")) {
                    //get the graph TaskEnum
                    if (split[1].trim().equalsIgnoreCase("neighbor")) {
                        graphTaskType = GraphTaskEnum.NEIGHBOR;
                    } else if (split[1].trim().equalsIgnoreCase("path_Boolean")) {
                        graphTaskType = GraphTaskEnum.PATH_BOOLEAN;
                    } else if (split[1].trim().equalsIgnoreCase("path_digit")) {
                        graphTaskType = GraphTaskEnum.PATH_DIGIT;
                    }
                    continue;
                }

                split = line.split("\t");

                int a = Integer.parseInt(split[0]);
                int b = Integer.parseInt(split[1]);
                pointA.add(a);
                pointB.add(b);

                String ans = "no";
                if (graphTaskType == GraphTaskEnum.NEIGHBOR
                        && graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(b))) {
                    ans = "yes";
                }

                GraphTask task = new GraphTask(graphTaskType, ans);
                allTasks.add(task);
                //System.out.println(task.getQuestion());
                //System.out.println(line);
            }
            System.out.println("Size of pointA " + pointA.size());
            System.out.println("Size of pointB " + pointB.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!initTask.done) {
            return;
        }
        if (firstRender) {  //Load the positions of the file here, and set other properties the developer specified
            readPropChangesFileAndUpdate();
            firstRender = false;
        }
        super.render(g);

    }

    public void advanceStudy() {
        if (testCounter > 0) //first time
        {
            // removeTestProperties();
        }
        if (testCounter < pointA.size()) {
            testNodeA = pointA.get(testCounter);
            testNodeB = pointB.get(testCounter);//[testCounter];
            testCounter++;
            showUserStudy(testNodeA, testNodeB);

        }
    }

    public void addTestProperties() {
        Property<PButton> pnext = new Property<PButton>("Advance.Next", new PButton()) {
            @Override
            public boolean updating(PButton newvalue) {

                if (testCounter < pointA.size()) { //increment the current task pointer if possible.

                    //remove and add the qn and the qn status
                    removeProperty("Task.Qn ");
                    ptask_status = new Property<PString>("Task.Qn ", new PString("(" + (testCounter + 1) + "/" + pointA.size() + "): "));
                    ptask_status.setReadOnly(true);
                    addProperty(ptask_status);
                    removeProperty("Task. ");
                    ptask = new Property<PText>("Task. ", new PText(allTasks.get(testCounter).getQuestion()));
                    ptask.setReadOnly(true);
                    addProperty(ptask);

                    advanceStudy();

//                        ptask_status.setReadOnly(false);
//                        ptask_status.setValue(new PString(("("+(testCounter+1)+ "/" + pointA.size()+"): ")));
//                       ptask.setReadOnly(false);
//                        ptask.setValue(new PText(allTasks.get(testCounter).getQuestion()));
                }

                return true;
            }
        };
        this.addProperty(pnext);

        panswer_yes = new Property<PBoolean>("Answer.Yes", new PBoolean(false)) {
            @Override
            public boolean updating(PBoolean newvalue) {
                boolean ans = ((PBoolean) newvalue).boolValue();

                if (ans) {
                    panswer_no.setValue(new PBoolean(false));
                    panswer_no.setReadOnly(true);
                } else {
                    panswer_no.setReadOnly(false);
                }
                return true;
            }
        };
        this.addProperty(panswer_yes);

        panswer_no = new Property<PBoolean>("Answer.No", new PBoolean(false)) {
            @Override
            public boolean updating(PBoolean newvalue) {
                boolean ans = ((PBoolean) newvalue).boolValue();

                if (ans) {
                    panswer_yes.setValue(new PBoolean(false));
                    panswer_yes.setReadOnly(true);
                } else {
                    panswer_yes.setReadOnly(false);
                }

                return true;
            }
        };
        this.addProperty(panswer_no);

        ptask_status = new Property<PString>("Task.Qn ", new PString("(" + (testCounter + 1) + "/" + pointA.size() + "): ")) {
            @Override
            public boolean updating(PString newvalue) {
                this.setReadOnly(true);
                return true;
            }
        };

        ptask_status.setReadOnly(true);
        this.addProperty(ptask_status);

        ptask = new Property<PText>("Task. ", new PText(allTasks.get(testCounter).getQuestion())) {
            @Override
            public boolean updating(PText newvalue) {
                System.out.println("#Task: " + newvalue.serialize());
                return true;
            }
        };
        ptask.setReadOnly(true);
        this.addProperty(ptask);

    }

    public void removeTestProperties() {
        this.removeProperty("Advance.Next");
        this.removeProperty("Advance.Current Qn: ");
        this.removeProperty("Answer.Yes");
        this.removeProperty("Answer.No");
        this.removeProperty("Task. ");

    }

    public void setPropChangesFile(String propChangesFile) {
        System.out.println("Yay!");
        this.propChangesFile = propChangesFile;
    }
    
    

    @Override
    public void renderNode(int i, boolean selected, boolean hovered, Graphics2D g) {
        if ((i == testNodeA) || (i == testNodeB)) { //if the node is part of the two nodes of the study          
            ovals.get(i).setColor(testNodeColor);
            ovals.get(i).render(g);
        } else {
            super.renderNode(i, selected, hovered, g);
        }
//        }
//
//            
//            
//            
//            //ovals.get(i).setColor(OtherNodesColor);
//            ovals.get(i).setColor(nodeColor);
//        }

    }

    public void showUserStudy(int index1, int index2) {
        //System.out.println(index1 + " " + index2);
        int x1 = (int) ovals.get(index1).x;
        int y1 = (int) ovals.get(index2).y;

        int x2 = (int) ovals.get(index2).x;
        int y2 = (int) ovals.get(index1).y;

        showedNode1 = index1;
        showedNode2 = index2;

        this.setTranslation(-(x1 + x2) / 2 + 600, -(y1 + y2) / 2 + 400);

        //translate();
        //exptime = new Date().getTime() + userStudy.taskDuration;
    }

}//end class
