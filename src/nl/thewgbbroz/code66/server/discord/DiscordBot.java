package nl.thewgbbroz.code66.server.discord;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import nl.thewgbbroz.code66.server.Main;
import nl.thewgbbroz.code66.server.code.Code;
import nl.thewgbbroz.code66.server.code.CodeManager;
import nl.thewgbbroz.code66.server.utils.Utils;

public class DiscordBot extends ListenerAdapter {
	private Main main;
	private JDA jda;

	private String channel;
	private String broadcastChannel;
	private String downloadLink64, downloadLink32;

	public DiscordBot(Main main, String token, String channel, String broadcastChannel, String downloadLink64, String downloadLink32) {
		this.main = main;
		this.channel = channel;
		this.broadcastChannel = broadcastChannel;
		this.downloadLink64 = downloadLink64;
		this.downloadLink32 = downloadLink32;

		try {
			this.jda = new JDABuilder(AccountType.BOT).setToken(token).addEventListener(this).setGame(Game.of("with the database")) // Playing with the database
					.buildBlocking();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		broadcastMessage("_Started Code 66 discord bot. Type **!c66help** for help._");

		// Doesn't work some times.
		Thread shutdownThread = new Thread(new Runnable() {
			public void run() {
				broadcastMessage("_Shutting down bot server_");
			}
		});
		shutdownThread.setPriority(Thread.MAX_PRIORITY);

		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}

	private void broadcastMessage(String msg, JDA jda) {
		for (TextChannel tc : jda.getTextChannelsByName(broadcastChannel, true)) {
			tc.sendMessage(msg).queue();
		}
	}

	public void broadcastMessage(String msg) {
		broadcastMessage(msg, this.jda);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();

		if (!event.getTextChannel().getName().equals(this.channel)) {
			return;
		}

		String msg = message.getContent();

		if (msg.startsWith("!code")) {
			System.out.println("Received !code command by " + message.getAuthor().getName());

			Code code = main.getCodeManager().generateCode(channel);
			channel.sendMessage("Your code: " + code.getCodeString()).queue();
		} else if (msg.startsWith("!valid")) {
			System.out.println("Received !valid command by " + message.getAuthor().getName());

			String theMsg;

			try {
				String codeStr = msg.substring("!valid ".length());
				if (codeStr.length() != Code.LENGTH) {
					theMsg = "A code has " + Code.LENGTH + " numbers!";
				} else {
					int[] code = main.getCodeManager().parseCode(codeStr);

					if (code == null || !main.getCodeManager().isCorrectCode(code)) {
						theMsg = "That is an **invalid** code!";
					} else {
						Code c = main.getCodeManager().getCode(code);
						long validFor = CodeManager.CODE_EXPIRY - (System.currentTimeMillis() - c.getCreateTime());

						theMsg = "That is a valid code! It's valid for " + Utils.formatTime((int) (validFor / 1000));
					}
				}
			} catch (Exception e) {
				theMsg = "Invalid code format.";
			}

			channel.sendMessage(theMsg).queue();
		} else if (msg.startsWith("!use")) {
			System.out.println("Received !use command by " + message.getAuthor().getName());

			String theMsg;

			try {
				String codeStr = msg.substring("!use ".length());
				if (codeStr.length() != Code.LENGTH) {
					theMsg = "A code has " + Code.LENGTH + " numbers!";
				} else {
					int[] code = main.getCodeManager().parseCode(codeStr);

					if (code == null || !main.getCodeManager().isCorrectCode(code)) {
						theMsg = "That is an invalid code!";
					} else {
						main.getCodeManager().useCode(code);
						theMsg = "Successfully used that code (It will no longer be valid).";
					}
				}
			} catch (Exception e) {
				theMsg = "Invalid code format.";
			}

			channel.sendMessage(theMsg).queue();
		} else if (msg.startsWith("!download")) {
			System.out.println("Received !download command by " + message.getAuthor().getName());

			channel.sendMessage("64 bit download: " + downloadLink64 + "\n" + "32 bit download: " + downloadLink32).queue();
		} else if (msg.startsWith("!c66help")) {
			System.out.println("Received !c66help command by " + message.getAuthor().getName());

			channel.sendMessage("**!code**  - Generates a new code\n" + "**!valid <code>**  - Checks if a code is valid\n" + "**!use <code>**  - Uses a code such that it's no longer valid\n" + "**!download**  - Download the Code 66 code program").queue();
		}
	}
}
