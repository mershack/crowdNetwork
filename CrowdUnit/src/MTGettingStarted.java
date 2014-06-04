
import com.amazon.mturk.requester.*;

import java.io.FileInputStream;
import java.util.ArrayList;

import com.amazon.mturk.requester.RESTResponse;

public class MTGettingStarted {

	private final static String AWS_ACCESS_KEY_ID = "AKIAIAK5C47XZSX42ZRQ";
	private final static String SECRET_KEY = "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";

	public static void main(String[] args) {
		try {
			//Construct the request
			AWSMechanicalTurkRequester turk = new AWSMechanicalTurkRequester(
					AWS_ACCESS_KEY_ID, SECRET_KEY);

			//create an HIT
			//call web service to create a new HIT
			RESTResponse res = turk
					.createHIT(
							"graphTasks6", // title
							"You will be shown a graph visualization with two nodes highlighted. You will be asked whether the "
                                                                + "highlighted nodes are connected or whether there is a path between them", // description
							"graph visualization, graph tasks, visualization, interactive graphs", //keywords
							"0.00", //reward : $0.01
							1, //maximum number of assignments
                                                        //6000, //deadline : 1 hour 40 minutes
							8000, //deadline : 1 hour 40 minutes
							3600, //autoapproval : 1 hour
							getQuestionXML("graphQuestionForm.xml"), //question
                                                        //getQuestionXML("question-eq.xml"), //question
							//"Hello Hello Hello",
                                                        //"100000"); //expiration of HIT: 1 day 3 hours
                                                        "200000"); //expiration of HIT: 1 day 3 hours
                        
			//print the xml (just for debugging)
			res.printXMLResponse();

                        //System.out.println("Hey2");
			//run xpath queries in DOM, so that you can get whatever you like from the response
			//read the REST XML response
			Object ohitid = res.getXPathValue("//HITId");
			String message = "";

			//return the response
			if (ohitid != null && ohitid != "") {
				message = "Hit Created : HITID = " + ohitid.toString();
				
				//test getHIT
				RESTResponse rest2 = turk.getHIT(ohitid.toString());
				rest2.printXMLResponse();
			} else {
				message = "Error :"
						+ res.getXPathValue("//Errors/Error/Message")
						+ "\n Try Again..";
			}

			System.out.println(message);

			RESTResponse res0 = turk.getReviewableHITs();
			ArrayList hitids = res0.getXPathValues("//HITId");
			if (hitids.size() <= 0)
				System.out.println("No Reviewable HITs");
			for (int i = 0; i < hitids.size(); i++) {
				String hitid = hitids.get(i).toString();
				System.out.println("HITID = " + hitid);
				RESTResponse res1 = turk.getAssignmentsForHIT(hitid);

				ArrayList assids = res1.getXPathValues("//AssignmentId");
				if (assids.size() <= 0)
					System.out.println("|-----No Assignments for this HIT");
				for (int j = 0; j < assids.size(); j++) {
					String assid = assids.get(j).toString();
					System.out.println("|-----AID = " + assid);
				}
			}

		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	private static String getQuestionXML(String filepath) {
		String result = "";
		try {
			FileInputStream file = new FileInputStream(filepath);
			byte[] b = new byte[file.available()];
			file.read(b);
			file.close();
			result = new String(b);

		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
                
               // System.out.println("The question String:::: "+result);
                
		return result;
	}
}