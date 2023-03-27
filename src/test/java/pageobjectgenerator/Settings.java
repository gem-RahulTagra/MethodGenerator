package pageobjectgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Settings {
//file name settings

	public static String LOCATOR_FILE_NAME;
	// Crawler Settings
	public static final int CRAWL_DEPTH = 0; /* 0 = unlimited. */
	public static final int MAX_STATES = 0; /* 0 = unlimited. */
	public static final int MAX_RUNTIME = 5; /* 5 minutes. */
	public static final int WAIT_TIME_AFTER_EVENT = 500;
	public static final int WAIT_TIME_AFTER_RELOAD = 500;

	//public static final String URL = "http://www.google.com/";

	//public static final String URL = "https://mymis.geminisolutions.com";
	public static final String URL = "C:\\Users\\rahul.tagra\\Desktop\\GradleProject\\src\\test\\resources\\data\\index.html";
	//Used For Logging the steps
	public static final Logger LOGGER= LoggerFactory.getLogger(Settings.class);
	// Output Settings
	public static final String FILESEPARATOR = File.separator;
	public static final String GEN_PO_DIR = "locators" + FILESEPARATOR;
	public static final String IMPLEMENTATION_PO_DIR = "implementation" + FILESEPARATOR;
	public static final String STEP_DEFINITION_PO_DIR = "stepdefinition" + FILESEPARATOR;
	public static final String OUT_DIR = "output" + FILESEPARATOR;
	public static final String DOMS_DIR = OUT_DIR + "doms" + FILESEPARATOR;
	public static final String CLUST_DIR = OUT_DIR + "clusters" + FILESEPARATOR;
	public static final String BROWSER = "FF"; // PH-FF


	// Page Object Generation Settings
	public static boolean CRAWLING = true;
	public static boolean CLUSTERING = false;
	public static boolean REPEAT_STATIC_ANALYSIS = true;
	public static boolean GENERATE_CODE = true;
	public static boolean USE_INPUT_SPECIFICATION = true;
	public static String NUMBER_OF_CLUSTERS = "2";

	public static final String USER_CLICK_ANNOTATION ="^User clicks on";
	public static final String USER_NAVIGATE_FUNCTION ="AndNavigateBack";
	public static final String USER_NAVIGATE_ANNOTATION ="and navigate back";
	public static final String USER_GETTEXT_FUNCTION ="userGetText";
	public static final String USER_GET_ATTRIBUTE_FUNCTION ="userGetsAttribute";
	public static final String USER_VERIFIES_VALUE="userVerifyValueFor";
	public static final String USER_CLEAR_ANNOTATION ="^User clears the";
	public static final String USER_CLEAR_FUNCTION ="userClear";
	public static final String USER_GETTEXT_ANNOTATION ="^User gets the text of";
	public static final String USER_GET_ATTRIBUTE_ANNOTATION ="^User gets " +"\\\"(.*)\\\""+ " attribute of ";
	public static final String USER_INPUT_ANNOTATION ="^User enters "+"\\\"(.*)\\\""+" as";
	public static final String USER_SELECT_FUNCTION ="userSelectFromDropdown";
	public static final String USER_CLICK_FUNCTION ="userClicksOn";
	public static final String USER_CLICKABLE_FUNCTION ="verifyUserIsClickable";
	public static final String USER_CLICKABLE_ANNOTATION ="^User is able to click";
	public static final String USER_DOUBLE_CLICK_FUNCTION ="userDoubleCLickON";
	public static final String USER_DOUBLE_CLICK_ANNOTATION ="^User double click on";
	public static final String USER_ENABLED_FUNCTION ="userIsEnabled";
	public static final String USER_ENABLED_ANNOTATION ="^User verify the given";
	public static final String USER_IMAGE_FUNCTION ="userUploadImage";
	public static final String USER_INPUT_FUNCTION ="userEntersAs";
	public static final String USER_IMAGE_ANNOTATION ="^User uploads image having path \\\"(.*)\\\" for";
	public static final String USER_HOME_PAGE ="openApplication";
	public static final String USER_SCROLL_CLICK_FUNCTION ="userScrollCLickOn";
	public static final String USER_SCROLL_CLICK_ANNOTATION ="^User scroll and clicks on ";

	public static final String USER_HOME_PAGE_ANNOTATION ="^User is on homepage";
	public static final String USER_SELECT_ANNOTATION="^user selects \\\"(.*)\\\" from ";

	public static final String USER_NAVIGATE_BACK ="^User Navigates Back to Previous Page";

	public static final String USER_NAVIGATE_FORWARD ="^User Navigates Forward to Next Page";

	public static final String USER_NAVIGATE_TO ="User navigates to {string}";

	public static String BEFORE_FUNCTION="setDriverInitialisation";

}
