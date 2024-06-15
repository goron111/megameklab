/*
 * Copyright (c) 2008 - jtighe (torren@users.sourceforge.net)
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMekLab.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megameklab.ui.generalUnit;

import megamek.client.ui.panes.ConfigurableMechViewPanel;
import megamek.client.ui.swing.MechViewPanel;
import megamek.client.ui.swing.alphaStrike.ConfigurableASCardPanel;
import megamek.client.ui.swing.calculationReport.FlexibleCalculationReport;
import megamek.common.Entity;
import megamek.common.ViewFormatting;
import megamek.common.alphaStrike.conversion.ASConverter;
import megamek.common.templates.TROView;
import megameklab.ui.EntitySource;
import megameklab.ui.util.ITab;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class PreviewTab extends ITab {

    private final ConfigurableMechViewPanel panelMekView = new ConfigurableMechViewPanel();
    private final MechViewPanel panelTROView = new MechViewPanel();
    private final ConfigurableASCardPanel cardPanel = new ConfigurableASCardPanel(null);

    public PreviewTab(EntitySource eSource) {
        super(eSource);
        setLayout(new BorderLayout());
        JTabbedPane panPreview = new JTabbedPane();
        panPreview.addTab("Summary", panelMekView);
        panPreview.addTab("TRO", panelTROView);
        panPreview.addTab("AS Card", cardPanel);
        add(panPreview, BorderLayout.CENTER);
        addComponentListener(refreshOnShow);
    }

    public void update() {
        boolean populateTextFields = true;
        final Entity selectedUnit = eSource.getEntity();
        selectedUnit.recalculateTechAdvancement();
        TROView troView = null;
        try {
            troView = TROView.createView(selectedUnit, ViewFormatting.HTML);
        } catch (Exception e) {
            LogManager.getLogger().error("", e);
            // error unit didn't load right. this is bad news.
            populateTextFields = false;
        }
        if (populateTextFields) {
            panelMekView.setEntity(selectedUnit);
            panelTROView.setMech(selectedUnit, troView);
            if (ASConverter.canConvert(selectedUnit)) {
                cardPanel.setASElement(ASConverter.convertInMML(selectedUnit, new FlexibleCalculationReport()));
            } else {
                cardPanel.setASElement(null);
            }
        } else {
            panelMekView.reset();
            panelTROView.reset();
            cardPanel.setASElement(null);
        }
    }

    public void refresh() {
        // This active refresh is needed for the few cases where the unit can be changed when the preview is
        // active, e.g. setting the fluff image.
        if (isVisible()) {
            update();
        }
    }

    ComponentListener refreshOnShow = new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent e) {
            update();
        }
    };
}