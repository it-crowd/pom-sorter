package pl.itcrowd.pom_sorter.ui;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;
import pl.itcrowd.pom_sorter.PomSorter;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SettingsForm {
// ------------------------------ FIELDS ------------------------------

    private JButton addButton;

    private JButton addChildButton;

    private JList childTagList;

    private ChildTagListModel childTagListModel;

    private CurrentSettingPropertyChangeListener currentSettingPropertyChangeListener;

    private JComboBox defaultSortModeComboBox;

    private JRadioButton dontSortRadioButton;

    private JButton downChildButton;

    private JButton editChildButton;

    private JRadioButton fixedOrderRadioButton;

    private boolean modified;

    private PomSorter pomSorter;

    private JButton removeButton;

    private JButton removeChildButton;

    private JButton restoreDefaultsButton;

    private JPanel rootComponent;

    private boolean settingCleanName;

    private JRadioButton sortByAttributeRadioButton;

    private JTextField sortByAttributeTextField;

    private JRadioButton sortByGroupIdArtifactIdRadioButton;

    private JRadioButton sortBySubtagRadioButton;

    private JTextField sortBySubtagTextField;

    private JRadioButton sortByTagNameRadioButton;

    private JList tagList;

    private TagListModel tagListModel;

    private JTextField tagNameTextField;

    private JButton upChildButton;

// --------------------------- CONSTRUCTORS ---------------------------

    public SettingsForm(final PomSorter pomSorter)
    {
        this.pomSorter = pomSorter;

        defaultSortModeComboBox.setModel(new EnumComboBoxModel<PomSorter.SortMode>(PomSorter.SortMode.class));
        defaultSortModeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                modified = true;
            }
        });

        tagNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent)
            {
                modified |= !settingCleanName;
                if (settingCleanName) {
                    return;
                }
                PresentableTagSortingSetting setting = tagListModel.getCurrentElement();


                if (setting != null) {
                    setting.setName(tagNameTextField.getText());
                }
            }
        });
        sortByAttributeTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent)
            {
                modified |= !settingCleanName;
                if (settingCleanName) {
                    return;
                }
                PresentableTagSortingSetting setting = tagListModel.getCurrentElement();

                if (setting != null) {
                    setting.setAttributeName(sortByAttributeTextField.getText());
                }
            }
        });
        sortBySubtagTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent)
            {
                modified |= !settingCleanName;
                if (settingCleanName) {
                    return;
                }
                PresentableTagSortingSetting setting = tagListModel.getCurrentElement();

                if (setting != null) {
                    setting.setAttributeName(sortBySubtagTextField.getText());
                }
            }
        });
        currentSettingPropertyChangeListener = new CurrentSettingPropertyChangeListener();

        tagListModel = new TagListModel(tagList);

        tagListModel.addPropertyChangeListener(TagListModel.PROPERTY_CURRENT_ELEMENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                PresentableTagSortingSetting setting = (PresentableTagSortingSetting) evt.getNewValue();
                currentSettingPropertyChangeListener.attachTo(setting);
                settingCleanName = true;
                tagNameTextField.setText(setting == null ? "" : setting.getName());
                sortByAttributeTextField.setText("");
                sortBySubtagTextField.setText("");
                if (null != setting && PomSorter.SortMode.ATTRIBUTE.equals(setting.getMode())) {
                    sortByAttributeTextField.setText(setting.getAttributeName());
                } else if (null != setting && PomSorter.SortMode.SUBTAG.equals(setting.getMode())) {
                    sortBySubtagTextField.setText(setting.getAttributeName());
                }
                settingCleanName = false;
                fixedOrderRadioButton.setSelected(setting != null && PomSorter.SortMode.FIXED.equals(setting.getMode()));
                dontSortRadioButton.setSelected(setting != null && PomSorter.SortMode.NONE.equals(setting.getMode()));
                sortByTagNameRadioButton.setSelected(setting != null && PomSorter.SortMode.ALPHABETIC.equals(setting.getMode()));
                sortByGroupIdArtifactIdRadioButton.setSelected(setting != null && PomSorter.SortMode.ARTIFACT.equals(setting.getMode()));
                sortByAttributeRadioButton.setSelected(setting != null && PomSorter.SortMode.ATTRIBUTE.equals(setting.getMode()));
                sortBySubtagRadioButton.setSelected(setting != null && PomSorter.SortMode.SUBTAG.equals(setting.getMode()));
                tagNameTextField.setEnabled(setting != null);
                fixedOrderRadioButton.setEnabled(setting != null);
                dontSortRadioButton.setEnabled(setting != null);
                sortByGroupIdArtifactIdRadioButton.setEnabled(setting != null);
                sortByTagNameRadioButton.setEnabled(setting != null);
                sortByAttributeRadioButton.setEnabled(setting != null);
                updateTagsControls();
                updateChildControls();
            }
        });
        tagList.setModel(tagListModel);

        childTagListModel = new ChildTagListModel(childTagList, tagListModel);
        childTagList.setModel(childTagListModel);

        childTagListModel.addPropertyChangeListener(ChildTagListModel.PROPERTY_CURRENT_ELEMENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                updateChildControls();
            }
        });

        final ChangeListener modeRadioButtonChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                modified = true;
                PresentableTagSortingSetting setting = tagListModel.getCurrentElement();
                if (setting != null) {
                    if (fixedOrderRadioButton.equals(e.getSource()) && fixedOrderRadioButton.isSelected()) {
                        setting.setMode(PomSorter.SortMode.FIXED);
                    } else if (dontSortRadioButton.equals(e.getSource()) && dontSortRadioButton.isSelected()) {
                        setting.setMode(PomSorter.SortMode.NONE);
                    } else if (sortByTagNameRadioButton.equals(e.getSource()) && sortByTagNameRadioButton.isSelected()) {
                        setting.setMode(PomSorter.SortMode.ALPHABETIC);
                    } else if (sortByGroupIdArtifactIdRadioButton.equals(e.getSource()) && sortByGroupIdArtifactIdRadioButton.isSelected()) {
                        setting.setMode(PomSorter.SortMode.ARTIFACT);
                    } else if (sortByAttributeRadioButton.equals(e.getSource()) && sortByAttributeRadioButton.isSelected()) {
                        setting.setMode(PomSorter.SortMode.ATTRIBUTE);
                    } else if (sortBySubtagRadioButton.equals(e.getSource()) && sortBySubtagRadioButton.isSelected()) {
                        setting.setMode(PomSorter.SortMode.SUBTAG);
                    }
                }
            }
        };
        fixedOrderRadioButton.addChangeListener(modeRadioButtonChangeListener);
        dontSortRadioButton.addChangeListener(modeRadioButtonChangeListener);
        sortByGroupIdArtifactIdRadioButton.addChangeListener(modeRadioButtonChangeListener);
        sortByTagNameRadioButton.addChangeListener(modeRadioButtonChangeListener);
        sortByAttributeRadioButton.addChangeListener(modeRadioButtonChangeListener);
        sortBySubtagRadioButton.addChangeListener(modeRadioButtonChangeListener);


        addChildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final PresentableTagSortingSetting currentElement = tagListModel.getCurrentElement();
                if (currentElement == null) {
                    return;
                }
                final String name = JOptionPane.showInputDialog($$$getRootComponent$$$(), "Tag name");
                if (name == null || StringUtils.isBlank(name)) {
                    return;
                }
                childTagListModel.add(name);
                modified = true;
            }
        });
        editChildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final StringHolder currentElement = childTagListModel.getCurrentElement();
                if (currentElement == null) {
                    return;
                }
                final String name = JOptionPane.showInputDialog($$$getRootComponent$$$(), "Tag name", currentElement.getValue());
                if (name == null || StringUtils.isBlank(name)) {
                    return;
                }
                currentElement.setValue(name);
                modified = true;
            }
        });
        removeChildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                childTagListModel.removeCurrentElement();
                modified = true;
            }
        });
        upChildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                childTagListModel.moveCurrentElementUp();
                modified = true;
            }
        });
        downChildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                childTagListModel.moveCurrentElementDown();
                modified = true;
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String name = JOptionPane.showInputDialog($$$getRootComponent$$$(), "Tag name");
                if (name == null || StringUtils.isBlank(name)) {
                    return;
                }
                tagListModel.add(new PresentableTagSortingSetting(name));
                modified = true;
            }
        });
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tagListModel.removeCurrentElement();
                modified = true;
            }
        });
        restoreDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final int result = JOptionPane.showConfirmDialog($$$getRootComponent$$$(), "Are you sure you want to restore defaults?");
                if (JOptionPane.OK_OPTION == result) {
                    pomSorter.restoreDefaults();
                    reset();
                }
            }
        });
        reset();
        updateTagsControls();
        updateChildControls();
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    // -------------------------- OTHER METHODS --------------------------

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return rootComponent;
    }

    public void apply()
    {
        final Map<String, PomSorter.TagSortingSetting> tagPriorities = pomSorter.getTagSortingSettings();
        tagPriorities.clear();
        for (int i = 0; i < tagListModel.getSize(); i++) {
            PresentableTagSortingSetting tag = tagListModel.getElementAt(i);
            tag.updateModel();
            tagPriorities.put(tag.setting.getName(), tag.setting);
        }
        pomSorter.setDefaultSortMode((PomSorter.SortMode) defaultSortModeComboBox.getSelectedItem());
        modified = false;
    }

    public boolean isModified()
    {
        return modified;
    }

    public void reset()
    {
        tagListModel.clear();
        for (PomSorter.TagSortingSetting setting : pomSorter.getTagSortingSettings().values()) {
            tagListModel.add(new PresentableTagSortingSetting(setting));
        }
        defaultSortModeComboBox.setSelectedItem(pomSorter.getDefaultSortMode());
        modified = false;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        rootComponent = new JPanel();
        rootComponent.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(9, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootComponent.add(panel1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Tag name");
        panel1.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tagNameTextField = new JTextField();
        panel1.add(tagNameTextField,
            new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sortByTagNameRadioButton = new JRadioButton();
        sortByTagNameRadioButton.setText("Sort by tag name and value");
        panel1.add(sortByTagNameRadioButton, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dontSortRadioButton = new JRadioButton();
        dontSortRadioButton.setText("Don't sort");
        panel1.add(dontSortRadioButton, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fixedOrderRadioButton = new JRadioButton();
        fixedOrderRadioButton.setText("Fixed order");
        panel1.add(fixedOrderRadioButton, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sortByGroupIdArtifactIdRadioButton = new JRadioButton();
        sortByGroupIdArtifactIdRadioButton.setText("Sort by GroupId:ArtifactId");
        panel1.add(sortByGroupIdArtifactIdRadioButton, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(0, 0, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        childTagList = new JList();
        scrollPane1.setViewportView(childTagList);
        addChildButton = new JButton();
        addChildButton.setIcon(new ImageIcon(getClass().getResource("/general/add.png")));
        addChildButton.setText("");
        addChildButton.setToolTipText("Add");
        panel2.add(addChildButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1,
            new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null,
                null, 0, false));
        removeChildButton = new JButton();
        removeChildButton.setIcon(new ImageIcon(getClass().getResource("/general/remove.png")));
        removeChildButton.setText("");
        removeChildButton.setToolTipText("Remove");
        panel2.add(removeChildButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        upChildButton = new JButton();
        upChildButton.setIcon(new ImageIcon(getClass().getResource("/actions/moveUp.png")));
        upChildButton.setText("");
        upChildButton.setToolTipText("Up");
        panel2.add(upChildButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downChildButton = new JButton();
        downChildButton.setIcon(new ImageIcon(getClass().getResource("/actions/moveDown.png")));
        downChildButton.setText("");
        downChildButton.setToolTipText("Down");
        panel2.add(downChildButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editChildButton = new JButton();
        editChildButton.setIcon(new ImageIcon(getClass().getResource("/actions/edit.png")));
        editChildButton.setText("");
        editChildButton.setToolTipText("Edit");
        panel2.add(editChildButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sortByAttributeRadioButton = new JRadioButton();
        sortByAttributeRadioButton.setText("Sort by attribute");
        panel1.add(sortByAttributeRadioButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sortByAttributeTextField = new JTextField();
        panel1.add(sortByAttributeTextField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null,
            0, false));
        sortBySubtagRadioButton = new JRadioButton();
        sortBySubtagRadioButton.setText("Sort by child tag");
        panel1.add(sortBySubtagRadioButton, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sortBySubtagTextField = new JTextField();
        panel1.add(sortBySubtagTextField, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null,
            0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootComponent.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel3.add(scrollPane2, new GridConstraints(0, 0, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tagList = new JList();
        scrollPane2.setViewportView(tagList);
        addButton = new JButton();
        addButton.setIcon(new ImageIcon(getClass().getResource("/general/add.png")));
        addButton.setText("");
        addButton.setToolTipText("Add");
        panel3.add(addButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeButton = new JButton();
        removeButton.setIcon(new ImageIcon(getClass().getResource("/general/remove.png")));
        removeButton.setText("");
        removeButton.setToolTipText("Remove");
        panel3.add(removeButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2,
            new GridConstraints(2, 1, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null,
                null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootComponent.add(panel4, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Default sort mode");
        panel4.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        defaultSortModeComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultSortModeComboBox.setModel(defaultComboBoxModel1);
        panel4.add(defaultSortModeComboBox,
            new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        restoreDefaultsButton = new JButton();
        restoreDefaultsButton.setText("Restore defaults");
        panel4.add(restoreDefaultsButton,
            new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null,
                new Dimension(100, -1), 0, false));
        label1.setLabelFor(tagNameTextField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(fixedOrderRadioButton);
        buttonGroup.add(sortByTagNameRadioButton);
        buttonGroup.add(dontSortRadioButton);
        buttonGroup.add(sortByGroupIdArtifactIdRadioButton);
        buttonGroup.add(sortByAttributeRadioButton);
        buttonGroup.add(sortBySubtagRadioButton);
    }

    private void updateChildControls()
    {
        final PresentableTagSortingSetting setting = tagListModel.getCurrentElement();
        final StringHolder currentChild = childTagListModel.getCurrentElement();
        final boolean sortingMode = setting != null && PomSorter.SortMode.FIXED.equals(setting.getMode());
        childTagList.setEnabled(sortingMode);
        addChildButton.setEnabled(sortingMode);
        editChildButton.setEnabled(sortingMode && currentChild != null);
        removeChildButton.setEnabled(sortingMode && currentChild != null);
        upChildButton.setEnabled(sortingMode && currentChild != null && !childTagListModel.isFirstSelected());
        downChildButton.setEnabled(sortingMode && currentChild != null && !childTagListModel.isLastSelected());
        sortByAttributeTextField.setEnabled(setting != null && PomSorter.SortMode.ATTRIBUTE.equals(setting.getMode()));
        sortBySubtagTextField.setEnabled(setting != null && PomSorter.SortMode.SUBTAG.equals(setting.getMode()));
    }

    private void updateTagsControls()
    {
        final PresentableTagSortingSetting setting = tagListModel.getCurrentElement();
        removeButton.setEnabled(setting != null);
    }

    // -------------------------- INNER CLASSES --------------------------

    private static class ChildTagListModel extends AbstractListModel implements PropertyChangeListener, ListDataListener, ListSelectionListener {
// ------------------------------ FIELDS ------------------------------

        public static final String PROPERTY_CURRENT_ELEMENT = "currentElement";

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        private JList childTagList;

        private Integer childTagSelectionIndex;

        private TagListModel tagListModel;

// --------------------------- CONSTRUCTORS ---------------------------

        public ChildTagListModel(JList childTagList, TagListModel tagListModel)
        {
            this.childTagList = childTagList;
            this.tagListModel = tagListModel;
            tagListModel.addPropertyChangeListener(TagListModel.PROPERTY_CURRENT_ELEMENT, this);
            childTagList.getSelectionModel().addListSelectionListener(this);
            childTagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ListDataListener ---------------------

        public StringHolder getCurrentElement()
        {
            return childTagSelectionIndex == null ? null : getElementAt(childTagSelectionIndex);
        }

        @Override
        public StringHolder getElementAt(int index)
        {
            final PresentableTagSortingSetting currentElement = tagListModel.getCurrentElement();
            if (currentElement == null) {
                return null;
            } else {
                List<StringHolder> order = currentElement.getOrder();
                return order.isEmpty() || order.size() <= index ? null : order.get(index);
            }
        }

        @Override
        public int getSize()
        {
            final PresentableTagSortingSetting currentElement = tagListModel.getCurrentElement();
            return currentElement == null ? 0 : currentElement.getOrder().size();
        }

// --------------------- Interface ListModel ---------------------

        public void add(String name)
        {
            final PresentableTagSortingSetting setting = tagListModel.getCurrentElement();
            if (setting == null) {
                return;
            }
            int index = childTagSelectionIndex == null ? setting.getOrder().size() : childTagSelectionIndex + 1;
            setting.addChildTag(index, name);
            getElementAt(index).addPropertyChangeListener(StringHolder.PROPERTY_VALUE, this);
            fireIntervalAdded(this, index, index);
            childTagList.setSelectionInterval(index, index);
        }

        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
        {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        }

// --------------------- Interface ListSelectionListener ---------------------

        @Override
        public void contentsChanged(ListDataEvent e)
        {
            fireContentsChanged(this, e.getIndex0(), e.getIndex1());
        }

// --------------------- Interface PropertyChangeListener ---------------------

        @Override
        public void intervalAdded(ListDataEvent e)
        {
            fireIntervalAdded(this, e.getIndex0(), e.getIndex1());
        }

// -------------------------- OTHER METHODS --------------------------

        @Override
        public void intervalRemoved(ListDataEvent e)
        {
            int index = e.getIndex0();
            fireIntervalRemoved(this, index, e.getIndex1());
            final int size = getSize();
            if (size > 0) {
                index = Math.min(size - 1, index);
                childTagList.setSelectionInterval(index, index);
            }
        }

        public void moveCurrentElementDown()
        {
            final PresentableTagSortingSetting setting = tagListModel.getCurrentElement();
            if (setting == null || childTagSelectionIndex == null || isLastSelected()) {
                return;
            }
            final int anchor = Math.min(setting.getOrder().size() - 1, childTagSelectionIndex + 1);
            final StringHolder removed = setting.removeChildTag(childTagSelectionIndex);
            setting.addChildTag(anchor, removed.getValue());
            childTagList.setSelectionInterval(anchor, anchor);
        }

        public void moveCurrentElementUp()
        {
            final PresentableTagSortingSetting setting = tagListModel.getCurrentElement();
            if (setting == null || childTagSelectionIndex == null || isFirstSelected()) {
                return;
            }
            final int anchor = Math.max(0, childTagSelectionIndex - 1);
            setting.addChildTag(anchor, setting.removeChildTag(childTagSelectionIndex).getValue());
            childTagList.setSelectionInterval(anchor, anchor);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (tagListModel.equals(evt.getSource()) && TagListModel.PROPERTY_CURRENT_ELEMENT.equals(evt.getPropertyName())) {
                fireContentsChanged(this, 0, -1);
                final Object oldValue = evt.getOldValue();
                if (oldValue != null && oldValue instanceof PresentableTagSortingSetting) {
                    ((PresentableTagSortingSetting) oldValue).removeOrderListener(this);
                }
                final Object newValue = evt.getNewValue();
                if (newValue != null && newValue instanceof PresentableTagSortingSetting) {
                    ((PresentableTagSortingSetting) newValue).addOrderListener(this);
                    for (StringHolder holder : ((PresentableTagSortingSetting) newValue).getOrder()) {
                        holder.addPropertyChangeListener(StringHolder.PROPERTY_VALUE, this);
                    }
                }
                childTagList.getSelectionModel().clearSelection();
            } else if (evt.getSource() instanceof StringHolder) {
                for (int i = 0; i < getSize(); i++) {
                    if (getElementAt(i).equals(evt.getSource())) {
                        fireContentsChanged(this, i, i);
                        return;
                    }
                }
            }
        }

        public void removeCurrentElement()
        {
            final PresentableTagSortingSetting setting = tagListModel.getCurrentElement();
            if (setting != null && childTagSelectionIndex != null) {
                setting.getOrder().get(childTagSelectionIndex).removePropertyChangeListener(StringHolder.PROPERTY_VALUE, this);
                setting.removeChildTag(childTagSelectionIndex);
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting()) {
                setChildTagSelectionIndex(childTagList.isSelectionEmpty() ? null : ((ListSelectionModel) e.getSource()).getLeadSelectionIndex());
            }
        }

        private boolean isFirstSelected()
        {
            final PresentableTagSortingSetting setting = tagListModel.getCurrentElement();
            return setting != null && childTagSelectionIndex != null && childTagSelectionIndex.equals(0);
        }

        private boolean isLastSelected()
        {
            final PresentableTagSortingSetting setting = tagListModel.getCurrentElement();
            return setting != null && childTagSelectionIndex != null && childTagSelectionIndex.equals(getSize() - 1);
        }

        private void setChildTagSelectionIndex(Integer childTagSelectionIndex)
        {
            Object oldValue = this.childTagSelectionIndex;
            this.childTagSelectionIndex = childTagSelectionIndex;
            propertyChangeSupport.firePropertyChange(PROPERTY_CURRENT_ELEMENT, oldValue, this.childTagSelectionIndex);
        }
    }

    public static class PresentableTagSortingSetting implements Comparable<PresentableTagSortingSetting> {
// ------------------------------ FIELDS ------------------------------

        public static final String PROPERTY_MODE = "mode";

        public static final String PROPERTY_NAME = "name";

        private final NullComparator nullComparator = new NullComparator();

        private final List<StringHolder> order;

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        private final List<StringHolder> unmodifiableOrder;

        private String attributeName;

        private List<ListDataListener> listeners = new ArrayList<ListDataListener>();

        private PomSorter.SortMode mode;

        private String name;

        private PomSorter.TagSortingSetting setting;

// --------------------------- CONSTRUCTORS ---------------------------

        public PresentableTagSortingSetting(PomSorter.TagSortingSetting setting)
        {
            this.setting = setting;
            setMode(setting.getMode());
            setName(setting.getName());
            setAttributeName(setting.getAttributeName());
            order = new ArrayList<StringHolder>();
            for (String element : setting.getOrder()) {
                order.add(new StringHolder(element));
            }
            unmodifiableOrder = Collections.unmodifiableList(order);
        }

        public PresentableTagSortingSetting(String name)
        {
            setName(name);
            setMode(PomSorter.SortMode.ALPHABETIC);
            order = new ArrayList<StringHolder>();
            unmodifiableOrder = Collections.unmodifiableList(order);
        }

        public String getAttributeName()
        {
            return attributeName;
        }

// --------------------- GETTER / SETTER METHODS ---------------------

        public void setAttributeName(String attributeName)
        {
            this.attributeName = attributeName;
        }

        public PomSorter.SortMode getMode()
        {
            return mode;
        }

        public void setMode(PomSorter.SortMode mode)
        {
            Object oldValue = this.mode;
            this.mode = mode;
            propertyChangeSupport.firePropertyChange(PROPERTY_MODE, oldValue, this.mode);
        }

// ------------------------ CANONICAL METHODS ------------------------

        public String getName()
        {
            return name;
        }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Comparable ---------------------

        public void setName(String name)
        {
            Object oldValue = this.name;
            this.name = name;
            propertyChangeSupport.firePropertyChange(PROPERTY_NAME, oldValue, this.name);
        }

// -------------------------- OTHER METHODS --------------------------

        public List<StringHolder> getOrder()
        {
            return unmodifiableOrder;
        }

        public void addChildTag(int index, String tag)
        {
            order.add(index, new StringHolder(tag));
            final ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index);
            for (ListDataListener listener : listeners) {
                listener.intervalAdded(event);
            }
        }

        public void addOrderListener(ListDataListener listener)
        {
            listeners.add(listener);
        }

        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
        {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        }

        @Override
        public int compareTo(PresentableTagSortingSetting o)
        {
            return nullComparator.compare(getName(), o.getName());
        }

        public StringHolder removeChildTag(int index)
        {
            final StringHolder removed = order.remove(index);
            final ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
            for (ListDataListener listener : listeners) {
                listener.intervalRemoved(event);
            }
            return removed;
        }

        public void removeOrderListener(ListDataListener listener)
        {
            listeners.remove(listener);
        }

        public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
        {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }

        @Override
        public String toString()
        {
            return getName();
        }

        public void updateModel()
        {
            if (setting == null) {
                setting = new PomSorter.TagSortingSetting();
            }
            setting.setMode(getMode());
            setting.setName(getName());
            setting.setAttributeName(getAttributeName());
            final List<String> strings = new ArrayList<String>();
            for (StringHolder holder : getOrder()) {
                strings.add(holder.getValue());
            }
            setting.setOrder(strings);
        }
    }

    private static class StringHolder {
// ------------------------------ FIELDS ------------------------------

        public static final String PROPERTY_VALUE = "value";

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        private String value;

// --------------------------- CONSTRUCTORS ---------------------------

        public StringHolder(String value)
        {
            this.value = value;
        }

// --------------------- GETTER / SETTER METHODS ---------------------

        public String getValue()
        {
            return value;
        }

// ------------------------ CANONICAL METHODS ------------------------

        public void setValue(String value)
        {
            Object oldValue = this.value;
            this.value = value;
            propertyChangeSupport.firePropertyChange(PROPERTY_VALUE, oldValue, this.value);
        }

// -------------------------- OTHER METHODS --------------------------

        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
        {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        }

        public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
        {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }

        @Override
        public String toString()
        {
            return value;
        }
    }

    private static class TagListModel extends AbstractListModel implements PropertyChangeListener, ListSelectionListener {
// ------------------------------ FIELDS ------------------------------

        public static final String PROPERTY_CURRENT_ELEMENT = "currentElement";

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        private final JList tagList;

        private Integer currentElementIndex;

        private PresentableTagSortingSetting settingBeingRemoved;

        private List<PresentableTagSortingSetting> settingList = new ArrayList<PresentableTagSortingSetting>();

// --------------------------- CONSTRUCTORS ---------------------------

        private TagListModel(JList tagList)
        {
            this.tagList = tagList;
            tagList.getSelectionModel().addListSelectionListener(this);
            tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ListModel ---------------------

        public PresentableTagSortingSetting getCurrentElement()
        {
            return currentElementIndex == null ? null : getElementAt(currentElementIndex);
        }

        @Override
        public PresentableTagSortingSetting getElementAt(int index)
        {
            if (settingList.size() <= index) {
                if (settingBeingRemoved != null) {
                    return settingBeingRemoved;
                }
                return null; //We should log this, as this is unexpected state
            }
            return settingList.get(index);
        }

// --------------------- Interface ListSelectionListener ---------------------

        @Override
        public int getSize()
        {
            return settingList.size();
        }

// --------------------- Interface PropertyChangeListener ---------------------

        public void add(PresentableTagSortingSetting setting)
        {
            add(currentElementIndex == null ? settingList.size() : currentElementIndex, setting);
        }

// -------------------------- OTHER METHODS --------------------------

        public synchronized void add(int index, PresentableTagSortingSetting setting)
        {
            settingList.add(index, setting);
            setting.addPropertyChangeListener(PresentableTagSortingSetting.PROPERTY_NAME, this);
            Collections.sort(settingList);
            for (int i = 0, tagsSize = settingList.size(); i < tagsSize; i++) {
                PresentableTagSortingSetting indexedSetting = settingList.get(i);
                if (indexedSetting.equals(setting)) {
                    index = i;
                }
            }
            fireIntervalAdded(this, index, index);
            tagList.setSelectionInterval(index, index);
        }

        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
        {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        }

        public void clear()
        {
            for (int i = settingList.size() - 1; i >= 0; i--) {
                remove(i);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (PresentableTagSortingSetting.PROPERTY_NAME.equals(evt.getPropertyName())) {
                for (int i = 0, tagsSize = settingList.size(); i < tagsSize; i++) {
                    PresentableTagSortingSetting setting = settingList.get(i);
                    if (setting.equals(evt.getSource())) {
                        fireContentsChanged(this, i, i);
                        return;
                    }
                }
            }
        }

        public synchronized PresentableTagSortingSetting remove(int index)
        {
            settingBeingRemoved = settingList.remove(index);
            settingBeingRemoved.removePropertyChangeListener(PresentableTagSortingSetting.PROPERTY_NAME, this);
            fireIntervalRemoved(this, index, index);
            final PresentableTagSortingSetting setting = settingBeingRemoved;
            settingBeingRemoved = null;
            return setting;
        }

        public void removeCurrentElement()
        {
            if (currentElementIndex == null) {
                return;
            }
            remove(currentElementIndex);
        }

        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting()) {
                if (tagList.isSelectionEmpty()) {
                    setCurrentElementIndex(null);
                } else {
                    final int selectionIndex = ((ListSelectionModel) e.getSource()).getLeadSelectionIndex();
                    setCurrentElementIndex(selectionIndex);
                }
            }
        }

        private void setCurrentElementIndex(Integer currentElementIndex)
        {
            Object oldValue = this.currentElementIndex == null ? null : getElementAt(this.currentElementIndex);
            this.currentElementIndex = currentElementIndex;
            Object newValue = this.currentElementIndex == null ? null : getElementAt(this.currentElementIndex);
            propertyChangeSupport.firePropertyChange(PROPERTY_CURRENT_ELEMENT, oldValue, newValue);
        }
    }

    private class CurrentSettingPropertyChangeListener implements PropertyChangeListener {
// ------------------------------ FIELDS ------------------------------

        private PresentableTagSortingSetting currentSetting;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PropertyChangeListener ---------------------

        public void attachTo(PresentableTagSortingSetting setting)
        {
            if (currentSetting != null) {
                currentSetting.removePropertyChangeListener(PresentableTagSortingSetting.PROPERTY_NAME, this);
                currentSetting.removePropertyChangeListener(PresentableTagSortingSetting.PROPERTY_MODE, this);
            }
            this.currentSetting = setting;
            if (this.currentSetting != null) {
                currentSetting.addPropertyChangeListener(PresentableTagSortingSetting.PROPERTY_NAME, this);
                currentSetting.addPropertyChangeListener(PresentableTagSortingSetting.PROPERTY_MODE, this);
            }
        }

// -------------------------- OTHER METHODS --------------------------

        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (PresentableTagSortingSetting.PROPERTY_MODE.equals(evt.getPropertyName())) {
                updateChildControls();
            }
        }
    }
}
                                                                                                          