package it.polito.tdp.gosales.model;

import java.time.LocalDate;
import java.util.*;

import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.gosales.dao.GOsalesDAO;
import it.polito.tdp.gosales.model.Event.EventType;

public class Model {
	
	GOsalesDAO dao;
	SimpleWeightedGraph<Retailers,DefaultWeightedEdge> graph;
	
	public Model() {
		
		dao = new GOsalesDAO();
	}
	
	public List<String> getAllCountries(){
		return dao.getAllCountries();
	}
	
	public SimpleWeightedGraph<Retailers,DefaultWeightedEdge> creaGrafo(String country, int anno, int M){
		
		graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		List<Retailers> vertex = new ArrayList<>(dao.getAllRetailers(country));
		
		Map<Integer,Retailers> mapRetailers = new HashMap<>();
		
		for (Retailers r : vertex)
			mapRetailers.put(r.getCode(), r);
		
		dao.setAllProducts(country, anno, mapRetailers);
		
		Graphs.addAllVertices(graph, vertex);
		
		for (Retailers r1 : graph.vertexSet())
			for (Retailers r2 : graph.vertexSet())
				if (!r1.equals(r2) && !graph.containsEdge(r2, r1)) {
					Set<Integer> intersezione = new HashSet<>(r1.getProducts());
					intersezione.retainAll(r2.getProducts());
					if (intersezione.size()>=M)
						Graphs.addEdge(graph, r1, r2, intersezione.size());
				}
		
		return graph;		
		
	}
	
	private int dimensioneConnessa;;
	private Set<Retailers> connessi;
	
	public double connessi(Retailers r){
		
		dimensioneConnessa = 0;
		
		ConnectivityInspector<Retailers,DefaultWeightedEdge> conn = new ConnectivityInspector<>(graph);
		
		connessi = new HashSet<>(conn.connectedSetOf(r));
		
		dimensioneConnessa = connessi.size();
		
		double totPeso = 0;
		
		for (Retailers r1 : connessi)
			for (Retailers r2 : connessi)
				if (r2.getCode() > r1.getCode() && graph.containsEdge(r1, r2))
					totPeso += graph.getEdgeWeight(graph.getEdge(r1, r2));
					
		return totPeso;
	}
	
	double spesaTot;
	double ricavoTot;
	double percentualeSoddisfatti;
	
	
	public void run(Retailers r, int productCode, int anno, int Q_rimanenze, int N_rifornimento) {
		
		double cosotMerceUnitario = dao.getCost(productCode);
		
		spesaTot = 0;
		ricavoTot = 0;
		
		int numClienti = 0;
		int numClientiInsoddisfatti = 0;
		
		int qtaDisponibile = 0;
		
		double costoMerce = cosotMerceUnitario*N_rifornimento; // 1 giorno di ogni mese
		
		double prezzoUnitario = dao.getPrice(productCode);
		
		PriorityQueue<Event> queue = new PriorityQueue<>();
		
		for (int i= 1; i<= 12; i++) {
			this.connessi(r);
			double prob = 0.2 + 0.01*this.dimensioneConnessa;
			if (prob > 0.5)
				prob = 0.5;
			if (Math.random() < prob)
				queue.add(new Event(EventType.RIFORNIMENTO_08,LocalDate.of(anno, i, 1)));
			else
				queue.add(new Event(EventType.RIFORNIMENTO_OK,LocalDate.of(anno, i, 1)));
		}
		
		int avgD = (12*30)/dao.getNumEventi(r, anno, productCode);
		
		int avgQ = (int) dao.getqtaTot(r, anno, productCode)/avgD;
		
		for (int i = 15; i <= 365; i+=avgD) {
			queue.add(new Event(EventType.VENDITA,LocalDate.ofYearDay(anno, i)));
		}
		
		while(!queue.isEmpty()) {
			
			Event event = queue.poll();
			LocalDate date = event.getDate();
			EventType type = event.getType();
			
			System.out.println(event.toString());
			
			switch(type) {
			
			case RIFORNIMENTO_08:
				
				qtaDisponibile += (int) 0.8*N_rifornimento;
				spesaTot += ((int) 0.8*N_rifornimento) * cosotMerceUnitario;
				
				break;
				
			case RIFORNIMENTO_OK:
				
				qtaDisponibile += N_rifornimento;
				spesaTot += costoMerce;
				
				break;
				
			case VENDITA:
				
				numClienti++;
				
				if (avgQ <= qtaDisponibile) {
					qtaDisponibile -= avgQ;
					ricavoTot += prezzoUnitario * avgQ;
				}
				else {
					int qtaVendute = qtaDisponibile - avgQ;
					if (qtaVendute/avgQ < 0.9)
						numClientiInsoddisfatti++;
					if (qtaVendute>0)
						ricavoTot += prezzoUnitario * qtaVendute;
				}
				
				break;
			}
			
			
			
		}
			
		this.percentualeSoddisfatti = 1 - numClientiInsoddisfatti/numClienti;	
		
	}
	
	
	
	
	
	
	
	
	
	
	

	public Set<Retailers> getConnessi() {
		return connessi;
	}

	public void setConnessi(Set<Retailers> connessi) {
		this.connessi = connessi;
	}

	public double getSpesaTot() {
		return spesaTot;
	}

	public void setSpesaTot(double spesaTot) {
		this.spesaTot = spesaTot;
	}

	public double getRicavoTot() {
		return ricavoTot;
	}

	public void setRicavoTot(double ricavoTot) {
		this.ricavoTot = ricavoTot;
	}

	public double getPercentualeSoddisfatti() {
		return percentualeSoddisfatti;
	}

	public void setPercentualeSoddisfatti(double percentualeSoddisfatti) {
		this.percentualeSoddisfatti = percentualeSoddisfatti;
	}

	public GOsalesDAO getDao() {
		return dao;
	}

	public void setDao(GOsalesDAO dao) {
		this.dao = dao;
	}

	public SimpleWeightedGraph<Retailers, DefaultWeightedEdge> getGraph() {
		return graph;
	}

	public void setGraph(SimpleWeightedGraph<Retailers, DefaultWeightedEdge> graph) {
		this.graph = graph;
	}

	public int getDimensioneConnessa() {
		return dimensioneConnessa;
	}

	public void setDimensioneConnessa(int dimensioneConnessa) {
		this.dimensioneConnessa = dimensioneConnessa;
	}
	
}
