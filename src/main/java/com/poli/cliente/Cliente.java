package main.java.com.poli.cliente;

import java.net.*;

import javax.swing.JOptionPane;

import main.java.com.poli.servidor.Server;

import java.io.*;

public class Cliente {
	
	/**
	 * Puerto de conexión del servidor correspondiente
	 */
	public final static int PUERTO_SERVIDOR = Server.PUERTO_SERVIDOR;
	
	/**
	 * Constructor de la clase actual.
	 * Contiene toda la lógica principal de negocio de la aplicación.
	 */
	public Cliente () {
		try {
			
			// Variable que modela la opción seleccionada por el usuario en el menú principal.
			String selOpt = "";
            
			while(!selOpt.equalsIgnoreCase("0")) {
				// Se utilizan JOptionPane para simular una interfaz sencilla.
				selOpt = JOptionPane.showInputDialog(null, ""
						+ "Bienvenido a la aplicación de BANCO XYZ.\n"
						+ "¿Qué acción desea realizar?\n"
						+ "1. Configuraciones.\n"
						+ "2. Realizar un retiro.\n"
						+ "3. Realizar una consulta.\n"
						+ "4. Say Hello.\n"
						+ "0. Salir.");
				
				// Confirmación de salida
				if(selOpt.equalsIgnoreCase("0")) { 
					if( 0 != JOptionPane.showConfirmDialog(null, "¿Está seguro que desea salir del programa?"))
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
							JOptionPane.showMessageDialog(null, "Opción no válida");
							break;						
					}
				}
			}
			JOptionPane.showMessageDialog(null, "¡Hasta pronto!");
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
	 * Método que gestiona la opción de configuraciones
	 */
	private void configuraciones() {
		
	}
	
	/**
	 * Método que gestiona la opción de realizar un retiro
	 */
	private void realizarRetiro() {
		
	}
	
	/**
	 * Método que gestiona la opción de realizar una consulta
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
						+ "Selecciona una opción\n"
						+ "1. Saludar.\n"
						+ "0. Atrás");
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
						JOptionPane.showMessageDialog(null, "Opción no válida");
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