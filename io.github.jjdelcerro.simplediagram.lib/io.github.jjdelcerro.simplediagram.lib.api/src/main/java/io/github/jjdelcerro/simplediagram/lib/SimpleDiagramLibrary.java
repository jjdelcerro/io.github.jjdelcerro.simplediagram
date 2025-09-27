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

package io.github.jjdelcerro.simplediagram.lib;

import org.gvsig.tools.ToolsLibrary;
import org.gvsig.tools.library.AbstractLibrary;
import org.gvsig.tools.library.LibraryException;

/**
 *
 * @author jjdelcerro
 */
public class SimpleDiagramLibrary extends AbstractLibrary {

    @Override
    public void doRegistration() {
        super.doRegistration();
        registerAsAPI(SimpleDiagramLibrary.class);
        this.require(ToolsLibrary.class);
    }

    @Override
    protected void doInitialize() throws LibraryException {
    }

    @Override
    protected void doPostInitialize() throws LibraryException {
    }

}
