package com.lspandimpl.server.core.wsfolderchange;

import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.projects.Project;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent;
import com.lspandimpl.server.api.context.BalWorkspaceContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WSFolderChangeHandler {
    
    public static void updateProjects(BalWorkspaceContext context,
                                      DidChangeWorkspaceFoldersParams params) {
        CompilerManager cManager = context.compilerManager();
        WorkspaceFoldersChangeEvent event = params.getEvent();
        List<WorkspaceFolder> modifiedFolders =
                Stream.concat(event.getAdded().stream(),
                event.getRemoved().stream()).collect(Collectors.toList());
        List<Path> reloadedProjects = new ArrayList<>();
        for (WorkspaceFolder folder : modifiedFolders) {
            Path path = CommonUtils.uriToPath(folder.getUri());
            Optional<Path> projectRoot = cManager.getProjectRoot(path);
            if (projectRoot.isEmpty()
                    || reloadedProjects.contains(projectRoot.get())) {
                continue;
            }
            Optional<Project> project =
                    cManager.getProject(projectRoot.get());
            if (project.isEmpty()) {
                continue;
            }
            // Note: This is only for the demonstration purposes and the users can perform other operations other than
            // reloading the projects
            // If folder is added, then reload the project instance
            cManager.reloadProject(projectRoot.get());
            reloadedProjects.add(projectRoot.get());
        }
    }
}
