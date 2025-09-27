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

package io.github.jjdelcerro.simplediagram.swing.impl.dynform;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;
import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramViewer;
import io.github.jjdelcerro.simplediagram.swing.impl.viewer.SimpleDiagramViewerImpl;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLocator;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.dynform.DynFormFieldDefinition;
import org.gvsig.tools.dynform.services.dynformfield.File.JDynFormFieldFile;
import org.gvsig.tools.dynform.spi.DynFormSPIManager;
import org.gvsig.tools.dynform.spi.dynformfield.JDynFormFieldFactory;
import org.gvsig.tools.dynobject.Tags;

public class JDynFormFieldSimpleDiagramFile extends JDynFormFieldFile {

  private SimpleDiagramViewer viewer;
  private JComponent fileContents;

  public JDynFormFieldSimpleDiagramFile(
    DynFormSPIManager serviceManager,
    DynFormSPIManager.ComponentsFactory componentsFactory,
    JDynFormFieldFactory factory,
    DynFormFieldDefinition definition,
    Object value
  ) {
    super(serviceManager, componentsFactory, factory, definition, value);
  }

  @Override
  public void setReadOnly(boolean readonly) {
    super.setReadOnly(readonly);
  }

  @Override
  public void initComponent() {
    if (this.fileContents != null) {
      return;
    }
    super.initComponent();
    this.fileContents = this.contents;

    this.viewer = new SimpleDiagramViewerImpl();
    JPanel panelButtons;
    panelButtons = (JPanel) this.fileContents;

    this.contents = new JPanel();
    this.contents.setLayout(new BorderLayout());
    this.contents.add(panelButtons, BorderLayout.NORTH);
    this.contents.add(this.viewer.asJComponent(), BorderLayout.CENTER);

    this.setReadOnly(this.readOnly);
  }

  @Override
  public void setValue(Object value) {
    super.setValue(value);
    if (this.viewer != null) {
      SimpleDiagram diagram = SimpleDiagramLocator.getSimpleDiagramManager().createSimpleDiagram(Objects.toString(value, null));
      this.viewer.setContents(diagram);
    }
  }

  @Override
  public void clear() {
    super.clear();
    if (this.viewer != null) {
      this.viewer.clean();
    }
  }

  @Override
  protected Object getAbsoluteFile(Object value) {
    // TODO: Eliminar este metodo para org.gvsig.desktop > 2.0.260, esta en la clase padre.        
    if (!(value instanceof File)) {
      return value;
    }
    File f = (File) value;
    if (!f.isAbsolute()) {
      Tags tags = this.getDefinition().getTags();
      if (tags.has("path")) {
        File folder;
        if (tags.get("path") instanceof File) {
          folder = (File) tags.get("path");
        } else {
          folder = new File(Objects.toString(tags.get("path"), null));
        }
        File f2 = new File(folder, f.getPath());
        if (f2.exists()) {
          f = f2;
        } else {
          folder = ToolsLocator.getFoldersManager().get("Project");
          if (folder != null) {
            f = new File(folder, f.getPath());
          }
        }
      }
    }
    return f;
  }
}
