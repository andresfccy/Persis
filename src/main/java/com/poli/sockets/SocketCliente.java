package main.java.com.poli.sockets;

import java.net.*;

import javax.swing.JOptionPane;

import main.java.com.poli.entidades.ClienteBanco;

import java.io.*;

public class SocketCliente {
	
	/**
	 * Puerto de conexi�n del servidor correspondiente
	 */
	private final static int PUERTO_SERVIDOR = SocketServer.PUERTO_SERVIDOR;
	
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
	public SocketCliente () {
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
						+ "3. Realizar una consignaci�n.\n"
						+ "4. Realizar una consulta.\n"
						//+ "4. Say Hello.\n"
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
							realizarConsignacion();
							break;
						case "4":
							realizarConsulta();
							break;
						//case "4":
						//	sayHello();
						//	break;
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
	 * @throws Exception 
	 */
	private void configuraciones() throws Exception {
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
						+ "2. Crear una cuenta a un cliente.\n"
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
					case "2":

						ClienteBanco[] clientes = new ClienteBanco[10]; // Lista de clientes que trae la consulta al servidor

						String selOpt = "";
						String opts = "";
						int paginaActual = 1;
				        
				        write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina 1
						String res2 = read.readUTF(); // Esperar a que el servidor responda
						if(res2.split(":")[0].equalsIgnoreCase("200")) {
							String serverResponse = res2.split(":")[1];
							String[] clientesString = serverResponse.split("\\$");
							for(int i = 0; i < 10 && i < clientesString.length; i++) {
								clientes[i] = new ClienteBanco(clientesString[i]);
								opts += (i+1) +". " + clientes[i].toString() + ". \n";
							}
							
							while(selOpt != null && !selOpt.equalsIgnoreCase("0")) {
								selOpt = JOptionPane.showInputDialog(null, ""
										+ "Selecciona un cliente para crearle una cuenta: \n"
										+ opts
										+ (clientesString.length > 10 ? "11. P�gina siguiente. \n" : "")
										+ (paginaActual > 1 ? "12. P�gina anterior. \n" : "")
										+ "0. Men� anterior");
								if( selOpt == null ) selOpt = "0";
								selOpt = selOpt.equalsIgnoreCase("11") ? clientesString.length > 10 ? "11" : "" : selOpt;
								selOpt = selOpt.equalsIgnoreCase("12") ? paginaActual > 1 ? "12" : "" : selOpt;
								
								switch(selOpt) {
									case "1":
									case "2":
									case "3":
									case "4":
									case "5":
									case "6":
									case "7":
									case "8":
									case "9":
									case "10":
										double nuevoSaldo = Double.parseDouble(JOptionPane.showInputDialog("Ingresa el saldo con el que se va a crear la cuenta: "));
										write.writeUTF("106:" + clientes[Integer.parseInt(selOpt) - 1].get_id() + ":" + nuevoSaldo); // Se env�a el ID del cliente seleccionado y el saldo con el que se va a crear la cuenta
										res2 = read.readUTF(); // Esperar a que el servidor responda
										if(res2.split(":")[0].equalsIgnoreCase("200")) {
											JOptionPane.showMessageDialog(null, "Operaci�n exitosa! \n"
													+ "N�mero de cuenta nueva: " + res2.split(";")[1]);
										}
										else if(res2.split(":")[0].equalsIgnoreCase("500")) {
											JOptionPane.showMessageDialog(null, "Ocurri� un error");
											System.out.println(res2.split(":")[1]);
										}
										break;
									case "11":
										paginaActual++;
										write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina siguiente
										res2 = read.readUTF(); // Esperar a que el servidor responda
										if(res2.split(":")[0].equalsIgnoreCase("200")) {
											clientesString = (res2.split(":")[1]).split("\\$");
											for(int i = 0; i < 10; i++) {
												clientes[i] = new ClienteBanco(clientesString[i]);
												opts += "1. " + clientes[i].toString() + ". \n";
											}
										}
										break;
									case "12":
										paginaActual--;
										write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina anterior
										res2 = read.readUTF(); // Esperar a que el servidor responda
										if(res2.split(":")[0].equalsIgnoreCase("200")) {
											clientesString = (res2.split(":")[1]).split("|");
											for(int i = 0; i < 10; i++) {
												clientes[i] = new ClienteBanco(clientesString[i]);
												opts += "1. " + clientes[i].toString() + ". \n";
											}
										}
										break;
									case "0":
										write.writeUTF("0:Say Bye");
										read.readUTF(); // Esperar a que el servidor responda
										break;
									default:
										selOpt = "";
										JOptionPane.showMessageDialog(null, "Opci�n no v�lida");
										break;
								}
							}
						}
						else if(res2.split(":")[0].equalsIgnoreCase("500")) {
							JOptionPane.showMessageDialog(null, "Ocurri� un error al consultar los clientes del banco");
							System.out.println(res2.split(":")[1]);
						}
						
						read.close();
						write.close();
						s.close();
						
						
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
		try {
			ClienteBanco[] clientes = new ClienteBanco[10]; // Lista de clientes que trae la consulta al servidor

			String selOpt = "";
			String opts = "";
			int paginaActual = 1;
			s = new Socket("localhost", this.PUERTO_SERVIDOR);
			// Variables de lectura y escritura
			read = new DataInputStream(s.getInputStream()); 
	        write = new DataOutputStream(s.getOutputStream());
	        
	        write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina 1
			String res = read.readUTF(); // Esperar a que el servidor responda
			if(res.split(":")[0].equalsIgnoreCase("200")) {
				String serverResponse = res.split(":")[1];
				String[] clientesString = serverResponse.split("\\$");
				for(int i = 0; i < 10 && i < clientesString.length; i++) {
					clientes[i] = new ClienteBanco(clientesString[i]);
					opts += (i+1) +". " + clientes[i].toString() + ". \n";
				}
				
				while(selOpt != null && !selOpt.equalsIgnoreCase("0")) {
					selOpt = JOptionPane.showInputDialog(null, ""
							+ "Selecciona un cliente para hacer un retiro: \n"
							+ opts
							+ (clientesString.length > 10 ? "11. P�gina siguiente. \n" : "")
							+ (paginaActual > 1 ? "12. P�gina anterior. \n" : "")
							+ "0. Men� anterior");
					if( selOpt == null ) selOpt = "0";
					selOpt = selOpt.equalsIgnoreCase("11") ? clientesString.length > 10 ? "11" : "" : selOpt;
					selOpt = selOpt.equalsIgnoreCase("12") ? paginaActual > 1 ? "12" : "" : selOpt;
					
					switch(selOpt) {
						case "1":
						case "2":
						case "3":
						case "4":
						case "5":
						case "6":
						case "7":
						case "8":
						case "9":
						case "10":
							write.writeUTF("104:" + clientes[Integer.parseInt(selOpt) - 1].get_id()); // Se env�a el ID del cliente seleccionado
							res = read.readUTF(); // Esperar a que el servidor responda
							if(res.split(":")[0].equalsIgnoreCase("200")) {
								String saldosResponseString =  res.split(":")[1];
								String[] listaSaldosResponseString = saldosResponseString.split("\\$");
								String opts2 = "";
								String tmp[];
								for (int i = 0; i < listaSaldosResponseString.length; i++) { 
									tmp = listaSaldosResponseString[i].split(";");
									opts2 += (i+1) + ". " + (tmp.length > 1 ? ("# de cuenta: " + tmp[0] + "\nSaldo: " + tmp[1] + "\n") : tmp[0]);
								}
								String selOpt2 = "";
								
								while(selOpt2 != null && !selOpt2.equalsIgnoreCase("0")) {
									selOpt2 = JOptionPane.showInputDialog(null, ""
											+ "Selecciona una cuenta para hacer un retiro: \n"
											+ opts2
											+ "0. Men� anterior");
									if( selOpt2 == null ) selOpt2 = "0";
									if( selOpt2 != "0" ) {
										if( Integer.parseInt(selOpt2) < 0 ) selOpt2 = "-1";
										if( Integer.parseInt(selOpt2) > listaSaldosResponseString.length ) selOpt2 = "-1";
										if( selOpt2.equalsIgnoreCase("-1")) JOptionPane.showMessageDialog(null, "Opci�n no v�lida");
										else {
											double saldoRetiro = Double.parseDouble(JOptionPane.showInputDialog("Ingrese el valor a retirar: "));
											write.writeUTF("105:" + listaSaldosResponseString[(Integer.parseInt(selOpt2) - 1)].split(";")[0] + ":" + saldoRetiro); // Se env�a el ID del cliente seleccionado
											res = read.readUTF(); // Esperar a que el servidor responda
											if(res.split(":")[0].equalsIgnoreCase("200")) {
												JOptionPane.showMessageDialog(null, "Se realiz� el retiro exitosamente.\n"
														+ "Nuevo saldo: " + res.split(";")[1]);
												selOpt2 = "0";
											}
										}
									}
								}
							}
							else if(res.split(":")[0].equalsIgnoreCase("500")) {
								JOptionPane.showMessageDialog(null, "Ocurri� un error");
								System.out.println(res.split(":")[1]);
							}
							break;
						case "11":
							paginaActual++;
							write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina siguiente
							res = read.readUTF(); // Esperar a que el servidor responda
							if(res.split(":")[0].equalsIgnoreCase("200")) {
								clientesString = (res.split(":")[1]).split("\\$");
								for(int i = 0; i < 10; i++) {
									clientes[i] = new ClienteBanco(clientesString[i]);
									opts += "1. " + clientes[i].toString() + ". \n";
								}
							}
							break;
						case "12":
							paginaActual--;
							write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina anterior
							res = read.readUTF(); // Esperar a que el servidor responda
							if(res.split(":")[0].equalsIgnoreCase("200")) {
								clientesString = (res.split(":")[1]).split("|");
								for(int i = 0; i < 10; i++) {
									clientes[i] = new ClienteBanco(clientesString[i]);
									opts += "1. " + clientes[i].toString() + ". \n";
								}
							}
							break;
						case "0":
							write.writeUTF("0:Say Bye");
							read.readUTF(); // Esperar a que el servidor responda
							break;
						default:
							selOpt = "";
							JOptionPane.showMessageDialog(null, "Opci�n no v�lida");
							break;
					}
				}
			}
			else if(res.split(":")[0].equalsIgnoreCase("500")) {
				JOptionPane.showMessageDialog(null, "Ocurri� un error al consultar los clientes del banco");
				System.out.println(res.split(":")[1]);
			}
			
			read.close();
			write.close();
			s.close();
		}
		catch(IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * M�todo que gestiona la opci�n de realizar una consignaci�n
	 */
	private void realizarConsignacion() {
		try {
			ClienteBanco[] clientes = new ClienteBanco[10]; // Lista de clientes que trae la consulta al servidor

			String selOpt = "";
			String opts = "";
			int paginaActual = 1;
			s = new Socket("localhost", this.PUERTO_SERVIDOR);
			// Variables de lectura y escritura
			read = new DataInputStream(s.getInputStream()); 
	        write = new DataOutputStream(s.getOutputStream());
	        
	        write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina 1
			String res = read.readUTF(); // Esperar a que el servidor responda
			if(res.split(":")[0].equalsIgnoreCase("200")) {
				String serverResponse = res.split(":")[1];
				String[] clientesString = serverResponse.split("\\$");
				for(int i = 0; i < 10 && i < clientesString.length; i++) {
					clientes[i] = new ClienteBanco(clientesString[i]);
					opts += (i+1) +". " + clientes[i].toString() + ". \n";
				}
				
				while(selOpt != null && !selOpt.equalsIgnoreCase("0")) {
					selOpt = JOptionPane.showInputDialog(null, ""
							+ "Selecciona un cliente para hacer una consignaci�n: \n"
							+ opts
							+ (clientesString.length > 10 ? "11. P�gina siguiente. \n" : "")
							+ (paginaActual > 1 ? "12. P�gina anterior. \n" : "")
							+ "0. Men� anterior");
					if( selOpt == null ) selOpt = "0";
					selOpt = selOpt.equalsIgnoreCase("11") ? clientesString.length > 10 ? "11" : "" : selOpt;
					selOpt = selOpt.equalsIgnoreCase("12") ? paginaActual > 1 ? "12" : "" : selOpt;
					
					switch(selOpt) {
						case "1":
						case "2":
						case "3":
						case "4":
						case "5":
						case "6":
						case "7":
						case "8":
						case "9":
						case "10":
							write.writeUTF("104:" + clientes[Integer.parseInt(selOpt) - 1].get_id()); // Se env�a el ID del cliente seleccionado
							res = read.readUTF(); // Esperar a que el servidor responda
							if(res.split(":")[0].equalsIgnoreCase("200")) {
								String saldosResponseString =  res.split(":")[1];
								String[] listaSaldosResponseString = saldosResponseString.split("\\$");
								String opts2 = "";
								String tmp[];
								for (int i = 0; i < listaSaldosResponseString.length; i++) { 
									tmp = listaSaldosResponseString[i].split(";");
									opts2 += (i+1) + ". " + (tmp.length > 1 ? ("# de cuenta: " + tmp[0] + "\nSaldo: " + tmp[1] + "\n") : tmp[0]);
								}
								String selOpt2 = "";
								
								while(selOpt2 != null && !selOpt2.equalsIgnoreCase("0")) {
									selOpt2 = JOptionPane.showInputDialog(null, ""
											+ "Selecciona una cuenta para hacer una consignaci�n: \n"
											+ opts2
											+ "0. Men� anterior");
									if( selOpt2 == null ) selOpt2 = "0";
									if( selOpt2 != "0" ) {
										if( Integer.parseInt(selOpt2) < 0 ) selOpt2 = "-1";
										if( Integer.parseInt(selOpt2) > listaSaldosResponseString.length ) selOpt2 = "-1";
										if( selOpt2.equalsIgnoreCase("-1")) JOptionPane.showMessageDialog(null, "Opci�n no v�lida");
										else {
											double saldoRetiro = Double.parseDouble(JOptionPane.showInputDialog("Ingrese el valor a consignar: "));
											write.writeUTF("107:" + listaSaldosResponseString[(Integer.parseInt(selOpt2) - 1)].split(";")[0] + ":" + saldoRetiro); // Se env�a el ID del cliente seleccionado
											res = read.readUTF(); // Esperar a que el servidor responda
											if(res.split(":")[0].equalsIgnoreCase("200")) {
												JOptionPane.showMessageDialog(null, "Se realiz� consignaci�n exitosamente.\n"
														+ "Nuevo saldo: " + res.split(";")[1]);
												selOpt2 = "0";
											}
										}
									}
								}
							}
							else if(res.split(":")[0].equalsIgnoreCase("500")) {
								JOptionPane.showMessageDialog(null, "Ocurri� un error");
								System.out.println(res.split(":")[1]);
							}
							break;
						case "11":
							paginaActual++;
							write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina siguiente
							res = read.readUTF(); // Esperar a que el servidor responda
							if(res.split(":")[0].equalsIgnoreCase("200")) {
								clientesString = (res.split(":")[1]).split("\\$");
								for(int i = 0; i < 10; i++) {
									clientes[i] = new ClienteBanco(clientesString[i]);
									opts += "1. " + clientes[i].toString() + ". \n";
								}
							}
							break;
						case "12":
							paginaActual--;
							write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina anterior
							res = read.readUTF(); // Esperar a que el servidor responda
							if(res.split(":")[0].equalsIgnoreCase("200")) {
								clientesString = (res.split(":")[1]).split("|");
								for(int i = 0; i < 10; i++) {
									clientes[i] = new ClienteBanco(clientesString[i]);
									opts += "1. " + clientes[i].toString() + ". \n";
								}
							}
							break;
						case "0":
							write.writeUTF("0:Say Bye");
							read.readUTF(); // Esperar a que el servidor responda
							break;
						default:
							selOpt = "";
							JOptionPane.showMessageDialog(null, "Opci�n no v�lida");
							break;
					}
				}
			}
			else if(res.split(":")[0].equalsIgnoreCase("500")) {
				JOptionPane.showMessageDialog(null, "Ocurri� un error al consultar los clientes del banco");
				System.out.println(res.split(":")[1]);
			}
			
			read.close();
			write.close();
			s.close();
		}
		catch(IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * M�todo que gestiona la opci�n de realizar una consulta
	 */
	private void realizarConsulta() {
		try {
			ClienteBanco[] clientes = new ClienteBanco[10]; // Lista de clientes que trae la consulta al servidor

			String selOpt = "";
			String opts = "";
			int paginaActual = 1;
			s = new Socket("localhost", this.PUERTO_SERVIDOR);
			// Variables de lectura y escritura
			read = new DataInputStream(s.getInputStream()); 
	        write = new DataOutputStream(s.getOutputStream());
	        
	        write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina 1
			String res = read.readUTF(); // Esperar a que el servidor responda
			if(res.split(":")[0].equalsIgnoreCase("200")) {
				String serverResponse = res.split(":")[1];
				String[] clientesString = serverResponse.split("\\$");
				for(int i = 0; i < 10 && i < clientesString.length; i++) {
					clientes[i] = new ClienteBanco(clientesString[i]);
					opts += (i+1) +". " + clientes[i].toString() + ". \n";
				}
				
				while(selOpt != null && !selOpt.equalsIgnoreCase("0")) {
					selOpt = JOptionPane.showInputDialog(null, ""
							+ "Selecciona un cliente a consultar: \n"
							+ opts
							+ (clientesString.length > 10 ? "11. P�gina siguiente. \n" : "")
							+ (paginaActual > 1 ? "12. P�gina anterior. \n" : "")
							+ "0. Men� anterior");
					if( selOpt == null ) selOpt = "0";
					selOpt = selOpt.equalsIgnoreCase("11") ? clientesString.length > 10 ? "11" : "" : selOpt;
					selOpt = selOpt.equalsIgnoreCase("12") ? paginaActual > 1 ? "12" : "" : selOpt;
					
					switch(selOpt) {
						case "1":
						case "2":
						case "3":
						case "4":
						case "5":
						case "6":
						case "7":
						case "8":
						case "9":
						case "10":
							write.writeUTF("104:" + clientes[Integer.parseInt(selOpt) - 1].get_id()); // Se env�a el ID del cliente seleccionado
							res = read.readUTF(); // Esperar a que el servidor responda
							if(res.split(":")[0].equalsIgnoreCase("200")) {
								String saldosResponseString =  res.split(":")[1];
								String[] listaSaldosResponseString = saldosResponseString.split("\\$");
								String saldosResponse = "";
								String tmp[];
								for (int i = 0; i < listaSaldosResponseString.length; i++) { 
									tmp = listaSaldosResponseString[i].split(";");
									saldosResponse += tmp.length > 1 ? ("# de cuenta: " + tmp[0] + "\nSaldo: " + tmp[1] + "\n") : tmp[0];
								}
								JOptionPane.showMessageDialog(null, "Operaci�n exitosa: \n"
										+ saldosResponse);
							}
							else if(res.split(":")[0].equalsIgnoreCase("500")) {
								JOptionPane.showMessageDialog(null, "Ocurri� un error");
								System.out.println(res.split(":")[1]);
							}
							break;
						case "11":
							paginaActual++;
							write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina siguiente
							res = read.readUTF(); // Esperar a que el servidor responda
							if(res.split(":")[0].equalsIgnoreCase("200")) {
								clientesString = (res.split(":")[1]).split("\\$");
								for(int i = 0; i < 10; i++) {
									clientes[i] = new ClienteBanco(clientesString[i]);
									opts += "1. " + clientes[i].toString() + ". \n";
								}
							}
							break;
						case "12":
							paginaActual--;
							write.writeUTF("103:" + paginaActual); // Consultar clientes del banco p�gina anterior
							res = read.readUTF(); // Esperar a que el servidor responda
							if(res.split(":")[0].equalsIgnoreCase("200")) {
								clientesString = (res.split(":")[1]).split("|");
								for(int i = 0; i < 10; i++) {
									clientes[i] = new ClienteBanco(clientesString[i]);
									opts += "1. " + clientes[i].toString() + ". \n";
								}
							}
							break;
						case "0":
							write.writeUTF("0:Say Bye");
							read.readUTF(); // Esperar a que el servidor responda
							break;
						default:
							selOpt = "";
							JOptionPane.showMessageDialog(null, "Opci�n no v�lida");
							break;
					}
				}
			}
			else if(res.split(":")[0].equalsIgnoreCase("500")) {
				JOptionPane.showMessageDialog(null, "Ocurri� un error al consultar los clientes del banco");
				System.out.println(res.split(":")[1]);
			}
			
			read.close();
			write.close();
			s.close();
		}
		catch(IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		new SocketCliente();
	}
}