package com.kms.katalon.composer.project.menu;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.constants.IdConstants;
import com.kms.katalon.constants.helper.ConstantsHelper;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.entity.project.ProjectEntity;

@SuppressWarnings("restriction")
public class RecentProjectsMenuContribution implements EventHandler {
    @Inject
    private ECommandService commandService;

    @Inject
    EModelService modelService;
    
    @Inject
    private IEventBroker eventBroker;
    
    private static List<ProjectEntity> recentProjects = new ArrayList<ProjectEntity>();

    @PostConstruct
    public void init() {
        eventBroker.subscribe(EventConstants.WORKSPACE_CREATED, this);
        eventBroker.subscribe(EventConstants.PROJECT_CREATED, this);
        eventBroker.subscribe(EventConstants.PROJECT_OPENED, this);
        eventBroker.subscribe(EventConstants.PROJECT_UPDATED, this);
    }
    
    @AboutToShow
    public void aboutToShow(List<MMenuElement> menuItems) {
        try {
            for (ProjectEntity project : recentProjects) {
                // Add temp command to avoid warning message
                MCommand command = MCommandsFactory.INSTANCE.createCommand();
                command.setCommandName("Temp");

                String labelName = project.getName() + "\t" + getLocationStringLabel(project.getLocation());

                // Create menu item
                MHandledMenuItem recentProjectMenuItem = MMenuFactory.INSTANCE.createHandledMenuItem();
                recentProjectMenuItem.setLabel(labelName);
                recentProjectMenuItem.setContributorURI(ConstantsHelper.getApplicationURI());
                recentProjectMenuItem.setCommand(command);

                // Create parameterized command
                Command recentProjectCommand = commandService.getCommand(IdConstants.OPEN_RECENT_PROJECT_COMMAND_ID);
                List<Parameterization> parameterization = new ArrayList<Parameterization>();
                IParameter param = recentProjectCommand
                        .getParameter(IdConstants.OPEN_RECENT_PROJECT_COMMAND_PARAMETER_ID);
                Parameterization params = new Parameterization(param, project.getId());
                parameterization.add(params);
                ParameterizedCommand parameterizedCommand = new ParameterizedCommand(recentProjectCommand,
                        parameterization.toArray(new Parameterization[parameterization.size()]));
                recentProjectMenuItem.setWbCommand(parameterizedCommand);

                menuItems.add(recentProjectMenuItem);
            }
        } catch (Exception e) {
            LoggerSingleton.getInstance().getLogger().error(e);
        }
    }

    private String getLocationStringLabel(String location) {
        if (location.length() > 60) {
            return location.substring(0, 60) + "...";
        } else {
            return location;
        }
    }

    @Override
    public void handleEvent(Event event) {
        try {
            recentProjects.clear();
            recentProjects.addAll(ProjectController.getInstance().getRecentProjects());
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
    }
    
    
}