package helpers;

/**
 * @author Anton Chepurov
 */
public interface PSLProperties {

	public static final String[] UNFORMATTED_PROPERTY_ARRAY = {
			"p1:assert always (((not ready) and (a = b))->next_e[1 to 3](ready));",
			"p2: assert always (reset -> next[2] ((not ready) until (a = b)));",
			"p3: assert never ((a /= b) and ready);",
			"p4: assert never ((a /= b) and (not ready));",
			"p5: assert always ( reset -> next_a[2 to 5](not ready) );",
			"p6: assert ( a -> next b);",
			"p7: assert always ( a -> next[2]b);"
	};
	public static final String[] FORMATTED_PROPERTY_ARRAY = {
			"p1 : assert always ( ( ( not ready ) and ( a = b ) ) -> next_e[ 1 to 3 ] ( ready ) ) ;",
			"p2 : assert always ( reset -> next[ 2 ] ( ( not ready ) until ( a = b ) ) ) ;",
			"p3 : assert never ( ( a /= b ) and ready ) ;",
			"p4 : assert never ( ( a /= b ) and ( not ready ) ) ;",
			"p5 : assert always ( reset -> next_a[ 2 to 5 ] ( not ready ) ) ;",
			"p6 : assert ( a -> next b ) ;",
			"p7 : assert always ( a -> next[ 2 ] b ) ;"
	};

	public static final String[][] EXAMPLE_OPERATOR_ARRAY = {
			{"always ( ( ( not ready ) and ( a = b ) ) -> next_e[ 1 to 3 ] ( ready ) )", "always TOP"},
			{"never ( ( a /= b ) and ready )", "never BOP"},
			{"leather <-> killed_animal", "BOP1 <-> BOP2"}, //todo...
			{"( a -> next b )", "BOP -> TOP"},
			{"( dzaga until dzaguga)", "TOP until BOP"},
			{"winter before summer", "BOP1 before BOP2"}, //todo...
			{"next bobda", "next TOP"},
			{"next[ 19 ] reality", "next[k] TOP"},
			{"next_e[ 25 to 99 ] (lunachar)", "next_e[j to k] BOP"},
			{"next_a[ 1 to 1000 ] brynza", "next_a[j to k] TOP"},
			{"next_event a_signal [4] result", "next_event (BOP) [k] (TOP)"}, //todo...
			{"bingo or bongo", "BOP or TOP"},
			{"grune and tee", "TOP1 and TOP2"},
			{"not ready", "not BOP"}
	};

	/**
	 * todo: remove this when ppg_future.lib gets taken into use
	 */
	public static final String[][] EXAMPLE_OPERATOR_ARRAY_OLD = {
			{"always ( ( ( not ready ) and ( a = b ) ) -> next_e[ 1 to 3 ] ( ready ) )", "always TOP"},
			{"never ( ( a /= b ) and ready )", "never TOP"},
			{"( a -> next b )", "BOP -> TOP"},
			{"( dzaga until dzaguga)", "BOP1 until BOP2"},
			{"next bobda", "next TOP"},
			{"next[ 19 ] reality", "next[n] TOP"},
			{"next_e[ 25 to 99 ] (lunachar)", "next_e[start to end] TOP"},
			{"next_a[ 1 to 1000 ] brynza", "next_a[start to end] TOP"},
			{"bingo or bongo", "BOP or TOP"},
			{"grune and tee", "BOP and TOP"}
	};

	public static final String[][] EXAMPLE_OPERAND_ARRAY = {
			{"( ( ( not ready ) and ( a = b ) ) -> next_e[ 1 to 3 ] ( ready ) )"},
			{"( ( a /= b ) and ready )"},
			{"leather", "killed_animal"},
			{"a", "next b"},
			{"dzaga", "dzaguga"},
			{"winter", "summer"},
			{"bobda"},
			{"reality"},
			{"(lunachar)"},
			{"brynza"},
			{"a_signal", "result"},
			{"bingo", "bongo"},
			{"grune", "tee"},
			{"ready"}
	};


}
