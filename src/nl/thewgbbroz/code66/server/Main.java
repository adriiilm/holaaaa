package nl.thewgbbroz.code66.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import nl.thewgbbroz.code66.server.code.CodeManager;
import nl.thewgbbroz.code66.server.discord.DiscordBot;

public class Main {
	private CodeManager codeManager;
	private DiscordBot discordBot;
	private Code66Server server;

	private Main(int port, String discordToken, String discordChannel, String discordBroadcastChannel, String discordDownloadLink64, String discordDownloadLink32) throws Exception {
		discordBot = new DiscordBot(this, discordToken, discordChannel, discordBroadcastChannel, discordDownloadLink64, discordDownloadLink32);
		codeManager = new CodeManager(this);
		server = new Code66Server(this, port);

		new Thread(new Runnable() {
			public void run() {
				int tps = 5;
				int waitMS = 1000 / tps;
				long start, wait;

				while (true) {
					start = System.currentTimeMillis();

					tick();

					wait = waitMS - (System.currentTimeMillis() - start);
					if (wait > 0) {
						try {
							Thread.sleep(wait);
						} catch (Exception e) {
						}
					}
				}
			}
		}).start();
	}

	private void tick() {
		codeManager.tick();
		server.tick();
	}

	public CodeManager getCodeManager() {
		return codeManager;
	}

	public DiscordBot getDiscordBot() {
		return discordBot;
	}

	public Code66Server getServer() {
		return server;
	}

	public static void main(String[] args) throws Exception {
		File propFile = new File("config.properties");
		if (!propFile.exists()) {
			System.out.println("No config.properties file found, saving default config to disk.");
			saveResource("/config.properties", propFile);
		}

		Properties prop = new Properties();
		prop.load(new FileInputStream(propFile));

		int port = Integer.valueOf(prop.getProperty("port"));
		System.out.println("Server port: " + port);

		String discordToken = prop.getProperty("discord-token");
		System.out.println("Discord token: " + discordToken);

		String discordChannel = prop.getProperty("discord-channel");
		System.out.println("Discord channel: " + discordChannel);

		String discordBroadcastChannel = prop.getProperty("discord-broadcast-channel");
		System.out.println("Discord broadcast channel: " + discordBroadcastChannel);

		String discordDownloadLink64 = prop.getProperty("discord-download-link-64-bit");
		System.out.println("Discord 64 bit download link: " + discordDownloadLink64);

		String discordDownloadLink32 = prop.getProperty("discord-download-link-32-bit");
		System.out.println("Discord 32 bit download link: " + discordDownloadLink32);

		System.out.println();

		new Main(port, discordToken, discordChannel, discordBroadcastChannel, discordDownloadLink64, discordDownloadLink32);
	}

	private static void saveResource(String name, File target) throws IOException {
		InputStream in = Main.class.getResourceAsStream(name);
		FileOutputStream fos = new FileOutputStream(target);

		byte[] buf = new byte[1024];
		int read;
		while ((read = in.read(buf)) > 0) {
			fos.write(buf, 0, read);
		}

		in.close();
		fos.close();
	}
}
