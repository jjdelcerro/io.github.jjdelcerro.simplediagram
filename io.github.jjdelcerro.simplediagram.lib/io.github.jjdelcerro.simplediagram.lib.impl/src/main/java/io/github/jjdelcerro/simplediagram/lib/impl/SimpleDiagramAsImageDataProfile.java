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

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.gvsig.fmap.dal.feature.AbstractDataProfile;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLocator;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramManager;
import org.gvsig.tools.dynobject.Tags;
import org.gvsig.tools.swing.api.SimpleImage;

class SimpleDiagramAsImageDataProfile extends AbstractDataProfile {

  /*friend*/ SimpleDiagramAsImageDataProfile() {
    super("Image", SimpleImage.class);
  }

  @Override
  protected Object doCreateData(Object diagramDefinition, Tags tags) {
    try {
      SimpleDiagramManager manager = SimpleDiagramLocator.getSimpleDiagramManager();
      String s = Objects.toString(diagramDefinition, null);
      if (StringUtils.isBlank(s)) {
        return null;
      }
      SimpleDiagram x = manager.createSimpleDiagram(s);
      return x.getSimpleImage();
    } catch (Exception ex) {
      LOGGER.debug("Can't get Image of SimpleDiagram", ex);
    }
    return null;
  }

}
