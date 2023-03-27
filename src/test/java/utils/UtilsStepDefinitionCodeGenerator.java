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
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class UtilsStepDefinitionCodeGenerator {

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
    public static CompilationUnit createEnhancedCompilationUnit(String name, String type) {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(name)));
        cu.setImports(UtilsStepDefinitionCodeGenerator.getAllImports(type));
        return cu;
    }

    /**
     * set the TypeDeclaration of a CompilationUnit i.e., whether is a class or an
     * interface
     *
     * @param c
     * @param className
     */
    public static void setTypeDeclaration(CompilationUnit c, String className) {
        // create the type declaration and class
        ClassOrInterfaceDeclaration type = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, className);
        ASTHelper.addTypeDeclaration(c, type);
    }

    /**
     * adds a WebDriver instance to the CompilationUnit c
     *
     * @param c CompilationUnit
     */
    public static void setWebDriverVariable(CompilationUnit c) {
        //setting the driver for the current class
        VariableDeclarator v = new VariableDeclarator();
        v.setId(new VariableDeclaratorId("driver"));
        FieldDeclaration f = ASTHelper.createFieldDeclaration(ModifierSet.PRIVATE,
                ASTHelper.createReferenceType("WebDriver", 0), v);

        ASTHelper.addMember(c.getTypes().get(0), f);
    }

    /**
     * adds a WebDriver instance to the CompilationUnit c
     *
     * @param c CompilationUnit
     */
    public static void setStepDefinitionVariable(CompilationUnit c, String classname) {
        //setting the variable for step definition
        VariableDeclarator v = new VariableDeclarator();
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        v.setId(new VariableDeclaratorId(nameOfFile));
        v.setInit(new ObjectCreationExpr(null, new ClassOrInterfaceType(null, classname), null));
        FieldDeclaration f = ASTHelper.createFieldDeclaration(ModifierSet.PRIVATE,
                ASTHelper.createReferenceType(classname, 0), v);

        ASTHelper.addMember(c.getTypes().get(0), f);
    }

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
        constructor.setJavaDoc(
                new JavadocComment("\n\t\tPage Object for " + s.getName() + " (" + s.getStateId() + ") \n\t"));

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
    private static List<ImportDeclaration> getAllImports(String type) {
        List<ImportDeclaration> imports = new LinkedList<>();

        if (StringUtils.equalsIgnoreCase(type, "PageObject")) {
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium"), false, true));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.support.FindBy"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.support.PageFactory"), false, false));
        } else {
            //adding imports for stepDefinition class

            imports.add(new ImportDeclaration(new NameExpr("io.cucumber.java.Before"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("io.cucumber.java.en.Given"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("io.cucumber.java.en.Then"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("io.cucumber.java.en.When"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("org.openqa.selenium.support.PageFactory"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("java.util.concurrent.TimeUnit"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("implementation" + "." + Settings.LOCATOR_FILE_NAME + "Implementation"), false, false));
            imports.add(new ImportDeclaration(new NameExpr("locators" + "." + Settings.LOCATOR_FILE_NAME), false, false));
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
        setWebDriverVariable(c);
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

            FieldDeclaration field = ASTHelper.createFieldDeclaration(ModifierSet.PRIVATE,
                    ASTHelper.createReferenceType("WebElement", 0), webElement);
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

    // before class for driver initialisation
    public static void setBeforeClass(CompilationUnit c) {
        String functionName = "";
        String annotationValue = "";
        String blockToEnter = "driver";
        String annotationType = "";
        List<Parameter> parameters = new LinkedList<>();
        functionName = Settings.BEFORE_FUNCTION;
        annotationType = "Before";
        Settings.LOGGER.info("Step created for initialisation of the driver");
        NameExpr typeOfException = new NameExpr("InterruptedException");
        List<NameExpr> throws_ = new LinkedList<>();
        throws_.add(typeOfException);
        MethodDeclaration method = new MethodDeclaration(null, ModifierSet.PUBLIC, null, null, ASTHelper.VOID_TYPE, functionName, null, 0, throws_, null);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();
        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);
        method.setAnnotations(list_espr);
        na.setName(new NameExpr(annotationType));
        ASTHelper.addStmt(block, new NameExpr("//This function is for initialisation of driver"));
        ASTHelper.addStmt(block, new NameExpr("System" + "." + "setProperty(" + "\"webdriver.chrome.driver\"" + "," + "\"src/test/resources/drivers/chromedriver.exe\"" + ")"));
        ASTHelper.addStmt(block, new NameExpr(Settings.LOCATOR_FILE_NAME + " index" + "=" + "PageFactory" + "." + "initElements(" + Settings.LOCATOR_FILE_NAME + "." + blockToEnter + "," + Settings.LOCATOR_FILE_NAME + "." + "class" + ")"));
        ASTHelper.addStmt(block, new NameExpr(Settings.LOCATOR_FILE_NAME + "." + blockToEnter + "." + "manage(" + ")" + "." + "window(" + ")" + "." + "maximize(" + ")"));
        ASTHelper.addStmt(block, new NameExpr(Settings.LOCATOR_FILE_NAME + "." + blockToEnter + "." + "manage(" + ")" + "." + "timeouts(" + ")" + "." + "wait(" + "5000" + ")"));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkStepDefinitionMethodValueThenVisibility(CompilationUnit c, Field field,
                                                                      boolean textOrVisibilityParameter) {

        String functionName = getMeaningFullName(field.getName(), false);
        meaningFulName = getMeaningFullName(field.getName(), true);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        String annotationValue = "";
        String blockToEnter = "";
        String annotationType = "";
        annotationType = "Then";
        MethodDeclaration method = null;
        List<Parameter> parameters = new LinkedList<>();
        if (!textOrVisibilityParameter) {
            functionName = "verify" + functionName + "IsDisplayed";
            annotationValue = "\"" + "^User verifies " + meaningFulName + " is visible" + "$" + "\"";
            blockToEnter = functionName + "(" + ")";
            method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        } else {
            functionName = "verify" + functionName + "Value";
            annotationValue = "\"" + "^" + "User verifies " + "\\\"(.*)\\\"" + meaningFulName + " value" + "$" + "\"";
            blockToEnter = functionName + "(" + "typeText" + ")";
            method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
            parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "typeText"));
            method.setParameters(parameters);
        }
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);
        ASTHelper.addStmt(block, new NameExpr("//This function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkStepDefinitionMethod(CompilationUnit c, Field field, String type) {


        meaningFulName = UtilsStepDefinitionCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);

        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";
        List<Parameter> parameters = new LinkedList<>();
        if (StringUtils.equalsIgnoreCase(type, "button") || StringUtils.equalsIgnoreCase(type, "click") || StringUtils.equalsIgnoreCase(type, "image") || StringUtils.equalsIgnoreCase(type, "a")) {
            functionName = Settings.USER_CLICK_FUNCTION + meaningFulName;
            annotationValue = "\"" + Settings.USER_CLICK_ANNOTATION + " " + meaningFulName + " " + type + "$" + "\""; //changed the step definition
            textToEnter = "clickOn" + meaningFulName;
            blockToEnter = textToEnter + "(" + ")";
            annotationType = "When";
            Settings.LOGGER.info("Step crated: " + annotationValue + "and function created: " + functionName);
        }
        if (StringUtils.equalsIgnoreCase(type, "click")) {
            functionName = Settings.USER_SCROLL_CLICK_FUNCTION + meaningFulName;
            annotationValue = "\"" + Settings.USER_SCROLL_CLICK_ANNOTATION + " " + meaningFulName + " " + "element" + "$" + "\""; //changed the step definition
            textToEnter = "scrollClick" + meaningFulName;
            blockToEnter = textToEnter + "(" + ")";
            annotationType = "When";
            Settings.LOGGER.info("Step crated: " + annotationValue + "and function created: " + functionName);
        }
        if (StringUtils.equalsIgnoreCase(type, "input")) {
            functionName = Settings.USER_INPUT_FUNCTION + meaningFulName;
            annotationValue = "\"" + Settings.USER_INPUT_ANNOTATION + " " + meaningFulName + " " + type + "$" + "\"";
            textToEnter = "typeTextInto" + meaningFulName;
            blockToEnter = textToEnter + "(" + "typeText" + ")";
            parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "typeText"));
            annotationType = "When";

        }
        if (StringUtils.equalsIgnoreCase(type, "dropdown")) {
            functionName = Settings.USER_SELECT_FUNCTION + meaningFulName;
            annotationValue = "\"" + Settings.USER_SELECT_ANNOTATION + " " + meaningFulName + " " + type + "$" + "\""; //changed the step definition
            textToEnter = "select" + meaningFulName;
            blockToEnter = textToEnter + "(" + "selectValue" + ")";
            parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "selectValue"));
            annotationType = "When";

        }
        if (StringUtils.equalsIgnoreCase(type, "image")) {
            functionName = Settings.USER_IMAGE_FUNCTION + meaningFulName;
            annotationValue = "\"" + Settings.USER_IMAGE_ANNOTATION + " " + meaningFulName + " " + type + "$" + "\""; //changed the step definition
            textToEnter = "uploadFileTo" + meaningFulName;
            blockToEnter = textToEnter + "(" + "filePath" + ")";
            parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "filePath"));
            annotationType = "When";
        }
        if (StringUtils.equalsIgnoreCase(type, "a")) {
            functionName = Settings.USER_CLICK_FUNCTION + meaningFulName + Settings.USER_NAVIGATE_FUNCTION;
            annotationValue = "\"" + Settings.USER_CLICK_ANNOTATION + " " + meaningFulName + " " + Settings.USER_NAVIGATE_ANNOTATION + "$" + "\""; //changed the step definition
            textToEnter = "clickOn" + meaningFulName + Settings.USER_NAVIGATE_FUNCTION;
            blockToEnter = textToEnter + "(" + ")";
            annotationType = "When";
            Settings.LOGGER.info("Step created: " + annotationValue + "and function created: " + functionName);
        }

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        Settings.LOGGER.info(String.valueOf(new NameExpr(nameOfFile + "." + blockToEnter)));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    //is selected verification step
    public static void setLinkStepDefinitionMethodThenSelected(CompilationUnit c, Field field) {
        String functionName = getMeaningFullName(field.getName(), false);
        meaningFulName = getMeaningFullName(field.getName(), true);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        String annotationValue = "";
        String blockToEnter = "";
        String annotationType = "";
        annotationType = "Then";
        MethodDeclaration method = null;
        List<Parameter> parameters = new LinkedList<>();
        functionName = "verify" + functionName + "IsSelected";
        annotationValue = "\"" + "^" + "User verifies " + meaningFulName + " is selected" + "$" + "\"";
        blockToEnter = functionName + "(" + ")";
        method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();
        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);
        method.setAnnotations(list_espr);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    //verification of attribute value
    public static void setLinkStepDefinitionAttributeGetter(CompilationUnit c, Field field, boolean valueVerification) {
        meaningFulName = UtilsStepDefinitionCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";
        List<Parameter> parameters = new LinkedList<>();

        if (valueVerification) {
            functionName = Settings.USER_VERIFIES_VALUE + meaningFulName;
            annotationValue = "\"" + "User verifies the " + "\\\"(.*)\\\"" + " for " + meaningFulName + " element" + "$" + "\""; //changed the step definition
            textToEnter = "getValueFrom" + meaningFulName;
            blockToEnter = textToEnter + "(" + "valueOfElement" + ")";
//        blockToEnter = functionName + "(" + "" + ")";
            annotationType = "Then";
            parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "valueOfElement"));
        } else {
            functionName = Settings.USER_GET_ATTRIBUTE_FUNCTION + meaningFulName;
            annotationValue = "\"" + Settings.USER_GET_ATTRIBUTE_ANNOTATION + meaningFulName + " element" + "$" + "\""; //changed the step definition
            textToEnter = "getAttributeFrom" + meaningFulName;
            blockToEnter = textToEnter + "(" + "attributeValue" + ")";
//        blockToEnter = functionName + "(" + "" + ")";
            annotationType = "When";
            parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "attributeValue"));
        }
        Settings.LOGGER.info("Step created: " + annotationValue + "and function created: " + functionName);

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
//        method.setParameters(parameters);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);
        ASTHelper.addStmt(block, new NameExpr("//This function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());

    }

    public static void setLinkStepDefinitionTextGetter(CompilationUnit c, Field field) {
        meaningFulName = UtilsStepDefinitionCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";
        List<Parameter> parameters = new LinkedList<>();

        functionName = Settings.USER_GETTEXT_FUNCTION + meaningFulName;
        annotationValue = "\"" + Settings.USER_GETTEXT_ANNOTATION + " " + meaningFulName + " " + "element" + "$" + "\""; //changed the step definition
        textToEnter = "getTextFrom" + meaningFulName;
        blockToEnter = textToEnter + "(" + ")";
        annotationType = "When";
        Settings.LOGGER.info("Step crated: " + annotationValue + "and function created: " + functionName);

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());

    }

    public static void setLinkStepDefinitionClear(CompilationUnit c, Field field) {
        meaningFulName = UtilsStepDefinitionCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";
        List<Parameter> parameters = new LinkedList<>();

        functionName = Settings.USER_CLEAR_FUNCTION + meaningFulName;
        annotationValue = "\"" + Settings.USER_CLEAR_ANNOTATION + " " + meaningFulName + " " + "element text" + "$" + "\""; //changed the step definition
        textToEnter = "clear" + meaningFulName;
        blockToEnter = textToEnter + "(" + ")";
        annotationType = "When";
        Settings.LOGGER.info("Step crated: " + annotationValue + "and function created: " + functionName);

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());

    }

    public static void setLinkStepDefinitionMethodGiven(CompilationUnit c, String homePage) {

        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";
        List<Parameter> parameters = new LinkedList<>();
        if (StringUtils.isNotBlank(homePage)) {
            functionName = Settings.USER_HOME_PAGE;
            annotationType = "Given";
            annotationValue = "\"" + Settings.USER_HOME_PAGE_ANNOTATION + "$" + "\"";
            textToEnter = Settings.USER_HOME_PAGE;
            blockToEnter = textToEnter + "(" + ")";

        }
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);

        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkStepDefinitionDoubleCLick(CompilationUnit c, Field field) {
        meaningFulName = UtilsStepDefinitionCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";
        List<Parameter> parameters = new LinkedList<>();

        functionName = Settings.USER_DOUBLE_CLICK_FUNCTION + meaningFulName;
        annotationValue = "\"" + Settings.USER_DOUBLE_CLICK_ANNOTATION + " " + meaningFulName + " " + "element" + "$" + "\""; //changed the step definition
        textToEnter = "doubleClickOn" + meaningFulName;
        blockToEnter = textToEnter + "(" + ")";
        annotationType = "When";
        Settings.LOGGER.info("Step crated: " + annotationValue + "and function created: " + functionName);

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkStepDefinitionMethodThenVisibility(CompilationUnit c, Field field,
                                                                 boolean textOrVisibilityParameter) {

        String functionName = getMeaningFullName(field.getName(), false);
        meaningFulName = getMeaningFullName(field.getName(), true);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        String annotationValue = "";
        String blockToEnter = "";
        String annotationType = "";
        annotationType = "Then";
        MethodDeclaration method = null;
        List<Parameter> parameters = new LinkedList<>();
        if (!textOrVisibilityParameter) {
            functionName = "verify" + functionName + "IsDisplayed";
            annotationValue = "\"" + "^" + meaningFulName + " is visible" + "$" + "\"";
            blockToEnter = functionName + "(" + ")";
            method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        } else {
            functionName = "verify" + functionName + "Text";
            annotationValue = "\"" + "^" + "User verifies" + " " + meaningFulName + " " + "\\\"(.*)\\\"" + " text" + "$" + "\"";
            blockToEnter = functionName + "(" + "typeText" + ")";
            method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
            parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "typeText"));
            method.setParameters(parameters);
        }
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);
        ASTHelper.addStmt(block, new NameExpr("//The below function is for web element @FindBy(" + Settings.LOCATOR_FILE_NAME + "." + field.getName() + ")"));
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkStepDefinitionMethodThenClickable(CompilationUnit c, Field field) {

        meaningFulName = UtilsStepDefinitionCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";
        List<Parameter> parameters = new LinkedList<>();

        functionName = Settings.USER_CLICKABLE_FUNCTION + meaningFulName;
        annotationValue = "\"" + Settings.USER_CLICKABLE_ANNOTATION + " " + meaningFulName + " " + "element" + "$" + "\""; //changed the step definition
        textToEnter = "elementIsClickable" + meaningFulName;
        blockToEnter = textToEnter + "(" + ")";
        annotationType = "Then";
        Settings.LOGGER.info("Step crated: " + annotationValue + "and function created: " + functionName);

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkStepDefinitionMethodThenEnabled(CompilationUnit c, Field field) {

        meaningFulName = UtilsStepDefinitionCodeGenerator.getMeaningFullName(field.getName(), false);
        Settings.LOGGER.info("Name of field: " + meaningFulName);
        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";
        List<Parameter> parameters = new LinkedList<>();

        functionName = Settings.USER_ENABLED_FUNCTION + meaningFulName;
        annotationValue = "\"" + Settings.USER_ENABLED_ANNOTATION + " " + meaningFulName + " " + "element is enabled" + "$" + "\""; //changed the step definition
        textToEnter = "verify" + meaningFulName + "IsEnabled";
        blockToEnter = textToEnter + "(" + ")";
        annotationType = "When";
        Settings.LOGGER.info("Step crated: " + annotationValue + "and function created: " + functionName);

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);
        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
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
                ASTHelper.addStmt(block, new NameExpr(field.getVariableName() + "." + field.getDefaultAction() + "(args"
                        + f.getFormFieldList().indexOf(field) + ")"));
                break;

            case "click":
                ASTHelper.addStmt(block, new NameExpr(field.getVariableName() + "." + field.getDefaultAction() + "()"));
                break;
            default:
                break;
        }

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
        for (String name : res.split("_")
        ) {
            meaningfulName.append(StringUtils.capitalize(name));
        }

        return meaningfulName.toString();
    }

    /**
     * makes the source code of each state persistent
     *
     * @throws IOException
     */
    public static void savePageObjectsOnFileSystem(String directoryBame, String className, CompilationUnit c, boolean stepGeneration) throws IOException {

        String data = "";
        String fileNameToCreate = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
                + File.separator + "java" + File.separator + directoryBame + className;
        // String fileNameToCreate = System.getProperty("user.dir") +poName;
        File f = new File(fileNameToCreate + ".java");

        if (BooleanUtils.isTrue(stepGeneration)) {
            data = StringUtils.replace(c.toString(), "xpath = ", "").toString();
        } else {
            data = c.toString();
        }
        FileUtils.writeStringToFile(f, data);

    }

    public static void setLinkStepDefinitionNavigateBack(CompilationUnit c) {
        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";

        List<Parameter> parameters = new LinkedList<>();
        functionName = "navigateBack";
        annotationType = "When";
        annotationValue = "\"" + Settings.USER_NAVIGATE_BACK + "$" + "\"";
        textToEnter = "backwardNavigation";
        blockToEnter = textToEnter + "(" + ")";
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);

        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkStepDefinitionNavigateForward(CompilationUnit c) {
        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";

        List<Parameter> parameters = new LinkedList<>();
        functionName = "navigateForward";
        annotationType = "When";
        annotationValue = "\"" + Settings.USER_NAVIGATE_FORWARD + "$" + "\"";
        textToEnter = "forwardNavigation";
        blockToEnter = textToEnter + "(" + ")";
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);

        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }

    public static void setLinkStepDefinitionNavigateTo(CompilationUnit c) {
        String functionName = "";
        String annotationValue = "";
        String textToEnter = "";
        String blockToEnter = "";
        String annotationType = "";

        List<Parameter> parameters = new LinkedList<>();
        parameters.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "url"));
        functionName = "navigateTo";
        annotationType = "Given";
        annotationValue = "\"" + Settings.USER_NAVIGATE_TO + "\"";
        textToEnter = "navigateTo";
        blockToEnter = textToEnter + "(url" + ")";
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, functionName);
        method.setParameters(parameters);
        // add a body to the method
        BlockStmt block = new BlockStmt();
        method.setBody(block);
        NormalAnnotationExpr na = new NormalAnnotationExpr();
        na.setName(new NameExpr(annotationType));
        List<MemberValuePair> list_mvp = new LinkedList<MemberValuePair>();
        MemberValuePair mvp = new MemberValuePair();

        List<AnnotationExpr> list_espr = new LinkedList<AnnotationExpr>();
        mvp = new MemberValuePair("xpath", new NameExpr(annotationValue));
        list_mvp.add(mvp);
        na.setPairs(list_mvp);
        list_espr.add(0, na);

        method.setAnnotations(list_espr);

        String firstLetter = Settings.LOCATOR_FILE_NAME.substring(0, 1).toLowerCase();
        String nameOfFile = firstLetter + Settings.LOCATOR_FILE_NAME.substring(1);
        ASTHelper.addStmt(block, new NameExpr(nameOfFile + "." + blockToEnter));
        ASTHelper.addMember(c.getTypes().get(0), method);
        Settings.LOGGER.info(method.toString());
        Settings.LOGGER.info(c.getTypes().get(0).toString());
    }
}
