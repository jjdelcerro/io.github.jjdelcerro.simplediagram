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

package io.github.jjdelcerro.simplediagram.lib.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram.DiagramType;
import static io.github.jjdelcerro.simplediagram.lib.SimpleDiagram.DiagramType.*;
import org.slf4j.LoggerFactory;
import org.gvsig.tools.swing.api.SimpleImage;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.ToolsSwingManager;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.internal.chartpart.Chart;

/**
 *
 * @author jjdelcerro
 */
@SuppressWarnings("UseSpecificCatch")
public class SimpleDiagramImpl implements SimpleDiagram {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SimpleDiagramImpl.class);

  private final String definition;
  private DiagramType type;
  private Chart chart;

  public SimpleDiagramImpl(String definition) {
    this.definition = definition;
    this.type = DiagramDetector.detectDiagramType(definition);
  }

  @Override
  public DiagramType getType() {
    return this.type;
  }

  @Override
  public SimpleImage getSimpleImage() {
    return this.getSimpleImage(800, 600);
  }

  public SimpleImage getSimpleImage(int width, int height) {
    switch (this.type) {
      case PLANTUML:
        return generatePlantUMLAsSimpleImage(this.definition);
      case MERMAID_PIE:
      case MERMAID_BAR:
      case MERMAID_LINE:
      case MERMAID_XY:
        return generateChartAsSimpleImage(this.getChart());
      case UNKNOWN:
      default:
        return null;
    }
  }

  @Override
  public String getDefinition() {
    return this.definition;
  }

  public Chart getChart() {
    if (this.chart == null) {
      MermaidXChartConverter converter = new MermaidXChartConverter();
      this.chart = converter.parseMermaidToXChart(this.definition);
    }
    return this.chart;
  }

  private static SimpleImage generatePlantUMLAsSimpleImage(String plantUMLSource) {
    try {
      ToolsSwingManager manager = ToolsSwingLocator.getToolsSwingManager();
      BufferedImage image = generatePlantUMLAsBufferedImage(plantUMLSource);
      SimpleImage simpleImage = manager.createSimpleImage(image);
      return simpleImage;
    } catch (IOException ex) {
      return null;
    }
  }

  private static SimpleImage generateChartAsSimpleImage(Chart chart) {
      ToolsSwingManager manager = ToolsSwingLocator.getToolsSwingManager();
      BufferedImage image = BitmapEncoder.getBufferedImage(chart);
      SimpleImage simpleImage = manager.createSimpleImage(image);
      return simpleImage;
  }
  
  
  private static BufferedImage generatePlantUMLAsBufferedImage(String plantUMLSource) throws IOException {
    SourceStringReader reader = new SourceStringReader(plantUMLSource);
    ByteArrayOutputStream os = new ByteArrayOutputStream(); // Stream en memoria para capturar los bytes de la imagen

    // Genera la imagen en formato PNG en el ByteArrayOutputStream
    // outputImage devuelve un Result de PlantUML, podemos ignorarlo si solo nos interesa el stream.
    reader.outputImage(os, new FileFormatOption(FileFormat.PNG));

    // Convierte los bytes del ByteArrayOutputStream a un BufferedImage
    try (ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray())) {
      return ImageIO.read(is);
    }
  }
}
