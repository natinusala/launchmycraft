package fr.launchmycraft.launcher;

import java.awt.GridLayout;
import java.net.URI;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.swing.JPanel;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

@SuppressWarnings({ "serial" })
public class WebsiteTab extends JPanel
{
	public WebsiteTab(final String url, final boolean externalBrowser)
	{
		final JFXPanel jfxPanel = new JFXPanel();
		this.setLayout(new GridLayout(1, 1));
		this.add(jfxPanel);
		Platform.runLater(new Runnable() 
		{
			@Override
			public void run() 
			{							
				final WebView view = new WebView();						
				view.getEngine().load(url);

				if (externalBrowser)
				{
					view.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() 
					{
						@Override
						public void changed(ObservableValue<? extends State> observable, final State oldValue, State newValue)
						{
							if (newValue.equals(State.SUCCEEDED))
							{
								NodeList nodeList = view.getEngine().getDocument().getElementsByTagName("a");
								for (int i = 0; i < nodeList.getLength(); i++)
								{
									Node node = nodeList.item(i);
									EventTarget eventTarget = (EventTarget) node;
									eventTarget.addEventListener("click", new EventListener()
									{
										@Override
										public void handleEvent(Event evt)
										{
											try
											{
												EventTarget target = evt.getCurrentTarget();
												HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
												String href = anchorElement.getHref();		                   
												OperatingSystem.openLink(new URI(href));
											}
											catch (Exception ex)
											{
												ex.printStackTrace();
											}

											evt.preventDefault();
										}
									}, false);
								}
							}
						}
					});
				}

				view.getStylesheets().add(WebsiteTab.class.getResource("/theme.css").toExternalForm());
				jfxPanel.setScene(new Scene(view));
			}
		});
	}
}