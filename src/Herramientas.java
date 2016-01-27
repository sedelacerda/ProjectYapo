import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
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
					System.out.println(par.size() + " - " +par.get(0) + " - " + par.get(1)+ " - "+ par.get(2) + " - " + par.get(3));
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
			  // TODO Auto-generated catch block
			  e.printStackTrace();			  
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		}
	}
	
	/** getLastTimeScanHour revisa el archivo lastTimeScanData y retorna un string
	 * de forma HH/MM que representa la hora de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanTime(){
		String fileHour = "00";
		String fileMin = "00";
		String fileSec = "00";
		File file = new File("lastTimeData.txt");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			boolean foundHour = false;
			String line;
			while ((line = br.readLine()) != null) {

				if (line.contains("<HORA>")) {
					fileHour = line.substring(line.indexOf("<HORA>") + 6, line.indexOf(":"));
					if (fileHour.length() < 2)
						fileHour = "0" + fileHour;
					line = line.substring(line.indexOf(":") + 1, line.length());
					fileMin = line.substring(0, line.indexOf(":"));
					if (fileMin.length() < 2)
						fileMin = "0" + fileMin;
					foundHour = true;
				}

				if (foundHour)
					break;
			}

		} catch (IOException e) {
			System.out.print("No se pudo encontrar la hora en el archivo lastTimeData.txt");
		}
		
		return fileHour+":"+fileMin+":"+fileSec;
	}
	
	/** getLastTimeScanDate revisa el archivo lastTimeScanData y retorna un string
	 * de forma DD/MM/YYYY que representa la fecha de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanDate(){
		
		String fileDay = "01";
		String fileMonth = "01";
		String fileYear = "2015";
		
		File file = new File("lastTimeData.txt");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			boolean foundDate = false;
			String line;
			while ((line = br.readLine()) != null) {

				if (line.contains("<FECHA>")) {
					fileDay = line.substring(line.indexOf("<FECHA>") + 7, line.indexOf("-"));
					line = line.substring(line.indexOf("-") + 1, line.length());
					fileMonth = line.substring(0, line.indexOf("-"));
					line = line.substring(line.indexOf("-") + 1, line.length());
					fileYear = line.substring(0, line.indexOf("</FECHA>"));
					foundDate = true;
				}

				if (foundDate)
					break;
			}

		} catch (IOException e) {
			System.out.print("No se pudo encontrar la fecha en el archivo lastTimeData.txt");
		}
		
		return fileDay + "/" + fileMonth + "/" + fileYear;
	}
	
	/** getLastTimeScanMinYear revisa el archivo lastTimeScanData y retorna un string
	 * que representa el año minimo de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanMinYear(){
		
		String fileMinYear = "2014";
		
		File file = new File("lastTimeData.txt");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			boolean foundMinYear = false;
			String line;
			while ((line = br.readLine()) != null) {

				if (line.contains("<ANO MIN>")) {
					fileMinYear = line.substring(line.indexOf("<ANO MIN>") + 9, line.indexOf("</ANO MIN>"));
					foundMinYear = true;
				}

				if (foundMinYear)
					break;
			}

		} catch (IOException e) {
			System.out.print("No se pudo encontrar el año mínimo en el archivo lastTimeData.txt");
		}
		
		return fileMinYear;
	}
	
	/** getLastTimeScanMaxYear revisa el archivo lastTimeScanData y retorna un string
	 * que representa el año maximo de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanMaxYear(){
		
		String fileMaxYear = "2015";
		
		File file = new File("lastTimeData.txt");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			boolean foundMaxYear = false;
			String line;
			while ((line = br.readLine()) != null) {

				if (line.contains("<ANO MAX>")) {
					fileMaxYear = line.substring(line.indexOf("<ANO MAX>") + 9, line.indexOf("</ANO MAX>"));
					foundMaxYear = true;
				}

				if (foundMaxYear)
					break;
			}

		} catch (IOException e) {
			System.out.print("No se pudo encontrar el año máximo en el archivo lastTimeData.txt");
		}
		
		return fileMaxYear;
	}
	
	/** getLastTimeScanMinPriceIndex revisa el archivo lastTimeScanData y retorna un string
	 * que representa el indice del precio minimo de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanMinPriceIndex(){
		
		String fileMinPriceIndex = "0";
		
		File file = new File("lastTimeData.txt");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			boolean foundMinPriceIndex = false;
			String line;
			while ((line = br.readLine()) != null) {

				if (line.contains("<INDEX PRECIO MIN>")) {
					fileMinPriceIndex = line.substring(line.indexOf("<INDEX PRECIO MIN>") + 18, line.indexOf("</INDEX PRECIO MIN>"));
					foundMinPriceIndex = true;
				}

				if (foundMinPriceIndex)
					break;
			}

		} catch (IOException e) {
			System.out.print("No se pudo encontrar el indice del precio minimo en el archivo lastTimeData.txt");
		}
		
		return fileMinPriceIndex;
	}
	
	/** getLastTimeScanMaxPriceIndex revisa el archivo lastTimeScanData y retorna un string
	 * que representa el indice del precio maximo de la ultima vez que se realizo un scan
	 * @return
	 */
	public static String getLastTimeScanMaxPriceIndex(){
		
		String fileMaxPriceIndex = "20";
		File file = new File("lastTimeData.txt");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			boolean foundMaxPriceIndex = false;
			String line;
			while ((line = br.readLine()) != null) {

				if (line.contains("<INDEX PRECIO MAX>")) {
					fileMaxPriceIndex = line.substring(line.indexOf("<INDEX PRECIO MAX>") + 18, line.indexOf("</INDEX PRECIO MAX>"));
					foundMaxPriceIndex = true;
				}

				if (foundMaxPriceIndex)
					break;
			}

		} catch (IOException e) {
			System.out.print("No se pudo encontrar el indice de precio maximo en el archivo lastTimeData.txt");
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
		
		String fileShouldRestart = "false";
		File file = new File("lastTimeData.txt");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			boolean foundShouldRestart = false;
			String line;
			while ((line = br.readLine()) != null) {

				if (line.contains("<REINICIAR>")) {
					fileShouldRestart = line.substring(line.indexOf("<REINICIAR>") + 11, line.indexOf("</REINICIAR>"));
					foundShouldRestart = true;
				}

				if (foundShouldRestart)
					break;
			}

		} catch (IOException e) {
			System.out.print("No se pudo encontrar el indice de precio maximo en el archivo lastTimeData.txt");
		}
		
		if(fileShouldRestart.toLowerCase().equals("true"))
			return true;
		
		else
			return false;
	}

	
}
