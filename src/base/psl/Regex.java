package base.psl;

/**
 * @author Anton Chepurov
 */
public interface Regex {

//    /* 'VUNIT ' regex */
//    public static final String VUNIT = "[vV][uU][nN][iI][tT]\\s"; // alternative "(vunit|VUNIT)\\s"
	/* VUNIT DECLARATION regexp */
	public static final String VUNIT_DECLARATION = ".*\\s*\\{$";
	/*  'default '       ||      'constant '     ||      'property '  */
	public static final String LITERAL_ENDS_WITH_WHITESPACE = "^[a-ZA-Z][\\w]*\\s$";
	/*  'always '     ||      'always(' */
	public static final String LITERAL_ENDS_WITH_WHITESPACE_OR_BRACKET = "^[a-zA-Z][\\w]*[\\s\\(\\{]$";
	/*  P1:  */
	public static final String LITERAL_ENDS_WITH_COLON = "[\\w]+:$";

}
