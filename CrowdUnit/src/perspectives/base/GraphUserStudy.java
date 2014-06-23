package perspectives.base;

import com.amazon.mturk.requester.MTurkRequestsMgr;
import graphEvaluation.GraphUserStudyViewer;
import graphEvaluation.GraphUserStudyViewerFactory;
import java.awt.Color;
import java.awt.Container;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JButton;
import javax.swing.JDialog;
import perspectives.graph.GraphViewer;
import perspectives.properties.PBoolean;
import perspectives.properties.PButton;
import perspectives.properties.PDouble;
import perspectives.properties.PFileInput;
import perspectives.properties.PInteger;
import perspectives.properties.POptions;
import perspectives.properties.PString;

/**
 *
 * @author mershack
 */
public class GraphUserStudy extends PropertyManager {

    Environment env;
    int cnt = 0;
    String controlViewerName;
    String testViewerName;
    String viewerNames[];
    String awsAccessKey = "";
    String secretKey = "";
    String title = "";
    int maxAssignments = 10; //this is for testing purposes, but it will be set when the hit is created
    double reward = -1.0;
    double bonus = -1.0;
    HashMap taskNamesAndSizes = new HashMap();
    ArrayList<String> qualitativeQuestion = new ArrayList<String>();
    ArrayList<String> qualitativeQuestionTaskType = new ArrayList();
    private final String STUDY_DATA_FILENAME = "data/study_data_filename.txt";
    private final String TASK_SPECIFICATION_FILENAME = "taskSpecification.txt";
    private final String QLT_TASK_FILENAME = "qualitativeTasks.txt";
    private final String EXPTYPE_FILENAME = "expType.txt";
    private final String PROPERTY_CHANGES_FILE = "propertyChangesFile.txt";
    private final String TOTAL_ASSIGNMENTS_FILE = "totalAssignments.txt";
    private String expType = "Between Subjects";
    private String expTypeArr[] = {"Between Subjects", "Within Subjects"};

    public GraphUserStudy(String name) {
        super(name);
    }

    public GraphUserStudy(String name, Environment env) {
        super(name);
        this.env = env;
        createProperties();
    }

    protected void createProperties() {
        // String vizs[] = {"Select One", "Parallel Coordinates", "HeatMap", "Graph"};

        //String tasks[] = {"Select One", "find nearest neighbor", "find dominant node", "find number of neighboring nodes"};
        String evalMethods[] = {"Select One", "Accuracy", "Response Time", "Likert Scale"};
        String evalMethods2[] = {"Select One", "Accuracy", "Response Time", "Likert Scale", "Accuracy & Likert Scale", "Accuracy & ResponseTime", "Response time & Likert Scale"};
        String numAttr[] = {"Select one", "one", "two"};
        String testAttr[] = {"attribute1", "attribute2", "attribute3"};
        expTypeArr[0] = "Between Subjects";
        expTypeArr[1] = "Within Subjects";

        viewerNames = new String[env.getViewers().size() + 1];
        viewerNames[0] = "Select One";

        for (int i = 0; i < env.getViewers().size(); i++) {
            viewerNames[i + 1] = env.getViewers().get(i).getName();
            //System.out.println(((GraphViewer)env.getViewers().get(i)).getDataFilePath());
        }
        try {

            Property<PButton> p_generateTaskFiles = new Property<PButton>("Study.Generate Task Files", new PButton()) {
                @Override
                public boolean updating(PButton newvalue) {
                    generateTaskSpecificationFiles();
                    return true;
                }
            };
            this.addProperty(p_generateTaskFiles);

            Property<PButton> p_studyPreview = new Property<PButton>("Study.Preview", new PButton()) {
                @Override
                public boolean updating(PButton newvalue) {
                    showTestPreview();
                    return true;
                }
            };
            this.addProperty(p_studyPreview);

            Property<PButton> p_studyBegin = new Property<PButton>("Study.create HIT", new PButton()) {
                @Override
                public boolean updating(PButton newvalue) {
                    //
                    createMTurkHIT();
                    return true;
                }
            };
            this.addProperty(p_studyBegin);

            Property<PString> p_mturk_accesskey = new Property<PString>("Mechanical Turk.Access Key", new PString(""));
            this.addProperty(p_mturk_accesskey);

            Property<PString> p_mturk_secretkey = new Property<PString>("Mechanical Turk.Secret Key", new PString(""));
            this.addProperty(p_mturk_secretkey);
            Property<PString> p_mturk_title = new Property<PString>("Mechanical Turk.HIT Title", new PString("")) {
                @Override
                public boolean updating(PString newvalue) {
                    title = newvalue.serialize();
                    return true;
                }
            };
            this.addProperty(p_mturk_title);

            //TODO: control for password
            Property<PInteger> p_mturk_hits = new Property<PInteger>("Mechanical Turk.Num of Assignments", new PInteger(20)) {
                @Override
                public boolean updating(PInteger newvalue) {
                    maxAssignments = newvalue.intValue();
                    return true;
                }
            };
            this.addProperty(p_mturk_hits);

            Property<PDouble> p_mturk_reward = new Property<PDouble>("Mechanical Turk.Reward", new PDouble(0.00)) {
                @Override
                public boolean updating(PDouble newvalue) {
                    reward = newvalue.doubleValue();
                    return true;
                }
            };
            this.addProperty(p_mturk_reward);

            Property<PString> p_serverURL = new Property<PString>("Host Server.Server URL", new PString("")) {
                @Override
                public boolean updating(PString newvalue) {

                    return true;
                }
            };
            this.addProperty(p_serverURL);

            Property<PBoolean> p_qualitative1 = new Property<PBoolean>("Qualitative Questions.Rate easiness of the Visualization tasks (1 - 5)", new PBoolean(false)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    String qn = "Rate how easy the Visualization tasks were between 1 - 5 (where 1 means Not-Easy)"
                            + " and 5 means Very-Easy";
                    String taskType = "Rating";
                    if (newvalue.boolValue()) {//add to the list
                        qualitativeQuestion.add(qn);
                        qualitativeQuestionTaskType.add(taskType);

                    } else { //remove them from the list
                        int index = -1;
                        for (int i = 0; i < qualitativeQuestion.size(); i++) {
                            if (qualitativeQuestion.get(i).equalsIgnoreCase(qn)) {
                                index = i;
                            }
                        }
                        if (index > -1) {
                            qualitativeQuestion.remove(index);
                            qualitativeQuestionTaskType.remove(index);
                        }
                    }

                    return true;
                }
            };
            this.addProperty(p_qualitative1);

            Property<PBoolean> p_qualitative2 = new Property<PBoolean>("Qualitative Questions.Did you have any problem with the Visualization", new PBoolean(false)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    String qn = "Did you have any problem with the Visualization?";
                    String taskType = "boolean";

                    if (newvalue.boolValue()) {//add to the list
                        qualitativeQuestion.add(qn);
                        qualitativeQuestionTaskType.add(taskType);

                    } else { //remove them from the list
                        int index = -1;
                        for (int i = 0; i < qualitativeQuestion.size(); i++) {
                            if (qualitativeQuestion.get(i).equalsIgnoreCase(qn)) {
                                index = i;
                            }
                        }
                        if (index > -1) {
                            qualitativeQuestion.remove(index);
                            qualitativeQuestionTaskType.remove(index);
                        }
                    }

                    return true;
                }
            };
            this.addProperty(p_qualitative2);

            Property<PBoolean> p_qualitative2_ = new Property<PBoolean>("Qualitative Questions.What problem did you have with the visualization? Write No if none", new PBoolean(false)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    String qn = "What problem did you have with the visualization? Write No if none.";
                    String taskType = "text";

                    if (newvalue.boolValue()) {//add to the list
                        qualitativeQuestion.add(qn);
                        qualitativeQuestionTaskType.add(taskType);

                    } else { //remove them from the list
                        int index = -1;
                        for (int i = 0; i < qualitativeQuestion.size(); i++) {
                            if (qualitativeQuestion.get(i).equalsIgnoreCase(qn)) {
                                index = i;
                            }
                        }
                        if (index > -1) {
                            qualitativeQuestion.remove(index);
                            qualitativeQuestionTaskType.remove(index);
                        }
                    }

                    return true;
                }
            };
            this.addProperty(p_qualitative2_);

            Property<PBoolean> p_qualitative3 = new Property<PBoolean>("Qualitative Questions.Rate easiness of using the interactive techniques", new PBoolean(false)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    String qn = "Rate how the easines of using the interactive techniques between 1 - 5 (where 1 means Not-Easy and 5 means Very-Easy)";
                    String taskType = "Rating";

                    if (newvalue.boolValue()) {//add to the list
                        qualitativeQuestion.add(qn);
                        qualitativeQuestionTaskType.add(taskType);

                    } else { //remove them from the list
                        int index = -1;
                        for (int i = 0; i < qualitativeQuestion.size(); i++) {
                            if (qualitativeQuestion.get(i).equalsIgnoreCase(qn)) {
                                index = i;
                            }
                        }
                        if (index > -1) {
                            qualitativeQuestion.remove(index);
                            qualitativeQuestionTaskType.remove(index);
                        }
                    }

                    return true;
                }
            };
            this.addProperty(p_qualitative3);

            Property<PBoolean> p_qualitative4 = new Property<PBoolean>("Qualitative Questions.Rate helpfulness of the interactive techniques to tasks", new PBoolean(false)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    String qn = "Rate how helpful the interactive techniques were to tasks between 1 - 5 (where 1 means Not-Helpful) and 5 means Very-Helpful)";

                    String taskType = "Rating";

                    if (newvalue.boolValue()) {//add to the list
                        qualitativeQuestion.add(qn);
                        qualitativeQuestionTaskType.add(taskType);

                    } else { //remove them from the list
                        int index = -1;
                        for (int i = 0; i < qualitativeQuestion.size(); i++) {
                            if (qualitativeQuestion.get(i).equalsIgnoreCase(qn)) {
                                index = i;
                            }
                        }
                        if (index > -1) {
                            qualitativeQuestion.remove(index);
                            qualitativeQuestionTaskType.remove(index);
                        }
                    }
                    return true;
                }
            };
            this.addProperty(p_qualitative4);

            //browser-based tasks            
            Property<PInteger> p_brwstask1 = new Property<PInteger>("Task.Browsing-Based Tasks (Select Size).# of nodes on a given path starting with a letter", new PInteger(0));
            this.addProperty(p_brwstask1);

            Property<PInteger> p_brwstask2 = new Property<PInteger>("Task.Browsing-Based Tasks (Select Size).# nodes starting with a letter on paths of 2 nodes", new PInteger(0));
            this.addProperty(p_brwstask2);

            /*  Property<PInteger> p_browserBased_size = new Property<PInteger>("Task.Browsing-Based Tasks (Select Size).Total size of Browser-based tasks", new PInteger(20));
             this.addProperty(p_browserBased_size);  */
            Property<PInteger> p_brws_time = new Property<PInteger>("Task.Browsing-Based Tasks (Select Size).Duration for each Browser-based task(seconds)", new PInteger(120));
            this.addProperty(p_brws_time);

            //attribute based tasks
            Property<PInteger> p_atttask1 = new Property<PInteger>("Task.Attribute-Based Tasks (Select Size).Is there an adjacent node starting with a letter", new PInteger(0));
            this.addProperty(p_atttask1);

            Property<PInteger> p_atttask2 = new Property<PInteger>("Task.Attribute-Based Tasks (Select Size).Find size of adjacent nodes starting with a letter", new PInteger(0));
            this.addProperty(p_atttask2);

            /*   Property<PInteger> p_attributeBased_size = new Property<PInteger>("Task.Attribute-Based Tasks (Select Size).Total size of Topology-based tasks", new PInteger(20));
             this.addProperty(p_attributeBased_size);  */
            Property<PInteger> p_att_time = new Property<PInteger>("Task.Attribute-Based Tasks (Select Size).Duration for each Attribute-based task(seconds)", new PInteger(120));
            this.addProperty(p_att_time);

            //topology based tasks
            Property<PInteger> p_neighbor = new Property<PInteger>("Task.Topology-Based Tasks (Select Size).Are two given nodes directly connected", new PInteger(0)) {
                @Override
                public boolean updating(PInteger newvalue) {
                    String taskname = "neighbor";
                    int size = newvalue.intValue();
                    taskNamesAndSizes.put(taskname, size);

                    return true;
                }
            };
            this.addProperty(p_neighbor);

            Property<PInteger> p_path1 = new Property<PInteger>("Task.Topology-Based Tasks (Select Size).Is there a direct path between 3 given nodes", new PInteger(0)) {
                @Override
                public boolean updating(PInteger newvalue) {
                    String name = "path_three_nodes";
                    int size = newvalue.intValue();
                    taskNamesAndSizes.put(name, size);
                    return true;
                }
            };
            this.addProperty(p_path1);

            Property<PInteger> p_path2 = new Property<PInteger>("Task.Topology-Based Tasks (Select Size).Is there a path between two given nodes", new PInteger(0)) {
                @Override
                public boolean updating(PInteger newvalue) {
                    String name = "path_two_nodes";
                    int size = newvalue.intValue();
                    taskNamesAndSizes.put(name, size);
                    return true;
                }
            };
            this.addProperty(p_path2);

            Property<PInteger> p_toptask2 = new Property<PInteger>("Task.Topology-Based Tasks (Select Size).How many nodes are adjacent to a given node", new PInteger(0));
            this.addProperty(p_toptask2);

            Property<PInteger> p_toptask3 = new Property<PInteger>("Task.Topology-Based Tasks (Select Size).Maximum adjacent nodes for two given nodes", new PInteger(0));
            this.addProperty(p_toptask3);

            /* Property<PInteger> p_topology_size = new Property<PInteger>("Task.Topology-Based.Total size of Topology-based tasks", new PInteger(20));
             this.addProperty(p_topology_size);  */
            Property<PInteger> p_top_time = new Property<PInteger>("Task.Topology-Based Tasks (Select Size).Duration for each Topology-based task(seconds)", new PInteger(120));
            this.addProperty(p_top_time);

            Property<POptions> p_exp_type = new Property<POptions>("Experiment Type.Type", new POptions(expTypeArr)) {
                @Override
                public boolean updating(POptions newvalue) {
                    expType = expTypeArr[newvalue.selectedIndex];
                    writeDataSourceNameToFile(controlViewerName);
                    return true;
                }
            };
            this.addProperty(p_exp_type);

            if (viewerNames.length > 0) {
                Property<POptions> p_vnames_c = new Property<POptions>("Viewers.Control", new POptions(viewerNames)) {
                    @Override
                    public boolean updating(POptions newvalue) {
                        controlViewerName = viewerNames[newvalue.selectedIndex];
                        writeDataSourceNameToFile(controlViewerName);
                        return true;
                    }
                };
                this.addProperty(p_vnames_c);

                Property<POptions> p_vnames_e = new Property<POptions>("Viewers.Test", new POptions(viewerNames)) {
                    @Override
                    public boolean updating(POptions newvalue) {
                        testViewerName = viewerNames[newvalue.selectedIndex];
                        return true;
                    }
                };
                this.addProperty(p_vnames_e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showTestPreview() {
        //get the datasource of the first viewer.       
        DataSource ds = null;
        String dsname = "";
        String viewerName = "preview" + cnt++;
        for (int i = 0; i < env.getViewers().size(); i++) {

            if (env.getViewers().get(i).getName().equalsIgnoreCase(controlViewerName)) {
                dsname = ((GraphViewer) env.getViewers().get(i)).getDataSourceName();
            }

        }
        //get the datasource
        for (int i = 0; i < env.getDataSources().size(); i++) {

            if (env.getDataSources().get(i).getName().equalsIgnoreCase(dsname)) {
                //System.out.println("Yay!");
                ds = env.getDataSources().get(i);
            }
        }

        GraphUserStudyViewerFactory gustudyFactory = new GraphUserStudyViewerFactory();
        gustudyFactory.addDataSource(ds);

        GraphUserStudyViewer gustudyViewer;
        if (gustudyFactory.isAllDataPresent()) {
            //create the viewer
            gustudyViewer = (GraphUserStudyViewer) gustudyFactory.create(viewerName);
            gustudyViewer.setPropertyManagerGroup(env);
            //set the fileName for the property changes
            //gustudyViewer.setPropChangesFile(controlViewerName + ".txt");

            env.addViewer(gustudyViewer);
        }
    }

    public void generateTaskSpecificationFiles() {
        try {
            FileWriter fileWritter = new FileWriter(new File("data/" + TASK_SPECIFICATION_FILENAME));
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            PrintWriter pw = new PrintWriter(bufferWritter);

            ArrayList<Integer> taskSizes = new ArrayList<Integer>(taskNamesAndSizes.values());
            ArrayList<String> taskNames = new ArrayList<String>(taskNamesAndSizes.keySet());

            for (int i = 0; i < taskSizes.size(); i++) {
                pw.println(taskNames.get(i) + ", " + taskSizes.get(i));
                //System.out.println("*** "+taskNames.get(i)+ "\t"+ taskSizes.get(i));
            }
            pw.close();

            //write the qualitative tasks to file as well
            fileWritter = new FileWriter(new File("data/" + QLT_TASK_FILENAME));
            bufferWritter = new BufferedWriter(fileWritter);

            pw = new PrintWriter(bufferWritter);

            for (int i = 0; i < qualitativeQuestion.size(); i++) {
                pw.println(qualitativeQuestion.get(i) + ", " + qualitativeQuestionTaskType.get(i));
            }

            pw.close();

            //write the expType to file
            fileWritter = new FileWriter(new File("data/" + EXPTYPE_FILENAME));
            bufferWritter = new BufferedWriter(fileWritter);

            pw = new PrintWriter(bufferWritter);

            pw.println(expType);

            pw.close();

            //write the viewer property changes to file as well
            FileWriter fileWritter1 = new FileWriter(new File("data/" + PROPERTY_CHANGES_FILE));
            BufferedWriter bufferWritter1 = new BufferedWriter(fileWritter1);

            pw = new PrintWriter(bufferWritter1);

            pw.println(controlViewerName + ".txt");
            pw.println(testViewerName + ".txt");

            pw.close();

            //write total assignments to file too
            FileWriter fileWritter2 = new FileWriter(new File("data/" + TOTAL_ASSIGNMENTS_FILE));
            BufferedWriter bufferWritter2 = new BufferedWriter(fileWritter2);

            pw = new PrintWriter(bufferWritter2);

            pw.println(maxAssignments);
        //    pw.println(controlViewerName + ".txt");
            //  pw.println(testViewerName + ".txt");

            pw.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * prepare and call a method to create the mturk HIT
     */
    public void createMTurkHIT() {
        MTurkRequestsMgr mturkrequestMgr = new MTurkRequestsMgr();
        //set the other variables if they are not null or empty
        if (!title.isEmpty()) {
            mturkrequestMgr.setTitle(title);
        }
        if (reward >= 0.0) {
            mturkrequestMgr.setReward(reward + "");
        }
        if (!awsAccessKey.isEmpty()) {
            mturkrequestMgr.setAwsAccessKey(awsAccessKey);
        }
        if (!secretKey.isEmpty()) {
            mturkrequestMgr.setSecretKey(secretKey);
        }
        //request the hit to e created
        mturkrequestMgr.createHITRequest();

    }

    public void writeDataSourceNameToFile(String vname) {
        String data_filename = "";
        String filepath;

        try {
            //get the datasourcename of the viewer
            for (int i = 0; i < env.getViewers().size(); i++) {
                if (env.getViewers().get(i).getName().equalsIgnoreCase(vname)) {
                    filepath = ((GraphViewer) env.getViewers().get(i)).getDataFilePath();
                    String split[];
                    if (filepath.split("\\\\").length > 0) {
                        split = filepath.split("\\\\");
                    } else {
                        split = filepath.split("/");
                    }

                    data_filename = split[split.length - 1];
                }
                //System.out.println(((GraphViewer)env.getViewers().get(i)).getDataFilePath());
            }
            // System.out.pprin
            FileWriter fileWritter = new FileWriter(new File(STUDY_DATA_FILENAME));
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            PrintWriter pw = new PrintWriter(bufferWritter);

            pw.println(data_filename);

            pw.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
