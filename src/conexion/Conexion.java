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
			int transaccion = Integer.parseInt(JOptionPane
					.showInputDialog("Seleccione la transacción que desee de acuerdo al número: \n1. Crear cliente \n"
							+ "2. Retirar \n" + "3. Consulta de datos \n" + "4. Salir \n"));
			switch (transaccion) {
			case 1:
				crearCliente(con, pstm);
				break;
			case 2:
				retiro(con, pstm);
				break;
			case 3:
				consultaDatos(con, pstm);
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
	
	public void retiro(Connection con, PreparedStatement pstm) throws SQLException, ParseException {
		ResultSet rs = null;
		JOptionPane.showMessageDialog(null, "Iniciando retiro de la cuenta");
		// Preparar query para consultar el saldo actual del cliente
		String sqlSaldoInicial = "SELECT cu.saldo FROM banco_db.cuenta cu WHERE cu.id_cuenta=1";
		pstm = con.prepareStatement(sqlSaldoInicial);
		rs = pstm.executeQuery();
		// Mostrar por visor el saldo del cliente
		if (rs.next()) {
			JOptionPane.showMessageDialog(null, "Su saldo actual es: " + rs.getString(1));
		}
		
		// Insertar el valor del retiro a la tabla moviento como registro del retiro
		String sql1 = "INSERT INTO `banco_db`.`movimiento` (`id_movimiento`, `valor`, `fecha`, `tipo_id`, `cuenta_id`)";
		sql1 += "VALUES (?, ?, ?, ?, ?)";
		
		int id_movimiento = Integer.parseInt(JOptionPane.showInputDialog("id_movimiento: "));
		Double valor = Double.parseDouble(JOptionPane.showInputDialog("Valor a retirar: "));
		String fecha = JOptionPane.showInputDialog("fecha: YYYY/MM/DD ");
		
		pstm = con.prepareStatement(sql1);
		pstm.setInt(1, id_movimiento);
		pstm.setDouble(2, valor);
		pstm.setString(3, fecha);
		pstm.setInt(4, 2);
		pstm.setInt(5, 1);
		
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
		
		// Actualizar el valor del saldo restando el valor actual del saldo menos el valor del retiro
		String sql2 = "UPDATE banco_db.cuenta cu\n"
				+ "SET cu.saldo=((SELECT cu.saldo)-(SELECT M.valor FROM banco_db.movimiento M ORDER BY M.id_movimiento DESC LIMIT 1))\n"
				+ "WHERE cu.id_cuenta=?";
		
		pstm = con.prepareStatement(sql2);
		pstm.setInt(1,1);
		
		int aplique2 = pstm.executeUpdate();
		if (aplique2 == 1) {
			System.out.println(aplique + " campo actualizado con éxito");
			// Nota : esto confirmará implícitamente la transacción activa y creará una nueva
			con.setAutoCommit(false);
			con.commit();
		} else {
			con.rollback();
			throw new RuntimeException("Error insertando la fila");
		}
		
		JOptionPane.showMessageDialog(null, "Iniciando retiro de la cuenta");
		String sqlSaldoFinal = "SELECT cu.saldo FROM banco_db.cuenta cu WHERE cu.id_cuenta=1";
		pstm = con.prepareStatement(sqlSaldoFinal);
		rs = pstm.executeQuery();
		if (rs.next()) {
			JOptionPane.showMessageDialog(null, "Su saldo final es: " + rs.getString(1));
		}
		
	}
	
	public void consultaDatos(Connection con, PreparedStatement pstm) throws SQLException, ParseException{
		ResultSet rs = null;
		// Realizacion de consulta sobre ultimo movimiento, saldo y cliente
		String sql = "SELECT M.valor, M.fecha, cu.saldo, cl.nombres, cl.apellidos, cl.numero_identificacion FROM banco_db.cuenta AS cu \n"
				+ "INNER JOIN banco_db.movimiento AS M\n" + "ON cu.id_cuenta=M.cuenta_id\n"
				+ "INNER JOIN banco_db.cliente AS cl\n" + "ON cl.id_cliente=cu.cliente_id\n" + "WHERE cl.id_cliente=4\n"
				+ "ORDER BY M.id_movimiento DESC LIMIT 1";
		
		pstm = con.prepareStatement(sql);
		rs = pstm.executeQuery();
		// Mostrar en la ventana la consulta realizada
		if (rs.next()) {
			JOptionPane.showMessageDialog(null,
					"Consulta datos cliente \n" + "Nombre: " + rs.getString(4) + " " + rs.getString(5) + "\n"
							+ "Numero de identificacion: " + rs.getString(6) + "\n" + "Valor ultimo movimiento: "
							+ rs.getString(1) + "\n" + "Fecha del movimiento: " + rs.getString(2) + "\n"
							+ "Saldo actual: " + rs.getString(3) + "\n");
		}
	}

}
