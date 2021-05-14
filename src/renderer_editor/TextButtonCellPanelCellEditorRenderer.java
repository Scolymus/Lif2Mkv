package renderer_editor;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import models.MyDataNode;

public class TextButtonCellPanelCellEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

	private static final long serialVersionUID = 1L;
	private TextButtonCellPanel renderer = new TextButtonCellPanel(true);
	private TextButtonCellPanel editor = new TextButtonCellPanel(false);

	public TextButtonCellPanelCellEditorRenderer(){
    	CellEditorListener lis = new CellEditorListener(){
  	      @Override
  	      public void editingCanceled(ChangeEvent e0) {
  	        System.out.println(e0);
  	        
  	      }
  	      //trick to change same path if lif out path was changed
  	      @Override
  	      public void editingStopped(ChangeEvent e1) {
  	    	  if (!editor.getNewPath().equals(editor.getOldPath())){
  	    		 boolean father = false;
  	    		 for (int i = 0; i<((MyDataNode) editor.getNodeEditing()).getChildrenCount(); i++){
  	    			TextButtonCell temp = ((MyDataNode) editor.getNodeEditing()).getChildren().get(i).getPathOut();
  	    			temp.Path = editor.getNewPath();
  	    			((MyDataNode) editor.getNodeEditing()).getChildren().get(i).setPathOut(temp);
  	    			father = true;
  	    		 }

  	    	  }
  	    	  //editor.getTable().setRowSelectionInterval(editor.getRowEditing(),editor.getRowEditing());
  	    	  editor.setRowEditing(-1);
  	    	  editor.getTable().repaint();
  	      }
    	};
    	addCellEditorListener(lis);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
        	if (isSelected){
        		renderer.setBackground(table.getSelectionBackground());        	
        	}else{        	
        		renderer.setBackground(table.getBackground());      
        	
        	}
			renderer.setComp((TextButtonCell) value);

		return renderer;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, 
			boolean isSelected, int row, int column) {
		//I need this because if I change the path of a lif project, the children must also change to the same
		if (editor.getRowEditing() == -1){
			editor.setRowEditing(row);
			editor.setOldPath(((TextButtonCell) value).Path);
			editor.setNodeEditing((Object) table.getModel().getValueAt(row, 99));
			editor.setTable(table);
		}

		editor.setComp((TextButtonCell) value);
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

class TextButtonCellPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    //private JPanel panel;

    private JTextField Pathfield = new JTextField();
    private JButton Button = new JButton("Search...");
    private JLabel Label = new JLabel();
    private int row_editing = -1;
    private String old_path = "";
    private Object node = null;
    private JTable table = null;
    
   public TextButtonCellPanel(boolean Render) {
       //panel = new JPanel();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        //Pathfield.setMaximumSize(new Dimension(100,20));
        //add(Box.createHorizontalStrut(0));
        if (!Render) {
            add(Pathfield);
        	Button.setMaximumSize(new Dimension(50,20));
        	Button.addActionListener(this);
        	add(Button);        	
        }else{
            add(Label);
        }       
    }

    public void setComp(TextButtonCell Comp) {
        Pathfield.setText(Comp.Path);
        Label.setText(Comp.Path);
    }

    public TextButtonCell getComp() {
        return new TextButtonCell(Pathfield.getText());
    }
    
    public int getRowEditing() {
        return row_editing;
    }
    
    public void setRowEditing(int value) {
        row_editing = value;
    }
    
    public String getOldPath() {
        return old_path;
    }
    
    public String getNewPath() {
        return Pathfield.getText();
    }
    
    public void setOldPath(String value) {
    	old_path = value;
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
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
        	JFileChooser j = new JFileChooser();
        	j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        	File temp = new File(Pathfield.getText());
        	if (temp.exists()){
        		if (temp.isDirectory()){
        			j.setCurrentDirectory(temp);
        		}
        	}
        	
        	Integer opt = j.showSaveDialog(this);
        	if(opt == JFileChooser.APPROVE_OPTION) {
        	    File Folder = j.getSelectedFile();
        	    Pathfield.setText(Folder.getAbsolutePath());
        	    Label.setText(Folder.getAbsolutePath());
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
        }
    }
}