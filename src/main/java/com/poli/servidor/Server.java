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
	 * Query de inserción estandar de un usuario aleatorio, dada su cedula y el id de la ciudad
	 * Se define una interpolación para los datos
	 */	
	private final static String queryUserInsert = ""
			+ "INSERT INTO CLIENTES (IDENTIFICACION, TIPO_DOCUMENTO, NOMBRE, APELLIDO, CIUDAD_ID) "
			+ "VALUES (?, ?, ?, ?, ?)";
	
	/**
	 * Query de la selección estandar de el último valor insertado en una tabla
	 * Se define la interpolación de datos para el nombre de la secuencia de la tabla
	 */
	private final static String queryGetLastUserId = "select clientes_pk.currval from dual";
	
	/**
	 * Query de inserción estandar de una cuenta, dado el id del usuario dueño
	 * Se define una interpolación para el id del cliente
	 */	
	private final static String queryAccountInsert = ""
    		+ "INSERT INTO CUENTAS (CLIENTE_ID) "
    		+ "VALUES (?)";
	
	/**
	 * Variable que modela la conexión con la base de datos
	 */
	private Connection conn;
	
	/**
	 * Constructor de la clase servidor
	 */
	public Server () {
		realizarConexionBD();
		
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
		                	case "102": // 102 Es el código para simular un usuario y su cuenta
		                		simularCreacionUsuario(read, write);
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
					System.out.println("----------------------");
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
	 * Método que realiza los accesos correspondientes a la BD para simular un usuario aleatorio de una ciudad aleatoria
	 * y le crea una cuenta asociada a dicho usuario, se realiza una transacción con la creación del usuario y su asociación a una cuenta nueva.
	 * @param read - Variable que modela la comunicación actual de escritura con el socket cliente que realizó la solicitud
	 * @param write - Variable que modela la comunicación actual del lectura con el socket cliente que realizó la solicitud
	 * @return idUsuarioInsertado - Variable que contiene el ID del usuario insertado en la tabla CLIENTES, si algo falla, devuelve -1 
	 * @throws SQLException
	 * @throws IOException
	 */
	private long simularCreacionUsuario(DataInputStream read, DataOutputStream write) throws SQLException, IOException {
		Random r = new Random();
		int low = 100000000;
		int high = 999999999;
		int id_ciudad = 0;
		ResultSet rs;
		
		int cedula = r.nextInt(high-low) + low;
		escribirLog("Cédula simulada: " + cedula);
		
		low = 1;		// Siempre es el 1 como "cantidad mínima de ciudades
		high = 1100;	// El número máximo de ciudades en la tabla ciudades
		int row_ciudad = r.nextInt(high-low) + low; // Selecciona una fila aleatoria, entre 1 y N ciudades
		escribirLog("Ciudad simulada: " + row_ciudad);
		
		PreparedStatement selectCity = conn.prepareStatement("select id from ( select c.*, rownum r from ciudades c ) where r = ?");
		selectCity.setInt(1, row_ciudad);
		rs = selectCity.executeQuery();
		if(rs.next()) {
			id_ciudad = rs.getInt(1);
		}
		
		PreparedStatement insertUser = null;
		PreparedStatement selectLastIdUser = null;
		PreparedStatement insertAccount = null;
		try {
			// Se establece que no se realice commit automáticamente
			conn.setAutoCommit(false);
			
			// Se prepara el primer query que se va a realizar, con la inserción de un usuario
			insertUser = conn.prepareStatement(queryUserInsert, Statement.RETURN_GENERATED_KEYS);
			insertUser.setInt(1, cedula); 				// Primer parámetro de la interpolación, es la cédula autogenerada
			insertUser.setString(2, "CC"); 				// Segundo parámetro de la interpolación, es el tipo de documento
			insertUser.setString(3,	"Autogen User"); 	// Tercer parámetro de la interpolación, es el nombre del usuario (como es auto generado, definimos un nombre estático)
			insertUser.setString(4,	"A"+cedula); 		// Cuarto parámetro de la interpolación, es el apellido
			insertUser.setInt(5, id_ciudad); 			// Quinto parámetro de la interpolación, es el id de la ciudad (obtenida aleatoriamente entre los ids en las semillas de ciudades)
			// Se ejecuta la inserción
			insertUser.execute();
			
			// Se obtiene el id del usuario insertado, mediante un select a la secuencia que define los ids en la tabla usuarios
			selectLastIdUser = conn.prepareStatement(queryGetLastUserId);
			// Se guarda en un resultset, el resultado del select
			rs = selectLastIdUser.executeQuery();
			
			String idUsuarioInsertado = "";
			// Si el resultset no está vacío, es decir, sí se insertó el usuario
			if (rs.next()) {
				// Asigna el id del usuario insertado a una variable de tipo int
				idUsuarioInsertado = rs.getString(1);
			    // Se prepara el query de inserción de una cuenta al usuario insertado previamente
			    insertAccount = conn.prepareStatement(queryAccountInsert);
			    insertAccount.setString(1, idUsuarioInsertado); // Primer parámetro de la interpolación, es el id del usuario dueño de la cuenta, previamente insertado
			    insertAccount.execute();
			}
			conn.commit();	
			escribirLog("Id de usuario insertado: " + idUsuarioInsertado);
    		write.writeUTF("200:Simulación de usuario y cuenta correctos");
    		return Long.parseLong(idUsuarioInsertado);
		}
		catch(SQLException ex) {
			ex.printStackTrace();
			System.out.println("----------------------");
			if (conn != null) {
	            try {
	                System.err.print("Transaction is being rolled back");
	                conn.rollback();
	            } catch(SQLException excep) {
	                excep.printStackTrace();
	                System.out.println("----------------------");
	            }
	        }
	        
	        write.writeUTF("500:Ocurrió un error\n" + ex.getMessage());
	    } finally {
	        if (insertUser != null) {
	            insertUser.close();
	        }
	        if (selectLastIdUser != null) {
	        	selectLastIdUser.close();
	        }
	        if (insertAccount != null) {
	        	insertAccount.close();
	        }
	        conn.setAutoCommit(true);
	    }
		return -1;
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