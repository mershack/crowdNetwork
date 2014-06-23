/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphEvaluation;

import java.io.File;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author Mershack
 */
public class GraphStudyResultsEvaluation {

    final String QLT_STUDY_RESULT_FILE = "studyResults/qualitativeStudyResults.csv";
    final String QUANT_CONTROLCONDITION_RESULT_FILE = "studyResults/quantitativeControlConditionResults.txt";
    final String QUANT_TESTCONDITION_RESULT_FILE = "studyResults/quantitativeTestConditionResults.txt";

    public GraphStudyResultsEvaluation() {

    }

    public void generateGraphs() {
        String[] Rargs = {"--vanilla"};
        //This time, give the R engine a callback listener.
        Rengine re = new Rengine(Rargs, false, null);
        if (!re.waitForR()) {
            System.out.println("Cannot load R");
            return;
        }

        // re.eval("library(JavaGD)");
        //re.eval("Sys.putenv('JAVAGD_CLASS_NAME'='MyJavaGD1')");
        //re.eval("JavaGD()");
        re.eval("mydataCon = read.csv(\"" + QUANT_CONTROLCONDITION_RESULT_FILE + "\")");
        re.eval("mydataTest = read.csv(\"" + QUANT_TESTCONDITION_RESULT_FILE + "\")");
        re.eval("mydataAll = data.frame(mydataCon, mydataTest)");
        re.eval("mydataAll = mydataAll[,order(names(mydataAll))]");
        //re.eval("boxplot(mydataAll)");
        re.eval("png(filename=\"" + "studyResults/quantitativeResultsImage.png" + "\")");
        re.eval("boxplot(mydataAll)");
        re.eval("dev.off()");

        re.end();
    }

    public static void main(String args[]) {
        GraphStudyResultsEvaluation gse = new GraphStudyResultsEvaluation();
        gse.generateGraphs();
    }

}
