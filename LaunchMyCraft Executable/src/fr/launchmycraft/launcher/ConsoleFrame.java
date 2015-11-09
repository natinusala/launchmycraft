package fr.launchmycraft.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

@SuppressWarnings("serial")
public class ConsoleFrame extends JFrame 
{
	static JTextPane editorPane;
	static JScrollPane scrollPane;
	
	static ConsoleFrame instance;
	
	public ConsoleFrame() throws IOException
	{
		instance = this;
		
		//Title
		this.setTitle("Console du launcher");
		this.setSize(new Dimension(640, 480));
		
		//Image
		ImageIcon img = new ImageIcon(ImageIO.read(ConsoleFrame.class.getResourceAsStream("/icon.png")));
		this.setIconImage(img.getImage());
        
        //Contenu
        JPanel panel = new JPanel(new BorderLayout());     
		
        editorPane = new JTextPane();
        editorPane.setBackground(Color.DARK_GRAY);
        editorPane.setForeground(Color.white);
        editorPane.setEditable(false);
         
        scrollPane = new JScrollPane(editorPane);
        panel.add(scrollPane, BorderLayout.CENTER); 
        
        JButton saveButton = new JButton("Enregistrer les logs...");
        saveButton.addActionListener(new ActionListener() 
        {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					LoggerUtils.openSavePrompt();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, "Impossible d'enregistrer les logs : " + ex.getLocalizedMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
				}
			}
        });
        panel.add(saveButton, BorderLayout.SOUTH);      
        
        this.setContentPane(panel);
	}
	
	static boolean firstLinePrinted = false;
	
	private static void scrollToBottom()
	{
		editorPane.setCaretPosition(editorPane.getText().length());	
	}
	
	public static void writeLine(String line)
	{
		if (!instance.isVisible())
		{
			return;
		}
		if (!firstLinePrinted)
		{
			editorPane.setText(editorPane.getText() + line);	
			firstLinePrinted = true;
		}
		else
		{
			editorPane.setText(editorPane.getText() + "\n" + line);	
		}		
		
		scrollToBottom();
	}
	
	public static void write(String text)
	{
		editorPane.setText(editorPane.getText() + text);	
		scrollToBottom();
	}
}
