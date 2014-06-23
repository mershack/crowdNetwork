/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package perspectives.base;

import com.amazon.mturk.requester.MTurkRequestsMgr;
import graphEvaluation.GraphUserStudyViewer;
import graphEvaluation.GraphUserStudyViewerFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import perspectives.graph.GraphViewer;
import perspectives.properties.PBoolean;
import perspectives.properties.PButton;
import perspectives.properties.PDouble;
import perspectives.properties.PInteger;
import perspectives.properties.POptions;
import perspectives.properties.PString;
import perspectives.properties.PText;

/**
 *
 * @author Mershack
 */
public class UserStudyResultsEvaluation extends PropertyManager {

    Environment env;
    private String hostURL = "";
    private final String QLT_STUDY_RESULT_FILE = "qualitativeStudyResults.txt";
    private final String QUANT_CONTROLCONDITION_RESULT_FILE = "quantitativeControlConditionResults.txt";
    private final String QUANT_TESTCONDITION_RESULT_FILE = "quantitativeTestConditionResults.txt";
    private final String QUANT_TEXT_RESULT_FILE = "textResult.txt";
    Rengine re;
    Property<PText> p_textResult;

    public UserStudyResultsEvaluation(String name) {
        super(name);
    }

    public UserStudyResultsEvaluation(String name, Environment env) {
        super(name);
        this.env = env;
        createProperties();

        String[] Rargs = {"--vanilla"};
        //This time, give the R engine a callback listener.
        re = new Rengine(Rargs, false, null);
        if (!re.waitForR()) {
            System.out.println("Cannot load R");
            //return null;
        }
    }

    protected void createProperties() {
        /* Property<PString> p_hitTitle = new Property<PString>("User-Study.HIT title", new PString(""));
         this.addProperty(p_hitTitle); */
        Property<PString> p_hostUrl = new Property<PString>("User-Study.Host URL for the study", new PString("")) {
            @Override
            public boolean updating(PString newvalue) {
                hostURL = newvalue.stringValue();
                return true;
            }
        };
        this.addProperty(p_hostUrl);

        Property<PButton> p_downloadResultFiles = new Property<PButton>("User-Study.Download Results", new PButton()) {
            @Override
            public boolean updating(PButton newvalue) {
                //call the download Results file;
                String result = "";
                result += downloadStudyResultsFile() + "\n---------------------\n\n";
                result += generateResultGraphs() + "\n---------------------\n\n";
                result += generateShapiroWilk() + "\n---------------------\n\n";
                result += generateTTest() + "\n---------------------\n\n";

                removeProperty("User-Study. ");

                p_textResult = new Property<PText>("User-Study. ", new PText(result));
                p_textResult.setReadOnly(true);
                addProperty(p_textResult);
                return true;
            }
        };
        this.addProperty(p_downloadResultFiles);

        Property<PButton> p_genGraphs = new Property<PButton>("User-Study.Generate Graphs", new PButton()) {
            @Override
            public boolean updating(PButton newvalue) {
                String result = generateResultGraphs();
                removeProperty("User-Study. ");

                p_textResult = new Property<PText>("User-Study. ", new PText(result));
                p_textResult.setReadOnly(true);
                addProperty(p_textResult);
                return true;
            }
        };
        //   this.addProperty(p_genGraphs);

        Property<PButton> p_shapiro = new Property<PButton>("User-Study.Shapiro-Wilk", new PButton()) {
            @Override
            public boolean updating(PButton newvalue) {

                String result = generateShapiroWilk();

                removeProperty("User-Study. ");

                p_textResult = new Property<PText>("User-Study. ", new PText(result));
                p_textResult.setReadOnly(true);
                addProperty(p_textResult);

                return true;
            }
        };
        // this.addProperty(p_shapiro);

        Property<PButton> p_ttest = new Property<PButton>("User-Study.Paired T-Test", new PButton()) {
            @Override
            public boolean updating(PButton newvalue) {
                String result = generateTTest();
                removeProperty("User-Study. ");
                p_textResult = new Property<PText>("User-Study. ", new PText(result));
                p_textResult.setReadOnly(true);
                addProperty(p_textResult);
                return true;
            }
        };
        //  this.addProperty(p_ttest);

        p_textResult = new Property<PText>("User-Study. ", new PText("")) {
            @Override
            public boolean updating(PText newvalue) {
                return true;
            }
        };
        p_textResult.setReadOnly(true);
        this.addProperty(p_textResult);

    }

    public String downloadStudyResultsFile() {
        //download the results file into the studyResults directory

        String textResult = "";
        if (!hostURL.isEmpty()) {

            String qltyFileStr = hostURL + "data/" + QLT_STUDY_RESULT_FILE;
            String quantControlFileStr = hostURL + "data/" + QUANT_CONTROLCONDITION_RESULT_FILE;
            String quantTestFileStr = hostURL + "data/" + QUANT_TESTCONDITION_RESULT_FILE;

            try {
                org.apache.commons.io.FileUtils.copyURLToFile(new URL(qltyFileStr), new File("studyResults/" + QLT_STUDY_RESULT_FILE));
                org.apache.commons.io.FileUtils.copyURLToFile(new URL(quantControlFileStr), new File("studyResults/" + QUANT_CONTROLCONDITION_RESULT_FILE));
                org.apache.commons.io.FileUtils.copyURLToFile(new URL(quantTestFileStr), new File("studyResults/" + QUANT_TESTCONDITION_RESULT_FILE));
            } catch (Exception ex) {
                textResult = "an error occured when trying to download the file";
                ex.printStackTrace();
            }
            textResult = "Result files downloaded to the directory  studyResults/";
        } else {
            textResult = "unable to download the files";
        }

        return textResult;
    }

    public String generateResultGraphs() {
        String textResult = "";

        try {
            re.eval("mydataCon = read.csv(\"studyResults/" + QUANT_CONTROLCONDITION_RESULT_FILE + "\")");
            re.eval("mydataTest = read.csv(\"studyResults/" + QUANT_TESTCONDITION_RESULT_FILE + "\")");
            re.eval("mydataAll = data.frame(mydataCon, mydataTest)");
            re.eval("mydataAll = mydataAll[,order(names(mydataAll))]");
            //re.eval("boxplot(mydataAll)");
            re.eval("png(filename=\"" + "studyResults/quantitativeResultsImage.png" + "\")");
            re.eval("boxplot(mydataAll)");
            re.eval("dev.off()");
            //  re.end();
        } catch (Exception ex) {
            textResult = "an error occurred when generating the graphs";
            ex.printStackTrace();
        }
        textResult = "Graphs have been generated in the directory studyResults";
        return textResult;

    }

    public String generateShapiroWilk() {
        System.out.println("Began Shapiro Wilk");
        String textResult = "";
        int numberofColumns = 0;

        try {
            File controlResultFile = new File("studyResults/" + QUANT_CONTROLCONDITION_RESULT_FILE);

            BufferedReader br = new BufferedReader(new FileReader(controlResultFile));
            String line = "";
            while ((line = br.readLine()) != null) {
                String split[] = line.split(",");
                numberofColumns = split.length;
                break;
            }
            br.close();

            if ((numberofColumns == 0)) {
                textResult = "Empty result file";
            } else {
                re.eval("mydataCon = read.csv(\"studyResults/" + QUANT_CONTROLCONDITION_RESULT_FILE + "\")");
                re.eval("mydataTest = read.csv(\"studyResults/" + QUANT_TESTCONDITION_RESULT_FILE + "\")");

                REXP rexpCon, rexpTest;
                rexpCon = re.eval("names(mydataCon)");
                rexpTest = re.eval("names(mydataTest)");
                String controlColumnNames[] = rexpCon.asStringArray();
                String testColumnNames[] = rexpTest.asStringArray();

                String strArrCon[];
                String strArrTest[];
                for (int i = 0; i < controlColumnNames.length; i++) {
                    re.eval("" + controlColumnNames[i] + "= c(mydataCon[," + (i + 1) + "])");
                    re.eval("" + testColumnNames[i] + "= c(mydataTest[," + (i + 1) + "])");
                    re.eval("results<-capture.output(shapiro.test(" + controlColumnNames[i] + "))");
                    strArrCon = re.eval("results").asStringArray();

                    re.eval("results<-capture.output(shapiro.test(" + testColumnNames[i] + "))");
                    strArrTest = re.eval("results").asStringArray();

                    //  System.out.println("------");
                    for (int j = 0; j < strArrCon.length; j++) {
                        textResult += strArrCon[j] + "\n";
                    }
                    for (int j = 0; j < strArrTest.length; j++) {
                        textResult += strArrTest[j] + "\n";
                    }
                }
            }
            //System.out.println("*****___***");
            //  re.end();
            System.out.println("------------shapiro-wilk generated successfully-----------");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return textResult;
    }

    public String generateTTest() {
        System.out.println("Began T-test");
        String textResult = "";
        int numberofColumns = 0;
        try {
            File controlResultFile = new File("studyResults/" + QUANT_CONTROLCONDITION_RESULT_FILE);
            BufferedReader br = new BufferedReader(new FileReader(controlResultFile));
            String line = "";
            while ((line = br.readLine()) != null) {
                String split[] = line.split(",");
                numberofColumns = split.length;
                break;
            }
            br.close();
            if ((numberofColumns == 0)) {
                textResult = "Empty result file";
            } else {
                re.eval("mydataCon = read.csv(\"studyResults/" + QUANT_CONTROLCONDITION_RESULT_FILE + "\")");
                re.eval("mydataTest = read.csv(\"studyResults/" + QUANT_TESTCONDITION_RESULT_FILE + "\")");

                REXP rexpCon, rexpTest;
                rexpCon = re.eval("names(mydataCon)");
                rexpTest = re.eval("names(mydataTest)");
                String controlColumnNames[] = rexpCon.asStringArray();
                String testColumnNames[] = rexpTest.asStringArray();

                String strArr[];
                //  String strArrTest[];
                for (int i = 0; i < controlColumnNames.length; i++) {
                    re.eval("" + controlColumnNames[i] + "= c(mydataCon[," + (i + 1) + "])");
                    re.eval("" + testColumnNames[i] + "= c(mydataTest[," + (i + 1) + "])");
                    re.eval("results<-capture.output(t.test(" + controlColumnNames[i] + "," + testColumnNames[i] + "))");
                    strArr = re.eval("results").asStringArray();

                    for (int j = 0; j < strArr.length; j++) {
                        textResult += strArr[j] + "\n";
                    }

                }
            }
            //re.end();
            System.out.println("------------T-test---generated successfully------------");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return textResult;
    }
}
