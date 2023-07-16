package it.polito.tdp.gosales;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.gosales.model.Arco;
import it.polito.tdp.gosales.model.Model;
import it.polito.tdp.gosales.model.Retailers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLController {
	
	private Model model;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnAnalizzaComponente;

    @FXML
    private Button btnCreaGrafo;

    @FXML
    private Button btnSimula;

    @FXML
    private ComboBox<Integer> cmbAnno;

    @FXML
    private ComboBox<String> cmbNazione;

    @FXML
    private ComboBox<Integer> cmbProdotto;

    @FXML
    private ComboBox<Retailers> cmbRivenditore;

    @FXML
    private TextArea txtArchi;

    @FXML
    private TextField txtN;

    @FXML
    private TextField txtNProdotti;

    @FXML
    private TextField txtQ;

    @FXML
    private TextArea txtResult;

    @FXML
    private TextArea txtVertici;

    @FXML
    void doAnalizzaComponente(ActionEvent event) {
    	
    	if (this.cmbRivenditore.getValue() == null) {
    		txtResult.setText("Scegli un rivenditore");
    		return;
    	}
    	
    	double pesoTot = model.connessi(cmbRivenditore.getValue());
    	
    	this.cmbProdotto.getItems().addAll(this.cmbRivenditore.getValue().getProducts());
    	
    	txtResult.appendText("La componente connessa di " + cmbRivenditore.getValue().toString() + " ha dimensione " + model.getDimensioneConnessa() + '\n');
    	txtResult.appendText("Il peso totale degli archi della componente connessa è " + (int) pesoTot);
    }

    @FXML
    void doCreaGrafo(ActionEvent event) {
    	
    	txtResult.clear();
    	txtArchi.clear();
    	txtVertici.clear();
    	
    	if (this.cmbNazione.getValue() == null) {
    		txtResult.setText("Scegli una nazione");
    		return;
    	}
    	
    	if (this.cmbAnno.getValue() == null) {
    		txtResult.setText("Scegli un anno");
    		return;
    	}
    	
    	if (this.txtNProdotti.getText() == ""){
    		txtResult.setText("Inserisci un numero di prodotti in comune");
    		return;
    	}
    	try {
    		int x = Integer.parseInt(txtNProdotti.getText());
    		if (x <= 0) {
    			txtResult.setText("Inserisci un numero di prodotti in comune positivo (>=0)");
    			return;
    		}
    	}
    	catch (Exception e) {
    		txtResult.setText("Inserisci un valore numerico intero per N");
    		return;
    	}


    	SimpleWeightedGraph<Retailers,DefaultWeightedEdge> graph = model.creaGrafo(cmbNazione.getValue(), cmbAnno.getValue(), Integer.parseInt(this.txtNProdotti.getText()));
    	
    	txtResult.setText("Grafo creato con " + graph.vertexSet().size() + " vertici e " + graph.edgeSet().size() + " archi.\n\n");
    	
    	List<Retailers> vertici = new ArrayList<>(graph.vertexSet());
    	
    	Collections.sort(vertici);
    	
    	for (Retailers r : vertici)
    		txtVertici.appendText(r.toString() + '\n');
    	
    	List<Arco> archi = new ArrayList<>();
    	
    	for (DefaultWeightedEdge edge : graph.edgeSet())
    		archi.add(new Arco(graph.getEdgeSource(edge), graph.getEdgeTarget(edge),graph.getEdgeWeight(edge)));
    	
    	Collections.sort(archi);
    	
    	for (Arco arco : archi)
    		txtArchi.appendText(arco.toString() + '\n');
    	
    	this.cmbProdotto.setDisable(false);
    	this.cmbRivenditore.setDisable(false);
    	this.cmbRivenditore.getItems().clear();
    	this.cmbRivenditore.getItems().addAll(vertici);
    	
    	this.btnAnalizzaComponente.setDisable(false);
    	this.btnSimula.setDisable(false);
    	
    	this.txtN.setDisable(false);
    	this.txtQ.setDisable(false);
    	
    	
    	
    }

    @FXML
    void doSimulazione(ActionEvent event) {
    	
    	if (this.cmbRivenditore.getValue() == null) {
    		txtResult.setText("Scegli un rivenditore");
    		return;
    	}
    	
    	if (this.cmbProdotto.getValue() == null) {
    		txtResult.setText("Scegli un prodotto");
    		return;
    	}
    	

    	if (this.txtN.getText() == ""){
    		txtResult.setText("Inserisci N");
    		return;
    	}
    	try {
    		int x = Integer.parseInt(txtN.getText());
    		if (x < 0) {
    			txtResult.setText("Inserisci un numero N positivo (>=0)");
    			return;
    		}
    	}
    	catch (Exception e) {
    		txtResult.setText("Inserisci un valore numerico intero per N");
    		return;
    	}
    	
    	if (this.txtQ.getText() == ""){
    		txtResult.setText("Inserisci Q");
    		return;
    	}
    	try {
    		int x = Integer.parseInt(txtQ.getText());
    		if (x < 0) {
    			txtResult.setText("Inserisci un numero Q positivo (>=0)");
    			return;
    		}
    	}
    	catch (Exception e) {
    		txtResult.setText("Inserisci un valore numerico intero per Q");
    		return;
    	}
    	
    	model.run(cmbRivenditore.getValue(), cmbProdotto.getValue(), cmbAnno.getValue(), Integer.parseInt(txtQ.getText()), Integer.parseInt(txtN.getText()));
    	
    	txtResult.setText("Simulazione eseguita. Il risultato è:\n");
    	txtResult.appendText("Soddisfazione: " + model.getPercentualeSoddisfatti() + '\n');
    	txtResult.appendText("Costo: " + model.getSpesaTot() + " k$\n");
    	txtResult.appendText("Ricavo: " + model.getRicavoTot() + " k$\n");
    	txtResult.appendText("Profitto: " + (model.getRicavoTot()-model.getSpesaTot())+ " k$\n");
    	

    }

    @FXML
    void initialize() {
        assert btnAnalizzaComponente != null : "fx:id=\"btnAnalizzaComponente\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnCreaGrafo != null : "fx:id=\"btnCreaGrafo\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnSimula != null : "fx:id=\"btnSimula\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbAnno != null : "fx:id=\"cmbAnno\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbNazione != null : "fx:id=\"cmbNazione\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbProdotto != null : "fx:id=\"cmbProdotto\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbRivenditore != null : "fx:id=\"cmbRivenditore\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtArchi != null : "fx:id=\"txtArchi\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtN != null : "fx:id=\"txtN\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtNProdotti != null : "fx:id=\"txtNProdotti\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtQ != null : "fx:id=\"txtQ\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtVertici != null : "fx:id=\"txtVertici\" was not injected: check your FXML file 'Scene.fxml'.";

    }
    
    public void setModel(Model model) {
    	this.model = model;
    	
    	for (int i=2015; i<=2018; i++)
    		this.cmbAnno.getItems().add(i);
    	
    	this.cmbNazione.getItems().addAll(model.getAllCountries());
    	
    	this.btnAnalizzaComponente.setDisable(true);
    	this.btnSimula.setDisable(true);
    }

}
