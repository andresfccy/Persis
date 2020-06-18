package main.java.com.poli.servidor;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JOptionPane;

import oracle.jdbc.pool.OracleDataSource;

public class Server {
	/**
	 * Puerto que se utilizará para abrir el socket servidor
	 */
	public final static int PUERTO_SERVIDOR = 4999;

	/**
	 * Url de conexión a la base de datos correspondiente
	 */
	private final static String url = "jdbc:oracle:thin:@localhost:1521:XE";
	
	/**
	 * Usuario del esquema seleccionado de la base de datos
	 */
	private final static String user = "banco";
	
	/**
	 * Contraseña del usuario de la base de datos 
	 */
	private final static String password = "poli1234";
	
	/**
	 * Variable que modela la conexión con la base de datos
	 */
	private Connection conn;
	
	/**
	 * Constructor de la clase servidor
	 */
	public Server () {
		// realizarConexionBD();
		
		abrirConexion();
	}
	
	/**
	 * Método que abre un socket servidor en el puerto correspondiente
	 */
	private void abrirConexion() {
		ServerSocket ss;
		Socket s;
		DataInputStream read;
		DataOutputStream write;
		String req = "";
		try {
			// Se establecen las configuraciones del socket servidor en el puerto configurado
			ss = new ServerSocket(this.PUERTO_SERVIDOR);
			escribirLog("Abriendo socket servidor en el puerto " + this.PUERTO_SERVIDOR);

			// Se establece un ciclo infinito para que el servidor quede escuchando siempre que esté corriendo
			while(true) {
				s = null;
				try {
					// Se establece el socket para que acepte una conexión
					s = ss.accept();
					escribirLog("Se ha conectado un nuevo cliente: " + s);
					
					// Variables de lectura y escritura
					read = new DataInputStream(s.getInputStream()); 
	                write = new DataOutputStream(s.getOutputStream());
					
	                while(!req.equalsIgnoreCase("exit")) {
		                req = read.readUTF();
		                
		                // Se establece como comunicación, que los códigos entre cliente y servidor, van definidos
		                //  como la primera parte de un string separado por ":"
		                escribirLog(req);
		                switch(req.split(":")[0]) {
		                	case "101": // 101 Es un saludo del cliente
		                		write.writeUTF("200:Hello");
		                		break;
		                	case "0": // 0 Es una despedida del cliente
		                		write.writeUTF("200:bye");
		                		req = "exit";
		                		break;
		                	default:
		                		write.writeUTF("403:Bad Request, conection aborted.");
		                		req = "exit";
		                		break;
		                }
	                }
	                req = "";
	                read.close();
	                write.close();
	                s.close();
				}
				catch(Exception ex) {
					s.close();
					ex.printStackTrace();
				}
			}
		}
		catch(IOException ex) {
			
			JOptionPane.showMessageDialog(
					null, 
					"Error: " + ex.getMessage(), 
					"Ha ocurrido un error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Método que maneja la escritura en consola del servidor
	 */
	private void escribirLog(String message) {
		System.out.println(new SimpleDateFormat("yyy-MM-dd hh:mm:ss").format(new Timestamp(System.currentTimeMillis())) + " - " + message);  
	}
	
	/**
	 * Método que realiza la conexión a la Base de Datos
	 */
	private void realizarConexionBD() {
		try {
			// Se instancia el datasource de la librería
			OracleDataSource dataSource = new OracleDataSource();
	
			// Se configura la url de conexión al XE local
			dataSource.setURL(url);
	
			// Se establece la conexión con los parámetros correspondientes de usuario y password
			conn = dataSource.getConnection(user, password);
		}
		catch(SQLException ex) {
			JOptionPane.showMessageDialog(null, "Ocurrió un error al intentar conectarse a la BD:\n" + ex.getMessage(), "BD Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Método main del servidor, y punto de entrada de la aplicación
	 * @param args
	 */
	public static void main(String[] args) {
		new Server();
	}
}