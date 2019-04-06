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
			documentoPdf.add(new Paragraph("Cliente:" + cliente));
			documentoPdf.add(new Paragraph("Endereço: " + endereco));
			documentoPdf.add(new Paragraph("Valor total da nota: " + String.valueOf(total)));
		} 
    	catch (DocumentException de) {
			de.printStackTrace();
		}
//    	System.out.println(cliente);
//    	System.out.println(endereco);
//    	System.out.println(total);
    	
    	DataSource.closeResultSet(rs);
    	DataSource.closeStatement(stmt);
    	DataSource.closeConnection(conn);
    	 	
    	com.datastax.driver.core.ResultSet resultCassandra = session.execute("SELECT * FROM notas WHERE invoice = "+n);
    	
    	System.out.println("Itens da nota");
    	try {
			documentoPdf.add(new Paragraph("Itens da nota"));
		} catch (DocumentException de) {
			de.printStackTrace();
		}
    	
    	for (Row row: resultCassandra)
    	{
    		documentoPdf.add(new Paragraph("Descrição do serviço: " + row.getString("service_description")));
//    		String descricao_servico = row.getString("service_description");
//    		int quantidade = row.getInt("quantity");
//    		documentoPdf.add(new Paragraph("Quantidade: " + quantidade));

    		documentoPdf.add(new Paragraph("Valor Unitário: " + String.valueOf(row.getDouble("unit_value"))));
//    		Double valor_unitario = row.getDouble("unit_value");
    		documentoPdf.add(new Paragraph("Nome do recurso: " + row.getString("resource")));
//    		String nome_recurso = row.getString("resource");
    		documentoPdf.add(new Paragraph("Função do recurso: " + row.getString("work")));
//    		String funcao_recurso = row.getString("work");
    		documentoPdf.add(new Paragraph("Taxa/Impostos: " + String.valueOf(row.getDouble("tax_percent"))));
//    		Double taxa = row.getDouble("tax_percent");
    		documentoPdf.add(new Paragraph("Desconto: " + String.valueOf(row.getDouble("discount_percent"))));
//    		Double desconto = row.getDouble("discount_percent");
    		documentoPdf.add(new Paragraph("Subtotal: " + String.valueOf(row.getDouble("subtotal"))));
//    		Double subtotal = row.getDouble("subtotal");
    		
//    		System.out.println(descricao_servico);
//    		System.out.println(quantidade);
//    		System.out.println(valor_unitario);
//    		System.out.println(nome_recurso);
//    		System.out.println(funcao_recurso);
//    		System.out.println(taxa);
//    		System.out.println(desconto);
//    		System.out.println(subtotal);
    		
    	}
    	documentoPdf.close();
    	cluster.close();
    }
}
