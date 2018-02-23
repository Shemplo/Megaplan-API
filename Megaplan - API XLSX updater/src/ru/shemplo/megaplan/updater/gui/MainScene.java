package ru.shemplo.megaplan.updater.gui;

import static ru.shemplo.megaplan.updater.MegaplanAPIManager.TABLE_MANAGER;
import static ru.shemplo.megaplan.updater.MegaplanAPIManager.getStage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import ru.shemplo.exception.TableException;
import ru.shemplo.megaplan.updater.AsyncTaskExecutor;
import ru.shemplo.megaplan.updater.DateFormatter;
import ru.shemplo.megaplan.utils.XLSXUtils;
import ru.shemplo.support.Pair;

public class MainScene extends AbstractScene {

	@SuppressWarnings ("unused")
	private Button chooseFile, addColumn, parseFile, 
					loadProfiles;
	private Label logLoading, logParsing;
	private ScrollPane tableFormScroll;
	private VBox tableParser;
	private HBox tableFormat;
	private BorderPane root;
	
	@SuppressWarnings ("unchecked")
	@Override
	public void onSceneOpen () {
		this.root = (BorderPane) scene.lookup ("#main_root_pane");
		root.requestFocus ();
		
		this.logLoading = (Label) scene.lookup ("#log_of_file_loading");
		logLoading.applyCss ();
		
		this.chooseFile = (Button) scene.lookup ("#choose_file");
		chooseFile.applyCss ();
		chooseFile.setOnAction (ae -> {
			FileChooser chooser = new FileChooser ();
			chooser.setInitialDirectory (new File ("."));
			chooser.setTitle ("Load XLSX table file");
			chooser.setInitialFileName ("input.xlsx");
			chooser.getExtensionFilters ().add (
				new ExtensionFilter ("Excel 2008+", "*.xlsx")
			);
			
			File input = chooser.showOpenDialog (getStage ());
			changeState (chooseFile, false, logLoading, "Loading...");
			
			AsyncTaskExecutor.execute (() -> {
				boolean parserOK = false;
				String message = "";
				
				try {
					TABLE_MANAGER.loadTableFile (input);
					message = input.getName () + " - loaded";
					parserOK = true;
				} catch (TableException te) {
					message = "Error: " + te.getMessage ();
				} finally {
					boolean par = parserOK;
					String mes = message;
					
					Platform.runLater (() -> {
						changeState (chooseFile, true, 
									logLoading, mes);
						changeState (tableParser, par, 
										logParsing, "");
					});
				}
			});
		});
		
		this.tableParser = (VBox) scene.lookup ("#table_parser");
		tableParser.applyCss ();
		
		this.tableFormScroll = (ScrollPane) scene.lookup ("#table_scroll");
		tableFormScroll.setMinHeight (180);
		tableFormScroll.setMinWidth (544);
		tableFormScroll.setMaxWidth (550);
		tableFormScroll.applyCss ();
		
		this.tableFormat = (HBox) tableFormScroll.getContent ();
		tableFormat.applyCss ();
		makeFormatTable (2, -1);
		
		this.addColumn = (Button) scene.lookup ("#add_column");
		addColumn.applyCss ();
		addColumn.setOnAction (ae -> {
			int size = tableFormat.getChildren ().size ();
			makeFormatTable (size + 1, -1);
		});
		
		this.parseFile = (Button) scene.lookup ("#parse_file");
		parseFile.applyCss ();
		parseFile.setOnAction (ae -> {
			changeState (chooseFile, false, logLoading, 
							logLoading.getText ());
			changeState (tableParser, false, 
							logParsing, "Parsing...");
			
			AsyncTaskExecutor.execute (() -> {
				String message = "";
				
				List <Pair <String, Function <String, String>>> 
					columns = new ArrayList <> ();
				
				for (Node node : tableFormat.getChildren ()) {
					VBox column = (VBox) node;
					
					ChoiceBox <String> field 
						= (ChoiceBox <String>) column.getChildren ().get (2);
					String fieldName = field.getValue ();
					
					Function <String, String> formatter = a -> a;
					if (DateFormatter.isDateField (fieldName)) {
						ChoiceBox <String> dateFormat 
							= (ChoiceBox <String>) column.getChildren ().get (4);
						formatter = DateFormatter.getFormatter (dateFormat.getValue ()).F;
					}
					
					columns.add (Pair.make (fieldName, formatter));
				}
				
				try {
					TABLE_MANAGER.parseFileByFormat (columns);
					message = "Success: File parsed";
				} catch (TableException te) {
					message = "Error: " + te.getMessage ();
				} finally {
					String mes = message;
					Platform.runLater (() -> {
						changeState (chooseFile, true, logLoading, 
										logLoading.getText ());
						changeState (tableParser, true, 
										logParsing, mes);
					});
				}
			});
		});
		
		this.logParsing = (Label) scene.lookup ("#log_of_file_parsing");
		logParsing.applyCss ();
		
		root.autosize ();
		root.applyCss ();
		
		scene.getWindow ().setHeight (root.getHeight () + 32);
		scene.getWindow ().setWidth (root.getWidth () + 5);
	}
	
	@SuppressWarnings ("unchecked")
	private void makeFormatTable (int columns, int removed) {
		List <String> options = new ArrayList <> ();
		options.add ("ZaklyucheniyaDogovora");
		options.add ("FirstDate");
		options.add ("Some words here");
		options.add ("Category183CustomField");
		options.add ("Birthday");
		
		List <Node> previous = new ArrayList <> (tableFormat.getChildren ());
		tableFormat.getChildren ().clear ();
		
		for (int i = 0; i < columns; i ++) {
			final int index = i;
			
			VBox column = new VBox ();
			HBox.setMargin (column, new Insets (5));
			column.setAlignment (Pos.TOP_CENTER);
			column.setMaxWidth (125);
			column.setMinWidth (125);
			
			Label title = new Label (XLSXUtils.numToString (i));
			column.getChildren ().add (title);
			
			Label selectField = new Label ("Select field:");
			VBox.setMargin (selectField, new Insets (5, 0, 0, 0));
			column.getChildren ().add (selectField);
			
			ObservableList <String> list = FXCollections.observableArrayList (options);
			ChoiceBox <String> box = new ChoiceBox <> (list);
			box.setMaxWidth (120);
			box.setMinWidth (120);
			box.valueProperty ().addListener (cl -> {
				VBox col = (VBox) tableFormat.getChildren ().get (index);
				
				ChoiceBox <String> formats = (ChoiceBox <String>) col.getChildren ().get (4);
				formats.getItems ().clear ();
				
				if (DateFormatter.isDateField (box.getValue ())) {
					formats.setItems (DateFormatter.getList ());
					formats.setValue (formats.getItems ().get (0));
				}
			});
			column.getChildren ().add (box);
			
			Label selectFormat = new Label ("Select format:");
			VBox.setMargin (selectFormat, new Insets (10, 0, 0, 0));
			column.getChildren ().add (selectFormat);
			
			ChoiceBox <String> formatsBox = new ChoiceBox <> ();
			formatsBox.setMaxWidth (120);
			formatsBox.setMinWidth (120);
			column.getChildren ().add (formatsBox);
			
			Button remove = new Button ("Remove");
			VBox.setMargin (remove, new Insets (10, 0, 0, 0));
			column.getChildren ().add (remove);
			
			remove.setOnAction (ae -> {
				makeFormatTable (columns - 1, index);
			});
			
			tableFormat.getChildren ().add (column);
		}
		
		int offset = 0;
		for (int i = 0; i < columns && i < previous.size (); i ++) {
			if (i == removed) { offset = 1; }
			
			VBox node = (VBox) previous.get (i + offset);
			VBox newNode = (VBox) tableFormat.getChildren ().get (i);
			ChoiceBox <String> filed  = (ChoiceBox <String>) node.getChildren ().get (2);
			((ChoiceBox <String>) newNode.getChildren ().get (2)).setValue (filed.getValue ());
			
			ChoiceBox <String> format = (ChoiceBox <String>) node.getChildren ().get (4);
			((ChoiceBox <String>) newNode.getChildren ().get (4)).setValue (format.getValue ());
		}
	}
	
	private void changeState (Node node, boolean active, Label log, String message) {
		if (node != null) { node.setDisable (!active); }
		if (log != null) { log.setText (message); }
	}

}
