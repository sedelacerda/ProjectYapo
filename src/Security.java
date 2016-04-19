import java.awt.Color;
import java.io.IOException;

public class Security {
		
	public static boolean unlock(String key, MainWindow context, Scanner scanner){
		boolean out = false;
		try {
			String source = scanner.getUrlSource("http://beatfan.site90.net/");
			if(source.contains("lmcxkm3p2k39")){
				context.insertNewProgramCurrentState("Conectado!", Color.BLUE, true);
				out = true;
			}
			else{
				context.insertNewProgramCurrentState("ERROR FATAL: El programa no puede iniciar, cierrelo e intente nuevamente.", Color.RED, false);
			}
		} catch (IOException e) {
			context.insertNewProgramCurrentState("ERROR FATAL: No se pudo establecer conexion, cierre el programa e intente nuevamente.", Color.RED, false);
		}
		return out;
	}
}
