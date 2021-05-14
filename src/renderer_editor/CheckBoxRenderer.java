package renderer_editor;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import models.MyDataModel;
import models.MyDataNode;
import models.MyTreeTableModelAdapter;

public class CheckBoxRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

	private static final long serialVersionUID = 1L;
	private CheckBoxCellPanel renderer = new CheckBoxCellPanel();
	private CheckBoxCellPanel editor = new CheckBoxCellPanel();

	public CheckBoxRenderer(){
    	CellEditorListener lis = new CellEditorListener(){
  	      @Override
  	      public void editingCanceled(ChangeEvent e0) {
  	        System.out.println(e0);
  	        
  	      }
  	      //trick to change same path if lif out path was changed
  	      @Override
  	      public void editingStopped(ChangeEvent e1) {
  	    	  if (editor.getState()==0 || editor.getState()==2){
  	    		 
  	    		 //is father 
  	    		 for (int i = 0; i<((MyDataNode) editor.getNodeEditing()).getChildrenCount(); i++){
  	    			((MyDataNode) editor.getNodeEditing()).getChildren().get(i).setExtract(editor.getState());
  	    		 }
  	    		 
  	    		 //is child
  	    		 if (((MyDataNode) editor.getNodeEditing()).getChildrenCount() == 0){
   	    			MyDataNode child = (MyDataNode) editor.getNodeEditing();
  	    			MyDataNode parent = ((MyDataModel) ((MyTreeTableModelAdapter) editor.getTable().getModel()).getTreeTableModel()).getParent(child);
  	    					//getParent(child);
  	    			int total = 0;
  	    			//WHATCHOUT! getParent returns first equal. If there is more than one equal and you are not modifying the first one => bug!!!
  	    			for (int i = 0; i<parent.getChildrenCount(); i++){
  	  	    			total += parent.getChildren().get(i).getExtract();  	  	    				
  	  	    		 }
  	    			if (total != 0 && total != parent.getChildrenCount()*2){
  	    				parent.setExtract(1);
  	    			}else if (total == 0){
  	    				parent.setExtract(0);
  	    			}else if (total == parent.getChildrenCount()*2){
  	    				parent.setExtract(2);
  	    			}
  	    		 }

  	    		 
  	    	  }
  	    	  int temp = editor.getRowEditing();  	    	  
  	    	  editor.setRowEditing(-1);
  	    	  editor.getTable().setRowSelectionInterval(temp, temp);
  	    	  editor.getTable().repaint();
  	      }
    	};
    	addCellEditorListener(lis);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
			
        	if (isSelected){
        		renderer.checkbox.setForeground(table.getSelectionForeground());        	
        		renderer.checkbox.setBackground(table.getSelectionBackground());  
        		renderer.setForeground(table.getSelectionForeground());        	
        		renderer.setBackground(table.getSelectionBackground());        	
        	}else{        	
        		renderer.setForeground(table.getForeground());  
        		renderer.setBackground(table.getBackground());      
        		renderer.checkbox.setBackground(table.getBackground());    
        		renderer.checkbox.setForeground(table.getForeground());  

        	}
			renderer.setComp((int) value);

		return renderer;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, 
			boolean isSelected, int row, int column) {
		if (editor.getRowEditing() == -1){
			editor.setRowEditing(row);
			editor.setNodeEditing((Object) table.getModel().getValueAt(row, 99));
			editor.setTable(table);
		}		
		
		int i = (Integer) value;
		if (i==1){ 
			editor.setComp(2);
		}else{
			editor.setComp((int) value);
		}				
		
		table.setRowSelectionInterval(row,row);
		return editor;
	}

	@Override
	public Object getCellEditorValue() {
		return editor.getComp();
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return false;
	}
}

class CheckBoxCellPanel extends JPanel {

    private static final long serialVersionUID = 24;
    //private JPanel panel;

    public TCheckBox checkbox;
    private int row_editing = -1;
    private Object node = null;
    private JTable table = null;
    
   public CheckBoxCellPanel() {
        //setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	   	setLayout(new GridLayout(1,1));
        //Pathfield.setMaximumSize(new Dimension(100,20));
        //add(Box.createHorizontalStrut(0));
	   	checkbox = new TCheckBox("");
	   	checkbox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
        	    Robot r;
				try {
					r = new Robot();
				    int keyCode = KeyEvent.VK_ENTER; // the A key
				    r.keyPress(keyCode);				    
				} catch (AWTException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
	   		
	   	});
        checkbox.setHorizontalAlignment(JCheckBox.CENTER);
        add(checkbox);
    }

    public void setComp(int Comp) {
    	checkbox.setSelectionState(Comp);
    }

    public CheckBoxCell getComp() {
        return new CheckBoxCell(checkbox.getSelectionState());
    }
    
    public int getState() {
        return checkbox.getSelectionState();
    }
    
    public int getRowEditing() {
        return row_editing;
    }
    
    public void setRowEditing(int value) {
        row_editing = value;
    }
    
    public Object getNodeEditing() {
        return node;
    }
    
    public void setNodeEditing(Object value) {
    	node = value;
    }
    
    public JTable getTable() {
        return table;
    }
    
    public void setTable(JTable value) {
    	table = value;
    }
    
}