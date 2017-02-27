package popenov.composite.ui;

import org.eclipse.osgi.util.NLS;

public class CompositeUIMessages extends NLS{
	
	public static String Add;
	public static String Edit;
	public static String Remove;
	public static String Up;
	public static String Down;	
	
	public static String tableColumn1Header;
	public static String tableColumn2Header;
	public static String tableColumn3Header;
	public static String tableColumn4Header;
	public static String tableColumn5Header;
		
	public static String titleForAddDialog;
	public static String titleMessageForAddDialog;
	public static String titleForEditDialog;
	public static String titleMessageForEditDialog;
		
	public static String dialogSelectLaunchError;
	public static String dialogEditSelectLaunchError;	
	
	public static String waitForTermination;
	public static String delayLabelMessage;
	public static String executionNumberLabelMessage;
	public static String modeLabelMessage;
	
	public static String checkedLaunchesError;
	public static String launchDoNotExistError;
	
	public static String cycleErrorTitle;
	public static String cycleErrorMessage;
	
	public static String runMode;
	public static String debugMode;
	
		
	private CompositeUIMessages() {
	}

	static {
        NLS.initializeMessages(CompositeUIMessages.class.getCanonicalName(), CompositeUIMessages.class);
    }

}
