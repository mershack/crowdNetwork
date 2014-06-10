
import graphEvaluation.GraphUserStudyViewerFactory;
import graphEvaluation.GraphUserStudyViewer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import perspectives.base.Environment;
import perspectives.base.Property;
import perspectives.base.PropertyType;
import perspectives.graph.GraphData;
import perspectives.graph.GraphDataFactory;
import perspectives.graph.GraphViewerFactory;
import perspectives.properties.PFileInput;
import perspectives.web.InitServlet;

public class MyInitServlet extends InitServlet {

    private String controlViewerName = "viewer1.txt";
    private final String STUDY_DATA_FILENAME = "study_data_filename.txt";
    private final String DATA_DIR = "data";

    @Override
    public void environmentInit(Environment e) {

        e.registerDataSourceFactory(new GraphDataFactory());
        e.registerViewerFactory(new PerformanceViewerFactory());
        e.registerViewerFactory(new GraphViewerFactory());
        e.registerViewerFactory(new GraphUserStudyViewerFactory());

        GraphData d = new GraphData("graphdata");
        PFileInput f = new PFileInput();
        String dataSourceFileName = "";
        //get the dataSourceFile name                        
        try {
            File fileNameFile = new File(getServletContext().getRealPath(DATA_DIR + File.separator + STUDY_DATA_FILENAME));

            if (fileNameFile.exists()) {//Do the property changes if the file exists
                BufferedReader br = new BufferedReader(new FileReader(fileNameFile));
                String line = "";
                if ((line = br.readLine()) != null) {
                    dataSourceFileName = line;
                }
            }

            f.path = getServletContext().getRealPath(DATA_DIR + File.separator + dataSourceFileName);

            d.graph.fromEdgeList(new File(((PFileInput) f).path));
            e.addDataSource(d, true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
