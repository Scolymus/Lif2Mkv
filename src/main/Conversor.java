package main;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import models.MyAbstractTreeTableModel;
import models.MyDataModel;
import models.MyDataNode;
import models.MyTreeTable;
import models.MyTreeTableModelAdapter;
import renderer_editor.CheckBoxRenderer;
import renderer_editor.ProgressBarRenderer;
import renderer_editor.TextButtonCell;
import renderer_editor.TextButtonCellPanelCellEditorRenderer;

import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.JTable;
import javax.swing.JTextField;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/*######################################################
#          			MKV2AVI SOFTWARE     		       #  
#                                                      #  
#        Author: Scolymus                              #
#        Date: 28/05/2018.                             #
#        License: CC BY-NC-SA 4.0                      #
#                                                      #
#------------------------------------------------------#
#  Version history:                                    #
#                                                      #
#    1.0: 28/05/2018. First version. mp4 and mkv to avi#
#                                                      #
#                                                      #
######################################################*/

public class Conversor {

	private JFrame frame;
	private JTable tblVideos;
    static ArrayList<MyDataNode> rootNodes_c = new ArrayList<MyDataNode>();
	static MyDataNode rootnode_c; 
	static MyAbstractTreeTableModel treeTableModel_c;
    MyTreeTable myTreeTable;	
	JPopupMenu popup_add;
	private JButton btnConvert;
	Thread worker;
	private static String ossep = System.getProperty("file.separator");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
		            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		            //SwingUtilities.updateComponentTreeUI(c);
		           // UIManager.put("ProgressBarUI", MetalTheme.class.MetalProgressBarUI);
		            Icon empty = new TreeIcon();
		            UIManager.put("Tree.closedIcon", empty);
		            UIManager.put("Tree.openIcon", empty);
		            UIManager.put("Tree.leafIcon", empty);
					Conversor window = new Conversor();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Conversor() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 862, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		
		gridBagLayout.columnWidths = new int[]{1, 0};
		gridBagLayout.rowHeights = new int[] {200, 50, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.90, 0.0, Double.MIN_VALUE};

		frame.getContentPane().setLayout(gridBagLayout);
		
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		frame.getContentPane().add(scrollPane, gbc_scrollPane);
		
        rootnode_c = new MyDataNode("R1", 2,new TextButtonCell(""), 0, rootNodes_c, "");
        treeTableModel_c = new MyDataModel(rootnode_c);

        update_tree(true);
        myTreeTable.setShowHorizontalLines(true);
        myTreeTable.setShowVerticalLines(true);
        myTreeTable.setFillsViewportHeight(true);

        //UIManager.put("Tree.leafIcon", icon);
        
        scrollPane.setViewportView(myTreeTable);
		
        popup_add = new JPopupMenu();		
        myTreeTable.setAutoCreateRowSorter(true);
		myTreeTable.setComponentPopupMenu(popup_add);
        //myTreeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        myTreeTable.setCellSelectionEnabled(true);
        myTreeTable.setColumnSelectionAllowed(true);
		
		//No column ordering. Must be before setModel
        myTreeTable.setColumnModel(new DefaultTableColumnModel() {

			private static final long serialVersionUID = 5;

			public void moveColumn(int columnIndex, int newIndex) {
					super.moveColumn(columnIndex, columnIndex);
			}
		});
        
		//Delete files when user press "supr"
        myTreeTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode()==KeyEvent.VK_DELETE){
					if (myTreeTable.getSelectedRows().length >0){
						boolean remove_ok = false;
						for (int i = myTreeTable.getSelectedRows().length-1; i>-1;i--){
							Object temp = myTreeTable.getModel().getValueAt(myTreeTable.getSelectedRows()[i], 99);							
							if (((MyDataNode) temp).getChildrenCount()!=0){							
								rootnode_c.getChildren().remove(temp);
								remove_ok = true;
							}							
							if (remove_ok) update_tree(false);
						}						
						if (treeTableModel_c.getChildCount(treeTableModel_c.getRoot()) == 0) btnConvert.setEnabled(false);
					}
					
				}
			}
		});

		
		//Add files by drag and drop. Can be either on scroll or on table
        myTreeTable.setDropTarget(new DropTarget(){
            /**
			 * 
			 */
			private static final long serialVersionUID = 2;

			@Override
            public synchronized void drop(DropTargetDropEvent dtde) {              
            	drop_files(dtde);
            }
        });
		scrollPane.setDropTarget(new DropTarget(){
            /**
			 * 
			 */
			private static final long serialVersionUID = 3;

			@Override
            public synchronized void drop(DropTargetDropEvent dtde) {
            	drop_files(dtde);
            }
        });
		
		myTreeTable.addMouseListener( new MouseAdapter(){
            public void mousePressed(MouseEvent e){
				if (e.getButton()==MouseEvent.BUTTON3){
					
                    int row = myTreeTable.rowAtPoint( e.getPoint() );
                    int column = myTreeTable.columnAtPoint( e.getPoint() );

                    if (rootnode_c.getChildrenCount()!=0 && row < myTreeTable.getRowCount() 
                    	&& myTreeTable.isEnabled()){                 	
                    	myTreeTable.setRowSelectionInterval(row, row);                    	 
                    	//myTreeTable.repaint();
                    	popup_add.show(e.getComponent(), e.getX(), e.getY());
                    }
                    	//changeSelection(row, column, false, false);

                    
                }
            }
        });
		
		scrollPane.addMouseListener( new MouseAdapter(){
            public void mouseReleased(MouseEvent e){
				if (e.getButton()==MouseEvent.BUTTON3){
					
                    int row = myTreeTable.rowAtPoint( e.getPoint() );
                    int column = myTreeTable.columnAtPoint( e.getPoint() );

                    if (rootnode_c.getChildrenCount()!=0 && row < myTreeTable.getRowCount()
                    		&& myTreeTable.isEnabled()){
                    	myTreeTable.setRowSelectionInterval(row, row);                    
                    	popup_add.show(e.getComponent(), e.getX(), e.getY());
                    	//myTreeTable.repaint();
                    }                    
                }
            }
        });
		
		Action action = new AbstractAction("Remove") {

		    /**
			 * 
			 */
			private static final long serialVersionUID = 4;

			@Override
		    public void actionPerformed(ActionEvent e) {
				boolean remove_ok = false;
				for (int i = myTreeTable.getSelectedRows().length-1; i>-1;i--){
					Object temp = myTreeTable.getModel().getValueAt(myTreeTable.getSelectedRows()[i], 99);							
					if (((MyDataNode) temp).getChildrenCount()!=0){

						rootnode_c.getChildren().remove(temp);
						remove_ok = true;
					}							
					if (remove_ok) update_tree(false);
				}						
				if (treeTableModel_c.getChildCount(treeTableModel_c.getRoot()) == 0) btnConvert.setEnabled(false);
		    }
		};

		popup_add.add(action);						
		
		btnConvert = new JButton("Convert to avi...");
		btnConvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (btnConvert.isEnabled() && btnConvert.getText() == "Convert to avi...") {
	            	worker = new Thread() {
	            		public void run() {
	            			btnConvert.setText("STOP!");
	            			myTreeTable.setEnabled(false);
	            			start_conversion();
	            		}
	            	};
	            	worker.start();		
				}else if (btnConvert.isEnabled() && btnConvert.getText() == "STOP!"){
					worker.interrupt();
					btnConvert.setText("Convert to avi...");
        			myTreeTable.setEnabled(true);
				}
		
			}
		});
		btnConvert.setEnabled(false);
		GridBagConstraints gbc_btnConvert = new GridBagConstraints();

		gbc_btnConvert.fill = GridBagConstraints.BOTH;
		gbc_btnConvert.insets = new Insets(0, 0, 0, 5);
		gbc_btnConvert.gridx = 0;
		gbc_btnConvert.gridy = 1;
		frame.getContentPane().add(btnConvert, gbc_btnConvert);
	}

	
	/**
	 * Convert mp4 videos to avi compatible format for powerpoint.
	 * 
	 * @param file  route to tif pictures including Series name but not ".tif"
	 * @param row	row of this video in the table
	 * @param fps	fps for video
	 * @param quality	value for quality (b:v ffmpeg option). It is in "k" units.
	 * 
	 * @return void
	*/
	public void start_conversion(){
		int j;
		double totalSecs;
		MyDataNode node;
		String command, out, dur, match;
		//String[] hms, matchSplit;
		File video;
		Process p;
		Scanner sc;
		Pattern durPattern, timePattern;		
		
    	//We expand everything
    	((MyTreeTableModelAdapter) myTreeTable.getModel()).expandAllNodes(((MyTreeTableModelAdapter) myTreeTable.getModel()).getTree(),
    			0, ((MyTreeTableModelAdapter) myTreeTable.getModel()).getTree().getRowCount());
    	
    	//Reset all temp folders!
    	//Not done in the main loop because if two files share temp folder, then images are removed before video is done
		for (j=0; j<treeTableModel_c.getChildCount(treeTableModel_c.getRoot());j++ ){
			node = (MyDataNode) treeTableModel_c.getChild(treeTableModel_c.getRoot(), j);	
			if (node.getPathOut().Path.lastIndexOf(".")<0){
				if (!node.getPathOut().Path.endsWith(ossep)) node.getPathOut().Path = node.getPathOut().Path+ossep;
				out = node.getPathOut().Path+node.getFullPathIn().substring(node.getFullPathIn().lastIndexOf(ossep)+1)+".avi";
			}else{
				out = node.getPathOut().Path.substring(0, node.getPathOut().Path.lastIndexOf("."))+".avi";
			}
			video = new File(out);
			if (video.exists()) video.delete();
			
			command = FileSystems.getDefault().getPath("").toAbsolutePath()+ossep+"ffmpeg.exe -i \""+node.getFullPathIn()+"\" -qscale 0  \""+ out +"\"";
			try{
				p = Runtime.getRuntime().exec(command);
				
		        sc = new Scanner(p.getErrorStream());
	
		        // Find duration
		        durPattern = Pattern.compile("(?<=Duration: )[^,]*");
		        dur = sc.findWithinHorizon(durPattern, 0);
		        if (dur == null)
		          throw new RuntimeException("Could not parse duration.");
		        String[] hms = dur.split(":");
		        totalSecs = Integer.parseInt(hms[0]) * 3600
		                         + Integer.parseInt(hms[1]) *   60
		                         + Double.parseDouble(hms[2]);
		        System.out.println("Total duration: " + totalSecs + " seconds.");
	
		        // Find time as long as possible.
		        timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
		        String[] matchSplit;
		        
		        while (null != (match = sc.findWithinHorizon(timePattern, 0))) {
		            matchSplit = match.split(":");
		            if (matchSplit.length<2) continue;
		            if (!matchSplit[0].equals("") && !matchSplit[1].equals("") && !matchSplit[2].equals("")){
			            double progress = Integer.parseInt(matchSplit[0]) * 3600 +
			                Integer.parseInt(matchSplit[1]) * 60 +
			                Double.parseDouble(matchSplit[2]) / totalSecs;
			            	node.setProgress((int) (progress*100+100));
							if (( (int) ( (progress*100) % 2) ) == 0){							
								myTreeTable.repaint();
							}
		            }
		        }
	        	node.setProgress((int) 610);
				myTreeTable.repaint();	
				p.waitFor();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		myTreeTable.repaint();			

		boolean activate = true;
		for (j=0; j<treeTableModel_c.getChildCount(treeTableModel_c.getRoot());j++ ){
			MyDataNode node1 = (MyDataNode) treeTableModel_c.getChild(treeTableModel_c.getRoot(), j);
			
			if (node1.getProgress()<100){
				activate = false;
			}
		}
		if (activate){
			btnConvert.setText("Convert to avi...");
			myTreeTable.setEnabled(true);
		}

	}
	
	public static class Editor_name extends DefaultCellEditor {
		  /**
		 * 
		 */
		private static final long serialVersionUID = 7;
		public Editor_name(JTextField ProgressBar) {
		   super(ProgressBar);
		  }
		  @Override
		  public boolean isCellEditable(EventObject anEvent) {
		    return false;
		  }
	}
	
	//*****************  DRAG AND DROP  *****************
	
	/**
	 * Action performed to add files by drag and drop for jtable add_table.
	 * 
	 * @param dtde   The DropTargetDropEvent of the component
	 * 
	 * @return void
	*/
	
	public void drop_files(DropTargetDropEvent dtde){
    // handle drop outside current table (e.g. add row)
	dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
    Transferable t = dtde.getTransferable();
		try {
			//Obtain all files dragged
			List<File> fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

			//For each: add it
		    for (int i = 0; i<fileList.size(); i++){	
		    	File f = (File)fileList.get(i);
		    	//if it is a directory, then we look for subdirectories
		    	if (f.isDirectory()){
		    		List<File> subfiles = new ArrayList<File>();
		    		searchForFiles(f,subfiles);
		    		
		    		//Only files are added. Folders not.
		    		for (int j = 0; j<subfiles.size();j++){
		    			if (subfiles.get(j).getAbsolutePath().endsWith(".mp4") || subfiles.get(j).getAbsolutePath().endsWith(".mkv")) {
		 			  		try {
		 				        String route = subfiles.get(j).getAbsolutePath().substring(0, subfiles.get(j).getAbsolutePath().lastIndexOf("."));
		 				        String seriesname = subfiles.get(j).getAbsolutePath().substring(subfiles.get(j).getAbsolutePath().lastIndexOf("\\")+1);
		 				        
		 				        rootNodes_c.add(new MyDataNode(seriesname, 2,new TextButtonCell(route+".avi"), 0, null, subfiles.get(j).getAbsolutePath()));
				    			btnConvert.setEnabled(true);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}		 			  		
		    			}
		    		}
		    	}else{
	    			if (f.getAbsolutePath().endsWith(".mp4") || f.getAbsolutePath().endsWith(".mkv")) {
		 			  	try {
		 				    String route = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf("."));
		 				    String seriesname = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf("\\")+1);
		 				        
		 				    rootNodes_c.add(new MyDataNode(seriesname, 2,new TextButtonCell(route+".avi"), 0, null, f.getAbsolutePath()));
 							update_tree(false);

				    		btnConvert.setEnabled(true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
		    	}
		    }			
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	

	
	public void update_tree(boolean create){
			if (create){
		        myTreeTable = new MyTreeTable(treeTableModel_c);
			}else{
				myTreeTable.Change_TreeTableModel(treeTableModel_c);	
			}
		
			myTreeTable.getColumnModel().getColumn(1).setMaxWidth(50);
			myTreeTable.getColumnModel().getColumn(1).setMinWidth(50);
			myTreeTable.getColumnModel().getColumn(1).setCellRenderer(new CheckBoxRenderer());
			myTreeTable.getColumnModel().getColumn(1).setCellEditor(new CheckBoxRenderer());
			myTreeTable.getColumnModel().getColumn(2).setCellEditor(new TextButtonCellPanelCellEditorRenderer());
			myTreeTable.getColumnModel().getColumn(2).setCellRenderer(new TextButtonCellPanelCellEditorRenderer());
			myTreeTable.getColumnModel().getColumn(3).setCellRenderer(new ProgressBarRenderer());
			myTreeTable.getColumnModel().getColumn(3).setCellEditor(new Editor_name(new JTextField()));
			myTreeTable.getColumnModel().getColumn(3).setPreferredWidth(150);
			myTreeTable.getColumnModel().getColumn(3).setMaxWidth(150);
			TableColumn pathin = myTreeTable.getColumnModel().getColumn(4);
			myTreeTable.getColumnModel().removeColumn(pathin);


	}
	
	/**
	 * Looks for all files that exist in a folder and subfolders.
	 * 
	 * @param root   Folder to look at.
	 * @param datOnly   List of files and folders just in the first level of root.
	 * @return 
	 * 
	 * @return void, although datOnly is modified with the list of files & folders
	*/
	public static void searchForFiles(File root, List<File> datOnly) {
	    if(root == null || datOnly == null) return; //just for safety   
	    if(root.isDirectory()) {
	        for(File file : root.listFiles()) {
	            searchForFiles(file, datOnly);
	        }
	    } else if(root.isFile()) {
	        datOnly.add(root);
	    }
	}
	
	//*****************  DRAG AND DROP  *****************
	
}
