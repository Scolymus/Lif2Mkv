package models;

/*
 * All files for tree were obtained from:
 *  from http://www.hameister.org/JavaSwingTreeTable.html
 *  Jörn Harmesteir
 *  
 *  Little changes were done to adopt the source to this project
 * 
 */

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
 
public class MyTreeTableSelectionModel extends DefaultTreeSelectionModel {
     
    public MyTreeTableSelectionModel() {
        super();
 
        getListSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
            	
            }
        });
        
    }
     
    ListSelectionModel getListSelectionModel() {
        return listSelectionModel;
    }
}