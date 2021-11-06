package com.lspandimpl.server.api.context;

import org.eclipse.lsp4j.Position;

import java.util.List;

public interface BalSelectionRangeContext extends BalTextDocumentContext {
    List<Position> positions();
}
