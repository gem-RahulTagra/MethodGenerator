
package pageobjectgenerator;

import annotation.LocatorType;
import japa.parser.ast.CompilationUnit;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serializer.LocatorSerializer;
import utils.UtilsMethodCodeGenerator;
import utils.UtilsStepDefinitionCodeGenerator;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class CodeGeneratorRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeGeneratorRunner.class);

    public static void main(String[] args) {

        ClassLoader classLoader = PageMethodGenerator.class.getClassLoader();
        for (int i = 0; i < args.length; i++) {

            Settings.LOCATOR_FILE_NAME = args[i];

            try {
                Class aClass = classLoader.loadClass("locators" + "." + Settings.LOCATOR_FILE_NAME);
                generatePageMethods(aClass.getDeclaredFields(), aClass);
                Thread.sleep(2000);
                generateStepMethods(aClass.getDeclaredFields(), aClass);

            } catch (ClassNotFoundException e) {
                throw new ArithmeticException();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void generatePageMethods(Field[] fields, Class aClass) throws IOException {

        String locator;
        String locatorType;
        boolean isByOrFindBy = false;
        CompilationUnit c = UtilsMethodCodeGenerator.createEnhancedCompilationUnit("implementation", "Method");
        UtilsMethodCodeGenerator.setTypeDeclaration(c, aClass.getSimpleName() + "Implementation");
        UtilsMethodCodeGenerator.setWebDriverVariable(c);


        for (Field field : fields) {
            locator = " ";
            locatorType = " ";
            LOGGER.info(field.getName());
            Annotation[] ann = field.getDeclaredAnnotations();
            isByOrFindBy = false;
            for (Annotation an : ann) {

                if (an instanceof FindBy) {
                    isByOrFindBy = true;
                    FindBy findBY = (FindBy) an;
                    if (!findBY.xpath().isEmpty()) {
                        locator = findBY.xpath();
                        LOGGER.info(findBY.xpath());

                    } else if (!findBY.id().isEmpty()) {
                        locator = findBY.id();
                        LOGGER.info(findBY.id());

                    } else if (!findBY.className().isEmpty()) {
                        locator = findBY.className();
                        LOGGER.info(findBY.className());

                    } else if (!findBY.name().isEmpty()) {
                        locator = findBY.name();
                        LOGGER.info(findBY.name());

                    } else if (!findBY.linkText().isEmpty()) {
                        locator = findBY.linkText();
                        LOGGER.info(findBY.linkText());

                    } else if (!findBY.partialLinkText().isEmpty()) {
                        locator = findBY.partialLinkText();
                        LOGGER.info(findBY.partialLinkText());

                    } else if (!findBY.tagName().isEmpty()) {
                        locator = findBY.tagName();
                        LOGGER.info(findBY.xpath());

                    } else {
                        locator = findBY.css();
                        LOGGER.info(findBY.xpath());
                    }
                } else {
                    if (field.isAnnotationPresent(LocatorType.class)) {
                        locatorType = LocatorSerializer.getSerializedKey(field);
                    }

                }

            }
// All input functions are covered for Gemjar reporting
            if (StringUtils.contains(locatorType, "input")) {
                UtilsMethodCodeGenerator.setLinkMethodsTypeSetter(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsTypeGetter(c, field);
                UtilsMethodCodeGenerator.setLinkMethodForEnabled(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsClear(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsAttributeGetter(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsValueVerification(c, field);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "button")) {
                UtilsMethodCodeGenerator.setLinkMethodsClick(c, field);// Radio and CheckBox and Normal Click Operation
                UtilsMethodCodeGenerator.setMethodClickable(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsAttributeGetter(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsValueVerification(c, field);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "click")) {
                UtilsMethodCodeGenerator.setLinkMethodsDoubleClick(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsClick(c, field);// Radio and CheckBox and Normal Click Operation
                UtilsMethodCodeGenerator.setMethodClickable(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsTypeGetter(c, field);
                UtilsMethodCodeGenerator.setMethodScrollClick(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsAttributeGetter(c, field);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "a")) {
                UtilsMethodCodeGenerator.setLinkMethodsClick(c, field);// Radio and CheckBox and Normal Click Operation
                UtilsMethodCodeGenerator.setMethodClickable(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsClickAndNavigateBack(c, field);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "dropdown")) {
                UtilsMethodCodeGenerator.setLinkMethodsDropDown(c, field);
                UtilsMethodCodeGenerator.setMethodClickable(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsTypeGetter(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsAttributeGetter(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsValueVerification(c, field);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "image") || StringUtils.equalsIgnoreCase(locatorType, "file")) {
                UtilsMethodCodeGenerator.setLinkMethodsClick(c, field);// Radio and CheckBox and Normal Click Operation
                UtilsMethodCodeGenerator.setMethodClickable(c, field);
                UtilsMethodCodeGenerator.setLinkMethodForEnabled(c, field);
                UtilsMethodCodeGenerator.setLinkMethodForUpload(c, field);
                UtilsMethodCodeGenerator.setLinkMethodForIsSelected(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsAttributeGetter(c, field);
                UtilsMethodCodeGenerator.setLinkMethodsValueVerification(c, field);
            }
            if (!StringUtils.equalsIgnoreCase(field.getName(), "driver")) {
                UtilsMethodCodeGenerator.setLinkMethodForVisibility(c, field);
                UtilsMethodCodeGenerator.setLinkMethodForText(c, field);
            }
        }
        UtilsMethodCodeGenerator.setLinkMethodsGetUrl(c);
        UtilsMethodCodeGenerator.setLinkMethodsVerifyUrl(c);
        UtilsMethodCodeGenerator.setLinkMethodsNavigateTo(c);
        UtilsMethodCodeGenerator.setLinkMethodsNavigateForward(c);
        UtilsMethodCodeGenerator.setLinkMethodsNavigateBack(c);
        UtilsMethodCodeGenerator.setLinkMethodsOpenHomePage(c);
        UtilsMethodCodeGenerator.savePageObjectsOnFileSystem(Settings.IMPLEMENTATION_PO_DIR,
                aClass.getSimpleName() + "Implementation", c, false);

    }

    public static void generateStepMethods(Field[] fields, Class aClass) throws IOException {
        CompilationUnit c = UtilsStepDefinitionCodeGenerator.createEnhancedCompilationUnit("stepdefinition", "StepDefinition");
        UtilsStepDefinitionCodeGenerator.setTypeDeclaration(c, aClass.getSimpleName() + "StepDefinition");

        UtilsStepDefinitionCodeGenerator.setStepDefinitionVariable(c, Settings.LOCATOR_FILE_NAME + "Implementation");
        String locator;
        String locatorType;

        //setting before class for driver initialisation
//        UtilsStepDefinitionCodeGenerator.setBeforeClass(c);

        for (Field field : fields) {
            locator = " ";
            locatorType = " ";
            Settings.LOGGER.info(field.getName());

            Annotation[] ann = field.getDeclaredAnnotations();

            for (Annotation an : ann) {
                if (an instanceof FindBy) {
                    FindBy findBY = (FindBy) an;
                    if (!findBY.xpath().isEmpty()) {
                        locator = findBY.xpath();
                        Settings.LOGGER.info(findBY.xpath());

                    } else if (!findBY.id().isEmpty()) {
                        locator = findBY.id();
                        Settings.LOGGER.info(findBY.id());

                    } else if (!findBY.className().isEmpty()) {
                        locator = findBY.className();
                        Settings.LOGGER.info(findBY.className());

                    } else if (!findBY.name().isEmpty()) {
                        locator = findBY.name();
                        Settings.LOGGER.info(findBY.name());

                    } else if (!findBY.linkText().isEmpty()) {
                        locator = findBY.linkText();
                        Settings.LOGGER.info(findBY.linkText());

                    } else if (!findBY.partialLinkText().isEmpty()) {
                        locator = findBY.partialLinkText();
                        Settings.LOGGER.info(findBY.partialLinkText());

                    } else if (!findBY.tagName().isEmpty()) {
                        locator = findBY.tagName();
                        Settings.LOGGER.info(findBY.xpath());

                    } else {
                        locator = findBY.css();
                        Settings.LOGGER.info(findBY.xpath());
                    }
                } else {
                    if (field.isAnnotationPresent(LocatorType.class)) {
                        locatorType = LocatorSerializer.getSerializedKey(field);
                    }

                }


            }

            if (StringUtils.equalsIgnoreCase(locatorType, "button")) {
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenClickable(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, true);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, true);
                UtilsStepDefinitionCodeGenerator.setClickStepDefinitionMethod(c, field, locatorType);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "input")) {
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionClear(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionTextGetter(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethod(c, field, locatorType);// Radio and CheckBox and Normal Click Operation
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, true);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenEnabled(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, true);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "click")) {
                UtilsStepDefinitionCodeGenerator.setClickStepDefinitionMethod(c, field, locatorType);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionDoubleCLick(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionTextGetter(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenClickable(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethod(c, field, locatorType);// Radio and CheckBox and Normal Click Operation
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, true);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, false);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "image")) {
                UtilsStepDefinitionCodeGenerator.setClickStepDefinitionMethod(c, field, locatorType);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenClickable(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenEnabled(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethod(c, field, locatorType);// Radio and CheckBox and Normal Click Operation
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, true);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenSelected(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, true);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "a")) {
                UtilsStepDefinitionCodeGenerator.setClickStepDefinitionMethod(c, field, locatorType);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenClickable(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethod(c, field, locatorType);// Radio and CheckBox and Normal Click Operation
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, true);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, false);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "dropdown")) {
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenClickable(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionTextGetter(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethod(c, field, locatorType);// Radio and CheckBox and Normal Click Operation
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, true);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, true);
            }

        }
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionGetUrl(c);
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionVerifyUrl(c);
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionNavigateTo(c);
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionNavigateForward(c);
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionNavigateBack(c);
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodGiven(c, "homePage");
        UtilsStepDefinitionCodeGenerator.savePageObjectsOnFileSystem(Settings.STEP_DEFINITION_PO_DIR,
                aClass.getSimpleName() + "StepDefinition", c, true);
    }
}
