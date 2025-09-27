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

package io.github.jjdelcerro.simplediagram.swing.impl;

import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramSwingManager;
import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramViewer;
import io.github.jjdelcerro.simplediagram.swing.impl.viewer.SimpleDiagramViewerImpl;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLocator;

/**
 *
 * @author jjdelcerro
 */
public class SimpleDiagramSwingManagerImpl implements SimpleDiagramSwingManager {

  @Override
  public SimpleDiagramViewer createSimpleDiagramViewer() {
    SimpleDiagramViewerImpl x = new SimpleDiagramViewerImpl();
    return x;
  }

  @Override
  public SimpleDiagramViewer createSimpleDiagramViewer(String definition) {
    SimpleDiagram diagram = SimpleDiagramLocator.getSimpleDiagramManager().createSimpleDiagram(definition);
    SimpleDiagramViewerImpl x = new SimpleDiagramViewerImpl();
    x.setContents(diagram);
    return x;
  }

  @Override
  public SimpleDiagramViewer createSimpleDiagramViewer(SimpleDiagram diagram) {
    SimpleDiagramViewerImpl x = new SimpleDiagramViewerImpl();
    x.setContents(diagram);
    return x;
  }
  
}
