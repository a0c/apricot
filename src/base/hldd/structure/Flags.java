package base.hldd.structure;

/**
 * Class stores information which is stored in AGM file for the needs of the simulator.
 * <br>In AGM it is presented as the substring between brackets, for instance:
 * <br><b>Code:</b>
 * <code>
 * <br>;inputs
 * <br>VAR#	0:  (i_________)	"IN1"	<15:0>
 * </code>
 * <br><b>Flags:</b>
 * <br>i_________
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 31.10.2009
 * <br>Time: 23:44:28
 */
public final class Flags {

    private boolean isConstant;
    private boolean isCout; // Control part outputs
    private boolean isDelay;
    private boolean isFunction;
    private boolean isFSM;
    private boolean isInput;
    private boolean isOutput;
    private boolean isReset;
    private boolean isState;
    private boolean isExpansion;

    /**
     * Empty flags.
     */
    public Flags() {
    }

    private Flags(Flags flags) {
        isConstant = flags.isConstant;
        isCout = flags.isCout;
        isDelay = flags.isDelay;
        isFunction = flags.isFunction;
        isFSM = flags.isFSM;
        isInput = flags.isInput;
        isOutput = flags.isOutput;
        isReset = flags.isReset;
        isState = flags.isState;
        isExpansion = flags.isExpansion;
    }

    public Flags setCout(boolean isCout) {
        this.isCout = isCout;
        return this;
    }

    public Flags setDelay(boolean isDelay) {
        this.isDelay = isDelay;
        return this;
    }

    public Flags setFSM(boolean isFSM) {
        this.isFSM = isFSM;
        return this;
    }

    public Flags setInput(boolean isInput) {
        this.isInput = isInput;
        return this;
    }

    public Flags setOutput(boolean isOutput) {
        this.isOutput = isOutput;
        return this;
    }

    public Flags setReset(boolean isReset) {
        this.isReset = isReset;
        return this;
    }

    public Flags setState(boolean isState) {
        this.isState = isState;
        return this;
    }

	public Flags setExpansion(boolean isExpansion) {
		this.isExpansion = isExpansion;
		return this;
	}

	public boolean isCout() {
        return isCout;
    }

    public boolean isDelay() {
        return isDelay;
    }

    public boolean isFSM() {
        return isFSM;
    }

    public boolean isInput() {
        return isInput;
    }

    public boolean isOutput() {
        return isOutput;
    }

    public boolean isReset() {
        return isReset;
    }

    public boolean isState() {
        return isState;
    }

	public boolean isExpansion() {
		return isExpansion;
	}

	public boolean isFunction() {
        return isFunction;
    }

    public void setFunction(boolean function) {
        isFunction = function;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }

    public Flags merge(Flags otherFlags) {

        Flags newFlags = new Flags(this);
        if (otherFlags.isConstant) newFlags.isConstant = true;
        if (otherFlags.isCout) newFlags.isCout = true;
        if (otherFlags.isDelay) newFlags.isDelay = true;
        if (otherFlags.isFSM) newFlags.isFSM = true;
        if (otherFlags.isFunction) newFlags.isFunction = true;
        if (otherFlags.isInput) newFlags.isInput = true;
        if (otherFlags.isOutput) newFlags.isOutput = true;
        if (otherFlags.isReset) newFlags.isReset = true;
        if (otherFlags.isState) newFlags.isState = true;
        if (otherFlags.isExpansion) newFlags.isExpansion = true;

        return newFlags;
    }

    public String toString() {
        StringBuilder b = new StringBuilder("__________");

        if (isConstant) b.setCharAt(0, 'c');
        if (isCout) b.setCharAt(2, 'n');
        if (isDelay) b.setCharAt(8, 'd');
        if (isFSM) b.setCharAt(8, 'F');
        if (isFunction) b.setCharAt(4, 'f');
        if (isInput) b.setCharAt(0, 'i');
        if (isOutput) b.setCharAt(4, 'o');
        if (isReset) b.setCharAt(2, 'r');
        if (isState) b.setCharAt(2, 's');
        if (isExpansion) b.setCharAt(8, 'E');

        return b.toString();
    }

    public static Flags parse(String flagsAsString) {

        flagsAsString = flagsAsString.toUpperCase();

        Flags flags = new Flags();

        if (flagsAsString.contains("C")) flags.setConstant(true);
        if (flagsAsString.contains("N")) flags.setCout(true);
        if (flagsAsString.contains("D")) flags.setDelay(true);
        if (flagsAsString.length() == 10){
			if (flagsAsString.charAt(8) == 'F') flags.setFSM(true);
			if (flagsAsString.charAt(4) == 'F') flags.setFunction(true);
			if (flagsAsString.charAt(8) == 'E') flags.setExpansion(true);
		} else if (flagsAsString.length() == 5) {
			if (flagsAsString.contains("F")) flags.setFunction(true);
		} else throw new RuntimeException("Flags: flags length is neither 10 (AGM), nor 5 (TGM)"); 
        if (flagsAsString.contains("I")) flags.setInput(true);
        if (flagsAsString.contains("O")) flags.setOutput(true);
        if (flagsAsString.contains("R")) flags.setReset(true);
        if (flagsAsString.contains("S")) flags.setState(true);

        return flags;
    }
}
