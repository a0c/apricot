package ui;

import org.junit.Test;

import java.io.File;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import static ui.BusinessLogic.ParserID.*;

/**
 * @author Anton Chepurov
 */
public class RTLOutputFileGeneratorTest {

	@Test
	public void returnNullForNullSourceFile() {
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSourceFile()).andReturn(null);
		replay(form);
		File file = new RTLOutputFileGenerator(form).generate();
		assertNull(file);
		verify(form);
	}

	@Test
	public void createCorrectFile() {
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSourceFile()).andReturn(new File("SomeFile.agm"));
		replay(form);
		File file = new RTLOutputFileGenerator(form).generate();
		assertNotNull(file);
		assertEquals(new File("SomeFile_RTL.agm"), file);
		verify(form);
	}

	@Test
	public void reactOnCorrectConverter() {
		/* Don't react */
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(VhdlBeh2HlddBeh);
		replay(form);
		new RTLOutputFileGenerator(form).react();
		verify(form);
		/* Don't react */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(VhdlBehDd2HlddBeh);
		replay(form);
		new RTLOutputFileGenerator(form).react();
		verify(form);
		/* Don't react */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(PSL2THLDD);
		replay(form);
		new RTLOutputFileGenerator(form).react();
		verify(form);
		/* React */
		form = createStrictMock(ApplicationForm.class);
		RTLOutputFileGenerator fileGenerator = new RTLOutputFileGenerator(form);
		expect(form.getSelectedParserId()).andReturn(HlddBeh2HlddRtl);
		expect(form.getSourceFile()).andReturn(new File("SomeFile.agm"));
		form.setRtlRtlFile(isA(File.class));
		replay(form);
		fileGenerator.react();
		verify(form);
	}
}
