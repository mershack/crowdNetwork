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
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import org.lwjgl.openal.AL10;
import perspectives.base.PEvent;

import perspectives.base.Property;
import perspectives.base.PropertyManager;
import perspectives.base.PropertyType;
import perspectives.properties.PBoolean;
import perspectives.properties.PFileInput;
import perspectives.graph.GraphData;
import perspectives.graph.GraphViewer;
import perspectives.properties.PButton;
import perspectives.properties.PColor;
import perspectives.properties.PFileOutput;
import perspectives.properties.POptions;
import perspectives.properties.PString;
import perspectives.properties.PText;
import perspectives.properties.PTextShort;
import perspectives.two_d.ViewerContainer2D;
import userstudy.UserStudyUtility;
//import util.Points2DViewer.PointAspectType;

public class GraphUserStudyViewer extends GraphViewer {

    ArrayList<Integer> testPointA;
    ArrayList<Integer> testPointB;
    ArrayList<Integer> testPointC;
    ArrayList<Integer> tutPointA;
    ArrayList<Integer> tutPointB;
    ArrayList<Integer> tutPointC;
  //  ArrayList<Integer> testPointA;
    //ArrayList<Integer> testPointB;

    //final String TASKFILE = "tasks.txt";
    final String TASKFILE = "tasks4.txt";
    private String propChangesFile1 = "viewer1.txt";
    private String propChangesFile2 = "viewer2.txt";
    private final String ANSWER_YES = "yes";
    private final String ANSWER_NO = "no";
    private final String TURK_CODE = "EDOCDOOG";
    private final String STUDY_RESULT_FILE = "StudyResults.txt";
    // private String studyType = "Within";//"Between";  //this can be Between or Within
    private String studyType = "Between";  //this can be Between or Within

    int testCounter = 0;
    int totalQualitativeQn = 0;
    int qualitativeAnscount = 0;
    String answer;
    Property<PBoolean> panswer_no;
    Property<PBoolean> panswer_yes;
    Property<PText> ptask;
    Property<PButton> pnext;
    Property<PButton> p_check_answer;
    Property<PString> p_answer;
    Property<PText> p_instruction;
    Property<PBoolean> pPartA, pPartB;
    Property<PButton> pfinish_study;

    ArrayList<GraphTask> testTasks = new ArrayList<GraphTask>();
    ArrayList<GraphTask> tutorialTasks = new ArrayList<GraphTask>();
    public int sizeOftutorial = 6;

    boolean isTutorial = false;
    boolean isTest = false;
    boolean firstTimeHelpfulRating = true;
    boolean firstTimeEaseOfInteractivityRating = true;
    boolean firstTimeInteractiveUseYesAns = true;
    boolean firstTimeInteractiveUseNoAns = true;
    boolean mouseOverUsed = false;
    boolean mouseClickingUsed = false;
    boolean zoomingUsed = false;
    boolean panningUsed = false;
    boolean noInteractivityUsed = false;

    int interact_helpfulnessRating;
    int interact_easenessRating;
    String interact_use;

    int tutorialCounter = 0;
    int totalTasks = 0;

    String userTurkID = "";

    boolean firstRender = true;
    boolean secondRender = true;
    int cnt = 0;
    int testNodeA = -1;
    int testNodeB = -1;
    int testNodeC = -1;
    Color testNodeColor = Color.red;
    Color OtherNodesColor = Color.lightGray;

    boolean advance = false;

    //String localDataDir = "";
    /////////////////////////////                 Constructor             //////////////////////////////
    public GraphUserStudyViewer(String name, GraphData g) {
        super(name, g);
        addInstructionAndTutorial();
    }

    public void addInstructionAndTutorial() {
        //instruction about the tutorial
        /*String tutinstr = "There are 2 sections in this task. "
         + "You will be given a simple tutorial involving  questions for each of the sections. \n\n"
         + "You can check whether your chodsen answer is correct or not during this tutorial session.\n";*/

        String tutinstr = "In this study there are two types of questions:\n"
                + "- Type 1: You will determine if two highlighted nodes are directly connected (Yes/No)\n"
                + "- Type 2: You will determine if three highlighted nodes are directly connected (Yes/No).\n\n"
                + "You will be given a simple trial involving questions of each type. "
                + "You can check whether your chosen answer is correct or not during the trial session.\n\n"
                + "There are 20 questions in total in the  main study";

        Property<PText> ptutorial = new Property<PText>("Tutorial.Instruction", new PText(tutinstr));
        ptutorial.setReadOnly(true);
        this.addProperty(ptutorial);

        Property<PButton> p_begin_tutorial = new Property<PButton>("Tutorial.Begin Tutorial", new PButton()) {

            @Override
            public boolean updating(PButton newvalue) {
                //start the tutorial
                startTutorial();
                return true;
            }
        };
        this.addProperty(p_begin_tutorial);
    }

    public void startTutorial() {
        //remove the tutorial instruction and the button
        removeProperty("Tutorial.Instruction");
        removeProperty("Tutorial.Begin Tutorial");

        isTutorial = true;

        addTestProperties();
        advanceStudy();

    }

    public void endTutorial() {
        //remove the tutorial properties and begin the actual study
        isTutorial = false; //end the tutorial.        

        removeProperty("Task.Check Answer");
        removeProperty("Task.Answer");
        removeProperty("Advance.Next");
        removeTestProperties();

        prepareToStartStudy_StepOne();
    }

    public void removeTestProperties() {
        removeProperty("Answer.Yes");
        removeProperty("Answer.No");
        removeProperty("Task. ");
        removeProperty("#Task: ");
        removeProperty("Task.Instruction");
    }

    /**
     * remove the turkId textbox when that value is provided
     */
    public void removeTurkID() {
        this.removeProperty("Enter your Turk ID:");
    }

    public void readPropChangesFileOneAndUpdate() {
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();
            File propFile = new File(localDataDir + File.separator + propChangesFile1);

            if (propFile.exists()) {//Do the property changes if the file exists

                BufferedReader br = new BufferedReader(new FileReader(propFile));
                String line = "";
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
                        if (split[0].equalsIgnoreCase("PFileInput")) {
                            //split the path of the file, look for the last item and then append it to the local path.
                            String split2[] = split[2].split("\\\\");
                            if (split[2].split("\\\\").length > 0) {
                                split2 = split[2].split("\\\\");
                            } else if (split[2].split("/").length > 0) {
                                split2 = split[2].split("/");
                            }

                            String filelocation = localDataDir + File.separator + split2[split2.length - 1];
                            propValue = this.deserialize(split[0], filelocation);
                        }

                        this.getProperty(split[1]).setValue(propValue);
                    }
                }
                br.close();
            }

            removeDefaultGraphProps();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void readPropChangesFileTwoAndUpdate() {
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();
            File propFile = new File(localDataDir + File.separator + propChangesFile2);

            addDefaultGraphProps(); //add the default graph properties again, just for the case of making the property changes to it.

            if (propFile.exists()) {//Do the property changes if the file exists
                //   System.out.println("^^^^^^^^^^ PropChanges File 2");
                BufferedReader br = new BufferedReader(new FileReader(propFile));
                String line = "";
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
                        if (split[0].equalsIgnoreCase("PFileInput")) {
                            //split the path of the file, look for the last item and then append it to the local path.
                            String split2[] = split[2].split("\\\\");
                            if (split[2].split("\\\\").length > 0) {
                                split2 = split[2].split("\\\\");
                            } else if (split[2].split("/").length > 0) {
                                split2 = split[2].split("/");
                            }

                            String filelocation = localDataDir + File.separator + split2[split2.length - 1];
                            propValue = this.deserialize(split[0], filelocation);
                        }

                        this.getProperty(split[1]).setValue(propValue);
                    }
                }
                br.close();
            }
            //remove the properties again
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

    public void addDefaultGraphProps() {
        addGraphProperties(this);
    }

    public void prepareToStartStudy_StepOne() {

        Property<PString> ptutinfo = new Property<PString>("Information: ", new PString("Tutorial Completed!"));
        this.addProperty(ptutinfo);

        Property<PButton> pcontinue = new Property<PButton>("Continue to Study", new PButton()) {
            @Override
            public boolean updating(PButton newvalue) {
                prepareToStartStudy_StepTwo();
                return true;
            }

        };
        this.addProperty(pcontinue);

    }

    public void prepareToStartStudy_StepTwo() {
        removeProperty("Information: ");
        removeProperty("Continue to Study");

        Property<PString> pturkId = new Property<PString>("Enter your Turk ID:", new PString("")) {

            @Override
            public boolean updating(PString newvalue) {

                //set the user's turkid and remove the turkid textbox.
                userTurkID = newvalue.stringValue();
                return true;
            }
        };
        this.addProperty(pturkId);

        Property<PButton> pstart = new Property<PButton>("Start Study", new PButton()) {
            @Override
            public boolean updating(PButton newvalue) {
                //remove the turkid button if it's not empty
                if (!(userTurkID.isEmpty())) {
                    removeTurkID();
                }

                startStudy();

                return true;
            }
        };
        this.addProperty(pstart);

    }

    public void startStudy() {
        //remove the start button property
        this.removeProperty("Start Study");
        this.removeProperty("Information: ");

        if (studyType.equalsIgnoreCase("Within")) {
            adjustTasksForWithinStudy();
        }
        addTestProperties();
        advanceStudy();
    }

    public void adjustTasksForWithinStudy() {
        //we will double the testtasks, add the same tasks to the end of the list for the testTasks and the points.
        totalTasks += testTasks.size();

        int length = testTasks.size();

        for (int i = 0; i < length; i++) {
            testTasks.add(testTasks.get(i));
            testPointA.add(testPointA.get(i));
            testPointB.add(testPointB.get(i));
            testPointC.add(testPointC.get(i));
        }

    }

    public void addQualitativeQuestions() {

        totalQualitativeQn = 3;

        //remove the test properties
        String rating[] = {"selectOne", "1", "2", "3", "4", "5"};

        removeTestProperties();
        removeProperty("Advance.Next");

        pfinish_study = new Property<PButton>("Qualitative.End Of Study", new PButton()) {

            @Override
            public boolean updating(PButton newvalue) {
                //do something
                endOfQualitativeQuestions();
                return true;
            }
        };
        pfinish_study.setReadOnly(true);
        this.addProperty(pfinish_study);

        String info = "------------Info-----------\n"
                + "Answer the questions below and then you can click on the button above to get your MTurk code";

        Property<PTextShort> pInfo = new Property<PTextShort>("Qualitative.Info", new PTextShort(info));
        pInfo.setReadOnly(true);
        this.addProperty(pInfo);

        String easeQn = "Can you rate the ease with which you used or adopted "
                + "the interactivity techniques using a 1 - 5 point where 1 means Not Easy and 5 means Very Easy. Answer below";

        String interactHelpfulQn = "Can you rate how helpful the interactivity was for the tasks you performed using a 1 - 5 point"
                + " where 1 means Not Helpful and 5 means Very Helpful. Answer below";

        String interactuse = "Did you successfully used any of  the interactivity shown below during the tasks? Check all that apply";

      
        
        
        
        
        
        Property<PTextShort> pinteractuse = new Property<PTextShort>("Qualitative.Qn1", new PTextShort(interactuse));
        pinteractuse.setReadOnly(true);
        this.addProperty(pinteractuse);

        Property<PBoolean> p_mouseclickUsed = new Property<PBoolean>("Qualitative.Clicking Nodes", new PBoolean(false)){
            @Override
            public boolean updating(PBoolean newvalue){
                boolean value  = newvalue.boolValue();
                
                    if(value){
                        if(!anyInteractivityUsed()){
                            qualitativeAnscount++;
                        }
                        mouseClickingUsed = newvalue.boolValue();                        
                    }
                    else{
                         mouseClickingUsed = newvalue.boolValue();
                         if(!anyInteractivityUsed()){
                             qualitativeAnscount--;
                         }
                    }                    
                    checkAllQualitativeQnAnswered();
                    
                return true;
            }
        };
        
        this.addProperty(p_mouseclickUsed);
        
        Property<PBoolean> p_mouseoverUsed = new Property<PBoolean>("Qualitative.Mouse-over Nodes", new PBoolean(false)){
            @Override
            public boolean updating(PBoolean newvalue){
                boolean value  = newvalue.boolValue();
                
                    if(value){
                        if(!anyInteractivityUsed()){
                            qualitativeAnscount++;
                        }
                        mouseOverUsed = newvalue.boolValue();                        
                    }
                    else{
                         mouseOverUsed = newvalue.boolValue();
                         if(!anyInteractivityUsed()){
                             qualitativeAnscount--;
                         }
                    }                    
                    checkAllQualitativeQnAnswered();
                    
                return true;
            }
        };
        this.addProperty(p_mouseoverUsed);
        
        
          Property<PBoolean> p_panningUsed = new Property<PBoolean>("Qualitative.Panning", new PBoolean(false)){
            @Override
            public boolean updating(PBoolean newvalue){
                boolean value  = newvalue.boolValue();
                
                    if(value){
                        if(!anyInteractivityUsed()){
                            qualitativeAnscount++;
                        }
                        panningUsed = newvalue.boolValue();                        
                    }
                    else{
                        panningUsed = newvalue.boolValue();
                         if(!anyInteractivityUsed()){
                             qualitativeAnscount--;
                         }
                    }                    
                    checkAllQualitativeQnAnswered();
                    
                return true;
            }
        };
        this.addProperty(p_panningUsed);
        
        
          Property<PBoolean> p_zoomingUsed = new Property<PBoolean>("Qualitative.Zooming", new PBoolean(false)){
            @Override
            public boolean updating(PBoolean newvalue){
                boolean value  = newvalue.boolValue();
                
                    if(value){
                        if(!anyInteractivityUsed()){
                            qualitativeAnscount++;
                        }
                        zoomingUsed = newvalue.boolValue();                        
                    }
                    else{
                         zoomingUsed = newvalue.boolValue();
                         if(!anyInteractivityUsed()){
                             qualitativeAnscount--;
                         }
                    }                    
                    checkAllQualitativeQnAnswered();
                    
                return true;
            }
        };
        this.addProperty(p_zoomingUsed);
        
        /*panswer_yes = new Property<PBoolean>("Qualitative.Yes", new PBoolean(false)) {
            @Override
            public boolean updating(PBoolean newvalue) {
                boolean ans = ((PBoolean) newvalue).boolValue();
                if (ans) {
                    // panswer_no.setValue(new PBoolean(false));
                    panswer_no.setReadOnly(true);
                    //   pnext.setReadOnly(false); 

                    interact_use = "yes";

                    if (firstTimeInteractiveUseYesAns) {
                        qualitativeAnscount++;
                        firstTimeInteractiveUseYesAns = false;
                    }

                } else {
                    if (!firstTimeInteractiveUseYesAns) {
                        qualitativeAnscount--;
                        firstTimeInteractiveUseYesAns = true;
                    }
                    panswer_no.setReadOnly(false);
                    //next.setReadOnly(true);
                }

                checkAllQualitativeQnAnswered();

                return true;
            }
        };
        this.addProperty(panswer_yes);
        panswer_no = new Property<PBoolean>("Qualitative.No", new PBoolean(false)) {
            @Override
            public boolean updating(PBoolean newvalue) {
                boolean ans = ((PBoolean) newvalue).boolValue();

                if (ans) {
                    //first set the given answer  
                    //panswer_yes.setValue(new PBoolean(false));
                    panswer_yes.setReadOnly(true);

                    if (firstTimeInteractiveUseNoAns) {
                        qualitativeAnscount++;
                        firstTimeInteractiveUseNoAns = false;
                    }

                    interact_use = "no";

                } else {
                    if (!firstTimeInteractiveUseNoAns) {
                        qualitativeAnscount--;
                        firstTimeInteractiveUseNoAns = true;
                    }

                    panswer_yes.setReadOnly(false);
                    // pnext.setReadOnly(true);
                }

                checkAllQualitativeQnAnswered();

                return true;
            }
        };
        this.addProperty(panswer_no);  */

        Property<PTextShort> pease = new Property<PTextShort>("Qualitative.Qn2 ", new PTextShort(easeQn));
        pease.setReadOnly(true);
        this.addProperty(pease);

        Property<POptions> peaseRating = new Property<POptions>("Qualitative.Easiness of Interactivity", new POptions(rating)) {

            @Override
            public boolean updating(POptions newvalue) {
                int rate = newvalue.selectedIndex;

                if (rate > 0) {

                    if (firstTimeEaseOfInteractivityRating) {
                        qualitativeAnscount++;
                        firstTimeEaseOfInteractivityRating = false;
                    }
                    interact_easenessRating = rate;

                } else {
                    if (!firstTimeEaseOfInteractivityRating) {
                        qualitativeAnscount--;
                        firstTimeEaseOfInteractivityRating = true;
                    }
                }

                checkAllQualitativeQnAnswered();

                return true;
            }
        };
        this.addProperty(peaseRating);

        Property<PTextShort> pinteractHelpful = new Property<PTextShort>("Qualitative.Qn3 ", new PTextShort(interactHelpfulQn));
        pinteractHelpful.setReadOnly(true);
        this.addProperty(pinteractHelpful);

        Property<POptions> pinteractHelpfulRating = new Property<POptions>("Qualitative.Helpfulness of Interactivity", new POptions(rating)) {

            @Override
            public boolean updating(POptions newvalue) {
                int rate = newvalue.selectedIndex;

                if (rate > 0) {
                    if (firstTimeHelpfulRating) {
                        qualitativeAnscount++;
                        firstTimeHelpfulRating = false;
                    }

                    interact_helpfulnessRating = rate;

                } else {
                    if (!firstTimeHelpfulRating) {
                        qualitativeAnscount--;
                        firstTimeHelpfulRating = true;
                    }

                }
                checkAllQualitativeQnAnswered();

                return true;
            }
        };
        this.addProperty(pinteractHelpfulRating);

        //If within user study, ask a question about which part of the visualization they liked best.
       /* if (studyType.equals("Within")) {

         totalQualitativeQn = 4;
         String partAOrBQn = "Which of the visualizations (i.e. PartA or PartB) would you prefer best?";

         Property<PTextShort> partAorB = new Property<PTextShort>("Qualitative.Qn4 ", new PTextShort(partAOrBQn));
         partAorB.setReadOnly(true);
         this.addProperty(partAorB);

         pPartA = new Property<PBoolean>("Qualitative.PartA", new PBoolean(false)) {
         @Override
         public boolean updating(PBoolean newvalue) {
         boolean ans = ((PBoolean) newvalue).boolValue();
                   
         if (ans) {
         qualitativeAnscount++;
         pPartB.setValue(new PBoolean(false));
         pPartB.setReadOnly(true);

         checkAllQualitativeQnAnswered();
         } else {
         qualitativeAnscount--;
         pPartB.setReadOnly(false);
         //next.setReadOnly(true);
         }
         return true;
         }
         };
         this.addProperty(pPartA);

         pPartB = new Property<PBoolean>("Qualitative.PartB", new PBoolean(false)) {
         @Override
         public boolean updating(PBoolean newvalue) {
         boolean ans = ((PBoolean) newvalue).boolValue();

         if (ans) {
         qualitativeAnscount++;
         pPartA.setValue(new PBoolean(false));
         pPartA.setReadOnly(true);

         checkAllQualitativeQnAnswered();

         } else {
         qualitativeAnscount--;
         pPartA.setReadOnly(false);
         // pnext.setReadOnly(true);
         }

         return true;
         }
         };
         this.addProperty(pPartB);

         }*/
    }

    
    public boolean  anyInteractivityUsed(){
        
        if(!(mouseClickingUsed||mouseOverUsed||panningUsed||zoomingUsed ||noInteractivityUsed)){
            return false;
        }
        else{
            return true;
        }
    }
    
    public void checkAllQualitativeQnAnswered() {
        if (qualitativeAnscount == totalQualitativeQn) {
            pfinish_study.setReadOnly(false);
        } else {
            pfinish_study.setReadOnly(true);
        }

    }

    public void endOfQualitativeQuestions() {

        removeProperty("Qualitative.End Of Study");
        removeProperty("Qualitative.Info");
        removeProperty("Qualitative.Qn1");
        removeProperty("Qualitative.Clicking Nodes");
        removeProperty("Qualitative.Mouse-over Nodes");
        removeProperty("Qualitative.Panning");
        removeProperty("Qualitative.Zooming");
        removeProperty("Qualitative.Yes");
        removeProperty("Qualitative.No");
        removeProperty("Qualitative.Qn2 ");
        removeProperty("Qualitative.Easiness of Interactivity");
        removeProperty("Qualitative.Qn3 ");
        removeProperty("Qualitative.Helpfulness of Interactivity");
        removeProperty("Qualitative.Qn4 ");
        removeProperty("Qualitative.PartA");
        removeProperty("Qualitative.PartB");

        //end the study
        endOfStudy();

    }

    public void loadTasks() {
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();

            //System.out.println("::: localDirPath::" + localDataDir);
            BufferedReader br = new BufferedReader(new FileReader(new File(localDataDir + File.separator + TASKFILE)));
            String line = " ";
            String split[];
            testPointA = new ArrayList<Integer>();
            testPointB = new ArrayList<Integer>();
            testPointC = new ArrayList<Integer>();
            tutPointA = new ArrayList<Integer>();
            tutPointB = new ArrayList<Integer>();
            tutPointC = new ArrayList<Integer>();

            GraphTaskEnum graphTaskType = null;
            int cnt = 0;
            int halfOfTutorial = sizeOftutorial / 2;
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

                    cnt = 0;

                    continue;
                }

                split = line.split("\t");

                int a = Integer.parseInt(split[0].trim());
                int b = Integer.parseInt(split[1].trim());
                int c = -1;
                if (split.length > 2) {
                    c = Integer.parseInt(split[2].trim());
                }

                String ans = "no";
                if (graphTaskType == GraphTaskEnum.NEIGHBOR
                        && graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(b))) {
                    ans = "yes";
                } else if ((graphTaskType == GraphTaskEnum.PATH_BOOLEAN)
                        && ((graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(b))
                        && graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(c)))
                        || (graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(c))
                        && graph.isEdge(graph.getNodes().get(b), graph.getNodes().get(c)))
                        || (graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(b))
                        && graph.isEdge(graph.getNodes().get(b), graph.getNodes().get(c))))) {
                    ans = "yes";
                }

                GraphTask task = new GraphTask(graphTaskType, ans);
                cnt++;
                totalTasks++;
                if (cnt <= halfOfTutorial) {
                    tutPointA.add(a);
                    tutPointB.add(b);
                    tutPointC.add(c);
                    tutorialTasks.add(task);
                } else {
                    testPointA.add(a);
                    testPointB.add(b);
                    testPointC.add(c);
                    testTasks.add(task);
                }

            }
            
          // printTaskCorrectAns();

            br.close();

//            System.out.println("Size of testPointA " + testPointA.size());
//            System.out.println("Size of testPointB " + testPointB.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void printTaskCorrectAns(){
        
        System.out.println("*************::::::::::::::::::********************");
        
        for (int i=0; i<testTasks.size(); i++){           
           System.out.println(testPointA.get(i)+ "\t" + testPointB.get(i) + "\t" + testPointC.get(i) + "\t: " + testTasks.get(i).getCorrectAns());                   
        }
        
        System.out.println("*************::::::::::::::::::********************");
        
    }   

    @Override
    public void render(Graphics2D g) {
        if (!initTask.done) {
            return;
        }
        if (firstRender) {  //Load the positions of the file here, and set other properties the developer specified
            loadTasks();
            readPropChangesFileOneAndUpdate();
            firstRender = false;
        }

        super.render(g);

    }

    public void advanceStudy() {

        if ((testCounter + tutorialCounter) < totalTasks) {
            pnext.setReadOnly(true); //disable the next button until an answer is selected

            //add the tutorial properties.
            if (tutorialCounter < sizeOftutorial) {
                isTutorial = true;

                testNodeA = tutPointA.get(tutorialCounter);
                testNodeB = tutPointB.get(tutorialCounter);//[testCounter];
                testNodeC = tutPointC.get(tutorialCounter);

                showUserStudy(testNodeA, testNodeB);
                //can we use a specific zoom?
                this.setZoom(0.8);

                addTutorialProperties();
                p_check_answer.setReadOnly(true);
                tutorialCounter++;

            } else if (!isTest) {      //end tutorial before the actual study begins.                 
                endTutorial();
                isTest = true;
            } else {
                // System.out.println("&&& testCounter is::: "+testCounter);
                if (studyType.equalsIgnoreCase("Within") && (testCounter == testTasks.size() / 2)) {
                    //System.out.println("--- In Within::: Yay!!");
                    readPropChangesFileTwoAndUpdate();
                    //  request;
                }

                testNodeA = testPointA.get(testCounter);
                testNodeB = testPointB.get(testCounter);//[testCounter];
                testNodeC = testPointC.get(testCounter);

                showUserStudy(testNodeA, testNodeB);
                this.setZoom(0.8);
                testCounter++;
            }

        }
    }

    public void addTestProperties() {
        pnext = new Property<PButton>("Advance.Next", new PButton()) {

            @Override
            public boolean updating(PButton newvalue) {
                //remove the turkid button if it's not empty
                if (!(userTurkID.isEmpty())) {
                    removeTurkID();
                }

                if ((testCounter + tutorialCounter) < totalTasks) { //increment the current task pointer if possible.
                    //add questions and the answer options                    
                    if (isTutorial && tutorialCounter < tutorialTasks.size()) {
                        addQuestionAndOptions();
                        addTutorialProperties();
                    } else if (isTest) {
                        addQuestionAndOptions();
                    }
                    advanceStudy();
                } else {
                    addQualitativeQuestions();

                    //endOfStudy();
                    //remove the next button also
                    //removeProperty("Advance.Next");
                }

                return true;
            }

        };
        pnext.setReadOnly(true);
        this.addProperty(pnext);

        //add the questions and options
        addQuestionAndOptions();

    }

    public void addQuestionAndOptions() {
        //first remove the properties if they already exist
        removeTestProperties();

        panswer_yes = new Property<PBoolean>("Answer.Yes", new PBoolean(false)) {
            @Override
            public boolean updating(PBoolean newvalue) {
                boolean ans = ((PBoolean) newvalue).boolValue();

                if (ans) {

                    //first set the given answer  
                    if (isTutorial) {
                        tutorialTasks.get(tutorialCounter - 1).setGivenAns(ANSWER_YES);
                    } else {
                        testTasks.get(testCounter - 1).setGivenAns(ANSWER_YES);
                    }
                    //allTasks.get(testCounter-1).setGivenAns(ANSWER_YES);

                    panswer_no.setValue(new PBoolean(false));
                    panswer_no.setReadOnly(true);
                    pnext.setReadOnly(false);

                    if (isTutorial) {
                        p_check_answer.setReadOnly(false);
                    }
                } else {
                    panswer_no.setReadOnly(false);
                    pnext.setReadOnly(true);

                    if (isTutorial) {
                        p_check_answer.setReadOnly(true);
                    }
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
                    //first set the given answer  
                    if (isTutorial) {
                        tutorialTasks.get(tutorialCounter - 1).setGivenAns(ANSWER_NO);
                    } else {
                        testTasks.get(testCounter - 1).setGivenAns(ANSWER_NO);
                    }

                    panswer_yes.setValue(new PBoolean(false));
                    panswer_yes.setReadOnly(true);
                    pnext.setReadOnly(false);

                    if (isTutorial) {
                        p_check_answer.setReadOnly(false);
                    }
                } else {
                    panswer_yes.setReadOnly(false);
                    pnext.setReadOnly(true);
                    if (isTutorial) {
                        p_check_answer.setReadOnly(true);
                    }
                }

                return true;
            }
        };
        this.addProperty(panswer_no);
        int currentNum;
        int totalNum;
        String status_string = "";
        String qn;

        // S
        if (!isTutorial) {
            // System.out.println("^^^^^^^^^^^^Test counter is ::::: " + testCounter);
            qn = testTasks.get(testCounter).getQuestion();
            currentNum = testCounter + 1;
            totalNum = testTasks.size();
        } else {
            qn = tutorialTasks.get(tutorialCounter).getQuestion();
            currentNum = tutorialCounter + 1;
            totalNum = tutorialTasks.size();
        }

        String qn2 = "\t   Qn (" + currentNum + "/" + totalNum + "): "
                + "\n\t   ---------- \n\n";
        qn2 = qn2 + qn;

        ptask = new Property<PText>("Task. ", new PText(qn2));
        ptask.setDisabled(true);
        this.addProperty(ptask);

        p_instruction = new Property<PText>("Task.Instruction", new PText(new GraphTask().getInstruction()));
        p_instruction.setReadOnly(true);
        this.addProperty(p_instruction);

    }

    public void addTutorialProperties() {
        removeTutorialProperties(); //first remove the tutorial properties
        p_check_answer = new Property<PButton>("Task.Check Answer", new PButton()) {
            @Override
            public boolean updating(PButton newvalue) {
                showAnswer();
                return true;
            }
        };
        p_check_answer.setReadOnly(true); // it will be enabled when an answer is selected
        this.addProperty(p_check_answer);

    }

    public void showAnswer() {
        String answer = "WRONG answer";
        System.out.println("***Given " + tutorialTasks.get(tutorialCounter - 1).getGivenAns()
                + "  **** " + tutorialTasks.get(tutorialCounter - 1).getCorrectAns());
        if (tutorialTasks.get(tutorialCounter - 1).isAnswerCorrect()) {
            answer = "CORRECT answer";
        }

        // removeTutorialProperties();
        // removeProperty("Task.Check Answer");
        addTutorialProperties();

        p_answer = new Property<PString>("Task.Answer", new PString(answer));
        p_answer.setReadOnly(true);
        this.addProperty(p_answer);

    }

    public void removeTutorialProperties() {
        removeProperty("Task.Check Answer");
        removeProperty("Task.Answer");
    }

    public void endOfStudy() {
        //TODO: 1- remove the test properties
        //2. Show the Mechanical Turk code
        //3. Write answers to a file.

        removeTestProperties();
        Property<PString> ptask_info = new Property<PString>("Task.Task Information", new PString("Completed!"));
        ptask_info.setReadOnly(true);
        this.addProperty(ptask_info);

        Property<PString> p_turk_code = new Property<PString>("Task.Your Turk Code", new PString(TURK_CODE));
        p_turk_code.setReadOnly(true);
        this.addProperty(p_turk_code);

        //writing answers to file
        writeAnswersToFile();
    }

    public void writeAnswersToFile() {
        //TODO. Write the anser into the answers file in the following format
        //Format:  TurkID, correctnessOfAns1, CorrectnessOfAns2, ..., correctnessOfAnsN

        //create a File with the name STUDY_RESULT and save that file in the data directory.
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();
            File studyResults = new File(localDataDir + File.separator + STUDY_RESULT_FILE);

            boolean newFile = false;

            if (!studyResults.exists()) {
                studyResults.createNewFile();
                newFile = true;
            }
            //do the actual writings of the results to the file
            FileWriter fileWritter = new FileWriter(studyResults, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            PrintWriter pw = new PrintWriter(bufferWritter);

            //print header
            if (newFile) {
                pw.printf("TurkID");
                for (int i = 0; i < testTasks.size(); i++) {
                    pw.print(",Qn" + (i + 1));
                }
            //    pw.printf("," + "interactive_use" + "interactive_easenessRating" + "interactive_helpfulnessRating");
                    pw.printf("," + "Node-Clicking-Used, Mouse-Over-Used, Panning-Used, Zooming-Used,"
                            + "interactive_easenessRating, interactive_helpfulnessRating");
                pw.println();
            }

            pw.print(userTurkID); //the turkid is in the first column

            for (GraphTask gtask : testTasks) {
                pw.print("," + gtask.isAnswerCorrect());
            }

            
            //append the quantitative answers to the answers
          //  pw.printf("," + interact_use + "," + interact_easenessRating + "," + interact_helpfulnessRating);
             // pw.printf("," + "Node-Clicking-Used, Mouse-Over-Used, Panning-Used, Zooming-Used,"
                           // + "interactive_easenessRating, interactive_helpfulnessRating");
              pw.printf("," + mouseClickingUsed + "," +mouseOverUsed + "," + panningUsed + "," + zoomingUsed + ","
                      + interact_easenessRating + "," + interact_helpfulnessRating);

            pw.println(); //next entry should go to the next line.

            //close the streams
            pw.close();
            bufferWritter.close();
            fileWritter.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setStudyType(String stype) {
        studyType = stype;
    }

    public void setPropChangesFileOne(String propChangesFile) {
        //System.out.println("Yay!");
        this.propChangesFile1 = propChangesFile;
    }

    public void setPropChangesFileTwo(String propChangesFile) {
        //System.out.println("Yay!");
        this.propChangesFile1 = propChangesFile;
    }

    @Override
    public void renderNode(int i, boolean selected, boolean hovered, Graphics2D g) {
        if ((i == testNodeA) || (i == testNodeB) || (i == testNodeC)) { //if the node is part of the two nodes of the study          
            ovals.get(i).setColor(testNodeColor);
            ovals.get(i).render(g);
            //also draw a square around the node
           /* g.drawRect((int)(ovals.get(i).x - (int)ovals.get(i).w/2), (int)(ovals.get(i).y - (int)ovals.get(i).h/2), 
             (int)ovals.get(i).h, (int)ovals.get(i).w); */
        } else {
            super.renderNode(i, selected, hovered, g);
        }

    }
    int cnt2 = 0;

    public void showUserStudy(int index1, int index2) {

        int x1 = (int) ovals.get(index1).x;
        int y1 = (int) ovals.get(index2).y;

        int x2 = (int) ovals.get(index2).x;
        int y2 = (int) ovals.get(index1).y;

        this.setTranslation(-(x1 + x2) / 2 + 300, -(y1 + y2) / 2 + 400);

        if (!(studyType.equalsIgnoreCase("Within") && (testCounter == testTasks.size() / 2))) {
            getContainer().resetTiles();
        }

        this.requestRender();

    }

}//end class
