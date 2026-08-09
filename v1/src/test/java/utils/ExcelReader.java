package utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

public class ExcelReader {

    private static final String ENTRY_FILE = System.getProperty("user.dir") + "/testdata/inputs/entry_data.xlsx";

    private final LoggerHandler logging = new LoggerHandler();

    // ── DataProviders ─────────────────────────────────────────────────────────

    /** One row of entry_data.xlsx per invocation, as {number, text, password, date}.
     *  Reference from a test via @Test(dataProvider = "webInputs", dataProviderClass = ExcelReader.class). */
    @DataProvider(name = "webInputs")
    public static Object[][] webInputs() throws IOException {
        return new ExcelReader().readAsDataProvider(ENTRY_FILE, 4);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /** Reads a single cell from the first sheet of the given Excel file.
     *  rowNo and colNo are zero-based integers (row 0 = header, col 0 = column A).
     *  Returns the value as a String — numbers without decimals, dates as yyyy-MM-dd. */
    public String input(String fileName, int rowNo, int colNo, ExtentTest test) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        XSSFRow row = sheet.getRow(rowNo);
        String value = (row == null) ? "" : getCellValue(row.getCell(colNo));

        workbook.close();
        fis.close();

        logging.info("Read [" + value + "] from " + fileName + " (row " + rowNo + ", col " + colNo + ")");
        if (test != null) test.log(Status.INFO, "Read [" + value + "] from row " + rowNo + ", col " + colNo);
        return value;
    }

    /** Same as input(String, int, int, ExtentTest) but accepts rowNo and colNo as Strings.
     *  Useful when reading values passed in from a data table or external config.
     *  Internally converts the Strings to ints before delegating. */
    public String input(String fileName, String rowNo, String colNo, ExtentTest test) throws IOException {
        return input(fileName, Integer.parseInt(rowNo.trim()), Integer.parseInt(colNo.trim()), test);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Writes a single String value into the specified cell of the first sheet.
     *  Creates the row and cell if they don't exist yet; opens, updates and saves
     *  the file in one call so every write is immediately flushed to disk. */
    public void output(String fileName, int rowNo, int colNo, String value, ExtentTest test) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        fis.close();

        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFRow row = sheet.getRow(rowNo);
        if (row == null) row = sheet.createRow(rowNo);
        XSSFCell cell = row.getCell(colNo);
        if (cell == null) cell = row.createCell(colNo);
        cell.setCellValue(value == null ? "" : value);

        FileOutputStream fos = new FileOutputStream(fileName);
        workbook.write(fos);
        fos.close();
        workbook.close();

        logging.info("Wrote [" + value + "] to " + fileName + " (row " + rowNo + ", col " + colNo + ")");
        if (test != null) test.log(Status.INFO, "Wrote [" + value + "] to row " + rowNo + ", col " + colNo);
    }

    /** Same as output(String, int, int, String, ExtentTest) but accepts rowNo and colNo as Strings.
     *  Useful when the row/column coordinates come from a data-driven source.
     *  Internally converts the Strings to ints before delegating. */
    public void output(String fileName, String rowNo, String colNo, String value, ExtentTest test) throws IOException {
        output(fileName, Integer.parseInt(rowNo.trim()), Integer.parseInt(colNo.trim()), value, test);
    }

    // ── Clear ─────────────────────────────────────────────────────────────────

    /** Deletes every data row from the first sheet while keeping the header row (row 0) intact.
     *  Call this at the start of a test run to ensure output files don't carry stale data
     *  from a previous execution. Iterates in reverse to avoid index-shift issues in POI. */
    public void clearData(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        fis.close();

        XSSFSheet sheet = workbook.getSheetAt(0);
        for (int i = sheet.getLastRowNum(); i >= 1; i--) {
            XSSFRow row = sheet.getRow(i);
            if (row != null) sheet.removeRow(row);
        }

        FileOutputStream fos = new FileOutputStream(fileName);
        workbook.write(fos);
        fos.close();
        workbook.close();

        logging.info("Cleared data rows in " + fileName);
    }

    // ── Bulk read ─────────────────────────────────────────────────────────────

    /** Reads all data rows (skipping the header at row 0) from the first sheet.
     *  Each row is returned as a String[] of length columnCount; blanks become "".
     *  Numbers lose trailing decimals, Excel date cells are formatted as yyyy-MM-dd. */
    public List<String[]> readAllRows(String fileName, int columnCount) throws IOException {
        List<String[]> rows = new ArrayList<>();
        FileInputStream fis = new FileInputStream(fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) continue;
            String[] values = new String[columnCount];
            for (int c = 0; c < columnCount; c++) {
                values[c] = getCellValue(row.getCell(c));
            }
            rows.add(values);
        }

        workbook.close();
        fis.close();
        return rows;
    }

    /** Reads all data rows and returns them as Object[][], the exact shape a TestNG
     *  @DataProvider must return. Thin wrapper over readAllRows so any data-driven test
     *  can back a provider with one call. Each element is a String[] of length columnCount. */
    public Object[][] readAsDataProvider(String fileName, int columnCount) throws IOException {
        List<String[]> rows = readAllRows(fileName, columnCount);
        Object[][] data = new Object[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            data[i] = rows.get(i);
        }
        return data;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    /** Converts any POI cell to a clean String regardless of cell type.
     *  Numeric cells are cast to long (drops the .0), date cells use yyyy-MM-dd,
     *  booleans become "true"/"false", and null/blank cells return "". */
    private String getCellValue(XSSFCell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return cell.getStringCellValue().trim();
        }
    }
}
