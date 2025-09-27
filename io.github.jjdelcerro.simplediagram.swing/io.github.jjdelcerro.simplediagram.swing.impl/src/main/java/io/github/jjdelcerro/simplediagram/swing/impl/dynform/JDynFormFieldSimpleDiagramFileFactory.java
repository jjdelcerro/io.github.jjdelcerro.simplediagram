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

import java.net.URL;
import org.gvsig.tools.dataTypes.DataTypes;
import org.gvsig.tools.dynform.DynFormFieldDefinition;
import org.gvsig.tools.dynform.JDynFormField;
import org.gvsig.tools.dynform.spi.DynFormSPILocator;
import org.gvsig.tools.dynform.spi.DynFormSPIManager;
import org.gvsig.tools.dynform.spi.dynformfield.AbstractJDynFormFieldFactory;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.icontheme.IconTheme;

public class JDynFormFieldSimpleDiagramFileFactory extends AbstractJDynFormFieldFactory {

    public JDynFormFieldSimpleDiagramFileFactory() {
        super("CHART", DataTypes.FILE, "chart");
    }

    @Override
    public JDynFormField create(
            DynFormSPIManager serviceManager,
            DynFormSPIManager.ComponentsFactory componentsFactory,
            DynFormFieldDefinition fieldDefinition,
            Object value
    ) {
        return new JDynFormFieldSimpleDiagramFile(serviceManager, componentsFactory, this, fieldDefinition, value);
    }
    
    public static void selfRegister() {
        DynFormSPIManager manager = DynFormSPILocator.getDynFormSPIManager();
        manager.registerDynFieldFactory(new JDynFormFieldSimpleDiagramFileFactory());

        IconTheme theme = ToolsSwingLocator.getIconThemeManager().getCurrent();
        URL url = JDynFormFieldSimpleDiagramFileFactory.class.getResource("form-open-pdf.png");

        theme.registerDefault("PDF", "form", "form-open-pdf", null, url);
        
    }
}
