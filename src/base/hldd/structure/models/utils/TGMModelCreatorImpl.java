package base.hldd.structure.models.utils;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.nodes.Node;
import base.psl.structure.Property;
import base.psl.structure.AbstractExpression;
import base.psl.structure.ExpressionImpl;
import base.psl.structure.PSLOperator;
import parsers.psl.ModelManager;

import java.util.logging.Logger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.11.2008
 * <br>Time: 13:48:08
 */
public class TGMModelCreatorImpl implements ModelCreator {
    private static final Logger LOG = Logger.getLogger(TGMModelCreatorImpl.class.getName());
    private final Property[] properties;
    private final ModelManager modelManager;

    private BehModel model;

    public TGMModelCreatorImpl(Property[] properties, base.hldd.structure.models.utils.ModelManager hlddModelManager) {
        this.properties = properties;
        this.modelManager = new ModelManager(hlddModelManager);
    }

    /**
     * <b><u>Algorithm description:</u></b><br>
     * The approach generally involves traversal of property graphs <b>in reverse order</b>. While traversing, created
     * Nodes are added to collector ({@link parsers.psl.ModelManager}), where they are internally stored in a map that
     * maps the Nodes with their relative index.
     * <p>
     * Due to reverse traversal, in general case
     * (when the graph contains no cycles), all successors of Control Nodes are added to collector before the Control
     * Nodes themselves are processed. Thus the Control Node successors can be added to the Control Node directly and
     * as soon as the processing of Control Node is reached.
     * <p>
     * In case of <b>CYCLIC HLDDs</b>, not all of the successors are available
     * at the time when the Control Node is reached. The addition of the missing successors is <b>postponed</b>
     * ({@link ModelManager.ContextManager#postpone(int, int, base.hldd.structure.nodes.TemporalNode)}) and performed after
     * all the nodes in property graph are added to collector.
     * <p>
     * Traversal of property graphs has a sligh difference between processing
     * of the starting expression and all the internal expressions. The difference is:<br>
     * *) For Starting Expression, in property graph, Terminal Nodes (property output ports) are added to collector
     *    at once.<br>
     * *) For all other expressions, Terminal Nodes are mapped to the main output ports (the ones of starting expr.)
     * <p>
     * Control Node in property graph gets replaced with the corresponding {@link AbstractExpression} operand using
     * {@link ModelManager#replaceOperandNodeWithExpression(Node, AbstractExpression)}. 
     * 
     * @throws Exception if some error occurs
     */
    private void create() throws Exception {
        for (Property property : properties) {
            /* Collect Metadata */
            modelManager.initNewPropertyGraph();
            modelManager.setCurrentPropertyName(property.getName());
            modelManager.addComment(property.getComment());


            /* Create Structure */
            LOG.info("Creating TGM structure for property " + property.getName());
            AbstractExpression startExpression = property.getStartExpression();
            if (startExpression instanceof ExpressionImpl) {
                ExpressionImpl startExpressionImpl = (ExpressionImpl) startExpression;
                PSLOperator expressionOperator = startExpressionImpl.getPslOperator();

                /* Init new context */
                modelManager.initNewPPGContext(startExpressionImpl.getWindow(), expressionOperator.getWindowPlaceholders());
                /* Process nodes in reverse order,
                *  so that when Control Nodes are reached all Terminal Nodes are already hashed
                *  (so are all control node successors as well) */
                Node[] propertyGraphNodes = expressionOperator.getPropertyGraph().getGraph().getRootNode().toArray(null);
                for (int index = propertyGraphNodes.length - 1; index >= 0; index--) {
                    Node node = propertyGraphNodes[index];

                    if (node.isTerminalNode()) {

                        /* Hash property OUTPUT PORTS (TERMINAL NODES):
                         * 1) in MODEL COLLECTOR (final model)
                         * 2) in relative_to_absolute_indices hash */
                        Node clonedNode = node.clone();
                        modelManager.addNode(index, clonedNode);

                    } else {

                        /* Replace operand node with operand expression */
                        AbstractExpression operandExpression = startExpressionImpl.getOperandByName(node.getDependentVariable().getName());
                        modelManager.addNode(index, modelManager.replaceOperandNodeWithExpression(node, operandExpression));

                    }
                }

                /* If cycle exists (some successors are null), map(set) the missing successors */
                modelManager.fillMissingSuccessors();                

                /* Create Property Graph */
                modelManager.finalizePropertyGraph();

                /* Remove current context from stack */
                modelManager.dismissCurrentPPGContext();

            } else {
                throw new Exception("Unexpected bug: Property contains boolean expression only. Implement is missing."); //todo...
            }
        }
        LOG.info("TGM structures created for ALL properties");
        /* Create model */
        /* Here use functionality of standard BehModelCreatorImpl (one ModelCreator implementation invokes another) */
        LOG.info("Creating " + BehModel.class.getSimpleName() + " ( TGM file )");        
        VariableManager variableManager = modelManager.getVariableManager();
        BehModelCreatorImpl modelCreator =
                new BehModelCreatorImpl(variableManager.getConstantsAsArray(), variableManager.getVariablesAsArray());
        model = modelCreator.getModel();
        model.setMode("BEHAVIORAL");

        LOG.info("TGM model created");
    }

    public BehModel getModel() {
        if (model == null) {
            try {
                create();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return model;
    }

    public String getComment() {
        if (model == null) {
            try {
                create();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }            
        }
        return modelManager.getComment();
    }
}
