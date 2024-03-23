package client.scenes.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;

import com.mysql.cj.protocol.Resultset;

import database.database;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class MainController {
	@FXML
	private VBox messagesPane;
	@FXML
	private ListView<String> roomsPane;
	@FXML
	private TextField yourMessage;
	@FXML
	private TextField yourName;
	@FXML
	private Button joinBtn;
	@FXML
	private Button sendBtn;
	@FXML
	private TextField roomName;
	@FXML
	private Button createRoom;

	@FXML
	public void sendMsg() {
		addMessage("User", yourMessage.getText());
	}

	public JSONObject jsonobject(Object... keyValuePairs) {
		if (keyValuePairs.length % 2 != 0) {
			throw new IllegalArgumentException("Anahtar-değer çiftlerinin sayısı çift olmalıdır.");
		}

		JSONObject json = new JSONObject();
		for (int i = 0; i < keyValuePairs.length; i += 2) {
			if (!(keyValuePairs[i] instanceof String)) {
				throw new IllegalArgumentException("Anahtar bir String olmalıdır.");
			}
			String key = (String) keyValuePairs[i];
			Object value = keyValuePairs[i + 1];
			json.put(key, value);
		}
		return json;
	}

	@FXML
	public void createRoom() throws IOException {
		String name = roomName.getText();
		roomName.setText("");
		if (name == null || name.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Hata");
			alert.setHeaderText(null);
			alert.setContentText("Oda ismini boş bırakamazsınız.");
			alert.showAndWait();
			return;
		}

		JSONObject createRoomMsg = jsonobject("NAME", name, "TYPE", "CMD", "CMD_TYPE", "CREATE_ROOM");
		out.println(createRoomMsg);

	}

	private final String SERVER = "localhost";
	private Socket socket = null;
	private final int PORT = 12345;
	PrintWriter out = null;
	String currentRoomName;
	String username;

	  public static CompletableFuture<Void> fetchRoomsAsync(ListView<String> roomsPane) {
	        return CompletableFuture.runAsync(() -> {
	            Connection connection = null;
	            PreparedStatement statement = null;
	            ResultSet results = null;

	            try {
	                connection = database.connect();
	                String sql = "SELECT * FROM rooms";
	                statement = connection.prepareStatement(sql);
	                results = statement.executeQuery();
	                while (results.next()) {
	                    String roomName = results.getString("roomName");
	                    Platform.runLater(() -> roomsPane.getItems().add(roomName));
	                }
	            } catch (SQLException e) {
	                e.printStackTrace();
	            } finally {
	                try {
	                    if (results != null) results.close();
	                    if (statement != null) statement.close();
	                    if (connection != null) connection.close();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                }
	            }
	        });
	    }
	  
	@FXML
	public void connectSocket() throws SQLException {
		username = yourName.getText();
		try {
			socket = new Socket(SERVER, PORT);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.println(username);

			String connectServerResponse = in.readLine();

			if (!connectServerResponse.toLowerCase().contains("bağlandınız")) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Bilinmeyen socket");
				alert.setHeaderText(null);
				alert.setContentText("Hata: " + connectServerResponse);

				alert.showAndWait();
				return;
			} else {
		        CompletableFuture<Void> future = fetchRoomsAsync(roomsPane);
				
				
				yourName.setDisable(true);
				yourMessage.setDisable(false);
				messagesPane.setDisable(false);
				roomsPane.setDisable(false);
				sendBtn.setDisable(false);
				joinBtn.setDisable(true);
				createRoom.setDisable(false);
				roomName.setDisable(false);

				new Thread(() -> {

					try {
						String message;
						while ((message = in.readLine()) != null) {
							String finalMessage = message;
							JSONObject object = new JSONObject(finalMessage);

							Platform.runLater(() -> {
								String type = object.getString("TYPE");
								if ("NEW_ROOM".equals(type)) {
									String newRoomName = object.getString("NEW_ROOM_NAME");
									ObservableList<String> items = roomsPane.getItems();
									items.add(newRoomName);
								}
								if ("NEW_JOIN_ROOM".equals(type)) {
									currentRoomName = object.getString("NEW_ROOM_NAME");
								}
								if ("SOMEONE_NEW_JOINED".equals(type))
								{
									String roomName = object.getString("ROOM_NAME");
									String username = object.getString("USERNAME");
									if (roomName.equals(currentRoomName))
									{
										addConnectInfo(username);
									}
								}
								if ("SOMEONE_NEW_LEFT".equals(type))
								{
									String oldRoomName = object.getString("OLD_ROOM_NAME");
									String username = object.getString("USERNAME");
									if (oldRoomName.equals(currentRoomName))
									{
										addLeftInfo(username);
									}
								}
								if ("REFRESH_MESSAGES".equals(type)) {
									messagesPane.getChildren().clear();
								}
								if ("ROOM_MSG".equals(type)) {
									String roomName = object.getString("ROOM_NAME");
									String msg = object.getString("MSG");
									String sender = object.getString("SENDER");
									if (roomName.equals(currentRoomName)) {
										addMessage(sender, msg);
									}
								}
								if ("TO_USER".equals(type)) {
									String icon = object.getString("ICON");

									Alert alert;
									if ("INFO".equals(icon)) {
										alert = new Alert(AlertType.INFORMATION);
										alert.setContentText(object.getString("MSG"));
										alert.setHeaderText(null);
										alert.showAndWait();

									} else if ("ERR".equals(icon)) {
										alert = new Alert(AlertType.ERROR);
										alert.setContentText(object.getString("MSG"));
										alert.setHeaderText(null);
										alert.showAndWait();
									}
								}
							});
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();

			}
		} catch (UnknownHostException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Bilinmeyen socket");
			alert.setHeaderText(null);
			alert.setContentText("Bilinmeyen bir host. Sunucu adresini ve portunu kontrol edin.");

			alert.showAndWait();
		} catch (ConnectException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Bağlantı reddedildi");
			alert.setHeaderText(null);
			alert.setContentText("Bilinmeyen bir host olabilir. Hata mesajı: " + e);

			alert.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void roomSelected() {
		String selectedItem = roomsPane.getSelectionModel().getSelectedItem();

		if (selectedItem != null) {
			if (currentRoomName == null || currentRoomName.isEmpty())
			{
				JSONObject createRoomMsg = jsonobject("NEW_ROOM_NAME", selectedItem, "TYPE", "CMD", "CMD_TYPE", "CONNECT_ROOM", "CONNECT_ROOM_TYPE", "CONNECT", "USERNAME", username);	
				out.println(createRoomMsg);
			}
			else {
				JSONObject createRoomMsg = jsonobject("NEW_ROOM_NAME", selectedItem, "TYPE", "CMD", "CMD_TYPE", "CONNECT_ROOM", "CONNECT_ROOM_TYPE", "LEFT_AND_CONNECT", "OLD_ROOM_NAME", currentRoomName, "USERNAME", username);	
				out.println(createRoomMsg);
			}
			
		}
	}

	@FXML
	public void sendMessage() {
		String msg = yourMessage.getText();
		yourMessage.setText("");
		if (msg == null || msg.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText(null);
			alert.setContentText("Boş mesaj gönderemezsiniz.");
			alert.showAndWait();
		}

		JSONObject finalMsg = jsonobject("TYPE", "ROOM_MSG", "MSG", msg, "SENDER", username, "ROOM_NAME",
				currentRoomName);
		out.println(finalMsg);
	}

	private void addMessage(String userName, String messageText) {
		VBox messageBox = new VBox();
		messageBox.setStyle("-fx-background-radius: 10; -fx-padding: 10 10 0 10; ");
		Label userLabel = new Label(userName);
		userLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
		Label messageLabel = new Label(messageText);
	    messageLabel.setWrapText(true);

		
		messageBox.getChildren().addAll(userLabel, messageLabel);
		messageBox.setSpacing(0.1);
		messagesPane.getChildren().add(messageBox);
	}
	public void addConnectInfo(String userName)
	{
		VBox messageBox = new VBox();
		messageBox.setStyle("-fx-background-radius: 10; -fx-padding: 10 10 0 10; ");
		Label userNameLabel = new Label(userName + " odaya girdi.");
		userNameLabel.setStyle("-fx-text-fill: #3492eb; -fx-font-weight: bold;");
		userNameLabel.setWrapText(true);

		
		messageBox.getChildren().addAll(userNameLabel);
		messageBox.setSpacing(0.1);
		messagesPane.getChildren().add(messageBox);
	}
	public void addLeftInfo(String userName)
	{
		VBox messageBox = new VBox();
		messageBox.setStyle("-fx-background-radius: 10; -fx-padding: 10 10 0 10; ");
		Label userNameLabel = new Label(userName + " odadan ayrıldı.");
		userNameLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
		userNameLabel.setWrapText(true);

		
		messageBox.getChildren().addAll(userNameLabel);
		messageBox.setSpacing(0.1);
		messagesPane.getChildren().add(messageBox);
	}
}
