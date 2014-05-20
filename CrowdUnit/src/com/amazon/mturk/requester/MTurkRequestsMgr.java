/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amazon.mturk.requester;

import java.io.FileInputStream;
import java.util.ArrayList;

/**
 *
 * @author Mershack
 */
public class MTurkRequestsMgr {
    
    
    private final static String AWS_ACCESS_KEY_ID = "AKIAIAK5C47XZSX42ZRQ";
	private final static String SECRET_KEY = "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";
        private final static String QN_FILE_NAME = "graphQuestionForm.xml";
        private String awsAccessKey;
        private String secretKey;
        private String title = "graphTasks17";
        private String description = "You will be shown a graph visualization with two nodes highlighted. You will be asked whether the "
                                                                + "highlighted nodes are connected or whether there is a path between them"; // description
        private String questionFileName;
        private String question;
        private String lifetimeInSeconds = "100000"; //expiration of HIT: 1 day 3 hours        
        private String keywords = "graph visualization, graph tasks, visualization, interactive graphs"; //keywords
        private String reward = "0.00"; //reward : $0.01
         private int maxAssignments = 5;
        private int assignmentDurationInSeconds = 900; //deadline : 15mins
        private int autoApprovalDelayInSeconds = 3600; //autoapproval : 1 hour
       
        
        public MTurkRequestsMgr(){
            awsAccessKey = AWS_ACCESS_KEY_ID;
            secretKey = SECRET_KEY;
            questionFileName = QN_FILE_NAME;
            question = getQuestionXML(questionFileName);
        }
        
        public MTurkRequestsMgr(String title, String AwsAccessKey, String secretKey, String reward, int maxAssignments){
            this.title = title;
            this.awsAccessKey = AwsAccessKey;
            this.secretKey = secretKey;
            this.reward = reward;
            this.maxAssignments = maxAssignments;
        }
        
        
        public void createHITRequest(){
            
            try {
			//Construct the request
			AWSMechanicalTurkRequester turk = new AWSMechanicalTurkRequester(
					awsAccessKey, secretKey);
			//call web service to create a new HIT
			RESTResponse res = turk
					.createHIT(
							title, // title
							description, // description
							keywords, //keywords
							reward, //reward : $0.01
							maxAssignments, //maximum number of assignments
							assignmentDurationInSeconds, //deadline : 
							autoApprovalDelayInSeconds, //autoapproval : 1 hour
							question, //question
                                                        lifetimeInSeconds); //expiration of HIT: 1 day 3 hours
                        
                        System.out.println("----------------*Begin*createHit-responseXML------------------------------------------");			
                            //print the xml (just for debugging)
			res.printXMLResponse();
                        
                        System.out.println("-----------------*End of* createHit-responseXML---------------------------------------");

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
        
        
        

	/*public static void main(String[] args) {
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
	}  */

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
		return result;
	}

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuestionFileName() {
        return questionFileName;
    }

    public void setQuestionFileName(String questionFileName) {
        this.questionFileName = questionFileName;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getLifetimeInSeconds() {
        return lifetimeInSeconds;
    }

    public void setLifetimeInSeconds(String lifetimeInSeconds) {
        this.lifetimeInSeconds = lifetimeInSeconds;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public int getMaxAssignments() {
        return maxAssignments;
    }

    public void setMaxAssignments(int maxAssignments) {
        this.maxAssignments = maxAssignments;
    }

    public int getAssignmentDurationInSeconds() {
        return assignmentDurationInSeconds;
    }

    public void setAssignmentDurationInSeconds(int assignmentDurationInSeconds) {
        this.assignmentDurationInSeconds = assignmentDurationInSeconds;
    }

    public int getAutoApprovalDelayInSeconds() {
        return autoApprovalDelayInSeconds;
    }

    public void setAutoApprovalDelayInSeconds(int autoApprovalDelayInSeconds) {
        this.autoApprovalDelayInSeconds = autoApprovalDelayInSeconds;
    }
        
        
    
}
