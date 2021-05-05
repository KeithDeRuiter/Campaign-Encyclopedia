package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.display.NavigationPath;
import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityType;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipManager;
import campaignencyclopedia.data.RelationshipType;
import campaignencyclopedia.data.TimelineEntry;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.swing.JComponent;

/**
 * A custom component that implements CanvasDisplay, specifically for displaying plot-related 
 * entities and the relevant relationships.
 * @author adam
 */
public class PlotEntityCanvas extends JComponent implements CanvasDisplay  {

    // RENDERING VALUES
    private static final int DOT_LINE_LENGTH = 225;
    private static final int TEXT_LINE_LENGTH = 265;
    private static final int CIRCLE_RADIUS = 40;
    private static final int PAD = 5;
    private static final int BIG_PAD = 15;
    private static final Font PRIMARY_ENTITY_FONT = new Font("Arial", Font.BOLD, 20);
    private static final String RELATIONSHIPS = "Relationships:";
    private static final Shape BACK_BUTTON = new Rectangle2D.Double(0, 0, 40, 20);
    private static final Shape FWD_BUTTON = new Rectangle2D.Double(40, 0, 40, 20);
    private static final int ARROW_X_PTS[] = {0, 6, -6};
    private static final int ARROW_Y_PTS[] = {0, 14, 14};

    /** The user's navigation history.  Used to aid in navigating around the orbital display. */
    private NavigationPath m_path;

    /** The map of Entity IDs to rendering configuration objects.  Used to both render and handle user mouse interaction. */
    private final Set<RenderingConfig> m_renderingConfigs;

    /** The shapes rendered for the current entity. Used to determine if the user has selected to edit this Entity. */
    private Shape m_currentEntityShape;

    /** Current Entity */
    private UUID m_currentEntityId;

    /** The currently hovered over entity. */
    private UUID m_hoveredEntity;

    /** The position where the user is currently hovering. */
    private Point2D.Double m_hoverPoint;

    /** A data accessor. */
    private final DataAccessor m_accessor;

    /** An EntityDisplay to show Entity data on. */
    private final EntityDisplay m_display;



    /**
     * Creates a new instance of Orbital Entity Canvas.
     * @param display an entity display to show Entity data on.
     * @param accessor a data accessor to fetch Entity data from.
     */
    public PlotEntityCanvas(EntityDisplay display, DataAccessor accessor) {
        if (display == null) {
            throw new IllegalArgumentException("Parameter 'initialId' cannot be null.");
        }
        m_accessor = accessor;
        m_display = display;
        m_renderingConfigs = new HashSet<>();
        
        initializeMouseListener();
    }

    public final void show(Entity entity) {
        m_currentEntityId = entity.getId();
        m_path = new NavigationPath(entity.getId());
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Rendering stuff
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics orignalFontMetrics = g2.getFontMetrics();
        Font originalFont = g2.getFont();
        Font boldFont = originalFont.deriveFont(Font.BOLD);

        // RENDER ENTITY
        if (m_currentEntityId != null) {
            Entity currentEntity = m_accessor.getEntity(m_currentEntityId);
            if (currentEntity != null) {
                // Clear the location map
                m_renderingConfigs.clear();


                // Fetch some required values
                RelationshipManager currentRelMgr = m_accessor.getRelationshipsForEntity(m_currentEntityId);
                Set<Relationship> relationships = new HashSet<>(currentRelMgr.getAllRelationships());

//                Set<UUID> uniqueIds = new HashSet<>();
//                List<Entity> relatedEntities = new ArrayList<>();
//                for (Relationship rel : relationships) {
//                    //Get the IDs of the related entities we care about.
//                    uniqueIds.add(rel.getRelatedEntity());
//                    relatedEntities.add(m_accessor.getEntity(rel.getRelatedEntity()));
//                }
//                int relationshipCount = uniqueIds.size();
                
                
                //Sort out what goes where based on type and relationships
                Entity sideWorldConnection = null;
                List<Entity> topConnections = new ArrayList<>();
                List<Entity> bottomConnections = new ArrayList<>();
                
                //TODO make this ALL more robust w.r.t. relationship types/text/names/etc
                if (currentEntity.getType() == EntityType.PLOT_LEAD) {
                    //If this is a lead, then get the *plot point* nodes it leads to, and the node(s) it is found at
                    
                    for (Relationship r : relationships) {
                        Entity dstEntity = m_accessor.getEntity(r.getRelatedEntity());
                        Entity srcEntity = m_accessor.getEntity(r.getEntityId());
                        //For rendering leads, we only care about connected points
                        if (dstEntity.getType() != EntityType.PLOT_POINT && srcEntity.getType() != EntityType.PLOT_POINT) {
                            continue;
                        }
                        
                        if (r.getRelationshipText().equals(RelationshipType.LEADS_TO.getDisplayString())) {
                            //Found a thing that this lead leads to, put it 'on top'
                            topConnections.add(dstEntity);
                        } else if (r.getRelationshipText().equals(RelationshipType.REVEALS.getDisplayString())) {
                            //Found a thing which reveals this lead, put it 'on bottom'
                            bottomConnections.add(srcEntity);
                        }
                    }
                    
                } else if (currentEntity.getType() == EntityType.PLOT_POINT) {
                    //If this is a plot point, then get the *leads* leading to here and the leads found here
                    
                    for (Relationship r : relationships) {
                        Entity dstEntity = m_accessor.getEntity(r.getRelatedEntity());
                        Entity srcEntity = m_accessor.getEntity(r.getEntityId());
                        //For rendering plot points, we only care about connected leads
                        if (dstEntity.getType() != EntityType.PLOT_LEAD && srcEntity.getType() != EntityType.PLOT_LEAD) {
                            continue;
                        }
                        
                        if (r.getRelationshipText().equals(RelationshipType.REVEALS.getDisplayString())) {
                            //Find leads that are revealed based on this plot point and put them 'on top'
                            topConnections.add(dstEntity);
                        } else if (r.getRelationshipText().equals(RelationshipType.LEADS_TO.getDisplayString())) {
                            //Find leads that lead to this plot point and put them 'on bottom'
                            bottomConnections.add(srcEntity);
                        }
                    }
                    
                } else {
                    //Something else, just grab all relationships and put them below.  Probably shouldn't happen.
                    for (Relationship r : relationships) {
                        Entity dstEntity = m_accessor.getEntity(r.getRelatedEntity());
                        Entity srcEntity = m_accessor.getEntity(r.getEntityId());
                        
                        if (currentEntity.equals(srcEntity)) {
                            topConnections.add(dstEntity);
                        } else {
                            bottomConnections.add(dstEntity);
                        }
                    }
                }
                
                //Ensure consistent ordering of items
                Collections.sort(topConnections);
                Collections.sort(bottomConnections);

                
                //Grab general rendering values for everything
                Point2D.Double center = new Point2D.Double(getWidth() / 2, getHeight() / 2);
                int dotRadius = getDotRadius();
                int halfDotRadius = getDotRadius() / 2;
                float fanWidth = 135.0f;
                
                //Populate the top relationships
                int numTops = topConnections.size();
                float topAngleDelta = fanWidth / numTops;
                float currentAngle = 360.0f - ((topAngleDelta + (180.0f - fanWidth)) / 2.0f);  //Top starts at 360, offset by half the spread angle and goes CCW
                for (Entity e : topConnections) {
                    RenderingConfig config = new RenderingConfig();
                    config.dotPoint = getPoint(center, currentAngle, getDotLineLength());
                    config.textPoint = getPoint(center, currentAngle, getTextLineLength());
                    config.entity = e;
                    config.isTop = true;
                    config.angle = currentAngle;  //Store angle for ease of computation later
                    m_renderingConfigs.add(config);
                    currentAngle -= topAngleDelta;  //See getPoint on this class
                }
                
                // Repopulate the bottom location map.
                int numBottoms = bottomConnections.size();
                float bottomAngleDelta = fanWidth / numBottoms;
                currentAngle = 0.0f + ((bottomAngleDelta + (180.0f - fanWidth)) / 2.0f);  //Bottom starts at 0, offset by half the spread angle and goes CW
                for (Entity e : bottomConnections) {
                    RenderingConfig config = new RenderingConfig();
                    config.dotPoint = getPoint(center, currentAngle, getDotLineLength());
                    config.textPoint = getPoint(center, currentAngle, getTextLineLength());
                    config.entity = e;
                    config.isTop = false;
                    config.angle = currentAngle;  //Store angle for ease of computation later
                    m_renderingConfigs.add(config);
                    currentAngle += bottomAngleDelta;  //See getPoint on this class
                }

                // Draw all of the lines and their relationship dots
                for (RenderingConfig rc : m_renderingConfigs) {
                    Entity relatedTo = rc.entity;
                    if (relatedTo != null) {
                        // Lines first
                        g2.setPaint(Colors.LINE);
                        g2.draw(new Line2D.Double(center.x, center.y, rc.dotPoint.x, rc.dotPoint.y));
                        
                        //Arrowheads
                        AffineTransform p = g2.getTransform();  //store old transform to allow for rotation/translation
                        g2.translate(center.x, center.y);
                        
                        if (rc.isTop) {
                            double angleRad = Math.toRadians(rc.angle + 90);
                            g2.rotate(angleRad);
                            g2.translate(0, halfDotRadius - DOT_LINE_LENGTH);
                        } else {
                            double angleRad = Math.toRadians(rc.angle - 90);
                            g2.rotate(angleRad);
                            g2.translate(0, CIRCLE_RADIUS);
                        }
                        g2.fill(new Polygon(ARROW_X_PTS, ARROW_Y_PTS, ARROW_X_PTS.length));
                        
                        g2.setTransform(p);

                        // Then Dots
                        g2.setPaint(Colors.getColor(rc.entity.getType()));
                        rc.dot = new Ellipse2D.Double(rc.dotPoint.x - halfDotRadius, rc.dotPoint.y - halfDotRadius, dotRadius, dotRadius);
                        g2.fill(rc.dot);

                        // Then Text
                        g2.setPaint(Color.BLACK);
                        double strWidth = orignalFontMetrics.stringWidth(rc.entity.getName());
                        String name = rc.entity.getName();
                        if (rc.textPoint.x < center.x) {
                            g2.drawString(name, (float)(rc.textPoint.x - strWidth), (float)rc.textPoint.y);
                        } else {
                            g2.drawString(name, (float)rc.textPoint.x, (float)rc.textPoint.y);
                        }
                    }
                }

                // RENDER CURRENT PRIMARY ENTITY
                // --- Gather needed values

                // --- DOT
                g2.setPaint(Colors.getColor(currentEntity.getType()));
                m_currentEntityShape = new Ellipse2D.Double(center.x - dotRadius, center.y - dotRadius, dotRadius * 2, dotRadius * 2);
                g2.fill(m_currentEntityShape);

                // --- LICENSE PLATE
                // --- The background
                g2.setFont(PRIMARY_ENTITY_FONT);
                FontMetrics bigFontMetrics = g2.getFontMetrics();
                g2.setPaint(Color.WHITE);
                int licensePlateWidth = dotRadius * 3;
                boolean licensePlateUsedMinWidth = true;
                if (bigFontMetrics.stringWidth(currentEntity.getName()) > licensePlateWidth) {
                    licensePlateWidth = bigFontMetrics.stringWidth(currentEntity.getName());
                    licensePlateUsedMinWidth = false;
                }
                int licensePlateHeight = bigFontMetrics.getHeight();
                double x = center.x - (licensePlateWidth / 2) - PAD;
                double y = center.y - (licensePlateHeight / 2);
                g2.fill(new Rectangle2D.Double(x, y, (licensePlateWidth + PAD * 2), licensePlateHeight));
                // --- The Border
                g2.setPaint(Color.BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Rectangle2D.Double(x, y, (licensePlateWidth + PAD * 2), licensePlateHeight));

                // --- License Plate Entity Text
                g2.setFont(PRIMARY_ENTITY_FONT);
                float primaryEntityNameX = (float)center.x - (licensePlateWidth / 2);
                float primaryEntityNameY = (float)center.y + (licensePlateHeight / 2) + 1;
                if (licensePlateUsedMinWidth) {
                    primaryEntityNameX += (licensePlateWidth - bigFontMetrics.stringWidth(currentEntity.getName())) / 2.0f;
                }
                g2.drawString(currentEntity.getName(), primaryEntityNameX, primaryEntityNameY - PAD);


                // RENDER RELATIONSHIP HOVER DATA (IF VALID TO DO SO)
                if (m_hoveredEntity != null) {
                    Entity hovered = m_accessor.getEntity(m_hoveredEntity);
                    if (hovered != null) {
                        int maxWidth = orignalFontMetrics.stringWidth(RELATIONSHIPS);
                        List<String> hoverRelationships = new ArrayList<>();
                        hoverRelationships.add(RELATIONSHIPS);
                        for (Relationship rel : currentRelMgr.getPublicRelationships()) {
                            if (rel.getRelatedEntity().equals(m_hoveredEntity) || rel.getEntityId().equals(m_hoveredEntity)) {
                                String line = "\n  - " + m_accessor.getEntity(rel.getEntityId()).getName() + " " + rel.getRelationshipText() + " " + m_accessor.getEntity(rel.getRelatedEntity()).getName();
                                hoverRelationships.add(line);
                                int stringWidth = orignalFontMetrics.stringWidth(line);
                                if (maxWidth < stringWidth) {
                                    maxWidth = stringWidth;
                                }
                            }
                        }
                        for (Relationship rel : currentRelMgr.getSecretRelationships()) {
                            if (rel.getRelatedEntity().equals(m_hoveredEntity) || rel.getEntityId().equals(m_hoveredEntity)) {
                                String line = "\n  - " + m_accessor.getEntity(rel.getEntityId()).getName() + " " + rel.getRelationshipText() + " " + m_accessor.getEntity(rel.getRelatedEntity()).getName() + " (Secret)";
                                hoverRelationships.add(line);
                                int stringWidth = orignalFontMetrics.stringWidth(line);
                                if (maxWidth < stringWidth) {
                                    maxWidth = stringWidth;
                                }
                            }
                        }

                        // Background
                        int hoverWidth = maxWidth + BIG_PAD * 2;
                        int hoverHeight = hoverRelationships.size() * orignalFontMetrics.getHeight() + BIG_PAD;
                        g2.setPaint(Color.WHITE);
                        g2.setFont(originalFont);
                        g2.fill(new Rectangle2D.Double(m_hoverPoint.x, m_hoverPoint.y + BIG_PAD, hoverWidth, hoverHeight));

                        // Border
                        g2.setPaint(Color.BLACK);
                        g2.draw(new Rectangle2D.Double(m_hoverPoint.x, m_hoverPoint.y + BIG_PAD, hoverWidth, hoverHeight));

                        // Text
                        float hoverRelTextY = (float)m_hoverPoint.y + BIG_PAD + PAD;
                        for (String relString : hoverRelationships) {
                            hoverRelTextY += orignalFontMetrics.getHeight();
                            g2.drawString(relString, (float)m_hoverPoint.x + BIG_PAD, hoverRelTextY);
                        }
                    } else {
                        m_hoveredEntity = null;
                    }
                }
            } else {
                m_currentEntityId = null;
            }
        } else {
            g2.drawString("No Data", this.getWidth() / 2, this.getHeight() / 2);
        }

//        // Re BACK / FWD Buttons
//        g2.setFont(originalFont);
//        g2.setPaint(Color.WHITE);
//        g2.fill(BACK_BUTTON);
//        g2.setPaint(Color.BLACK);
//        g2.draw(BACK_BUTTON);
//        if (!m_path.isBackPossible()) {
//            g2.setPaint(Color.GRAY);
//        }
//        g2.drawString("Back", 10.0f, 15.0f);
//
//        g2.setPaint(Color.WHITE);
//        g2.fill(FWD_BUTTON);
//        g2.setPaint(Color.BLACK);
//        g2.draw(FWD_BUTTON);
//        if (!m_path.isForwardPossible()) {
//            g2.setPaint(Color.GRAY);
//        }
//        g2.drawString("Fwd", 50.0f, 15.0f);
//
//        // Render Navigation History
//        RecentHistory recentHistory = m_path.getRecentHistory();
//        g2.setFont(boldFont);
//        g2.setPaint(Color.BLACK);
//        float historyYpos = 20.0f + (recentHistory.getRecentHistory().size() * orignalFontMetrics.getHeight());
//        for (int i = 0; i < recentHistory.getRecentHistory().size(); i++) {
//            String name = m_accessor.getEntity(recentHistory.getRecentHistory().get(i)).getName();
//            if (i == recentHistory.getCurrentIndex()) {
//                g2.setFont(boldFont);
//                g2.drawString(name, PAD, historyYpos);
//                historyYpos = historyYpos - g2.getFontMetrics().getHeight();
//            } else {
//                g2.setPaint(Color.BLACK);
//                g2.setFont(originalFont);
//                g2.drawString(name, PAD, historyYpos);
//                historyYpos = historyYpos - g2.getFontMetrics().getHeight();
//            }
//        }
    }

    /**
     * Generates a new point that is {@code distance} units away from {@code center} at {@code angle}
     * The angle is measured clockwise from 3 o'clock, so 0 degrees is directly right, 90 degrees
     * is straight down, etc.
     * @param center The center point to project out from.
     * @param angle The angle, in degrees.
     * @param distance The distance away to generate a point at.
     * @return A new point the stated distance and direction away from the specified 'center'
     */
    private Point2D.Double getPoint(Point2D.Double center, double angle, double distance) {
        // Angles in java are measured clockwise from 3 o'clock.
        double theta = Math.toRadians(angle);
        Point2D.Double p = new Point2D.Double();
        p.x = center.x + distance*Math.cos(theta);
        p.y = center.y + distance*Math.sin(theta);
        return p;
    }

    private int getDotLineLength() {
        return DOT_LINE_LENGTH;
    }

    private int getTextLineLength() {
        return TEXT_LINE_LENGTH;
    }

    private int getDotRadius() {
        return CIRCLE_RADIUS;
    }

    private void initializeMouseListener() {
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent me) {
                Point click = me.getPoint();
                if (BACK_BUTTON.contains(click)) {
                    if(m_path.back()) {
                        show(m_accessor.getEntity(m_path.getCurrentId()));
                    }
                } else if (FWD_BUTTON.contains(click)) {
                    if (m_path.forward()) {
                        show(m_accessor.getEntity(m_path.getCurrentId()));
                    }
                } else if (m_currentEntityId != null &&
                           m_currentEntityShape != null &&
                           m_currentEntityShape.contains(click)) {
                    //Don't require a modifier to jump to next entity, cursor will change
                    //This covers the showing the already shown entity, which probably isn't an issue...
                    m_display.showEntity(m_currentEntityId);
                } else {
                    for (RenderingConfig rc : m_renderingConfigs) {
                        if (rc.dot.contains(me.getPoint())) {
                            UUID id = rc.entity.getId();
                            
                            //You clicked on an entity, so make the whole display show that one
                            m_display.showEntity(id);
                                
//                            //This code is for "change displayed entities on CTRL click, but change display of orbit only on normal click
//                            if (me.isControlDown()) {
//                                m_display.showEntity(id);
//                            } else {
//                                //Show the next thing without actually changing which entity is displayed on the whole display
//                                m_path.add(id);
//                                show(m_accessor.getEntity(id));
//                            }

                            // Clear out hover data so we don't have any lingering displays.
                            m_hoveredEntity = null;
                            m_hoverPoint = null;
                            // Quit looking you found what you wanted.
                            break;
                        }
                    }
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent me) {

                // Find out if we're hovering over a given entity.
                boolean found = false;
                for (RenderingConfig rc : m_renderingConfigs) {
                    if (rc.dot.contains(me.getPoint())) {
                        found = true;
                        m_hoveredEntity = rc.entity.getId();
                        m_hoverPoint = new Point2D.Double(me.getX(), me.getY());
                        repaint();
                        break;
                    }
                }

                // Clear out the hovered entity and cursor if none exists, else set cursor
                if (found == false) {
                    m_hoveredEntity = null;
                    m_hoverPoint = null;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                } else {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }
        });
    }

    
    /** {@inheritDoc} */
    @Override
    public void dataRemoved(UUID id) {
        if (id.equals(m_currentEntityId)) {
            m_currentEntityId = null;
            m_hoveredEntity = null;
        } else if (id.equals(m_hoveredEntity)) {
            m_hoveredEntity = null;
        }
        m_path.removeAll(id);
        repaint();
    }

    /** {@inheritDoc} */
    @Override
    public void dataAddedOrUpdated(Entity entity) {
        repaint();
    }
    
    @Override
    public void timelineEntryAddedOrUpdated(TimelineEntry tle) {
        // ignored
    }

    @Override
    public void timelineEntryRemoved(UUID id) {
        // ignored
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void clearAllData() {
        m_currentEntityId = null;
        m_hoveredEntity = null;
        repaint();
    }

    /** A data bag for holding the locations calculated for rendering data. */
    private class RenderingConfig {
        private Point2D.Double dotPoint;
        private Point2D.Double textPoint;
        private Shape dot;
        private Entity entity;  //Store entity to prevent double lookups during repaint
        /** In Degrees. */
        private float angle;
        private boolean isTop;
    }
}