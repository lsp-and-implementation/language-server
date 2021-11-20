package com.lspandimpl.server.core.fileevents;

import com.lspandimpl.server.core.utils.CommonUtils;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.CreateFileOptions;
import org.eclipse.lsp4j.CreateFilesParams;
import org.eclipse.lsp4j.FileCreate;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Collections;
import java.util.Optional;

/**
 * File operation events such as willCreate, didCreate, willDelete, didDelete, willRename, didRename
 * are handled here.
 * In this original implementation only the file creation events have been handled and the same concept
 * can be extended for the other events as well.
 */
public class FileOperationEventsHandler {
    private static final String BALLERINA_TOML = "Ballerina.toml";

    private FileOperationEventsHandler() {
    }

    public static WorkspaceEdit willCreate(CreateFilesParams params) {
        Optional<FileCreate> tomlCreation = params.getFiles().stream()
                .filter(fileCreate -> fileCreate.getUri().endsWith(BALLERINA_TOML))
                .findAny();
        if (tomlCreation.isEmpty()) {
            return null;
        }

        CreateFile readmeOperation = new CreateFile();
        CreateFileOptions options = new CreateFileOptions();
        options.setIgnoreIfExists(true);
        String readmeUri = tomlCreation.get().getUri().replace(BALLERINA_TOML, "README.md");
        readmeOperation.setUri(readmeUri);
        readmeOperation.setOptions(options);
        readmeOperation.setKind(ResourceOperationKind.Create);

        WorkspaceEdit wsEdit = new WorkspaceEdit();
        wsEdit.setDocumentChanges(Collections.singletonList(Either.forRight(readmeOperation)));

        return wsEdit;
    }

    public static void didCreate(CreateFilesParams params, LanguageClient client) {
        Optional<FileCreate> tomlCreation = params.getFiles().stream()
                .filter(fileCreate -> fileCreate.getUri().endsWith(BALLERINA_TOML))
                .findAny();
        if (tomlCreation.isEmpty()) {
            return;
        }

        WorkspaceEdit wsEdit = new WorkspaceEdit();
        TextDocumentEdit defaultContent = new TextDocumentEdit();

        TextEdit edit = new TextEdit();
        edit.setNewText("[build-options]"
                + CommonUtils.LINE_SEPARATOR
                + "observabilityIncluded = true");
        edit.setRange(new Range(new Position(0, 0), new Position(0, 0)));

        VersionedTextDocumentIdentifier documentIdentifier = new VersionedTextDocumentIdentifier();
        documentIdentifier.setUri(tomlCreation.get().getUri());
        documentIdentifier.setVersion(1);
        
        defaultContent.setEdits(Collections.singletonList(edit));
        defaultContent.setTextDocument(documentIdentifier);
        
        wsEdit.setDocumentChanges(Collections.singletonList(Either.forLeft(defaultContent)));

        client.applyEdit(new ApplyWorkspaceEditParams(wsEdit));
    }
}
