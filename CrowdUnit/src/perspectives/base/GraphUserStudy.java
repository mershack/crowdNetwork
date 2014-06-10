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
    int maxAssignments = -1;
    double reward = -1.0;
    double bonus = -1.0;
    private final String STUDY_DATA_FILENAME = "study_data_filename.txt";

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
        String expType[] = {"Within Subjects", "Between Subjects"};

        viewerNames = new String[env.getViewers().size() + 1];
        viewerNames[0] = "Select One";

        for (int i = 0; i < env.getViewers().size(); i++) {
            viewerNames[i + 1] = env.getViewers().get(i).getName();
            //System.out.println(((GraphViewer)env.getViewers().get(i)).getDataFilePath());
        }
        try {

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
            Property<PInteger> p_mturk_hits = new Property<PInteger>("Mechanical Turk.Num of Assignments", new PInteger(5)) {
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

            Property<PDouble> p_mturk_bonus = new Property<PDouble>("Mechanical Turk.Bonus", new PDouble(0.00)) {
                @Override
                public boolean updating(PDouble newvalue) {
                    bonus = newvalue.doubleValue();
                    return true;
                }
            };
            this.addProperty(p_mturk_bonus);
            
            Property<PBoolean> p_qualitative1 = new Property<PBoolean>("Qualitative Questions.Rate easiness of the Visualization (1 - 5)", new PBoolean(false));
            this.addProperty(p_qualitative1);
            
            Property<PBoolean> p_qualitative2 = new Property<PBoolean>("Qualitative Questions.Did you have any problem with the Visualization", new PBoolean(false));
            this.addProperty(p_qualitative2);
            
            Property<PBoolean> p_qualitative3 = new Property<PBoolean>("Qualitative Questions.Rate how intuitive the tasks were  (1 - 5)", new PBoolean(false));
            this.addProperty(p_qualitative3);
            

            //browser-based tasks
            Property<PBoolean> p_brwstask1 = new Property<PBoolean>("Task.Browsing-Based.Follow a given path", new PBoolean(false));
            this.addProperty(p_brwstask1);

            Property<PBoolean> p_brwstask2 = new Property<PBoolean>("Task.Browsing-Based.Return to a previously visited node", new PBoolean(false));
            this.addProperty(p_brwstask2);

            Property<PInteger> p_browserBased_size = new Property<PInteger>("Task.Browsing-Based.Total size of Topology-based tasks", new PInteger(15));
            this.addProperty(p_browserBased_size);

            Property<PInteger> p_brws_time = new Property<PInteger>("Task.Browsing-Based.Duration for each Browser-based task(seconds)", new PInteger(120));
            this.addProperty(p_brws_time);

            //attribute based tasks
            Property<PBoolean> p_atttask1 = new Property<PBoolean>("Task.Attribute-Based.Find nodes having a specific attribute value", new PBoolean(false));
            this.addProperty(p_atttask1);

            Property<PBoolean> p_atttask2 = new Property<PBoolean>("Task.Attribute-Based.Review a set of nodes", new PBoolean(false));
            this.addProperty(p_atttask2);

            Property<PInteger> p_attributeBased_size = new Property<PInteger>("Task.Attribute-Based.Total size of Topology-based tasks", new PInteger(15));
            this.addProperty(p_attributeBased_size);

            Property<PInteger> p_att_time = new Property<PInteger>("Task.Attribute-Based.Duration for each Attribute-based task(seconds)", new PInteger(120));
            this.addProperty(p_att_time);

            //topology based tasks
            Property<PBoolean> p_toptask1 = new Property<PBoolean>("Task.Topology-Based.Find the set of nodes adjacent to a node", new PBoolean(false));
            this.addProperty(p_toptask1);

            Property<PBoolean> p_toptask2 = new Property<PBoolean>("Task.Topology-Based.How many nodes are adjacent to a node", new PBoolean(false));
            this.addProperty(p_toptask2);

            Property<PBoolean> p_toptask3 = new Property<PBoolean>("Task.Topology-Based.Which node has a maximum number of adjacent nodes", new PBoolean(false));
            this.addProperty(p_toptask3);

            Property<PBoolean> p_toptask4 = new Property<PBoolean>("Task.Topology-Based.Find a set of nodes connected to all given nodes", new PBoolean(false));
            this.addProperty(p_toptask4);

            Property<PBoolean> p_toptask5 = new Property<PBoolean>("Task.Topology-Based.Find the shortest path between two nodes", new PBoolean(false));
            this.addProperty(p_toptask5);

            Property<PInteger> p_topology_size = new Property<PInteger>("Task.Topology-Based.Total size of Topology-based tasks", new PInteger(15));
            this.addProperty(p_topology_size);

            Property<PInteger> p_top_time = new Property<PInteger>("Task.Topology-Based.Duration for each Topology-based task(seconds)", new PInteger(120));
            this.addProperty(p_top_time);

           
            Property<POptions> p_exp_type = new Property<POptions>("Experiment Type.Type", new POptions(expType));
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
            gustudyViewer.setPropChangesFile(controlViewerName + ".txt");

            env.addViewer(gustudyViewer);
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
