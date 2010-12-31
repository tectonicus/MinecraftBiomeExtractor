package com.google.code.minecraftbiomeextractor;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class MinecraftBiomeExtractorGUI extends JPanel implements ActionListener, KeyListener {
	
	private static final long serialVersionUID = 920622019968842955L;
	static private final String newline = "\n";
	JComboBox openMenu;
	JTextArea log;
	JButton go;
	JCheckBox delete_existing_files;
	JFileChooser fc;
	
	File minecraftFolderPath;
	
	File world_folder = null;
	boolean world_selected = false;
	
	boolean bound = false;
	Thread process_thread = null;
	WorldProcessor world_processor = null;
	
	public void message(String msg)
	{
		log.append(msg);
		log.setCaretPosition(log.getDocument().getLength());
	}
	
	public MinecraftBiomeExtractorGUI()  {
	super(new BorderLayout());
	
	if (Os.isMac())
	{
		minecraftFolderPath = new File(FileSystemView.getFileSystemView().getHomeDirectory(),"Library");
		minecraftFolderPath = new File(minecraftFolderPath,"Application Support");
		minecraftFolderPath = new File(minecraftFolderPath,"minecraft");
	}
	else if (Os.isWindows())
	{
		minecraftFolderPath = new File(System.getenv("APPDATA"),".minecraft");
	}
	else if (Os.isNix())
	{
		minecraftFolderPath = new File(FileSystemView.getFileSystemView().getHomeDirectory(),".minecraft");
	}
    else
	{
		minecraftFolderPath = new File(FileSystemView.getFileSystemView().getHomeDirectory(),".minecraft");
	}
	
	//Create the log first, because the action listeners
	//need to refer to it.
	log = new JTextArea(20,50);
	log.setMargin(new Insets(5,5,5,5));
	log.setEditable(false);
	JScrollPane logScrollPane = new JScrollPane(log);

	//Create a file chooser
	fc = new JFileChooser();
	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	// Create the Combo Box
	String[] defaultStrings = { "Choose a world to update","World1", "World2", "World3", "World4", "World5", "Open World Folder..." };
	File checkworld;
	
	openMenu = new JComboBox(defaultStrings);
	openMenu.setSelectedIndex(0);
	openMenu.addActionListener(this);
	
	delete_existing_files = new JCheckBox("Clear Cache",false);
	
	go = new JButton("Go");
	go.addActionListener(this);
	
	for(int i = 5; i > 0; i--)
	{
		checkworld = new File(new File(minecraftFolderPath,"saves"),"World"+Integer.toString(i));
		if (!checkworld.exists())
			openMenu.removeItemAt(i);
	}
	
	//For layout purposes, put the buttons in a separate panel
	JPanel buttonPanel = new JPanel(); //use FlowLayout
	buttonPanel.add(openMenu);
	buttonPanel.add(delete_existing_files);
	buttonPanel.add(go);
	//Add the buttons and the log to this panel.
	add(buttonPanel, BorderLayout.PAGE_START);
	add(logScrollPane, BorderLayout.CENTER);
	
	// Create our escape key listener
	log.addKeyListener(this);
	//openMenu.addKeyListener(this);
	
	log.append("Minecraft Biome Extractor (v0.7 beta)"+newline);
	log.append("By Donkey Kong"+newline+newline);
	
	log.repaint();
	
	// Create a world processor and bind to minecraft
	world_processor = new WorldProcessor(this,false,delete_existing_files.isSelected());
	bound = world_processor.bindToMinecraft();
	
	if (bound)
	{
		message("Select a world to extract its biomes."+newline);
	}
	else
	{
		message("Failed to bind to Minecraft, cannot generate biomes." +newline 
				 + "Review the above messages to see if there's anything you can do about it." +newline
				 + "If not, check online for a new version."+newline);
	}
	
}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode()== KeyEvent.VK_ESCAPE)
			world_processor.stopRunning();
    }

public void startupWorkThread()
{	
	// Make sure that world_processor is done
	if (process_thread == null || !process_thread.isAlive())
	{
		// Setup world_processor with the current worldpath and options
		world_processor.setWorldFolder(world_folder);
		world_processor.setFlush(delete_existing_files.isSelected());
		
		// Create a new thread and start it.
		process_thread = new Thread(world_processor);
		process_thread.start();
	}
}

public void actionPerformed(ActionEvent e) 
{
	//Handle open button action.
	if (!bound)
	{
		message(newline + newline + "Setup failed, cannot extract biomes. See above for more details." + newline + newline);
		// Reset the dropdown to its proper state
		openMenu.setSelectedIndex(0);
		return;
	}
	
	if (e.getSource() == go && world_selected  && (process_thread == null || !process_thread.isAlive()))
	{
		this.startupWorkThread();
	}
	else if (e.getSource() == go && world_selected  && process_thread != null && process_thread.isAlive())
	{
		// This case is not used at the moment.
	}
	else if (e.getSource() == openMenu)
	{
		if (openMenu.getSelectedIndex()==(openMenu.getItemCount()-1) )
		{
			int returnVal = fc.showOpenDialog(MinecraftBiomeExtractorGUI.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				world_folder = fc.getSelectedFile();
				world_selected = true;
			} else {
				// Reset the dropdown to its proper state
				openMenu.setSelectedIndex(0);
				world_selected = false;
			}
			log.setCaretPosition(log.getDocument().getLength());
		}
		else if (openMenu.getSelectedIndex() !=0)
		{
			world_folder =  new File(new File(minecraftFolderPath,"saves"),(String)openMenu.getSelectedItem());
			world_selected = true;
		}
	}
}

/**
* Create the GUI and show it.  For thread safety,
* this method should be invoked from the
* event dispatch thread.
 * @throws IOException 
*/
private static void createAndShowGUI() 
{
	//Create and set up the window.
	JFrame frame = new JFrame("Minecraft Biome Extractor");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	//Add content to the window.
	frame.add(new MinecraftBiomeExtractorGUI());

	//Display the window.
	frame.pack();
	frame.setVisible(true);
}

public static void launchGUI()
{
	SwingUtilities.invokeLater(new Runnable() {
		public void run() 
		{
			//Turn off metal's use of bold fonts
			UIManager.put("swing.boldMetal", Boolean.FALSE); 
			createAndShowGUI();
		}
	}
	);
}

public void keyPressed(KeyEvent e) {
	// Ignore. This just needs to be here to handle the esc key.
}

public void keyTyped(KeyEvent e) {
	// Ignore. This just needs to be here to handle the esc key.
}

}
