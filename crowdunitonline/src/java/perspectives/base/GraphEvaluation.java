package perspectives.base;

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
public class GraphEvaluation extends PropertyManager {

    Environment env;
    int cnt = 0;
    String controlViewerName;
    String testViewerName;
    String viewerNames[];
    private final String STUDY_DATA_FILENAME = "study_data_filename.txt";

    public GraphEvaluation(String name) {
        super(name);
    }

    public GraphEvaluation(String name, Environment env) {
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

            Property<PButton> p_studyBegin = new Property<PButton>("Study.Begin Study", new PButton()) {
                @Override
                public boolean updating(PButton newvalue) {
                    // setArffFilePath(newvalue.path);
                    return true;
                }
            };
            this.addProperty(p_studyBegin);

            Property<PString> p_mturk_uname = new Property<PString>("Mechanical Turk.Username", new PString(""));
            this.addProperty(p_mturk_uname);

            //TODO: control for password
            Property<PInteger> p_mturk_hits = new Property<PInteger>("Mechanical Turk.Size of Hits", new PInteger(12));
            this.addProperty(p_mturk_hits);

            Property<PDouble> p_mturk_reward = new Property<PDouble>("Mechanical Turk.Reward", new PDouble(0.02));
            this.addProperty(p_mturk_reward);

            Property<PDouble> p_mturk_bonus = new Property<PDouble>("Mechanical Turk.Bonus", new PDouble(0.00));
            this.addProperty(p_mturk_bonus);

            Property<POptions> p_exp_type = new Property<POptions>("Experiment.Type", new POptions(expType));
            this.addProperty(p_exp_type);

            Property<POptions> p_eval_criteria = new Property<POptions>("Experiment.Criteria", new POptions(evalMethods));
            this.addProperty(p_eval_criteria);

            Property<PBoolean> p4 = new Property<PBoolean>("Task.Neighbor", new PBoolean(false)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    // setArffFilePath(newvalue.path);
                    return true;
                }
            };
            this.addProperty(p4);

            Property<PInteger> p_neighb_size = new Property<PInteger>("Task.Size of Neighbor", new PInteger(50));
            this.addProperty(p_neighb_size);

            Property<PBoolean> p5 = new Property<PBoolean>("Task.Path", new PBoolean(false)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    // setArffFilePath(newvalue.path);
                    return true;
                }
            };
            this.addProperty(p5);

            Property<PInteger> p_path_size = new Property<PInteger>("Task.Size of Path", new PInteger(50));
            this.addProperty(p_path_size);

            Property<PBoolean> p6 = new Property<PBoolean>("Task.Dominant", new PBoolean(false)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    // setArffFilePath(newvalue.path);
                    return true;
                }
            };
            this.addProperty(p6);

            Property<PInteger> p_dominant_size = new Property<PInteger>("Task.Size of Dominant", new PInteger(50));
            this.addProperty(p_dominant_size);

            Property<PBoolean> p8 = new Property<PBoolean>("Interaction Allowed.Panning", new PBoolean(true)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    // setArffFilePath(newvalue.path);
                    return true;
                }
            };
            this.addProperty(p8);

            Property<PBoolean> p9 = new Property<PBoolean>("Interaction Allowed.Zooming", new PBoolean(true)) {
                @Override
                public boolean updating(PBoolean newvalue) {
                    // setArffFilePath(newvalue.path);
                    return true;
                }
            };
            this.addProperty(p9);

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
