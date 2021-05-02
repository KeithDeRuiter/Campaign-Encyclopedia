package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.ColoredDisplayable;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * A renderer for Displayables that also have a Color associated with them. In this renderer, a
 * simple colored dot will be rendered to the left of the text.
 *
 * @author adam
 */
public class ColoredDisplayableCellRenderer implements ListCellRenderer<ColoredDisplayable>, TreeCellRenderer {

    /**
     * An Insets instance.
     */
    private static final Insets INSETS = new Insets(3, 0, 3, 0);

    /**
     * Default renderer for tree cells in case normal rendering fails.
     */
    private static final DefaultTreeCellRenderer DEFAULT_RENDERER = new DefaultTreeCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList<? extends ColoredDisplayable> jlist, ColoredDisplayable e, int i, boolean isSelected, boolean hasFocus) {
        Component cell = renderColoredDisplayable(e, isSelected);
        return cell;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component returnValue = null;

        if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof ColoredDisplayable) {
                ColoredDisplayable e = (ColoredDisplayable) userObject;
                returnValue = renderColoredDisplayable(e, selected);
            }
        }

        if (returnValue == null) {
            returnValue = DEFAULT_RENDERER.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }

        return returnValue;
    }

    private JComponent renderColoredDisplayable(ColoredDisplayable d, boolean selected) {
        JPanel cell = new JPanel(new GridBagLayout());
        cell.setOpaque(false);

        JLabel label = new JLabel(d.getDisplayString());
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        label.setHorizontalAlignment(JLabel.LEFT);

        Color deselectedBackground = cell.getBackground();
        Color deselectedTextColor = cell.getForeground();

        // LAYOUT COMPONENTS
        // Dot
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0.0f;
        gbc.fill = GridBagConstraints.NONE;
        cell.add(new Dot(d.getColor()), gbc);

        // Label
        gbc.gridx = 1;
        gbc.weightx = 1.0f;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cell.add(label, gbc);

        if (selected) {
            cell.setOpaque(true);
            cell.setBackground(MetalLookAndFeel.getTextHighlightColor());
        } else {
            cell.setBackground(deselectedBackground);
            cell.setForeground(deselectedTextColor);
        }

        return cell;
    }
}
