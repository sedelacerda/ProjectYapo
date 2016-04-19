import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import jxl.write.WriteException;

/**
 * Clase GlobalScanner sirve para escanear todo Yapo.cl cumpliendo los filtros
 * de busqueda, esto se hace para obtener los precios de referencia (precios
 * promedio) del dia.
 * 
 * @author Seba
 *
 */
public class GlobalScanner extends Scanner {

	private int endTimeShowerCounter = 0;
	private long startTime = 0;
	/**
	 * El parametro OUTLIERS_BOUNDS regula los limites para clasificar un dato
	 * como anomalo, se debe mantener con un valor superior a 1 para un correcto
	 * funcionamiento.
	 */
	private final double OUTLIERS_BOUNDS = 1.5;

	private ArrayList<ArrayList<String>> BMIndexs = new ArrayList<ArrayList<String>>();
	/**
	 * Contiene todos los pares Marca-Modelo junto al index de cada atributo
	 * ------------------------------- |IndexB | Brand |IndexM | Model |
	 * ------------------------------- | 0 | 1 | 2 | 3 | | | | | | | | | | | | |
	 * | | | -------------------------------
	 */
	
	public GlobalScanner(MainWindow mainWindow) {
		super(mainWindow);
	}

	/**
	 * scanYapo revisa revisa todas las marcas de autos y todos los modelos de
	 * cada marca de Yapo y con esta informacion rellena el arreglo data
	 * 
	 * @throws IOException
	 */
	public void scanAllYapoCars() throws IOException {
		
		
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (runScan) {
					endTimeShowerCounter = 0;
					startTime = System.currentTimeMillis();
					
					
					
					loadBMIndexs();
					
					
					
					
					if (runScan) {
						for (int t = 0; t < Herramientas.CANT_SCAN_THREADS; t++) {

							final int k = t;
							new Thread() {
								@Override
								public void run() {
									if (runScan) {
										final int[] partition = Herramientas.getBMIndexsPartitions().get(k);
										try {
											scanSomeYapoCars(partition[0], partition[1]);
											System.out.println(endThreadCounter);
											showEndTime();
											
											if (Herramientas.CANT_SCAN_THREADS <= endThreadCounter) {
												
												/* Guardamos la informacion de la busqueda */
												saveLastScanDataToFile();
												
												/* Avisamos que se termino el scan diario */
												context.insertNewProgramCurrentState(
														"El scan diario ha terminado! Pronto comenzará un nuevo scan para encontrar nuevas publicaciones.",
														Color.BLACK, false);

												/* Guardamos las estadisticas del dia a un excel */
												String[][] outStatistics = new String[statistics.size()][];
												for (int i = 0; i < statistics.size(); i++) {
													outStatistics[i] = statistics.get(i);
												}

												// Ordenamos el array segun el nombre de la marca
												Arrays.sort(outStatistics, new Comparator<String[]>() {
													public int compare(final String[] entry1, final String[] entry2) {
														final String brand1 = entry1[0];
														final String brand2 = entry2[0];
														return brand1.compareTo(brand2);
													}
												});

												try {
													if (!runScan) {
														return;
													}
													/* Guardamos el archivo */
													FileManager.writeStatistics("Estadisticas.xls", outStatistics);
													/* Enviamos el archivo por mail */
													emailSender.sendFileEmail("Daily Yapo statistics",
															"En el archivo adjunto se podra observar las estadisticas de los autos entre los años "
																	+ context.globalScan.ANO_MIN + " y el " + context.globalScan.ANO_MAX,
															new String[] { "Estadisticas.xls" }, Usuarios.getAllEmails());
													/* Comenzamos el local scanner */
													context.runLocalScanner();
												} catch (WriteException e) {
													e.printStackTrace();
												}
											}
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
							}.start();
						}
					}
				}
			}
		};

		Timer ti = new Timer();
		/* 86.400.000 milisegundos = 1 dia */
		ti.scheduleAtFixedRate(timerTask, 0, 86400000);
	}

	/** showEndTime se encarga de mostrar un mensaje en pantalla que informa que un thread termino
	 * y ademas muestra cuanto se demoro.
	 */
	private void showEndTime() {
		endTimeShowerCounter++;
		long endTime = System.currentTimeMillis();
		context.insertNewProgramCurrentState("Scanner " + endTimeShowerCounter + "/" + Herramientas.CANT_SCAN_THREADS
				+ " termino con tiempo: " + (endTime - startTime) / 1000 + " segundos.", Color.BLACK, false);

		if (Herramientas.CANT_SCAN_THREADS <= endTimeShowerCounter) {
			context.insertNewProgramCurrentState(
					"Tiempo total: " + (endTime - startTime) / 1000 + " segundos.\nEquivale a "
							+ (endTime - startTime) / 60000 + " minutos.\nCantidad de errores: " + errorCounter,
					Color.BLUE, false);
			endTimeShowerCounter = 0;
		}
	}

	/**
	 * scanSomeYapoCars revisa, de entre todos los autos, solo los que
	 * pertenecen a las marcas que pertenecen al conjunto [initBrandIndex,
	 * lastBrandIndex). Notar que initBrandIndex se incluye pero lastBrandIndex
	 * se exluye. Los indexs entregados son los imaginarios, no los reales, es
	 * decir, el index 0 representa la primera marca que hay en BMIndexs, esta
	 * podria ser Acura que realmente tiene index 2 en la pagina de Yapo.
	 * 
	 * @param initBrandIndex
	 * @param lastBrandIndex
	 * @throws IOException
	 */
	private void scanSomeYapoCars(int initBrandIndex, int lastBrandIndex) throws IOException {

		ArrayList<ArrayList<String>> BMIndexsPartition = getBMIndexsPartition(initBrandIndex, lastBrandIndex);

		// Para cada ano
		for (Integer year = ANO_MIN; year <= ANO_MAX; year++) {
			if (!runScan) {
				return;
			}
			String lastIndex = "" + 0;

			// Para cada par marca-modelo PEDIDO
			for (ArrayList<String> fila : BMIndexsPartition) {
				if (!runScan) {
					return;
				}
				Integer cant = 0;
				Integer pMin = 999999999;
				Integer pMax = 0;
				Integer pSum = 0;

				if (lastIndex.equals(fila.get(0)) == false)
					context.insertNewProgramCurrentState("Buscando datos para modelos de la marca " + fila.get(0)
							+ ": " + fila.get(1) + " del año " + year, Color.BLACK, false);

				ArrayList<Integer> precios = new ArrayList<Integer>();

				// Para cada pagina
				for (int pag = 1;; pag++) {

					if (!runScan) {
						return;
					}

					String source = getUrlSource("http://www.yapo.cl/" + REGION + "/autos?ca=" + COD_REGION
							+ "&l=0&w=1&st=s&br=" + fila.get(0) + "&mo=" + fila.get(2) + "&o=" + pag + "&rs=" + year
							+ "&re=" + year + "&ps=" + INDEX_PRECIO_MIN + "&pe=" + INDEX_PRECIO_MAX);

					if (source.contains("Resultado no encontrado")
							|| source.contains("span class=\"price\"") == false) {
						lastIndex = fila.get(0);
						Integer pMean = 0;

						if (cant > 0) {

							precios = removeOutliers(precios);
							DescriptiveStatistics ds = new DescriptiveStatistics();
							for (Integer p : precios) {
								ds.addValue(p);
							}
							/*
							 * System.out.println(pMin+"	"+ds.getMin());
							 * System.out.println(pMax+"	"+ds.getMax());
							 * pMean = pSum/cant; System.out.println(pMean+"   "
							 * +ds.getMean());
							 */
							cant = precios.size();
							pMin = (int) ds.getMin();
							pMax = (int) ds.getMax();
							pMean = (int) ds.getMean();

							String[] resultStatistic = new String[7];
							resultStatistic[0] = fila.get(1);
							resultStatistic[1] = fila.get(3);
							resultStatistic[2] = year.toString();
							resultStatistic[3] = cant.toString();
							resultStatistic[4] = Herramientas.toPrice(pMin.toString());
							resultStatistic[5] = Herramientas.toPrice(pMax.toString());
							resultStatistic[6] = Herramientas.toPrice(pMean.toString());
							statistics.add(resultStatistic);
							context.insertStatistics(resultStatistic);
						}

						break;
					}

					else {
						while (source.contains("<span class=\"price\">")) {
							if (!runScan) {
								return;
							}
							String link = "No se puede obtener el link";
							try {
								source = source.substring(source.indexOf("class=\"thumbs_subject\""), source.length());
								source = source.substring(source.indexOf("<a href=\"") + 9, source.length());
								link = source.substring(0, source.indexOf("\""));
								source = source.substring(source.indexOf("class=\"title\">") + 14, source.length());
								String titulo = source.substring(0, source.indexOf("</a>"));
								source = source.substring(source.indexOf("span class=\"price\">$ ") + 21,
										source.length());
								String precio = source.substring(0, source.indexOf("</span>")).replaceAll("\\.", "");
								data.add(new String[] { fila.get(1), fila.get(3), year.toString(), titulo, precio,
										link });

								cant++;

								/*
								 * if(pMin>Integer.parseInt(precio)) pMin =
								 * Integer.parseInt(precio);
								 * if(pMax<Integer.parseInt(precio)) pMax =
								 * Integer.parseInt(precio);
								 */

								/* Agregamos el precio a la lista de precios */
								precios.add(Integer.parseInt(precio));

								// pSum += Integer.parseInt(precio);
								// System.out.println(marca+" "+modelo+"
								// "+titulo+" "+precio+" "+link);
							} catch (java.lang.StringIndexOutOfBoundsException e) {
								System.out.println("No se pudo obtener todos los datos de la publicacion: " + link);
							}

						}
					}
				}
			}
		}

		if (!runScan) {
			return;
		}

		synchronized (endThreadCounter) {
			endThreadCounter++;
		}
	}

	/**
	 * El metodo removeOutliers remueve los datos anomalos de la lista de
	 * precios de los autos encontrados (por ahora en scanSomeYapoCars)
	 * 
	 * @param allPrices
	 * @return
	 */
	public ArrayList<Integer> removeOutliers(ArrayList<Integer> allPrices) {

		ArrayList<Integer> out = new ArrayList<Integer>();
		double h = OUTLIERS_BOUNDS * (Calculate.quartile3(allPrices) - Calculate.quartile1(allPrices));
		double undLim = Calculate.quartile1(allPrices) - h;
		double supLim = Calculate.quartile3(allPrices) + h;
		for (Integer price : allPrices) {
			if (undLim <= price && price <= supLim)
				out.add(price);
		}
		return out;
	}

	/**
	 * Indexs entregados son imaginarios, no los reales, leer la descripcion de
	 * scanAllYapoCars para entender esto.
	 * 
	 * @param initBrandIndex
	 * @param lastBrandIndex
	 * @return
	 */
	private ArrayList<ArrayList<String>> getBMIndexsPartition(int initBrandIndex, int lastBrandIndex) {
		ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
		for (int i = initBrandIndex; i < lastBrandIndex; i++) {
			for (ArrayList<String> par : BMIndexs) {
				if (par.get(0).equals(Herramientas.getRealBrandIndex(i)))
					out.add(par);
			}
		}
		return out;
	}

	/**
	 * getBrandsIndexs busca en la pagina
	 * http://www.yapo.cl/region_metropolitana/autos?ca=15_s&l=0&w=1&st=s el
	 * index de cada marca
	 * 
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("serial")
	public void fillBrandsAndModelsIndexs() throws IOException {

		context.insertNewProgramCurrentState("Extrayendo listado de marcas...", Color.BLACK, false);
		String source = getUrlSource("http://www.yapo.cl/" + REGION + "/autos?ca=" + COD_REGION + "&l=0&w=1&st=s");
		source = source.substring(source.indexOf("Marca<"), source.length());

		if (source.indexOf("</option>") < source.indexOf("<option"))
			source = source.substring(source.indexOf("</option>"), source.indexOf("</select>"));
		else
			source = source.substring(0, source.indexOf("</select>"));

		int n = 0;
		while (source.contains("<option value=\"") && runScan) {

			source = source.substring(source.indexOf("<option value=\"") + 15, source.length());
			String index = source.substring(0, source.indexOf("\""));
			source = source.substring(source.indexOf(">") + 1, source.length());
			String brand = source.substring(0, source.indexOf("</option>"));
			n++;
			context.insertNewProgramCurrentState("Buscando listado de modelos para marca " + n + ": " + brand,
					Color.BLACK, false);
			ArrayList<String[]> models = getModelsIndexs(Integer.parseInt(index));
			final String index2 = index;
			final String brand2 = brand;
			
			for (String[] model : models) {
				final String m0 = model[0];
				final String m1 = model[1];
				BMIndexs.add(new ArrayList<String>() {
					{
						add(index2);
						add(brand2);
						add(m0);
						add(m1);
					};
				});
			}
		}

		/*
		 * int i =0; for(ArrayList<String> fila : BMIndexs){ for(String item :
		 * fila){ System.out.print(item+" "); } i++;
		 * System.out.print(fila.get(0)+" "+fila.get(1));
		 * System.out.print("\n"); } System.out.println("MarcasXModelos = "+i);
		 */
		
		//#############################################################

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:ProjectYapo.db");
			c.setAutoCommit(false);
			context.insertNewProgramCurrentState("Accediendo a la base de datos...", Color.BLACK, true);


			stmt = c.createStatement();
			String sql = "DELETE FROM MarcasModelos;";
			stmt.executeUpdate(sql);
			for(ArrayList<String> BMIndexsRow : BMIndexs){
				sql = "INSERT INTO MarcasModelos VALUES (" + BMIndexsRow.get(0) + ", '" + BMIndexsRow.get(1) + 
						"', " + BMIndexsRow.get(2) + ", '" + BMIndexsRow.get(3) + "');"; 
				stmt.executeUpdate(sql);
			}

			stmt.close();
			c.commit();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		context.insertNewProgramCurrentState("Datos guardados satisfactoriamente en la base de datos!", Color.BLACK, true);

		//#########################################################################

		context.insertNewProgramCurrentState("Iniciando el scan...", Color.BLACK, true);

	}

	/** loadBMIndexs toma los indices marca - modelo que se encuentran en la
	 * base de datos y los guarda en el arreglo BMIndexs
	 */
	private void loadBMIndexs(){

		BMIndexs = new ArrayList<ArrayList<String>>();

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:ProjectYapo.db");
			c.setAutoCommit(false);
			context.insertNewProgramCurrentState("Accediendo a la base de datos...", Color.BLACK, true);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM MarcasModelos;" );
			while ( rs.next() ) {
				int brandIndex = rs.getInt("indiceMarca");
				String brandName = rs.getString("nombreMarca");
				int modelIndex = rs.getInt("indiceModelo");
				String modelName = rs.getString("nombreModelo");
				
				ArrayList<String> BM = new ArrayList<String>();
				BM.add(brandIndex+"");
				BM.add(brandName);
				BM.add(modelIndex+"");
				BM.add(modelName);
				BMIndexs.add(BM);
				
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		context.insertNewProgramCurrentState("Indices de marcas y modelos cargados correctamente", Color.BLACK, true);

		context.insertNewProgramCurrentState("Filtrando marcas...", Color.BLACK, true);
		BMIndexs = Herramientas.filterBrands(BMIndexs);
	}

	/**
	 * getModelsIndexs revisa una pagina por cada marca y de esta extrae la
	 * lista de los modelos de la marca
	 * 
	 * @param _brandIndex
	 * @return
	 * @throws IOException
	 */
	private ArrayList<String[]> getModelsIndexs(int _brandIndex) throws IOException {

		ArrayList<String[]> out = new ArrayList<String[]>();
		String source = getUrlSource(
				"http://www.yapo.cl/" + REGION + "/autos?ca=" + COD_REGION + "&l=0&w=1&st=s&br=" + _brandIndex);
		if (!source.equals("")) {
			source = source.substring(source.indexOf(">Modelo<"), source.length());

			if (source.indexOf("</option>") < source.indexOf("<option"))
				source = source.substring(source.indexOf("</option>"), source.indexOf("</select>"));
			else
				source = source.substring(0, source.indexOf("</select>"));

			while (source.contains("<option value=\"")) {
				source = source.substring(source.indexOf("<option value=\"") + 15, source.length());
				String index = source.substring(0, source.indexOf("\""));
				source = source.substring(source.indexOf(">") + 1, source.length());
				String model = source.substring(0, source.indexOf("</option>"));
				out.add(new String[] { index, model });
			}
		}

		return out;
	}

	/**
	 * getSourceData recibe una URL y busca el precio y link de cada auto
	 * mostrado en la pagina
	 * 
	 * @param source
	 * @return
	 */
	public void getSourceData(String marca, String modelo, Integer year, String source) {

		while (source.contains("<span class=\"price\">")) {
			source = source.substring(source.indexOf("class=\"thumbs_subject\""), source.length());
			source = source.substring(source.indexOf("<a href=\"") + 9, source.length());
			String link = source.substring(0, source.indexOf("\""));
			source = source.substring(source.indexOf("class=\"title\">") + 14, source.length());
			String titulo = source.substring(0, source.indexOf("</a>"));
			source = source.substring(source.indexOf("span class=\"price\">$ ") + 21, source.length());
			String precio = source.substring(0, source.indexOf("</span>")).replaceAll("\\.", "");
			data.add(new String[] { marca, modelo, year.toString(), titulo, precio, link });
			// System.out.println(marca+" "+modelo+" "+titulo+" "+precio+"
			// "+link);
		}
	}

}
