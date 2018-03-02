package nl.thewgbbroz.code66.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.thewgbbroz.code66.CheatStrings;
import nl.thewgbbroz.code66.StringsCollection;
import nl.thewgbbroz.code66.server.code.Code;
import nl.thewgbbroz.code66.utils.Utils;

public class Code66Server {
	private static final int NUM_STRINGS_TO_CLIENT = 4;
	private static final int CONNECTION_BLOCK_LOCK_TIME = 1000 * 60 * 60;

	private Main main;

	private ServerSocket server;

	private StringsCollection stringsCollection;

	private Map<String, Integer> timesConnected = new HashMap<>();
	private long timesConnectionsRefreshed = System.currentTimeMillis();

	public Code66Server(Main main, int port) {
		this.main = main;

		System.out.println("Starting server on port " + port + "..");
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Error starting server! " + e.toString());
			e.printStackTrace();
			main.getDiscordBot().broadcastMessage("_An error accured while trying to start the main server (" + e.toString() + "). Please notify one of our staff members._");
			return;
		}

		System.out.println("Started server!");

		try {
			loadStringsCollection();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not load strings collection!");
		}

		acceptClients();
	}

	public void tick() {
		if (System.currentTimeMillis() - timesConnectionsRefreshed > CONNECTION_BLOCK_LOCK_TIME) {
			System.out.println("Refreshing times connected for each IP!");
			timesConnected.clear();

			timesConnectionsRefreshed = System.currentTimeMillis();
		}
	}

	private void acceptClients() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Socket socket = server.accept();
						System.out.println("Client connected " + socket.getInetAddress().getHostAddress());

						new Thread(new Runnable() {
							public void run() {
								try {
									handleClient(socket);
								} catch (IOException e) {
									System.out.println("Error while dealing with client! " + e.toString());
									e.printStackTrace();
								}
							}
						}).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void handleClient(Socket socket) throws IOException {
		String ip = socket.getInetAddress().getHostAddress();
		int connected = timesConnected.containsKey(ip) ? timesConnected.get(ip) : 0;
		connected++;
		timesConnected.put(ip, connected);
		System.out.println(ip + " connceted " + connected + " times.");

		if (connected >= 10) {
			socket.close();

			System.out.println("Refusing to handle client.");

			if (connected == 10) {
				main.getDiscordBot().broadcastMessage("WARNING! IP " + ip + " connected more than 10 times in one hour. Refusing to accept the connection for the next hour.");
			} else if (connected == 1000) {
				main.getDiscordBot().broadcastMessage("WARNING! IP " + ip + " connected for more than 1000 times in one hour. This is most likely a DDoS attack! Our services may be down for a few hours, depending on the severity");
			}

			return;
		}

		/*
		 * if(!fileToSend.exists()) {
		 * System.out.println("The file to send doesn't exist, disconnecting client.");
		 * socket.close(); return; }
		 */

		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();

		byte[] codeBytes = new byte[Code.LENGTH];
		in.read(codeBytes, 0, Code.LENGTH);

		int[] code = Utils.toIntArray(codeBytes);
		boolean correctCode = main.getCodeManager().isCorrectCode(code);
		if (correctCode) {
			System.out.println("Client used a valid code. Sending strings and file..");
			main.getCodeManager().useCode(code);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Send some of the strings to the client
			stringsCollection.getSome(NUM_STRINGS_TO_CLIENT).serialize(baos);

			// {FILE} is to throw the client off guard when someone's deobfuscating the
			// code.
			// The file name is used to en/decrypt some times.
			byte[] encrypted = Utils.encrypt(baos.toByteArray(), "{FILE}" + new String(codeBytes));
			out.write(encrypted);

			System.out.println("Done handling client!");
		} else {
			System.out.println("Client used an invalid code!");
		}

		socket.close();
		System.out.println("Client got disconnected.");
	}

	private void loadStringsCollection() throws IOException {
		System.out.println("Loading strings..");

		File dir = new File("strings/");
		if (!dir.exists()) {
			System.out.println("WARNING! Could not find strings directory. Creating one! This will result in an error. Please restart the server once the strings are in place!");
			dir.mkdirs();
		}

		File clientsDir = new File(dir, "clients/");
		if (!clientsDir.exists()) {
			System.out.println("WARNING! Could not find clients directory. Creating one! This will result in an error. Please restart the server once the strings are in place!");
			dir.mkdirs();
		}

		List<CheatStrings> clientStrings = new ArrayList<>();
		for (File f : clientsDir.listFiles()) {
			CheatStrings cs = CheatStrings.fromStream(new FileInputStream(f));
			clientStrings.add(cs);

			System.out.println("Loaded client strings " + f.getName() + " ('" + cs.getClientName() + "', " + cs.getCheatStrings().size() + " strings)");
		}

		CheatStrings csrssStrings = CheatStrings.fromStream(new FileInputStream(new File(dir, "csrss.txt")));
		CheatStrings explorerStrings = CheatStrings.fromStream(new FileInputStream(new File(dir, "explorer.txt")));
		CheatStrings dwmStrings = CheatStrings.fromStream(new FileInputStream(new File(dir, "dwm.txt")));

		stringsCollection = new StringsCollection(clientStrings, csrssStrings, explorerStrings, dwmStrings);

		System.out.println("Done loading strings.");
	}
}
