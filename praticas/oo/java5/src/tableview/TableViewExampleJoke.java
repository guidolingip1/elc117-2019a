import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

  
public class TableViewExampleJoke extends Application {
  
  private HttpJSONService http = new HttpJSONService();
  
  private TableView<TableData> table = new TableView<TableData>();
  
  private final ObservableList<TableData> data =
    FXCollections.observableArrayList();
  
  public static void main(String[] args) {
    launch(args);
  }
  
  @Override
  public void start(Stage stage) {
    
    Scene scene = new Scene(new Group());
    
    final Label label = new Label("Gerador de Insultos");
    label.setFont(new Font("Arial", 20));
    
    TableColumn<TableData,String> fstCol = new TableColumn<TableData,String>("Number");
    fstCol.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
    
    TableColumn<TableData,String> sndCol = new TableColumn<TableData,String>("Insult");
    sndCol.setCellValueFactory(cellData -> cellData.getValue().insultProperty());
    
    TableColumn<TableData,String> trdCol = new TableColumn<TableData,String>("Shown");
    trdCol.setCellValueFactory(cellData -> cellData.getValue().shownProperty());
    
    sndCol.setCellFactory(column -> {
      return new TableCell<TableData, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);
          if (item == null || empty) {
            setText(null);
            setStyle("");
          } else {
            Text text = new Text(item.toString());
            
            text.wrappingWidthProperty().bind(widthProperty());
            text.textProperty().bind(itemProperty());
            this.setWrapText(true);
            setGraphic(text);
            
          }
        }
      };
    });
    
    
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.getColumns().add(fstCol);
    table.getColumns().add(sndCol);
    table.getColumns().add(trdCol);

    table.setItems(data);
    
    Button btn = new Button("Add a Insult");
    
    btn.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        Map json = null;
        try {
          json = http.sendGet("https://evilinsult.com/generate_insult.php?lang=en&type=json");
        } catch (Exception e) {
          Alert alert = new Alert(AlertType.WARNING);
          alert.setTitle("Warning");
          alert.setHeaderText("Connection failed");
          alert.setContentText("Please check your Internet connection!");
          alert.showAndWait();
        }
        if (json != null)
          data.add(new TableData((String)json.get("number"), (String)json.get("insult"), (String)json.get("shown")));
      }
    });
    
    final VBox vbox = new VBox();
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 10, 10, 10));
    vbox.getChildren().addAll(label, table, btn);
    
    vbox.applyCss();
    vbox.layout();
    
    stage.setScene(new Scene(vbox, 500, 500));
    stage.show();
    
  }
  
  public class TableData {
    private final SimpleStringProperty number;
    private final SimpleStringProperty insult;
    private final SimpleStringProperty shown;
    
    private TableData(String number, String insult, String shown) {
      this.number = new SimpleStringProperty(number);
      this.insult = new SimpleStringProperty(insult);
      this.shown = new SimpleStringProperty(shown);
    }
    
    public SimpleStringProperty shownProperty() {
      return shown;
    }
    
    public String getShown() {
      return shown.get();
    }
    
    public void setShown(String shown) {
      this.shown.set(shown);
    }
    
    public SimpleStringProperty numberProperty() {
      return number;
    }
    public String getNumber() {
      return number.get();
    }
    public void setNumber(String number) {
      this.number.set(number);
    }
    public SimpleStringProperty insultProperty() {
      return insult;
    }
    public String getInsult() {
      return insult.get();
    }
    public void setInsult(String insult) {
      this.insult.set(insult);
    }
    
  }
}


class HttpJSONService {
  
  private final String USER_AGENT = "Mozilla/5.0";
  private JSONParsing engine = new JSONParsing();
  
  // HTTP GET request
  public Map sendGet(String url) throws Exception {
        
    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    
    con.setRequestMethod("GET");
    con.setRequestProperty("User-Agent", USER_AGENT);
    
    int responseCode = con.getResponseCode();
    
    System.out.println("\n'GET' request sent to URL : " + url);
    System.out.println("Response Code : " + responseCode);
    
    BufferedReader in = new BufferedReader(
      new InputStreamReader(con.getInputStream()));
    StringBuffer response = new StringBuffer();
    String inputLine;
    
    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    
    // Print result
    // System.out.println(response.toString());

    // Parse JSON result
    JSONParsing engine = new JSONParsing();
    return engine.parseJSON(response.toString());
  }
  
}


class JSONParsing {
  
  private ScriptEngine engine;
  
  public JSONParsing() {
    ScriptEngineManager sem = new ScriptEngineManager();
    this.engine = sem.getEngineByName("javascript");
  }
  
  public Map parseJSON(String json) throws IOException, ScriptException {
    String script = "Java.asJSONCompatible(" + json + ")";
    Object result = this.engine.eval(script);
    Map contents = (Map) result;
    return contents;
  }
  
}
