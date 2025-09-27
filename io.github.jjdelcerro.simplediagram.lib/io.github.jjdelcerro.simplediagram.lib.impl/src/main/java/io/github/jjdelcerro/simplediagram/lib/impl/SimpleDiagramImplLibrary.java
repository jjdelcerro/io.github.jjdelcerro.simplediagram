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

import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLibrary;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLocator;
import org.gvsig.tools.ToolsLibrary;
import org.gvsig.tools.library.AbstractLibrary;
import org.gvsig.tools.library.LibraryException;
import org.gvsig.tools.swing.api.ToolsSwingLibrary;

public class SimpleDiagramImplLibrary extends AbstractLibrary {

    @Override
    public void doRegistration() {
        super.doRegistration();
        registerAsImplementationOf(SimpleDiagramLibrary.class);
        this.require(ToolsLibrary.class);
        this.require(ToolsSwingLibrary.class);
    }

    @Override
    protected void doInitialize() throws LibraryException {
        SimpleDiagramLocator.registerDefaultSimpleDiagramManager(SimpleDiagramManagerImpl.class);
        
        
    }

    @Override
    protected void doPostInitialize() throws LibraryException {
        DataManager dataManager = DALLocator.getDataManager();
        dataManager.registerDataProfile(new SimpleDiagramProfile());
        dataManager.registerDataProfile(new SimpleDiagramAsImageDataProfile());
    }

}
