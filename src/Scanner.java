import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

public abstract class Scanner {

	
	protected int ANO_MIN = 2012; // cuidar que ANO_MIN sea mayor que 1960
	protected int ANO_MAX = 2012;
	protected int INDEX_PRECIO_MAX = 24;
	protected int INDEX_PRECIO_MIN = 0;
	protected int errorCounter = 0;
	protected Integer endThreadCounter = 0;
	protected volatile boolean runScan = true;
	protected MainWindow context;
	protected Email emailSender;

	protected ArrayList<String[]> data = new ArrayList<String[]>();
	/**
	 * --------------------------------------- | Brand | Model | Year | Price |
	 * Link | --------------------------------------- | 0 | 1 | 2 | 3 | 4 | | |
	 * | | | | | | | | | | | | | | | | ---------------------------------------
	 */

	protected ArrayList<String[]> statistics = new ArrayList<String[]>();

	/**
	 * Este arreglo se rellena en GlobalScanner y se actualiza cada vez que se
	 * mete un dato en LocalScanner.
	 * 
	 * -------------------------------------------------------------------------
	 * --- | Brand | Model | Year | Quantity |Min Price |Max Price |Mean Price|
	 * -------------------------------------------------------------------------
	 * --- | 0 | 1 | 2 | 3 | 4 | 5 | 6 | | | | | | | | | | | | | | | | | | | | |
	 * | | | |
	 * -------------------------------------------------------------------------
	 * ---
	 */

	public Scanner(MainWindow _context){
		this.context = _context;
		emailSender = new Email(_context);
	}
	
	public void saveLastScanDataToFile(){
		saveLastScanDataToFile(false);
	}
	
	public void saveLastScanDataToFile(boolean shouldStartOver) {
		Writer writer;
		java.util.TimeZone tz = java.util.TimeZone.getTimeZone(Constants.ZONA_HORARIA);
		java.util.Calendar c = java.util.Calendar.getInstance(tz);

		System.out.println(c.get(java.util.Calendar.HOUR_OF_DAY) + ":" + c.get(java.util.Calendar.MINUTE) + ":"
				+ c.get(java.util.Calendar.SECOND));

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("lastTimeData.txt"), "utf-8"));
			int mes = c.get(Calendar.MONTH) + 1;
			writer.write("<HORA>" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":"
					+ c.get(Calendar.SECOND) + "</HORA>");
			writer.write(System.getProperty("line.separator"));
			writer.write("<FECHA>" + c.get(Calendar.DAY_OF_MONTH) + "-" + mes + "-" + c.get(Calendar.YEAR) + "</FECHA>");
			writer.write(System.getProperty("line.separator"));
			writer.write("<ANO MIN>" + ANO_MIN + "</ANO MIN>");
			writer.write(System.getProperty("line.separator"));
			writer.write("<ANO MAX>" + ANO_MAX + "</ANO MAX>");
			writer.write(System.getProperty("line.separator"));
			writer.write("<INDEX PRECIO MIN>" + INDEX_PRECIO_MIN + "</INDEX PRECIO MIN>");
			writer.write(System.getProperty("line.separator"));
			writer.write("<INDEX PRECIO MAX>" + INDEX_PRECIO_MAX + "</INDEX PRECIO MAX>");
			writer.write(System.getProperty("line.separator"));
			writer.write("<REINICIAR>" + shouldStartOver + "</REINICIAR>");
			writer.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * getUrlSource recive una url en forma de string y retorna el codigo de
	 * fuente de esta url
	 * 
	 * @param _url
	 * @return
	 * @throws IOException
	 */
	public String getUrlSource(String _url) throws IOException {

		int maxTry = 3;
		while (true) {
			try {
				URL url = new URL(_url);
				URLConnection yc = url.openConnection();

				BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
				String inputLine;
				StringBuilder a = new StringBuilder();
				while ((inputLine = in.readLine()) != null)
					a.append(inputLine);
				in.close();
				context.setConnectionStatus(true);
				return a.toString();
			}

			catch (IOException e) {
				
				context.setConnectionStatus(false);
				
				maxTry--;
				if (maxTry <= 0) {
					if(!_url.equalsIgnoreCase("http://beatfan.site90.net/")){
						context.insertNewProgramCurrentState(
								"No se pudo extraer el codigo de fuente de la siguiente URL:" + _url, Color.RED, false);
						errorCounter++;
					}
					return "";
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public void setAnoMinimo(Integer ano) {
		ANO_MIN = ano;
	}

	public void setAnoMaximo(Integer ano) {
		ANO_MAX = ano;
	}

	public void setIndexPrecioMinimo(Integer precio) {
		INDEX_PRECIO_MIN = precio;
	}

	public void setIndexPrecioMaximo(Integer precio) {
		INDEX_PRECIO_MAX = precio;
	}

	public ArrayList<String[]> getData() {
		return data;
	}

	public ArrayList<String[]> getStatistics() {
		return statistics;
	}

	public void stopScan() {
		runScan = false;
	}
}
