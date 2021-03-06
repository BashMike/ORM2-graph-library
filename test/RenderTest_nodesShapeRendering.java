import com.orm2_graph_library.anchor_points.AnchorPosition;
import com.orm2_graph_library.core.Diagram;
import com.orm2_graph_library.core.DiagramElement;
import com.orm2_graph_library.core.Edge;
import com.orm2_graph_library.core.Node;
import com.orm2_graph_library.edges.SubtypingRelationEdge;
import com.orm2_graph_library.nodes.common.EntityType;
import com.orm2_graph_library.nodes.common.ObjectType;
import com.orm2_graph_library.nodes.constraints.Constraint;
import com.orm2_graph_library.nodes.constraints.SubsetConstraint;
import com.orm2_graph_library.nodes.predicates.ObjectifiedPredicate;
import com.orm2_graph_library.nodes.predicates.Predicate;
import com.orm2_graph_library.nodes.predicates.Role;
import com.orm2_graph_library.nodes.predicates.RolesSequence;
import com.orm2_graph_library.utils.Point2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class RenderTest_nodesShapeRendering extends JFrame implements MouseListener {
    // ================== STATIC ==================
    static Dimension _objectTypeSize                           = new Dimension(100, 50);

    static int       _constraintSize                           = 35;

    static Dimension _objectifiedPredicateGapsDistances        = new Dimension(20, 20);
    static int       _objectifiedPredicateBorderRoundingDegree = 15;

    static Dimension _roleSize                                 = new Dimension(20, 10);
    static Class     _nodeTypeToPlace                          = EntityType.class;

    // ================ OPERATIONS ================
    // ----------------- creating -----------------
    static public void main(String[] args) {
        SwingUtilities.invokeLater(RenderTest_nodesShapeRendering::new);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        System.out.println(me.getPoint());
    }

    @Override public void mousePressed(MouseEvent me) {
        System.out.println(me.getPoint());
    }

    @Override public void mouseReleased(MouseEvent me) {}
    @Override public void mouseEntered(MouseEvent me) {}
    @Override public void mouseExited(MouseEvent me) {}

    public RenderTest_nodesShapeRendering() {
        // Set JFrame settings
        this.setTitle("Nodes...");
        this.setSize(new Dimension(640, 480));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setBackground(Color.BLACK);
        this.getContentPane().setBackground(Color.BLACK);

        // Render nodes
        Diagram diagram = this._createDiagram();
        DiagramElementsPanel panel = new DiagramElementsPanel(diagram, DiagramElementsPanel.RenderMode.WIREFRAME);

        this.getContentPane().add(panel);
        panel.setFocusable(false);
        panel.setFocusable(true);
        panel.requestFocus(true);
        panel.requestFocusInWindow();

        this.toFront();
        this.requestFocusInWindow();

        System.out.println(panel.hasFocus());
        System.out.println(this.hasFocus());
        System.out.println(this.getFocusOwner());
        System.out.println(this.getMostRecentFocusOwner());
    }

    private Diagram _createDiagram() {
        Diagram diagram = new Diagram();

        // Create nodes
        Predicate sp0 = diagram.addNode(new Predicate(4));
        Predicate sp1 = diagram.addNode(new Predicate(3));
        ObjectifiedPredicate op0 = diagram.addNode(new ObjectifiedPredicate(sp1));

        EntityType e0 = diagram.addNode(new EntityType());
        EntityType e1 = diagram.addNode(new EntityType());
        EntityType e2 = diagram.addNode(new EntityType());

        Constraint c0 = diagram.addNode(new SubsetConstraint());

        this._initDiagramElementsSizes(diagram);

        // Modify nodes
        sp0.moveTo(new Point2D(197, 197));
        sp0.setOrientation(DiagramElement.Orientation.VERTICAL);
        sp1.moveTo(new Point2D(20, 20));

        e0.moveTo(new Point2D(350, 150));
        e1.moveTo(new Point2D(450, 250));
        e2.moveTo(new Point2D(200, 320));

        c0.moveTo(new Point2D(80, 80));

        op0.moveTo(new Point2D(200, 200));
        op0.innerPredicate().setOrientation(DiagramElement.Orientation.VERTICAL);

        // Connect nodes
        SubtypingRelationEdge se0 = diagram.connectBySubtypingRelation(e0.centerAnchorPoint(), e1.centerAnchorPoint());
        diagram.connectByRoleConstraintRelation(c0.centerAnchorPoint(), sp0.rolesSequence(0, 3));
        diagram.connectByRoleRelation(sp0.getRole(2).anchorPoint(AnchorPosition.LEFT),  op0.rightAnchorPoint());
        // diagram.connectByRoleRelation(sp1.getRole(2).anchorPoint(AnchorPosition.DOWN), op0.leftAnchorPoint());

        diagram.reconnectSubtypingRelation(e2.centerAnchorPoint(), se0);

        // Modify diagram elements
        c0.moveBy(300, 100);
        e1.moveTo(new Point2D(450, 250));

        return diagram;
    }

    private void _initDiagramElementsSizes(Diagram diagram) {
        for (DiagramElement de : diagram.getElements(DiagramElement.class).collect(Collectors.toCollection(ArrayList::new))) {
            if (de instanceof ObjectType) {
                ((ObjectType)de).setBorderSize(_objectTypeSize.width, _objectTypeSize.height);
            }
            else if (de instanceof Constraint) {
                ((Constraint)de).setBorderSize(_constraintSize);
            }
            else if (de instanceof Predicate) {
                ((Predicate)de).setRolesBorderSize(_roleSize.width, _roleSize.height);
            }
            else if (de instanceof ObjectifiedPredicate) {
                ((ObjectifiedPredicate)de).setGapsDistances(_objectifiedPredicateGapsDistances.width, _objectifiedPredicateGapsDistances.height);
                ((ObjectifiedPredicate)de).setBorderRoundingDegree(_objectifiedPredicateBorderRoundingDegree);

                ((ObjectifiedPredicate)de).innerPredicate().setRolesBorderSize(_roleSize.width, _roleSize.height);
            }
        }
    }

    static class DiagramElementsPanel extends JPanel {
        public enum RenderMode { NORMAL, WIREFRAME }

        private final Diagram _diagram;
        private final RenderMode _renderMode;

        public DiagramElementsPanel(Diagram diagram, RenderMode renderMode) {
            this._diagram    = diagram;
            this._renderMode = renderMode;

            this.requestFocusInWindow();
        }

        @Override
        public void paintComponent(Graphics g) {
            // Collect diagram elements to render
            ArrayList<DiagramElement> initialDiagramElements = this._diagram.getElements(DiagramElement.class).collect(Collectors.toCollection(ArrayList::new));
            ArrayList<DiagramElement> diagramElementsToRender = new ArrayList<>(initialDiagramElements);

            for (DiagramElement de : initialDiagramElements) {
                if (de instanceof Predicate) {
                    for (int i=0; i<((Predicate)de).arity(); i++) {
                        diagramElementsToRender.add(((Predicate)de).getRole(i));
                    }
                }
                else if (de instanceof ObjectifiedPredicate) {
                    for (int i=0; i<((ObjectifiedPredicate)de).innerPredicate().arity(); i++) {
                        diagramElementsToRender.add(((ObjectifiedPredicate)de).innerPredicate().getRole(i));
                    }
                }
            }

            // Render nodes
            Graphics2D g2 = (Graphics2D)g;

            for (DiagramElement de : diagramElementsToRender) {
                Polygon polygon = (de instanceof RolesSequence ? null : de.geometryApproximation().polygon());

                if (de instanceof Node) {
                    if (this._renderMode == RenderMode.NORMAL) {
                        g2.setColor(Color.WHITE);
                        g2.draw(polygon);
                    } else if (this._renderMode == RenderMode.WIREFRAME) {
                        g2.setColor(Color.BLUE);
                        g2.fill(polygon);

                        g2.setColor(Color.MAGENTA);
                        g2.draw(polygon);

                        for (int i=0; i<polygon.npoints; i++) {
                            g2.setColor(Color.GREEN);
                            g2.drawRect(polygon.xpoints[i], polygon.ypoints[i], 0, 0);
                        }
                    }
                }
            }

            // Render edges
            for (DiagramElement de : diagramElementsToRender) {
                Polygon polygon = (de instanceof RolesSequence ? null : de.geometryApproximation().polygon());

                if (de instanceof Edge && polygon.npoints > 1) {
                    if (this._renderMode == RenderMode.NORMAL)         { g2.setColor(Color.WHITE); }
                    else if (this._renderMode == RenderMode.WIREFRAME) { g2.setColor(Color.GREEN); }

                    for (int i=0; i<polygon.npoints-1; i++) {
                        g2.drawLine(polygon.xpoints[i], polygon.ypoints[i], polygon.xpoints[i+1], polygon.ypoints[i+1]);
                    }
                }
            }

            // Render intersections of nodes
            for (DiagramElement de : diagramElementsToRender) {
                for (DiagramElement de2 : diagramElementsToRender) {
                    boolean areNodes                             = (de instanceof Node && de2 instanceof Node);
                    boolean isSecondPredicateForFirstObjectified = (de instanceof ObjectifiedPredicate && de2 instanceof Predicate &&
                            ((ObjectifiedPredicate)de).innerPredicate() == de2);
                    boolean isSecondObjectifiedForFirstPredicate = (de instanceof Predicate && de2 instanceof ObjectifiedPredicate &&
                            ((ObjectifiedPredicate)de2).innerPredicate() == de);
                    boolean isSecondRoleForFirstPredicate        = (de instanceof Predicate && de2 instanceof Role &&
                            ((Predicate)de).roles().anyMatch(e -> e == de2));
                    boolean isSecondPredicateForFirstRole        = (de instanceof Role && de2 instanceof Predicate &&
                            ((Predicate)de2).roles().anyMatch(e -> e == de));
                    boolean isSecondRoleForFirstObjectified      = (de instanceof ObjectifiedPredicate && de2 instanceof Role &&
                            ((ObjectifiedPredicate)de).innerPredicate().roles().anyMatch(e -> e == de2));
                    boolean isSecondObjectifiedForFirstRole      = (de instanceof Role && de2 instanceof ObjectifiedPredicate &&
                            ((ObjectifiedPredicate)de2).innerPredicate().roles().anyMatch(e -> e == de));

                    if (de != de2 && areNodes &&
                            !isSecondPredicateForFirstObjectified && !isSecondRoleForFirstPredicate &&
                            !isSecondObjectifiedForFirstPredicate && !isSecondPredicateForFirstRole &&
                            !isSecondRoleForFirstObjectified && !isSecondObjectifiedForFirstRole)
                    {
                        Shape intersectionShape = de.geometryApproximation().getIntersectionWith(de2.geometryApproximation());

                        g2.setColor(Color.RED);
                        g2.fill(intersectionShape);
                    }
                }
            }
        }
    }
}
