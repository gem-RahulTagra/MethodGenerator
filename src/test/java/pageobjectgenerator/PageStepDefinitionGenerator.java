package pageobjectgenerator;

import annotation.LocatorType;
import japa.parser.ast.CompilationUnit;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.support.FindBy;
import serializer.LocatorSerializer;
import utils.UtilsStepDefinitionCodeGenerator;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class PageStepDefinitionGenerator {

    public static void main(String[] args) {

        ClassLoader classLoader = PageStepDefinitionGenerator.class.getClassLoader();
        Class aClass;
        for (int i = 0; i < args.length; i++) {

            Settings.LOCATOR_FILE_NAME = args[i];

            try {
                aClass = classLoader.loadClass("locators" + "." + Settings.LOCATOR_FILE_NAME);
                generateStepMethods(aClass.getDeclaredFields(), aClass);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
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
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethod(c, field, locatorType);// Radio and CheckBox and Normal Click Operation
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenClickable(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, true);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, true);
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
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionDoubleCLick(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionTextGetter(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenClickable(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethod(c, field, locatorType);// Radio and CheckBox and Normal Click Operation
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, true);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, false);
            }
            if (StringUtils.equalsIgnoreCase(locatorType, "image")) {
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
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenClickable(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethod(c, field, locatorType);// Radio and CheckBox and Normal Click Operation

            }
            if (StringUtils.equalsIgnoreCase(locatorType, "dropdown")) {
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenClickable(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionTextGetter(c, field);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethod(c, field, locatorType);// Radio and CheckBox and Normal Click Operation
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodThenVisibility(c, field, true);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, false);
                UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionAttributeGetter(c, field, true);
            }

        }
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionNavigateTo(c);
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionNavigateForward(c);
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionNavigateBack(c);
        UtilsStepDefinitionCodeGenerator.setLinkStepDefinitionMethodGiven(c, "homePage");
        UtilsStepDefinitionCodeGenerator.savePageObjectsOnFileSystem(Settings.STEP_DEFINITION_PO_DIR,
                aClass.getSimpleName() + "StepDefinition", c, true);
    }
}
