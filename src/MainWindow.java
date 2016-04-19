import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import de.javasoft.plaf.synthetica.SyntheticaPlainLookAndFeel;

import javax.swing.plaf.metal.MetalLookAndFeel;

import java.util.Comparator;
import java.awt.CardLayout;

public class MainWindow {
	
	private JFrame frame;
	public String indexPrecioMin = Herramientas.getLastTimeScanMinPriceIndex();
	public String indexPrecioMax = Herramientas.getLastTimeScanMaxPriceIndex();
	public String anoMin = Herramientas.getLastTimeScanMinYear();
	public String anoMax = Herramientas.getLastTimeScanMaxYear();
	
	public boolean[] regiones = new boolean[]{true, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false,};
	
	private JLabel lblConnectionStatus;
	private JLabel lblConexion;
	private JLabel lblFaltanMinutos;
	private JTable tableStatistics;
	private JTable tableRecomended;
	private JTextPane textPaneLog;
	private JProgressBar progressBar;
	private JScrollPane scrollPaneLog;
	
	private boolean isRunning = false;
	public Thread scanAll;
	public GlobalScanner globalScan;
	public LocalScanner localScan;
	
	private PopUpConfigurations popUpConfig;
	
	
	public static void main(String[] args) {
		
		try 
	    {
			//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
			
			UIManager.setLookAndFeel(new SyntheticaPlainLookAndFeel());
	    } 
	    catch (Exception e) 
	    {
	      e.printStackTrace();
	    }
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setTitle("Project Yapo");
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public MainWindow() {
		initialize();
	}

	
	private void initialize() {
		
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.LIGHT_GRAY);
		frame.setSize(800, 600);
		//frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panelLog = new JPanel();
		panelLog.setPreferredSize(new Dimension(100,300));
		tabbedPane.addTab("Estado del programa", null, panelLog, null);
		panelLog.setLayout(new BorderLayout(0, 0));
		
		scrollPaneLog = new JScrollPane();
		panelLog.add(scrollPaneLog, BorderLayout.CENTER);
		
		
		textPaneLog = new JTextPane();
		textPaneLog.setBackground(Color.WHITE);
		textPaneLog.setEditable(false);
		
		scrollPaneLog.setViewportView(textPaneLog);
		
		JPanel panelStatistics = new JPanel();
		tabbedPane.addTab("Estadisticas", null, panelStatistics, null);
		panelStatistics.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panelStatistics.add(scrollPane);
		
		tableStatistics = new JTable();
		tableStatistics.setAutoCreateRowSorter(true);
		tableStatistics.setModel(new DefaultTableModel(new Object[][] {},
				new String[] { "Marca", "Modelo", "A\u00F1o", "Cantidad", "Precio Min", "Precio Max", "Precio Prom" }) {
			boolean[] columnEditables = new boolean[] { false, false, false, false, false, false, false };

			@Override
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}			
		});
		
		TableRowSorter<DefaultTableModel> rowSorter = (TableRowSorter<DefaultTableModel>)tableStatistics.getRowSorter();
		
		for(int i=2; i<7; i++){
			rowSorter.setComparator(i, new Comparator<String>() {
				public int compare(String o1, String o2)
				{
					return Integer.parseInt(o1.replace(".","")) - Integer.parseInt(o2.replace(".",""));
				}
			});
		}
		
		
		
		scrollPane.setViewportView(tableStatistics);
		
		JPanel panelRecomended = new JPanel();
		tabbedPane.addTab("Recomendados", null, panelRecomended, null);
		panelRecomended.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPaneRecomended = new JScrollPane();
		panelRecomended.add(scrollPaneRecomended);
		
		tableRecomended = new JTable();
		tableRecomended.setAutoCreateRowSorter(true);
		tableRecomended.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Titulo", "Marca",
				"Modelo", "Version", "A\u00F1o", "Kilometraje", "Transmision", "Precio", "Link" }) {
			boolean[] columnEditables = new boolean[] { false, false, false, false, false, false, false, false, false };

			@Override
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		scrollPaneRecomended.setViewportView(tableRecomended);
		
		JPanel panelConnection = new JPanel();
		panelConnection.setPreferredSize(new Dimension(100,30));
		panelConnection.setLayout(new BorderLayout(0, 0));
		frame.getContentPane().add(panelConnection, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panelConnection.add(panel, BorderLayout.EAST);
		
		lblConexion = new JLabel("Conexion: ");
		panel.add(lblConexion);
		lblConnectionStatus = new JLabel("");
		panel.add(lblConnectionStatus);
		
		JPanel panel_1 = new JPanel();
		panelConnection.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new CardLayout(0, 0));
		
		lblFaltanMinutos = new JLabel("Bienvenido!");
		lblFaltanMinutos.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblFaltanMinutos, "name_872730900023863");
		
		progressBar = new JProgressBar(0, 100);
		panel_1.add(progressBar, "name_872730877438591");
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		
		JButton btnIniciar = new JButton("Iniciar");
		btnIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(!isRunning){
					if (Security.unlock("lmcxkm3p2k39", MainWindow.this, new GlobalScanner(MainWindow.this))) {
						try{
							globalScan.setAnoMinimo(Integer.parseInt(anoMin));
							globalScan.setAnoMaximo(Integer.parseInt(anoMax));
							globalScan.setIndexPrecioMaximo(Integer.parseInt(indexPrecioMax));
							globalScan.setIndexPrecioMinimo(Integer.parseInt(indexPrecioMin));
							isRunning = true;
							runGlobalScanner();
							((JButton)arg0.getSource()).setText("Detener");
						} catch(ParseException e){}
					}
				}
				
				else{
					globalScan.stopScan();
					if(localScan != null)
						localScan.stopScan();
					globalScan = new GlobalScanner(MainWindow.this);
					isRunning = false;
					((JButton)arg0.getSource()).setText("Iniciar");
				}
			}
		});
		
		panelConnection.add(btnIniciar, BorderLayout.WEST);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("Configuraciones");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmBusqueda = new JMenuItem(new AbstractAction("Par\u00E1metros") {
		    
			public void actionPerformed(ActionEvent ae) {
		    	popUpConfig.show();
		    }
		});
		mntmBusqueda.setText("B\u00FAsqueda");
		mnNewMenu.add(mntmBusqueda);
		
		JMenu mnDestinatarios = new JMenu("Destinatarios");
		mnNewMenu.add(mnDestinatarios);
		
		JRadioButtonMenuItem rdbtnmntmSebastin = new JRadioButtonMenuItem("Sebasti\u00E1n");
		mnDestinatarios.add(rdbtnmntmSebastin);
		
		JRadioButtonMenuItem rdbtnmntmGiovanni = new JRadioButtonMenuItem("Giovanni");
		mnDestinatarios.add(rdbtnmntmGiovanni);
		
		JRadioButtonMenuItem rdbtnmntmTodos = new JRadioButtonMenuItem("Todos");
		rdbtnmntmTodos.setSelected(true);
		mnDestinatarios.add(rdbtnmntmTodos);
		
		JMenuItem mntmParametros = new JMenuItem("Par\u00E1metros");
		mnNewMenu.add(mntmParametros);
		
		JMenu mnEjecutar = new JMenu("Ejecutar");
		menuBar.add(mnEjecutar);
		

		JMenuItem mntmActualizarModelos = new JMenuItem(new AbstractAction("Actualizar modelos y marcas") {
		    
			public void actionPerformed(ActionEvent ae) {
				try{
					runBMIndexsUpdate();
				} catch(ParseException e){}
		    }
		});
		
		mnEjecutar.add(mntmActualizarModelos);;

		setConnectionStatus(false);
		
		popUpConfig = new PopUpConfigurations(MainWindow.this);
		globalScan = new GlobalScanner(MainWindow.this);
		
		System.out.println(Herramientas.getLastTimeScanDate());
		System.out.println(Herramientas.getLastTimeScanMinYear());
		System.out.println(Herramientas.getLastTimeScanMaxYear());
		System.out.println(Herramientas.getLastTimeScanMinPriceIndex());
		System.out.println(Herramientas.getLastTimeScanMaxPriceIndex());
		System.out.println(Herramientas.getLastTimeScanShouldRestart());
		
		
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				String ObjButtons[] = {"Si","No"};
		        int PromptResult = JOptionPane.showOptionDialog(null,"Está seguro de que desea salir?","Terminar",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
		        if(PromptResult==JOptionPane.YES_OPTION)
		        {
		            System.exit(0);
		        }
			}
		});
	}

	public Class<?> getColumnClass(int columnIndex) {
	    return Object.class;
	}
	
	public void setConnectionStatus(boolean status){
		
		BufferedImage myPicture;
		try {
			File f;
			if(status)
				f = new File("img/rsz_green-circle.png");
			else
				f = new File("img/rsz_red-circle.png");
			myPicture = ImageIO.read(f);
			lblConnectionStatus.setText("");
			lblConnectionStatus.setIcon(new ImageIcon(myPicture));
		} catch (IOException e) {}		
	}
	
	public void insertNewProgramCurrentState(String _newState, Color _color, boolean _bold){
		synchronized(textPaneLog){
			Document doc = textPaneLog.getDocument();
			SimpleAttributeSet style = new SimpleAttributeSet();
			StyleConstants.setForeground(style, _color);
			StyleConstants.setBold(style, _bold);
			try {
				doc.insertString(doc.getLength(), "\n" + _newState, style);
				JScrollBar vertical = scrollPaneLog.getVerticalScrollBar();
				vertical.setValue( vertical.getMaximum() );
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void insertStatistics(Object[] data) {
		synchronized(tableStatistics){
			try {
				DefaultTableModel model = (DefaultTableModel) tableStatistics.getModel();
				model.addRow(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void insertRecomended(Object[] data) {
		synchronized(tableRecomended){
			try {
				DefaultTableModel model = (DefaultTableModel) tableRecomended.getModel();
				model.addRow(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void runBMIndexsUpdate(){
		progressBar.setVisible(true);
		progressBar.setValue(0);
		lblFaltanMinutos.setVisible(false);
		
		Thread updateBMIndexs = new Thread() {
			@Override
			public void run() {
				try {
					globalScan.fillBrandsAndModelsIndexs();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		updateBMIndexs.start();
	}
	
	public void runGlobalScanner() {
		progressBar.setVisible(true);
		progressBar.setValue(0);
		lblFaltanMinutos.setVisible(false);
		
		scanAll = new Thread() {
			@Override
			public void run() {
				try {
					globalScan.runScan = true;
					globalScan.scanAllYapoCars();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		scanAll.start();
		
		popUpConfig.disable();
	}
	
	public void runLocalScanner() {
		progressBar.setVisible(false);
		lblFaltanMinutos.setVisible(true);
		lblFaltanMinutos.setText("Faltan 30 minutos para un nuevo scan");
		
		localScan = new LocalScanner(globalScan.getData(), globalScan.getStatistics(), MainWindow.this);
		localScan.run();
	}
		
	public void setProgress(int currentProgress){
		synchronized(progressBar){
			progressBar.setValue(currentProgress);
		}
	}
	
	public void setBottomStatusText(String status){
		lblFaltanMinutos.setText(status);
	}
	
}

//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.EventQueue;
//import java.awt.Font;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.IOException;
//
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JSeparator;
//import javax.swing.JSpinner;
//import javax.swing.JTable;
//import javax.swing.JTextArea;
//import javax.swing.JToolBar;
//import javax.swing.SpinnerListModel;
//import javax.swing.UIManager;
//import javax.swing.border.EmptyBorder;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import javax.swing.table.DefaultTableModel;
//
//import com.jgoodies.forms.factories.FormFactory;
//import com.jgoodies.forms.layout.ColumnSpec;
//import com.jgoodies.forms.layout.FormLayout;
//import com.jgoodies.forms.layout.RowSpec;
//
//public class MainWindow {
//
//	private JFrame frame;
//	public static JTextArea TA_EstadoActual;
//	public static JTable TB_Estadisticas;
//	public static JTable TB_Recomendados;
//	public static JSpinner SR_AnoMin;
//	public static JSpinner SR_AnoMax;
//	public static JSpinner SR_PrecioMin;
//	public static JSpinner SR_PrecioMax;
//	public static Integer SR_PrecioMin_SelIndex = 0;
//	public static Integer SR_PrecioMax_SelIndex = 0;
//	public static String[] ListaPreciosMin = new String[] { "0", "100.000", "150.000", "200.000", "250.000", "300.000",
//			"400.000", "500.000", "750.000", "1.000.000", "1.500.000", "2.000.000", "2.500.000", "3.000.000",
//			"4.000.000", "5.000.000", "7.500.000", "10.000.000", "15.000.000", "20.000.000", "25.000.000", "30.000.000",
//			"40.000.000", "50.000.000", "75.000.000" };
//	public static String[] ListaPreciosMax = new String[] { "0", "100.000", "150.000", "200.000", "250.000", "300.000",
//			"400.000", "500.000", "750.000", "1.000.000", "1.500.000", "2.000.000", "2.500.000", "3.000.000",
//			"4.000.000", "5.000.000", "7.500.000", "10.000.000", "15.000.000", "20.000.000", "25.000.000", "30.000.000",
//			"40.000.000", "50.000.000", "75.000.000", "+75.000.000" };
//	public static Thread scanAll;
//	public static boolean isScanningAll = false;
//	public static GlobalScanner globalScan = new GlobalScanner();
//	public static LocalScanner localScan;
//
//	/**
//	 * Launch the application.
//	 */
//	public static void main(String[] args) {
//
//		try 
//	    {
//	      UIManager.setLookAndFeel(new de.javasoft.plaf.synthetica.SyntheticaPlainLookAndFeel());
//	    } 
//	    catch (Exception e) 
//	    {
//	      e.printStackTrace();
//	    }
//		EventQueue.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					MainWindow window = new MainWindow();
//					window.frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//
//		/*
//		 * for(int i=830; i<860; i++){ String url =
//		 * "http://www.yapo.cl/region_metropolitana/autos?ca=15_s&st=s&cg=2020&o="
//		 * +i; String out = wb.getUrlSource(url); if(out.contains(
//		 * "span class=\"price\"")==false) System.out.println("########### "
//		 * +url+" ###########"); else System.out.println(i); }
//		 */
//		// UserInterface ui = new UserInterface();
//
//	}
//
//	/**
//	 * Create the application.
//	 */
//	public MainWindow() {
//		initialize();
//	}
//
//	/**
//	 * Initialize the contents of the frame.
//	 */
//	@SuppressWarnings("serial")
//	private void initialize() {
//		frame = new JFrame();
//		frame.setTitle("Project Yapo");
//		frame.setBounds(100, 100, 800, 600);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//		JToolBar TB_toolbar = new JToolBar();
//		TB_toolbar.setBackground(Color.GRAY);
//		TB_toolbar.setFloatable(false);
//		frame.getContentPane().add(TB_toolbar, BorderLayout.NORTH);
//
//		JButton BT_IniciarScan = new JButton("Iniciar Scan");
//		BT_IniciarScan.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				if(Security.unlock("lmcxkm3p2k39")){
//					if (!MainWindow.isScanningAll) {
//						isScanningAll = true;
//						// TODO Esto se cambio para testear
//						runGlobalScanner();
//						// runLocalScanner();
//					}
//				}
//			}
//		});
//		BT_IniciarScan.setForeground(Color.WHITE);
//		BT_IniciarScan.setBackground(Color.DARK_GRAY);
//		TB_toolbar.add(BT_IniciarScan);
//
//		JButton BT_Detener = new JButton("Detener");
//		BT_Detener.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				globalScan.stopScan();
//				// localScan.stopScan();
//				globalScan = new GlobalScanner();
//				globalScan.setAnoMinimo(Integer.parseInt(SR_AnoMin.getValue().toString()));
//				globalScan.setAnoMaximo(Integer.parseInt(SR_AnoMax.getValue().toString()));
//				globalScan.setIndexPrecioMaximo(getSelectedIndex(SR_PrecioMax, ListaPreciosMax));
//				globalScan.setIndexPrecioMinimo(getSelectedIndex(SR_PrecioMin, ListaPreciosMin));
//				MainWindow.isScanningAll = false;
//			}
//		});
//		BT_Detener.setBackground(Color.DARK_GRAY);
//		BT_Detener.setForeground(Color.WHITE);
//		TB_toolbar.add(BT_Detener);
//
//		JLabel lblAoMin = new JLabel("  A\u00F1o Min:");
//		lblAoMin.setForeground(Color.WHITE);
//		TB_toolbar.add(lblAoMin);
//
//		SR_AnoMin = new JSpinner();
//		SR_AnoMin.addChangeListener(new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent arg0) {
//				globalScan.setAnoMinimo(Integer.parseInt(SR_AnoMin.getValue().toString()));
//			}
//		});
//		SR_AnoMin.setModel(new SpinnerListModel(new String[] { "1960", "1961", "1962", "1963", "1964", "1965", "1966",
//				"1967", "1968", "1969", "1970", "1971", "1972", "1973", "1974", "1975", "1976", "1977", "1978", "1979",
//				"1980", "1981", "1982", "1983", "1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992",
//				"1993", "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003", "2004", "2005",
//				"2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015" }));
//		SR_AnoMin.setValue("2015");
//		TB_toolbar.add(SR_AnoMin);
//
//		JLabel lblAoMax = new JLabel(" A\u00F1o Max:");
//		lblAoMax.setForeground(Color.WHITE);
//		TB_toolbar.add(lblAoMax);
//
//		SR_AnoMax = new JSpinner();
//		SR_AnoMax.addChangeListener(new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent arg0) {
//				globalScan.setAnoMaximo(Integer.parseInt(SR_AnoMax.getValue().toString()));
//			}
//		});
//		SR_AnoMax.setModel(new SpinnerListModel(new String[] { "1960", "1961", "1962", "1963", "1964", "1965", "1966",
//				"1967", "1968", "1969", "1970", "1971", "1972", "1973", "1974", "1975", "1976", "1977", "1978", "1979",
//				"1980", "1981", "1982", "1983", "1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992",
//				"1993", "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003", "2004", "2005",
//				"2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015" }));
//		SR_AnoMax.setValue("2015");
//		TB_toolbar.add(SR_AnoMax);
//
//		JLabel lblPrecioMin = new JLabel(" Precio Min:");
//		lblPrecioMin.setForeground(Color.WHITE);
//		TB_toolbar.add(lblPrecioMin);
//
//		SR_PrecioMin = new JSpinner();
//		SR_PrecioMin.addChangeListener(new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent arg0) {
//				globalScan.setIndexPrecioMinimo(getSelectedIndex(SR_PrecioMin, ListaPreciosMin));
//			}
//		});
//		SR_PrecioMin.setModel(new SpinnerListModel(ListaPreciosMin));
//		SR_PrecioMin.getModel().setValue("1.000.000");
//		TB_toolbar.add(SR_PrecioMin);
//
//		JLabel lblPrecioMax = new JLabel(" Precio Max:");
//		lblPrecioMax.setForeground(Color.WHITE);
//		TB_toolbar.add(lblPrecioMax);
//
//		SR_PrecioMax = new JSpinner();
//		SR_PrecioMax.addChangeListener(new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent arg0) {
//				globalScan.setIndexPrecioMaximo(getSelectedIndex(SR_PrecioMax, ListaPreciosMax));
//			}
//		});
//		SR_PrecioMax.setModel(new SpinnerListModel(ListaPreciosMax));
//		SR_PrecioMax.getModel().setValue("+75.000.000");
//		TB_toolbar.add(SR_PrecioMax);
//
//		JPanel panel = new JPanel();
//		panel.setBorder(new EmptyBorder(0, 0, 10, 10));// arriba,izquierda,abajo,derecha
//		// frame.getContentPane().add(panel, BorderLayout.CENTER);
//		panel.setLayout(
//				new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
//						new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, RowSpec.decode("6dlu"),
//								FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("100dlu"),
//								RowSpec.decode("6dlu"), FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
//								RowSpec.decode("default:grow"), }));
//
//		JScrollPane mainSP = new JScrollPane(panel);
//		frame.getContentPane().add(mainSP, BorderLayout.CENTER);
//
//		JLabel LB_EstadoActual = new JLabel("Estado actual del programa");
//		LB_EstadoActual.setFont(new Font("Tahoma", Font.BOLD, 12));
//		panel.add(LB_EstadoActual, "2, 2");
//
//		JScrollPane SP_EstadoActual = new JScrollPane();
//		panel.add(SP_EstadoActual, "2, 4, fill, fill");
//
//		MainWindow.TA_EstadoActual = new JTextArea();
//		MainWindow.TA_EstadoActual.setRows(8);
//		MainWindow.TA_EstadoActual.setEditable(false);
//		SP_EstadoActual.setViewportView(MainWindow.TA_EstadoActual);
//
//		JSeparator SR_Separador1 = new JSeparator();
//		panel.add(SR_Separador1, "2, 5");
//
//		JLabel LB_Estadisticas = new JLabel("Estadisticas de los resultados encontrados");
//		LB_Estadisticas.setFont(new Font("Tahoma", Font.BOLD, 12));
//		panel.add(LB_Estadisticas, "2, 6");
//
//		JScrollPane SP_Estadisticas = new JScrollPane();
//		panel.add(SP_Estadisticas, "2, 8, fill, fill");
//
//		MainWindow.TB_Estadisticas = new JTable();
//		MainWindow.TB_Estadisticas.setAutoCreateRowSorter(true);
//		MainWindow.TB_Estadisticas.setFillsViewportHeight(true);
//		MainWindow.TB_Estadisticas.setModel(new DefaultTableModel(new Object[][] {},
//				new String[] { "Marca", "Modelo", "A\u00F1o", "Cantidad", "Precio Min", "Precio Max", "Precio Prom" }) {
//			boolean[] columnEditables = new boolean[] { false, false, false, false, false, false, false };
//
//			@Override
//			public boolean isCellEditable(int row, int column) {
//				return columnEditables[column];
//			}
//		});
//
//		MainWindow.TB_Estadisticas.setPreferredScrollableViewportSize(new Dimension(525, 100));
//		SP_Estadisticas.setViewportView(MainWindow.TB_Estadisticas);
//
//		JSeparator SR_Separador2 = new JSeparator();
//		panel.add(SR_Separador2, "2, 9");
//
//		JLabel LB_Nuevos = new JLabel("Recomendados");
//		LB_Nuevos.setFont(new Font("Tahoma", Font.BOLD, 12));
//		panel.add(LB_Nuevos, "2, 10");
//
//		JScrollPane SP_Recomendados = new JScrollPane();
//		panel.add(SP_Recomendados, "2, 12, fill, fill");
//
//		MainWindow.TB_Recomendados = new JTable();
//		MainWindow.TB_Recomendados.setAutoCreateRowSorter(true);
//		MainWindow.TB_Recomendados.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Titulo", "Marca",
//				"Modelo", "Version", "A\u00F1o", "Kilometraje", "Transmision", "Precio", "Link" }) {
//			boolean[] columnEditables = new boolean[] { false, false, false, false, false, false, false, false, false };
//
//			@Override
//			public boolean isCellEditable(int row, int column) {
//				return columnEditables[column];
//			}
//		});
//		MainWindow.TB_Recomendados.setFillsViewportHeight(true);
//		MainWindow.TB_Recomendados.setPreferredScrollableViewportSize(new Dimension(525, 100));
//		SP_Recomendados.setViewportView(MainWindow.TB_Recomendados);
//	}
//
//	public static void runGlobalScanner() {
//
//		EventQueue.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					SR_AnoMin.setEnabled(false);
//					SR_AnoMax.setEnabled(false);
//					SR_PrecioMin.setEnabled(false);
//					SR_PrecioMax.setEnabled(false);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		MainWindow.scanAll = new Thread() {
//			@Override
//			public void run() {
//				try {
//					globalScan.runScan = true;
//					globalScan.scanAllYapoCars();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		};
//
//		MainWindow.scanAll.start();
//	}
//
//	public static void insertNewProgramCurrentState(String _newState, Color color) {
//		final String _newState2 = _newState;
//		final Color color2 = color;
//		EventQueue.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					MainWindow.TA_EstadoActual.setForeground(color2);
//					MainWindow.TA_EstadoActual.append(_newState2 + "\n");
//					MainWindow.TA_EstadoActual.setCaretPosition(MainWindow.TA_EstadoActual.getDocument().getLength());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//
//	}
//
//	public static void insertStatistics(Object[] data) {
//		final Object[] data2 = data;
//		EventQueue.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					DefaultTableModel model = (DefaultTableModel) MainWindow.TB_Estadisticas.getModel();
//					model.addRow(data2);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}
//
//	public static void insertRecomended(Object[] data) {
//		final Object[] data2 = data;
//		EventQueue.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					DefaultTableModel model = (DefaultTableModel) MainWindow.TB_Recomendados.getModel();
//					model.addRow(data2);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}
//
//	public int getSelectedIndex(JSpinner spinner, String[] values) {
//		int index = 0;
//		for (Object o : values) {
//			if (o.equals(spinner.getValue()))
//				return index;
//			index++;
//		}
//		return -1;
//	}
//
//	public static void runLocalScanner() {
//		localScan = new LocalScanner(globalScan.getData(), globalScan.getStatistics(),
//				Integer.parseInt(SR_AnoMin.getValue().toString()), Integer.parseInt(SR_AnoMax.getValue().toString()));
//		localScan.run();
//	}
//
//}
