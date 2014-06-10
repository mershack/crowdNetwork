/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package perspectives.base;

import com.amazon.mturk.requester.MTurkRequestsMgr;
import graphEvaluation.GraphUserStudyViewer;
import graphEvaluation.GraphUserStudyViewerFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import perspectives.graph.GraphViewer;
import perspectives.properties.PBoolean;
import perspectives.properties.PButton;
import perspectives.properties.PDouble;
import perspectives.properties.PInteger;
import perspectives.properties.POptions;
import perspectives.properties.PString;

/**
 *
 * @author Mershack
 */
public class UserStudyResultsEvaluation extends PropertyManager {

    Environment env;

    public UserStudyResultsEvaluation(String name) {
        super(name);
    }

    public UserStudyResultsEvaluation(String name, Environment env) {
        super(name);
        this.env = env;
        createProperties();
    }

    protected void createProperties() {

        Property<PString> p_hitTitle = new Property<PString>("User-Study.HIT title", new PString(""));
        this.addProperty(p_hitTitle);

        Property<PString> p_hostUrl = new Property<PString>("User-Study.Host URL for the study", new PString(""));
        this.addProperty(p_hostUrl);
        
        Property<PButton> p_showResults = new Property<PButton>("User-Study.Show Results", new PButton());
        this.addProperty(p_showResults);
        
    }

    public void runRscripts() {

    }

}
