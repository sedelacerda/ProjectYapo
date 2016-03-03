import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Herramientas {
	
	public static String[] marcas; // Este arreglo ayuda a convertir de index
									// imaginario a real (por ej: para
									// particionar BMIndexs)

	public static String toPrice(String precio) {
		precio = precio.replace(".", "");
		Integer precioint = Integer.parseInt(precio);
		String out = precio;
		NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.GERMANY);
		out = numberFormatter.format(precioint);
		return out;
	}

	public static ArrayList<ArrayList<String>> filterBrands(ArrayList<ArrayList<String>> paresModeloMarca) {

		/**
		 * A continuacion se listan los indexs de las marcas que si se quieren
		 * tomar en cuenta 2 ACURA 3 ALFA ROMEO 8 AUDI 9 AUSTIN 12 BMW 96
		 * BRILLIANCE 13 BUICK 14 BYD 17 CHERY 18 CHEVROLET 19 CHRYSLER 20
		 * CITROEN 22 DACIA 23 DAEWOO 24 DAIHATSU 25 DATSUN 26 DODGE 30 FIAT 31
		 * FORD 34 GEELY 35 GREAT WALL 37 HAIMA 39 HONDA 40 HYUNDAI 97 INFINITI
		 * 42 ISUZU 43 JAC 44 JAGUAR 45 JEEP 47 KIA MOTORS 48 LADA 49 LANCIA 50
		 * LAND ROVER 51 LEXUS 52 LIFAN 55 MAHINDRA 56 MASERATI 57 MAZDA 58
		 * MERCEDES BENZ 60 MG 61 MINI 62 MITSUBISHI 99 MORGAN 64 NISSAN 66
		 * OLDSMOBILE 67 OPEL 68 PEUGEOT 71 PONTIAC 72 PORSCHE 75 RENAULT 76
		 * ROVER 77 SAAB 79 SAMSUNG 80 SEAT 82 SKODA 84 SSANGYONG 85 SUBARU 86
		 * SUZUKI 87 TATA 88 TOYOTA 89 VOLKSWAGEN 90 VOLVO
		 */

		marcas = new String[] { "2", "3", "8", "9", "12", "96", "13", "14", "17", "18", "19", "20", "22", "23", "24",
				"25", "26", "30", "31", "34", "35", "37", "39", "40", "97", "42", "43", "44", "45", "47", "48", "49",
				"50", "51", "52", "55", "56", "57", "58", "60", "61", "62", "99", "64", "66", "67", "68", "71", "72",
				"75", "76", "77", "79", "80", "82", "84", "85", "86", "87", "88", "89", "90" };

		ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();

		// Copiamos solo las marcas que deseamos al arraylist out
		for (String index : marcas) {
			for (ArrayList<String> par : paresModeloMarca) {
				if (index.equals(par.get(0))){
					out.add(par);
				}
			}
		}

		return out;
	}

	/**
	 * Metodo que convierte de indice imaginario de marca a indice real de la
	 * pagina Yapo.
	 * 
	 * @param imaginaryIndex
	 * @return
	 */
	public static String getRealBrandIndex(int imaginaryIndex) {

		if (0 < imaginaryIndex && imaginaryIndex < marcas.length)
			return marcas[imaginaryIndex];
		else
			return "0";
	}

	public static ArrayList<int[]> getBMIndexsPartitions() {
		ArrayList<int[]> out = new ArrayList<int[]>();

		int partLength = marcas.length / Constants.CANT_SCAN_THREADS;

		for (int i = 0; i < Constants.CANT_SCAN_THREADS; i++) {
			if (i != Constants.CANT_SCAN_THREADS - 1) {
				out.add(new int[] { i * partLength, i * partLength + partLength });
			} else {
				out.add(new int[] { i * partLength, marcas.length }); // la
																		// ultima
																		// particion
																		// se
																		// lleva
																		// los
																		// sobrantes
			}
		}

		return out;
	}
	
	public static void restartApplication(Scanner scanner)
	{
	  final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
	  File currentJar;
	
	  try {		
		  currentJar = new File(MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		  /* is it a jar file? */
		  if(!currentJar.getName().endsWith(".jar"))
			  return;
		  
		  
		  /* Build command: java -jar application.jar */
		  final ArrayList<String> command = new ArrayList<String>();
		  command.add(javaBin);
		  command.add("-jar");
		  command.add(currentJar.getPath());
		  
		  scanner.saveLastScanDataToFile(true);
		  
		  final ProcessBuilder builder = new ProcessBuilder(command);
		  builder.start();
		  System.exit(0);
		  System.out.println("No se pudo cerrar");
		  } catch (URISyntaxException e) {			
			  e.printStackTrace();			  
		  } catch (IOException e) {
			  e.printStackTrace();
		}
	}
	
	/** getLastTimeScanHour revisa el archivo lastTimeScanData y retorna un string
	 * de forma HH/MM que representa la hora de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanTime(){
				
		String fileTime = "";
		
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:ProjectYapo.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery( "SELECT hora FROM UltimaBusqueda;" );
			while ( rs.next() ) {
				fileTime = rs.getString("hora");
			}
			
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.out.print("No se pudo encontrar la fecha en la base de datos");
		}
		
		return fileTime;
		
	}
	
	/** getLastTimeScanDate revisa el archivo lastTimeScanData y retorna un string
	 * de forma DD/MM/YYYY que representa la fecha de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanDate(){
		
		String fileDate = "";
		
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:ProjectYapo.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery( "SELECT fecha FROM UltimaBusqueda;" );
			while ( rs.next() ) {
				fileDate = rs.getString("fecha");
			}
			
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.out.print("No se pudo encontrar la fecha en la base de datos");
		}
		
		return fileDate;
	}
	
	/** getLastTimeScanMinYear revisa el archivo lastTimeScanData y retorna un string
	 * que representa el año minimo de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanMinYear(){
		
		String fileMinYear = "";
		
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:ProjectYapo.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery( "SELECT anoMin FROM UltimaBusqueda;" );
			while ( rs.next() ) {
				fileMinYear = rs.getString("anoMin");
			}
			
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.out.print("No se pudo encontrar el año mínimo en la base de datos");
		}
		
		return fileMinYear;
	}
	
	/** getLastTimeScanMaxYear revisa el archivo lastTimeScanData y retorna un string
	 * que representa el año maximo de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanMaxYear(){
		
		String fileMaxYear = "";
		
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:ProjectYapo.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery( "SELECT anoMax FROM UltimaBusqueda;" );
			while ( rs.next() ) {
				fileMaxYear = rs.getString("anoMax");
			}
			
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.out.print("No se pudo encontrar el año máximo en la base de datos");
		}
		
		return fileMaxYear;
	}
	
	/** getLastTimeScanMinPriceIndex revisa el archivo lastTimeScanData y retorna un string
	 * que representa el indice del precio minimo de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanMinPriceIndex(){
		
		String fileMinPriceIndex = "";
		
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:ProjectYapo.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery( "SELECT indexPrecioMin FROM UltimaBusqueda;" );
			while ( rs.next() ) {
				fileMinPriceIndex = rs.getString("indexPrecioMin");
			}
			
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.out.print("No se pudo encontrar el índice de precio mínimo en la base de datos");
		}
		
		return fileMinPriceIndex;
	}
	
	/** getLastTimeScanMaxPriceIndex revisa el archivo lastTimeScanData y retorna un string
	 * que representa el indice del precio maximo de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanMaxPriceIndex(){
		
		String fileMaxPriceIndex = "";
		
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:ProjectYapo.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery( "SELECT indexPrecioMax FROM UltimaBusqueda;" );
			while ( rs.next() ) {
				fileMaxPriceIndex = rs.getString("indexPrecioMax");
			}
			
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.out.print("No se pudo encontrar el índice de precio máximo en la base de datos");
		}
		
		return fileMaxPriceIndex;
	}
	
	/** getLastTimeScanShouldRestart revisa el archivo lastTimeScanData y retorna un boolean
	 * que si es true significa que debe iniciarse automaticamente el scan, si es false
	 * significa que es primer scan desde que se abrio el programa haciendo doble click y
	 * que se le debe esperar al usuario [ara que inicie el scan.
	 * @return
	 */
	public static boolean getLastTimeScanShouldRestart(){
				
		Integer fileRestart = 0;
		
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:ProjectYapo.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery( "SELECT reiniciar FROM UltimaBusqueda;" );
			while ( rs.next() ) {
				fileRestart = rs.getInt("reiniciar");
			}
			
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.out.print("No se pudo encontrar la variable 'reiniciar' en la base de datos");
		}
		
		if(fileRestart > 1)
			return true;
		else
			return false;
	}

	
}
