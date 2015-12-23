import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JComboBox;
import javax.swing.JButton;

import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;

import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;

public class PopUpConfigurations implements ItemListener {

	private JFrame frame;
	
	private JComboBox CB_PrecioMin;
	private JComboBox CB_PrecioMax;
	private JComboBox CB_AnoMin;
	private JComboBox CB_AnoMax;
	private JButton BT_GuardarConf;
	
	private boolean isFirstTime = true;
		
	private JCheckBox chckbxTodas;
	private ArrayList<JCheckBox> checkBoxesList = new ArrayList<JCheckBox>();
	
	private MainWindow context;
	
	
	
	public void show(){
		
		frame.setVisible(true);
		
		CB_PrecioMax.setSelectedIndex(Integer.parseInt(context.indexPrecioMax));
		CB_PrecioMin.setSelectedIndex(Integer.parseInt(context.indexPrecioMin));
		
		for(int i=0; i<Constants.LISTA_ANOS.length;i++){
			if(Constants.LISTA_ANOS[i].equals(context.anoMax)){
				CB_AnoMax.setSelectedIndex(i);
				break;
			}
		}
		
		for(int i=0; i<Constants.LISTA_ANOS.length;i++){
			if(Constants.LISTA_ANOS[i].equals(context.anoMin)){
				CB_AnoMin.setSelectedIndex(i);
				break;
			}
		}
	}
	
	/**
	 * Create the application.
	 */
	public PopUpConfigurations(MainWindow mainWindow) {
		context = mainWindow;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */	
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 388, 473);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("Configuracion de Parámetros");
		
		JLabel lblPrecioMnimo = new JLabel("Precio M\u00EDnimo:");
		
		JLabel lblPrecioMximo = new JLabel("Precio M\u00E1ximo:");
		
		JLabel lblAoMnimo = new JLabel("A\u00F1o M\u00EDnimo:");
		
		JLabel lblAoMximo = new JLabel("A\u00F1o M\u00E1ximo:");
		
		CB_PrecioMin = new JComboBox(Constants.LISTA_PRECIOS_MIN);
				
		CB_PrecioMax = new JComboBox(Constants.LISTA_PRECIOS_MAX);
			
		CB_PrecioMin.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	int preciomin = Integer.parseInt(CB_PrecioMin.getSelectedItem().toString().replace(".", "").trim());
		    	int preciomax = Integer.parseInt(CB_PrecioMax.getSelectedItem().toString().replace(".", "").trim());
		        if(preciomin > preciomax){
		        	CB_PrecioMin.setSelectedIndex(CB_PrecioMax.getSelectedIndex());
		        }
		    }
		});
		
		CB_PrecioMax.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	int preciomin = Integer.parseInt(CB_PrecioMin.getSelectedItem().toString().replace(".", "").trim());
		    	int preciomax = Integer.parseInt(CB_PrecioMax.getSelectedItem().toString().replace(".", "").trim());
		        if(preciomin > preciomax){
		        	CB_PrecioMax.setSelectedIndex(CB_PrecioMin.getSelectedIndex());
		        }
		    }
		});
		
		CB_AnoMin = new JComboBox(Constants.LISTA_ANOS);
		
		CB_AnoMax = new JComboBox(Constants.LISTA_ANOS);
		
		CB_AnoMin.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		        if(CB_AnoMin.getSelectedIndex() > CB_AnoMax.getSelectedIndex()){
		        	CB_AnoMin.setSelectedIndex(CB_AnoMax.getSelectedIndex());
		        }
		    }
		});
		
		CB_AnoMax.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		        if(CB_AnoMin.getSelectedIndex() > CB_AnoMax.getSelectedIndex()){
		        	CB_AnoMax.setSelectedIndex(CB_AnoMin.getSelectedIndex());
		        }
		    }
		});
				
		BT_GuardarConf = new JButton("Guardar");
		BT_GuardarConf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				context.indexPrecioMin = ""+CB_PrecioMin.getSelectedIndex();
				context.indexPrecioMax = ""+CB_PrecioMax.getSelectedIndex();
				context.anoMin = CB_AnoMin.getSelectedItem().toString();
				context.anoMax = CB_AnoMax.getSelectedItem().toString();
				for(int i=0; i<context.regiones.length;i++){
					context.regiones[i] = checkBoxesList.get(i).isSelected();
				}
				frame.setVisible(false);
				//frame.hide();
			}
		});
		
		JLabel lblBsqueda = new JLabel("B\u00FAsqueda:");
		lblBsqueda.setFont(new Font("Tahoma", Font.BOLD, 13));
		
		JLabel lblRegiones = new JLabel("Regiones:");
		
		JScrollPane scrollPane = new JScrollPane();
		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblRegiones)
								.addComponent(lblPrecioMximo)
								.addComponent(lblPrecioMnimo)
								.addComponent(lblAoMnimo)
								.addComponent(lblAoMximo))
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(18)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(CB_AnoMax, 0, 240, Short.MAX_VALUE)
										.addComponent(CB_PrecioMin, 0, 240, Short.MAX_VALUE)
										.addComponent(CB_PrecioMax, 0, 240, Short.MAX_VALUE)
										.addComponent(CB_AnoMin, 0, 240, Short.MAX_VALUE)))
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(20)
									.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)))
							.addGap(0))
						.addComponent(lblBsqueda)
						.addComponent(BT_GuardarConf, GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(25)
					.addComponent(lblBsqueda)
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPrecioMnimo)
						.addComponent(CB_PrecioMin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPrecioMximo)
						.addComponent(CB_PrecioMax, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAoMnimo)
						.addComponent(CB_AnoMin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblAoMximo)
						.addComponent(CB_AnoMax, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblRegiones)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(BT_GuardarConf)
					.addContainerGap())
		);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		chckbxTodas = new JCheckBox("Todas");
		chckbxTodas.addItemListener(this);
		panel.add(chckbxTodas);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("RM - Regi\u00F3n Metropolitana");
		panel.add(chckbxNewCheckBox);
		
		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("XV - Arica y Parinacota");
		panel.add(chckbxNewCheckBox_1);
		
		JCheckBox chckbxNewCheckBox_2 = new JCheckBox("I - Tarapac\u00E1");
		panel.add(chckbxNewCheckBox_2);
		
		JCheckBox chckbxNewCheckBox_3 = new JCheckBox("II - Antofagasta");
		panel.add(chckbxNewCheckBox_3);
		
		JCheckBox chckbxNewCheckBox_4 = new JCheckBox("III - Atacama");
		panel.add(chckbxNewCheckBox_4);
		
		JCheckBox chckbxNewCheckBox_5 = new JCheckBox("IV - Coquimbo");
		panel.add(chckbxNewCheckBox_5);
		
		JCheckBox chckbxNewCheckBox_6 = new JCheckBox("V - Valparaiso");
		panel.add(chckbxNewCheckBox_6);
		
		JCheckBox chckbxNewCheckBox_7 = new JCheckBox("VI - O'Higgins");
		panel.add(chckbxNewCheckBox_7);
		
		JCheckBox chckbxNewCheckBox_8 = new JCheckBox("VII - Maule");
		panel.add(chckbxNewCheckBox_8);
		
		JCheckBox chckbxNewCheckBox_9 = new JCheckBox("VIII - Biob\u00EDo");
		panel.add(chckbxNewCheckBox_9);
		
		JCheckBox chckbxNewCheckBox_10 = new JCheckBox("IX - Araucan\u00EDa");
		panel.add(chckbxNewCheckBox_10);
		
		JCheckBox chckbxNewCheckBox_11 = new JCheckBox("XIV - Los R\u00EDos");
		panel.add(chckbxNewCheckBox_11);
		
		JCheckBox chckbxNewCheckBox_12 = new JCheckBox("X - Los Lagos");
		panel.add(chckbxNewCheckBox_12);
		
		JCheckBox chckbxNewCheckBox_13 = new JCheckBox("XI - Ais\u00E9n");
		panel.add(chckbxNewCheckBox_13);
		
		JCheckBox chckbxNewCheckBox_14 = new JCheckBox("XII - Magallanes & Ant\u00E1rtica");
		panel.add(chckbxNewCheckBox_14);
		frame.getContentPane().setLayout(groupLayout);
		
		checkBoxesList.add(chckbxNewCheckBox);
		checkBoxesList.add(chckbxNewCheckBox_1);
		checkBoxesList.add(chckbxNewCheckBox_2);
		checkBoxesList.add(chckbxNewCheckBox_3);
		checkBoxesList.add(chckbxNewCheckBox_4);
		checkBoxesList.add(chckbxNewCheckBox_5);
		checkBoxesList.add(chckbxNewCheckBox_6);
		checkBoxesList.add(chckbxNewCheckBox_7);
		checkBoxesList.add(chckbxNewCheckBox_8);
		checkBoxesList.add(chckbxNewCheckBox_9);
		checkBoxesList.add(chckbxNewCheckBox_10);
		checkBoxesList.add(chckbxNewCheckBox_11);
		checkBoxesList.add(chckbxNewCheckBox_12);
		checkBoxesList.add(chckbxNewCheckBox_13);
		checkBoxesList.add(chckbxNewCheckBox_14);
		
		boolean shouldSel = true;
		for(int i=0; i<context.regiones.length; i++){
			if(context.regiones[i]){
				checkBoxesList.get(i).setSelected(true);
			}
			else{
				shouldSel = false;
			}
		}
		if(shouldSel)
			chckbxTodas.setSelected(true);
		
		
	}
	
	public void itemStateChanged(ItemEvent ie) {
		
		if(ie.getStateChange() == ItemEvent.SELECTED){
			for(JCheckBox check: checkBoxesList){
				check.setSelected(true);
			}
		}
		else{
			for(JCheckBox check: checkBoxesList){
				check.setSelected(false);
			}
		}
	}

	public void disable(){
		try{
			CB_PrecioMin.setEnabled(false);
			CB_PrecioMax.setEnabled(false);
			CB_AnoMin.setEnabled(false);
			CB_AnoMax.setEnabled(false);
			BT_GuardarConf.setEnabled(false);
			chckbxTodas.setEnabled(false);
			
			for(JCheckBox box : checkBoxesList){
				box.setEnabled(false);
			}
		}catch(NullPointerException e){
			
		}
	}
	
	public void enable(){
		try{
			CB_PrecioMin.setEnabled(true);
			CB_PrecioMax.setEnabled(true);
			CB_AnoMin.setEnabled(true);
			CB_AnoMax.setEnabled(true);
			BT_GuardarConf.setEnabled(true);
			chckbxTodas.setEnabled(true);
			
			for(JCheckBox box : checkBoxesList){
				box.setEnabled(true);
			}
		}catch(NullPointerException e){
			
		}
	}
}
