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

package io.github.jjdelcerro.simplediagram.swing.api;

import org.gvsig.tools.locator.AbstractLocator;
import org.gvsig.tools.locator.Locator;
import org.gvsig.tools.locator.LocatorException;

@SuppressWarnings("rawtypes")
public class SimpleDiagramSwingLocator extends AbstractLocator {

	private static final String LOCATOR_NAME = "SimpleDiagramSwingLocator";
	
	public static final String SIMPLEDIAGRAM_SWING_MANAGER_NAME =
			"org.gvsig.diagram.swing.manager";

	private static final String SIMPLEDIAGRAM_SWING_MANAGER_DESCRIPTION =
			"SimpleDiagram Swing Manager for gvSIG desktop";
	
	private static final SimpleDiagramSwingLocator instance = new SimpleDiagramSwingLocator();

	private SimpleDiagramSwingLocator() {

	}

	/**
	 * Return the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static SimpleDiagramSwingLocator getInstance() {
		return instance;
	}

        @Override
	public String getLocatorName() {
		return LOCATOR_NAME;
	}

	/**
	 * Return a reference to SimpleDiagramSwingManager.
	 * 
	 * @return a reference to SimpleDiagramSwingManager
	 * @throws LocatorException
	 *             if there is no access to the class or the class cannot be
	 *             instantiated
	 * @see Locator#get(String)
	 */
	public static SimpleDiagramSwingManager getSimpleDiagramSwingManager()
			throws LocatorException {
		return (SimpleDiagramSwingManager) getInstance().get(SIMPLEDIAGRAM_SWING_MANAGER_NAME);
	}

	/**
	 * Registers the Class implementing the SimpleDiagramSwingManager interface.
	 * 
	 * @param clazz
	 *            implementing the SimpleDiagramSwingManager interface
	 */
	public static void registerSimpleDiagramSwingManager(Class clazz) {
		getInstance().register(SIMPLEDIAGRAM_SWING_MANAGER_NAME,
				SIMPLEDIAGRAM_SWING_MANAGER_DESCRIPTION, clazz);
	}

	public static void registerDefaultSimpleDiagramSwingManager(Class clazz) {
		getInstance().registerDefault(SIMPLEDIAGRAM_SWING_MANAGER_NAME,
				SIMPLEDIAGRAM_SWING_MANAGER_DESCRIPTION, clazz);
	}


}
