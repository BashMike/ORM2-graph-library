package com.orm2_graph_library.core;

import com.orm2_graph_library.edges.ConstraintRelationEdge;
import com.orm2_graph_library.edges.RoleRelationEdge;
import com.orm2_graph_library.edges.SubtypeRelationEdge;
import com.orm2_graph_library.nodes.common.EntityType;
import com.orm2_graph_library.nodes.common.ObjectType;
import com.orm2_graph_library.nodes.predicates.Role;
import com.orm2_graph_library.nodes.predicates.RoleParticipant;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.stream.Stream;

public class Diagram {
    // ================ ATTRIBUTES ================
    private final ArrayList<DiagramElement> _innerElements = new ArrayList<>();

    protected final ActionManager           _actionManager = new ActionManager();
    protected ArrayList<LogicError>         _logicErrors   = new ArrayList<>();

    // ================ OPERATIONS ================
    // ---------------- attributes ----------------
    public ArrayList<LogicError> logicErrors() { return new ArrayList<>(this._logicErrors); }

    void _addLogicError(@NotNull LogicError logicError) { this._logicErrors.add(logicError); }
    void _removeLogicError(@NotNull LogicError logicError) { this._logicErrors.remove(logicError); }

    // ----------------- contract -----------------
    public <T extends Node> T addNode(T node) {
        this._actionManager.executeAction(new AddNodeAction(this, node));
        return node;
    }

    public void removeNode(Node node) { this._actionManager.executeAction(new RemoveNodeAction(this, node)); }

    public <T extends EntityType, G extends EntityType> SubtypeRelationEdge connectBySubtypeRelation(T begin, G end) {
        SubtypeRelationEdge edge = new SubtypeRelationEdge(begin, end);
        this._actionManager.executeAction(new ConnectBySubtypeRelationAction(this, edge));

        return edge;
    }

    public <T extends Role, G extends RoleParticipant> RoleRelationEdge connectByRoleRelation(T begin, G end) {
        return null;
    }

    public <T extends RoleParticipant, G extends Role> RoleRelationEdge connectByRoleRelation(T begin, G end) {
        return null;
    }

    public void connectByConstraintRelation(Node begin, Node end) {
    }

    public <T extends DiagramElement> Stream<T> getElements(Class<T> elementType) {
        return (Stream<T>)this._innerElements.stream().filter(elem -> elementType.isAssignableFrom(elem.getClass()));
    }

    // Undo & redo state
    public boolean canUndoState() { return this._actionManager.canUndo(); }
    public void undoState()       { this._actionManager.undo(); }
    public boolean canRedoState() { return this._actionManager.canRedo(); }
    public void redoState()       { this._actionManager.redo(); }

    // TODO - @structure :: Add non-public connection to the action manager (object type uses it to disable recording action of setting its name).
    public ActionManager _actionManager() { return this._actionManager; }

    // -------------- sub-operations --------------
    private <T extends DiagramElement> T _addElement(T element) {
        element.setOwner(this);
        this._innerElements.add(element);

        return element;
    }

    private void _removeElement(DiagramElement element) {
        element.unsetOwner();
        this._innerElements.remove(element);
    }

    // ================= SUBTYPES =================
    private class AddNodeAction extends Action {
        private final Node _node;

        public AddNodeAction(Diagram diagram, @NotNull Node node) {
            super(diagram);
            this._node = node;
        }

        @Override
        public void _execute() { this._diagram._addElement(this._node); }
        @Override
        public void _undo() { this._diagram._removeElement(this._node); }
    }

    private class RemoveNodeAction extends Action {
        private final Node            _node;
        private final ArrayList<Edge> _incidentEdges;

        public RemoveNodeAction(Diagram diagram, @NotNull Node node) {
            super(diagram);

            this._node          = node;
            this._incidentEdges = node.getIncidentElements(Edge.class);
        }

        @Override
        public void _execute() {
            this._diagram._removeElement(this._node);
            for (Edge edge : this._incidentEdges) { this._diagram._removeElement(edge); }
        }

        @Override
        public void _undo() {
            this._diagram._addElement(this._node);
            for (var edge : this._incidentEdges) { this._diagram._addElement(edge); }
        }
    }

    private abstract class ConnectAction extends Action {
        protected final Edge _edge;

        public ConnectAction(@NotNull Diagram diagram, @NotNull Edge edge) {
            super(diagram);
            this._edge = edge;
        }

        @Override
        public void _execute() { this._diagram._addElement(this._edge); }
        @Override
        public void _undo() { this._diagram._removeElement(this._edge); }
    }

    private class ConnectBySubtypeRelationAction extends ConnectAction {
        public ConnectBySubtypeRelationAction(@NotNull Diagram diagram, @NotNull SubtypeRelationEdge edge) {
            super(diagram, edge);
        }
    }

    private class ConnectByRoleRelationAction extends ConnectAction {
        public ConnectByRoleRelationAction(@NotNull Diagram diagram, @NotNull RoleRelationEdge edge) {
            super(diagram, edge);
        }
    }

    private class ConnectByConstraintRelationAction extends ConnectAction {
        public ConnectByConstraintRelationAction(@NotNull Diagram diagram, @NotNull ConstraintRelationEdge edge) {
            super(diagram, edge);
        }
    }

    private abstract class DisconnectAction extends Action {
        private final Edge _edge;

        public DisconnectAction(@NotNull Diagram diagram, @NotNull Edge edge) {
            super(diagram);
            this._edge = edge;
        }

        @Override
        public void _execute() { this._diagram._removeElement(this._edge); }
        @Override
        public void _undo() { this._diagram._addElement(this._edge); }
    }

    private class DisconnectSubtypeAction extends DisconnectAction {
        public DisconnectSubtypeAction(@NotNull Diagram diagram, @NotNull SubtypeRelationEdge edge) {
            super(diagram, edge);
        }
    }
}
