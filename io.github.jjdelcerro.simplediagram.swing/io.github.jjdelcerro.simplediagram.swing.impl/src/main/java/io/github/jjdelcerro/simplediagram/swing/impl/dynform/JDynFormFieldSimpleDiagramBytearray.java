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
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;
import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramViewer;
import io.github.jjdelcerro.simplediagram.swing.impl.viewer.SimpleDiagramViewerImpl;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLocator;
import org.gvsig.tools.dynform.DynFormFieldDefinition;
import org.gvsig.tools.dynform.services.dynformfield.Bytearray.JDynFormFieldBytearray;
import org.gvsig.tools.dynform.spi.DynFormSPIManager;
import org.gvsig.tools.dynform.spi.dynformfield.JDynFormFieldFactory;

/**
 *
 * @author jjdelcerro
 */
public class JDynFormFieldSimpleDiagramBytearray extends JDynFormFieldBytearray {

  private SimpleDiagramViewer viewer;
  private JComponent fileContents;

  public JDynFormFieldSimpleDiagramBytearray(
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

}
