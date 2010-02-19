package base.psl.structure;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 24.09.2008
 * <br>Time: 13:30:02
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
