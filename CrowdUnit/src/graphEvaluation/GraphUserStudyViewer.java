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
import java.util.HashMap;
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
    final String TESTCONDITION = "testCondition";
    final String CONTROLCONDITION = "controlCondition";
    final String TASKSPECIFICATIONFILE = "taskSpecification.txt";
    private final String QLT_TASK_FILENAME = "qualitativeTasks.txt";
    private final String QLT_STUDY_RESULT_FILE = "qualitativeStudyResults.txt";
    private final String QUANT_CONTROLCONDITION_RESULT_FILE = "quantitativeControlConditionResults.txt";
    private final String QUANT_TESTCONDITION_RESULT_FILE = "quantitativeTestConditionResults.txt";
    private final String PROPERTY_CHANGES_FILE = "propertyChangesFile.txt";
    private final String EXPTYPE_FILENAME = "expType.txt";
    private final String TOTAL_ASSIGNMENTS_FILE = "totalAssignments.txt";
    private final String WITHIN = "Within Subjects";
    private final String BETWEEN = "Between Subjects";
    private String propChangesFile1 = "viewer1.txt";
    private String propChangesFile2 = "viewer2.txt";
    private int totalAssignments = 0;
    private int currentAssignmentCount = 0;
    private final String ANSWER_YES = "yes";
    private final String ANSWER_NO = "no";
    private final String TURK_CODE = "EDOCDOOG";
    private final String rating[] = {"selectOne", "1", "2", "3", "4", "5"};
    // private String studyType = "Within";//"Between";  //this can be Between or Within
    private String expType = WITHIN;  //this can be Between or Within
    private String controlConditionFilename = "viewer1.txt";
    private String testConditionFilename = "viewer2.txt";
    private String currentExpCondition = "";
    private String firstExpCondition = "";
    private String secondExpCondition = "";

    int testCounter = 0;
    int totalQualitativeQn = 0;
    int qualitativeAnscount = 0;
    //   private String expType="";
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
    ArrayList<String> qualitativeTask = new ArrayList<String>();
    ArrayList<String> qualitativeTaskType = new ArrayList<String>();
    ArrayList<String> qualitativeTasksLabels = new ArrayList<String>();
    HashMap<Integer, String> qualitativeTaskAnswers = new HashMap<Integer, String>();
    public int sizeOftutorial = 3;
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
    HashMap<Object, Object> taskAndSize = new HashMap();
    boolean advance = false;
    ArrayList<String> taskTypes = new ArrayList<String>();

    //String localDataDir = "";
    /////////////////////////////                 Constructor             //////////////////////////////
    public GraphUserStudyViewer(String name, GraphData g) {
        super(name, g);  
         setIsUserStudy(true);    
    }

    public void addInstructionAndTutorial() {
        
        String tutinstr = "In this study there are "+ taskTypes.size() +" two types of questions:\n"
                + "You will be given a simple trial  involving 3 questions of each type. "
                + "You can check whether your chosen answer is correct or not during the trial session.\n\n"
                + "There are " + testTasks.size() + " questions in total for the  main study";

        Property<PText> ptutorial = new Property<PText>("Tutorial. ", new PText(tutinstr));
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
        removeProperty("Tutorial. ");
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
        removeProperty("Task. ");
    }

    /**
     * remove the turkId textbox when that value is provided
     */
    public void removeTurkID() {
        this.removeProperty("Enter your Turk ID:");
    }

    public void readPropChangesFile() {

        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();
            File propFile = new File(localDataDir + File.separator + PROPERTY_CHANGES_FILE);

            if (propFile.exists()) {//Do the property changes if the file exists
                BufferedReader br = new BufferedReader(new FileReader(propFile));
                String line = "";
                ArrayList<String> lines = new ArrayList<String>();
                //NB: controlcondition filename is first line and test condition filename is second line

                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
                br.close();

                //NB: controlcondition filename is first line and test condition filename is second line
                if (lines.size() > 1) {
                    controlConditionFilename = lines.get(0);
                    testConditionFilename = lines.get(1);
                }
            }
            //read the totalAssignmets file also

            File totalFile = new File(localDataDir + File.separator + TOTAL_ASSIGNMENTS_FILE);
            BufferedReader br = new BufferedReader(new FileReader(totalFile));
            String line = "";

            //the total assignments file will contain the value for the total assignments
            while ((line = br.readLine()) != null) {
                totalAssignments = Integer.parseInt(line);
            }
            br.close();

            //get the currentAssignmentCount also
            File resultFile = new File(localDataDir + File.separator + QUANT_CONTROLCONDITION_RESULT_FILE);

            int cnt = 0;
            //the total assignments file will contain the value for the total assignments

            if (resultFile.exists()) {

                br = new BufferedReader(new FileReader(resultFile));
                while ((line = br.readLine()) != null) {
                    cnt++;
                }
                br.close();
            }
            currentAssignmentCount = cnt;

            System.out.println("Current Assingment count is " + currentAssignmentCount + " and totalAssignmentCount is " + totalAssignments);

            //call the respective method to perform the property changes
            if (currentAssignmentCount <= (totalAssignments / 2)) {
                //System.out.println("-*-*-");
                readPropChangesForControlCondition();
            } else {
                // System.out.println("-B-B-");
                readPropChangesForTestCondition();
            }

            firstExpCondition = currentExpCondition;

            //removeDefaultGraphProps();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void readPropChangesForControlCondition() {
        try {

            currentExpCondition = CONTROLCONDITION;
            // System.out.println("First Condition is "+ currentExpCondition );

            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();
            File propFile = new File(localDataDir + File.separator + controlConditionFilename);

            removeDefaultGraphProps();
            addDefaultGraphProps();

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

    public void readPropChangesForTestCondition() {
        try {
            currentExpCondition = TESTCONDITION;
            // System.out.println("First Condition is "+ currentExpCondition );
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();
            File propFile = new File(localDataDir + File.separator + testConditionFilename);

            removeDefaultGraphProps();
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

        if (expType.equalsIgnoreCase(WITHIN)) {
            adjustTasksForWithinStudy();
        }
        addTestProperties();
        advanceStudy();
    }

    public void adjustTasksForWithinStudy() {
        //we will double the testtasks, add the same tasks to the end of the list for the testTasks and the points.
        totalTasks += testTasks.size();

        int length = testTasks.size();

        String firstHalfCondition = testTasks.get(0).getExperimentCondition();
        String secondHalfCondition = "";
        System.out.println("the condition is " + firstExpCondition + "&&&&" + testTasks.size());
        if (firstHalfCondition.equalsIgnoreCase(CONTROLCONDITION)) {
            System.out.println("------ " + firstHalfCondition);
            secondHalfCondition = TESTCONDITION;
        } else {
            System.out.println("******* " + firstHalfCondition);
            secondHalfCondition = CONTROLCONDITION;
        }

        for (int i = 0; i < length; i++) {

            GraphTask task = testTasks.get(i);

            GraphTask gt = new GraphTask(task.getTask(), task.getCorrectAns(), secondHalfCondition);
            testTasks.add(gt);

            testPointA.add(testPointA.get(i));
            testPointB.add(testPointB.get(i));
            testPointC.add(testPointC.get(i));
        }

    }

    public void addQualitativeQuestions() {
        totalQualitativeQn = qualitativeTask.size();
        //remove the test properties
        removeTestProperties();
        removeProperty("Advance.Next");
        if (totalQualitativeQn == 0) {  //end the study if there are no qualitative questions
            endOfQualitativeQuestions();
        } else { //add the qualitative questions
            pfinish_study = new Property<PButton>("Qualitative.End Of Study", new PButton()) {
                @Override
                public boolean updating(PButton newvalue) {
                    endOfQualitativeQuestions();
                    return true;
                }
            };
            pfinish_study.setReadOnly(true);
            this.addProperty(pfinish_study);

            for (int i = 0; i < qualitativeTask.size(); i++) {
                String label = "Qualitative.Qn" + (i + 1);
                qualitativeTasksLabels.add(label);
                String qn = qualitativeTask.get(i);
                String qnType = qualitativeTaskType.get(i).trim();
                Property<PTextShort> p = new Property<PTextShort>(label, new PTextShort(qn));
                p.setReadOnly(true);
                this.addProperty(p);

                if (qnType.equalsIgnoreCase("rating")) {
                    addQualitativeRatingProperty(i);
                } else if (qnType.equalsIgnoreCase("boolean")) {
                    addQualitativeBoolean(i);
                } else if (qnType.equalsIgnoreCase("text")) {
                    addQualitativeText(i);
                }
            }

        }

    }

    public void addQualitativeRatingProperty(int indx) {

        final int index = indx;

        String label = "Qualitative.Answer for Qn" + (index + 1);

        qualitativeTasksLabels.add(label);

        Property<POptions> peaseRating = new Property<POptions>(label, new POptions(rating)) {
            @Override
            public boolean updating(POptions newvalue) {
                int rate = newvalue.selectedIndex;

                if (rate > 0) {
                    qualitativeTaskAnswers.put(index, rate + "");
                } else {
                    qualitativeTaskAnswers.remove(index);
                }
                checkAllQualitativeQnAnswered();
                return true;
            }
        };
        this.addProperty(peaseRating);
    }

    public void addQualitativeBoolean(int indx) {

        final int index = indx;

        String label = "Qualitative.Answer for Qn" + (index + 1);
        String labelYes = label + "  -   Yes";
        String labelNo = label + "   -   No";

        qualitativeTasksLabels.add(labelYes);
        qualitativeTasksLabels.add(labelNo);

        panswer_yes = new Property<PBoolean>(labelYes, new PBoolean(false)) {
            @Override
            public boolean updating(PBoolean newvalue) {
                boolean ans = ((PBoolean) newvalue).boolValue();
                if (ans) {
                    qualitativeTaskAnswers.put(index, "yes");
                    panswer_no.setReadOnly(true);
                } else {
                    qualitativeTaskAnswers.remove(index);
                    panswer_no.setReadOnly(false);
                }

                checkAllQualitativeQnAnswered();
                return true;
            }
        };
        this.addProperty(panswer_yes);

        panswer_no = new Property<PBoolean>(labelNo, new PBoolean(false)) {
            @Override
            public boolean updating(PBoolean newvalue) {
                boolean ans = ((PBoolean) newvalue).boolValue();

                if (ans) {
                    qualitativeTaskAnswers.put(index, "yes");
                    panswer_yes.setReadOnly(true);
                } else {
                    qualitativeTaskAnswers.remove(index);
                    panswer_yes.setReadOnly(false);
                }
                checkAllQualitativeQnAnswered();
                return true;
            }
        };
        this.addProperty(panswer_no);

    }

    public void addQualitativeText(int indx) {

        final int index = indx;

        String label = "Qualitative.Answer for Qn" + (index + 1);

        qualitativeTasksLabels.add(label);

        Property<PString> p = new Property<PString>(label, new PString("")) {
            @Override
            public boolean updating(PString newvalue) {
                String str = newvalue.serialize();
                String str2 = "";
                //replace the commas with a '-' 
                str2 = str.replace(',', '-');

                if (!str2.isEmpty()) {
                    qualitativeTaskAnswers.put(index, str2);
                } else {
                    qualitativeTaskAnswers.remove(index);
                }

                checkAllQualitativeQnAnswered();
                return true;
            }
        };
        this.addProperty(p);
    }

    public boolean anyInteractivityUsed() {

        if (!(mouseClickingUsed || mouseOverUsed || panningUsed || zoomingUsed || noInteractivityUsed)) {
            return false;
        } else {
            return true;
        }
    }

    public void checkAllQualitativeQnAnswered() {
        //check if the answers given equals to the questions given
        if (qualitativeTaskAnswers.size() == qualitativeTask.size()) {
            pfinish_study.setReadOnly(false);
        } else {
            pfinish_study.setReadOnly(true);
        }

    }

    public void endOfQualitativeQuestions() {

        for (int i = 0; i < qualitativeTasksLabels.size(); i++) {
            removeProperty(qualitativeTasksLabels.get(i));
        }

        removeProperty("Qualitative.End Of Study");

        /* removeProperty("Qualitative.End Of Study");
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
         removeProperty("Qualitative.PartB"); */
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
            // int halfOfTutorial = sizeOftutorial / 2;
            String taskName = "";

            ArrayList<String> lines = new ArrayList<String>();
            boolean firstTaskType = true;
            while ((line = br.readLine()) != null) {

                split = line.split(":");
                if (split[0].startsWith("#task")) {
                    //get the graph TaskEnum
                    if (split[1].trim().equalsIgnoreCase("neighbor")) {

                        if (!firstTaskType) {
                            setTasksForTaskType(taskName, lines);
                        }

                        lines = new ArrayList<String>();
                        taskName = "neighbor";
                        firstTaskType = false;
                    } else if (split[1].trim().equalsIgnoreCase("path_three_nodes")) {
                        if (!firstTaskType) {
                            setTasksForTaskType(taskName, lines);
                        }
                        lines = new ArrayList<String>();
                        taskName = "path_three_nodes";
                        firstTaskType = false;
                        //graphTaskType = GraphTaskEnum.PATH_THREE_NODES;
                    } else if (split[1].trim().equalsIgnoreCase("path_two_nodes")) {

                        if (!firstTaskType) {
                            setTasksForTaskType(taskName, lines);
                        }
                        lines = new ArrayList<String>();
                        taskName = "path_two_nodes";
                        firstTaskType = false;
                        //graphTaskType = GraphTaskEnum.PATH_TWO_NODES;
                    }

                    cnt++;

                    continue;
                }

                lines.add(line);

            }
            setTasksForTaskType(taskName, lines);

            // printTaskCorrectAns();
            br.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadTaskSpecifications() {

        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();

            BufferedReader br = new BufferedReader(new FileReader(new File(localDataDir + File.separator + TASKSPECIFICATIONFILE)));
            String line = " ";
            String[] split;

            while ((line = br.readLine()) != null) {
                split = line.split(",");
                taskAndSize.put(split[0], Integer.parseInt(split[1].trim()));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadQualitativeTasks() {
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();

            BufferedReader br = new BufferedReader(new FileReader(new File(localDataDir + File.separator + QLT_TASK_FILENAME)));
            String line = " ";
            String[] split;

            while ((line = br.readLine()) != null) {
                split = line.split(",");

                qualitativeTask.add(split[0]);
                qualitativeTaskType.add(split[1]);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadExpType() {

        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();

            BufferedReader br = new BufferedReader(new FileReader(new File(localDataDir + File.separator + EXPTYPE_FILENAME)));
            String line = " ";

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    expType = line.trim();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setTasksForTaskType(String taskType, ArrayList lines) {
        taskTypes.add(taskType);

        if (taskType.equalsIgnoreCase("neighbor")) {
            setNeighborTasks(lines);
        } else if (taskType.equalsIgnoreCase("path_three_nodes")) {
            setPathThreeNodesTasks(lines);
        }
    }

    public void setNeighborTasks(ArrayList<String> lines) {
        String taskName = "neighbor";
        GraphTaskEnum graphTaskType = GraphTaskEnum.NEIGHBOR;
        int taskSize = (Integer) taskAndSize.get(taskName);

        // System.out.println("The size of Neigbor is :: "+ size);
        //int tutorialCount = 0;
        //int testCount = 0;
        int count = 0;
        int testCount = 0;

        for (int i = 0; i < lines.size(); i++) {
            String split[] = lines.get(i).split("\t");

            int a = Integer.parseInt(split[0].trim());
            int b = Integer.parseInt(split[1].trim());
            int c = -1;

            String ans = "no";

            if (graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(b))) {
                ans = "yes";
            }
            GraphTask task = new GraphTask(graphTaskType, ans, currentExpCondition);

            count++;
            if (count <= sizeOftutorial) {
                tutPointA.add(a);
                tutPointB.add(b);
                tutPointC.add(c);
                tutorialTasks.add(task);
            } else {
                testCount++;
                testPointA.add(a);
                testPointB.add(b);
                testPointC.add(c);
                testTasks.add(task);
            }

            if (testCount == taskSize) { //break when you get to the size for the tasktype
                break;
            }

        }

    }

    public void setPathThreeNodesTasks(ArrayList<String> lines) {
        String taskName = "path_three_nodes";
        GraphTaskEnum graphTaskType = GraphTaskEnum.PATH_THREE_NODES;

        int taskSize = (Integer) taskAndSize.get(taskName);

        int count = 0;
        int testCount = 0;
        for (int i = 0; i < lines.size(); i++) {
            String split[] = lines.get(i).split("\t");

            int a = Integer.parseInt(split[0].trim());
            int b = Integer.parseInt(split[1].trim());
            int c = Integer.parseInt(split[2].trim());

            String ans = "no";
            if (((graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(b))
                    && graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(c)))
                    || (graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(c))
                    && graph.isEdge(graph.getNodes().get(b), graph.getNodes().get(c)))
                    || (graph.isEdge(graph.getNodes().get(a), graph.getNodes().get(b))
                    && graph.isEdge(graph.getNodes().get(b), graph.getNodes().get(c))))) {

                ans = "yes";
            }

            GraphTask task = new GraphTask(graphTaskType, ans, currentExpCondition);

            count++;

            if (count <= sizeOftutorial) {
                tutPointA.add(a);
                tutPointB.add(b);
                tutPointC.add(c);
                tutorialTasks.add(task);
            } else {
                testCount++;
                testPointA.add(a);
                testPointB.add(b);
                testPointC.add(c);
                testTasks.add(task);
            }

            if (testCount == taskSize) { //break when you get to the size for the tasktype
                break;
            }

        }

    }

    public void printTaskCorrectAns() {

        System.out.println("*************::::::::::::::::::********************");

        for (int i = 0; i < testTasks.size(); i++) {
            System.out.println(testPointA.get(i) + "\t" + testPointB.get(i) + "\t" + testPointC.get(i) + "\t: " + testTasks.get(i).getCorrectAns());
        }

        System.out.println("*************::::::::::::::::::********************");

    }

    @Override
    public void render(Graphics2D g) {
        if (!initTask.done) {
            return;
        }
        if (firstRender) {  //Load the positions of the file here, and set other properties the developer specified
            //System.out.println("FirstRender ****8");
            loadExpType();
            readPropChangesFile();
            loadTaskSpecifications();
            loadQualitativeTasks();
            loadTasks();
        addInstructionAndTutorial();

            //readPropChangesForControlCondition();
            firstRender = false;
        }

        super.render(g);

    }

    public void advanceStudy() {

        if ((testCounter + tutorialCounter) < (testTasks.size() + (tutorialTasks.size()))) {
            pnext.setReadOnly(true); //disable the next button until an answer is selected

            //add the tutorial properties.
            if (tutorialCounter < tutorialTasks.size()) {
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
                if (expType.equalsIgnoreCase(WITHIN) && (testCounter == testTasks.size() / 2)) {

                    if (firstExpCondition.equalsIgnoreCase(CONTROLCONDITION)) {
                        System.out.println("Second Condition is ::: Test***");
                        secondExpCondition = TESTCONDITION;
                        readPropChangesForTestCondition();
                    } else {
                        System.out.println("Second Condition is :::: Control *****");
                        secondExpCondition = CONTROLCONDITION;
                        readPropChangesForControlCondition();
                    }
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

                if ((testCounter + tutorialCounter) < (testTasks.size() + (tutorialTasks.size()))) { //increment the current task pointer if possible.
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

        p_instruction = new Property<PText>("Task. ", new PText(new GraphTask().getInstruction()));
        p_instruction.setReadOnly(false);
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
        //  writeAnswersToFile();
        writeAnswersToFile2();
        writeTurkIDToFile();

    }

    public void writeAnswersToFile() {
        //TODO. Write the anser into the answers file in the following format
        //Format:  TurkID, correctnessOfAns1, CorrectnessOfAns2, ..., correctnessOfAnsN

        //create a File with the name STUDY_RESULT and save that file in the data directory.
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();

            File controlStudyResults = new File(localDataDir + File.separator + QUANT_CONTROLCONDITION_RESULT_FILE);
            File testCondStudyResults = new File(localDataDir + File.separator + QUANT_TESTCONDITION_RESULT_FILE);

            boolean newFileC = false;
            boolean newFileT = false;
            if (!controlStudyResults.exists()) {
                // System.out.println("---------------");
                controlStudyResults.createNewFile();
                newFileC = true;
            }

            if (!testCondStudyResults.exists()) {
                testCondStudyResults.createNewFile();
                newFileT = true;
            }

            //do the actual writings of the results to the file
            FileWriter fileWritterCon = new FileWriter(controlStudyResults, true);
            BufferedWriter bwCon = new BufferedWriter(fileWritterCon);

            FileWriter fileWriterTest = new FileWriter(testCondStudyResults, true);
            BufferedWriter bwTest = new BufferedWriter(fileWriterTest);
            PrintWriter pwFirst;
            PrintWriter pwSecond;

            if (firstExpCondition.equalsIgnoreCase(CONTROLCONDITION)) {
                pwFirst = new PrintWriter(bwCon);
                pwSecond = new PrintWriter(bwTest);
            } else {
                pwFirst = new PrintWriter(bwTest);
                pwSecond = new PrintWriter(bwCon);
            }

            String expCondition = testTasks.get(0).getExperimentCondition();

            //print header
            if (newFileC) {

                // System.out.println("**expType is " + expType + " and WITHIN is " + WITHIN);
                if (expType.equalsIgnoreCase(WITHIN)) {
                    pwFirst.printf("TurkID, ExperimentCondition");
                    for (int i = 0; i < testTasks.size() / 2; i++) {
                        pwFirst.print(",Qn" + (i + 1));
                    }

                    pwSecond.print("TurkID, ExperimentCondition");
                    for (int i = testTasks.size() / 2; i < testTasks.size(); i++) {
                        pwSecond.print(",Qn" + (i + 1));
                    }

                } else {
                    //System.out.println("Betweeen!!!!1111");
                    pwFirst.printf("TurkID, ExperimentCondition");
                    for (int i = 0; i < testTasks.size(); i++) {
                        pwFirst.print(",Qn" + (i + 1));
                    }

                    pwSecond.printf("TurkID, ExperimentCondition");
                    for (int i = 0; i < testTasks.size(); i++) {
                        pwSecond.print(",Qn" + (i + 1));
                    }
                }
                pwFirst.println();
                pwSecond.println();
            }

            if (expType.equalsIgnoreCase(WITHIN)) {
                pwFirst.print(userTurkID); //the turkid is in the first column
                pwFirst.print("," + firstExpCondition);

                for (int i = 0; i < testTasks.size() / 2; i++) {
                    pwFirst.print("," + testTasks.get(i).isAnswerCorrect());
                }
                pwFirst.println();

                //expCondition = testTasks.get(testTasks.size() - 1).getExperimentCondition();
                pwSecond.print(userTurkID); //the turkid is in the first column
                pwSecond.print("," + secondExpCondition);

                for (int i = testTasks.size() / 2; i < testTasks.size(); i++) {
                    pwSecond.print("," + testTasks.get(i).isAnswerCorrect());
                }
                pwSecond.println();

            } else {
                //between study
                pwFirst.print(userTurkID); //the turkid is in the first column
                pwFirst.printf("," + firstExpCondition);

                for (int i = 0; i < testTasks.size(); i++) {
                    pwFirst.print("," + testTasks.get(i).isAnswerCorrect());
                }
                pwFirst.println();
            }
            //next entry should go to the next line.

            pwFirst.close();
            pwSecond.close();
            //close the streams
            pwFirst.close();
          //  bufferWritter.close();
            //fileWritter.close();

            //write the qualitative also to file
            File qual_studyResults = new File(localDataDir + File.separator + QLT_STUDY_RESULT_FILE);
            boolean newFile = false;
            if (!qual_studyResults.exists()) {
                controlStudyResults.createNewFile();
                newFile = true;
            }
            //do the actual writings of the results to the file
            FileWriter fileWritter = new FileWriter(qual_studyResults, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            pwFirst = new PrintWriter(bufferWritter);
            //print header

            if (newFile) {
                pwFirst.printf("TurkID");
                for (int i = 0; i < qualitativeTask.size(); i++) {
                    pwFirst.print("," + qualitativeTask.get(i));
                }
                pwFirst.println();
            }

            pwFirst.print(userTurkID);

            for (int i = 0; i < qualitativeTask.size(); i++) {
                pwFirst.print("," + qualitativeTaskAnswers.get(i));
            }

            pwFirst.println();

            //close the streams
            pwFirst.close();
            bufferWritter.close();
            fileWritter.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeAnswersToFile2() {
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();

            File controlStudyResults = new File(localDataDir + File.separator + QUANT_CONTROLCONDITION_RESULT_FILE);
            File testCondStudyResults = new File(localDataDir + File.separator + QUANT_TESTCONDITION_RESULT_FILE);

            boolean newFileC = false;
            boolean newFileT = false;
            if (!controlStudyResults.exists()) {
                // System.out.println("---------------");
                controlStudyResults.createNewFile();
                newFileC = true;
            }

            if (!testCondStudyResults.exists()) {
                testCondStudyResults.createNewFile();
                newFileT = true;
            }

            //do the actual writings of the results to the file
            FileWriter fileWritterCon = new FileWriter(controlStudyResults, true);
            BufferedWriter bwCon = new BufferedWriter(fileWritterCon);

            FileWriter fileWriterTest = new FileWriter(testCondStudyResults, true);
            BufferedWriter bwTest = new BufferedWriter(fileWriterTest);
            PrintWriter pwFirst;
            PrintWriter pwSecond;
            String firstStr = "", secondStr = "";
            if (firstExpCondition.equalsIgnoreCase(CONTROLCONDITION)) {

                pwFirst = new PrintWriter(bwCon);
                pwSecond = new PrintWriter(bwTest);
                firstStr = "-Con";
                secondStr = "-Test";
            } else {
                firstStr = "-Test";
                secondStr = "-Con";
                pwFirst = new PrintWriter(bwTest);
                pwSecond = new PrintWriter(bwCon);
            }

            //print header
            if (newFileC) {
                //write the headings
                pwFirst.print(taskTypes.get(0) + firstStr);
                for (int i = 1; i < taskTypes.size(); i++) {
                    pwFirst.print("," + taskTypes.get(i)+ firstStr);
                }
                pwSecond.print(taskTypes.get(0) + secondStr);
                for (int i = 1; i < taskTypes.size(); i++) {
                    pwSecond.print("," + taskTypes.get(i)+ secondStr);
                }
                pwFirst.println();
                pwSecond.println();
            }

            if (expType.equalsIgnoreCase(WITHIN)) {
                int j = 0;
                taskAndSize.get(taskTypes.get(j));
                int taskSize = (Integer) taskAndSize.get(taskTypes.get(j));
                cnt = 0;
                int numCorrect = 0;
                for (int i = 0; i < testTasks.size() / 2; i++) {
                    cnt++;
                    if (!(cnt <= taskSize)) {

                        if (j == 0) {
                            pwFirst.print((double) numCorrect / taskSize);
                        } else {
                            pwFirst.print("," + (double) numCorrect / taskSize);
                        }
                        taskSize = (Integer) taskAndSize.get(taskTypes.get(j));
                        j++;
                        cnt = 0;
                        numCorrect = 0;
                    }
                    if (testTasks.get(i).isAnswerCorrect()) {
                        numCorrect++;
                    }
                }
                pwFirst.print("," + (double) numCorrect / taskSize);
                pwFirst.println();

                //  pwSecond.print(userTurkID); //the turkid is in the first column
                j = 0;
                taskSize = (Integer) taskAndSize.get(taskTypes.get(j));
                cnt = 0;
                numCorrect = 0;

                for (int i = testTasks.size() / 2; i < testTasks.size(); i++) {
                    cnt++;
                    if (!(cnt <= taskSize)) {
                        if (j == 0) {
                            pwSecond.print((double) numCorrect / taskSize);
                        } else {
                            pwSecond.print("," + (double) numCorrect / taskSize);
                        }
                        taskSize = (Integer) taskAndSize.get(taskTypes.get(j));
                        j++;
                        cnt = 0;
                        numCorrect = 0;
                    }
                    if (testTasks.get(i).isAnswerCorrect()) {
                        numCorrect++;
                    }
                }
                pwSecond.print("," + (double) numCorrect / taskSize);
                pwSecond.println();
            } else {
                //between study
                int j = 0;
                taskAndSize.get(taskTypes.get(j));
                int taskSize = (Integer) taskAndSize.get(taskTypes.get(j));
                cnt = 0;
                int numCorrect = 0;

                //pwFirst.print(userTurkID); //the turkid is in the first column
                for (int i = 0; i < testTasks.size(); i++) {
                    cnt++;
                    if (!(cnt <= taskSize)) {
                        // taskCorrectness.add(cnt);
                        if (j == 0) {
                            pwFirst.print((double) numCorrect / taskSize);
                        } else {
                            pwFirst.print("," + (double) numCorrect / taskSize);
                        }
                        taskSize = (Integer) taskAndSize.get(taskTypes.get(j));
                        j++;
                        cnt = 0;
                        numCorrect = 0;

                    }
                    if (testTasks.get(i).isAnswerCorrect()) {
                        numCorrect++;
                    }
                }
                pwFirst.print("," + (double) numCorrect / taskSize);
                pwFirst.println();
            }
            //next entry should go to the next line.

            pwFirst.close();
            pwSecond.close();
            //close the streams
            pwFirst.close();
          //  bufferWritter.close();
            //fileWritter.close();

            //write the qualitative also to file
            File qual_studyResults = new File(localDataDir + File.separator + QLT_STUDY_RESULT_FILE);
            boolean newFile = false;
            if (!qual_studyResults.exists()) {
                controlStudyResults.createNewFile();
                newFile = true;
            }
            //do the actual writings of the results to the file
            FileWriter fileWritter = new FileWriter(qual_studyResults, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            pwFirst = new PrintWriter(bufferWritter);
            //print header

            if (newFile) {
                pwFirst.printf("TurkID");
                for (int i = 0; i < qualitativeTask.size(); i++) {
                    pwFirst.print("," + qualitativeTask.get(i));
                }
                pwFirst.println();
            }

            pwFirst.print(userTurkID);

            for (int i = 0; i < qualitativeTask.size(); i++) {
                pwFirst.print("," + qualitativeTaskAnswers.get(i));
            }

            pwFirst.println();

            //close the streams
            pwFirst.close();
            bufferWritter.close();
            fileWritter.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeTurkIDToFile() {
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();

            File turkIdsfile = new File(localDataDir + File.separator + "turkIDs.txt");
            File testCondStudyResults = new File(localDataDir + File.separator + QUANT_TESTCONDITION_RESULT_FILE);

            boolean newFileC = false;
            //boolean newFileT = false;
            if (!turkIdsfile.exists()) {
                // System.out.println("---------------");
                turkIdsfile.createNewFile();
                newFileC = true;
            }
            //do the actual writings of the results to the file
            FileWriter fileWritterCon = new FileWriter(turkIdsfile, true);
            BufferedWriter bwCon = new BufferedWriter(fileWritterCon);

            PrintWriter pwFirst;
            // PrintWriter pwSecond;
            pwFirst = new PrintWriter(bwCon);

            //print header
            if (newFileC) {
                //write the headings
                pwFirst.print("TurkId");

            }
            //print the turk id
            pwFirst.print(userTurkID);
            pwFirst.println();
            pwFirst.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void setStudyType(String stype) {
        expType = stype;
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

        if (!(expType.equalsIgnoreCase("Within") && (testCounter == testTasks.size() / 2))) {
            getContainer().resetTiles();
        }

        this.requestRender();

    }
}//end class
