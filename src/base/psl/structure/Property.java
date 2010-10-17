package base.psl.structure;

/**
 * @author Anton Chepurov
 */
public class Property {
	private final String comment;
	private final String name;
	private final VerificationDirective directive;
	private final AbstractExpression startExpression;

	public Property(String sourceLine, String name, VerificationDirective directive, AbstractExpression startExpression) {
		comment = sourceLine;
		this.name = name;
		this.directive = directive;
		this.startExpression = startExpression;
	}

	/* #### G E T T E R S #### */

	public String getComment() {
		return comment;
	}

	public String getName() {
		return name;
	}

	public AbstractExpression getStartExpression() {
		return startExpression;
	}

}
