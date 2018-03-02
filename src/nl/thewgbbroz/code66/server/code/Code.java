package nl.thewgbbroz.code66.server.code;

import net.dv8tion.jda.core.entities.MessageChannel;

public class Code {
	public static final int LENGTH = 4;

	private final int[] code;
	private final long createTime;

	protected final MessageChannel messageChannel;

	protected Code(int[] code, MessageChannel messageChannel) {
		if (code.length != LENGTH)
			throw new IllegalArgumentException("code.length must be equal to " + LENGTH + "!");

		this.code = code;
		this.messageChannel = messageChannel;
		this.createTime = System.currentTimeMillis();
	}

	public boolean equals(int[] other) {
		if (other.length != LENGTH)
			return false;

		for (int i = 0; i < code.length; i++) {
			if (other[i] != code[i]) {
				return false;
			}
		}

		return true;
	}

	public boolean equals(Code other) {
		return equals(other.code);
	}

	public int[] getCode() {
		return code;
	}

	public long getCreateTime() {
		return createTime;
	}

	public String getCodeString() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < code.length; i++) {
			sb.append(code[i] + ", ");
		}
		sb.delete(sb.length() - 2, sb.length());

		return sb.toString();
	}

	@Override
	public String toString() {
		return "Code[" + getCodeString() + "]";
	}
}
