package base.vhdl.structure;

import base.Type;
import base.TypeResolver;

/**
 * @author Anton Chepurov
 */
public abstract class ASTObject implements TypeResolver {

	private final ASTObject parent;

	public ASTObject(ASTObject parent) {
		this.parent = parent;
	}

	public ASTObject resolve(String name) {
		ASTObject astObject = doResolve(name);
		if (astObject != null) {
			return astObject;
		}
		return (parent != null) ? parent.resolve(name) : null;
	}

	protected ASTObject doResolve(String name) {
		throw new RuntimeException("doResolve() is not supported for " + getClass().getSimpleName());
	}

	public Constant resolveConstant(String constantName) {
		ASTObject astObject = resolve(constantName);
		if (astObject instanceof Constant) {
			return (Constant) astObject;
		}
		throw new RuntimeException("resolveConstant(): " + constantName + " resolved to " + astObject.getClass().getSimpleName());
	}

	public Type getType() {
		throw new RuntimeException("getType() is not supported for " + getClass().getSimpleName());
	}

	@Override
	public Type resolveType(String objectName) {
		ASTObject astObject = resolve(objectName);
		if (astObject == null) {
			return null;
		}
		return astObject.getType();
	}
}
