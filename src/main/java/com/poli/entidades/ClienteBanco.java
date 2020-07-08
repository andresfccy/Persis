package main.java.com.poli.entidades;

public class ClienteBanco {
	
	// --------------------------------------------------------------------------------------------------------------------------------------
	// Atributos privados de la clase
	// --------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Atributo que modela el id en base de datos del cliente
	 */
	private long _id;
	
	/**
	 * Atributo que modela la identificación del cliente
	 */
	private String _identificacion;
	
	/**
	 * Atributo que modela el tipo de documento del cliente
	 */
	private String _tipoDocumento;
	
	/**
	 * Atributo que modela el nombre del cliente
	 */
	private String _nombre;
	
	/**
	 * Atributo que modela el apellido del cliente
	 */
	private String _apellido;
	
	/**
	 * Atributo que modela el nombre de la ciudad en la que se encuentra el cliente
	 */
	private String _ubicacion;

	// --------------------------------------------------------------------------------------------------------------------------------------
	// Constructores
	// --------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Método constructor de un objeto cliente
	 * @param id - Identificación de la base de datos del cliente
	 * @param identificacion - Identificación del cliente
	 * @param tipoDocumento - Tipo de documento del cliente
	 * @param nombre - Nombre del cliente
	 * @param apellido - Apellido del cliente
	 * @param ubicacion - Ubicación del cliente
	 */
	public ClienteBanco(long id, String identificacion, String tipoDocumento, String nombre, String apellido, String ubicacion) {
		this._id = id;
		this._identificacion = identificacion;
		this._tipoDocumento = tipoDocumento;
		this._nombre = nombre;
		this._apellido = apellido;
		this._ubicacion = ubicacion;
	}
	
	/**
	 * Método para construir un cliente dado un string de sus atributos separados por ;
	 * @param clienteString - String con la información de un cliente para ser parseada
	 */
	public ClienteBanco(String clienteString) throws Exception {
		try {
			String[] ats = clienteString.split(";");
			this._id = Integer.parseInt(ats[0]);
			this._identificacion = ats[1];
			this._tipoDocumento = ats[2];
			this._nombre = ats[3];
			this._apellido = ats[4];
			this._ubicacion = ats[5];
		}
		catch(Exception ex) {
			throw new Exception("Ocurrió un error al intentar crear el cliente: \n" + ex.getMessage() + "\n" + ex.getStackTrace());
		}
	}

	// --------------------------------------------------------------------------------------------------------------------------------------
	// Getter and setters
	// --------------------------------------------------------------------------------------------------------------------------------------
	
	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public String get_identificacion() {
		return _identificacion;
	}

	public void set_identificacion(String _identificacion) {
		this._identificacion = _identificacion;
	}

	public String get_tipoDocumento() {
		return _tipoDocumento;
	}

	public void set_tipoDocumento(String _tipoDocumento) {
		this._tipoDocumento = _tipoDocumento;
	}

	public String get_nombre() {
		return _nombre;
	}

	public void set_nombre(String _nombre) {
		this._nombre = _nombre;
	}

	public String get_apellido() {
		return _apellido;
	}

	public void set_apellido(String _apellido) {
		this._apellido = _apellido;
	}

	public String get_ubicacion() {
		return _ubicacion;
	}

	public void set_ubicacion(String _ubicacion) {
		this._ubicacion = _ubicacion;
	}
	
	public String toStringForSend() {
		return get_id() + ";" + get_identificacion() + ";" + get_tipoDocumento() + ";" + get_nombre() + ";" + get_apellido() + ";" + get_ubicacion();
	}
	
	public String toString() {
		return get_nombre() + " " + get_apellido() + " " + get_tipoDocumento() + ": " + get_identificacion();
	}
}
