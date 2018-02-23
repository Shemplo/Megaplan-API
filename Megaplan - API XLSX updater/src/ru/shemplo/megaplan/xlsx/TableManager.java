package ru.shemplo.megaplan.xlsx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ru.shemplo.exception.TableException;
import ru.shemplo.exception.UserProfileException;
import ru.shemplo.support.Pair;
import ru.shemplo.support.UserProfile;

public class TableManager implements AutoCloseable {

	private List <UserProfile> profiles;
	private XSSFWorkbook workbook;
	private OPCPackage pkg;
	
	public void loadTableFile (File file) throws TableException {
		if (file == null) {
			String message = "Path to input file is null";
			throw new TableException (message);
		}
		
		try { close (); } 
		catch (Exception e) {
			throw new TableException (e.getMessage (), e);
		}
		
		if (!file.exists () || !file.isFile ()) {
			String message = "Input file is not a table";
			throw new TableException (message);
		}
		
		try {
			this.pkg = OPCPackage.open (file);
			this.workbook = new XSSFWorkbook (pkg);
		} catch (InvalidFormatException ife) {
			throw new TableException (ife.getMessage (), ife);
		} catch (IOException ioe) {
			throw new TableException (ioe.getMessage (), ioe);
		}
	}
	
	public void parseFileByFormat (List <Pair <String, Function <String, String>>> columns) throws TableException {
		if (pkg == null || workbook == null) {
			String message = "Table file is not openned";
			throw new TableException (message);
		}
		
		if (columns == null || columns.size () == 0) {
			String message = "Table format must have at least one column";
			throw new TableException (message);
		}
		
		XSSFSheet sheet = workbook.getSheetAt (0);
		profiles.clear (); // Drop old profiles
		
		for (int row = 0; row < sheet.getLastRowNum (); row++) {
			Row selectedRow = sheet.getRow (row);
			if (selectedRow.getLastCellNum () < columns.size ()) { continue; }
			
			List <Pair <String, String>> values = new ArrayList <> ();
			for (int c = 0; c < selectedRow.getLastCellNum () && c < columns.size (); c++) {
				Cell cell = selectedRow.getCell (c); // Cell in a row
				String fieldName = columns.get (c).f;
				
				if (fieldName == null || fieldName.length () == 0) {
					continue; // Value of this column will be ignored
				}
				
				String value = "";
				switch (cell.getCellTypeEnum ()) {
					case NUMERIC:
						value = "" + cell.getNumericCellValue ();
						break;
						
					case STRING:
						value = cell.getStringCellValue ();
						break;
						
					case FORMULA:
						value = cell.getCellFormula ();
						break;
						
					default: break;
				}
				
				value = columns.get (c).s.apply (value);
				values.add (Pair.make (fieldName, value));
			}
			
			try {
				UserProfile profile = new UserProfile (row, values);
				profiles.add (profile); // Saving in a list of parsed
			} catch (UserProfileException upe) {
				throw new TableException (upe.getMessage (), upe);
			}
		}
	}
	
	@Override
	public void close () throws Exception {
		if (pkg != null) {
			pkg.close ();
		}
	}
	
}
