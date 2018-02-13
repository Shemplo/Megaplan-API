package ru.shemplo.megaplan.xlsx;

import static ru.shemplo.megaplan.network.APIConnection.sendRequest;
import static ru.shemplo.megaplan.updater.ConsoleAdapter.getFormattedAnswer;
import static ru.shemplo.megaplan.updater.ConsoleAdapter.getVariantedAnswer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import ru.shemplo.exception.ConsoleException;
import ru.shemplo.exception.RequestException;
import ru.shemplo.exception.UserProfileException;
import ru.shemplo.exception.WorkbookException;
import ru.shemplo.megaplan.network.APIFormatter;
import ru.shemplo.megaplan.network.RequestAction;
import ru.shemplo.support.Pair;
import ru.shemplo.support.UserProfile;

public class XLSXManager implements AutoCloseable {

	public static final String INPUT_FILE_NAME = "input.xlsx";
	
	private Map <String, UserProfile> clients;
	private XSSFWorkbook workbook;
	private OPCPackage pkg;
	private File bookFile;
	
	public void loadWorkbook (String path, boolean read) throws WorkbookException {
		Objects.requireNonNull (path, "Given path to workbook is null");
		
		if (pkg != null) { // Some book is already opened
			String message = "Book " + bookFile.getName () + " is opened";
			throw new WorkbookException (message);
		}
		
		openWorkbook (path, read);
	}
	
	public void loadWorkbook (String path) throws WorkbookException {
		loadWorkbook (path, false);
	}
	
	public void readWorkbook () throws WorkbookException {
		if (clients != null) { return; } // Already read
		this.clients = new HashMap <> ();
		
		String sheetName = null;
		try {
			sheetName = System.getProperty ("megaplan.xlsx.sheet");
		} catch (SecurityException se) {
			String message = "(Security) failed get sheet name";
			throw new WorkbookException (message, se);
		} catch (IllegalArgumentException iae) {
			String message = "(Argument) property with sheet isn't declared";
			throw new WorkbookException (message, iae);
		}
		
		XSSFSheet sheet = workbook.getSheet (sheetName);
		List <Integer> fields = null; int attempts = 2;
		
		while (attempts > 0 && fields == null) {
			try {
				fields = initParseFormat ();
				if (fields == null) {
					attempts ++; // It request for list
				}
			} catch (WorkbookException we) {
				Throwable cause = we.getCause ();
				System.err.println (cause != null ? cause : we);
			}
			
			attempts--;
		}
		
		if (fields == null) {
			String message = "(Attempts) attempts expired";
			throw new WorkbookException (message);
		}
		
		for (int i = 0; i <= sheet.getLastRowNum (); i ++) {
			Row row = sheet.getRow (i); Cell fioCell = row.getCell (1);
			if (fioCell.getCellTypeEnum ().equals (CellType.STRING)
					&& (fioCell.getStringCellValue () != null 
					&& fioCell.getStringCellValue ().length () > 0)) {
				try {
					List <Pair <String, String>> properties = new ArrayList <> ();
					for (int j = 0; j < fields.size (); j ++) {
						if (fields.get (j) < 0) { continue; }
						
						Cell cell = row.getCell (j);
						if (cell == null) { break; }
						
						String column = FIELDS.get (fields.get (j)).f;
						String value = ""; // Default empty value
						
						if (cell.getCellTypeEnum ().equals (CellType.STRING)) {
							value = cell.getStringCellValue ();
						} else if (cell.getCellTypeEnum ().equals (CellType.NUMERIC)) {
							value = "" + cell.getNumericCellValue ();
						} else if (cell.getCellTypeEnum ().equals (CellType.FORMULA)) {
							value = cell.getCellFormula ();
						}
						
						if (value.equals ("")) { continue; } // Empty value -> ignore it
						
						value = APIFormatter.format (column, value);
						properties.add (Pair.make (column, value));
					}
					
					@SuppressWarnings ("unchecked")
					Pair <String, String> [] propsArray = new Pair [properties.size ()];
					properties.toArray (propsArray);
					
					UserProfile profile = new UserProfile (i, propsArray);
					clients.put (profile.getValue ("Name"), profile); // Save in local storage
				} catch (UserProfileException upe) {
					System.err.println ("Problem with " + i + "line in workbook");
				}
			}
		}
		
		System.out.println (clients.size () + " clients loaded");
	}
	
	private static List <Integer> initParseFormat () throws WorkbookException {
		List <Integer> fields = new ArrayList <> ();
		fields.add (-1);
		fields.add (0);
		
		String regexp = "^(list|(\\s*\\d+)+)$";
		String question = "Select table format (firts two columns are selected)";
		try {
			loadFieldsList ();
			
			String stringList = getFormattedAnswer (question, 3, regexp);
			if (stringList.equals ("list")) {
				printFieldsList ();
				return null;
			}
			
			StringTokenizer st = new StringTokenizer (stringList);
			List <String> tokens = new ArrayList <> ();
			while (st.hasMoreTokens ()) { tokens.add (st.nextToken ()); }
			
			for (String token : tokens) {
				int field = Integer.parseInt (token) - 1;
				if (field < 0 || field >= FIELDS.size ()) {
					String message = "(Format) number of field is out of range";
					throw new WorkbookException (message);
				}
				
				fields.add (field);
			}
		} catch (ConsoleException ce) {
			String message = "(Format) failed to set up table format";
			throw new WorkbookException (message, ce);
		} catch (RequestException | NullPointerException e) {
			String message = "(API) failed to load fields list";
			throw new WorkbookException (message, e);
		}
		
		StringBuilder format = new StringBuilder ();
		
		for (int field : fields) {
			String name = "Anything";
			if (field >= 0) {
				name = FIELDS.get (field).f;
			}
			
			format.append ("%-");
			format.append (name.length () + 1);
			format.append ("s ");
		}
		
		Object [] titles = new String [fields.size ()];
		Object [] names  = new String [fields.size ()];
		for (int i = 0; i < titles.length; i ++) {
			String name = "Anything";
			if (fields.get (i) >= 0) {
				name = FIELDS.get (fields.get (i)).f;
			}
			
			titles [i] = XLSXUtils.numToString (i);
			names [i] = name;
		}
		
		System.out.println (String.format (format.toString (), titles));
		System.out.println (String.format (format.toString (), names));
		try {
			String answer = getVariantedAnswer ("Is it right format of table", 
												2, new String [] {"yes", "no"});
			if ("no".equals (answer)) { return null; } // Try do this again
		} catch (ConsoleException ce) {
			String message = "(Console) failed read console input";
			throw new WorkbookException (message, ce);
		}
		
		return fields;
	}
	
	private static final List <Pair <String, String>> FIELDS;
	private static int MAX_TRANSLATION_WIDTH;
	
	static {
		FIELDS = new ArrayList <> ();
		MAX_TRANSLATION_WIDTH = 0;
	}
	
	private static void printFieldsList () {
		String format = "%3d. %-" + (MAX_TRANSLATION_WIDTH + 1) + "s %s";
		for (int i = 0; i < FIELDS.size (); i ++) {
			Pair <String, String> pair = FIELDS.get (i);
			System.out.println (String.format (format, i + 1, 
												pair.s, pair.f));
		}
	}
	
	private static void loadFieldsList () throws RequestException, NullPointerException {
		try {
			JSONObject answer = sendRequest (RequestAction.CLIENT_FIELDS_LIST, null);
			JSONArray fields = answer.getJSONObject ("data")
										.getJSONArray ("Fields");
			MAX_TRANSLATION_WIDTH = 0;
			FIELDS.clear ();
			
			for (int i = 0; i < fields.length (); i ++) {
				JSONObject field = fields.getJSONObject (i);
				String translation = field.getString ("Translation");
				FIELDS.add (Pair.make (field.getString ("Name"), translation));
				MAX_TRANSLATION_WIDTH = Math.max (MAX_TRANSLATION_WIDTH, 
													translation.length ());
			}
		} finally {}
	}
	
	public List <Pair <UserProfile, List <String>>> 
		findUpdatedProfiles (List <UserProfile> profiles) throws WorkbookException {
		
		if (profiles == null) {
			String message = "(NPE) given profiles list is null";
			throw new WorkbookException (message);
		}
		
		List <Pair <UserProfile, List <String>>> updates = new ArrayList <> ();
		for (UserProfile remoteClient : profiles) {
			try {
				String fio = remoteClient.getValue ("Name");
				if (!clients.containsKey (fio)) { continue; }
				
				UserProfile local = clients.get (fio);
				List <String> keys = local.findDifferences (remoteClient);
				keys = keys.stream ().distinct ()
										.collect (Collectors.toList ());
				if (keys.size () == 0) { continue; } // No differences
				
				for (String key : keys) {
					remoteClient.setNewValue (key, local.getValue (key));
				}
				
				if (remoteClient.getValue ("FirstName") == null) {
					String name = remoteClient.getValue ("Name");
					
					remoteClient.setNewValue ("FirstName", name.split (" ") [1]);
				}
				
				updates.add (Pair.make (remoteClient, keys));
			} catch (UserProfileException upe) {
				// To know that this profile has problems
				updates.add (Pair.make (remoteClient, null));
			}
		}
		
		return updates;
	}
	
	private void openWorkbook (String path, boolean read) throws WorkbookException {
		URL rootPath = XLSXManager.class.getProtectionDomain ()
											.getCodeSource ()
											.getLocation ();
		File root = new File (".");
		try {
			root = new File (rootPath.toURI ()).getParentFile ();
		} catch (URISyntaxException e) {}
		
		File workbookFile = new File (root, path);
		System.out.println ("Loaded file: " + workbookFile);
		
		if (!workbookFile.exists () || !workbookFile.isFile ()) {
			String message = "Workbook file not found";
			throw new WorkbookException (message);
		}
		
		try {
			OPCPackage pack = OPCPackage.open (workbookFile);
			this.workbook = new XSSFWorkbook (pack); // .xlsx
			this.bookFile = workbookFile;
			this.clients = null;
			this.pkg = pack;
			
			// Done
		} catch (IOException ioe) {
			String message = "(I/O) Failed to read from workbook";
			throw new WorkbookException (message, ioe);
		} catch (InvalidFormatException ife) {
			String message = "(Format) Wrong format of file";
			throw new WorkbookException (message, ife);
		}
		
		if (read) { readWorkbook (); }
	}
	
	@Override
	public void close () throws Exception {
		if (pkg != null) { pkg.close (); }
	}
	
	public static void writeToNewFile (List <List <Pair <String, String>>> values) {
		List <String> fields = new ArrayList <> ();
		fields.add ("Name");
		
		if (values == null) { return; }
		for (int i = 0; i < values.size (); i ++) {
			List <Pair <String, String>> line = values.get (i);
			for (int j = 0; j < line.size (); j ++) {
				if (fields.indexOf (line.get (j).f) == -1) {
					fields.add (line.get (j).f);
				}
			}
		}
		
		File errorFile = new File ("error.xlsx");
		if (errorFile.exists ()) {
			errorFile.delete ();
		}
		
		try (
			XSSFWorkbook workbook = new XSSFWorkbook ();
			OutputStream os = new FileOutputStream (errorFile);
		) {
			String shName = System.getProperty ("megaplan.xlsx.sheet");
			XSSFSheet sheet = workbook.createSheet (shName);
			
			for (int i = 0; i < values.size (); i ++) {
				List <Pair <String, String>> line = values.get (i);
				Row row = sheet.createRow (i);
				
				for (int j = 0; j < line.size (); j ++) {
					Pair <String, String> value = line.get (j);
					int columnNumber = 1 + fields.indexOf (value.f);
					Cell cell = row.createCell (columnNumber);
					cell.setCellValue (value.s);
				}
			}
			
			workbook.write (os);
		} catch (IOException ioe) {
			ioe.printStackTrace ();
		}
	}
	
}
