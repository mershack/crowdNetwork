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
   private final String instruction = "------- Other Info ---------"
           + "\n1. You can zoom in and out of the"
           + " graph by holding the right mouse button down on an empty space and dragging in or out.\n\n"
           + "2. You can pan the visualization by holding the left mouse button on an empty space and dragging.\n\n"
           + "3. To change answers: first uncheck your old answer before checking your new answer.\n\n"
           + "4. The Next button becomes active only when an answer is selected";
   private final String qnNeigh = "Are the two highlighted nodes connected?";
   private final String qnPath_bool = "Does any of the highlighted nodes directly connect to both of the other two?";
   
   private final String qnPath_digit = "What is the size of the shortest path between the two highlighted nodes";
   public GraphTask(){
       
   }
   public GraphTask(GraphTaskEnum task, String correctAns){
       this.task = task;
       this.correctAns = correctAns;
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
            case PATH_BOOLEAN:
                question = qnPath_bool;
                break;
            case PATH_DIGIT:
                question = qnPath_digit;
                break;
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
}


