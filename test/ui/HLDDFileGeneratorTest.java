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
public class HLDDFileGeneratorTest {

	@Test
	public void returnNullForNullSourceFile() {
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSourceFile()).andReturn(null);
		expect(form.getHlddRepresentationType()).andReturn(null);
		replay(form);

		HLDDFileGenerator generator = new HLDDFileGenerator(form);
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

		HLDDFileGenerator generator = new HLDDFileGenerator(form);
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
		HLDDFileGenerator generator = new HLDDFileGenerator(form);
		File file = generator.generate();
		assertNotNull("HLDDFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("test/designs/ITC99/orig/b00/b00_F_FU.agm", file.getPath());
		verify(form);
		/* _F_EX */
		form = createApplicationForm(FULL_TREE);
		expect(form.shouldCreateCSGraphs()).andReturn(false);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(true);
		expect(form.shouldFlattenCS()).andReturn(false);
		replay(form);
		generator = new HLDDFileGenerator(form);
		file = generator.generate();
		assertNotNull("HLDDFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("test/designs/ITC99/orig/b00/b00_F_EX.agm", file.getPath());
		verify(form);
		/* _F_GR */
		form = createApplicationForm(FULL_TREE);
		expect(form.shouldCreateCSGraphs()).andReturn(true);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		replay(form);
		generator = new HLDDFileGenerator(form);
		file = generator.generate();
		assertNotNull("HLDDFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("test/designs/ITC99/orig/b00/b00_F_GR.agm", file.getPath());
		verify(form);
		/* _F_FL */
		form = createApplicationForm(FULL_TREE);
		expect(form.shouldCreateCSGraphs()).andReturn(false);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(true);
		replay(form);
		generator = new HLDDFileGenerator(form);
		file = generator.generate();
		assertNotNull("HLDDFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("test/designs/ITC99/orig/b00/b00_F_FL.agm", file.getPath());
		verify(form);
		/* _M_FU */
		form = createApplicationForm(MINIMIZED);

		expect(form.shouldCreateCSGraphs()).andReturn(false);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		replay(form);
		generator = new HLDDFileGenerator(form);
		file = generator.generate();
		assertNotNull("HLDDFileGenerator.generate(): should create correct file. Actual: null.", file);
		assertEquals("test/designs/ITC99/orig/b00/b00_M_FU.agm", file.getPath());
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

		HLDDFileGenerator generator = new HLDDFileGenerator(form);
		generator.stateChanged(null);
		verify(form);

		/* NON-NULL */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(BusinessLogic.ParserID.VhdlBeh2HlddBeh);
		expect(form.areSmartNamesAllowed()).andReturn(true);
		expect(form.getSourceFile()).andReturn(new File("test/designs/ITC99/orig/b00/b00.vhd")).anyTimes();
		expect(form.getHlddRepresentationType()).andReturn(MINIMIZED).anyTimes();
		expect(form.shouldCreateCSGraphs()).andReturn(true);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		File outputFile = new File("test/designs/ITC99/orig/b00/b00_M_GR.agm");
		form.setBehHlddFile(eq(outputFile));
		replay(form);

		generator = new HLDDFileGenerator(form);
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

		HLDDFileGenerator generator = new HLDDFileGenerator(form);
		generator.insertUpdate(null);
		verify(form);

		/* NON-NULL */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(BusinessLogic.ParserID.VhdlBeh2HlddBeh);
		expect(form.areSmartNamesAllowed()).andReturn(true);
		expect(form.getSourceFile()).andReturn(new File("test/designs/ITC99/orig/b00/b00.vhd")).anyTimes();
		expect(form.getHlddRepresentationType()).andReturn(MINIMIZED).anyTimes();
		expect(form.shouldCreateCSGraphs()).andReturn(true);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		File outputFile = new File("test/designs/ITC99/orig/b00/b00_M_GR.agm");
		form.setBehHlddFile(eq(outputFile));
		replay(form);

		generator = new HLDDFileGenerator(form);
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

		HLDDFileGenerator generator = new HLDDFileGenerator(form);
		generator.changedUpdate(null);
		verify(form);

		/* NON-NULL */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(BusinessLogic.ParserID.VhdlBeh2HlddBeh);
		expect(form.areSmartNamesAllowed()).andReturn(true);
		expect(form.getSourceFile()).andReturn(new File("test/designs/ITC99/orig/b00/b00.vhd")).anyTimes();
		expect(form.getHlddRepresentationType()).andReturn(MINIMIZED).anyTimes();
		expect(form.shouldCreateCSGraphs()).andReturn(true);
		expect(form.shouldCreateExtraCSGraphs()).andReturn(false);
		expect(form.shouldFlattenCS()).andReturn(false);
		File outputFile = new File("test/designs/ITC99/orig/b00/b00_M_GR.agm");
		form.setBehHlddFile(eq(outputFile));
		replay(form);

		generator = new HLDDFileGenerator(form);
		generator.changedUpdate(null);
		verify(form);
	}

	private ApplicationForm createApplicationForm(BusinessLogic.HLDDRepresentationType hlddType) {
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSourceFile()).andReturn(new File("test/designs/ITC99/orig/b00/b00.vhd")).anyTimes();
		expect(form.getHlddRepresentationType()).andReturn(hlddType).anyTimes();
		return form;
	}
}
