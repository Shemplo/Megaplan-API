package ru.shemplo.megaplan.updater;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ru.shemplo.exception.GUIException;
import ru.shemplo.megaplan.network.MegaplanConnection;
import ru.shemplo.megaplan.updater.gui.AppScene;
import ru.shemplo.megaplan.xlsx.TableManager;

public class MegaplanAPIManager extends Application {
	
	public static final String FIELDS_PREFIX = "Category183CustomField";
	public static final String SERVER_PROTOCOL = "https://";
	public static final String MEGAPLAN_HOST = "megaplan.ru";
	
	public static void main (String... args) {
		System.setProperty ("megaplan.xlsx.sheet", "TDSheet");
		
		launch (args);
		
		/*
		System.out.println ("Authorizing...");
		
		try {
			APIConnection.authorize ();
			if (!APIConnection.isAuthorized ()) {
				stopExecution (1, "Not authorized");
			}
		} catch (AuthorizationException e) {
			stopExecution (1, e.toString ());
		}
		
		System.out.println ("Client authorized");
		System.out.println ("Downloading remote clients...");
		
		List <UserProfile> remoteProfiles = new ArrayList <> ();
		try {
			List <Pair <String, ?>> params = new ArrayList <> ();
			int offset = 0;
			
			while (true) {
				params.clear ();
				params.add (Pair.make ("Offset", "" + offset));
				params.add (Pair.make ("Limit", "1000"));
				
				JSONObject answer = APIConnection.sendRequest (RequestAction.CLIENTS_LIST, params);
				JSONObject data = answer.getJSONObject ("data");
				
				JSONArray clients = data.getJSONArray ("clients");
				for (int i = 0; i < clients.length (); i ++) {
					JSONObject object = clients.getJSONObject (i);
					remoteProfiles.add (UserProfile.makeFromJSON (object));
				}
				
				offset += 1000;
				if (clients.length () == 0) {
					break;
				}
			}
			
			System.out.println (remoteProfiles.size () + " remote clients loaded");
		} catch (UserProfileException upe) {
			stopExecution (1, upe.toString ());
		} catch (RequestException | NullPointerException e) {
			stopExecution (1, e.toString ());
		}
		
		System.out.println ("Loading `xlsx` file...");
		List <Pair <UserProfile, List <String>>> differences = null;
		
		try (
			XLSXManager xlsx = new XLSXManager ();
		) {
			xlsx.loadWorkbook (XLSXManager.INPUT_FILE_NAME, true);
			System.out.println ("`xlsx` file loaded");
			
			differences = xlsx.findUpdatedProfiles (remoteProfiles);
			System.out.println (differences.size () + " differences found:");
			if (differences.size () == 0) {
				System.out.println ("Stopping application...");
				stopExecution (0);
			}
			
			for (int i = 0; i < differences.size (); i ++) {
				Pair <UserProfile, List <String>> pair = differences.get (i);
				System.out.println (pair.f.getValue ("Name") + " " + pair.s);
			}
		} catch (UserProfileException upe) {
			stopExecution (1, upe.toString ());
		} catch (WorkbookException we) {
			stopExecution (1, we.toString ());
		} catch (Exception e) {
			stopExecution (1, e.toString ());
		}
		
		try {
			String [] anses = {"yes", "no"};
			String sendAnswer = ConsoleAdapter.getVariantedAnswer ("Send updates", 2, anses);
			if ("no".equals (sendAnswer)) {
				System.out.println ("Sending canceled");
				stopExecution (0);
			}
		} catch (ConsoleException ce) {
			stopExecution (1, ce.toString ());
		}
		
		System.out.println ("Sending updated to server...");
		int successfulSendings = 0;
		
		List <Integer> unsuccess = new ArrayList <> ();
		int index = 0;
		
		for (Pair <UserProfile, List <String>> pair : differences) {
			try {
				List <Pair <String, ?>> params = new ArrayList <> ();
				params.add (Pair.make ("Id", pair.f.ID));
				
				for (String key : pair.s) {
					params.add (Pair.make ("Model[" + key + "]", pair.f.getValue (key)));
				}
				
				if (pair.s.indexOf ("FirstName") == -1) {
					params.add (Pair.make ("Model[FirstName]", pair.f.getValue ("FirstName")));
				}
				
				System.out.println ("Sending: " + params);
				JSONObject answer = APIConnection.sendRequest (RequestAction.EDIT_CLIENT_DATA, params);
				JSONObject status = answer.getJSONObject ("status");
				String code = status.getString ("code");
				
				if (!code.equals ("ok")) {
					System.out.println (status.getString ("message"));
					unsuccess.add (index);
				}
				
				index ++;
			} catch (RequestException | NullPointerException e) {
				System.err.println (e.toString ());
			} catch (UserProfileException upe) {
				System.err.println (upe.toString ());
			}
		}
		
		System.out.println ("Sent updated: " + differences.size () 
							+ " (successful: " + successfulSendings + ")");
		if (successfulSendings != differences.size ()) {
			System.out.println ("Saving failed to the file... ");
			List <List <Pair <String, String>>> values = new ArrayList <> ();
			
			for (int unsIndex : unsuccess) {
				UserProfile profile = differences.get (unsIndex).f;
				List <String> fields = differences.get (unsIndex).s;
				
				try {
					List <Pair <String, String>> line = new ArrayList <> ();
					line.add (Pair.make ("Name", profile.getValue ("Name")));
					
					for (String field : fields) {
						String value = profile.getValue (field);
						line.add (Pair.make (field, value));
					}
					
					values.add (line);
				} catch (UserProfileException upe) {
					System.out.println (upe.toString ());
				}
			}
			
			XLSXManager.writeToNewFile (values);
		}
		
		System.out.println ("Done");
		*/
	}
	
	public static final String SCENES_LOCATION = "ru/shemplo/megaplan/updater/gui/schema";
	public static final String TITLE = "Megaplan API updater";
	
	public static final MegaplanConnection CONNECTION;
	public static final TableManager TABLE_MANAGER;
	
	private static final Map <AppScene, Parent> SCENES;
	private static Scene STAGE_SCENE = null;
	private static Stage STAGE = null;
	
	static {
		CONNECTION = MegaplanConnection.getInstance ();
		TABLE_MANAGER = new TableManager ();
		SCENES = new HashMap <> ();
	}
	
	public static Scene getStageScene () {
		return STAGE_SCENE;
	}
	
	public static Stage getStage () {
		return STAGE;
	}
	
	@Override
	public void start (Stage stage) throws Exception {
		STAGE_SCENE = new Scene (new Pane (), 100, 50);
		switchScenes (AppScene.LOADING_SCENE);
		
		stage.setScene (STAGE_SCENE);
		stage.setAlwaysOnTop (true);
		stage.setFullScreen (false);
		stage.setResizable (false);
		stage.setTitle (TITLE);
		stage.show ();
		
		stage.setOnCloseRequest (we -> {
			AsyncTaskExecutor.stop ();
		});
		
		switchScenes (AppScene.LOGIN_SCENE);
		STAGE = stage;
	}
	
	public static void switchScenes (AppScene scene) {
		if (!SCENES.containsKey (scene)) {
			try {
				Parent tmp = loadSceneScheme (scene.FILE);
				SCENES.put (scene, tmp);
			} catch (GUIException guie) {
				System.err.println (guie.getMessage ());
				return;
			}
		}
		
		Parent parent = SCENES.get (scene);
		Platform.runLater (() -> {
			STAGE_SCENE.setRoot (parent);
			if (scene.INSTANCE != null) {
				scene.INSTANCE.onSceneOpen ();
			}
		});
	}
	
	private static Parent loadSceneScheme (String sceneFile) throws GUIException {
		Parent parent = null;
		
		try {
			String path = SCENES_LOCATION + "/" + sceneFile;
			URL resource = ClassLoader.getSystemResource (path);
			parent = FXMLLoader.load (resource);
		} catch (IOException | NullPointerException ioe) {
			String message = "(GUI) Failed to load scene file";
			throw new GUIException (message, ioe);
		}
		
		return parent;
	}
	
	public static void stopExecution (int code, String... message) {
		if (message != null && message.length > 0 && code != 0) {
			System.err.println (message [0]);
		}
		
		System.exit (code);
	}
	
}
