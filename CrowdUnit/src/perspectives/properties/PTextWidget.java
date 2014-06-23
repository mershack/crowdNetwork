package perspectives.properties;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import perspectives.base.Property;
import perspectives.base.PropertyType;
import perspectives.base.PropertyWidget;

public class PTextWidget extends PropertyWidget{
	public PTextWidget(Property p) {
		super(p);
		// TODO Auto-generated constructor stub
	}


	JTextArea control = null;
	//JLabel readOnlyControl = null;
        JTextArea readOnlyControl = null;
        
        JScrollPane controlScrollPane = null;
        JScrollPane readOnlyControlScrollPane = null;
        
        JPanel controlPanel = new JPanel();
        JPanel readOnlyPanel = new JPanel();
	
	public void widgetLayout()
	{			
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(new JLabel(this.p.getDisplayName()));

		final PropertyWidget th = this;
		
		control = new JTextArea();
		control.setLineWrap(true);
		//control.setEditable(false);
		control.setText(((PText)this.p.getValue()).stringValue());
		control.setMaximumSize(new Dimension(180,500));
		control.setPreferredSize(new Dimension(180,500));
		
                controlScrollPane = new JScrollPane(control); 
                controlScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                
                
              controlPanel.add(controlScrollPane);
//                controlPanel.setBounds(10,10, 20, 50);
                
                 controlPanel.setLayout(new BorderLayout());
                controlPanel.add(controlScrollPane);               
                controlPanel.setPreferredSize(new Dimension(180, 200));
                
                
		//readOnlyControl = new JLabel();                
                readOnlyControl = new JTextArea(); 
                readOnlyControl.setLineWrap(true);
		readOnlyControl.setText(((PText)this.p.getValue()).stringValue());
                readOnlyControl.setMaximumSize(new Dimension(180,500));
                readOnlyControl.setPreferredSize(new Dimension(180,500));
		readOnlyControl.setEnabled(false);
                //readOnlyControl.setMaximumSize(new Dimension(200,20));
		
		readOnlyControlScrollPane = new JScrollPane(readOnlyControl);
                readOnlyControlScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                
                readOnlyPanel.setLayout(new BorderLayout());
                readOnlyPanel.add(readOnlyControlScrollPane);
               
                readOnlyPanel.setPreferredSize(new Dimension(180, 200));
		this.add(Box.createRigidArea(new Dimension(5,1)));
		this.add(controlPanel);	
		this.add(Box.createHorizontalGlue());
	
		
		propertyReadonlyUpdated(p.getReadOnly());	
	}		


	@Override
	public <T extends PropertyType> void propertyValueUpdated(T newvalue) {
		control.setText(((PText)newvalue).stringValue());		
	}

	@Override
	public void propertyReadonlyUpdated(boolean r) {
		if (control != null)
		{
			if (r)
			{
				//this.remove(control);					
                            //this.add(readOnlyControl,2);
                                this.remove(controlPanel);
                                this.add(readOnlyPanel,2);
				
			}
			else
			{
				//this.remove(readOnlyControl);					
				//this.add(control,2);
                                  this.remove(readOnlyPanel);
                                  this.add(controlPanel, 2);
                            
			}
		}		
	}

	@Override
	public void propertyVisibleUpdated(boolean newvalue) {
		// TODO Auto-generated method stub
		
	}
}
