package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.display.CampaignDataManagerListener;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import toolbox.display.DisplayUtilities;

/**
 * A graphical timeline viewer for timeline events.
 * @author adam
 * @author keith
 */
public class GraphicalTimelineViewer implements CampaignDataManagerListener {

    /** A logger. */
    private static final Logger LOGGER = Logger.getLogger(GraphicalTimelineViewer.class.getName());
    
    /** The starting dimensions of the top-level window. */
    private static final Dimension m_windowSize = new Dimension(900, 675);
    
    /** The JFrame of this display. */
    private JFrame m_frame;
    
    /** The timeline canvas upon which the data will be rendered. */
    private final GraphicalTimelineCanvas m_canvas;
    
    /**
     * Creates a new OrbitalEntityViewer.
     * @param display an EntityDisplay to show Entity data on.
     * @param da the data accessor to fetch data to view.
     */
    public GraphicalTimelineViewer(EntityDisplay display, DataAccessor da) {
        m_canvas = new GraphicalTimelineCanvas(display, da);
        initialize();
    }
    
    public void launch() {
        m_frame.pack();
        DisplayUtilities.positionWindowInDisplayCenter(m_frame, m_windowSize);
        try {
            m_frame.setIconImage(ImageIO.read(new File("./assets/app.png")));
        } catch (IOException ex) {
            LOGGER.log(Level.CONFIG, "Unable to load application icon.", ex);
        }
        m_frame.setVisible(true);
    }
    
    private void initialize() {
        m_frame = new JFrame("Graphical Timeline");
        m_frame.setLayout(new BorderLayout());
        m_frame.setPreferredSize(m_windowSize);
        m_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        m_canvas.setFocusable(true);
        m_canvas.requestFocusInWindow();
        JScrollPane canvasScroller = new JScrollPane(m_canvas);
        
        m_frame.add(canvasScroller, BorderLayout.CENTER);
    }

    @Override
    public void dataRemoved(UUID id) {
        m_canvas.dataRemoved(id);
    }

    @Override
    public void dataAddedOrUpdated(Entity entity) {
        m_canvas.dataAddedOrUpdated(entity);
    }
    
}
