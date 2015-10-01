import java.io.File;
import java.io.IOException;
import java.util.Locale;
import jxl.Cell;
import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class FileManager {

	private static WritableCellFormat timesBold;
	private static WritableCellFormat times;

	public static String[][] read(String inputFile) throws IOException {
		String[][] data = null;
		File inputWorkbook = new File(inputFile);
		Workbook w;

		try {
			w = Workbook.getWorkbook(inputWorkbook);

			Sheet sheet = w.getSheet(0);
			data = new String[sheet.getColumns()][sheet.getRows()];
			for (int j = 0; j < sheet.getColumns(); j++) {
				for (int i = 0; i < sheet.getRows(); i++) {
					Cell cell = sheet.getCell(j, i);
					data[j][i] = cell.getContents();
				}
			}
		} catch (BiffException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static void writeStatistics(String inputFile, String[][] data) throws IOException, WriteException {
		File file = new File(inputFile);
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(new Locale("en", "EN"));

		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet("Estadisticas", 0);
		WritableSheet excelSheet = workbook.getSheet(0);
		createLabel(excelSheet, data);
		createContent(excelSheet, data);

		workbook.write();
		workbook.close();
	}

	public static void writeNewFounds(String inputFile, String[][] data) throws IOException, WriteException {
		File file = new File(inputFile);
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(new Locale("en", "EN"));

		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet("Ultimos", 1);
		WritableSheet excelSheet = workbook.getSheet(1);
		createContent(excelSheet, data);

		workbook.write();
		workbook.close();
	}

	private static void createLabel(WritableSheet sheet, String[][] data) throws WriteException {
		// Lets create a times font
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		// Define the cell format
		times = new WritableCellFormat(times10pt);
		// Lets automatically wrap the cells
		times.setWrap(true);

		// create create a bold font with unterlines
		WritableFont times10ptBold = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false);
		timesBold = new WritableCellFormat(times10ptBold);
		// Lets automatically wrap the cells
		timesBold.setWrap(true);

		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBold);
		cv.setAutosize(true);

		// Write a few headers
		addCaption(sheet, 0, 0, "Marca");
		addCaption(sheet, 1, 0, "Modelo");
		addCaption(sheet, 2, 0, "Año");
		addCaption(sheet, 3, 0, "Cantidad");
		addCaption(sheet, 4, 0, "Precio Min");
		addCaption(sheet, 5, 0, "Precio Max");
		addCaption(sheet, 6, 0, "Precio Prom");

	}

	private static void createContent(WritableSheet sheet, String[][] data)
			throws WriteException, RowsExceededException {
		/*
		 * // Write a few number for (int i = 1; i < 10; i++) { // First column
		 * addNumber(sheet, 0, i, i + 10); // Second column addNumber(sheet, 1,
		 * i, i * i); }
		 */

		/*
		 * // Lets calculate the sum of it StringBuffer buf = new
		 * StringBuffer(); buf.append("SUM(A2:A10)"); Formula f = new Formula(0,
		 * 10, buf.toString()); sheet.addCell(f); buf = new StringBuffer();
		 * buf.append("SUM(B2:B10)"); f = new Formula(1, 10, buf.toString());
		 * sheet.addCell(f);
		 */

		// now a bit of text
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				addLabel(sheet, j, i + 1, data[i][j]);
			}
		}
	}

	private static void addCaption(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, timesBold);
		sheet.addCell(label);
	}

	private static void addNumber(WritableSheet sheet, int column, int row, Integer integer)
			throws WriteException, RowsExceededException {
		Number number;
		number = new Number(column, row, integer, times);
		sheet.addCell(number);
	}

	private static void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, times);
		sheet.addCell(label);
	}

}
