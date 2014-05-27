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
import perspectives.properties.PString;
import perspectives.properties.PText;
import perspectives.two_d.ViewerContainer2D;
import userstudy.UserStudyUtility;
//import util.Points2DViewer.PointAspectType;

public class GraphUserStudyViewer extends GraphViewer {

    ArrayList<Integer> pointA;
    ArrayList<Integer> pointB;
    //final String TASKFILE = "tasks.txt";
    final String TASKFILE = "tasks2.txt";
    private String propChangesFile = "viewer1.txt";
    private final String ANSWER_YES = "yes";
    private final String ANSWER_NO = "no";
    private final String TURK_CODE = "EDOCDOOG";
    private final String STUDY_RESULT_FILE = "StudyResults.txt";

    int testCounter = 0;
    String answer;
    Property<PBoolean> panswer_no;
    Property<PBoolean> panswer_yes;
    Property<PText> ptask;
    Property<PButton> pnext;
    Property<PButton> p_check_answer;
    Property<PString> p_answer;

    ArrayList<GraphTask> allTasks = new ArrayList<GraphTask>();
    ArrayList<GraphTask> tutorialTasks = new ArrayList<GraphTask>();
    public int sizeOftutorial = 6;

    boolean isTutorial = false;
    boolean isTest = false;
    int tutorialCounter = 0;

    String userTurkID = "";
    int showedNode1, showedNode2;

    boolean firstRender = true;
    boolean secondRender = true;
    int cnt = 0;
    int testNodeA = -1;
    int testNodeB = -1;
    Color testNodeColor = Color.red;
    Color OtherNodesColor = Color.lightGray;

    Property<PString> ptask_status;

    //  Graphics2D myg;
    boolean advance = false;

    //String localDataDir = "";
    /////////////////////////////                 Constructor             //////////////////////////////
    public GraphUserStudyViewer(String name, GraphData g) {
        super(name, g);
        addInstructionAndTutorial();
    }

    public void addInstructionAndTutorial() {
        //instruction about the tutorial
        String tutinstr = "There are 2 sections in this task. "
                + "You will be given a simple tutorial involving  questions for each of the sections. "
                + "During this tutorial session, you will be given the correct answer. "
                + "";

        Property<PText> ptutorial = new Property<PText>("Tutorial.Intruction", new PText(tutinstr));
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
        removeProperty("Tutorial.Intruction");
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
        removeProperty("Task.Qn ");
        removeProperty("Task. ");
        removeProperty("#Task: ");
    }

    /**
     * remove the turkId textbox when that value is provided
     */
    public void removeTurkID() {

        this.removeProperty("Enter your Turk ID:");

    }

    public void readPropChangesFileAndUpdate() {
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();
            File propFile = new File(localDataDir + File.separator + propChangesFile);

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
                        System.out.println("::::::: " + propValue);
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
        addTestProperties();
        advanceStudy();
    }

    public void loadTasks() {
        try {
            String localDataDir = this.getContainer().getEnvironment().getLocalDataPath();

            //System.out.println("::: localDirPath::" + localDataDir);
            BufferedReader br = new BufferedReader(new FileReader(new File(localDataDir + File.separator + TASKFILE)));
            String line = " ";
            String split[];
            pointA = new ArrayList<Integer>();
            pointB = new ArrayList<Integer>();
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
                cnt++;
                if (cnt <= halfOfTutorial) {
                    tutorialTasks.add(task);
                } else {
                    allTasks.add(task);
                }

            }

            br.close();

//            System.out.println("Size of pointA " + pointA.size());
//            System.out.println("Size of pointB " + pointB.size());
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
            loadTasks();
            readPropChangesFileAndUpdate();
            firstRender = false;
        }

        super.render(g);

    }

    public void advanceStudy() {

        if ((testCounter + tutorialCounter) < pointA.size()) {
            pnext.setReadOnly(true); //disable the next button until an answer is selected

            //add the tutorial properties.
            if (tutorialCounter < sizeOftutorial) {
                isTutorial = true;

                testNodeA = pointA.get(tutorialCounter);
                testNodeB = pointB.get(tutorialCounter);//[testCounter];
                showUserStudy(testNodeA, testNodeB);

                addTutorialProperties();
                p_check_answer.setReadOnly(true);
                tutorialCounter++;

            } else if (!isTest) {      //end tutorial before the actual study begins.                 
                endTutorial();
                isTest = true;
            } else {
                testNodeA = pointA.get(testCounter);
                testNodeB = pointB.get(testCounter);//[testCounter];
                
                showUserStudy(testNodeA, testNodeB);

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

                if ((testCounter + tutorialCounter) < pointA.size()) { //increment the current task pointer if possible.
                    //add questions and the answer options
                    addQuestionAndOptions();
                    if (isTutorial) {
                        addTutorialProperties();
                    }
                    advanceStudy();

                    //requestRender();
                } else {
                    endOfStudy();
                    //remove the next button also
                    removeProperty("Advance.Next");
                }

                return true;
            }

            /*     @Override
             protected void receivedBroadcast(PButton newvalue, PropertyManager sender) {
             this.setValue(newvalue);

             }  */
        };
        //pnext.setPublic(true);
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
                        allTasks.get(testCounter - 1).setGivenAns(ANSWER_YES);
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

                        allTasks.get(testCounter - 1).setGivenAns(ANSWER_NO);
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

       // S
        if (!isTutorial) {
            System.out.println("^^^^^^^^^^^^Test counter is ::::: " + testCounter);
            currentNum = testCounter + 1;
            totalNum = pointA.size() - sizeOftutorial;
        } else {
            currentNum = tutorialCounter + 1;
            totalNum = sizeOftutorial;
        }

        ptask_status = new Property<PString>("Task.Qn ", new PString("(" + currentNum + "/" + totalNum + "): ")) {
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
        ptask.setDisabled(true);
        this.addProperty(ptask);

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

            if (!studyResults.exists()) {
                studyResults.createNewFile();
            }
            //do the actual writings of the results to the file
            FileWriter fileWritter = new FileWriter(studyResults, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            PrintWriter pw = new PrintWriter(bufferWritter);

            pw.print(userTurkID); //the turkid is in the first column

            for (GraphTask gtask : allTasks) {
                pw.print("," + gtask.isAnswerCorrect());
            }
            pw.println(); //next entry should go to the next line.

            //close the streams
            pw.close();
            bufferWritter.close();
            fileWritter.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* public void removeTestProperties() {
     this.removeProperty("Advance.Next");
     this.removeProperty("Answer.Yes");
     this.removeProperty("Answer.No");
     this.removeProperty("Task.Qn ");
     this.removeProperty("Task. ");
     } */
    public void setPropChangesFile(String propChangesFile) {
        //System.out.println("Yay!");
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

    }
    int cnt2 = 0;

    public void showUserStudy(int index1, int index2) {
        //System.out.println(index1 + " " + index2);

        //   System.out.println("StudyCount::::: "+ (cnt2++));
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
