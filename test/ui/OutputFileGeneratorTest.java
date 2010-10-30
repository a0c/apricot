package ui;

import org.junit.Test;

import java.io.File;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import static ui.BusinessLogic.HLDDRepresentationType.FULL_TREE;
import static ui.BusinessLogic.HLDDRepresentationType.MINIMIZED;

/**
 * @author Anton Chepurov
 */
public class OutputFileGeneratorTest {

	@Test
	public void returnNullForNullSourceFile() {
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSourceFile()).andReturn(null);
		expect(form.getHlddRepresentationType()).andReturn(null);
		replay(form);

		OutputFileGenerator generator = new OutputFileGenerator(form);
		File file = generator.generate();
		assertNull(file);
		verify(form);
	}

	@Test
	public void returnNullForNullHLDDType() {
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSourceFile()).andReturn(new File(""));
		expect(form.getHlddRepresentationType()).andReturn(null);
		replay(form);

		OutputFileGenerator generator = new OutputFileGenerator(form);
		File file = generator.generate();
		assertNull(file);
		verify(form);
	}

	@Test
	public void createCorrectFile() {

		/* _F_FU */
		ApplicationForm form = createApplicationForm(FULL_TREE);
		expect(form.shouldCreateCSGraphs()).andReturn(false);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		replay(form);
		OutputFileGenerator generator = new OutputFileGenerator(form);
		File file = generator.generate();
		assertNotNull("OutputFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("D:\\WORKSPACE\\tr\\b13_F_FU.agm", file.getAbsolutePath());
		verify(form);
		/* _F_EX */
		form = createApplicationForm(FULL_TREE);
		expect(form.shouldCreateCSGraphs()).andReturn(false);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(true);
		expect(form.shouldFlattenCS()).andReturn(false);
		replay(form);
		generator = new OutputFileGenerator(form);
		file = generator.generate();
		assertNotNull("OutputFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("D:\\WORKSPACE\\tr\\b13_F_EX.agm", file.getAbsolutePath());
		verify(form);
		/* _F_GR */
		form = createApplicationForm(FULL_TREE);
		expect(form.shouldCreateCSGraphs()).andReturn(true);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		replay(form);
		generator = new OutputFileGenerator(form);
		file = generator.generate();
		assertNotNull("OutputFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("D:\\WORKSPACE\\tr\\b13_F_GR.agm", file.getAbsolutePath());
		verify(form);
		/* _F_FL */
		form = createApplicationForm(FULL_TREE);
		expect(form.shouldCreateCSGraphs()).andReturn(false);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(true);
		replay(form);
		generator = new OutputFileGenerator(form);
		file = generator.generate();
		assertNotNull("OutputFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("D:\\WORKSPACE\\tr\\b13_F_FL.agm", file.getAbsolutePath());
		verify(form);
		/* _M_FU */
		form = createApplicationForm(MINIMIZED);

		expect(form.shouldCreateCSGraphs()).andReturn(false);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		replay(form);
		generator = new OutputFileGenerator(form);
		file = generator.generate();
		assertNotNull("OutputFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("D:\\WORKSPACE\\tr\\b13_M_FU.agm", file.getAbsolutePath());
		verify(form);
	}

	@Test
	public void reactOnChangedState() {
		/* NULL */
		//todo: use Mockito
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(BusinessLogic.ParserID.VhdlBeh2HlddBeh);
		expect(form.areSmartNamesAllowed()).andReturn(true);
		expect(form.getSourceFile()).andReturn(null);
		expect(form.getHlddRepresentationType()).andReturn(null);
		replay(form);

		OutputFileGenerator generator = new OutputFileGenerator(form);
		generator.stateChanged(null);
		verify(form);

		/* NON-NULL */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(BusinessLogic.ParserID.VhdlBeh2HlddBeh);
		expect(form.areSmartNamesAllowed()).andReturn(true);
		expect(form.getSourceFile()).andReturn(new File("D:\\WORKSPACE\\tr\\b13.vhd")).anyTimes();
		expect(form.getHlddRepresentationType()).andReturn(MINIMIZED).anyTimes();
		expect(form.shouldCreateCSGraphs()).andReturn(true);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		File outputFile = new File("D:\\WORKSPACE\\tr\\b13_M_GR.agm");
		form.setBehHlddFile(eq(outputFile));
		replay(form);

		generator = new OutputFileGenerator(form);
		generator.stateChanged(null);
		verify(form);
	}

	@Test
	public void reactOnInsertUpdate() {
		/* NULL */
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(BusinessLogic.ParserID.VhdlBeh2HlddBeh);
		expect(form.areSmartNamesAllowed()).andReturn(true);
		expect(form.getSourceFile()).andReturn(null);
		expect(form.getHlddRepresentationType()).andReturn(null);
		replay(form);

		OutputFileGenerator generator = new OutputFileGenerator(form);
		generator.insertUpdate(null);
		verify(form);

		/* NON-NULL */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(BusinessLogic.ParserID.VhdlBeh2HlddBeh);
		expect(form.areSmartNamesAllowed()).andReturn(true);
		expect(form.getSourceFile()).andReturn(new File("D:\\WORKSPACE\\tr\\b13.vhd")).anyTimes();
		expect(form.getHlddRepresentationType()).andReturn(MINIMIZED).anyTimes();
		expect(form.shouldCreateCSGraphs()).andReturn(true);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		File outputFile = new File("D:\\WORKSPACE\\tr\\b13_M_GR.agm");
		form.setBehHlddFile(eq(outputFile));
		replay(form);

		generator = new OutputFileGenerator(form);
		generator.insertUpdate(null);
		verify(form);
	}

	@Test
	public void reactOnChangeUpdate() {
		/* NULL */
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(BusinessLogic.ParserID.VhdlBeh2HlddBeh);
		expect(form.areSmartNamesAllowed()).andReturn(true);
		expect(form.getSourceFile()).andReturn(null);
		expect(form.getHlddRepresentationType()).andReturn(null);
		replay(form);

		OutputFileGenerator generator = new OutputFileGenerator(form);
		generator.changedUpdate(null);
		verify(form);

		/* NON-NULL */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(BusinessLogic.ParserID.VhdlBeh2HlddBeh);
		expect(form.areSmartNamesAllowed()).andReturn(true);
		expect(form.getSourceFile()).andReturn(new File("D:\\WORKSPACE\\tr\\b13.vhd")).anyTimes();
		expect(form.getHlddRepresentationType()).andReturn(MINIMIZED).anyTimes();
		expect(form.shouldCreateCSGraphs()).andReturn(true);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		File outputFile = new File("D:\\WORKSPACE\\tr\\b13_M_GR.agm");
		form.setBehHlddFile(eq(outputFile));
		replay(form);

		generator = new OutputFileGenerator(form);
		generator.changedUpdate(null);
		verify(form);
	}

	private ApplicationForm createApplicationForm(BusinessLogic.HLDDRepresentationType hlddType) {
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSourceFile()).andReturn(new File("D:\\WORKSPACE\\tr\\b13.vhd")).anyTimes();
		expect(form.getHlddRepresentationType()).andReturn(hlddType).anyTimes();
		return form;
	}
}
