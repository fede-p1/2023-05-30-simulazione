package it.polito.tdp.gosales.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.gosales.model.DailySale;
import it.polito.tdp.gosales.model.Products;
import it.polito.tdp.gosales.model.Retailers;

public class GOsalesDAO {
	
	
	/**
	 * Metodo per leggere la lista di tutti i rivenditori dal database
	 * @return
	 */

	public List<Retailers> getAllRetailers(String country){
		String query = "SELECT * "
				+ "FROM go_retailers "
				+ "WHERE Country = ? "
				+ "ORDER BY Retailer_name ASC";
		List<Retailers> result = new ArrayList<Retailers>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(query);
			st.setString(1, country);

			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				
				result.add(new Retailers(rs.getInt("Retailer_code"), 
							rs.getString("Retailer_name"),
							rs.getString("Type"), 
							rs.getString("Country")));
			}
			conn.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
		
	}
	
	public void setAllProducts(String country, int anno, Map<Integer,Retailers> mapRetailers){
		String query = "SELECT go_retailers.Retailer_code, Product_number "
				+ "FROM go_daily_sales, go_retailers "
				+ "WHERE go_retailers.Retailer_code = go_daily_sales.Retailer_code AND Country = ? AND YEAR(DATE) = ? "
				+ "GROUP BY go_retailers.Retailer_code, Product_number "
				+ "ORDER BY go_retailers.Retailer_code, Product_number";
		
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(query);
			st.setString(1, country);
			st.setInt(2, anno);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				
				if (mapRetailers.containsKey(rs.getInt("Retailer_code")))
					mapRetailers.get(rs.getInt("Retailer_code")).getProducts().add(rs.getInt("Product_number"));
			}
			conn.close();
			return ;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
		
	}
	
	
	/**
	 * Metodo per leggere la lista di tutti i prodotti dal database
	 * @return
	 */
	public List<Products> getAllProducts(){
		String query = "SELECT * from go_products";
		List<Products> result = new ArrayList<Products>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(query);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Products(rs.getInt("Product_number"), 
						rs.getString("Product_line"), 
						rs.getString("Product_type"), 
						rs.getString("Product"), 
						rs.getString("Product_brand"), 
						rs.getString("Product_color"),
						rs.getDouble("Unit_cost"), 
						rs.getDouble("Unit_price")));
			}
			conn.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
		
	}

	
	/**
	 * Metodo per leggere la lista di tutte le vendite nel database
	 * @return
	 */
	public List<DailySale> getAllSales(){
		String query = "SELECT * from go_daily_sales";
		List<DailySale> result = new ArrayList<DailySale>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(query);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new DailySale(rs.getInt("retailer_code"),
				rs.getInt("product_number"),
				rs.getInt("order_method_code"),
				rs.getTimestamp("date").toLocalDateTime().toLocalDate(),
				rs.getInt("quantity"),
				rs.getDouble("unit_price"),
				rs.getDouble("unit_sale_price")  ));
			}
			conn.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	public List<String> getAllCountries(){
		String query = "SELECT DISTINCT Country "
				+ "FROM go_retailers "
				+ "ORDER BY Country ASC";
		List<String> result = new ArrayList<>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(query);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(rs.getString("Country"));
			}
			conn.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	public double getCost(int productNumber){
		String query = "SELECT Unit_cost "
				+ "FROM go_products "
				+ "WHERE Product_number = ?";
		

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(query);
			st.setInt(1, productNumber);
			ResultSet rs = st.executeQuery();
			List<Double> result = new ArrayList<>();

			while (rs.next()) {
				result.add(rs.getDouble("Unit_cost"));
			}
			conn.close();
			return result.get(result.size()-1);
		
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	public double getPrice(int productNumber){
		String query = "SELECT Unit_price "
				+ "FROM go_products "
				+ "WHERE Product_number = ?";
		

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(query);
			st.setInt(1, productNumber);
			ResultSet rs = st.executeQuery();
			List<Double> result = new ArrayList<>();

			while (rs.next()) {
				result.add(rs.getDouble("Unit_price"));
			}
			conn.close();
			return result.get(result.size()-1);
		
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	public int getNumEventi(Retailers r, int anno, int productNumber){
		String query = "SELECT Retailer_code, Product_number, COUNT(*) AS numEventi "
				+ "FROM go_daily_sales "
				+ "WHERE YEAR(DATE) = ? AND Retailer_code = ? AND Product_number = ? "
				+ "GROUP BY Retailer_code, Product_number "
				+ "ORDER BY Retailer_code, Product_number";
		List<Integer> result = new ArrayList<>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(query);
			st.setInt(1, anno);
			st.setInt(2, r.getCode());
			st.setInt(3, productNumber);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(rs.getInt("numEventi"));
			}
			conn.close();
			return result.get(result.size()-1);
		
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	public int getqtaTot(Retailers r, int anno, int productNumber){
		String query = "SELECT Retailer_code, Product_number, SUM(Quantity) AS qtaTot "
				+ "FROM go_daily_sales "
				+ "WHERE YEAR(DATE) = ? AND Retailer_code = ? AND Product_number = ? "
				+ "GROUP BY Retailer_code, Product_number "
				+ "ORDER BY Retailer_code, Product_number";
		
		List<Integer> result = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(query);
			st.setInt(1, anno);
			st.setInt(2, r.getCode());
			st.setInt(3, productNumber);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(rs.getInt("qtaTot"));
			}
			conn.close();
			return result.get(result.size()-1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
}
