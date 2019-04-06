package br.com.caearduino.MySQLtoCassandra;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class App 
{
    
	public static void main( String[] args ) throws SQLException, DocumentException
    {
        Scanner keyboard = new Scanner(System.in);
        int n, numberInvoice;
        int linha = 1;
        String cliente, endereco;
        Double total;
        
        Document documentoPdf = new Document();
        
        try {
        	PdfWriter.getInstance(documentoPdf, new FileOutputStream("/Users/caearduino/Documents/relatorio.pdf"));
        	documentoPdf.open();
        	documentoPdf.setPageSize(PageSize.A4);
        	
        }
        catch (DocumentException de) {
			de.printStackTrace();
		}
        catch (IOException ioe) {
			ioe.printStackTrace();
		}
    	
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
    	
    	while(rs.next()) 
    	{
    		if (n == rs.getInt("invoice")) 
    		{
        		session.execute("INSERT INTO notas (id, client, address, invoice, "
        				+ "service_description, quantity, unit_value, tax_percent, discount_percent,"
        				+ " subtotal, value, resource, work) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        				rs.getInt("id"), rs.getString("client"), rs.getString("address"), rs.getInt("invoice"), 
        				rs.getString("service_description"), rs.getInt("quantity"), rs.getDouble("unit_value"), 
        				rs.getDouble("tax_percent"), rs.getDouble("discount_percent"), rs.getDouble("subtotal"), 
        				rs.getDouble("value"), rs.getString("resource"), rs.getString("work")) ;
        		System.out.println("Encontrado na linha:" + rs.getRow());
        		linha = rs.getRow();
    		}
    	}
    	rs.absolute(linha);
    	cliente = rs.getString("client");
    	endereco = rs.getString("address");
    	total = rs.getDouble("value");
    	
    	try {
			documentoPdf.add(new Paragraph("CLIENTE: " + cliente));
			documentoPdf.add(new Paragraph("ENDEREÇO: " + endereco));
			documentoPdf.add(new Paragraph("VALOR TOTAL: R$" + String.valueOf(total)));
			documentoPdf.add(new Paragraph("|---------------------------------------------------------------"
					+ "-----------------------------------------------------------------|"));
			documentoPdf.add(new Paragraph());
		} 
    	catch (DocumentException de) {
			de.printStackTrace();
		}
    	
    	DataSource.closeResultSet(rs);
    	DataSource.closeStatement(stmt);
    	DataSource.closeConnection(conn);
    	 	
    	com.datastax.driver.core.ResultSet resultCassandra = session.execute("SELECT * FROM notas WHERE invoice = "+n);
    	
    	try {
			documentoPdf.add(new Paragraph("ITENS DA NOTA"));
			documentoPdf.add(new Paragraph());
		} catch (DocumentException de) {
			de.printStackTrace();
		}
    	
    	for (Row row: resultCassandra)
    	{
			documentoPdf.add(new Paragraph("|---------------------------------------------------------------"
					+ "-----------------------------------------------------------------|"));
    		documentoPdf.add(new Paragraph("Descrição do serviço: " + row.getString("service_description")));
    		documentoPdf.add(new Paragraph("Quantidade: " + String.valueOf(row.getInt("quantity"))));
    		documentoPdf.add(new Paragraph("Valor Unitário: R$" + String.valueOf(row.getDouble("unit_value"))));
    		documentoPdf.add(new Paragraph("Nome do recurso: " + row.getString("resource")));
    		documentoPdf.add(new Paragraph("Função do recurso: " + row.getString("work")));
    		documentoPdf.add(new Paragraph("Taxa/Impostos: " + String.valueOf(100 * row.getDouble("tax_percent")) + "%"));
    		documentoPdf.add(new Paragraph("Desconto: " + String.valueOf(100 * row.getDouble("discount_percent"))+ "%"));
    		documentoPdf.add(new Paragraph("Subtotal: R$" + String.valueOf(row.getDouble("subtotal"))));  
    		documentoPdf.add(new Paragraph());
    	}
    	documentoPdf.close();
    	cluster.close();
    }
}
