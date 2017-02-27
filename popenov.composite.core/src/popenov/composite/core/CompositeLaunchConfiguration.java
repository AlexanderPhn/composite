package popenov.composite.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

public class CompositeLaunchConfiguration implements ILaunchConfigurationDelegate  {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		List<LaunchElement> launchConfigurations = CompositeUtils.loadLaunchElements(configuration, true);
		List<ILaunch> processes = new ArrayList<>();
		SubMonitor subMonitor = SubMonitor.convert(monitor, CompositeUtils.configurationCount(launchConfigurations));
		try{
			if (launchConfigurations.isEmpty()){
				DebugPlugin.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, IStatus.WARNING, 
						"Warning empty launch configuration:" + configuration.getName() ,null));				
			}
			for (LaunchElement launchElement : launchConfigurations){
				if (launchElement.configuration == null){
					DebugPlugin.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, 
							"Error cannot find launch configuration " + launchElement.name,null));
					continue;
				}
				if (!launchElement.configuration.supportsMode(launchElement.mode)){					
					DebugPlugin.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, 
							"Error cannot launch " + launchElement.name + " in the " + launchElement.mode + " mode",null));
					continue;
				}
				if (launchElement.delay > 0){
					try {
						Thread.sleep(launchElement.delay * 1000);
					} catch (InterruptedException e) {						
					}
				}
				for(int i = 0; i < launchElement.executionNumber; i++){
					ILaunch currentLaunch = launchElement.configuration.launch(launchElement.mode, subMonitor.newChild(1));
					for (IDebugTarget debugTarget : currentLaunch.getDebugTargets()) {
						launch.addDebugTarget(debugTarget);
					}
					for(IProcess process : currentLaunch.getProcesses()){
						launch.addProcess(process);
					}
					processes.add(currentLaunch);
				}
				if (launchElement.waitForTermination){
					waitForSetTermination(processes);
				}
				processes.clear();
			
			}
		} finally {
			monitor.done();
		}
		
	}

	private void waitForSetTermination(List<ILaunch> processes) {
		for(ILaunch launch: processes){
			waiForTermination(launch);
		}
		
	}

	private void waiForTermination(ILaunch launch) {
		while(!launch.isTerminated()){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				
			}
		}
		
	}	
	
}
