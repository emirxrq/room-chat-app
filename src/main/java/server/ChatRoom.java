package server;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class ChatRoom {
	private String name;
	private Set<PrintWriter> clients;
	
	public ChatRoom(String name)
	{
		this.name = name;
		this.clients = new HashSet<>();
	}
	
	public String getName()
	{
		return name;
	}
	
	public synchronized void addClient(PrintWriter client)
	{
		clients.add(client);
	}
	
	public synchronized void removeClient(PrintWriter client)
	{
		clients.remove(client);
	}
	
	public synchronized void broadcast(String message, PrintWriter sender)
	{ 
		for (PrintWriter client : clients)
		{
			if (client != sender)
			{
				client.println(message);
				client.flush();
			}
		}
	}
	
}
