/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package perspectives.properties;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import perspectives.base.Property;
import perspectives.base.PropertyType;
import perspectives.base.PropertyWidget;

/**
 *
 * @author Mershack
 */
public class PButtonWidget extends PropertyWidget {
    
    
    public PButtonWidget(Property p) {
		super(p);		
	}

	JButton control = null;
	//FileFilter[] fileFilters;
	
	public void widgetLayout()
	{
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		final PropertyWidget th = this;
		
		control = new JButton(this.p.getDisplayName());//, new ImageIcon(Toolkit.getDefaultToolkit().getImage("images/Save16.gif")));			
		control.setPreferredSize(new Dimension(130,20));
                
                final PButton prop = (PButton)th.getProperty().getValue();
                
		ActionListener listener = new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
                        PButton v = prop.copy();
		    		  //v.path = fc.getSelectedFile().getAbsolutePath();			        
		              th.updateProperty(v);
                      }
		    	
		    };
		control.addActionListener(listener);

		this.add(Box.createHorizontalGlue());
		this.add(control);
		this.add(Box.createHorizontalGlue());
		
		p.setReadOnly(p.getReadOnly());
		p.setVisible(p.getVisible());
	}
        
	@Override
	public <T extends PropertyType> void propertyValueUpdated(T newvalue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyReadonlyUpdated(boolean r) {
		if (control != null)
			control.setEnabled(!r);
		
	}

	@Override
	public void propertyVisibleUpdated(boolean newvalue) {
		// TODO Auto-generated method stub
		
	}
    
    
    
}
