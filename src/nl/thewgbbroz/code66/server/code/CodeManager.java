package nl.thewgbbroz.code66.server.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.dv8tion.jda.core.entities.MessageChannel;
import nl.thewgbbroz.code66.server.Main;

public class CodeManager {
	public static final long CODE_EXPIRY = 1000 * 60 * 5; // 5 minutes

	private static final Random RAND = new Random();

	private Main main;
	private List<Code> activeCodes = new ArrayList<>();

	public CodeManager(Main main) {
		this.main = main;
	}

	public void tick() {
		long now = System.currentTimeMillis();

		for (int i = 0; i < activeCodes.size(); i++) {
			Code c = activeCodes.get(i);
			if (now - c.getCreateTime() > CODE_EXPIRY) {
				System.out.println("Code " + c.toString() + " got expired!");

				// main.getDiscordBot().broadcastMessage("Code " + c.getCodeString() + " got
				// expired!");
				c.messageChannel.sendMessage("Code " + c.getCodeString() + " got expired!");

				activeCodes.remove(i--);
			}
		}
	}

	public Code generateCode(MessageChannel discordChannel) {
		int[] code = new int[Code.LENGTH];
		for (int i = 0; i < code.length; i++)
			code[i] = RAND.nextInt(10); // 0-9

		Code c = new Code(code, discordChannel);
		activeCodes.add(c);

		System.out.println("Generated code " + c.toString());

		return c;
	}

	public boolean isCorrectCode(int[] code) {
		return getCode(code) != null;
	}

	public Code getCode(int[] code) {
		if (code.length != Code.LENGTH)
			throw new IllegalArgumentException("code.length must be equal to " + Code.LENGTH + "!");

		for (Code c : activeCodes) {
			if (c.equals(code))
				return c;
		}

		return null;
	}

	public void useCode(int[] code) {
		if (code.length != Code.LENGTH)
			throw new IllegalArgumentException("code.length must be equal to " + Code.LENGTH + "!");

		for (int i = 0; i < activeCodes.size(); i++) {
			Code c = activeCodes.get(i);

			if (c.equals(code)) {
				System.out.println("Code " + c.toString() + " got used!");
				main.getDiscordBot().broadcastMessage("Code " + c.getCodeString() + " got used!");

				activeCodes.remove(i--);
			}
		}
	}

	public int[] parseCode(String s) {
		if (s.length() != Code.LENGTH)
			return null;

		try {
			int[] code = new int[Code.LENGTH];

			for (int i = 0; i < Code.LENGTH; i++) {
				int num = s.charAt(i) - 48;
				code[i] = num;
			}

			return code;
		} catch (Exception e) {
		}

		return null;
	}
}
