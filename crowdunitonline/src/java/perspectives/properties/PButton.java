/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package perspectives.properties;

import perspectives.base.PropertyType;

/**
 *
 * @author Mershack
 */
public class PButton extends PropertyType {
    private String value;
    
    public PButton copy() {
        //PButton pb = new PButton();
        // TODO Auto-generated method stub	
        return new PButton();
    }

    public String typeName() {
        // TODO Auto-generated method stub
        return "PButton";
    }

    @Override
    public String serialize() {
        // TODO Auto-generated method stub
        return "";
        //return null;
    }

    @Override
    public PButton deserialize(String s) {
       // PButton pf = copy();
        return new PButton();
    }

}
