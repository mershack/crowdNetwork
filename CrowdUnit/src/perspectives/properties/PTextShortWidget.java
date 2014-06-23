/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package perspectives.properties;

import javax.swing.Box;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import perspectives.base.Property;
import perspectives.base.PropertyWidget;

/**
 *
 * @author Mershack
 */
public class PTextShortWidget extends PTextWidget {

    public PTextShortWidget(Property p) {
        super(p);
    }

    @Override
    public void widgetLayout() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.add(new JLabel(this.p.getDisplayName()));

        final PropertyWidget th = this;

        control = new JTextArea();
        control.setLineWrap(true);
        //control.setEditable(false);
        control.setText(((PText) this.p.getValue()).stringValue());
        control.setMaximumSize(new Dimension(180, 200));
        control.setPreferredSize(new Dimension(180, 200));

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
        readOnlyControl.setText(((PText) this.p.getValue()).stringValue());
        readOnlyControl.setMaximumSize(new Dimension(180, 200));
        readOnlyControl.setPreferredSize(new Dimension(180, 200));
        readOnlyControl.setEnabled(false);
        //readOnlyControl.setMaximumSize(new Dimension(200,20));

        readOnlyControlScrollPane = new JScrollPane(readOnlyControl);
        readOnlyControlScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        readOnlyPanel.setLayout(new BorderLayout());
        readOnlyPanel.add(readOnlyControlScrollPane);

        readOnlyPanel.setPreferredSize(new Dimension(180, 200));
        this.add(Box.createRigidArea(new Dimension(5, 1)));
        this.add(controlPanel);
        this.add(Box.createHorizontalGlue());


        propertyReadonlyUpdated(p.getReadOnly());
    }
}
