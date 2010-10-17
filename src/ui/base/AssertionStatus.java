package ui.base;

/**
 * @author Anton Chepurov
 */
public enum AssertionStatus {

	INACTIVE("X"),
	CHECKING("C"),
	PASS("P"),
	FAIL("F"),
	@SuppressWarnings({"EnumeratedConstantNamingConvention"})
	PASS_AND_FAIL("&");

	private final String shortcut;

	AssertionStatus(String shortcut) {
		this.shortcut = shortcut;
	}

	@Deprecated
	public static AssertionStatus[] parse(char... shortcuts) {
		AssertionStatus[] statuses = new AssertionStatus[shortcuts.length];
		for (int i = 0; i < shortcuts.length; i++) {
			statuses[i] = statusOfShortcut(String.valueOf(shortcuts[i]));
		}
		return statuses;
	}


	public static AssertionStatus statusOfShortcut(String shortcut) {
		for (AssertionStatus status : values())
			if (status.shortcut.equals(shortcut)) {
				return status;
			}
		throw new IllegalArgumentException("Enum " + AssertionStatus.class.getName() + " has no constant with shortcut " + shortcut);
	}
}
