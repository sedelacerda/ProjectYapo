import java.awt.Color;
import java.io.IOException;

public class Security {
	
	private static String correctKey = "lmcxkm3p2k39";
	
	public static boolean unlock(String key){
		boolean out = false;
		try {
			String source = Scanner.getUrlSource("http://beatfan.site90.net/");
			if(source.contains(correctKey)){
				MainWindow.insertNewProgramCurrentState("Conectado!", Color.BLUE);
				out = true;
			}
			else{
				MainWindow.insertNewProgramCurrentState("ERROR FATAL: El programa no puede iniciar, cierrelo e intente nuevamente.", Color.RED);
			}
		} catch (IOException e) {
			MainWindow.insertNewProgramCurrentState("ERROR FATAL: No se pudo establecer conexion, cierre el programa e intente nuevamente.", Color.RED);
		}
		return out;
	}
}
