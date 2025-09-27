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

import java.io.File;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.gvsig.fmap.dal.feature.AbstractDataProfile;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLocator;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramManager;
import org.gvsig.tools.dataTypes.CoercionException;
import org.gvsig.tools.dataTypes.DataType;
import org.gvsig.tools.dataTypes.DataTypes;
import org.gvsig.tools.dynobject.Tags;



@SuppressWarnings("UseSpecificCatch")
public class SimpleDiagramProfile extends AbstractDataProfile {

    /*friend*/SimpleDiagramProfile() {
        super("SimpleDiagram", File.class);
    }

    @Override
    protected Object doCreateData(Object diagramDefinition, Tags tags) {
      SimpleDiagramManager manager = SimpleDiagramLocator.getSimpleDiagramManager();
      String s = Objects.toString(diagramDefinition, null);
      if( s == null ) {
        return null;
      }
      SimpleDiagram x = manager.createSimpleDiagram(s);
      return x;
    }

    @Override
    protected Object doCoerce(DataType dataType, Object data, Tags tags) throws CoercionException {
        if( data == null ) {
          return null;
        }
        try {
            SimpleDiagram diagram = null;
            if( data instanceof SimpleDiagram ) {
              diagram = (SimpleDiagram) data;
            } else {
              SimpleDiagramManager manager = SimpleDiagramLocator.getSimpleDiagramManager();
              String s = Objects.toString(data, null);
              if( StringUtils.isBlank(s) ) {
                throw new CoercionException("Can't convert SimpleDiagram to " + dataType.getName());
              }
              diagram = manager.createSimpleDiagram(s);
            }
            switch (dataType.getType()) {
                case DataTypes.BYTEARRAY:
                  return diagram.toString().getBytes();
                case DataTypes.FILE:
                    if (data instanceof File) {
                        return data;
                    }
                    break;
                case DataTypes.STRING:
                  return diagram.toString();
                default:
                    break;
            }
        } catch (Exception ex) {
            throw new CoercionException("Can't convert SimpleDiagram to " + dataType.getName(), ex);
        }
        throw new CoercionException("Can't convert SimpleDiagram to " + dataType.getName());
    }

}
