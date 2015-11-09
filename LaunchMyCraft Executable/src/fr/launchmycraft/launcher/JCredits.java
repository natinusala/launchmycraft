package fr.launchmycraft.launcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.net.URL;
import java.util.Map;

import javax.swing.JTextField;

@SuppressWarnings("serial")
public class JCredits extends JTextField
{
	@SuppressWarnings("unchecked")
	public JCredits(String text, final String url)
	{
		super(text);
		
		Font font = getFont();
		@SuppressWarnings("rawtypes")
		Map attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		attributes.put(TextAttribute.SIZE, 10);
		setFont(font.deriveFont(attributes));
		setOpaque(false);
		setBorder(null);
		setForeground(new Color(135,121,117));
		setEditable(false);
        setFocusable(false);
        setBorder(null);
		
		this.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {					
				try {
					OperatingSystem.openLink(new URL(url).toURI());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {}

			@Override
			public void mouseExited(MouseEvent arg0) {}

			@Override
			public void mousePressed(MouseEvent arg0) {}

			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
		});
	}
}