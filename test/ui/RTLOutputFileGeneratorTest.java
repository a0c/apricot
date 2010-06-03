package ui;

import org.junit.Test;

import javax.swing.*;
import java.io.File;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import static ui.BusinessLogic.ParserID.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 02.06.2010
 * <br>Time: 17:39:01
 */
public class RTLOutputFileGeneratorTest {

	@Test public void returnNullForNullSourceFile() {
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSourceFile()).andReturn(null);
		replay(form);
		File file = new RTLOutputFileGenerator(form, null).generate();
		assertNull(file);
		verify(form);
	}
	@Test public void createCorrectFile() {
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSourceFile()).andReturn(new File("SomeFile.agm"));
		replay(form);
		File file = new RTLOutputFileGenerator(form, null).generate();
		assertNotNull(file);
		assertEquals(new File("SomeFile_RTL.agm"), file);
		verify(form);
	}
	@Test public void reactOnCorrectConverter() {
		/* Don't react */
		ApplicationForm form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(VhdlBeh2HlddBeh);
		replay(form);
		new RTLOutputFileGenerator(form, null).react();
		verify(form);
		/* Don't react */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(VhdlBehDd2HlddBeh);
		replay(form);
		new RTLOutputFileGenerator(form, null).react();
		verify(form);
		/* Don't react */
		form = createStrictMock(ApplicationForm.class);
		expect(form.getSelectedParserId()).andReturn(PSL2THLDD);
		replay(form);
		new RTLOutputFileGenerator(form, null).react();
		verify(form);
		/* React */
		JButton fileButton = new JButton("asdfasdfsad");
		form = createStrictMock(ApplicationForm.class);
		RTLOutputFileGenerator fileGenerator = new RTLOutputFileGenerator(form, fileButton);
		expect(form.getSelectedParserId()).andReturn(HlddBeh2HlddRtl);
		expect(form.getSourceFile()).andReturn(new File("SomeFile.agm"));
		form.setDestFile(isA(File.class));
		form.updateTextFieldFor(eq(fileButton), isA(File.class));
		replay(form);
		fileGenerator.react();
		verify(form);
	}
}
