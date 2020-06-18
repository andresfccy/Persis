package main.java.com.poli.cliente;

import java.net.*;

import javax.swing.JOptionPane;

import main.java.com.poli.servidor.Server;

import java.io.*;

public class Cliente {
	
	/**
	 * Puerto de conexi�n del servidor correspondiente
	 */
	private final static int PUERTO_SERVIDOR = Server.PUERTO_SERVIDOR;
	
	/**
	 * Variable en la que se inicializar� el socket de conexi�n con el servidor
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
	 * Contiene toda la l�gica principal de negocio de la aplicaci�n.
	 */
	public Cliente () {
		try {
			
			// Variable que modela la opci�n seleccionada por el usuario en el men� principal.
			String selOpt = "";
            
			while(selOpt != null && !selOpt.equalsIgnoreCase("0")) {
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
				if(selOpt == null || selOpt.equalsIgnoreCase("0")) { 
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
			ex.printStackTrace();
			JOptionPane.showMessageDialog(
					null, 
					"Error: " + ex.getMessage(), 
					"Ha ocurrido un error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * M�todo que gestiona la opci�n de configuraciones, CRUD de entidades y configuraci�n inicial del sistema
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
						+ "Selecciona una opci�n\n"
						+ "1. Simular un cliente con saldo.\n"
						+ "0. Atr�s");
				if( opt == null ) opt = "";
				
				switch(opt) {
					case "1":
						write.writeUTF("102:simular cliente");
						String res = read.readUTF(); // Esperar a que el servidor responda
						if(res.split(":")[0].equalsIgnoreCase("200"))
							JOptionPane.showMessageDialog(null, "Simulaci�n correcta!");
						else if(res.split(":")[0].equalsIgnoreCase("500")) {
							JOptionPane.showMessageDialog(null, "Ocurri� un error");
							System.out.println(res.split(":")[1]);
						}
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
			ex.printStackTrace();
		}
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
	
	/**
	 * M�todo de prueba para gestionar una conexi�n infinita con el servidor a menos de que se reciba un comando de salida
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
						+ "Selecciona una opci�n\n"
						+ "1. Saludar.\n"
						+ "0. Atr�s");
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
						JOptionPane.showMessageDialog(null, "Opci�n no v�lida");
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