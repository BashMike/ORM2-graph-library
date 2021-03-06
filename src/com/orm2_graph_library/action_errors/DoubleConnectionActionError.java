package com.orm2_graph_library.action_errors;

import com.orm2_graph_library.core.ActionError;
import com.orm2_graph_library.core.DiagramElement;
import com.orm2_graph_library.core.Edge;
import org.jetbrains.annotations.NotNull;

public class DoubleConnectionActionError<T extends DiagramElement, G extends DiagramElement, F extends Edge> extends ActionError {
    // ================ ATTRIBUTES ================
    private final T _beginDiagramElement;
    private final G _endDiagramElement;
    private final F _existEdge;

    // ================ OPERATIONS ================
    // ----------------- creating -----------------
    public DoubleConnectionActionError(@NotNull T beginDiagramElement, @NotNull G endDiagramElement, @NotNull F existEdge) {
        this._beginDiagramElement = beginDiagramElement;
        this._endDiagramElement   = endDiagramElement;
        this._existEdge           = existEdge;
    }

    // ---------------- attributes ----------------
    public T beginDiagramElement() { return this._beginDiagramElement; }
    public G endDiagramElement()   { return this._endDiagramElement; }
    public F existEdge()           { return this._existEdge; }

    // ----------------- contract -----------------
    @Override
    public String description() { return "Double connection between \"" + this._beginDiagramElement + "\" and \"" + this._endDiagramElement + "\"" + "."; }

    // ---------------- comparison ----------------
    @Override
    public boolean equals(Object other) {
        if (other instanceof DoubleConnectionActionError) {
            DoubleConnectionActionError<T, G, F> otherConverted = (DoubleConnectionActionError<T, G, F>)other;

            return (this._beginDiagramElement == otherConverted._beginDiagramElement &&
                    this._endDiagramElement   == otherConverted._endDiagramElement &&
                    this._existEdge           == otherConverted._existEdge);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() { return this._beginDiagramElement.hashCode() * this._endDiagramElement.hashCode() * this._existEdge.hashCode(); }
}
