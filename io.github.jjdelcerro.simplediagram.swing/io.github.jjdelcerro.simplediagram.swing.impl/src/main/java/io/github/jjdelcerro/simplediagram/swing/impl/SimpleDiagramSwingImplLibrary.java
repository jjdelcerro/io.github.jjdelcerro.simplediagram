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

import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramSwingLibrary;
import io.github.jjdelcerro.simplediagram.swing.api.SimpleDiagramSwingLocator;
import io.github.jjdelcerro.simplediagram.swing.impl.dynform.JDynFormFieldSimpleDiagramBytearrayFactory;
import io.github.jjdelcerro.simplediagram.swing.impl.dynform.JDynFormFieldSimpleDiagramFileFactory;
import io.github.jjdelcerro.simplediagram.swing.impl.dynform.JDynFormFieldSimpleDiagramURLFactory;
import io.github.jjdelcerro.simplediagram.swing.impl.viewer.SimpleDiagramViewerFactory;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagramLibrary;
import org.gvsig.tools.library.AbstractLibrary;
import org.gvsig.tools.library.LibraryException;


/**
 *
 * @author jjdelcerro
 */
public class SimpleDiagramSwingImplLibrary extends AbstractLibrary {

    @Override
    public void doRegistration() {
        super.doRegistration();
        registerAsImplementationOf(SimpleDiagramSwingLibrary.class);
        this.require(SimpleDiagramLibrary.class);
    }

    @Override
    protected void doInitialize() throws LibraryException {
        SimpleDiagramSwingLocator.registerDefaultSimpleDiagramSwingManager(SimpleDiagramSwingManagerImpl.class);

    }

    @Override
    protected void doPostInitialize() throws LibraryException {
        JDynFormFieldSimpleDiagramFileFactory.selfRegister();
        JDynFormFieldSimpleDiagramURLFactory.selfRegister();
        JDynFormFieldSimpleDiagramBytearrayFactory.selfRegister();

        SimpleDiagramViewerFactory.selfRegister();
    }

}
