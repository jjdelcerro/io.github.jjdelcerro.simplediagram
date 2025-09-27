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

package io.github.jjdelcerro.diagram.app.mainplugin;

import java.util.Objects;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.gvsig.andami.plugins.Extension;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLocator;
import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramSwingLocator;
import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramSwingManager;
import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramViewer;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.arguments.Arguments;
import org.gvsig.tools.exception.BaseException;
import org.gvsig.tools.i18n.I18nManager;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;

/**
 *
 * @author jjdelcerro
 */
public class DiagramExtension extends Extension {

  @Override
  public void initialize() {

  }

  @Override
  public void postInitialize() {
  }

  @Override
  public void execute(String action) {
  }

  @Override
  public void execute(String command, Object[] args) {
    if (StringUtils.equalsIgnoreCase(command, "show-diagram")) {
      Arguments arguments = Arguments.create(args);
      String title = (String) arguments.get("title", "Diagram");
      WindowManager.MODE mode = (WindowManager.MODE) arguments.get("mode", WindowManager.MODE.WINDOW);
      if (arguments.contains("diagram")) {
        SimpleDiagram diagram = (SimpleDiagram) arguments.get("diagram");
        showDiagram(diagram, title, mode);
        return;
      }
      if (arguments.contains("source")) {
        Object source = arguments.get("source");
        showDiagram(Objects.toString(source, null), title, mode);
        return;
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isVisible() {
    return false;
  }

  public void showDiagram(String definition, String title, WindowManager.MODE mode) {
    SimpleDiagram diagram = SimpleDiagramLocator.getSimpleDiagramManager().createSimpleDiagram(definition);
    showDiagram(diagram, title, mode);
  }

  public void showDiagram(SimpleDiagram diagram, String title, WindowManager.MODE mode) {
    try {

      final SimpleDiagramSwingManager simpleDiagramSwingManager = SimpleDiagramSwingLocator.getSimpleDiagramSwingManager();
      SimpleDiagramViewer viewer = simpleDiagramSwingManager.createSimpleDiagramViewer(diagram);
      viewer.setMode(SimpleDiagramViewer.MODE_LIGHT);
      WindowManager winManager = ToolsSwingLocator.getWindowManager();
      winManager.showWindow(viewer.asJComponent(), title, mode);
    } catch (Exception ex) {
      logger.warn("Can't show Diagram.'", ex);
      I18nManager i18n = ToolsLocator.getI18nManager();
      ThreadSafeDialogsManager dialogs = ToolsSwingLocator.getThreadSafeDialogsManager();
      dialogs.messageDialog(
        i18n.getTranslation("_Cant_show_diagram") + ")\n\n" + BaseException.getMessageStack(ex, 0),
        i18n.getTranslation("_Show_diagram"),
        JOptionPane.WARNING_MESSAGE
      );
    }

  }
}
