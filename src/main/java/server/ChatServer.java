package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;

import org.json.JSONObject;

import database.database;

public class ChatServer {
	private static final int PORT = 12345;
	private static Map<String, ChatRoom> chatRooms;
	private static Map<String, PrintWriter> allClients = new HashMap<>();
	
	public static void main(String[] args) throws SQLException {
		Connection connection = database.connect();
		PreparedStatement statement = null;
	    ResultSet resultSet = null;
		chatRooms = new HashMap<>();
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		try {
			 String sql = "SELECT roomName FROM rooms";
	            statement = connection.prepareStatement(sql);
	            resultSet = statement.executeQuery();

	            while (resultSet.next()) {
	                String roomName = resultSet.getString("roomName");
	                ChatRoom newRoom = new ChatRoom(roomName);
	                chatRooms.put(roomName, newRoom);
	            }
	            
			serverSocket = new ServerSocket(PORT);
			System.out.println("Sunucu dinleniyor...");
			
			while (true)
			{
				clientSocket = serverSocket.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				
				String clientUsername = in.readLine();
				
				if (clientUsername == null || clientUsername.isEmpty())
				{
					out.println("Kullanıcı adı bilgisi boş olamaz.");
					clientSocket.close();
					continue;
				}
				
				if (allClients.containsKey(clientUsername))
				{
					out.println("Bu kullanıcı adı ile giriş yapan başka bir kullanıcı bulunuyor, lütfen farklı bir kullanıcı adı ile giriş yapmayı deneyin.");
					clientSocket.close();
					continue;
				}
				
				out.println("Sunucuya başarıyla bağlandınız.");
				allClients.put(clientUsername, out);
				new Thread(new ClientHandler(clientUsername, clientSocket, out)).start();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
	
	private static class ClientHandler implements Runnable {
		private Socket clientSocket;
		public String clientUsername;
		public PrintWriter writer;
		private BufferedReader reader;
		private ChatRoom currentRoom;
		
		public ClientHandler(String username, Socket clientSocket, PrintWriter out)
		{
			try {
				this.clientSocket = clientSocket;
				this.writer = out;
				this.clientUsername = username;
				this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		
		public JSONObject jsonobject(Object...keyValuePairs)
		{
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
		public void sendMsgToClient(Object...KeyValuePairs)
		{
			JSONObject message = jsonobject(KeyValuePairs);
			writer.println(message);
			writer.flush();
		}
		public void broadcast(JSONObject message)
		{
			for (Map.Entry<String, PrintWriter> entry : allClients.entrySet()) {
                String username = entry.getKey();
                PrintWriter writer = entry.getValue();

                
                if (!username.equals(clientUsername)) {
                    writer.println(message);
                }
            }
		}
		public void allBroadcast(JSONObject message)
		{
			for (Map.Entry<String, PrintWriter> entry : allClients.entrySet()) {
                String username = entry.getKey();
                PrintWriter writer = entry.getValue();
                writer.println(message);
                
            }
		}
		
		
		public void connectRoom(String roomName) {
			JSONObject message = new JSONObject();
			ChatRoom room = chatRooms.get(roomName);
			
			if (room != null)
			{
				if (currentRoom != null)
				{
					currentRoom.removeClient(writer);
				}
				
				currentRoom = room;
				room.addClient(writer);
				sendMsgToClient("MSG", "Odaya başarıyla bağlanıldı.", "TYPE", "TO_USER", "ICON", "INFO");
				sendMsgToClient("TYPE", "REFRESH_MESSAGES", "NEW_ROOM_NAME", roomName);
				sendMsgToClient("TYPE", "NEW_JOIN_ROOM", "NEW_ROOM_NAME", roomName);
			}
			else {
				sendMsgToClient("MSG", "Böyle bir oda bulunamadı!", "TYPE", "TO_USER", "ICON", "ERR");
			}
		}
		
		public void createRoom(String roomName) throws IOException
		{
			JSONObject message = new JSONObject();
			
			if (roomName == null || roomName.isEmpty())
			{
				sendMsgToClient("MSG", "Oda ismi boş bırakılamaz!", "TYPE", "TO_USER", "ICON", "ERR");
				return;
			}
			if (chatRooms.containsKey(roomName))
			{
				sendMsgToClient("MSG", "Bu isimle başka bir oda zaten açılmış.", "TYPE", "TO_USER", "ICON", "ERR");

				return;
			}
			ChatRoom newRoom = new ChatRoom(roomName);
			chatRooms.put(roomName, newRoom);
			Connection connection = database.connect();
			PreparedStatement statement = null;
		    ResultSet resultSet = null;
		    
			 String sql = "INSERT INTO rooms (roomName) VALUES (?)";
	            try {
					statement = connection.prepareStatement(sql);
					statement.setString(1, roomName);
			        statement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	           
			
			sendMsgToClient("MSG", "Oda başarıyla oluşturuldu!", "TYPE", "TO_USER", "ICON", "INFO");
			sendMsgToClient("TYPE", "NEW_ROOM", "NEW_ROOM_NAME", roomName);
			broadcast(jsonobject("TYPE", "NEW_ROOM", "NEW_ROOM_NAME", roomName));
			
		}
		
		@Override
		public void run()
		{
			JSONObject jsonObject;
			try {

				String message;
				while ((message = reader.readLine()) != null)
				{
					jsonObject = new JSONObject(message);
					
					
					String msgType = jsonObject.getString("TYPE");
					if ("CMD".equals(msgType)) { 
				        String commandType = jsonObject.getString("CMD_TYPE");
				        if ("CREATE_ROOM".equals(commandType)) { 
				            String roomName = jsonObject.getString("NAME");
				            createRoom(roomName);
				        }
				        if ("CONNECT_ROOM".equals(commandType))
				        {
				        	String connectRoomType = jsonObject.getString("CONNECT_ROOM_TYPE");
				        	String roomName = jsonObject.getString("NEW_ROOM_NAME");
				        	String username = jsonObject.getString("USERNAME");
				        	if (connectRoomType.equals("LEFT_AND_CONNECT"))
				        	{
					        	String oldRoomName = jsonObject.getString("OLD_ROOM_NAME");
					        	allBroadcast(jsonobject("TYPE", "SOMEONE_NEW_LEFT", "USERNAME", username, "OLD_ROOM_NAME", oldRoomName));
				        	}
				        
				        	connectRoom(roomName);
				        	allBroadcast(jsonobject("TYPE", "SOMEONE_NEW_JOINED", "USERNAME", username, "ROOM_NAME", roomName));
				        }
				    }
					if ("ROOM_MSG".equals(msgType))
					{
						String msg = jsonObject.getString("MSG");
						String roomName = jsonObject.getString("ROOM_NAME");
						String sender = jsonObject.getString("SENDER");
												
						allBroadcast(jsonobject("TYPE", "ROOM_MSG", "MSG", msg, "SENDER", sender, "ROOM_NAME", roomName));
											}
					
					
				}
			}
			catch (IOException e)
			{
			}
			finally {
	            try {
	            	allClients.remove(clientUsername);
	                clientSocket.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
		}
	}

}
