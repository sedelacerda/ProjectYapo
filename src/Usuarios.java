import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Usuarios {

	/**
	 * Si se quiere agregar un nuevo usuario, basta con ingresar el par
	 * nombre-email en el Map 'mails'
	 **/
	private static final Map<String, String> mails;

	static {
		mails = new HashMap<String, String>();
		for(int i=0; i<Constants.LISTA_EMAILS.length; i++){
			mails.put(Constants.LISTA_EMAILS[i][0], Constants.LISTA_EMAILS[i][1]);
		}
	}

	/**
	 * Si a getEmail se le entrega un nombre de usuario que no existe entonces
	 * retorna null
	 **/
	public static String getEmail(String user) {
		return mails.get(user);
	}

	public static String[] getAllEmails() {
		String[] allMails = new String[mails.size()];
		int n = 0;
		for (Entry<String, String> entry : mails.entrySet()) {
			allMails[n] = entry.getValue();
			n++;
		}

		return allMails;
	}

}
