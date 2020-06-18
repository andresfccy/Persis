package main.java.com.poli.cliente;

import java.net.*;

import javax.swing.JOptionPane;

import main.java.com.poli.servidor.Server;

import java.io.*;

public class Cliente {
	
	/**
	 * Puerto de conexión del servidor correspondiente
	 */
	private final static int PUERTO_SERVIDOR = Server.PUERTO_SERVIDOR;
	
	/**
	 * Variable en la que se inicializará el socket de conexión con el servidor
	 */
	private Socket s;
	
	/**
	 * Variable que se usa para leer todo mensaje recibido del servidor
	 */
	private DataInputStream read;
	
	/**
	 * Variable que se usa para escribir los mensajes al servidor
	 */
	private DataOutputStream write;
	
	/**
	 * Constructor de la clase actual.
	 * Contiene toda la lógica principal de negocio de la aplicación.
	 */
	public Cliente () {
		try {
			
			// Variable que modela la opción seleccionada por el usuario en el menú principal.
			String selOpt = "";
            
			while(selOpt != null && !selOpt.equalsIgnoreCase("0")) {
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
				if(selOpt == null || selOpt.equalsIgnoreCase("0")) { 
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
			ex.printStackTrace();
			JOptionPane.showMessageDialog(
					null, 
					"Error: " + ex.getMessage(), 
					"Ha ocurrido un error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Método que gestiona la opción de configuraciones, CRUD de entidades y configuración inicial del sistema
	 */
	private void configuraciones() {
		try {
			String opt = "";
			s = new Socket("localhost", this.PUERTO_SERVIDOR);
			// Variables de lectura y escritura
			read = new DataInputStream(s.getInputStream()); 
	        write = new DataOutputStream(s.getOutputStream());
	        
			while(opt != null && !opt.equalsIgnoreCase("0")) {
				opt = JOptionPane.showInputDialog(null, ""
						+ "Selecciona una opción\n"
						+ "1. Simular un cliente con saldo.\n"
						+ "0. Atrás");
				if( opt == null ) opt = "";
				
				switch(opt) {
					case "1":
						write.writeUTF("102:simular cliente");
						String res = read.readUTF(); // Esperar a que el servidor responda
						if(res.split(":")[0].equalsIgnoreCase("200"))
							JOptionPane.showMessageDialog(null, "Simulación correcta!");
						else if(res.split(":")[0].equalsIgnoreCase("500")) {
							JOptionPane.showMessageDialog(null, "Ocurrió un error");
							System.out.println(res.split(":")[1]);
						}
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
			ex.printStackTrace();
		}
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
	
	/**
	 * Método de prueba para gestionar una conexión infinita con el servidor a menos de que se reciba un comando de salida
	 */
	private void sayHello() {
		try {
			String opt = "";
			s = new Socket("localhost", this.PUERTO_SERVIDOR);
			// Variables de lectura y escritura
			read = new DataInputStream(s.getInputStream()); 
	        write = new DataOutputStream(s.getOutputStream());
	        
			while(opt != null && !opt.equalsIgnoreCase("0")) {
				opt = JOptionPane.showInputDialog(null, ""
						+ "Selecciona una opción\n"
						+ "1. Saludar.\n"
						+ "0. Atrás");
				if( opt == null ) opt = "";
				
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
			ex.printStackTrace();
		}
	}
	
	/**
	 * Clase principal y punto de entrada del programa
	 */
	public static void main(String[] args) {
		new Cliente();
	}
}