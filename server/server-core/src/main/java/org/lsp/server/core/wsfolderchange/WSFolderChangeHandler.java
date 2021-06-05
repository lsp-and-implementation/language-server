package org.lsp.server.core.wsfolderchange;

import io.ballerina.projects.Project;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent;
import org.lsp.server.api.context.BalWorkspaceContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;
import org.lsp.server.core.utils.CommonUtils;

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
            // If folder is added, then reload the project instance
            cManager.reloadProject(projectRoot.get());
            reloadedProjects.add(projectRoot.get());
        }
    }
}
