package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Relationship;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Renders text for both ends of the relationship
 * @author Keith
 */
public class DoubleEndedRelationshipCellRenderer implements ListCellRenderer<Relationship> {

    private final DataAccessor m_accessor;

    /**
     * Creates a new RelationshipCellRenderer.
     * @param accessor the accessor used to get details about the related Entity in order to display valid info to the
     * user.
     */
    public DoubleEndedRelationshipCellRenderer(DataAccessor accessor) {
        m_accessor = accessor;
    }

    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(JList<? extends Relationship> jlist, final Relationship e, int i, boolean isSelected, boolean hasFocus) {
        JLabel originalEntityLabel = new JLabel(m_accessor.getEntity(e.getEntityId()).getName());
        JLabel relationshipLabel = new JLabel(e.getRelationshipText());
        JLabel relatedEntityLabel = new JLabel(m_accessor.getEntity(e.getRelatedEntity()).getName());

        originalEntityLabel.setOpaque(false);
        relationshipLabel.setOpaque(false);
        relatedEntityLabel.setOpaque(false);

        originalEntityLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        relationshipLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        relatedEntityLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));

        originalEntityLabel.setForeground(Color.BLUE);
        originalEntityLabel.setFont(originalEntityLabel.getFont().deriveFont(Font.BOLD));

        Color deselectedBackground = relationshipLabel.getBackground();
        Color deselectedTextColor = relationshipLabel.getForeground();

        relatedEntityLabel.setForeground(Color.BLUE);
        relatedEntityLabel.setFont(relatedEntityLabel.getFont().deriveFont(Font.BOLD));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(originalEntityLabel, gbc);
        gbc.gridx = 1;
        panel.add(relationshipLabel, gbc);
        gbc.gridx = 2;
        panel.add(relatedEntityLabel, gbc);
        if (e.isSecret()) {
            JLabel secretLabel = new JLabel("(Secret)");
            secretLabel.setForeground(Color.RED);
            secretLabel.setFont(relatedEntityLabel.getFont().deriveFont(Font.BOLD));
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(secretLabel, gbc);            
        }
        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        panel.add(new JLabel(), gbc);

        if (isSelected){
            originalEntityLabel.setOpaque(true);
            relationshipLabel.setOpaque(true);
            relatedEntityLabel.setOpaque(true);
            
            panel.setOpaque(true);
            panel.setBackground(MetalLookAndFeel.getTextHighlightColor());
            
            originalEntityLabel.setBackground(MetalLookAndFeel.getTextHighlightColor());
            relationshipLabel.setBackground(MetalLookAndFeel.getTextHighlightColor());
            relatedEntityLabel.setBackground(MetalLookAndFeel.getTextHighlightColor());
        } else {
            originalEntityLabel.setBackground(deselectedBackground);
            relationshipLabel.setBackground(deselectedBackground);
            relatedEntityLabel.setBackground(deselectedBackground);
            
            panel.setBackground(deselectedBackground);
            relationshipLabel.setForeground(deselectedTextColor);
            panel.setForeground(deselectedTextColor);
        }


        return panel;
    }

}
