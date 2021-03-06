/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graphEvaluation;

/**
 *
 * @author Mershack
 */
public class GraphTask {
   private GraphTaskEnum task;
   private String question;
   private String correctAns;
   private String givenAns;
   private String experimentCondition;
   
   private final String instruction = "------- Other Info ---------"
           + "\n1. When you select a node or mouse-over a node  -- all its connections gets highlighted.\n\n"
           + "2. You can zoom in and out of the"
           + " graph by holding the right mouse button down on an empty space and dragging in or out.\n\n"
           + "3. You can pan the visualization by holding the left mouse button on an empty space and dragging.\n\n"
           + "4. To change answers: first uncheck your old answer before checking your new answer.\n\n"
           + "5. The Next button becomes active only when an answer is selected";
   
   
   private final String qnNeigh = "Are the two highlighted nodes connected?";
   private final String qnPath_three_nodes = "Is there a direct path between the three highlighted nodes?";
   private final String qnPath_two_nodes = "Is there a direct path between the two highlighted nodes?";
   private final String qnSize_of_adjacent ="How many nodes are adjacent to the highlighted node?";
   private final String qnMaxSizeOfAdjacent = "What is the size of the maximum size of nodes forthe adjacent nodes?";
   public GraphTask(){
       
   }
   public GraphTask(GraphTaskEnum task, String correctAns, String experimentCondition){
       this.task = task;
       this.correctAns = correctAns;
       this.experimentCondition = experimentCondition;
       question="";
   }

    public GraphTaskEnum getTask() {
        return task;
    }

    public void setTask(GraphTaskEnum task) {
        this.task = task;
    }

    public String getQuestion() {
        
        switch(task){
            case NEIGHBOR:
                question = qnNeigh;
                break;
            case PATH_THREE_NODES:
                question = qnPath_three_nodes;
                break;
            case PATH_TWO_NODES:
                question = qnPath_two_nodes;
                break;
            case SIZE_OF_ADJACENT_NODES:
                question = qnSize_of_adjacent;
            case MAX_SIZE_OF_ADJACENT_NODES:
                question = qnMaxSizeOfAdjacent;
            default:
                //TODO:
        }
        
        
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAns() {
        return correctAns;
    }

    public void setCorrectAns(String correctAns) {
        this.correctAns = correctAns;
    }

    public String getGivenAns() {
        return givenAns;
    }

    public void setGivenAns(String givenAns) {
        this.givenAns = givenAns;
    }
   
    public boolean isAnswerCorrect(){
        //check if the given answer is correct.
        return (correctAns.trim().equalsIgnoreCase(givenAns));
    }
    
     public String getInstruction(){
        return instruction;
    }

    public String getExperimentCondition() {
        return experimentCondition;
    }

    public void setExperimentCondition(String experimentCondition) {
        this.experimentCondition = experimentCondition;
    }
    
     
}


