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

import org.gvsig.tools.locator.AbstractLocator;
import org.gvsig.tools.locator.Locator;
import org.gvsig.tools.locator.LocatorException;

@SuppressWarnings("rawtypes")
public class SimpleDiagramLocator extends AbstractLocator {

	private static final String LOCATOR_NAME = "SimpleDiagramLocator";
	
	public static final String SIMPLEDIAGRAM_MANAGER_NAME =
			"SimpleDiagramManager";

	private static final String SIMPLEDIAGRAM_MANAGER_DESCRIPTION =
			"SimpleDiagram Manager for gvSIG desktop";
	
	private static final SimpleDiagramLocator instance = new SimpleDiagramLocator();

	private SimpleDiagramLocator() {

	}

	/**
	 * Return the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static SimpleDiagramLocator getInstance() {
		return instance;
	}

        @Override
	public String getLocatorName() {
		return LOCATOR_NAME;
	}

	/**
	 * Return a reference to DiagramManager.
	 * 
	 * @return a reference to DiagramManager
	 * @throws LocatorException
	 *             if there is no access to the class or the class cannot be
	 *             instantiated
	 * @see Locator#get(String)
	 */
	public static SimpleDiagramManager getSimpleDiagramManager()
			throws LocatorException {
		return (SimpleDiagramManager) getInstance().get(SIMPLEDIAGRAM_MANAGER_NAME);
	}

	public static void registerDefaultSimpleDiagramManager(Class clazz) {
		getInstance().registerDefault(SIMPLEDIAGRAM_MANAGER_NAME,
				SIMPLEDIAGRAM_MANAGER_DESCRIPTION, clazz);
	}


}
