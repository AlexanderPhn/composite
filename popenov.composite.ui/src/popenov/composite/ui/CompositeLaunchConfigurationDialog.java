package popenov.composite.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationFilteredTree;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupFilter;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.PatternFilter;

import popenov.composite.core.CompositeUtils;
import popenov.composite.core.LaunchElement;

/**
 * Dialog to select launch configuration(s)
 *
 */
public class CompositeLaunchConfigurationDialog extends TitleAreaDialog{

	private Text delayAmountWidget;
    private Text executionNumberWidget;
    
    private String mode;
    private ILaunchGroup launchGroup;
    private boolean openForEditing;
    private boolean waitForTermination;
    private ViewerFilter emptyTypeFilter;
    private IStructuredSelection initialSelection;
    private ISelection selection;
    private int delayAmount;
    private int executionNumber;

	/**
	 * @param parentShell
	 * @param mode
	 * @param configuration
	 * @param forEditing <code>false<code> if add new launch configuration(s) <code>true<code> if edit existing launch configuration
	 */
	public CompositeLaunchConfigurationDialog(Shell parentShell, String mode, ILaunchConfiguration configuration, boolean forEditing) {
		super(parentShell);
		this.mode = mode;
		launchGroup = CompositeUtils.getLaunchGroup(mode);
		openForEditing = forEditing;
		delayAmount = 0;
		executionNumber = 1;
		emptyTypeFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof ILaunchConfigurationType) {
					try {
						ILaunchConfigurationType type = (ILaunchConfigurationType) element;
						if(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type).length == 0) {
							return false;
						} else if(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type).length == 1){
							if (DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type)[0].equals(configuration)){
								return false;
							} else {
								return true;
							}
						} else {
							return true;
						}
					} catch (CoreException e) {
						return false;
					}
				} else if (element instanceof ILaunchConfiguration) {
					return DebugUIPlugin.doLaunchConfigurationFiltering((ILaunchConfiguration)element) && 
							!WorkbenchActivityHelper.filterItem((ILaunchConfiguration)element) &&
							!configuration.equals((ILaunchConfiguration)element);
				} else {
					return false;
				}
				
			}
		};
	}
	
	@Override
	public void create(){
		
		super.create();
		setTitle(openForEditing ?
				CompositeUIMessages.titleMessageForEditDialog:
				CompositeUIMessages.titleMessageForAddDialog);	
		validate();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite area = (Composite) super.createDialogArea(parent);
		
		LaunchConfigurationFilteredTree lTree = new LaunchConfigurationFilteredTree(area, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter(), 
				launchGroup, null);
    	lTree.createViewControl();
    	ViewerFilter[] filters = lTree.getViewer().getFilters();
		for (ViewerFilter viewerFilter : filters) {
			if (viewerFilter instanceof LaunchGroupFilter) {
				lTree.getViewer().removeFilter(viewerFilter);
			}
		}
		lTree.getViewer().addFilter(emptyTypeFilter);
		lTree.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selection = event.getSelection();
				validate();
				
			}
		});
		if (initialSelection != null){
			lTree.getViewer().setSelection(initialSelection, true);
		}
				
		createWaitForTerminationCheckBox(area);	
				
		Composite parametersComposite = new Composite(area, SWT.NONE);
		parametersComposite.setLayout(new GridLayout(2,false));
		parametersComposite.setLayoutData(new GridData(GridData.FILL)); 
		createComboModeWidget(parametersComposite);
		createPreLaunchDelayField(parametersComposite);
        createExecutionNumberField(parametersComposite);
        
        return area;
		
		
	}
	
	protected void validate() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		boolean isValid = true;
		if (getSelectedLaunchConfiguration().length < 1){
			setErrorMessage(CompositeUIMessages.dialogSelectLaunchError);
			isValid = false;
		} else {
			setErrorMessage(null);
		}
		
		if(isValid){
			if (openForEditing){
				if (getSelectedLaunchConfiguration().length > 1){
					setErrorMessage(CompositeUIMessages.dialogEditSelectLaunchError);
					isValid = false;
				}
			}
		}
		
		if (okButton != null){
			okButton.setEnabled(isValid);
		}
	}
	
	@Override
	protected void configureShell(Shell newShell){
		super.configureShell(newShell);
		newShell.setText(openForEditing ?
				CompositeUIMessages.titleForEditDialog:
				CompositeUIMessages.titleForAddDialog);
	}
	
	private void createWaitForTerminationCheckBox(Composite parent) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(CompositeUIMessages.waitForTermination);
		checkBox.addSelectionListener(new SelectionAdapter(){
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				waitForTermination = ((Button) e.widget).getSelection();
			}
		});
		checkBox.setSelection(waitForTermination);

	}

	private void createComboModeWidget(Composite parent){
		Label modeLabel = new Label(parent, SWT.NONE);
		modeLabel.setText(CompositeUIMessages.modeLabelMessage);
		
		Combo comboModeWidget = new Combo(parent, SWT.READ_ONLY);
		comboModeWidget.add(CompositeUIMessages.runMode);
		comboModeWidget.add(CompositeUIMessages.debugMode);
		if (mode.equals(CompositeUIMessages.runMode)){
			comboModeWidget.select(0);
		} else if (mode.equals(CompositeUIMessages.debugMode)){
			comboModeWidget.select(1);
		}
		comboModeWidget.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				mode = ((Combo)e.widget).getText();
			}
		});		
	}
	
	private void createPreLaunchDelayField(Composite parent){
		Label delayAmountLabel = new Label(parent, SWT.NONE);
		delayAmountLabel.setText(CompositeUIMessages.delayLabelMessage);	
		
		delayAmountWidget = new Text(parent, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = convertWidthInCharsToPixels(CompositeUIConstants.DIALOG_TEXT_WIDTH);
		delayAmountWidget.setLayoutData(gridData);
		delayAmountWidget.setText(String.valueOf(delayAmount));		
        delayAmountWidget.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {

            	if (e.character != 0 && e.keyCode != SWT.BS
    					&& e.keyCode != SWT.DEL
    					&& !Character.isDigit(e.character)) {
    				e.doit = false;
    			}
            }
            
        });
        delayAmountWidget.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = ((Text)e.widget).getText();
				try {
					delayAmount = Integer.valueOf(text);
				}
				catch (NumberFormatException exc) {
					delayAmountWidget.setText("0");
				}				
			}
		});
	}
	
	private void createExecutionNumberField(Composite parent){
		Label executionNumberLabel = new Label(parent, SWT.NONE);
		executionNumberLabel.setText(CompositeUIMessages.executionNumberLabelMessage);	
		
		executionNumberWidget = new Text(parent, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = convertWidthInCharsToPixels(CompositeUIConstants.DIALOG_TEXT_WIDTH);
		executionNumberWidget.setLayoutData(gridData);
		executionNumberWidget.setText(String.valueOf(executionNumber));
        executionNumberWidget.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {

            	if (e.character != 0 && e.keyCode != SWT.BS
    					&& e.keyCode != SWT.DEL
    					&& !Character.isDigit(e.character)) {
    				e.doit = false;
    			}
            }
            
        });
        executionNumberWidget.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = ((Text)e.widget).getText();
				try {
					executionNumber = Integer.valueOf(text);
				}
				catch (NumberFormatException exc) {
					executionNumberWidget.setText("1");
				}				
			}
		});
	}

	
	@Override
	protected boolean isResizable() {
	        return true;
	}	
	@Override
	protected Point getInitialSize(){
		return new Point(CompositeUIConstants.DIALOG_WEIGHT, CompositeUIConstants.DIALOG_HEIGHT);
	}
	
	public ILaunchConfiguration[] getSelectedLaunchConfiguration(){
		List<ILaunchConfiguration> configs = new ArrayList<ILaunchConfiguration>(); 
		if (selection != null && !selection.isEmpty()) {
			for (Iterator<?> iter = ((IStructuredSelection)selection).iterator(); iter.hasNext();) {
				Object selection = iter.next();
				if (selection instanceof ILaunchConfiguration) {
					configs.add((ILaunchConfiguration)selection);
				}
			}
		}
		return configs.toArray(new ILaunchConfiguration[configs.size()]);
	}
	
	public String getMode(){
		return mode;
	}
	
	public int getDelayAmount(){
		return delayAmount;
	}
	
	public int getExecutionNumber(){
		return executionNumber;
	}
	
	public boolean getWaitAction(){
		return waitForTermination;
	}
	
	public void setInitialselection(LaunchElement launchElement){
		mode = launchElement.mode;
		delayAmount = launchElement.delay;
		waitForTermination = launchElement.waitForTermination;
		executionNumber = launchElement.executionNumber;
		initialSelection = new StructuredSelection(launchElement.configuration);
		selection = initialSelection;
	}
}
