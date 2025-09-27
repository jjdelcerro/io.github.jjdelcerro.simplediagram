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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.github.jjdelcerro.simplediagram.lib.SimpleDiagram.DiagramType;

/**
 * Clase de utilidad para detectar el tipo de diagrama a partir de un texto de entrada.
 * Soporta la detecciin de diagramas PlantUML y de tipos especificos de Mermaid
 * (Tarta, Barras, Lineas, XY) que el MermaidXChartConverter puede procesar.
 */
public class DiagramDetector {

    /**
     * Enumeracion que define los tipos de diagramas que pueden ser detectados.
     */

    // Patron para detectar el inicio de un bloque YAML en Mermaid.
    private static final Pattern YAML_BLOCK_PATTERN = Pattern.compile(
            "---(?s)(.*?)(?:\\n)?---(.*)", Pattern.DOTALL
    );

    // Patron para detectar el bloque 'init' de Mermaid.
    private static final Pattern INIT_CONFIG_PATTERN = Pattern.compile(
            "%%\\{init:\\s*[^}]+?}\\s*%%"
    );

    // Patron para detectar comentarios de una sola linea en Mermaid.
    private static final Pattern SINGLE_LINE_COMMENT_PATTERN = Pattern.compile(
            "^\\s*%%.*$", Pattern.MULTILINE
    );


    /**
     * Detecta el tipo de diagrama de un texto de entrada.
     * Realiza las siguientes comprobaciones en orden:
     * 1. Detecta si es un diagrama PlantUML.
     * 2. Si no es PlantUML, preprocesa el texto para eliminar bloques YAML/init y comentarios Mermaid.
     * 3. Detecta si es uno de los tipos de diagrama Mermaid soportados.
     * 4. Si no coincide con ninguno, devuelve UNKNOWN.
     *
     * @param diagramText El texto completo del diagrama a analizar.
     * @return El {@code DiagramType} detectado.
     */
    public static DiagramType detectDiagramType(String diagramText) {
        if (diagramText == null || diagramText.trim().isEmpty()) {
            return DiagramType.UNKNOWN;
        }

        String trimmedText = diagramText.trim();

        // 1. Comprobar si es PlantUML
        if (trimmedText.startsWith("@startuml") && trimmedText.endsWith("@enduml")) {
            return DiagramType.PLANTUML;
        }

        // 2. Si no es PlantUML, preprocesar el texto para la deteccion de Mermaid.
        // Eliminar bloques YAML
        Matcher yamlMatcher = YAML_BLOCK_PATTERN.matcher(trimmedText);
        String cleanedText = trimmedText;
        if (yamlMatcher.find()) {
            // El group(2) contiene el texto despues del bloque YAML
            cleanedText = yamlMatcher.group(2).trim();
        }

        // Eliminar bloques init
        Matcher initMatcher = INIT_CONFIG_PATTERN.matcher(cleanedText);
        if (initMatcher.find()) {
            cleanedText = initMatcher.replaceAll("").trim();
        }

        // Eliminar comentarios de una sola linea (%% comentario)
        cleanedText = SINGLE_LINE_COMMENT_PATTERN.matcher(cleanedText).replaceAll("").trim();


        // 3. Detectar tipo de grifico Mermaid
        String[] lines = cleanedText.split("\\n");
        if (lines.length == 0) {
            return DiagramType.UNKNOWN;
        }

        // Encontrar la primera linea no vacia que podria ser el tipo de diagrama
        String chartTypeLine = null;
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                chartTypeLine = trimmedLine;
                break;
            }
        }

        if (chartTypeLine == null) {
            return DiagramType.UNKNOWN;
        }

        if (chartTypeLine.startsWith("pie")) {
            return DiagramType.MERMAID_PIE;
        } else if (chartTypeLine.startsWith("barChart")) {
            return DiagramType.MERMAID_BAR;
        } else if (chartTypeLine.startsWith("lineChart")) {
            return DiagramType.MERMAID_LINE;
        } else if (chartTypeLine.startsWith("xychart-beta")) {
            return DiagramType.MERMAID_XY;
        }

        // 4. Si no es ninguno de los anteriores
        return DiagramType.UNKNOWN;
    }

    /**
     * Mitodo main para probar la funcionalidad de detecciin de diagramas.
     * Incluye varios ejemplos de diferentes tipos de diagramas.
     */
    public static void main(String[] args) {
        // Ejemplo de PlantUML
        String plantUMLText =
                "@startuml\n" +
                "Alice -> Bob: Authentication Request\n" +
                "Bob --> Alice: Authentication Response\n" +
                "@enduml";
        System.out.println("Texto de PlantUML: " + DiagramDetector.detectDiagramType(plantUMLText)); // Esperado: PLANTUML

        // Ejemplo de Mermaid Pie Chart con YAML
        String mermaidPie =
                "---\n" +
                "title: Ventas\n" +
                "---\n" +
                "pie\n" +
                "\"Apples\": 42.96\n" +
                "\"Bananas\": 50.58";
        System.out.println("Texto de Mermaid Pie: " + DiagramDetector.detectDiagramType(mermaidPie)); // Esperado: MERMAID_PIE

        // Ejemplo de Mermaid Bar Chart con init
        String mermaidBar =
                "%%{init: {'theme': 'dark'} }%%\n" +
                "barChart\n" +
                "xAxis A, B, C\n" +
                "series \"Series1\"\n" +
                "10, 20, 30";
        System.out.println("Texto de Mermaid Bar: " + DiagramDetector.detectDiagramType(mermaidBar)); // Esperado: MERMAID_BAR

        // Ejemplo de Mermaid Line Chart
        String mermaidLine =
                "lineChart\n" +
                "x-axis Jan, Feb, Mar\n" +
                "series \"Data1\"\n" +
                "1, 2, 3";
        System.out.println("Texto de Mermaid Line: " + DiagramDetector.detectDiagramType(mermaidLine)); // Esperado: MERMAID_LINE

        // Ejemplo de Mermaid XY Chart
        String mermaidXY =
                "xychart-beta\n" +
                "x-axis-type numeric\n" +
                "x-axis 0 --> 10\n" +
                "line \"MyLine\"\n" +
                "1, 2, 3\n" +
                "10, 20, 30";
        System.out.println("Texto de Mermaid XY: " + DiagramDetector.detectDiagramType(mermaidXY)); // Esperado: MERMAID_XY

        // Ejemplo de texto desconocido
        String unknownText = "Esto es un texto que no es un diagrama.";
        System.out.println("Texto desconocido: " + DiagramDetector.detectDiagramType(unknownText)); // Esperado: UNKNOWN

        // Ejemplo de texto vacio
        String emptyText = "";
        System.out.println("Texto vacio: " + DiagramDetector.detectDiagramType(emptyText)); // Esperado: UNKNOWN

        // Ejemplo de Mermaid con comentarios
        String mermaidComments =
                "%% Este es un comentario\n" +
                "pie\n" +
                "%% Otro comentario\n" +
                "\"A\": 10";
        System.out.println("Mermaid con comentarios: " + DiagramDetector.detectDiagramType(mermaidComments)); // Esperado: MERMAID_PIE
    }
}
