package pageobjectgenerator;

import japa.parser.ast.CompilationUnit;
import locatorstrategyform.State;
import org.apache.commons.io.FileUtils;
import utils.UtilsMethodCodeGenerator;

import java.io.File;
import java.io.IOException;

public class CodeGenerator {

	/**
	 * run the code generation from the info gathered by the static analysis
	 * 
	 * @throws Exception
	 */
	static void run() throws Exception {
		System.out.println("[LOG]\tSTARTED PAGE OBJECTS GENERATION");

		createPageObjects();

		savePageObjectsOnFileSystem();

		System.out.println("[LOG]\tENDED PAGE OBJECTS GENERATION");
	}

	/**
	 * creates the source code for each state with creation of compilation units and
	 * dynamic AST modifications TODO: only java language support
	 * 
	 * @throws Exception
	 */
	private static void createPageObjects() throws Exception {

		for (State s : StaticAnalyzer.getStatesList()) {

			CompilationUnit c = UtilsMethodCodeGenerator.createEnhancedCompilationUnit("locators", "PageObject");

			UtilsMethodCodeGenerator.setTypeDeclaration(c, s.getName());

			UtilsMethodCodeGenerator.setVariables(c, s.getWebElements());

			UtilsMethodCodeGenerator.setDefaultConstructor(c, s);

//			UtilsCodeGenerator.setLinkMethods(c, s);
//
//			UtilsCodeGenerator.setFormMethods(c, s);

			UtilsMethodCodeGenerator.setGettersMethods(c, s);

			UtilsMethodCodeGenerator.setSourceCode(c, s);

		}

	}

	/**
	 * makes the source code of each state persistent
	 * 
	 * @throws IOException
	 */
	static void savePageObjectsOnFileSystem() throws IOException {

		for (State s : StaticAnalyzer.getStatesList()) {

			String poName = s.getName();
			String fileNameToCreate = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
					+ File.separator + "java" + File.separator + Settings.GEN_PO_DIR + poName;
			// String fileNameToCreate = System.getProperty("user.dir") +poName;
			File f = new File(fileNameToCreate + ".java");

			FileUtils.writeStringToFile(f, s.getSourceCode());
		}

	}

}
