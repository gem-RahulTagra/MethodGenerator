package utils;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import locatorstrategyform.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import pageobjectgenerator.Settings;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class UtilsMethodCodeGenerator {

    private static String meaningFulName = "";

    /**
     * returns a basic plain CompilationUnit object
     *
     * @return
     */
    public static CompilationUnit createBasicCompilationUnit() {
        return new CompilationUnit();
    }

    /**
     * returns a CompilationUnit object decorated with package and some basic import
     * instructions
     *
     * @return
     */
    public static CompilationUnit createEnhancedCompilationUnit(String name, String type) throws IOException {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(name)));
        cu.setImports(UtilsMethodCodeGenerator.getAllImports(type));
        return cu;

    }

    /**
     * set the TypeDeclaration of a CompilationUnit i.e., whether is a class or an
     * interface
     *
     * @param c
     * @param className
     */
    public static void setTypeDeclaration(CompilationUnit c, String className) throws IOException {
        // create the type declaration and class
        String classToExtend = "";
        if (readProperties("Framework").contains("GEMJAR")) {
            classToExtend = classToExtend + "DriverAction";
        } else {
            classToExtend = classToExtend + "PageObject";
        }
        ClassOrInterfaceType typeForExtends = new ClassOrInterfaceType(classToExtend);
        List<ClassOrInterfaceType> extendsList = new ArrayList<ClassOrInterfaceType>();
        extendsList.add(typeForExtends);
        ClassOrInterfaceDeclaration type = new ClassOrInterfaceDeclaration(null, ModifierSet.PUBLIC, null, false, className, null, extendsList, null, null);

        ASTHelper.addTypeDeclaration(c, type);

    }

    /**
     * adds a WebDriver instance to the CompilationUnit c
     *
     * @param c CompilationUnit
     */
    //set implementation variable
    public static void setWebDriverVariable(CompilationUnit c) throws IOException {
        //setting the driver for the current class
        VariableDeclarator v = new VariableDeclarator();
        if (!(readProperties("Framework").contains("GEMJAR"))) {
            v.setId(new VariableDeclaratorId("driver =" + "getDriver(" + ")"));
//        v.setInit(new ObjectCreationExpr(null, new ClassOrInterfaceType(null, classname), null));
            FieldDeclaration f = ASTHelper.createFieldDeclaration(ModifierSet.PRIVATE, ASTHelper.createReferenceType("WebDriver", 0), v);

            ASTHelper.addMember(c.getTypes().get(0), f);
        }
    }


    /**
     * adds a WebDriver instance to the CompilationUnit c
     *
     * @param c CompilationUnit
     */

    /**
     * adds a constructor to the CompilationUnit c with a WebDriver instance and
     * PageFactory initialization
     *
     * @param c
     * @param s
     */
    public static void setDefaultConstructor(CompilationUnit c, State s) {

        // creates the class constructor
        ConstructorDeclaration constructor = new ConstructorDeclaration();
        constructor.setName(s.getName());
        constructor.setModifiers(ModifierSet.PUBLIC);

        // sets the WebDriver instance parameter
        List<Parameter> parameters = new LinkedList<>();
        parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("WebDriver", 0), "driver"));

        constructor.setParameters(parameters);
        constructor.setJavaDoc(new JavadocComment("\n\t\tPage Object for " + s.getName() + " (" + s.getStateId() + ") \n\t"));

        // add the body to the constructor
        BlockStmt constructor_block = new BlockStmt();
        constructor.setBlock(constructor_block);

        // add basic statements do the constructor method body
        ASTHelper.addStmt(constructor_block, new NameExpr("this.driver = driver"));
        ASTHelper.addStmt(constructor_block, new NameExpr("PageFactory.initElements(driver, this)"));

        ASTHelper.addMember(c.getTypes().get(0), constructor);

    }

    /**
     * set the package of the CompilationUnit c
     *
     * @param c
     */
    public static void setPackage(CompilationUnit c) {
        c.setPackage(new PackageDeclaration(ASTHelper.createNameExpr("locators")));
    }

    /**
     * adds Selenium imports to the compilation unit
     */
    private static List<ImportDeclaration> getAllImports(String type) throws IOException {
        List<ImportDeclaration> imports = new LinkedList<>();

        if (StringUtils.equalsIgnoreCase(type, "PageObject")) {
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium"), false, true));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.support.FindBy"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.support.PageFactory"), false, false));
        } else {
            //adding imports for implementation class
            if (readProperties("Framework").contains("GEMJAR")) {
                imports.add(new ImportDeclaration(new NameExpr("com.gemini.generic.reporting.GemTestReporter"), false, false));
                imports.add(new ImportDeclaration(new NameExpr("com.gemini.generic.reporting.STATUS"), false, false));
                imports.add(new ImportDeclaration(new NameExpr("com.gemini.generic.ui.utils.DriverAction.takeSnapShot"), true, false));
                imports.add(new ImportDeclaration(new NameExpr("com.gemini.generic.ui.utils.DriverManager"), false, false));
                imports.add(new ImportDeclaration(new NameExpr("com.gemini.generic.ui.utils.DriverAction"), false, false));
            } else {
                imports.add(new ImportDeclaration(new NameExpr("net.serenitybdd.core.pages.WebElementFacade"), false, false));
                imports.add(new ImportDeclaration(new NameExpr("net.serenitybdd.core.pages.PageObject"), false, false));
            }

            imports.add(new ImportDeclaration(new NameExpr("pageobjectgenerator.Settings"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("static org.junit.Assert.assertTrue"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("locators" + "." + Settings.LOCATOR_FILE_NAME), false, false));
//                imports.add(new ImportDeclaration(new NameExpr("static locators" + "." + Settings.LOCATOR_FILE_NAME + "." + "driver"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.support.ui.ExpectedConditions"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.support.ui.WebDriverWait"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("java.time.Duration"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.WebElement"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.JavascriptExecutor"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.apache.commons.lang.StringUtils"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.interactions.Actions"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.WebDriver"), false, false));

        }

        Settings.LOGGER.info("Imports added are:" + imports);
        return imports;
    }

    /**
     * creates the webElements and WedDriver variables together with the correct
     * locators (for now XPath or CSS locators are used)
     *
     * @param c           CompilationUnit
     * @param webElements List<CandidateWebElement>
     */
    public static void setVariables(CompilationUnit c, Set<CandidateWebElement> webElements) {

        setWebElements(c, webElements);
//        setWebDriverVariable(c,String className);
        //setGetter(c,webElements);

    }

    /**
     * creates the webElements instances
     *
     * @param c           CompilationUnit
     * @param webElements List<CandidateWebElement>
     */
    private static void setWebElements(CompilationUnit c, Set<CandidateWebElement> webElements) {

        for (CandidateWebElement cwe : webElements) {

            VariableDeclarator webElement = new VariableDeclarator();
            webElement.setId(new VariableDeclaratorId(cwe.getVariableName()));

            FieldDeclaration field = ASTHelper.createFieldDeclaration(ModifierSet.PRIVATE, ASTHelper.createReferenceType("WebElement", 0), webElement);
            List<AnnotationExpr> list_espr = new LinkedList<>();

            NormalAnnotationExpr locator_annotation = new NormalAnnotationExpr();
            locator_annotation.setName(new NameExpr("FindBy"));

            List<MemberValuePair> list_mvp = new LinkedList<>();
            MemberValuePair mvp = new MemberValuePair();

            if (cwe.getCssLocator() == null) {
                String xpathLocator = cwe.getXpathLocator();
                xpathLocator = "\"" + xpathLocator + "\"";
                mvp = new MemberValuePair("xpath", new NameExpr(xpathLocator));
            } else if (cwe.getCssLocator() != null) {
                String cssLocator = cwe.getCssLocator();
                cssLocator = "\"" + cssLocator + "\"";
                mvp = new MemberValuePair("css", new NameExpr(cssLocator));
            } else if (cwe.getVariableName() == null) {

            }
            list_mvp.add(mvp);
            locator_annotation.setPairs(list_mvp);
            list_espr.add(0, locator_annotation);

            field.setAnnotations(list_espr);
            ASTHelper.addMember(c.getTypes().get(0), field);
        }
    }

    /**
     * creates the webElements instances
     *
     * @param c           CompilationUnit
     * @param webElements List<CandidateWebElement>
     */
    private static void setGetter(CompilationUnit c, Set<CandidateWebElement> webElements) {

        for (CandidateWebElement cwe : webElements) {

            // Add the Getter method
            // /////////////////////////////////////////////////////////////
            MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.createReferenceType("WebElement", 0), "get" + cwe.getVariableName());
            // add a body to the method
            BlockStmt block = new BlockStmt();
            method.setBody(block);

            // add a statement do the method body
            ASTHelper.addStmt(block, new NameExpr("return " + cwe.getVariableName()));
            ASTHelper.addMember(c.getTypes().get(0), method);
        }
    }

    /**
     * For each CompilationUnit c associated to a State s creates the link methods
     * to navigate towards other page objects
     *
     * @param c
     * @param s
     */
    public static void setLinkMethods(CompilationUnit c, State s) {

        for (Edge edge : s.getLinks()) {

            String towards = UtilsStaticAnalyzer.getStateNameFromStateId(edge.getTo());

            // add the necessary import
            ImportDeclaration new_import = new ImportDeclaration(new NameExpr("locators." + towards), false, false);

            if (!towards.equals("") && !c.getImports().contains(new_import)) {
                c.getImports().add(new_import);
            }

            String l = edge.getVia();
            l = l.replace("xpath ", "");
            String we = s.getWebElementNameFromLocator(l);

            if (we.equals("")) {
                System.err.println("[ERROR] UtilsCodeGenerator.setLinkMethods getWebElementNameFromLocator failed");
                // System.exit(1);
            }

            String eventType = edge.getEvent() + "()";

            MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.createReferenceType(towards, 0), "goTo" + towards);
            // + "_via_" + we);

            // add a body to the method
            BlockStmt block = new BlockStmt();
            method.setBody(block);

            // add a statement do the method body
            ASTHelper.addStmt(block, new NameExpr(we + "." + eventType));
            ASTHelper.addStmt(block, new NameExpr("return new " + towards + "(driver)"));

            String name = method.getName();
            int occ = 0;

            for (BodyDeclaration bd : c.getTypes().get(0).getMembers()) {
                if (bd instanceof MethodDeclaration) {
                    if (((MethodDeclaration) bd).getName().contains(name)) {
                        occ++;
                    }
                }
            }

            if (occ > 0) {
                method.setName(name + "_" + occ);
            }

            ASTHelper.addMember(c.getTypes().get(0), method);
        }
    }

    // get attribute method
    public static void setLinkMethodsAttributeGetter(CompilationUnit c, Field field) throws IOException {

        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        // Add the Getter method
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.createReferenceType("String", 0), "getAttributeFrom" + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        List<Parameter> parameters = new LinkedList<>();
        Settings.LOGGER.info(parameters.toString());
        parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "attributeValue"));
        method.setParameters(parameters);
        // add a statement do the method body
        ASTHelper.addStmt(block, new NameExpr("//This function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        //DriverAction.getAttributeName() of Gemjar Framework to get the specific attribute of an element

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("String text = new String()"));
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\ttext = getAttributeName(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ", attributeValue)"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "GemTestReporter.addTestStep(\"Get value of \" + attributeValue + \" attribute for \" + \"" + field.getName() + " field\",\"Unable to get attribute value\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}"));
            ASTHelper.addStmt(block, new NameExpr("return text"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets attribute as \"" + "+" + "attributeValue" + "+" + "\" for " + field.getName() + " element\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\t$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "getAttribute(" + "attributeValue" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}\n\t\t" + "return " + "attributeValue"));
        }
        Settings.LOGGER.info(String.valueOf(new NameExpr(field.getName() + "." + "getAttribute(" + "attributeValue" + ")")));
        Settings.LOGGER.info(String.valueOf(new NameExpr("return " + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ".getAttribute(" + "attributeValue" + ")")));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    //navigate back to the tab method
    public static void setLinkMethodsClickAndNavigateBack(CompilationUnit c, Field field) throws IOException {


        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "clickOn" + meaningFulName + "AndNavigateBack");
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        ASTHelper.addStmt(block, new NameExpr("//This function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tclick(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tnavigateBack()"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("Settings" + "." + "LOGGER" + "." + "info(" + "\"User click on " + field.getName() + " element\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "click" + "(" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}\n\t\t" + Settings.LOCATOR_FILE_NAME + "." + "driver" + "." + "navigate().back()"));
            Settings.LOGGER.info(String.valueOf(new NameExpr(Settings.LOCATOR_FILE_NAME + "." + "driver" + "." + "navigate().back()")));
            ASTHelper.addStmt(block, new NameExpr("Settings" + "." + "LOGGER" + "." + "info(" + "\"User navigates back  to previous page\")"));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    // get value assertion
    public static void setLinkMethodsValueVerification(CompilationUnit c, Field field) throws IOException {

        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        // Add the Getter method
        // /////////////////////////////////////////////////////////////
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.createReferenceType("String", 0), "getValueFrom" + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        List<Parameter> parameters = new LinkedList<>();
        parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "typeText"));
        method.setParameters(parameters);

        // add a statement do the method body
        ASTHelper.addStmt(block, new NameExpr("//This function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        //DriverAction.getAttributeName(element,"value") of Gemjar Framework to get the value attribute of an element

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("String text = new String()"));
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\ttext = getAttributeName(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ",\"value\")"));
            ASTHelper.addStmt(block, new NameExpr("\tif(typeText.equals(text)){\n\t\t\t\t" + "GemTestReporter.addTestStep(\"Verify if value of Value attribute matches\" +typeText,\"Validation Successfull\", STATUS.PASS, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("\t}\n\t\t\telse{\n\t\t\t\t" + "GemTestReporter.addTestStep(\"Verify if value of Value attribute matches\" +typeText,\"Validation Failed\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("\t}\n\t\t}" + "\n\t\tcatch(" + "Exception e" + "){"));
            ASTHelper.addStmt(block, new NameExpr("\tGemTestReporter.addTestStep(\"Get value of Value attribute for " + field.getName() + " field\",\"Unable to get attribute value\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}"));
            ASTHelper.addStmt(block, new NameExpr("return text"));

        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "assertTrue(" + "StringUtils.equalsIgnoreCase(" + "typeText" + "," + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "getAttribute(\"value\")" + "))"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}\n\t\t" + "return " + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + ".getAttribute(\"value\")"));
            Settings.LOGGER.info(String.valueOf(new NameExpr("return " + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + ".getAttribute(\"value\")")));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    //isSelect Method verification
    public static void setLinkMethodForIsSelected(CompilationUnit c, Field field) throws IOException {
        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "verify" + meaningFulName + "IsSelected");
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        ASTHelper.addStmt(block, new NameExpr("//This function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "getElement(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ").isSelected()"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User verifies the given " + field.getName() + " element is selected\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tGemTestReporter.addTestStep(\"Verify if the given " + field.getName() + " is selected\",\"The given " + field.getName() + " is selected\", STATUS.PASS, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "GemTestReporter.addTestStep(\"Verify if the given " + field.getName() + " is selected\",\"The given " + field.getName() + " is not selected\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "assertTrue(" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "isSelected" + "(" + "))"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User verifies the given " + field.getName() + " element is selected\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        }
        Settings.LOGGER.info(String.valueOf(new NameExpr("assertTrue(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + "." + "isSelected" + "(" + "))")));

        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkMethodsTypeSetter(CompilationUnit c, Field field) throws IOException {
        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "typeTextInto" + meaningFulName);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        List<Parameter> parameters = new LinkedList<>();
        Settings.LOGGER.info(parameters.toString());
        parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "typeText"));
        method.setParameters(parameters);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        //DriverAction.typetext() of Gemjar Framework to type the text into the input box

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "typeText(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + "," + "typeText" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User enters " + '"' + "+" + "typeText" + "+" + '"' + " as value\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "GemTestReporter.addTestStep(\"Enter the text in " + field.getName() + " field\",\"Unable to Enter Text in " + field.getName() + " field\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "typeInto(" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "," + "typeText" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User enters " + '"' + "+" + "typeText" + "+" + '"' + " as value\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
            Settings.LOGGER.info(String.valueOf(new NameExpr(field.getName() + "Element" + "." + "type" + "(" + "typeText" + ")")));
        }


        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }


    public static void setLinkMethodsClear(CompilationUnit c, Field field) throws IOException {


        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "clear" + meaningFulName);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        //DriverAction.clearText() of Gemjar Framework to clear the text from the input field

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tclearText(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "GemTestReporter.addTestStep(\"Check if " + field.getName() + " is cleared\",\"" + field.getName() + " is unable to be cleared\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ") " + "." + "clear" + "(" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User deletes the value for " + field.getName() + " element\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }


    public static void setLinkMethodsTypeGetter(CompilationUnit c, Field field) throws IOException {

        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        // Add the Getter method

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.createReferenceType("String", 0), "getTextFrom" + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        // add a statement do the method body
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        //DriverAction.getElementText() of Gemjar Framework to get the the text of an element

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("String text = new String()"));
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\ttext = getElementText(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("} \n\t\treturn text"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets the text of " + field.getName() + " element\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}\n\t\t" + "return " + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "getText" + "(" + ")"));
            Settings.LOGGER.info(String.valueOf(new NameExpr("return " + Settings.LOCATOR_FILE_NAME + "." + field.getName() + "." + "getText" + "(" + ")")));
        }

        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setMethodScrollClick(CompilationUnit c, Field field) throws IOException {
        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "scrollClick" + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);

// add a statement do the method body
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tscrollIntoView(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + "); \n\t\t\t" + "click(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "JavascriptExecutor" + " " + "js" + "=" + "(" + "JavascriptExecutor" + ")" + "driver"));
            ASTHelper.addStmt(block, new NameExpr("\tjs" + "." + "executeScript(\"arguments[0].scrollIntoView()\"" + "," + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + "))"));
            ASTHelper.addStmt(block, new NameExpr("\t$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "click" + "(" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}\n\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User is able to scroll and click on the " + field.getName() + " element\"" + ")"));

        }
        Settings.LOGGER.info(String.valueOf(new NameExpr("js" + "." + "executeScript(\"arguments[0].scrollIntoView()\"" + "," + field.getName() + "Element" + ")")));


        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());

    }

    public static void setLinkMethodsDropDown(CompilationUnit c, Field field) throws IOException {


        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "select" + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        List<Parameter> parameters = new LinkedList<>();
        parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "typeText"));
        method.setParameters(parameters);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "dropDown(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + "," + "typeText" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User is able to select \" + " + "typeText" + " +\" visible text in the dropdown\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "selectByVisibleText" + "(" + "typeText" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User is able to select \" + " + "typeText" + " +\" visible text in the dropdown\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        }

        Settings.LOGGER.info(String.valueOf(new NameExpr("new Select (" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "selectByVisibleText" + "(" + "typeText" + ")")));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setMethodClickable(CompilationUnit c, Field field) throws IOException {
        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "elementIsClickable" + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tWebDriverWait wait = new WebDriverWait(DriverManager.getWebDriver(), Duration.ofSeconds((long) 10))"));
            ASTHelper.addStmt(block, new NameExpr("\tWebElement element = wait.until(ExpectedConditions.elementToBeClickable(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + "))"));
            ASTHelper.addStmt(block, new NameExpr("\tGemTestReporter.addTestStep(\"Check if " + field.getName() + " is clickable\",\"" + field.getName() + " is clickable\", STATUS.PASS, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "GemTestReporter.addTestStep(\"Check if " + field.getName() + " is clickable\",\"" + field.getName() + " is not clickable\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "assertTrue(" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "isClickable" + "(" + "))"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User verifies the given " + field.getName() + " element is clickable\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        }
        Settings.LOGGER.info(String.valueOf(new NameExpr("assertTrue(" + field.getName() + "Element" + "." + field.getName() + "." + "isClickable" + "(" + "))")));


        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkMethodForEnabled(CompilationUnit c, Field field) throws IOException {

        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "verify" + meaningFulName + "IsEnabled");
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        //DriverAction.getElement().isEnabled() of Gemjar Framework to check whether the element is enabled

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tgetElement(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ").isEnabled()"));
            ASTHelper.addStmt(block, new NameExpr("\tGemTestReporter.addTestStep(\"Check if " + field.getName() + " is enabled\",\"" + field.getName() + " is enabled\", STATUS.PASS, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "GemTestReporter.addTestStep(\"Check if " + field.getName() + " is enabled\",\"" + field.getName() + " is not enabled\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "assertTrue(" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "isEnabled" + "(" + "))"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
            Settings.LOGGER.info(String.valueOf(new NameExpr("assertTrue(" + field.getName() + "Element" + "." + "isEnabled" + "(" + "))")));
            ASTHelper.addStmt(block, new NameExpr("Settings" + "." + "LOGGER" + "." + "info(" + "\"User verifies the given " + field.getName() + " element is enabled\"" + ")"));
        }

        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }


    public static void setLinkMethodForVisibility(CompilationUnit c, Field field) throws IOException {

        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "verify" + meaningFulName + "IsDisplayed");
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tgetElement(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ").isDisplayed()"));
            ASTHelper.addStmt(block, new NameExpr("\tGemTestReporter.addTestStep(\"Check if " + field.getName() + " is visible\",\"" + field.getName() + " is visible\", STATUS.PASS, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "GemTestReporter.addTestStep(\"Check if " + field.getName() + " is visible\",\"" + field.getName() + " is not visible\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "assertTrue(" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "isDisplayed" + "(" + "))"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User verifies " + field.getName() + " element is displayed\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }


    public static void setLinkMethodForText(CompilationUnit c, Field field) throws IOException {

        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "verify" + meaningFulName + "Text");
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        List<Parameter> parameters = new LinkedList<>();
        parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "typeText"));
        method.setParameters(parameters);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));

        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tif(getElementText(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ").equals(typeText)){"));
            ASTHelper.addStmt(block, new NameExpr("\t\tGemTestReporter.addTestStep(\"Check if text inside " + field.getName() + " is equal to \" + typeText,\"Text inside " + field.getName() + " is equal to \" + typeText, STATUS.PASS, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("\t} else{\n\t\t\t\t" + "GemTestReporter.addTestStep(\"Check if text inside " + field.getName() + " is equal to \" + typeText,\"Text inside " + field.getName() + " is not equal\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("\t}\n\t\t}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "GemTestReporter.addTestStep(\"Check if text inside " + field.getName() + " is equal to \" + typeText,\"Text inside " + field.getName() + " is not equal\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "assertTrue(" + "StringUtils.equalsIgnoreCase(" + "typeText" + "," + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "getText" + "(" + ")))"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets the text of " + field.getName() + " element\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        }
        Settings.LOGGER.info(String.valueOf(new NameExpr("assertTrue(" + "StringUtils.equalsIgnoreCase(" + "typeText" + "," + field.getName() + "Element" + "." + "getText" + "(" + ")))")));

        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static String readProperties(String property) throws IOException { // Function to read Data from Properties File
        FileReader read = new FileReader("C:\\Users\\rahul.tagra\\Desktop\\MethodGenerator\\src\\test\\resources\\config.properties");
        Properties credential = new Properties();
        credential.load(read);
        return credential.getProperty(property);
    }

    public static void setLinkMethodsOpenHomePage(CompilationUnit c) throws IOException {


        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, Settings.USER_HOME_PAGE);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tDriverManager.setUpBrowser()"));
            ASTHelper.addStmt(block, new NameExpr("} \n\t\tcatch(" + "Exception e" + "){" + "\n\t\t\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("getDriver(" + ")" + "." + "get" + "(" + "Settings.URL" + ")"));
            ASTHelper.addStmt(block, new NameExpr("Settings" + "." + "LOGGER" + "." + "info(" + "\"User launches the application\"" + ")"));
            Settings.LOGGER.info(String.valueOf(new NameExpr("getDriver()" + "." + "get" + "(" + "Settings.URL" + ")")));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkMethodsClick(CompilationUnit c, Field field) throws IOException {

        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "clickOn" + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);

        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tclick(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "click" + "(" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User click on the " + field.getName() + " element\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }


    public static void setLinkMethodsDoubleClick(CompilationUnit c, Field field) throws IOException {


        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "doubleClickOn" + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        //Double click on element
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tdoubleClick(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "Actions" + " " + "action" + "=" + "new" + " " + "Actions(" + "driver" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\taction" + "." + "doubleClick(" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + ")" + "." + "perform" + "(" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User double click on the element\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());

    }

    public static void setLinkMethodForUpload(CompilationUnit c, Field field) throws IOException {
        meaningFulName = UtilsMethodCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "uploadFileTo" + meaningFulName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        List<Parameter> parameters = new LinkedList<>();
        parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "fileName"));
        method.setParameters(parameters);
        String filePath = System.getProperty("user.dir") + "\\src\\main\\resources\\data\\";
        Settings.LOGGER.info(parameters.toString());
        //path of file
        filePath = filePath.replace("\\", "\\\\");
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "typeText(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ", \"" + filePath + " \" + fileName" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User uploads the " + '"' + "+" + "fileName" + "+" + '"' + " file\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "GemTestReporter.addTestStep(\"Upload the\" + fileName + \"file\",\"Unable to upload\" + fileName + \"field\", STATUS.FAIL, takeSnapShot())"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\t" + "$(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")" + "." + "sendKeys" + "(" + '"' + filePath + '"' + "+" + "fileName" + ")"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User uploads the file\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}" + "\n\t\tcatch(" + "Exception e" + "){\n\t\t\t" + "Settings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        }
        Settings.LOGGER.info(String.valueOf(new NameExpr(field.getName() + "Element" + "." + "sendKeys" + "(" + '"' + filePath + '"' + "+" + "fileName" + ")")));


        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    private static int countTowards(Set<Edge> links, Edge edge3) {

        int c = 0;

        for (Edge edge : links) {
            if (edge3.getTo().equals(edge.getTo())) {
                c++;
            }
        }

        return c;
    }

    /**
     * For each CompilationUnit c associated to a State s creates the form methods.
     * <p>
     * It follows a naive approach: parses the form objects and puts everything in
     * the method
     * </p>
     *
     * @param c CompilationUnit
     * @param s State
     */
    public static void setFormMethods(CompilationUnit c, State s) {

        if (s.getForms() == null) {
            return;
        }

        for (Form f : s.getForms()) {

            MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, f.getFormName());
            BlockStmt block = new BlockStmt();
            method.setBody(block);

            for (int i = 0; i < f.getFormFieldList().size(); i++) {

                addIndexedParameterToFormMethod(f, i, method);

            }

            for (FormField field : f.getFormFieldList()) {

                addFormInstructionToBlockMethod(block, f, field);

            }

            ASTHelper.addMember(c.getTypes().get(0), method);
        }

    }

    /**
     * For each CompilationUnit c associated to a State s creates the form
     * methods.
     * <p>
     * It follows a more sophisticated approach: parses the form objects and creates
     * a method for each submit/button only
     * </p>
     *
     * @param c CompilationUnit
     * @param s State
     * @throws Exception
     */
    public static void setFormMethodsFromButtonAndSubmit(CompilationUnit c, State s) throws Exception {

        if (s.getForms() == null) {
            return;
        }

        for (Form f : s.getForms()) {

            for (InputField i : f.getSubmitList()) {
                System.out.println("[LOG] " + f.getSubmitList().size() + " submit/button(s) found in form " + f.getFormName());

                MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, f.getFormName() + "_" + i.getVariableName());

                BlockStmt block = new BlockStmt();
                method.setBody(block);

                if (f.getSubmitList().size() == 1) {

                    for (int j = 0; j < f.getFormFieldList().size(); j++) {

                        addIndexedParameterToFormMethod(f, j, method);

                    }

                    for (FormField field : f.getFormFieldList()) {

                        addFormInstructionToBlockMethod(block, f, field);

                    }

                } else if (f.getSubmitList().size() > 1) {
                    addFormInstructionToBlockMethod(block, f, i);
                } else {
                    throw new Exception("Form does not contains any submit!");
                }

                ASTHelper.addMember(c.getTypes().get(0), method);

            }
        }

    }

    private static void addParameterToFormMethod(Form f, MethodDeclaration method) {

        Parameter par = ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "param");
        par.setVarArgs(false);
        ASTHelper.addParameter(method, par);

    }

    private static void addIndexedParameterToFormMethod(Form f, int i, MethodDeclaration method) {

        if (f.getFormFieldList().get(i).getDefaultAction().equals("sendKeys")) {
            Parameter par = ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "args" + i);
            par.setVarArgs(false);
            ASTHelper.addParameter(method, par);
        }

    }

    private static void addFormInstructionToBlockMethod(BlockStmt block, Form f, FormField field) {

        switch (field.getDefaultAction()) {
            case "sendKeys":
                ASTHelper.addStmt(block, new NameExpr(field.getVariableName() + "." + field.getDefaultAction() + "(args" + f.getFormFieldList().indexOf(field) + ")"));
                break;

            case "click":
                ASTHelper.addStmt(block, new NameExpr(field.getVariableName() + "." + field.getDefaultAction() + "()"));
                break;
            default:
                break;
        }

    }

    /**
     * formats the string to get a valid variable name
     *
     * @param s
     * @return
     */
    public static String formatToVariableName(String s) {

        String res = s;

        res = UtilsStaticAnalyzer.toSentenceCase(res);
        res = res.replaceAll(" ", "");
        res = StringUtils.uncapitalize(res);

        return res;
    }

    /**
     * Creates getters methods to be used for assertions. Created on differences
     * between adjacent pages
     *
     * @param c
     * @param s
     */
    public static void setGettersMethods(CompilationUnit c, State s) {

        if (s.getDiffs() == null) {
            return;
        }

        for (Getter d : s.getDiffs()) {

            // /////////////////////////////////////////////////////////////
            // Add the WebElement
            // /////////////////////////////////////////////////////////////
            VariableDeclarator webElement = new VariableDeclarator();
            webElement.setId(new VariableDeclaratorId(d.getWebElementName()));

            FieldDeclaration field = ASTHelper.createFieldDeclaration(ModifierSet.PRIVATE, ASTHelper.createReferenceType("WebElement", 0), webElement);

            List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();

            NormalAnnotationExpr na = new NormalAnnotationExpr();
            na.setName(new NameExpr("FindBy"));

            List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
            MemberValuePair mvp = new MemberValuePair();

            String xpathLocator = d.getLocator();
            xpathLocator = "\"" + xpathLocator + "\"";
            mvp = new MemberValuePair("xpath", new NameExpr(xpathLocator));

            list_mvp.add(mvp);
            na.setPairs(list_mvp);
            list_espr.add(0, na);

            field.setAnnotations(list_espr);
            ASTHelper.addMember(c.getTypes().get(0), field);

            // /////////////////////////////////////////////////////////////
            // Add the Getter method
            // /////////////////////////////////////////////////////////////
            MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.createReferenceType("String", 0), "get_" + d.getWebElementName());

            // add a body to the method
            BlockStmt block = new BlockStmt();
            method.setBody(block);

            /**
             * public String getGroupsName() { return groupContainer.getText(); }
             */
            JavadocComment javaDoc = new JavadocComment("\n\t\tsource: " + d.getSourceState() + "" + "\n\t\ttarget: " + d.getTargetState() + "" + "\n\t\tcause: " + d.getCause() + "" + "\n\t\tbefore: " + d.getBefore() + "" + "\n\t\tafter: " + d.getAfter() + "" + " \n\t");
            method.setJavaDoc(javaDoc);

            // add a statement do the method body

            ASTHelper.addStmt(block, new NameExpr("return " + d.getWebElementName() + ".getText()"));
            Settings.LOGGER.info(String.valueOf(new NameExpr("return " + d.getWebElementName() + ".getText()")));
            ASTHelper.addMember(c.getTypes().get(0), method);
            Settings.LOGGER.info(method.toString());
            Settings.LOGGER.info(c.getTypes().get(0).toString());
        }

    }

    public static void setSourceCode(CompilationUnit c, State s) {
        s.setSourceCode(StringUtils.replace(c.toString(), "private WebElement", "public static WebElement"));
    }


    /**
     * formats a string to camelCase
     *
     * @param tempVarName
     * @return
     */
    private static String getMeaningFullName(String tempVarName, boolean isMethodCall) {
        String res = tempVarName;
        res = res.replaceAll("\\s", "");
        res = res.replaceAll("&amp", "");
        res = res.replaceAll("#", "");

        StringBuilder meaningfulName = new StringBuilder();
        for (String name : res.split("_")) {
            meaningfulName.append(StringUtils.capitalize(name));
        }
//		if(BooleanUtils.isTrue(isMethodCall)) {
//			res = res.replaceAll("_", " ");
//		}else {
//			res = res.replaceAll("_", "");
//		}

//		res = StringUtils.capitalize(res);

        return meaningfulName.toString();
    }

    /**
     * makes the source code of each state persistent
     *
     * @throws IOException
     */
    public static void savePageObjectsOnFileSystem(String directoryBame, String className, CompilationUnit c, boolean stepGeneration) throws IOException {

        String data = "";
        String fileNameToCreate = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator + directoryBame + className;
        // String fileNameToCreate = System.getProperty("user.dir") +poName;
        File f = new File(fileNameToCreate + ".java");

        if (BooleanUtils.isTrue(stepGeneration)) {
            data = StringUtils.replace(c.toString(), "xpath = ", "").toString();
        } else {
            data = c.toString();
        }
        FileUtils.writeStringToFile(f, data);

    }

    public static void setLinkMethodsNavigateBack(CompilationUnit c) throws IOException {
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "backwardNavigation");
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tnavigateBack()"));
            ASTHelper.addStmt(block, new NameExpr("} \n\t\tcatch(" + "Exception e" + "){" + "\n\t\t\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tgetDriver().navigate().back()"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User successfully navigated back\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("} \n\t\tcatch(" + "Exception e" + "){" + "\n\t\t\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
            Settings.LOGGER.info(String.valueOf(new NameExpr("getDriver()" + "." + "get" + "(" + "Settings.URL" + ")")));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkMethodsNavigateForward(CompilationUnit c) throws IOException {
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "forwardNavigation");
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tnavigateForward()"));
            ASTHelper.addStmt(block, new NameExpr("} \n\t\tcatch(" + "Exception e" + "){" + "\n\t\t\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tgetDriver().navigate().forward()"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User successfully navigated Forward\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("} \n\t\tcatch(" + "Exception e" + "){" + "\n\t\t\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
            Settings.LOGGER.info(String.valueOf(new NameExpr("getDriver()" + "." + "get" + "(" + "Settings.URL" + ")")));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkMethodsNavigateTo(CompilationUnit c) throws IOException {
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "navigateTo");
        // add a body to the method
        List<Parameter> parameters = new LinkedList<>();
        Settings.LOGGER.info(parameters.toString());
        parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "url"));
        method.setParameters(parameters);
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        if (readProperties("Framework").contains("GEMJAR")) {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tDriverManager.getWebDriver().navigate().to(url)"));
            ASTHelper.addStmt(block, new NameExpr("} \n\t\tcatch(" + "Exception e" + "){" + "\n\t\t\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
        } else {
            ASTHelper.addStmt(block, new NameExpr("try{\n\t\t\tgetDriver().navigate().to(url)"));
            ASTHelper.addStmt(block, new NameExpr("\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User successfully navigated Forward\"" + ")"));
            ASTHelper.addStmt(block, new NameExpr("} \n\t\tcatch(" + "Exception e" + "){" + "\n\t\t\tSettings" + "." + "LOGGER" + "." + "info(" + "\"User gets an exception: \"" + "+" + "e" + ")"));
            ASTHelper.addStmt(block, new NameExpr("}"));
            Settings.LOGGER.info(String.valueOf(new NameExpr("getDriver()" + "." + "get" + "(" + "Settings.URL" + ")")));
        }
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }
}
