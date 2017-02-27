# Composite Launch Configuration

Eclipse plug-in. The user can launch multiple applications at the same time or in sequential order by using composite launch configuration

## Composite tab

The Launches tab lets you add and delete launch configuration sets to the composite launch. It also allows you to temporarily disable, re-order and edit properties of the elements in the composite. 

![N|Solid](https://github.com/AlexanderPhn/composite/blob/master/composite1.png?raw=true)

| Component | Description | 
| --- | --- |
| Name | Displays the name of the launch configuration and a checkbox for enabling or disabling the configuration set |
| Mode | Displays the mode the configuration set will run in when the composite is launched |
| Delay | Display prelaunch delay - specified number of seconds to wait before launching configuration set|
| Wait Termination | Display necessary to wait until current launch configuration set is terminated |
| Execution | Display execution number of the given launch configuration set |
| Add... | Opens a dialog to add a new configuration |
| Edit... | Opens a dialog to edit values for configuration(s) |
| Remove | Removes selected configuration(s) from the list |
| Up | Move selected configuration set up |
| Down | Move selected configuration set down |

### Add and Edit launch configuration Dialog

![N|Solid](https://github.com/AlexanderPhn/composite/blob/master/composite2.png?raw=true)

| Component | Description | 
| --- | --- |
|Filter input | Type in filter input to filter list of configurations by name|
| Configurations tree | Lists all available launch configurations, filtered by Filter input |
| Wait for termination | Checkbox for setting wait termination option |
| Launch mode | Combobox for selecting launch mode |
| Pre launch delay seconds | Text field for setting delay |
| Number of executions | Text field for setting number of executions |
