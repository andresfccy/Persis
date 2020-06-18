package main.java.com.poli.cliente;

import java.net.*;

import javax.swing.JOptionPane;

import main.java.com.poli.servidor.Server;

import java.io.*;

public class Cliente {
	
	/**
	 * Puerto de conexi�n del servidor correspondiente
	 */
	public final static int PUERTO_SERVIDOR = Server.PUERTO_SERVIDOR;
	
	/**
	 * Constructor de la clase actual.
	 * Contiene toda la l�gica principal de negocio de la aplicaci�n.
	 */
	public Cliente () {
		try {
			
			// Variable que modela la opci�n seleccionada por el usuario en el men� principal.
			String selOpt = "";
            
			while(!selOpt.equalsIgnoreCase("0")) {
				// Se utilizan JOptionPane para simular una interfaz sencilla.
				selOpt = JOptionPane.showInputDialog(null, ""
						+ "Bienvenido a la aplicaci�n de BANCO XYZ.\n"
						+ "�Qu� acci�n desea realizar?\n"
						+ "1. Configuraciones.\n"
						+ "2. Realizar un retiro.\n"
						+ "3. Realizar una consulta.\n"
						+ "4. Say Hello.\n"
						+ "0. Salir.");
				
				// Confirmaci�n de salida
				if(selOpt.equalsIgnoreCase("0")) { 
					if( 0 != JOptionPane.showConfirmDialog(null, "�Est� seguro que desea salir del programa?"))
						selOpt = "";
				} 
				else {						
					switch(selOpt) {
						case "1":
							configuraciones();
							break;
						case "2":
							realizarRetiro();
							break;
						case "3":
							realizarConsulta();
							break;
						case "4":
							sayHello();
							break;
						default:
							selOpt = "";
							JOptionPane.showMessageDialog(null, "Opci�n no v�lida");
							break;						
					}
				}
			}
			JOptionPane.showMessageDialog(null, "�Hasta pronto!");
		}
		catch(Exception ex) {
			JOptionPane.showMessageDialog(
					null, 
					"Error: " + ex.getMessage(), 
					"Ha ocurrido un error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * M�todo que gestiona la opci�n de configuraciones
	 */
	private void configuraciones() {
		
	}
	
	/**
	 * M�todo que gestiona la opci�n de realizar un retiro
	 */
	private void realizarRetiro() {
		
	}
	
	/**
	 * M�todo que gestiona la opci�n de realizar una consulta
	 */
	private void realizarConsulta() {
		
	}
	
	private void sayHello() {
		Socket s;
		DataInputStream read;
		DataOutputStream write;
		try {
			String opt = "";
			s = new Socket("localhost", this.PUERTO_SERVIDOR);
			// Variables de lectura y escritura
			read = new DataInputStream(s.getInputStream()); 
	        write = new DataOutputStream(s.getOutputStream());
	        
			while(!opt.equalsIgnoreCase("0")) {
				opt = JOptionPane.showInputDialog(null, ""
						+ "Selecciona una opci�n\n"
						+ "1. Saludar.\n"
						+ "0. Atr�s");
				switch(opt) {
					case "1":
						write.writeUTF("101:Say Hello");
						read.readUTF(); // Esperar a que el servidor responda
						break;
					case "0":
						write.writeUTF("0:Say Bye");
						read.readUTF(); // Esperar a que el servidor responda
						break;
					default:
						opt = "";
						JOptionPane.showMessageDialog(null, "Opci�n no v�lida");
						break;
				}
			}
			
			read.close();
			write.close();
			s.close();
		}
		catch(IOException ex) {
			System.out.println("ERROR:\n"+ex.getMessage());
		}
	}
	
	/**
	 * Clase principal y punto de entrada del programa
	 */
	public static void main(String[] args) {
		new Cliente();
	}
}