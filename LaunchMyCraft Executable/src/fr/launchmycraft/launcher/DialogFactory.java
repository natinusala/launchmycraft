package fr.launchmycraft.launcher;

import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DialogFactory 
{
	//TODO Entrée pour bouton 1
	
	public enum DialogType {
		ERROR("ic_error_black_24dp_2x.png", "Erreur"),
		QUESTION("ic_help_black_24dp_2x.png", "Question"),
		INFO("ic_info_black_24dp_2x.png", "Information"),
		WARNING("ic_warning_black_24dp_2x.png", "Attention");
		
		private String icon;
		private String title;
		
		private DialogType(String icon, String title)
		{
			this.icon = icon;
			this.title = title;
		}

		public String getIcon() 
		{
			return icon;
		}

		public String getTitle() 
		{
			return title;
		}
	};
	
	public static void showDialog(DialogType dialogType, String message, String title, String button1Message, String button2Message, EventHandler<ActionEvent> button2Handler)
	{
		new JFXPanel(); //pour initialiser la plateforme

		Platform.runLater(new Runnable() {	
			@Override
			public void run() 
			{
				Stage alert = new Stage();
				alert.initModality(Modality.WINDOW_MODAL);
				alert.setTitle(title);
						
				Parent root = null;
				try 
				{
					root = FXMLLoader.load(DialogFactory.class.getClassLoader().getResource("theme/dialog.fxml"));
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				
				Scene scene = new Scene(root);
				
				//Titre
				((Label) scene.lookup("#titleLabel")).setText(title);
				
				//Icone
				((ImageView) scene.lookup("#iconImageView")).setImage(new Image(DialogFactory.class.getClassLoader().getResource("theme/icons/" + dialogType.icon).toExternalForm()));
				
				//Texte
				((Label) scene.lookup("#textLabel")).setText(message);
				
				//Bouton
				Button button1 = ((Button) scene.lookup("#button1"));
				button1.setText(button1Message);
				button1.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent arg0) 
					{
						alert.close();
					}
				});
				
				if (button2Message != null)
				{
					Button button2 = ((Button) scene.lookup("#button2"));
					button2.setText(button2Message);
					button2.setOnAction(button2Handler);
				}
				
				alert.setScene(scene);
				alert.showAndWait();
			}
		});
	}
	
	public static void showDialog(DialogType dialogType, String message)
	{
		showDialog(dialogType, message, dialogType.title, "OK", null, null);
	}
}
