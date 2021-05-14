package models;

/*
 * All files for tree were obtained from:
 *  from http://www.hameister.org/JavaSwingTreeTable.html
 *  Jörn Harmesteir
 *  
 *  Little changes were done to adopt the source to this project
 * 
 */

import javax.swing.JButton;

import renderer_editor.CheckBoxCell;
import renderer_editor.TextButtonCell;

public class MyDataModel extends MyAbstractTreeTableModel {
    // Spalten Name.
    static protected String[] columnNames = { "Name", "Extract",  "Save in", "Progess", "in"};
 
    // Spalten Typen.
    static protected Class<?>[] columnTypes = { MyTreeTableModel.class, Integer.class, Object.class, Integer.class, String.class};
 
    private MyDataNode root_node;
    
    public MyDataModel(MyDataNode rootNode) {
        super(rootNode);
        root = rootNode;
        root_node = rootNode;
    }
    
    /**
     * Whatch out! It has been done for root-parent-child. Either cases you should change this function
     * to a recursive function!!!
     * 
     * @param child
     * @return
     */
    public MyDataNode getParent(Object child) {
    	
        for (int i = 0; i<getChildCount(root_node); i++){
        	MyDataNode root_son = (MyDataNode) getChild(root_node, i);
        	if (child.equals(root_son)){
        		return (root_node);
        	}else{        		
        		for (int j = 0; j<getChildCount(root_son); j++){
        			MyDataNode root_grandson = (MyDataNode) getChild(root_son, j);
        			if (child.equals(root_grandson)){
                		return (root_son);
                	}
                }		
        	}
        	
        }
        
        //Not found!
    	return null;
    }
 
    public MyDataNode getRootNode() {
        return root_node;
    }
    
    public Object getChild(Object parent, int index) {
        return ((MyDataNode) parent).getChildren().get(index);
    }
 
 
    public int getChildCount(Object parent) {
        return ((MyDataNode) parent).getChildren().size();
    }
 
 
    public int getColumnCount() {
        return columnNames.length;
    }
 
 
    public String getColumnName(int column) {
        return columnNames[column];
    }
 
 
    public Class<?> getColumnClass(int column) {
        return columnTypes[column];
    }
 
    public Object getValueAt(Object node, int column) {
        switch (column) {
        case 0:
            return ((MyDataNode) node).getName();
        case 1:
            return ((MyDataNode) node).getExtract();
        case 2:
            return ((MyDataNode) node).getPathOut();
        case 3:
            return ((MyDataNode) node).getProgress();
        case 4:
            return ((MyDataNode) node).getFullPathIn();          
        case 99:
            return ((MyDataNode) node);
        default:
            break;
        }
        return null;
    }
 
    public boolean isCellEditable(Object node, int column) {
        return true; // Important to activate TreeExpandListener
    }
 
    public void setValueAt(Object aValue, Object node, int column) {
    	if (aValue.getClass().equals(TextButtonCell.class)){
    		((MyDataNode) node).setPathOut( (TextButtonCell) aValue);
    	}else if(aValue.getClass().equals(CheckBoxCell.class) ){
    		((MyDataNode) node).setExtract(((CheckBoxCell) aValue).value);
        	/*if (((MyDataNode) node).getExtract() == false){
        		((MyDataNode) node).setExtract(true);
        	}else{
        		((MyDataNode) node).setExtract(false);
        	}*/
    	}
    }
 
}