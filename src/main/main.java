package main;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.FileSystems;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.IFormatWriter;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.codec.CodecOptions;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import models.MyAbstractTreeTableModel;
import models.MyTreeTableModelAdapter;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.unit.Unit;
import ome.xml.model.primitives.PositiveInteger;
import models.MyDataModel;
import models.MyDataNode;
import models.MyTreeTable;
import renderer_editor.CheckBoxRenderer;
import renderer_editor.ProgressBarRenderer;
import renderer_editor.TextButtonCell;
import renderer_editor.TextButtonCellPanelCellEditorRenderer;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.JCheckBox;

/*######################################################
#          			LIF2AVI SOFTWARE     		       #  
#                                                      #  
#        Author: Scolymus                              #
#        Date: 13/04/2018.                             #
#        License: CC BY-NC-SA 4.0                      #
#                                                      #
#------------------------------------------------------#
#  Version history:                                    #
#                                                      #
#    1.0: 13/04/2018. Drag&Drop, fps, progressbar      #
#    2.0: 20/04/2018. New visual style			       #
#    3.0: 30/04/2018. Tried to use ffmpeg on the fly   #
#    3.1: 30/04/2018. Bug 4GB .lif solved		       #
#    3.2: 18/05/2018. Bug first video solved	       #
#	 3.3: 28/05/2018. Added photo, Z-stack support with#
#					  compresion (8bit+jpeg),		   #
#					  Metadata is included in videos   #
#					  not in Zstacks and images but all#
#					  metadata is saved in a xml file  #
#					  (change mp4 per mkv)			   #
#	 4.0: 09/07/2018. Added support for channels in    #
#					  video + photos	   	           #
#					  Extract via ffmpeg/opencv javacpp#
#					  Z-stack prints with ImageWriter  #
#					  xml "%20" bug solved  		   #
#					  .lif with one file couldn't be   #
#					  added.				  		   #
#                                                      #
######################################################*/

public class main {

	private JFrame frame;
    static ArrayList<MyDataNode> rootNodes = new ArrayList<MyDataNode>();
	static MyDataNode rootnode; 
	static MyAbstractTreeTableModel treeTableModel;
    MyTreeTable myTreeTable;	
	JPopupMenu popup_add;

    private JFormattedTextField txtquality;
    private JCheckBox chckbxVideosToImages;
	JButton btnExtract;
	Thread worker, worker2;
	private JFormattedTextField txtZstack;
	
	ArrayList<String> channels_name = new ArrayList<String>(10);
	
	private static String ossep = System.getProperty("file.separator");
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Change Style for Tree
	                try {
	                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	                    //SwingUtilities.updateComponentTreeUI(c);
	                   // UIManager.put("ProgressBarUI", MetalTheme.class.MetalProgressBarUI);
	                    Icon empty = new TreeIcon();
	                    UIManager.put("Tree.closedIcon", empty);
	                    UIManager.put("Tree.openIcon", empty);
	                    UIManager.put("Tree.leafIcon", empty);
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	                // Create window 
					main window = new main();
				    //System.loadLibrary( Core.NATIVE_LIBRARY_NAME+".dll" );
					//System.out.println(System.getProperty("java.library.path")); 
					//System.load( "C:\\Users\\LASX-User\\.m2\\repository\\org\\bytedeco\\opencv\\4.1.0-1.5.1\\opencv-4.1.0-1.5.1-windows-x86.jar" );
					
					// Add closing listener
					/*
					window.frame.addWindowListener(new WindowAdapter(){
						public void windowClosing(WindowEvent e){
							System.exit(0);//Close app
						}
					});
					 */

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
	public main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 993, 515);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		
		gridBagLayout.columnWidths = new int[]{1, 0};
		gridBagLayout.rowHeights = new int[] {200, 50, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.90, 0.0, Double.MIN_VALUE};

		frame.getContentPane().setLayout(gridBagLayout);
		
		// #################  TABLE SECTION  #################
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		frame.getContentPane().add(scrollPane, gbc_scrollPane);
		
        rootnode = new MyDataNode("R1", 2,new TextButtonCell(""), 0, rootNodes, "");
        treeTableModel = new MyDataModel(rootnode);

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
        
		// Delete files when user press "supr"
        myTreeTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// If user press "supr"
				if (arg0.getKeyCode()==KeyEvent.VK_DELETE){
					// If there are projects
					if (myTreeTable.getSelectedRows().length >0){
						// We do a loop starting from the end
						boolean remove_ok = false;
						for (int i = myTreeTable.getSelectedRows().length-1; i>-1;i--){
							Object temp = myTreeTable.getModel().getValueAt(myTreeTable.getSelectedRows()[i], 99);	
							// If element has videos/images
							if (((MyDataNode) temp).getChildrenCount()!=0){							
								rootnode.getChildren().remove(temp);
								remove_ok = true;
							}							
							if (remove_ok) update_tree(false);
						}						
						if (treeTableModel.getChildCount(treeTableModel.getRoot()) == 0) btnExtract.setEnabled(false);
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

                    if (rootnode.getChildrenCount()!=0 && row < myTreeTable.getRowCount() 
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

                    if (rootnode.getChildrenCount()!=0 && row < myTreeTable.getRowCount()
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
						rootnode.getChildren().remove(temp);
						remove_ok = true;
					}							
					if (remove_ok) update_tree(false);
				}						
				if (treeTableModel.getChildCount(treeTableModel.getRoot()) == 0) btnExtract.setEnabled(false);
		    }
		};

		popup_add.add(action);						

		
		// #################  BUTTONS SECTION  #################
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 0, 10, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		//gbc_panel.rowHeights = new int[] {200, 50, 0};

		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		frame.getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {1, 400};
		gbl_panel.rowHeights = new int[]{40, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JPanel panel_4 = new JPanel();
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_4.insets = new Insets(0, 0, 0, 10);
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 0;
		panel.add(panel_4, gbc_panel_4);
		
		JButton btnChannels = new JButton("Channels...");
		btnChannels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Channels channels_frame = new Channels();
				channels_frame.Load_channels(channels_name);
				channels_frame.addWindowListener(new WindowAdapter(){
			        @Override
					public void windowClosing(WindowEvent e) {
						channels_name = channels_frame.getName_channels();
						channels_frame.dispose();
					 }
				});
				channels_frame.setVisible(true);
			}
		});
		panel_4.add(btnChannels);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panel_4.add(horizontalStrut_1);
		
		JLabel lblZstackQuality = new JLabel("Z-stack quality:");
		panel_4.add(lblZstackQuality);
		
		txtZstack = new JFormattedTextField(NumberFormat.getIntegerInstance());
		txtZstack.setHorizontalAlignment(SwingConstants.CENTER);
		txtZstack.setToolTipText("This number must be between 0 (lowest quality) and 100 (higher quality).");
		txtZstack.setText("90");
		txtZstack.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    //warn();
			  }
			  public void removeUpdate(DocumentEvent e) {
			    //warn();
			  }
			  public void insertUpdate(DocumentEvent e) {
			    //warn();
			  }

			  public void warn() {
				 if (txtZstack.getText().equals("")) txtZstack.setText("0");
			     if (Integer.parseInt(txtZstack.getText())>100) txtZstack.setText("100");       			   
			  }
			});
		panel_4.add(txtZstack);
		txtZstack.setColumns(4);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		panel_4.add(horizontalStrut);
		
		JLabel lblNewLabel = new JLabel("Video quality:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblNewLabel);
		
		txtquality = new JFormattedTextField(NumberFormat.getIntegerInstance());
		txtquality.setToolTipText("This number does not have a limit. 1000 seems to be ok for good quality/compresion");
		txtquality.setText("1000");
		txtquality.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(txtquality);
		txtquality.setColumns(8);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		panel_4.add(horizontalStrut_2);
		
		chckbxVideosToImages = new JCheckBox("Videos to images");
		panel_4.add(chckbxVideosToImages);
		
		btnExtract = new JButton("EXTRACT VIDEOS!");
		btnExtract.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (btnExtract.isEnabled() && btnExtract.getText() == "EXTRACT VIDEOS!") {
	            	worker = new Thread() {
	            		public void run() {
	            			btnExtract.setText("STOP!");
	            			myTreeTable.setEnabled(false);
	            			start_extraction();
	            		}
	            	};
	            	worker.start();		
				}else if (btnExtract.isEnabled() && btnExtract.getText() == "STOP!"){
					worker.interrupt();
        			btnExtract.setText("EXTRACT VIDEOS!");
        			myTreeTable.setEnabled(true);
				}
		
			}
		});
		btnExtract.setEnabled(false);
		GridBagConstraints gbc_btnExtract = new GridBagConstraints();
		gbc_btnExtract.insets = new Insets(0, 20, 0, 0);
		gbc_btnExtract.fill = GridBagConstraints.BOTH;
		gbc_btnExtract.gridx = 1;
		gbc_btnExtract.gridy = 0;
		panel.add(btnExtract, gbc_btnExtract);
		
		//Load default channels names
		Channels channels_frame = new Channels();
		channels_name = channels_frame.getName_channels();
		channels_frame.dispose();

	}

	/**
	 * Extract all the videos present in the table.
	 * 
	 * @return void
	*/
	public void start_extraction(){
		System.load( "C:\\Users\\Lucas\\Downloads\\opencv_java410.dll" );
		//  System.loadLibrary( "opencv_java410"); //Core.NATIVE_LIBRARY_NAME );
		//IO
	    IFormatReader reader = null; 			  		
	    IFormatWriter writer = null;
		InputStream in = null;
	    ImageOutputStream ios = null;
	    javax.imageio.ImageWriter writerIO = null;	    
	    String filetoopen, filetosave;

	    //METADATA
	    //MetadataStore metadata;
        IMetadata metadata, omexml;
	    ServiceFactory factory;
	    OMEXMLService service;
	    Hashtable<String, Object> metaseries;
	    ImageWriteParam param = null;

	    //IMAGE/VIDEO GENERAL
	    String seriename, filename, route, dimension_order;
	    File folder, fOutputFile;    	    
	    int image, num_slides;
	    double fps;
	    byte[] plane;
	    //org.opencv.core.Mat mat;
	    Buffer[] buf;
	    Boolean depth = null;
	    float progress_series = 0.f;
	    int progress_lif = 0;    		
	    MyDataNode node;
	    
	    //IMAGE REDUCTION SIZE
	    byte[] plane8;
	    short[] shorts = null;
	    BufferedImage convertedGrayscale = null;
	    TIFFImageWriterSpi tiffspi = null;
	    
	    //Z-stack
	    int max, min;
	    double range;
	    
		//FFMPEG COMPRESSION
		FFmpegFrameRecorder recorder = null;
		Frame frame = null;
		boolean bypass = false;
		int bypassv = 1000000;
		
        // We check the final quality of the video
        int quality = 0;
        try {
        	quality = Integer.parseInt(txtquality.getText());
        }catch ( NumberFormatException ee){
        	quality = 0;
        }
        if (quality <= 0){
        	quality = 1000;
        }
		
        // and of images stored as a Z-stack
        int zquality = 0;
        try {
        	zquality = Integer.parseInt(txtZstack.getText());
        }catch ( NumberFormatException ee){
        	zquality = 0;
        }
        if (zquality <= 0){
        	zquality = 100;
        }
        
        int counter_slide = 0;
        
	    //Start tif extraction
	    try {
	    	// We expand everything
	    	((MyTreeTableModelAdapter) myTreeTable.getModel()).expandAllNodes(((MyTreeTableModelAdapter) myTreeTable.getModel()).getTree(),
	    			0, ((MyTreeTableModelAdapter) myTreeTable.getModel()).getTree().getRowCount());
	    	
	    	// for each lif in the table
			for (int lif=0; lif<treeTableModel.getChildCount(treeTableModel.getRoot());lif++ ){
				node = (MyDataNode) treeTableModel.getChild(treeTableModel.getRoot(), lif);
				progress_lif = 0;
				
				// We create the metadata xml template
				factory = new ServiceFactory();
				service = factory.getInstance(OMEXMLService.class);
				metadata = service.createOMEXMLMetadata();

				// Now we init the image reader
				reader = new ImageReader(); 
				
				// We set the metadata
				reader.setMetadataStore(metadata);
				
				// And we start reading the lif file
				filetoopen = node.getFullPathIn();
				reader.setId(filetoopen);

				// We go file per file inside that lif
				for (int series = 0; series<node.getChildrenCount();series++){
					if (node.getChildren().get(series).getExtract() != 2){
					  	progress_lif += 100/node.getChildrenCount(); 
						node.getChildren().get(series).setProgress((int) 0);						
						continue;
					}

					// We take the output path and create folder
					filetosave = node.getChildren().get(series).getPathOut().Path;
					if (!filetosave.endsWith(ossep)) filetosave = filetosave + ossep;
					folder = new File(filetosave.substring(0, filetosave.lastIndexOf(ossep)));
					folder.mkdirs();
									      
					// We set the series that we want to read
					reader.setSeries(series);
					
					// Fill metadata for this series
					// 1. Create an xml file for this series. This will be included within the mkv file
					omexml = fill_metadata(service, metadata, series, reader.getSizeZ());
					// 2. Save all the metadata in an external xml file					
					metaseries = reader.getSeriesMetadata();		
					save_metadata(metaseries, filetosave+node.getName()+"_metadata.xml");
											            					
					// Get series name
					seriename = metaseries.get("Image name").toString();
					if (seriename == null) seriename = series+"";

					// Get series fps. If null, set 25 FPS as default!
					fps = Double.valueOf(metaseries.get("Image|ATLCameraSettingDefinition|CycleTime").toString());
					if (fps == Double.NaN) fps = 0.04;
					
		            // Now, construct a buffer to hold one frame of the video, or the image itself
		            plane = new byte[FormatTools.getPlaneSize(reader)];
					
					// Sometimes, mp4 can give some error codification due to fps decimals. We can solve it with this trick!
		            // We multiply the fps by a value (bypassv). Initially is large (1000000). Then, we take only
		            // the integer from this value, and convert later to double. Now, we divide it again by bypassv
		            // and set as false to avoid entering again. This way, the FPS is very similar, but change in
		            // the decimals. Although stupid, it prevents the error I was having.
					if (bypass == true) {						
						fps = fps*bypassv;
						fps = (double)((int) fps);
						fps = fps/bypassv;
						bypass = false;
					} else {	//if false we reset value
						bypassv = 1000000;
					}

					// Now we will extract frame per frame from the series already opened
					// Take the number of frames
					num_slides = reader.getImageCount();
					// Take the name of the lif file, but remove the .lif extension. Replace / just in case...
					filename = reader.getCurrentFile().substring(reader.getCurrentFile().lastIndexOf(ossep)+1,reader.getCurrentFile().length()-4).replace("/", "__");
					// Set to 0 the counter
					counter_slide = 0;
					
					// For every channel
					for (int channel = 0; channel<reader.getSizeC(); channel++){
						
						// We will save it as out_path+lif_filename_without_.lif+channel(if more than 1)+seriesname
						if (reader.getSizeC() == 1){ // There is only one channel
							route = filetosave.substring(0, filetosave.lastIndexOf(ossep)+1)+filename+"_"+seriename;
						}else{						// We have more than one channel
							route = filetosave.substring(0, filetosave.lastIndexOf(ossep)+1)+filename+"_"+channels_name.get(channel)+"_"+seriename;
						}
						
						if (reader.getSizeZ()==1) {	//@@@@@@@@  NO Z-STACK  @@@@@@@@@
							if (reader.getSizeT()!=1){  //@@@@@@@@  VIDEO   @@@@@@@@
								// Take the number of frames
								num_slides = reader.getSizeT();
						        try {
						        	//System.out.println("x: "+reader.getSizeX()+" y: "+reader.getSizeY()+"\n"); 						            						           
						            //recorder.setVideoOption("allow_raw_vfw", "1");
						        	
						        	// We can store videos as tiff collections or mkv
						        	if (chckbxVideosToImages.isSelected()) {	// TO TIFF
						        		writer = new ImageWriter();
							            writer.setMetadataRetrieve(omexml);
										//writer.setMetadataRetrieve(service.asRetrieve(reader.getMetadataStore()));
							            writer.setInterleaved(reader.isInterleaved());
						        	}else {										// TO MKV
							        	// Create the recorder object which will save the video
										recorder = new FFmpegFrameRecorder(route+".mkv",reader.getSizeX(),reader.getSizeY()); 

							        	// Set recorder properties such as fps, quality, compression method...		
							        	
							            recorder.setFrameRate(1/fps);						        	
							            recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
							            recorder.setVideoBitrate(quality*1000);
							            recorder.setFormat("mkv");						            
							            recorder.setMetadata(load_metadata_map(metaseries));
							            recorder.setVideoQuality(0);
							            recorder.start();																        		
						        	}

						            // Now, we need to know if we are dealing with 8-bit or 16-bit frames
									if (reader.getBitsPerPixel() == 8) {
										depth = false;
						            	if (!chckbxVideosToImages.isSelected()) frame = new Frame(reader.getSizeX(),reader.getSizeY(), Frame.DEPTH_BYTE, 1);
									}else if(reader.getBitsPerPixel() == 16) {
										depth = true;
										if (!chckbxVideosToImages.isSelected()) frame = new Frame(reader.getSizeX(),reader.getSizeY(), Frame.DEPTH_SHORT, 1);						            											
									}					
									
									// Lif videos can be stored in several ways. Take the one we have. It is important to know
									// if it comes first the time or the channel, because then we need to apply a few changes.
									/*Posibilities:
									 * https://downloads.openmicroscopy.org/bio-formats/5.7.1/api/loci/formats/IFormatReader.html#getDimensionOrder--
										 * XYCTZ	* XYTCZ		* XYZCT
										 * XYCZT	* XYTZC		* XYZTC			
										 * In cases where the channels are interleaved (e.g., CXYTZ), C will be the first dimension after X and Y 
										 * (e.g., XYCTZ) and the isInterleaved() method will return true.							 
										 * Since Z=1, here we can have: XYCT or XYTC
									 */
									dimension_order = reader.getDimensionOrder();

									int summ_count;		
									int first_image = 0;
									
									//channel first!
									if (dimension_order.equals("XYCTZ") || dimension_order.equals("XYCZT") || dimension_order.equals("XYZCT") || reader.isInterleaved()){
										summ_count = reader.getSizeC();
										num_slides = reader.getImageCount();
										first_image = channel;
									}else{	//time first!
										summ_count = 1;
										num_slides = reader.getSizeT()*(channel+1);
									}
									
									// Now we are ready to read the frame!
						            for (image=first_image; image<num_slides; image += summ_count) {
										reader.openBytes(image, plane);
										// If we had 16-bit, we will convert it to a 8-bit frame to reduce space to half (2 byte -> 1 byte)
										if (depth) {
											shorts = new short[plane.length/2];
								            ByteBuffer.wrap(plane).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);		
										}
							            
										// We can apply different things to frames. For instance, we could apply an autocontrast method.
										// Nevertheless, this was a proposed feature that I couldn't get working. I left here what I tried
										// in case someone in the future wants to try.
										// You will need to remove opencv exclusion from pom.xml
							            Boolean autocontrast = false;
							            if (autocontrast == true) {
							            	/*
							            	mat = new org.opencv.core.Mat();
							            	mat.create(reader.getSizeY(), reader.getSizeX(), 1);
							            	mat = Imgcodecs.imdecode(new MatOfByte(plane), Imgcodecs.IMREAD_UNCHANGED);
							            	System.out.println("MAT0 x: "+mat.width()+" y: "+mat.height()+"\n");

							            	//CLAHE clahe = Imgproc.createCLAHE();
							            	//clahe.setClipLimit(4);

							            	//clahe.apply(mat,mat2);					
							            	OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
							            	Imgproc.equalizeHist(mat,mat);
							            	System.out.println("MAT x: "+mat.width()+" y: "+mat.height()+"\n");
							            	frame = converter.convert(mat);
							            	System.out.println("CACA x: "+frame.imageWidth+" y: "+frame.imageHeight+"\n");
							            	//https://stackoverflow.com/questions/25535057/opencv-mat-to-javacv-mat-conversion
							            	//buf = new Buffer[] {ByteBuffer.wrap(new Mat(mat.dataAddr()).arrayData().asBuffer().array())};							            	
							            	 */
							            }else {
											if (depth == true) {
								            	buf = new Buffer[] {ByteBuffer.wrap(plane)};
								            }else {
								            	buf = new Buffer[] {ByteBuffer.wrap(plane)};
											}

											// Save as TIFF or MKV!
											if (chckbxVideosToImages.isSelected()) {
									            writer.setId(route+"_"+image+".tif");
									            writer.setSeries(0);
									            writer.saveBytes(0, plane);
					
									            writer.close();
											} else {
												// Set image frame as the frame opened
												frame.image = buf;
												// IDK why but sometimes stride != width. In those cases, there was always a fucking error with opencv
												//Fatal exception and the software broke. I hope the next line does not make strange/bad videos...
												frame.imageStride = frame.imageWidth;
											}
							            }										
								        
								        //System.out.println("frame "+image+" out of "+slides+" stride "+caca.imageStride + " width "+caca.imageWidth);

							            // Save frame in the video
								        if (!chckbxVideosToImages.isSelected()) recorder.record(frame);
								        
								        // Counter update for slides
								        counter_slide++;
								        
								        // Update progressbar of this series
										progress_series = (counter_slide)*100/reader.getImageCount();						
										node.getChildren().get(series).setProgress((int) progress_series);
										
										// Update progressbar of this lif
										node.setProgress((int) (progress_lif + progress_series/node.getChildrenCount()));
										
										if ((((int) progress_series) % 2) == 0){							
											myTreeTable.repaint();
										}		
									}
						            if (!chckbxVideosToImages.isSelected()) {
						            	recorder.stop();
						            	recorder.close();
						            }
		
						        }catch (org.bytedeco.javacv.FrameRecorder.Exception e){
						        	// Now, if it happens some error, probably is the problem of fps
						            e.printStackTrace();
						            // We will try this time by /10. We will do it several times
					        		bypassv /= 10;
					        		double fps_re = 0.0;
					        		// Limit is when bypassv is 10!
					        		if (bypassv < 100) {
					        			bypass = false;
					        		}else {
					        			bypass = true;
										fps_re = fps*bypassv;
										fps_re = (double)((int) fps_re);
										fps_re = fps_re/bypassv;
										
							        	System.out.println("We try with "+1/fps_re+" with video "+series+" in file "+lif+". Channel is: "+channel+". Original fps is "+1/Double.valueOf(metaseries.get("Image|ATLCameraSettingDefinition|CycleTime").toString())+"Bypass is "+bypassv+"\n");
							        	series--;	
					        		}
					        		
						            BufferedWriter writerERROR = null;
						            try {
						                //create a temporary file
						                File logFile = new File(filetosave.substring(0, filetosave.lastIndexOf(ossep))+ossep+"ErrorLOG.txt");

						                writerERROR = new BufferedWriter(new FileWriter(logFile, true));	//APPEND TO FILE
						                if (bypass){
						                	double difference_error = 1/fps_re-1/Double.valueOf(metaseries.get("Image|ATLCameraSettingDefinition|CycleTime").toString());
							                writerERROR.write("Video "+seriename+" in file "+filename+" and channel "+channel+" We cannot convert with original FPS "+
							                		+1/Double.valueOf(metaseries.get("Image|ATLCameraSettingDefinition|CycleTime").toString())+" so we try with "+1/fps_re+
							                		". Difference is: "+Math.abs(difference_error)+" Other data: series "+series+" lif "+lif+" bypassv "+bypassv);
						                }else {
							                writerERROR.write("After trying different fps we couldn't transform it. If there is a video, IT IS WRONG!");
						                }
						                writerERROR.newLine();
						            } catch (Exception ee) {
						                ee.printStackTrace();
						            } finally {
						                try {
						                    // Close the writer regardless of what happens...
						                	writerERROR.close();
						                } catch (Exception ee) {
						                }
						            }
					        		
						        }						    				        
							}else{		//@@@@@@@@  SINGLE IMAGE  @@@@@@@@@
								writer = new ImageWriter();
					            writer.setMetadataRetrieve(omexml);
								//writer.setMetadataRetrieve(service.asRetrieve(reader.getMetadataStore()));
					            writer.setInterleaved(reader.isInterleaved());

					            writer.setId(route+".tif");
					            writer.setSeries(0);
					            reader.openBytes(channel, plane);
					            writer.saveBytes(0, plane);
		
					            writer.close();
					            
						        counter_slide++;
						        
						        progress_series = (counter_slide)*100/reader.getImageCount();						
								node.getChildren().get(series).setProgress((int) progress_series);						
								node.setProgress((int) (progress_lif + progress_series/node.getChildrenCount()));
								myTreeTable.repaint();

							}									
						}else{	//@@@@@@@@  Z-STACK @@@@@@@@@
							// In this case I tried several things.
							// One thing was to include metadata too, which I definetely fail
							// But that's why metadata is also saved as a different file in the output folder ;)
							// Another thing I didn't managed was to compress images... but at least I convert them to 8 bit!
							
							/* method 1
							writer = new ImageWriter();
				            writer.setMetadataRetrieve(omexml);
				            writer.setInterleaved(reader.isInterleaved());
				            writer.setCompression("JPEG-2000 Lossy");
				            CodecOptions opt = fill_codec(zquality, reader, writer.getColorModel());
				            writer.setCodecOptions(opt);
				            System.out.println("es :" + opt.colorModel.getPixelSize());
				            writer.setId(route+".tif");
				            writer.setSeries(0);
	
							for (image=0; image<slides; image++) {
								//reader.openPlane(arg0, 0, 0, omexml.width, arg4)
					            reader.openBytes(image, plane);
					            //https://havecamerawilltravel.com/photographer/tiff-image-compression/
					            //16bit tiff=>ZIP. LZW increases the size!; 8bit lif=>ZIP or LZW
					            //for (int iii = 0; iii<writer.getCompressionTypes().length; iii++) {
					            //	System.out.println(writer.getCompressionTypes()[iii]);
					            //}
					            //Result:
					            //  	JPEG			!>8 bit not possible!
								//	JPEG-2000			!It takes a lot of time. Does it actually work??? Because result image is black!!!
								//	JPEG-2000 Lossy		!It takes a lot of time. Does it actually work??? Because result image is black!!! Image is 1/10 of JPEG-2000
								//	LZW					!I cannot use it since file size will be bigger (we work with 16 bit images)
								//	Uncompressed		!I want to compress them!
								//	zlib				!It is not compressing!!!
					            //
					            
					            shorts = new short[plane.length/2];
					            ByteBuffer.wrap(plane).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

					            max = 0;
					            min = 999999;
					            for (int v = 0; v<shorts.length;v++){
					            	if (max<shorts[v]) max = shorts[v];
					            	if (min>shorts[v]) min = shorts[v];
					            }
					            range = 255./(max-min);	//It will be always >0! no needing an abs!
					            plane8 = new byte[plane.length/2];
					            int max2 = 0;
					            int min2 = 999999;
					            for (int v = 0; v<plane8.length;v++){
					            	plane8[v] = (byte) ((shorts[v]+min)*range - 128);	
					            	if (max2<plane8[v]) max2 = plane8[v];
					            	if (min2>plane8[v]) min2 = plane8[v];
					            }	
					            					            
					            writer.saveBytes(image, plane);
					            
						        counter_slide++;
						        
								total = (counter_slide)*100/reader.getImageCount();				
								node.getChildren().get(jj).setProgress((int) total);						
								node.setProgress((int) (total2 + total/node.getChildrenCount()));
								if ((((int) total) % 2) == 0){							
									myTreeTable.repaint();
								}
							}
							
				            writer.close();
				            */
							
							ImageIO.scanForPlugins();
				            tiffspi = new TIFFImageWriterSpi();
				            writerIO = tiffspi.createWriterInstance();
				            param = writerIO.getDefaultWriteParam();
				            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				            
				            /*
				            J2KImageWriterSpi jpgspi = new J2KImageWriterSpi();
				            javax.imageio.ImageWriter writerIOjpg = jpgspi.createWriterInstance();
				            ImageWriteParam paramjpg = writerIOjpg.getDefaultWriteParam();
				            paramjpg.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				            paramjpg.setCompressionType("JPEG2000");
				            paramjpg.setCompressionQuality(0.8f);
				            File fOutputFilejpg = new File(route+".jpg");
				            ImageOutputStream iosjpg = ImageIO.createImageOutputStream(fOutputFilejpg);
				            writerIOjpg.setOutput(iosjpg);
				            
				            for (int iii = 0; iii<paramjpg.getCompressionTypes().length; iii++) {
					           	System.out.println(paramjpg.getCompressionTypes()[iii]);
					         }*/
					 		//Result:
				            //	CCITT RLE			!javax.imageio.IIOException: I/O error writing TIFF file! ????
				            //	CCITT T.4			!javax.imageio.IIOException: I/O error writing TIFF file! ????
				            //	CCITT T.6	        !javax.imageio.IIOException: I/O error writing TIFF file! ????
							//	LZW					!I cannot use it since file size will be bigger (we work with 16 bit images)
					        //  JPEG				!>8 bit not possible!
							//	ZLib				!Does not compress too much...
				            //	PackBits			!Does not compress anything
				            //	Deflate				!Similar to zlib???
				            //	EXIF JPEG			!javax.imageio.IIOException: Old JPEG compression not supported!
					        //

				            fOutputFile = new File(route+".tif");
				            ios = ImageIO.createImageOutputStream(fOutputFile);
				            writerIO.setOutput(ios);
				            
	     /*
				            List pipi = reader.getCoreMetadataList();			            
				            TIFFImageMetadata im = new TIFFImageMetadata(pipi.get(0));			       
				    Node root
				            = imageMetadata.getAsTree(TIFFImageMetadata.NATIVE_METADATA_FORMAT_NAME);
				    im.setFromTree(TIFFImageMetadata.NATIVE_METADATA_FORMAT_NAME, root);
				    return im;
	
				            IIOMetadata zstackmeta = null;
				            MetadataRoot cacae = omexml.getRoot();
				            System.out.println(cacae.toString());
				            Node caca = ((Node) omexml.getRoot());
				            caca.getNodeType();
				            zstackmeta.setFromTree(TIFFImageMetadata.nativeMetadataFormatName, (Node) omexml.getRoot());
	*/
				            //TIFFImageMetadata im = new TIFFImageMetadata(reader.getMetadataStore().);
				            
							if (reader.getBitsPerPixel() == 8) {
								depth = false;
							}else if(reader.getBitsPerPixel() == 16) {
								depth = true;
							}
							IIOMetadata zstackmeta;
							/*
				            if (depth == true) {
					            zstackmeta = writerIO.getDefaultImageMetadata(
					            		ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_USHORT_GRAY), param);					            	
				            }else {
					            param.setCompressionType("JPEG");
					            param.setCompressionQuality(Float.valueOf(txtZstack.getText())/100.f);
					            zstackmeta = writerIO.getDefaultImageMetadata(
					            		ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_GRAY), param);
				            }
				            

				            //zstackmeta.mergeTree("javax_imageio_tif_1.0", load_metadata_zstack(zstackmeta.getAsTree(zstackmeta.getNativeMetadataFormatName()),metaseries));

				            zstackmeta.setFromTree(zstackmeta.getNativeMetadataFormatName(), 
				            		load_metadata_zstack(zstackmeta.getAsTree(zstackmeta.getNativeMetadataFormatName()),metaseries));
				            */
							String order = reader.getDimensionOrder();
							/*Posibilities:
							 * https://downloads.openmicroscopy.org/bio-formats/5.7.1/api/loci/formats/IFormatReader.html#getDimensionOrder--
								 * XYCTZ	* XYTCZ		* XYZCT
								 * XYCZT	* XYTZC		* XYZTC			
								 * In cases where the channels are interleaved (e.g., CXYTZ), C will be the first dimension after X and Y 
								 * (e.g., XYCTZ) and the isInterleaved() method will return true.							 
								 * Since Z=1, here we can have: XYCT or XYTC
							 */
							//channel first!
							int summ_count;		
							int first_image = 0;
							
							if (order.equals("XYCTZ") || order.equals("XYCZT") || order.equals("XYZCT") || reader.isInterleaved()){
								summ_count = 1;
								num_slides = reader.getSizeZ()*(channel+1);
							}else{
								//time first!
								summ_count = reader.getSizeC();
								num_slides = reader.getImageCount();
								first_image = channel;
							}		            
				            
							int count_normal = 0;
							
				            for (image=first_image; image<num_slides; image += summ_count) {
					            reader.openBytes(image, plane);
					            //in = new ByteArrayInputStream(plane);
					            //!!!plane is byte. In java byte is signed: -128:128
					            //Therefore, FormatTools.getPlaneSize when reads that is 16bit
					            //convert the 16bit = 2 byte into 2 bytes.
					            //That is why plane is X*Y*2. I want shorts!!!
					            max = 0;
					            min = 999999;
					            int max2 = 0;
					            int min2 = 999999;

								if (depth) {
									shorts = new short[plane.length/2];
						            ByteBuffer.wrap(plane).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);	
						            
						            //	Check if I have unsigned or signed. If neg values appear, I have signed
						            for (int v = 0; v<shorts.length;v++){
						            	if (max<shorts[v]) max = shorts[v];
						            	if (min>shorts[v]) min = shorts[v];
						            }
						            range = 255./(max-min);	//It will be always >0! no needing an abs!
						            plane8 = new byte[plane.length/2];
						            for (int v = 0; v<plane8.length;v++){
						            	plane8[v] = (byte) ((shorts[v]+min)*range - 128);	
						            	if (max2<plane8[v]) max2 = plane8[v];
						            	if (min2>plane8[v]) min2 = plane8[v];
						            }	
						            

								}else {
						            //Even I have signed, it is ok with ushort.Change to Byte if I have 8 bit!!!
						            /**/
						            //	Check if I have unsigned or signed. If neg values appear, I have signed
						            for (int v = 0; v<plane.length;v++){
						            	if (max<plane[v]) max = plane[v];
						            	if (min>plane[v]) min = plane[v];
						            }
						            range = 255./(max-min);	//It will be always >0! no needing an abs!
						            plane8 = plane;
						            for (int v = 0; v<plane8.length;v++){
						            	plane8[v] = (byte) ((plane[v]+min)*range - 128);	
						            	if (max2<plane8[v]) max2 = plane8[v];
						            	if (min2>plane8[v]) min2 = plane8[v];
						            }	
									
									
								}			
								
								convertedGrayscale = new BufferedImage(reader.getSizeX(),
					            		reader.getSizeY(), BufferedImage.TYPE_BYTE_GRAY);	
					            convertedGrayscale.getRaster().setDataElements(0, 0, reader.getSizeX(),
					            		reader.getSizeY(), plane8);		
					            /* 	
					            if (image!=first_image){
					            	writerIO.writeInsert(count_normal, new IIOImage(convertedGrayscale, null, zstackmeta), param);		            
					            }else{
					            	writerIO.write(null, new IIOImage(convertedGrayscale, null, zstackmeta), param);
						            //writerIOjpg.write(null, new IIOImage(convertedGrayscale, null, null), paramjpg);
					            }
					            */
								
								if (image!=first_image){
					            	writerIO.writeInsert(count_normal, new IIOImage(convertedGrayscale, null, null), param);		            
					            }else{
					            	writerIO.write(null, new IIOImage(convertedGrayscale, null, null), param);
						            //writerIOjpg.write(null, new IIOImage(convertedGrayscale, null, null), paramjpg);
					            }
								
						        counter_slide++;
						        count_normal++;
						        
						        progress_series = (counter_slide)*100/reader.getImageCount();				
								node.getChildren().get(series).setProgress((int) progress_series);						
								node.setProgress((int) (progress_lif + progress_series/node.getChildrenCount()));
								if ((((int) progress_series) % 2) == 0){							
									myTreeTable.repaint();
								}
							}
							writerIO.dispose();
				            ios.flush();
				            ios.close();									           
				            
						}
						//Finishing!
						
						boolean activate = true;
						for (int j1=0; j1<treeTableModel.getChildCount(treeTableModel.getRoot());j1++ ){
							MyDataNode node1 = (MyDataNode) treeTableModel.getChild(treeTableModel.getRoot(), j1);
							
							if (node1.getProgress()<100){
								activate = false;
							}
						}
						if (activate){
							btnExtract.setText("EXTRACT VIDEOS!");
							myTreeTable.setEnabled(true);
						}
						if (!bypass) {
							node.getChildren().get(series).setProgress((int) 100);										
							myTreeTable.repaint();
						}
					}
					//progress_lif += 100/node.getChildrenCount();
				} //next series
				node.setProgress(501);
			  	reader.close();
			} //next lif

		} catch (Exception e) {
			// TODO Auto-generated catch block		
			e.printStackTrace();

			if (!reader.equals(null)){
				try {
					reader.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if (!writer.equals(null)){
				try {
					writer.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if (!writerIO.equals(null)){
				writerIO.dispose();
				convertedGrayscale.flush();
			}			
			if (!ios.equals(null)){
				try {
					ios.flush();
					ios.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

	}
	
	/**
	 * Fill metadata for this serie.
	 * 
	 * @param service  	An OMEXMLService
	 * @param omexml	Original metadata
	 * @param series	Serie to select
	 * 
	 * @return omexml2	New metadata for this serie
	*/
	public IMetadata fill_metadata(OMEXMLService service, IMetadata omexml, int series, int zstacks){
        IMetadata omexml2;
		try {
			omexml2 = service.createOMEXMLMetadata();
			omexml2.setImageID("Image:0", 0);
			omexml2.setPixelsID("Pixels:0", 0);

			int channelCount = 1;
			omexml2.setPixelsBigEndian(omexml.getPixelsBigEndian(series), 0);
			omexml2.setPixelsDimensionOrder(omexml.getPixelsDimensionOrder(series), 0);
			omexml2.setPixelsType(omexml.getPixelsType(series), 0);
	        omexml2.setPixelsSizeX(omexml.getPixelsSizeX(series), 0);
	        omexml2.setPixelsSizeY(omexml.getPixelsSizeY(series), 0);
	        omexml2.setPixelsSizeZ(omexml.getPixelsSizeZ(series), 0);
	        omexml2.setPixelsSizeZ(new PositiveInteger(zstacks), 0);
	        omexml2.setPixelsSizeC(new PositiveInteger(channelCount), 0);
	        omexml2.setPixelsSizeT(new PositiveInteger(1), 0);
	
	        for (int channel=0; channel<channelCount; channel++) {
	          omexml2.setChannelID("Channel:0:" + 0, 0, 0);
	          omexml2.setChannelSamplesPerPixel(new PositiveInteger(1), 0, channel);
	        }
	        
	        Unit<Length> unit = UNITS.MICROM; //MICROMETER
	       /* Length physicalSizeX = new Length(1.0, unit);
	        Length physicalSizeY = new Length(1.5, unit);*/
	        //Length physicalSizeZ = new Length(1, unit);
	        omexml2.setPixelsPhysicalSizeX(omexml.getPixelsPhysicalSizeX(series), 0);
	        omexml2.setPixelsPhysicalSizeY(omexml.getPixelsPhysicalSizeY(series), 0);
	        //omexml2.setPixelsPhysicalSizeZ(physicalSizeZ, 0);
	        omexml2.setPixelsPhysicalSizeZ(omexml.getPixelsPhysicalSizeZ(series), 0);
	        return omexml2;
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
        
	}
	
	/**
	 * Fill CodecOptions for this serie.
	 * 
	 * @param quality  	quality for compression
	 * @param reader	ImageReader where data comes from
	 * @param Color		ColorModel from the writer
	 * 
	 * @return opt	New CodecOptions for this serie
	*/
	public CodecOptions fill_codec(int quality, IFormatReader reader, ColorModel Color){
        CodecOptions opt = new CodecOptions();
        opt.bitsPerSample=8;
        opt.channels=reader.getSizeC();
        opt.colorModel=Color;
        opt.height=reader.getSizeY();
        opt.interleaved=reader.isInterleaved();
        opt.littleEndian=reader.isLittleEndian();
        opt.lossless=true;
        opt.maxBytes=reader.getSizeX()*reader.getSizeY();
        opt.previousImage=null;
        opt.quality=quality;
        opt.signed=true;
        opt.tileGridXOffset=0;
        opt.tileGridYOffset=0;
        opt.tileHeight=reader.getSizeY();
        opt.tileWidth=reader.getSizeX();
        opt.width=reader.getSizeX();
        opt.ycbcr=false;
        
		return opt;
	}
		
	/**
	 * Helper to add metadata to each z-stack. Read TIFF_tags.txt file for available data structure.
	 * 
	 * @param metadata  Hashtable<String, Object> with all the metadata of this .lif
	 * 
	 * @return result	String full of metadata for ffmpeg
	 * 
	*/
	public Node load_metadata_zstack(Node rootIOMetadata, Hashtable<String, Object> metadata){    
	    IIOMetadataNode textEntry;
	    IIOMetadataNode text = new IIOMetadataNode("TIFFIFD");

		//We get all the keys and values
		for(Map.Entry<String, Object> entry: metadata.entrySet()){
        	if (!entry.getKey().equals(null)){
        	    textEntry = new IIOMetadataNode("tEXtEntry");
        	    textEntry.setAttribute("keyword", entry.getKey().toString());
        	    textEntry.setAttribute("value", entry.getValue().toString());
        	    text.appendChild(textEntry);
        	}
        }	    

	    IIOMetadataNode root = new IIOMetadataNode("com_sun_media_imageio_plugins_tiff_image_1.0");
	    root.appendChild(text);
	    
	    return root;

	    /*
        IIOMetadataNode n = new IIOMetadataNode("Data");
        
		//We get all the keys and values
		for(Map.Entry<String, Object> entry: metadata.entrySet()){
        	if (!entry.getKey().equals(null)){
                n.setAttribute(entry.getKey().toString(), entry.getValue().toString());
        	}
        }
        rootIOMetadata.appendChild(n);
        		return rootIOMetadata;
*/	    
	    
	}
	
	/**
	 * Helper to add metadata to each final video.
	 * 
	 * @param metadata  Hashtable<String, Object> with all the metadata of this .lif
	 * 
	 * @return result	String full of metadata for ffmpeg
	 * 
	*/
	public Map<String, String> load_metadata_map(Hashtable<String, Object> metadata){
		Map<String, String> meta_unsorted = new HashMap<String, String>(0);
		
		//We get all the keys and values
		for(Map.Entry<String, Object> entry: metadata.entrySet()){
        	if (!entry.getKey().equals(null)){
        		meta_unsorted.put(entry.getKey().toString(), entry.getValue().toString());
        	}
        }

        Map<String, String> treeMap = new TreeMap<String, String>(meta_unsorted);
        
		return treeMap;
	}
	
	/**
	 * Helper to add metadata to each final video.
	 * 
	 * @param metadata  Hashtable<String, Object> with all the metadata of this .lif
	 * 
	 * @return result	String full of metadata for ffmpeg
	 * @deprecated
	*/
	public String load_metadata(Hashtable<String, Object> metadata){
		String result = "";
		String[][] meta_ordered = new String[metadata.keySet().size()][2];
		//Object[] keys = metadata.keySet().toArray();
		int i = 0;
		
		//We get all the keys and values
		for(Map.Entry<String, Object> entry: metadata.entrySet()){
        	if (!entry.getKey().equals(null)){
        		meta_ordered[i][0] = entry.getKey();
        		meta_ordered[i][1] = (String) entry.getValue();
        		i++;
        	}
        }
		
		//Then we order them!
		Arrays.sort(meta_ordered, new Comparator<String[]>(){
		    @Override
		    public int compare(String[] first, String[] second){
		        // compare the first element
		        int comparedTo = first[0].compareTo(second[0]);
		        // if the first element is same (result is 0), compare the second element
		        if (comparedTo == 0) return first[1].compareTo(second[1]);
		        else return comparedTo;
		    }
		});

		//And finally we add them!
		for(int j = 0; j<i;j++) {
			result = result + " -metadata \"" + meta_ordered[j][0] +"\"=\""+meta_ordered[j][1]+"\"";
		}
		
		//for(Map.Entry<String, Object> entry: metadata.entrySet()){
		//for(Object key : meta_ordered) {
        	//if (!entry.getKey().equals(null)){
			//if (!metadata.get(key).equals(null)){
        		//result = result + " -metadata \"" + metadata.values()..getKey()+"\"=\""+entry.getValue()+"\"";
        	//}
        //}
       // }

		result.trim();
		return result;
	}
	
	/**
	 * Helper to save metadata file.
	 * 
	 * @param metadata  Hashtable<String, Object> with all the metadata of this .lif
	 * 
	 * @return result	String full of metadata for ffmpeg
	*/
	public void save_metadata(Hashtable<String, Object> metadata, String outFile){
		String[][] meta_ordered = new String[metadata.keySet().size()][2];
		//Object[] keys = metadata.keySet().toArray();
		int i = 0;
		
		//We get all the keys and values
		for(Map.Entry<String, Object> entry: metadata.entrySet()){
        	if (!entry.getKey().equals(null)){
        		meta_ordered[i][0] = entry.getKey();
        		meta_ordered[i][1] = (String) entry.getValue();
        		i++;
        	}
        }
		
		//Then we order them!
		Arrays.sort(meta_ordered, new Comparator<String[]>(){
		    @Override
		    public int compare(String[] first, String[] second){
		        // compare the first element
		        int comparedTo = first[0].compareTo(second[0]);
		        // if the first element is same (result is 0), compare the second element
		        if (comparedTo == 0) return first[1].compareTo(second[1]);
		        else return comparedTo;
		    }
		});
		try {
			//http://www.mkyong.com/java/how-to-create-xml-file-in-java-dom/
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	
			// root elements
			/* parse existing file to DOM */
			Document doc = null;
			Element rootElement = null;
			if (new File(outFile).exists()){
				doc = docBuilder.parse(new File(outFile));	
				rootElement = doc.getDocumentElement();
			}else{
				doc = docBuilder.newDocument();	
				rootElement = doc.createElement("images");
				doc.appendChild(rootElement);
			}

			// staff elements
			Element staff = doc.createElement("image");
			rootElement.appendChild(staff);
	
			// set attribute to staff element
			Attr attr = doc.createAttribute("name");
			attr.setValue(meta_ordered[0][1]);
			staff.setAttributeNode(attr);
	
			// shorten way
			// staff.setAttribute("id", "1");
	
			Element sElement;
	        for (int i1 = 0; i1<meta_ordered.length; i1++){
	        	sElement = doc.createElement(meta_ordered[i1][0].replace(" ", "_").replace("|", "__"));
	        	sElement.appendChild(doc.createTextNode(meta_ordered[i1][1]));
	    		staff.appendChild(sElement);
	        }	   
	    
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			//if FileOutputStream is not present, a space will be displayed as %20
			//https://stackoverflow.com/questions/20434510/transformer-library-that-comes-with-java-converts-spaces-in-file-paths-to-20
			StreamResult result = new StreamResult(new FileOutputStream(new File(outFile)));
	
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }	
	
	/**
	 * Convert tif images into mkv video. After, it deletes all tif pictures and move video from temp folder.
	 * 
	 * @param file  route to tif pictures including Series name but not ".tif"
	 * @param row	row of this video in the table
	 * @param fps	fps for video
	 * @param quality	value for quality (b:v ffmpeg option). It is in "k" units.
	 * 
	 * @return void
	 * @deprecated
	*/
	public void ffmpeg(String file, MyDataNode node, int row, double fps, int quality, Hashtable<String, Object> metadata){
		/*
		 * ffmpeg -i input -c copy
       				-metadata key1=value1
       				-metadata:s:v key2=value2
       				-metadata:s:a:0 key3=value3
					out.mkv
					The first metadata options set a global value, the 2nd is applied to all video streams, 
					and the third to the 1st audio stream only.
			from: https://superuser.com/questions/1102265/how-to-save-custom-data-into-header-in-movie-file-using-ffmpeg/1102278#1102278
		 */
		String metadata_ff = load_metadata(metadata);
		System.out.println(metadata_ff+"\n");
		String command = FileSystems.getDefault().getPath("").toAbsolutePath()+"\\ffmpeg.exe -framerate "+fps+" -i \""+file+"_%d.tif\" -b:v "+quality+"k "+ metadata_ff +" \""+ file +".mkv\"";
		File video = new File(file+".mkv");
		if (video.exists()) video.delete();
		
		
		try {
			Process p = Runtime.getRuntime().exec(command);
			
	        Scanner sc = new Scanner(p.getErrorStream());

	        // Find duration
	        Pattern durPattern = Pattern.compile("(?<=Duration: )[^,]*");
	        String dur = sc.findWithinHorizon(durPattern, 0);
	        if (dur == null)
	          throw new RuntimeException("Could not parse duration.");
	        String[] hms = dur.split(":");
	        double totalSecs = Integer.parseInt(hms[0]) * 3600
	                         + Integer.parseInt(hms[1]) *   60
	                         + Double.parseDouble(hms[2]);
	        System.out.println("Total duration: " + totalSecs + " seconds.");

	        // Find time as long as possible.
	        Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
	        String[] matchSplit;
	        String match;
	        
	        while (null != (match = sc.findWithinHorizon(timePattern, 0))) {
	            matchSplit = match.split(":");
	            if (matchSplit.length<2) continue;
	            if (!matchSplit[0].equals("") && !matchSplit[1].equals("") && !matchSplit[2].equals("")){
		            double progress = Integer.parseInt(matchSplit[0]) * 3600 +
		                Integer.parseInt(matchSplit[1]) * 60 +
		                Double.parseDouble(matchSplit[2]) / totalSecs;
		            	node.getChildren().get(row).setProgress((int) (progress*100+100));
						if (( (int) ( (progress*100) % 2) ) == 0){							
							myTreeTable.repaint();
						}
	            }
	        }
        	node.getChildren().get(row).setProgress((int) 510);
			myTreeTable.repaint();	
			p.waitFor();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File folder = new File(file.substring(0, file.lastIndexOf("\\")));
		String file_rem = file.substring(file.lastIndexOf("\\")+1, file.length())+"_";
		for (File f : folder.listFiles()) {
		    if (f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf("\\")+1,f.getAbsolutePath().length()).startsWith(file_rem)){
			//if (f.getName().substring().startsWith(file_rem)) {
		        f.delete();
		    }
		}
		video = new File(file+".mkv");
		String dir = video.getAbsolutePath().substring(0, video.getAbsolutePath().lastIndexOf("\\"));
		String dir2 = dir.substring(0, dir.lastIndexOf("\\"));
		//if (new File(dir2+"\\"+file_rem.substring(0, file_rem.length()-1)+".mkv").exists()) new File(dir2+"\\"+file_rem.substring(0, file_rem.length()-1)+".mkv").delete();
		if (video.exists()) video.renameTo(new File(dir2+"\\"+file_rem.substring(0, file_rem.length()-1)+".mkv"));
    	node.getChildren().get(row).setProgress((int) 610);
		myTreeTable.repaint();			

		/*if (row==LIFs.getRowCount()-1){
			video = new File(dir+"\\");
			for (File f : video.listFiles()) {
			        f.delete();			    
			}
			video.delete();
		}*/	
		boolean activate = true;
		for (int j=0; j<treeTableModel.getChildCount(treeTableModel.getRoot());j++ ){
			MyDataNode node1 = (MyDataNode) treeTableModel.getChild(treeTableModel.getRoot(), j);
			
			if (node1.getProgress()<100){
				activate = false;
			}
		}
		if (activate){
			btnExtract.setText("EXTRACT VIDEOS!");
			myTreeTable.setEnabled(true);
		}

	}
	
	/**
	 * 
	 * @deprecated
	 */
	public void save_image(String file, MyDataNode node, int row){
		File folder = new File(file.substring(0, file.lastIndexOf("\\")));
		String file_rem = file.substring(file.lastIndexOf("\\")+1, file.length())+"_";

		File image = new File(file+"_.tif");
		String dir = image.getAbsolutePath().substring(0, image.getAbsolutePath().lastIndexOf("\\"));
		String dir2 = dir.substring(0, dir.lastIndexOf("\\"));
		//if (new File(dir2+"\\"+file_rem.substring(0, file_rem.length()-1)+".mkv").exists()) new File(dir2+"\\"+file_rem.substring(0, file_rem.length()-1)+".mkv").delete();
		if (image.exists()) image.renameTo(new File(dir2+"\\"+file_rem.substring(0, file_rem.length()-1)+".tif"));
    	node.getChildren().get(row).setProgress((int) 610);
		myTreeTable.repaint();			

		/*if (row==LIFs.getRowCount()-1){
			video = new File(dir+"\\");
			for (File f : video.listFiles()) {
			        f.delete();			    
			}
			video.delete();
		}*/	
		boolean activate = true;
		for (int j=0; j<treeTableModel.getChildCount(treeTableModel.getRoot());j++ ){
			MyDataNode node1 = (MyDataNode) treeTableModel.getChild(treeTableModel.getRoot(), j);
			
			if (node1.getProgress()<100){
				activate = false;
			}
		}
		if (activate){
			btnExtract.setText("EXTRACT VIDEOS!");
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
		    			if (subfiles.get(j).getAbsolutePath().endsWith(".lif")) {
		    				IFormatReader reader = new ImageReader();
		 			  		try {
								reader.setId(subfiles.get(j).getAbsolutePath());
		 						Hashtable<String, Object> metaseries = reader.getSeriesMetadata();
		 						String seriename;
		 						
		 				        List<MyDataNode> children = new ArrayList<MyDataNode>();		 				         
			 			  		for (int k = 0; k<reader.getSeriesCount();k++){
			 			  			reader.setSeries(k);
			 			  			metaseries = reader.getSeriesMetadata();
			 			  			seriename = metaseries.get("Image name").toString();
		 							String temp2 = reader.getCurrentFile().substring(0,reader.getCurrentFile().lastIndexOf(ossep));
			 				        children.add(new MyDataNode(seriename, 2,new TextButtonCell(temp2), 0, null, reader.getCurrentFile()));
				    				btnExtract.setEnabled(true);
			 			  		}
			 			  		
		 						if (reader.getSeriesCount()>0) {
		 							String temp = reader.getCurrentFile().substring(reader.getCurrentFile().lastIndexOf(ossep)+1,reader.getCurrentFile().length());
		 							String temp2 = reader.getCurrentFile().substring(0,reader.getCurrentFile().lastIndexOf(ossep));
		 							rootNodes.add(new MyDataNode(temp, 2,new TextButtonCell(temp2), 0, children, reader.getCurrentFile()));
		 							update_tree(false);
		 						}

			 			  		reader.close();
							} catch (FormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}		 			  		
		    			}
		    		}
		    	}else{
	    			if (f.getAbsolutePath().endsWith(".lif")){
	    				IFormatReader reader = new ImageReader();
	 			  		try {
							reader.setId(f.getAbsolutePath());
	 						Hashtable<String, Object> metaseries = reader.getSeriesMetadata();
	 						String seriename;

	 				        List<MyDataNode> children = new ArrayList<MyDataNode>();		 				         
		 			  		for (int k = 0; k<reader.getSeriesCount();k++){
		 			  			reader.setSeries(k);
		 			  			metaseries = reader.getSeriesMetadata();
		 			  			seriename = metaseries.get("Image name").toString();
	 							String temp2 = reader.getCurrentFile().substring(0,reader.getCurrentFile().lastIndexOf(ossep));

		 				        children.add(new MyDataNode(seriename, 2,new TextButtonCell(temp2), 0, null, reader.getCurrentFile()));
			    				btnExtract.setEnabled(true);
		 			  		}
		 			  		
	 						if (reader.getSeriesCount()>0){
	 							String temp = reader.getCurrentFile().substring(reader.getCurrentFile().lastIndexOf(ossep)+1,reader.getCurrentFile().length());
	 							String temp2 = reader.getCurrentFile().substring(0,reader.getCurrentFile().lastIndexOf(ossep));

	 							rootNodes.add(new MyDataNode(temp, 2,new TextButtonCell(temp2), 0, children, reader.getCurrentFile()));
	 							update_tree(false);
	 						}

	 						reader.close();
						} catch (FormatException e) {
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
		        myTreeTable = new MyTreeTable(treeTableModel);
			}else{
				myTreeTable.Change_TreeTableModel(treeTableModel);	
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

class TreeIcon implements Icon {

  private int SIZE = 0;

  public TreeIcon() {
  }

  public int getIconWidth() {
      return SIZE;
  }

  public int getIconHeight() {
      return SIZE;
  }
  
	@Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
      //System.out.println(c.getWidth() + " " + c.getHeight() + " " + x + " " + y);
  }

}

