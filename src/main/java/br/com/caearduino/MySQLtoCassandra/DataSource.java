package br.com.caearduino.MySQLtoCassandra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataSource {
	
		private static final String banco = "invoice_system_univali";
		private static final String conexao = "jdbc:mysql://localhost:3306/" + banco;
		private static final String user = "root";
		private static final String password = "";
		
		public static Connection getConnection(){
            try {
                Connection conn = null;
                conn = DriverManager.getConnection(conexao, user, password);
                System.out.println("Conectado");
                return conn;
            } catch (SQLException e) {
                System.out.println("Erro ao obter a Connection.");
                return null;
            }
		}
		
		public static void closeConnection(Connection conn){
			try {
				if(conn != null){
					conn.close();
				}
			} catch (SQLException e) {
				System.out.println("Problema no fechamento da conexão.");
			}	
		}
		
		public static Statement getStatement(Connection conn){
			try {
				Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				return stmt;
			} catch (SQLException e) {
				System.out.println("Erro ao obter o Statement.");
				return null;
			}
		}
		
		public static void closeStatement(Statement stmt){
			try {
				if(stmt != null){
					stmt.close();
				}
			} catch (SQLException e) {
				System.out.println("Problema no fechamento do Statement.");
			}	
		}
		
		public static PreparedStatement getPreparedStatement(Connection conn){
			try {
				PreparedStatement stmt = null;
				return stmt;
			} catch (Exception e) {
				System.out.println("Erro ao obter o PreparedStatement.");
				return null;
			}
		}
		
		public static void closePreparedStatement(Statement stmt){
			try {
				if(stmt != null){
					stmt.close();
				}
			} catch (SQLException e) {
				System.out.println("Problema no fechamento do PreparedStatement.");
			}	
		}
		
		public static void closeResultSet(ResultSet result){
			try {
				if(result != null){
					result.close();
				}
			} catch (SQLException e) {
				System.out.println("Problema no fechamento do ResultSet");
			}
		}
		
		
		

		
		
		
		

}
