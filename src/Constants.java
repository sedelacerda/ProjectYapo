
public class Constants {
	
	/**PERCENT_RECOMEND es el porcentaje minimo de diferencia de precio que se 
	 * usara para clasificar un auto como recomendado. 0<=PERCENT_RECOMEND<=1
	 */
	public static final double PORCENT_RECOMEND = 0.2;
	
	/** Cada 1800 segundos (30 minutos) se volvera a buscar nuevos autos */
	public static final int TIEMPO_REPOSO = 30 * 60;
	
	/**
	 * El parametro OUTLIERS_BOUNDS regula los limites para clasificar un dato
	 * como anomalo, se debe mantener con un valor superior a 1 para un correcto
	 * funcionamiento.
	 */
	public static final double OUTLIERS_BOUNDS = 1.5;
	
	public static final String ADMIN_EMAIL = "projectyapo@gmail.com";
	
	public static final String ADMIN_PASS = "giulietta2015";
	
	/** 
	 * Cantidad de threads que se usan para hacer el scan de todos los autos
	 * de yapo (Main Scan)
	 */
	public static final int CANT_SCAN_THREADS = 8;
	
	public static final String ZONA_HORARIA = "GMT-3";
	
	public static final int ANO_ACTUAL = 2015;
	
	public static final String REGION = "region_metropolitana";
	
	public static final String COD_REGION = "15_s"; // region metropolitana
	
	public static final String CORRECT_KEY = "lmcxkm3p2k39";
	
	public static final String SECURITY_URL = "http://beatfan.site90.net/";
	
	public static final String[][] LISTA_EMAILS = new String[][]{
		{"Sebastian", "sebadelacerda@gmail.com"},
		//{"Giovanni", "giovanni.t.d@gmail.com"}
	};
	
	public static String[] LISTA_PRECIOS_MIN = new String[] { "0", "100.000", "150.000", "200.000", "250.000", "300.000",
		"400.000", "500.000", "750.000", "1.000.000", "1.500.000", "2.000.000", "2.500.000", "3.000.000",
		"4.000.000", "5.000.000", "7.500.000", "10.000.000", "15.000.000", "20.000.000", "25.000.000", "30.000.000",
		"40.000.000", "50.000.000", "75.000.000" };
	
	public static String[] LISTA_PRECIOS_MAX = new String[] { "0", "100.000", "150.000", "200.000", "250.000", "300.000",
		"400.000", "500.000", "750.000", "1.000.000", "1.500.000", "2.000.000", "2.500.000", "3.000.000",
		"4.000.000", "5.000.000", "7.500.000", "10.000.000", "15.000.000", "20.000.000", "25.000.000", "30.000.000",
		"40.000.000", "50.000.000", "75.000.000", "+75.000.000" };
	
	public static String[] LISTA_ANOS_MIN = new String[] { "1960", "1961", "1962", "1963", "1964", "1965", "1966",
		"1967", "1968", "1969", "1970", "1971", "1972", "1973", "1974", "1975", "1976", "1977", "1978", "1979",
		"1980", "1981", "1982", "1983", "1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992",
		"1993", "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003", "2004", "2005",
		"2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015" };
	
	public static String[] LISTA_ANOS_MAX = new String[] { "1960", "1961", "1962", "1963", "1964", "1965", "1966",
		"1967", "1968", "1969", "1970", "1971", "1972", "1973", "1974", "1975", "1976", "1977", "1978", "1979",
		"1980", "1981", "1982", "1983", "1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992",
		"1993", "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003", "2004", "2005",
		"2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015" };

}
