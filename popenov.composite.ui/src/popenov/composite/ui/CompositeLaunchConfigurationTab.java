package popenov.composite.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

import popenov.composite.core.CompositeUtils;
import popenov.composite.core.LaunchElement;

/**
 * Composite Tab GUI
 *
 */
public class CompositeLaunchConfigurationTab extends AbstractLaunchConfigurationTab{

	private CheckboxTableViewer launchesTable;
	private String mode;	
	private List<LaunchElement> launchConfigurations = new ArrayList<LaunchElement>();
	private String[] compositeTableColumnHeaders = {
			CompositeUIMessages.tableColumn1Header,
			CompositeUIMessages.tableColumn2Header,
			CompositeUIMessages.tableColumn3Header,
			CompositeUIMessages.tableColumn4Header,
			CompositeUIMessages.tableColumn5Header,
	};
	private int[] compositeTableColumnWidths = {
		CompositeUIConstants.TABLE_COLUMN1_WIDTH,
		CompositeUIConstants.TABLE_COLUMN2_WIDTH,
		CompositeUIConstants.TABLE_COLUMN3_WIDTH,
		CompositeUIConstants.TABLE_COLUMN4_WIDTH,
		CompositeUIConstants.TABLE_COLUMN5_WIDTH,
	};
	private Button compositeAddButton;
	private Button compositeEditButton;
	private Button compositeRemoveButton;
	private Button compositeUpButton;
	private Button compositeDownButton; 
		
	private ILaunchConfiguration currentLaunchConfiguration;
		
	private class CompositeCheckStateProvider implements ICheckStateProvider {

		@Override
		public boolean isChecked(Object element) {			
			if (element instanceof LaunchElement){
				return ((LaunchElement)element).checked;
			}
			return false;
		}

		@Override
		public boolean isGrayed(Object element) {
			return false;
		}
		
	}
		
	private class CompositeLabelProvider extends LabelProvider implements ITableLabelProvider {
	
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0){
				LaunchElement launchElement = (LaunchElement) element;
				if (launchElement.configuration != null){
					return DebugUITools.newDebugModelPresentation().getImage(launchElement.configuration);
				}
				else{
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
					
				}
			}
			return null;
		}
	
		@Override
		public String getColumnText(Object element, int columnIndex) {
			LaunchElement launchElement = (LaunchElement) element; 
			   switch (columnIndex) { 
			   case 0: 
				   try {
						return (launchElement.configuration != null) ? 
								launchElement.configuration.getType().getName() + "::" + launchElement.name : launchElement.name;
					}
					catch (CoreException e) {
						return launchElement.name;
					} 
			   case 1: 
			    return launchElement.mode;
			   case 2:
			    return String.valueOf(launchElement.delay);
			   case 3:
			   	return String.valueOf(launchElement.waitForTermination);
			   case 4:
			   	return String.valueOf(launchElement.executionNumber);
			   }
			return null;
		}
	}
	
	public CompositeLaunchConfigurationTab(String mode){
		this.mode = mode;
	}
	
	@Override
	public void createControl(Composite parent) {
								
		Composite mainComposite = new Composite(parent, SWT.NONE);		
		setControl(mainComposite);
		mainComposite.setLayout(new GridLayout(2,false));
		
		createCheckBoxTableViewer(mainComposite);
		createButtonComposition(mainComposite);
		
	}

	private void createCheckBoxTableViewer(Composite parent) {
		
		
		launchesTable = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		Table table = launchesTable.getTable();
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(parent.getFont());
		for (int i = 0; i < compositeTableColumnHeaders.length; ++i){
			createTableColumn(launchesTable, compositeTableColumnHeaders[i], compositeTableColumnWidths[i]);
		}
		launchesTable.setColumnProperties(compositeTableColumnHeaders);
		launchesTable.setContentProvider(ArrayContentProvider.getInstance());
		launchesTable.setLabelProvider(new CompositeLabelProvider());
		launchesTable.setCheckStateProvider(new CompositeCheckStateProvider());
		launchesTable.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonsEnablement();
				
			}
			
		});
		launchesTable.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (!launchesTable.getSelection().isEmpty()) {
					handleCompositeEditButtonPressed();
				}
				
			}
		});	
		
		launchesTable.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				((LaunchElement)event.getElement()).checked = event.getChecked();
				updateLaunchConfigurationDialog();
				
			}
		});							
		
		launchesTable.setInput(launchConfigurations);	
		
	}
	
	private void createButtonComposition(Composite parent){
		
		Composite buttonsComposite = new Composite(parent, SWT.NONE);
		buttonsComposite.setLayout(new GridLayout());
		GridData layoutData = new GridData(GridData.GRAB_VERTICAL);
		layoutData.verticalAlignment = SWT.BEGINNING;
		buttonsComposite.setLayoutData(layoutData);
				
		compositeAddButton = createCompositePushButton(buttonsComposite, CompositeUIMessages.Add);
		compositeAddButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent event){
				handleCompositeAddButtonPressed();
			}
			
		});
				
		compositeEditButton = createCompositePushButton(buttonsComposite, CompositeUIMessages.Edit);
		compositeEditButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent event){
				handleCompositeEditButtonPressed();
			}
			
		});
		
		compositeRemoveButton = createCompositePushButton(buttonsComposite, CompositeUIMessages.Remove);
		compositeRemoveButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent event){
				handleCompositeRemoveButtonPressed();
			}
			
		});
		
		compositeUpButton = createCompositePushButton(buttonsComposite, CompositeUIMessages.Up);
		compositeUpButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent event){
				handleCompositeUpButtonPressed();
			}
			
		});
		
		compositeDownButton = createCompositePushButton(buttonsComposite, CompositeUIMessages.Down);
		compositeDownButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent event){
				handleCompositeDownButtonPressed();
			}
			
		});
		
		updateButtonsEnablement();		
	}
	
	private void createTableColumn(CheckboxTableViewer viewer, String title, int width) {
		
		TableColumn tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText(title);
		tableColumn.setWidth(width);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		
	}

	private Button createCompositePushButton(Composite parent,String label){
		
		Button pushButton = new Button(parent, SWT.PUSH);
		pushButton.setText(label);
		pushButton.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		pushButton.setLayoutData(data);
		return pushButton;
	}
	
	private void updateButtonsEnablement(){
		
		IStructuredSelection selection = launchesTable.getStructuredSelection();
		int index = launchConfigurations.indexOf((LaunchElement)selection.getFirstElement());
		
		compositeEditButton.setEnabled(selection.size() == 1);
		compositeRemoveButton.setEnabled(selection.size() > 0);		
		compositeUpButton.setEnabled(selection.size() == 1 && index != 0);
		compositeDownButton.setEnabled(selection.size() == 1 && index != launchConfigurations.size() - 1);		
		
	}
		
	private void handleCompositeAddButtonPressed(){
									
		CompositeLaunchConfigurationDialog dialog = new CompositeLaunchConfigurationDialog(getShell(),mode,currentLaunchConfiguration, false);
		dialog.create();
		if (dialog.open() == Window.OK){
			ILaunchConfiguration[] configurations = dialog.getSelectedLaunchConfiguration();
			if (configurations.length < 1){
				return;
			}
			for (ILaunchConfiguration configuration : configurations){
				LaunchElement launchElement = new LaunchElement();
				launchElement.name = configuration.getName();
				launchElement.checked = true;
				launchElement.mode = dialog.getMode();
				launchElement.delay = dialog.getDelayAmount();
				launchElement.waitForTermination = dialog.getWaitAction();
				launchElement.executionNumber = dialog.getExecutionNumber();
				launchElement.configuration = configuration;
				launchConfigurations.add(launchElement);
				StringBuilder cycleElements = CompositeUtils.findCycle(currentLaunchConfiguration, launchConfigurations);
				if (cycleElements.length() != 0){
					IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, cycleElements.toString());
					ErrorDialog.openError(getShell(), CompositeUIMessages.cycleErrorTitle, CompositeUIMessages.cycleErrorMessage, status);
					launchConfigurations.remove(launchElement);
				}				
			}
		}
		
		launchesTable.refresh();
		updateButtonsEnablement();
		updateLaunchConfigurationDialog();
	}
	
	private void handleCompositeEditButtonPressed(){
				
		IStructuredSelection selection = launchesTable.getStructuredSelection();
		LaunchElement launchElement = (LaunchElement) selection.getFirstElement();
		if (launchElement == null){
			return;
		}
		
		CompositeLaunchConfigurationDialog dialog = new CompositeLaunchConfigurationDialog(getShell(),mode,currentLaunchConfiguration, true);
		if (DebugUIPlugin.doLaunchConfigurationFiltering(launchElement.configuration) && 
				!WorkbenchActivityHelper.filterItem(launchElement.configuration)){
			dialog.setInitialselection(launchElement);
		}
		dialog.create();
		if (dialog.open() == Window.OK){
			ILaunchConfiguration[] configurations = dialog.getSelectedLaunchConfiguration();
			if (configurations.length < 0){
				return;
			}	
			launchElement.name = configurations[0].getName();
			launchElement.mode = dialog.getMode();
			launchElement.delay = dialog.getDelayAmount();
			launchElement.waitForTermination = dialog.getWaitAction();
			launchElement.executionNumber = dialog.getExecutionNumber();
			launchElement.configuration = configurations[0];
			StringBuilder cycleElements = CompositeUtils.findCycle(currentLaunchConfiguration, launchConfigurations);
			if (cycleElements.length() != 0){
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, cycleElements.toString());
				ErrorDialog.openError(getShell(), CompositeUIMessages.cycleErrorTitle, CompositeUIMessages.cycleErrorMessage, status);
				launchConfigurations.remove(launchElement);
			}			
			launchesTable.refresh();
			updateButtonsEnablement();
			updateLaunchConfigurationDialog();
		}
		
	}
	
	private void handleCompositeRemoveButtonPressed(){
		
		IStructuredSelection selection = launchesTable.getStructuredSelection();
		for(Iterator<?> i = selection.iterator(); i.hasNext();){
			LaunchElement launchElement = (LaunchElement) i.next();
			launchConfigurations.remove(launchElement);
		}
		launchesTable.refresh(true);
		updateButtonsEnablement();
		updateLaunchConfigurationDialog();
	}
	
	private void handleCompositeUpButtonPressed(){
		
		IStructuredSelection selection = launchesTable.getStructuredSelection();
		int index = launchConfigurations.indexOf((LaunchElement)selection.getFirstElement());
		
		Collections.swap(launchConfigurations,index,index - 1);
		launchesTable.refresh(true);
		updateButtonsEnablement();
		updateLaunchConfigurationDialog();
		
	}
	
	private void handleCompositeDownButtonPressed(){
		
		IStructuredSelection selection = launchesTable.getStructuredSelection();
		int index = launchConfigurations.indexOf((LaunchElement)selection.getFirstElement());
		
		Collections.swap(launchConfigurations,index,index + 1);
		launchesTable.refresh(true);
		updateButtonsEnablement();
		updateLaunchConfigurationDialog();
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
				
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {		
		currentLaunchConfiguration = configuration;
		launchConfigurations = CompositeUtils.loadLaunchElements(configuration,false);
		if (launchesTable != null) {						
			launchesTable.setInput(launchConfigurations);
		}
		
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		currentLaunchConfiguration = configuration.getOriginal();
		CompositeUtils.saveLaunchElements(configuration, launchConfigurations);			
		
	}

	@Override
	public String getName() {		
		return "Composite";
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setMessage(null);
		setErrorMessage(null);
		Object[] elements = launchesTable.getCheckedElements();
		for (LaunchElement launchElement : launchConfigurations){
			if (launchElement.configuration == null){
				setErrorMessage(CompositeUIMessages.launchDoNotExistError);
				return false;
			}
		}
		if (elements.length < 1){
			setErrorMessage(CompositeUIMessages.checkedLaunchesError);
			return false;
		}
		else{
			return true;
		}
	}	
	
}
