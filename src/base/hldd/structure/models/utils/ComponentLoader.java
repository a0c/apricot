package base.hldd.structure.models.utils;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.variables.AbstractVariable;
import base.vhdl.structure.ComponentDeclaration;
import base.vhdl.structure.ComponentInstantiation;
import ui.ConverterSettings;
import ui.ExtendedException;
import ui.utils.ConvertingWorker;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * @author Anton Chepurov
 */
public class ComponentLoader {

	public static BehModel loadModel(ComponentInstantiation component, ConverterSettings baseSettings) throws ExtendedException {

		ComponentDeclaration componentDeclaration = component.getDeclaration();

		try {
			/* Convert component */
			File sourceFile = componentDeclaration.getSourceFile();

			if (sourceFile == null) {
				// todo: create stub for missing component implementation
				throw new RuntimeException("ComponentLoader: implement me!!! Create stub for missing component implementation");
			}

			ConverterSettings settings = new ConverterSettings.Builder(baseSettings).setSourceFile(sourceFile).build();

			BehModel model = ConvertingWorker.convertInSeparateThreadAndWait(settings);

			new PrefixAdder(model, component).addPrefix(component.getName());

			return model;

		} catch (InterruptedException e) {
			throw new ExtendedException("Converter thread was interrupted while processing component " +
					component.getName(), ExtendedException.ERROR_TEXT);
		} catch (ExecutionException e) {
			throw new ExtendedException("Converter failed while processing component " +
					component.getName(), ExtendedException.ERROR_TEXT);
		}
	}

	private static class PrefixAdder {

		private final BehModel model;

		private final ComponentInstantiation component;

		public PrefixAdder(BehModel model, ComponentInstantiation component) {
			this.model = model;
			this.component = component;
		}

		public void addPrefix(String contextPrefix) {

			for (AbstractVariable variable : model.getVariables()) {

				String oldName = variable.getName();

				variable.addNamePrefix(contextPrefix);

				if (variable.isInput() || variable.isOutput()) {
					component.renameFormalMapping(oldName, variable.getName());
				}

			}

		}
	}

}
