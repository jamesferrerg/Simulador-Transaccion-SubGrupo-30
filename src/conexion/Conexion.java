package conexion;

import java.sql.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

/**
 * 
 * @author James F. Gomez V.
 * 2021-11-20
 * Simulador-Transaccion-SubGrupo-30
 */
public class Conexion {

	public static void main(String[] args) {
		Conexion con = new Conexion();
		con.conectar();
	}

	Scanner seleccionTransaccion = new Scanner(System.in);

	public Connection conectar() {
		Connection con = null;
		PreparedStatement pstm = null;
		// Agregar libreria al path de Mysql, version que maneja actual es 8.0.27 y SO Ubuntu 20
		// Conexion driver para Mysql
		String driver = "com.mysql.cj.jdbc.Driver";
		String user = "root";
		String pwd = "123456";
		String url = "jdbc:mysql://localhost:3306/banco_db";

		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, user, pwd);
			JOptionPane.showMessageDialog(null, "Conexión establecida con éxito");
			int transaccion = Integer.parseInt(JOptionPane.showInputDialog(
					"Seleccione la transacción que desee de acuerdo al número: \n1. Crear cliente \n" + "2. Salir \n"));
			switch (transaccion) {
			case 1:
				crearCliente(con, pstm);
				break;
			default:
				System.out.println("Salio sin realizar algun tipo de transacción");
				break;
			}
		} catch (Exception e) {
			System.out.println("Error. No se conecto " + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return con;
	}

	// DAO - Create
	public void crearCliente(Connection con, PreparedStatement pstm) throws SQLException, ParseException {
		JOptionPane.showMessageDialog(null, "Iniciando creación de cliente");
		String sql = "INSERT INTO `banco_db`.`cliente` (`id_cliente`, `nombres`, `apellidos`, "
				+ "`numero_identificacion`, `fecha_nacimiento`, `telefono`, `email`, `direccion`)";

		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		int id_cliente = Integer.parseInt(JOptionPane.showInputDialog("id_cliente: "));
		String nombres = JOptionPane.showInputDialog("nombres: ");
		String apellidos = JOptionPane.showInputDialog("apellidos: ");
		int numero_identificacion = Integer.parseInt(JOptionPane.showInputDialog("numero_identificacion: "));
		String fecha_nacimiento = JOptionPane.showInputDialog("fecha_nacimiento: YYYY/MM/DD ");
		long telefono = Long.parseLong(JOptionPane.showInputDialog("telefono: "));
		String email = JOptionPane.showInputDialog("email: ");
		String direccion = JOptionPane.showInputDialog("direccion ");

		pstm = con.prepareStatement(sql);
		pstm.setInt(1, id_cliente);
		pstm.setString(2, nombres);
		pstm.setString(3, apellidos);
		pstm.setInt(4, numero_identificacion);
		pstm.setString(5, fecha_nacimiento);
		pstm.setLong(6, telefono);
		pstm.setString(7, email);
		pstm.setString(8, direccion);

		int aplique = pstm.executeUpdate();
		if (aplique == 1) {
			System.out.println(aplique + " fila insertada con éxito");
			// Nota : esto confirmará implícitamente la transacción activa y creará una nueva
			con.setAutoCommit(false);
			con.commit();
		} else {
			con.rollback();
			throw new RuntimeException("Error insertando la fila");
		}
	}

}
