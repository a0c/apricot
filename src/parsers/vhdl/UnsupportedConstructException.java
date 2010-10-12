package parsers.vhdl;

/**
 * @author Anton Chepurov
 */
public class UnsupportedConstructException extends Exception {

	private final String unsupportedConstruct;

	public UnsupportedConstructException(String message, String unsupportedConstruct) {
		super(message);
		this.unsupportedConstruct = unsupportedConstruct;
	}

	public String getUnsupportedConstruct() {
		return unsupportedConstruct;
	}
}
