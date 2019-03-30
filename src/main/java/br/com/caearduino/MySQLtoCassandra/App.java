package br.com.caearduino.MySQLtoCassandra;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class App 
{
    
	public static void main( String[] args ) throws SQLException
    {
        Scanner keyboard = new Scanner(System.in);
        int n, numberInvoice;
        

    	
		Connection conn = DataSource.getConnection();
    	Statement stmt = DataSource.getStatement(conn);
    	ResultSet rs = null;
    	rs = stmt.executeQuery("select customer.id_customer as 'id', customer.name as 'client',  customer.address, invoice.number as 'invoice', "
    			+ "service.service_description, invoice_item.quantity, invoice_item.unit_value, invoice_item.tax_percent, invoice_item.discount_percent, "
    			+ "invoice_item.subtotal, invoice.value, resource.employee as 'resource', department.name_department as 'work' from customer "
    			+ "join invoice on customer.id_customer = invoice.customer_id "
    			+ "join invoice_item on invoice.number = invoice_item.invoice_id "
    			+ "join resource on invoice_item.resource_id = resource.id_resource "
    			+ "join service on service.service_id = invoice_item.service_id "
    			+ "join department on resource.department = department.id_department");
    	
    	
    	System.out.println("Insira número da nota para consulta");
    	n = keyboard.nextInt();
    	
    	Cluster cluster;
		Session session;
		cluster = Cluster.builder().addContactPoint("localhost").build();
		session = cluster.connect("segsoft");
    	
    	while(rs.next()) {
    		if (n == rs.getInt("invoice")) {
        		session.execute("INSERT INTO notas (id, client, address, invoice, "
        				+ "service_description, quantity, unit_value, tax_percent, discount_percent,"
        				+ " subtotal, value, resource, work) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        				rs.getInt("id"), rs.getString("client"), rs.getString("address"), rs.getInt("invoice"), 
        				rs.getString("service_description"), rs.getInt("quantity"), rs.getDouble("unit_value"), 
        				rs.getDouble("tax_percent"), rs.getDouble("discount_percent"), rs.getDouble("subtotal"), 
        				rs.getDouble("value"), rs.getString("resource"), rs.getString("work")) ;
        		System.out.println("Encontrado na linha:" + rs.getRow());
    		} 
    		else {	
    				System.out.println("Número da nota não encontrado na linha");
    			 }
    	}
  
    	DataSource.closeResultSet(rs);
    	DataSource.closeStatement(stmt);
    	DataSource.closeConnection(conn);
    	
    	
    	
    	com.datastax.driver.core.ResultSet resultCassandra = session.execute("SELECT * FROM notas WHERE invoice = "+n);
    	
    	cluster.close();	
    	
    }
}
