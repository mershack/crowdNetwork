package graphEvaluation;

import perspectives.base.Viewer;
import perspectives.base.ViewerFactory;
import perspectives.graph.GraphData;

public class GraphUserStudyViewerFactory extends ViewerFactory {
	
	

		public RequiredData requiredData() {
			
			RequiredData rd = new RequiredData("GraphData","1");
			return rd;
		}

		@Override
		public String creatorType() {
			// TODO Auto-generated method stub
			return "GraphUserStudy";
		}

		@Override
		public Viewer create(String name) {
			if (this.isAllDataPresent())
				return new GraphUserStudyViewer(name, (GraphData)this.getData().get(0));
			return null;
		}
}
