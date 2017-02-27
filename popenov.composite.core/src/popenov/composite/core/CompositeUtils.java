package popenov.composite.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;

import popenov.composite.core.CompositeCoreConstants;

/**
 * Composite launch configuration utils
 *
 */
public class CompositeUtils {
	
	/**
	 * Store list of launches elements in composite configuration
	 * 
	 * @param configuration Composite launch configuration
	 * @param input List of launch elements
	 */
	public static void saveLaunchElements(ILaunchConfigurationWorkingCopy configuration, List<LaunchElement> input){
		
		List<String> elementParameters = new ArrayList<String>();
		
		for (LaunchElement launchElement : input) {
			
			elementParameters.add(launchElement.name);
			elementParameters.add(String.valueOf(launchElement.checked));
			elementParameters.add(launchElement.mode);
			elementParameters.add(String.valueOf(launchElement.delay));
			elementParameters.add(String.valueOf(launchElement.waitForTermination));
			elementParameters.add(String.valueOf(launchElement.executionNumber));			
			
		}
		
		configuration.setAttribute(CompositeCoreConstants.COMPOSITE_ATTRIBUTE_NAME, elementParameters);
		
	}
	
	/**
	 * Load list of launch elements of composite configuration
	 * 
	 * @param configuration Composite launch configuration
	 * @param getOnlyChecked if <code>true<code> return only checked launch elements, if <code>false<code> return all launch elements
	 * @return List of launch elements
	 */
	public static List<LaunchElement> loadLaunchElements(ILaunchConfiguration configuration, boolean getOnlyChecked){
		
		List<LaunchElement> result = new ArrayList<LaunchElement>();
		
		List<String> elementParameters = new ArrayList<>();
		try {
			elementParameters = configuration.getAttribute(CompositeCoreConstants.COMPOSITE_ATTRIBUTE_NAME, new ArrayList<>());
		} catch (CoreException e) {
			DebugPlugin.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Error reading configuration",e));			
			return result;			
		}
		
		for(int i = 0; i < elementParameters.size() - 1; i = i + 6){
			
			LaunchElement launchElement = new LaunchElement();
			
			launchElement.name = elementParameters.get(i);
			launchElement.checked = Boolean.parseBoolean(elementParameters.get(i+1));			
			launchElement.mode = elementParameters.get(i+2);
			launchElement.delay = Integer.parseInt(elementParameters.get(i+3));
			launchElement.waitForTermination = Boolean.parseBoolean(elementParameters.get(i+4));
			launchElement.executionNumber = Integer.parseInt(elementParameters.get(i+5));
			try {
				launchElement.configuration = findLaunch(launchElement.name);
			} catch (Exception e) {
				DebugPlugin.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Error finding launch configuration " + launchElement.name,e));
				launchElement.configuration = null;
			}
			
			if (launchElement.checked && getOnlyChecked){
				result.add(launchElement);
			} else if(!getOnlyChecked){
				result.add(launchElement);
			}
		}
		
		return result;
	}

	/**
	 * Find launch configuration by name
	 * 
	 * @param name 
	 * @return launch configuration or <code>null<code> if cannot find launch configuration by given name
	 * @throws CoreException
	 */
	public static ILaunchConfiguration findLaunch(String name) throws CoreException {
		ILaunchConfiguration[] launchConfigurations = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
		for (ILaunchConfiguration lConf : launchConfigurations) {			
			if (lConf.getName().equals(name)) return lConf;
		}
		return null;
	}

	/**
	 * Get specified launch group
	 * 
	 * @param launchMode Configuration launch mode
	 * @return Launch group
	 */
	public static ILaunchGroup getLaunchGroup(String launchMode){
		ILaunchGroup[] launchGroups = DebugUITools.getLaunchGroups();		
		for (ILaunchGroup launchGroup : launchGroups) {
			if (launchGroup.getMode().equals(launchMode) && launchGroup.getIdentifier().equals(CompositeCoreConstants.LAUNCH_GROUP_IDENTIFIER + launchMode)) {
				return launchGroup;
			}
		}
		return null;
	}
	
	/**
	 * Cycle search in composite configuration 
	 * 
	 * @param currentConfiguration Reference to the current composite configuration
	 * @param configurations List of launch element of given configuration
	 * @return list with cycle path if found cycle, empty list if no cycle
	 */
	public static StringBuilder findCycle(ILaunchConfiguration currentConfiguration, List<LaunchElement> configurations) {
		boolean cyclicConfiguration = false;				
		List<String> cyclePath = new ArrayList<>();
		StringBuilder cycleElements = new StringBuilder();
		List<String> visitedConfigurations = new ArrayList<>();		
		visitedConfigurations.add(currentConfiguration.getName());
		try {
			cyclicConfiguration = dfsCycle(configurations,visitedConfigurations,cyclePath,currentConfiguration.getType(),cyclicConfiguration);
		} catch (CoreException e) {
			DebugPlugin.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Error getting configuration type",e));	
			return cycleElements;
		}		
		if (cyclicConfiguration){
			cycleElements.append(currentConfiguration.getName());
			cycleElements.append(" -> \n");	
			for (int i = cyclePath.size() - 1; i >= 0; --i){
				cycleElements.append(cyclePath.get(i));
				cycleElements.append(" -> \n");						
			}						
		}
		return cycleElements;
		
	}

	/**
	 * Depth-first search in composite configuration dependency graph
	 * 
	 * @param configurations 
	 * @param visitedConfigurations List of visited configuration during dfs
	 * @param cyclePath List of cycle path
	 * @param compositeType Composite configuration type
	 * @param cyclicConfiguration <code>true<code> if found cycle, <code>false<code> if no cycle
	 * @return <code>true<code> if found cycle, <code>false<code> if no cycle
	 * @throws CoreException
	 */
	private static boolean dfsCycle(List<LaunchElement> configurations, List<String> visitedConfigurations, List<String> cyclePath, 
			ILaunchConfigurationType compositeType, boolean cyclicConfiguration) {
	
		for(LaunchElement launchElement : configurations){
			try {
				if (launchElement.configuration.getType().equals(compositeType)){
					if (visitedConfigurations.contains(launchElement.name)){
						cyclePath.add(launchElement.name);
						return true;
						
					}				
					visitedConfigurations.add(launchElement.name);
					cyclicConfiguration = dfsCycle(loadLaunchElements(launchElement.configuration, false),visitedConfigurations,cyclePath,compositeType,cyclicConfiguration);
					if (cyclicConfiguration){
						cyclePath.add(launchElement.name);
						return cyclicConfiguration;
					}				
					visitedConfigurations.remove(launchElement.name);
					return cyclicConfiguration;
				}
			} catch (CoreException e) {
				DebugPlugin.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Error getting configuration type",e));
				return cyclicConfiguration;
			}
		}
		return cyclicConfiguration;
	}
	
	/**
	 * All configurations calculation (include repeating)
	 * 
	 * @param launchConfigurations List of launch element 
	 * @return Configurations count
	 */
	public static int configurationCount(List<LaunchElement> launchConfigurations){
		int count = 0;
		for (LaunchElement launchElement : launchConfigurations){
			count += launchElement.executionNumber;
		}
		return count;
	}
}