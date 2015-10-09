import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LocalScanner extends Scanner {

	// TODO se cambio para testing
	// private final int TIEMPO_REPOSO = 30*60; //Cada 1800 segundos (30
	// minutos) se volvera a buscar nuevos autos
	private final int TIEMPO_REPOSO = 30 * 60;

	private TimerTask timerTask;
	private TimerTask timerTask2;
	private Timer timer;
	private int minTransc = 0;

	// recomendPercent es el porcentaje minimo de diferencia de precio que se
	// usara para clasificar un auto como recomendado. 0<=recomendPercent<=1
	private final double PORCENT_RECOMEND = 0.2;

	public LocalScanner(ArrayList<String[]> globalData, ArrayList<String[]> globalStatistics, int anoMin, int anoMax) {
		this.data = globalData;
		this.statistics = globalStatistics;
		this.ANO_MIN = anoMin;
		this.ANO_MAX = anoMax;
	}

	/**
	 * El metodo run() activa el timer que sera el encargado de generar avisos
	 * cuando se deba efectuar una nueva busqueda de nuevas publicaciones.
	 * Ademas define que se debe hacer cuando se generan estos avisos.
	 */
	public void run() {
		// Aqui definimos lo que se quiere hacer cada vez que suena el timer
		timerTask = new TimerTask() {
			@Override
			public void run() {
				java.util.TimeZone tz = java.util.TimeZone.getTimeZone(ZONA_HORARIA);
				Calendar c = java.util.Calendar.getInstance(tz);
				int mes = c.get(Calendar.MONTH) + 1;

				MainWindow.insertNewProgramCurrentState(c.get(Calendar.DAY_OF_MONTH) + "/" + mes + "/"
						+ c.get(Calendar.YEAR) + " " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE)
						+ "! Hora de ver si ha aparecido algo nuevo!", Color.BLACK);

				try {
					searchForNewPublications();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		timerTask2 = new TimerTask() {
			@Override
			public void run() {
				if (runScan) {
					MainWindow.insertNewProgramCurrentState(
							"Faltan " + (TIEMPO_REPOSO / 60 - minTransc) + " minutos para un nuevo scan.", Color.BLACK);
					minTransc++;
					// TODO checkear esto
					if (TIEMPO_REPOSO / 60 - minTransc <= 0)
						minTransc = 0;
				}
			}
		};

		// Aquí se pone en marcha el timer
		Timer timer = new Timer();

		// TODO tomar en cuenta que esto solo va a funcionar luego de
		// 'TIEMPO_REPOSO'*1000 milisegundos
		// Dentro de 'TIEMPO_REPOSO'*1000 milisegundos avísame cada
		// 'TIEMPO_REPOSO'*1000 milisegundos
		timer.schedule(timerTask, TIEMPO_REPOSO * 1000, TIEMPO_REPOSO * 1000);

		// Dentro de 0 milisegundos avisame cada 60.000 milisegundos (1 minuto)
		timer.schedule(timerTask2, 0, 60000);
	}

	/**
	 * El metodo searchForNewPublications() es el encargado directamente de
	 * efectuar la busqueda de nuevas publicaciones. Este metodo revisa en las
	 * primeras paginas de la seccion de autos de Yapo, donde se encuentran las
	 * publicaciones mas recientes (listado original de Yapo esta ordenado segun
	 * fecha de publicacion) y para cada una de estas publicaciones va viendo si
	 * es que es una "papita" o no, esto ultimo lo hace comparando con los datos
	 * de data.
	 * 
	 * @throws IOException
	 * 
	 */
	private void searchForNewPublications() throws IOException {

		if (!runScan) {
			return;
		}
		MainWindow.insertNewProgramCurrentState("Buscando links de nuevas publicaciones...", Color.BLACK);
		/*
		 * Primero obtendremos los links de todos los autos publicados
		 * recientemente
		 */
		ArrayList<String> links = new ArrayList<String>();
		// Para cada pagina
		Mainloop: for (int pag = 1;; pag++) {
			if (!runScan) {
				return;
			}
			// TODO se cambio esto para testing
			String source = getUrlSource(
					"http://www.yapo.cl/" + REGION + "/autos?ca=" + COD_REGION + "&l=0&w=1&st=s&rs=" + ANO_MIN + "&re="
							+ ANO_MAX + "&ps=" + INDEX_PRECIO_MIN + "&pe=" + INDEX_PRECIO_MAX + "&o=" + pag);
							// String source =
							// getUrlSource("http://www.yapo.cl/"+REGION+"/autos?ca="+COD_REGION+"&l=0&w=1&st=s&rs="+ANO_MIN+"&re="+ANO_MAX+"&ps="+9+"&pe="+INDEX_PRECIO_MAX+"&o="+pag);

			// Pagina esta vacia...
			if (source.contains("Resultado no encontrado") || source.contains("span class=\"price\"") == false) {
				break Mainloop;
			}

			// Pagina tiene resultados
			else {
				while (source.contains("<span class=\"price\">")) {
					if (!runScan) {
						return;
					}
					try {
						source = source.substring(source.indexOf("class=\"listing_thumbs_date\""), source.length());
						source = source.substring(source.indexOf("class=\"date\">") + 13, source.length());
						String fechaStr = source.substring(0, source.indexOf("<"));
						source = source.substring(source.indexOf("class=\"hour\">") + 13, source.length());
						String horaStr = source.substring(0, source.indexOf("<"));

						if (isInRightTimeInterval(fechaStr, horaStr)) {
							source = source.substring(source.indexOf("class=\"thumbs_subject\""), source.length());
							source = source.substring(source.indexOf("<a href=\"") + 9, source.length());
							links.add(source.substring(0, source.indexOf("\"")));

							// MainWindow.insertRecomended(getDataFromPost(source.substring(0,
							// source.indexOf("\""))));
						} else
							break Mainloop;
					} catch (StringIndexOutOfBoundsException e) {
						break;
					}
				}
			}
		}

		MainWindow.insertNewProgramCurrentState("Terminó la revisión de links", Color.BLACK);

		saveLastScanDataToFile();

		MainWindow.insertNewProgramCurrentState("Buscando publicaciones recomendadas...", Color.BLACK);

		ArrayList<String[]> recomended = new ArrayList<String[]>();

		for (String link : links) {
			if (!runScan) {
				return;
			}

			/* TODO linkInfo SERA NULL SI EL AUTO ESTA CHOCADO */
			String[] linkInfo = getDataFromPost(link);
			

			if (linkInfo != null) {
				// Primero revisamos si es un recomendado o no
				for (String[] item : statistics) {
					if (!runScan) {
						return;
					}
					// Si son de igual marca
					if (item[0].equals(linkInfo[1])) {
						// Si son de igual modelo
						if (item[1].equals(linkInfo[2])) {
							// Si son de igual año
							if (item[2].equals(linkInfo[4])) {
								// Estadisticas correspondiente encontradas!

								Integer priceUndLimit = (int) ((1 - PORCENT_RECOMEND)* Integer.parseInt(item[6].replaceAll("[\\s.]", "")));
								// Revisamos si es un recomendado
								if (Integer.parseInt(linkInfo[7].replaceAll("[\\s.]", "")) <= priceUndLimit) {
									String[] allInfo = new String[linkInfo.length + 4];
									for(int i=0; i<linkInfo.length; i++){
										allInfo[i] = linkInfo[i];
									}
									allInfo[linkInfo.length] = item[3];
									allInfo[linkInfo.length+1] = item[4];
									allInfo[linkInfo.length+2] = item[5];
									allInfo[linkInfo.length+3] = item[6];
									
									recomended.add(allInfo);
									MainWindow.insertRecomended(linkInfo);
								}

								// Ahora lo metemos a las estadisticas
								int quantStat = Integer.parseInt(item[3]);
								int meanPriceStat = Integer.parseInt(item[6].replaceAll("[\\s.]", ""));
								Integer price = Integer.parseInt(linkInfo[7].replaceAll("[\\s.]", ""));

								/**TODO se puso el Herramientas.toPrice, si no funciona borrar **/
								// Actualizamos si es que es el precio minimo
								if (price < Integer.parseInt(item[4].replaceAll("[\\s.]", "")))
									item[4] = Herramientas.toPrice(linkInfo[7]);
								// Actualizamos si es que es el precio maximo
								if (Integer.parseInt(item[5].replaceAll("[\\s.]", "")) < price)
									item[5] = Herramientas.toPrice(linkInfo[7]);

								// Aumentamos la cantidad
								Integer newQuant = quantStat + 1;
								item[3] = newQuant.toString();

								// Actualizamos el precio promedio
								Integer newMean = ((meanPriceStat * quantStat) + price) / newQuant;
								item[6] = Herramientas.toPrice(newMean.toString());

								break;
							}
						}
					}
				}
			}
		}

		// Finalmente, si es que existe al menos un recomendado mandamos un mail
		// avisando
		if (0 < recomended.size()) {
			if (!runScan) {
				return;
			}
			String content = "A continuacion se listan los autos recomendados encontrados en los ultimos "
					+ TIEMPO_REPOSO / 60 + " minutos:\n\n";
			int count = 1;

			for (String[] car : recomended) {
				content = content + count + ". " + car[0] + "\n";
				content = content + "\tMarca: " + car[1] + "\n";
				content = content + "\tModelo: " + car[2] + "\n";
				content = content + "\tVersion: " + car[3] + "\n";
				content = content + "\tAno: " + car[4] + "\n";
				content = content + "\tKilometros: " + car[5] + "\n";
				content = content + "\tTransmision: " + car[6] + "\n";
				content = content + "\tPrecio: " + car[7] + "\n";
				content = content + "\tLink: " + car[8] + "\n";
				content = content + "\tRef: Precio Min = "+car[car.length-3];
				content = content + "\tRef: Precio Max = "+car[car.length-2];
				content = content + "\tRef: Precio Prom = "+car[car.length-1]+"\n\n";
				count++;
			}
			Email.sendSimpleEmail("Recomended Yapo cars", content, Usuarios.getAllEmails());
		}

		else {
			MainWindow.insertNewProgramCurrentState("No se ha encontrado ninguna publicacion recomendada", Color.BLACK);
		}
	}

	private boolean isInRightTimeInterval(String date, String time) {

		String hour = time.substring(0, time.indexOf(":"));
		String min = time.substring(time.indexOf(":") + 1, time.length());
		String day = "";
		String month = "";
		if (date.equalsIgnoreCase("Hoy")) {
			Calendar cal = Calendar.getInstance();
			day = "" + cal.get(Calendar.DAY_OF_MONTH);
			int mes = cal.get(Calendar.MONTH) + 1;
			month = "" + mes;
		} else if (date.equalsIgnoreCase("Ayer")) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			day = "" + cal.get(Calendar.DAY_OF_MONTH);
			int mes = cal.get(Calendar.MONTH) + 1;
			month = "" + mes;
		} else {
			day = date.substring(0, date.indexOf(" "));
			month = date.substring(date.indexOf(" ") + 1, date.length());
		}

		String fullDate = day + "/" + month + "/" + ANO_ACTUAL + " " + hour + ":" + min + ":00";

		String fileHour = "00";
		String fileMin = "00";
		String fileDay = "01";
		String fileMonth = "01";

		/*
		 * Primero obtenemos la fecha del ultimo scan desde el archivo
		 * lastTimeData
		 */
		File file = new File("lastTimeData.txt");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			boolean foundHour = false;
			boolean foundDate = false;
			String line;
			while ((line = br.readLine()) != null) {

				if (line.contains("<HORA>")) {
					fileHour = line.substring(line.indexOf("<HORA>") + 6, line.indexOf(":"));
					line = line.substring(line.indexOf(":") + 1, line.length());
					fileMin = line.substring(0, line.indexOf(":"));
					if (fileMin.length() < 2)
						fileMin = "0" + fileMin;
					foundHour = true;
				}

				if (line.contains("<FECHA>")) {
					fileDay = line.substring(line.indexOf("<FECHA>") + 7, line.indexOf("-"));
					line = line.substring(line.indexOf("-") + 1, line.length());
					fileMonth = line.substring(0, line.indexOf("-"));
					foundDate = true;
				}

				if (foundHour && foundDate)
					break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		String fileDate = fileDay + "/" + fileMonth + "/" + ANO_ACTUAL + " " + fileHour + ":" + fileMin + ":00";

		/* Ahora comparamos las fechas */
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		Date d1 = null;
		Date d2 = null;

		long diffSec = 0;

		try {
			d1 = format.parse(fileDate);
			d2 = format.parse(fullDate);

			// in milliseconds
			long diff = d2.getTime() - d1.getTime();

			diffSec = diff / 1000;

		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO arreglar y poner '0<=diffSec && diffSec<TIEMPO_REPOSO' si se
		// quiere que tenga como tope un tiempo
		// poner solo '0<=diffSec' si se quiere que revise hasta la fecha del
		// ultimo scan
		if (0 <= diffSec)
			return true;
		else
			return false;
	}
	
	public static boolean carIsUseless(String webPageSource){
		if ((webPageSource.toLowerCase().contains("chocado") 		|| webPageSource.toLowerCase().contains("chokado")
				|| webPageSource.toLowerCase().contains("chocada") 	|| webPageSource.toLowerCase().contains("chocad")
				|| webPageSource.toLowerCase().contains("chocao") 	|| webPageSource.toLowerCase().contains("shocado")
				|| webPageSource.toLowerCase().contains("chokdo") 	|| webPageSource.toLowerCase().contains("prenda") 
				|| webPageSource.toLowerCase().contains("choque")	|| webPageSource.toLowerCase().contains("choke")
				|| webPageSource.toLowerCase().contains("shoke")	|| webPageSource.toLowerCase().contains("desarme"))
						&& (!webPageSource.toLowerCase().contains("nunca chocado")
						&& !webPageSource.toLowerCase().contains("nunca chokado")
						&& !webPageSource.toLowerCase().contains("nunca chocada")
						&& !webPageSource.toLowerCase().contains("nunca chocad")
						&& !webPageSource.toLowerCase().contains("nunca chocao")
						&& !webPageSource.toLowerCase().contains("nunca shocado")
						&& !webPageSource.toLowerCase().contains("nunca chokdo")
						&& !webPageSource.toLowerCase().contains("nunk chocado")
						&& !webPageSource.toLowerCase().contains("nunk chokado")
						&& !webPageSource.toLowerCase().contains("nunk chocada")
						&& !webPageSource.toLowerCase().contains("nunk chocad")
						&& !webPageSource.toLowerCase().contains("nunk chocao")
						&& !webPageSource.toLowerCase().contains("nunk shocado")
						&& !webPageSource.toLowerCase().contains("nunk chokdo")
						&& !webPageSource.toLowerCase().contains("que prenda")
						&& !webPageSource.toLowerCase().contains("sin prenda")
						&& !webPageSource.toLowerCase().contains("sinprenda")
						&& !webPageSource.toLowerCase().contains("sin choque")
						&& !webPageSource.toLowerCase().contains("ni choque")
						&& !webPageSource.toLowerCase().contains("nichoque")
						&& !webPageSource.toLowerCase().contains("ni prenda")
						&& !webPageSource.toLowerCase().contains("niprenda")
						&& !webPageSource.toLowerCase().contains("ningun choque")
						&& !webPageSource.toLowerCase().contains("ningún choque")
						&& !webPageSource.toLowerCase().contains("sinchoque")
						&& !webPageSource.toLowerCase().contains("ningunchoque"))) {
			return true;
		}
		
		else{
			return false;
		}
	}
	
	private boolean isComposedBrand(String brandName){
		if (brandName.equalsIgnoreCase("alfa") || brandName.equalsIgnoreCase("american")
				|| brandName.equalsIgnoreCase("asia") || brandName.equalsIgnoreCase("aston")
				|| brandName.equalsIgnoreCase("gac") || brandName.equalsIgnoreCase("great")
				|| brandName.equalsIgnoreCase("kia") || brandName.equalsIgnoreCase("land")
				|| brandName.equalsIgnoreCase("mercedes") || brandName.equalsIgnoreCase("polski")
				|| brandName.equalsIgnoreCase("rolls")) {
			
			return true;
		}
		
		else{
			return false;
		}
	}

	/**
	 * getDataFromPost retorna un String[] que contiene: titulo, marca, modelo,
	 * version, ano, kms, transmision, precio, URL del link que se le ha
	 * entregado como parametro. RETORNA NULL si el auto esta chocado
	 **/
	private String[] getDataFromPost(String URL) {
		String[] data = new String[9];

		String source = "";
		try {
			source = getUrlSource(URL);
		} catch (IOException e) { }

		if (carIsUseless(source)) {
			
			System.out.println("Se elimino la siguiente publicacion: " + URL);
			return null;
		}

		else {
			
			String titulo = "";
			try{
				source = source.substring(source.indexOf("<title>") + 7, source.length());
				// Aqui obtenemos el titulo del post
				titulo = source.substring(0, source.indexOf(","));
			} catch(StringIndexOutOfBoundsException e){	}
			
			String marcModVer = "";
			String marca = "";
			String modVer = "";
			String modelo = "";
			try{
				source = source.substring(source.indexOf("class=\"car-title title-details\">") + 32, source.length());
				marcModVer = source.substring(0, source.indexOf("</h5>")).trim();
				// Aqui obtenemos la marca (solo si es simple)
				marca = marcModVer.substring(0, marcModVer.indexOf(" "));

				/**
				 * Cubrimos casos excepcionales como por ejemplo 'Alfa Romeo'
				 * que es un nombre compuesto
				 */

				if (isComposedBrand(marca)) {
					String aux = marcModVer.substring(marca.length(), marcModVer.length()).trim();
					// Aqui modificamos el nombre de la marca si este es
					// compuesto
					marca = marca + " " + aux.substring(0, aux.indexOf(" ")).trim();
				}

				modVer = marcModVer.substring(marca.length(), marcModVer.length()).trim();
				// Aqui obtenemos el modelo
				modelo = modVer.substring(0, modVer.indexOf("\t"));
			} catch (StringIndexOutOfBoundsException e){
				System.out.println("No se pudo obtener la marca y/o modelo de la siguiente publicación: " + URL);
				return null;
			}
			
			String version = "";
			try{
				// Aqui obtenemos la version
				version = modVer.substring(modelo.length(), modVer.length()).trim();
			} catch (StringIndexOutOfBoundsException e){ }

			String precio = "";
			String ano = "";
			try{
				source = source.substring(source.indexOf("price price-final"), source.length());
				source = source.substring(source.indexOf("<strong>") + 8, source.length());
				precio = source.substring(0, source.indexOf("</strong>"));
				source = source.substring(source.indexOf("<td>") + 4, source.length());
				ano = source.substring(0, source.indexOf("</td>"));
			} catch (StringIndexOutOfBoundsException e){
				System.out.println("No se pudo obtener el precio y/o año de la siguiente publicacion: " + URL);
				return null;
			}
			
			String kms = "";
			String transmision = "";
			
			try{
				source = source.substring(source.indexOf("<td>") + 4, source.length());
				kms = source.substring(0, source.indexOf("</td>"));
				source = source.substring(source.indexOf("(cambio)</th>"), source.length());
				source = source.substring(source.indexOf("<td>") + 4, source.length());
				String trans = source.substring(0, source.indexOf("</td>"));
				transmision = "";
				if (trans.equalsIgnoreCase("manual"))
					transmision = "mec";
				else
					transmision = "aut";
			} catch (StringIndexOutOfBoundsException e){ }

			data = new String[] { titulo, marca, modelo, version, ano, kms, transmision, precio, URL };
		}
		/*
		try {
			String source = getUrlSource(URL);

			if ((source.toLowerCase().contains("chocado") || source.toLowerCase().contains("chokado")
					|| source.toLowerCase().contains("chocada") || source.toLowerCase().contains("chocad")
					|| source.toLowerCase().contains("chocao") || source.toLowerCase().contains("shocado")
					|| source.toLowerCase().contains("chokdo"))
					&& (!source.toLowerCase().contains("nunca chocado")
							&& !source.toLowerCase().contains("nunca chokado")
							&& !source.toLowerCase().contains("nunca chocada")
							&& !source.toLowerCase().contains("nunca chocad")
							&& !source.toLowerCase().contains("nunca chocao")
							&& !source.toLowerCase().contains("nunca shocado")
							&& !source.toLowerCase().contains("nunca chokdo")
							&& !source.toLowerCase().contains("nunk chocado")
							&& !source.toLowerCase().contains("nunk chokado")
							&& !source.toLowerCase().contains("nunk chocada")
							&& !source.toLowerCase().contains("nunk chocad")
							&& !source.toLowerCase().contains("nunk chocao")
							&& !source.toLowerCase().contains("nunk shocado")
							&& !source.toLowerCase().contains("nunk chokdo"))) {
				System.out.println("Se elimino la siguiente publicacion: " + URL);
				data = null;
			}

			else {
				source = source.substring(source.indexOf("<title>") + 7, source.length());
				// Aqui obtenemos el titulo del post
				String titulo = source.substring(0, source.indexOf(","));
				source = source.substring(source.indexOf("class=\"car-title title-details\">") + 32, source.length());
				String marcModVer = source.substring(0, source.indexOf("</h5>")).trim();
				// Aqui obtenemos la marca (solo si es simple)
				String marca = marcModVer.substring(0, marcModVer.indexOf(" "));

				/** Cubrimos casos excepcionales como por ejemplo 'Alfa Romeo'
				 * que es un nombre compuesto
				 */
		/*
				if (marca.equalsIgnoreCase("alfa") || marca.equalsIgnoreCase("american")
						|| marca.equalsIgnoreCase("asia") || marca.equalsIgnoreCase("aston")
						|| marca.equalsIgnoreCase("gac") || marca.equalsIgnoreCase("great")
						|| marca.equalsIgnoreCase("kia") || marca.equalsIgnoreCase("land")
						|| marca.equalsIgnoreCase("mercedes") || marca.equalsIgnoreCase("polski")
						|| marca.equalsIgnoreCase("rolls")) {
					String aux = marcModVer.substring(marca.length(), marcModVer.length()).trim();
					// Aqui modificamos el nombre de la marca si este es
					// compuesto
					marca = marca + " " + aux.substring(0, aux.indexOf(" ")).trim();

				}

				String modVer = marcModVer.substring(marca.length(), marcModVer.length()).trim();
				// Aqui obtenemos el modelo
				String modelo = modVer.substring(0, modVer.indexOf("\t"));
				// Aqui obtenemos la version
				String version = modVer.substring(modelo.length(), modVer.length()).trim();

				source = source.substring(source.indexOf("price price-final"), source.length());
				source = source.substring(source.indexOf("<strong>") + 8, source.length());
				String precio = source.substring(0, source.indexOf("</strong>"));
				source = source.substring(source.indexOf("<td>") + 4, source.length());
				String ano = source.substring(0, source.indexOf("</td>"));
				source = source.substring(source.indexOf("<td>") + 4, source.length());
				String kms = source.substring(0, source.indexOf("</td>"));
				source = source.substring(source.indexOf("(cambio)</th>"), source.length());
				source = source.substring(source.indexOf("<td>") + 4, source.length());
				String trans = source.substring(0, source.indexOf("</td>"));
				String transmision = "";
				if (trans.equalsIgnoreCase("manual"))
					transmision = "mec";
				else
					transmision = "aut";

				data = new String[] { titulo, marca, modelo, version, ano, kms, transmision, precio, URL };
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (java.lang.StringIndexOutOfBoundsException o) {
			System.out.println("No se ha encontrado alguno de los datos de la publicacion ubicada en: " + URL);
		}
		*/
		return data;
	}

}
