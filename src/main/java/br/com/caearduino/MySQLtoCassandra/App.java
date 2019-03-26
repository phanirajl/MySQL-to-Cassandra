package br.com.caearduino.MySQLtoCassandra;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App 
{
    
	public static void main( String[] args ) throws SQLException
    {
        
    	Connection conn = DataSource.getConnection();
    	Statement stmt = DataSource.getStatement(conn);
    	ResultSet rs = null;
    	rs = stmt.executeQuery("select * from customer c join invoice iv on c.id_customer = iv.customer_id "
    			+ "join invoice_item ii on iv.number = ii.invoice_id "
    			+ "join resource rs on rs.id_resource = ii.resource_id");
    	
    	
    	rs.last();
    	
    	System.out.println(rs.getRow());
    	DataSource.closeResultSet(rs);
    	DataSource.closeStatement(stmt);
    	DataSource.closeConnection(conn);
    	
    	
    	
    }
}
