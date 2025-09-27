/*
 * Copyright (C) 2025 Joaquin del Cerro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.jjdelcerro.simplediagram.swing.impl.viewer;

import java.awt.BorderLayout;
import java.nio.charset.StandardCharsets;
import javax.swing.JComponent;
import javax.swing.JPanel;
import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramViewer;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram;
import static io.github.jjdelcerro.simplediagram.lib.SimpleDiagram.DiagramType.MERMAID_BAR;
import static io.github.jjdelcerro.simplediagram.lib.SimpleDiagram.DiagramType.MERMAID_LINE;
import static io.github.jjdelcerro.simplediagram.lib.SimpleDiagram.DiagramType.MERMAID_PIE;
import static io.github.jjdelcerro.simplediagram.lib.SimpleDiagram.DiagramType.MERMAID_XY;
import static io.github.jjdelcerro.simplediagram.lib.SimpleDiagram.DiagramType.PLANTUML;
import io.github.jjdelcerro.simplediagram.lib.impl.SimpleDiagramImpl;
import org.gvsig.tools.swing.api.SimpleImage;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.viewer.AbstractJViewer;
import org.gvsig.tools.swing.api.viewer.JViewer;
import org.gvsig.tools.swing.api.viewer.ViewerFactory;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.internal.chartpart.Chart;
import org.slf4j.LoggerFactory;

public class SimpleDiagramViewerImpl extends AbstractJViewer<SimpleDiagram> implements SimpleDiagramViewer {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SimpleDiagramViewerImpl.class);

  private SimpleDiagramImpl diagram;
  private XChartPanel chartPanel;
  private JPanel panel;
  private int mode;

  public SimpleDiagramViewerImpl() {
    this(new SimpleDiagramViewerFactory());
  }

  public SimpleDiagramViewerImpl(ViewerFactory factory) {
    super(factory);
    this.panel = new JPanel();
    this.diagram = null;
    this.chartPanel = null;
    this.mode = SimpleDiagramViewer.MODE_LIGHT;
    this.panel.setLayout(new BorderLayout());
  }

  @Override
  public void setMode(int mode) {
    this.mode = mode;
  }

  public SimpleDiagram getSimpleDiagram() {
    return this.diagram;
  }

  @Override
  public JComponent asJComponent() {
    return this.panel;
  }

  @Override
  public void clean() {
    this.panel.removeAll();
    this.panel.repaint();
  }

  @Override
  public void setContents(SimpleDiagram data) {
    this.diagram = (SimpleDiagramImpl) data;
    this.panel.removeAll();

    switch (this.diagram.getType()) {
      case PLANTUML:
        SimpleImage image = this.diagram.getSimpleImage();
        if( image!=null ) {
          ViewerFactory viewerFactory = ToolsSwingLocator.getToolsSwingManager().getViewerFactory("ImageViewer");
          JViewer theViewer = viewerFactory.createViewer();
          theViewer.setContents(image);
          this.panel.add(theViewer.asJComponent(), BorderLayout.CENTER);
        }
        break;
      case MERMAID_PIE:
      case MERMAID_BAR:
      case MERMAID_LINE:
      case MERMAID_XY:
        if (this.diagram != null) {
          Chart xchart = this.diagram.getChart();
          if (xchart != null) {
            this.chartPanel = new XChartPanel(xchart);
            this.panel.add(this.chartPanel, BorderLayout.CENTER);
          }
        }
        break;
      case UNKNOWN:
      default:
        break;
    }
    this.panel.repaint();
  }

  @Override
  public SimpleDiagram getContents() {
    return this.diagram;
  }

  @Override
  public byte[] getBytes() {
    if( this.diagram == null ) {
      return null;
    }
    return this.diagram.getDefinition().getBytes(StandardCharsets.UTF_8);
  }

}
