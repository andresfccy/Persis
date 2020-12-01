package main.java.com.poli.sockets;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JOptionPane;

import main.java.com.poli.entidades.ClienteBanco;
import oracle.jdbc.pool.OracleDataSource;

public class SocketServer {
	/**
	 * Puerto que se utilizar� para abrir el socket servidor
	 */
	public final static int PUERTO_SERVIDOR = 4999;

	/**
	 * Url de conexi�n a la base de datos correspondiente
	 */
	private final static String url = "jdbc:oracle:thin:@localhost:1521:XE";
	
	/**
	 * Usuario del esquema seleccionado de la base de datos
	 */
	private final static String user = "banco";
	
	/**
	 * Contrase�a del usuario de la base de datos 
	 */
	private final static String password = "poli1234";
	
	/**
	 * Query de inserci�n estandar de un usuario aleatorio, dada su cedula y el id de la ciudad
	 * Se define una interpolaci�n para los datos
	 */	
	private final static String queryUserInsert = ""
			+ "INSERT INTO CLIENTES (IDENTIFICACION, TIPO_DOCUMENTO, NOMBRE, APELLIDO, CIUDAD_ID) "
			+ "VALUES (?, ?, ?, ?, ?)";
	
	/**
	 * Query de inserci�n estandar de una cuenta, dado el id del usuario due�o
	 * Se define una interpolaci�n para el id del cliente
	 */	
	private final static String queryAccountInsert = ""
    		+ "INSERT INTO CUENTAS (CLIENTE_ID) "
    		+ "VALUES (?)";
	
	/**
	 * Query de inserci�n estandar de una cuenta, dado el id del usuario due�o
	 * Se define una interpolaci�n para el id del cliente
	 */	
	private final static String queryBalanceInsert = ""
    		+ "INSERT INTO SALDOS (CUENTA_ID, SALDO) "
    		+ "VALUES (?,?)";
	
	/**
	 * Query de la selecci�n estandar de el �ltimo valor insertado en la tabla de clientes
	 */
	private final static String queryGetLastUserId = "select clientes_pk.currval from dual";
	
	/**
	 * Query de la selecci�n estandar de el �ltimo valor insertado en la tabla de cuentas
	 */
	private final static String queryGetLastAccountId = "select cuentas_pk.currval from dual";
	
	/**
	 * Variable que modela la conexi�n con la base de datos
	 */
	private Connection conn;
	
	/**
	 * Constructor de la clase servidor
	 */
	public SocketServer () {
		realizarConexionBD();
		
		abrirConexion();
	}
	
	/**
	 * M�todo que abre un socket servidor en el puerto correspondiente
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

			// Se establece un ciclo infinito para que el servidor quede escuchando siempre que est� corriendo
			while(true) {
				s = null;
				try {
					// Se establece el socket para que acepte una conexi�n
					s = ss.accept();
					escribirLog("Se ha conectado un nuevo cliente: " + s);
					
					// Variables de lectura y escritura
					read = new DataInputStream(s.getInputStream()); 
	                write = new DataOutputStream(s.getOutputStream());
					
	                while(!req.equalsIgnoreCase("exit")) {
		                req = read.readUTF();
		                
		                // Se establece como comunicaci�n, que los c�digos entre cliente y servidor, van definidos
		                //  como la primera parte de un string separado por ":"
		                escribirLog(req);
		                switch(req.split(":")[0]) {
		                	case "101": // 101 Es un saludo del cliente
		                		write.writeUTF("200:Hello");
		                		break;
		                	case "102": // 102 Es el c�digo para simular un usuario y su cuenta
		                		simularCreacionUsuario(read, write);
		                		break;
		                	case "103": // 103 Es el c�digo consultar los usuarios registrados en el sistema
		                		consultarUsuarios(read, write, Integer.parseInt(req.split(":")[1]));
		                		break;
		                	case "104": // 104 Es el c�digo consultar el saldo de un usuario
		                		consultarSaldoUsuario(read, write, Integer.parseInt(req.split(":")[1]));
		                		break;
		                	case "105": // 105 Realizar retiro cuenta
		                		retirarSaldoCuenta(read, write, Integer.parseInt(req.split(":")[1]), Double.parseDouble(req.split(":")[2]));
		                		break;
		                	case "106": // 106 Crear cuenta a un cliente
		                		crearCuentaCliente(read, write, Integer.parseInt(req.split(":")[1]), Double.parseDouble(req.split(":")[2]));
		                		break;
		                	case "107": // 107 Agregar saldo a una cuenta de un cliente
		                		agregarSaldoCuenta(read, write, Integer.parseInt(req.split(":")[1]), Double.parseDouble(req.split(":")[2]));
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
	 * M�todo que permite agregar saldo a una cuenta existente
	 * @param read
	 * @param write
	 * @param parseInt
	 * @param parseDouble
	 * @throws IOException
	 * @throws SQLException
	 */
	private void agregarSaldoCuenta(DataInputStream read, DataOutputStream write, int id_cuenta, double saldoConsignar) throws IOException, SQLException {
		ResultSet rs;
		
		PreparedStatement selectBalance = null;
		PreparedStatement updateBalance = null;
		try {			
			// Se prepara el primer query que se va a realizar, con la actualizaci�n del saldo
			selectBalance = conn.prepareStatement(""
					+ "select s.saldo "
					+ "from  saldos s "
					+ "where s.cuenta_id = ?");
			selectBalance.setInt(1, id_cuenta); 				// Primer par�metro de la interpolaci�n, es el id la cuenta a consultar
			// Se ejecuta la consulta
			rs = selectBalance.executeQuery();
			double saldoActual = 0;
			if (rs.next()) {
				// Se obtiene el saldo actual
				saldoActual = rs.getDouble(1);
			    // Se prepara el query de actualizaci�n del saldo
				updateBalance = conn.prepareStatement("update saldos set saldo = ? where cuenta_id = ?");
			    updateBalance.setDouble(1, (saldoActual + saldoConsignar));	// primer par�metro de la interpolaci�n, es el saldo final despu�s de la consignaci�n
			    updateBalance.setInt(2, id_cuenta); 						// segundo par�metro de la interpolaci�n, es el id de la cuenta a la que pertenece el saldo
			    updateBalance.execute();
			}
			else {
				// Se prepara el query de creaci�n del saldo
				updateBalance = conn.prepareStatement("insert into saldos(saldo, cuenta_id) values(?,?");
			    updateBalance.setDouble(1, (saldoConsignar));	// primer par�metro de la interpolaci�n, es el saldo final despu�s de la consignaci�n
			    updateBalance.setInt(2, id_cuenta); 			// segundo par�metro de la interpolaci�n, es el id de la cuenta a la que pertenece el saldo
			    updateBalance.execute();
			}
			
			escribirLog("Consignaci�n exitosa, saldo final: " + (saldoActual + saldoConsignar));
    		write.writeUTF("200:Consignaci�n exitosa;" + (saldoActual + saldoConsignar));
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
	        
	        write.writeUTF("500:Ocurri� un error\n" + ex.getMessage());
	    } finally {
	        if (selectBalance != null) {
	        	selectBalance.close();
	        }
	        if (updateBalance != null) {
	        	updateBalance.close();
	        }
	        conn.setAutoCommit(true);
	    }
	}

	/**
	 * M�todo que permite retirar un saldo de una cuenta existente.
	 * @param read
	 * @param write
	 * @param id_usuario
	 * @param saldoRetiro
	 * @throws IOException
	 * @throws SQLException
	 */
	private void retirarSaldoCuenta(DataInputStream read, DataOutputStream write, int id_cuenta, double saldoRetiro) throws IOException, SQLException {
		ResultSet rs;
		
		PreparedStatement selectBalance = null;
		PreparedStatement updateBalance = null;
		try {			
			// Se prepara el primer query que se va a realizar, con la actualizaci�n del saldo
			selectBalance = conn.prepareStatement(""
					+ "select s.saldo "
					+ "from  saldos s "
					+ "where s.cuenta_id = ?");
			selectBalance.setInt(1, id_cuenta); 				// Primer par�metro de la interpolaci�n, es el id la cuenta a consultar
			// Se ejecuta la consulta
			rs = selectBalance.executeQuery();
			double saldoActual = 0;
			if (rs.next()) {
				// Se obtiene el saldo actual
				saldoActual = rs.getDouble(1);
				if(saldoActual < saldoRetiro) {
					escribirLog("No hay suficiente saldo para realizar un retiro.\n"
							+ "Saldo actual: " + saldoActual);
		    		write.writeUTF("500:Saldo insuficiente;" + saldoActual);
				} 
				else {
				    // Se prepara el query de actualizaci�n del saldo
					updateBalance = conn.prepareStatement("update saldos set saldo = ? where cuenta_id = ?");
				    updateBalance.setDouble(1, (saldoActual - saldoRetiro));	// primer par�metro de la interpolaci�n, es el saldo final despu�s del retiro
				    updateBalance.setInt(2, id_cuenta); 						// segundo par�metro de la interpolaci�n, es el id de la cuenta a la que pertenece el saldo
				    updateBalance.execute();

					escribirLog("Retiro exitoso, saldo final: " + (saldoActual - saldoRetiro));
		    		write.writeUTF("200:Retiro exitoso;" + (saldoActual - saldoRetiro));
				}
			}
			else {
				escribirLog("No hay suficiente saldo para realizar un retiro.\n"
						+ "Saldo actual: " + saldoActual);
	    		write.writeUTF("500:Saldo insuficiente;" + saldoActual);
			}
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
	        
	        write.writeUTF("500:Ocurri� un error\n" + ex.getMessage());
	    } finally {
	        if (selectBalance != null) {
	        	selectBalance.close();
	        }
	        if (updateBalance != null) {
	        	updateBalance.close();
	        }
	        conn.setAutoCommit(true);
	    }
	}
	
	/**
	 * M�todo que crea una cuenta nueva de un cliente con un saldo inicial, si el saldo inicial.
	 * @param read
	 * @param write
	 * @param id_usuario
	 * @param saldoCreacion
	 * @throws IOException
	 * @throws SQLException
	 */
	private void crearCuentaCliente(DataInputStream read, DataOutputStream write, int id_usuario, double saldoCreacion) throws IOException, SQLException {
		ResultSet rs;
		
		PreparedStatement insertAccount = null;
		PreparedStatement selectLastIdAccount = null;
		PreparedStatement insertBalance = null;
		try {
			// Se establece que no se realice commit autom�ticamente
			conn.setAutoCommit(false);
			
			// Se prepara el primer query que se va a realizar, con la inserci�n de una cuenta
			insertAccount = conn.prepareStatement(queryAccountInsert, Statement.RETURN_GENERATED_KEYS);
			insertAccount.setInt(1, id_usuario); 				// Primer par�metro de la interpolaci�n, es el id del cliente al que se le quiere asociar la cuenta
			// Se ejecuta la inserci�n
			insertAccount.execute();
			
			// Se obtiene el id de la cuenta insertado, mediante un select a la secuencia que define los ids en la tabla cuentas
			selectLastIdAccount = conn.prepareStatement(queryGetLastAccountId);
			// Se guarda en un resultset, el resultado del select
			rs = selectLastIdAccount.executeQuery();
			
			String idCuentaInsertada = "";
			// Si el resultset no est� vac�o, es decir, s� se insert� la cuenta
			if (rs.next()) {
				// Asigna el id de la cuenta insertada a una variable de tipo int
				idCuentaInsertada = rs.getString(1);
			    // Se prepara el query de inserci�n de un saldo a la cuenta insertada previamente
			    insertBalance = conn.prepareStatement(queryBalanceInsert);
			    insertBalance.setString(1, idCuentaInsertada); 	// Primer par�metro de la interpolaci�n, es el id de la cuenta, previamente insertada
			    insertBalance.setDouble(2, saldoCreacion); 		// Segundo par�metro de la interpolaci�n, es el saldo con el que se crea la cuenta
			    insertBalance.execute();
			}
			conn.commit();	
			escribirLog("Id de la cuenta creada, n�mero de cuenta: " + idCuentaInsertada);
    		write.writeUTF("200:Creaci�n de cuenta correcta.;" + idCuentaInsertada);
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
	        
	        write.writeUTF("500:Ocurri� un error\n" + ex.getMessage());
	    } finally {
	        if (insertAccount != null) {
	            insertAccount.close();
	        }
	        if (selectLastIdAccount != null) {
	        	selectLastIdAccount.close();
	        }
	        if (insertBalance != null) {
	        	insertBalance.close();
	        }
	        conn.setAutoCommit(true);
	    }
	}

	/**
	 * M�todo que consulta el saldo de un usuario.
	 * @param read - Variable que modela la comunicaci�n actual de escritura con el socket cliente que realiz� la solicitud
	 * @param write - Variable que modela la comunicaci�n actual del lectura con el socket cliente que realiz� la solicitud
	 * @param id_usuario - Id en la base de datos del cliente del que se va a consultar la informaci�n
	 * @throws IOException
	 * @throws SQLException
	 */
	private void consultarSaldoUsuario(DataInputStream read, DataOutputStream write, long id_usuario) throws IOException, SQLException {
		ResultSet rs;
		String response = "";
		PreparedStatement selectBalance = null;
		try {
			selectBalance = conn.prepareStatement(""
					+ "select NVL(c.id,0) numero_cuenta, NVL(s.saldo,0) saldo " 
					+ "from clientes cl " 
					+ "left join cuentas c on c.cliente_id = cl.id "
					+ "left join saldos s on c.id = s.cuenta_id "
					+ "where cl.id = ?");
			selectBalance.setLong(1, id_usuario);
			
			rs = selectBalance.executeQuery();
			while (rs.next()) {
				escribirLog("Numero cuenta: " + rs.getLong(1) + " Saldo:" + rs.getDouble(2));
				if(rs.getLong(1) != 0)
					response += rs.getLong(1) + ";" + rs.getDouble(2) + "$";
				else
					response += "Este cliente no tiene una cuenta creada.";
			}
			
			if (response != null && response.length() > 0 && response.charAt(response.length() - 1) == '$') response = response.substring(0, response.length() - 1);
			
			escribirLog("Mensaje a enviar: " + response);
    		write.writeUTF("200:" + response);
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
	        
	        write.writeUTF("500:Ocurri� un error\n" + ex.getMessage());
	    } finally {
	        if (selectBalance != null) {
	        	selectBalance.close();
	        }
	    }
	}

	/**
	 * M�todo que consulta los clientes del banco registrados hasta el momento
	 * @param read - Variable que modela la comunicaci�n actual de escritura con el socket cliente que realiz� la solicitud
	 * @param write - Variable que modela la comunicaci�n actual del lectura con el socket cliente que realiz� la solicitud
	 * @param pagina - P�gina actual en la que se quieren consultar los clientes
	 * @throws SQLException
	 * @throws IOException
	 */
	private void consultarUsuarios(DataInputStream read, DataOutputStream write, int pagina) throws SQLException, IOException {
		ResultSet rs;
		ClienteBanco cli = null;
		int inicio = 1;
		int cant = 11;
		PreparedStatement selectBankClients = conn.prepareStatement(""
				+ "select * "
				+ "from ( "
					+ "select c.*, CONCAT(CONCAT(ci.nombre,', '), p.nombre) ubicacion, rownum r "
					+ "from clientes c "
					+ "join ciudades ci on c.ciudad_id = ci.id "
					+ "join paises p on ci.pais_id = p.id) "
				+ "where r >= ? and r < ?");
		selectBankClients.setInt(1, inicio * pagina);
		selectBankClients.setInt(2, inicio * pagina + cant);
		String response = "";
		try {
			rs = selectBankClients.executeQuery();
			while (rs.next()) {
				cli = new ClienteBanco(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(7));
				escribirLog("Cliente consultado: " + cli.toString());
				response += cli.toStringForSend() + "$";
			}
			
			if (response != null && response.length() > 0 && response.charAt(response.length() - 1) == '$') response = response.substring(0, response.length() - 1);
			
			escribirLog("Mensaje a enviar: " + response);
    		write.writeUTF("200:" + response);
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
	        
	        write.writeUTF("500:Ocurri� un error\n" + ex.getMessage());
	    } finally {
	        if (selectBankClients != null) {
	        	selectBankClients.close();
	        }
	    }
	}

	/**
	 * M�todo que realiza los accesos correspondientes a la BD para simular un usuario aleatorio de una ciudad aleatoria
	 * y le crea una cuenta asociada a dicho usuario, se realiza una transacci�n con la creaci�n del usuario y su asociaci�n a una cuenta nueva.
	 * @param read - Variable que modela la comunicaci�n actual de escritura con el socket cliente que realiz� la solicitud
	 * @param write - Variable que modela la comunicaci�n actual del lectura con el socket cliente que realiz� la solicitud
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
		escribirLog("C�dula simulada: " + cedula);
		
		low = 1;		// Siempre es el 1 como "cantidad m�nima de ciudades
		high = 1100;	// El n�mero m�ximo de ciudades en la tabla ciudades
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
			// Se establece que no se realice commit autom�ticamente
			conn.setAutoCommit(false);
			
			// Se prepara el primer query que se va a realizar, con la inserci�n de un usuario
			insertUser = conn.prepareStatement(queryUserInsert, Statement.RETURN_GENERATED_KEYS);
			insertUser.setInt(1, cedula); 				// Primer par�metro de la interpolaci�n, es la c�dula autogenerada
			insertUser.setString(2, "CC"); 				// Segundo par�metro de la interpolaci�n, es el tipo de documento
			insertUser.setString(3,	"Autogen User"); 	// Tercer par�metro de la interpolaci�n, es el nombre del usuario (como es auto generado, definimos un nombre est�tico)
			insertUser.setString(4,	"A"+cedula); 		// Cuarto par�metro de la interpolaci�n, es el apellido
			insertUser.setInt(5, id_ciudad); 			// Quinto par�metro de la interpolaci�n, es el id de la ciudad (obtenida aleatoriamente entre los ids en las semillas de ciudades)
			// Se ejecuta la inserci�n
			insertUser.execute();
			
			// Se obtiene el id del usuario insertado, mediante un select a la secuencia que define los ids en la tabla usuarios
			selectLastIdUser = conn.prepareStatement(queryGetLastUserId);
			// Se guarda en un resultset, el resultado del select
			rs = selectLastIdUser.executeQuery();
			
			String idUsuarioInsertado = "";
			// Si el resultset no est� vac�o, es decir, s� se insert� el usuario
			if (rs.next()) {
				// Asigna el id del usuario insertado a una variable de tipo int
				idUsuarioInsertado = rs.getString(1);
			    // Se prepara el query de inserci�n de una cuenta al usuario insertado previamente
			    insertAccount = conn.prepareStatement(queryAccountInsert);
			    insertAccount.setString(1, idUsuarioInsertado); // Primer par�metro de la interpolaci�n, es el id del usuario due�o de la cuenta, previamente insertado
			    insertAccount.execute();
			}
			conn.commit();	
			escribirLog("Id de usuario insertado: " + idUsuarioInsertado);
    		write.writeUTF("200:Simulaci�n de usuario y cuenta correctos;" + idUsuarioInsertado);
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
	        
	        write.writeUTF("500:Ocurri� un error\n" + ex.getMessage());
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
	 * M�todo que maneja la escritura en consola del servidor
	 */
	private void escribirLog(String message) {
		System.out.println(new SimpleDateFormat("yyy-MM-dd hh:mm:ss").format(new Timestamp(System.currentTimeMillis())) + " - " + message);  
	}
	
	/**
	 * M�todo que realiza la conexi�n a la Base de Datos
	 */
	private void realizarConexionBD() {
		try {
			// Se instancia el datasource de la librer�a
			OracleDataSource dataSource = new OracleDataSource();
	
			// Se configura la url de conexi�n al XE local
			dataSource.setURL(url);
	
			// Se establece la conexi�n con los par�metros correspondientes de usuario y password
			conn = dataSource.getConnection(user, password);
		}
		catch(SQLException ex) {
			JOptionPane.showMessageDialog(null, "Ocurri� un error al intentar conectarse a la BD:\n" + ex.getMessage(), "BD Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * M�todo main del servidor, y punto de entrada de la aplicaci�n
	 * @param args
	 */
	public static void main(String[] args) {
		new SocketServer();
	}
}