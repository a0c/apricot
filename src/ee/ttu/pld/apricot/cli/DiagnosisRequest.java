package ee.ttu.pld.apricot.cli;

import org.w3c.dom.Node;

import java.util.List;

/**
 * @author Anton Chepurov
 */
public class DiagnosisRequest extends Request {

	private final Settings settings;

	public DiagnosisRequest(Node requestNode, Settings settings) {
		super(requestNode, settings.design);
		this.settings = settings;
	}

	@Override
	public void buildCommand(List<String> cmd) {
		cmd.add("-diagnosis");
		if (settings.optimize) {
			cmd.add("optimize");
		}
		if (settings.potential) {
			cmd.add("potential");
		}
		if (settings.sort != null) {
			cmd.add(settings.sort);
		}
		cmd.add("operator");
		cmd.add(settings.operators);
		if (settings.random != null) {
			cmd.add("-random");
			cmd.add("" + settings.random);
		}
		cmd.add(getHlddFile().getAbsolutePath().replace(".agm", ""));
	}

	public static class Settings {

		private static final String SCOREBYRATIO = "scorebyratio";

		private final String design;
		private boolean optimize;
		private boolean potential;
		private String sort;
		private String operators;
		private Integer random;

		public Settings(String design) {
			this.design = design;
		}

		public void setOptimize(String optimize) {
			if (optimize.equalsIgnoreCase("true")) {
				this.optimize = true;
			} else if (optimize.equalsIgnoreCase("false")) {
				this.optimize = false;
			} else {
				throw new IllegalArgumentException("Illegal <optimize> flag. Actual: " + optimize + ". Expected: [ true | false ]");
			}
		}

		public void setPotential(String potential) {
			if (potential.equalsIgnoreCase("true")) {
				this.potential = true;
			} else if (potential.equalsIgnoreCase("false")) {
				this.potential = false;
			} else {
				throw new IllegalArgumentException("Illegal <potential> flag. Actual: " + potential + ". Expected: [ true | false ]");
			}
		}

		public void setSort(String sort) {
			if (sort == null) {
				this.sort = null;
			} else if (sort.equalsIgnoreCase(SCOREBYRATIO)) {
				this.sort = SCOREBYRATIO;
			} else {
				throw new IllegalArgumentException("Illegal <sort> flag. Actual: " + sort + ". Expected: [ " + SCOREBYRATIO + " ]");
			}
		}

		public void setOperators(String operators) {
			if (operators.isEmpty()) {
				this.operators = "arlus";
			} else {
				this.operators = operators;
			}
		}

		public void setRandom(String random) {
			if (random == null) {
				this.random = null;
			} else {
				this.random = Integer.parseInt(random);
			}
		}
	}
}
