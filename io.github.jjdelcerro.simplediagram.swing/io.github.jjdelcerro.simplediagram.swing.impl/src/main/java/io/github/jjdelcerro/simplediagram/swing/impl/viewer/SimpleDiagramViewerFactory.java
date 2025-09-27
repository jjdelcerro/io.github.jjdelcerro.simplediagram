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

import java.io.File;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLocator;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.ToolsSwingManager;
import org.gvsig.tools.swing.api.viewer.AbstractViewerFactory;
import org.gvsig.tools.swing.api.viewer.JViewer;

/**
 *
 * @author jjdelcerro
 */
@SuppressWarnings("UseSpecificCatch")
public class SimpleDiagramViewerFactory extends AbstractViewerFactory {

  public SimpleDiagramViewerFactory() {
    super("SimpleDiagramViewer", "SimpleDiagram", "text/plain", false);
  }

  @Override
  public JViewer createViewer() {
    return new SimpleDiagramViewerImpl();
  }

  @Override
  public boolean isApplicable(Object... args) {
    Object data = args[0];
    if (data instanceof SimpleDiagram) {
      return true;
    }
    if (data instanceof File) {
      return true;
    }
    if (data instanceof byte[]) {
      try {
        SimpleDiagram chart = SimpleDiagramLocator.getSimpleDiagramManager().createSimpleDiagram(new String((byte[]) data));
        return chart != null;
      } catch (Exception e) {
        return false;
      }
    }
    if (data instanceof String) {
      try {
        SimpleDiagram chart = SimpleDiagramLocator.getSimpleDiagramManager().createSimpleDiagram((String) data);
        return chart != null;
      } catch (Exception e) {
        return false;
      }
    }
    return false;
  }

  public static void selfRegister() {
    ToolsSwingManager manager = ToolsSwingLocator.getToolsSwingManager();
    manager.registerViewer(new SimpleDiagramViewerFactory());
    
  }

}
