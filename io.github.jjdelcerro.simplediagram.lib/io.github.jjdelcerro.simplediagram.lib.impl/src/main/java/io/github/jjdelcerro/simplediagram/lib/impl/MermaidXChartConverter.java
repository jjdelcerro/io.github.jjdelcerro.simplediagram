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

import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.PieStyler;

import org.knowm.xchart.style.AxesChartStyler; // Importacion crucial para acceder a los metodos de estilo de los ejes
import org.knowm.xchart.internal.chartpart.Chart; // Importacion para la interfaz base de los Charts

import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.StringReader;
import java.util.stream.Collectors;

// Importacion necesaria para SwingWrapper para mostrar los graficos
import org.knowm.xchart.SwingWrapper;


/**
 * Clase para convertir la sintaxis de graficos Mermaid a objetos Chart de la libreria XChart.
 * Soporta graficos de Tarta, Barras, Lineas y xychart-beta, incluyendo opciones avanzadas como
 * bloques YAML para configuracion, mezcla de tipos de graficos y rangos explicitos en los ejes.
 * 
 * <pre>
(* Elementos Lexicos Basicos *)
<DIGITO> ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
<LETRA> ::= "a" | ... | "z" | "A" | ... | "Z"
<CARACTER_CUALQUIERA> ::= (* Cualquier caracter Unicode (excluyendo saltos de linea para CADENA_TEXTO sin comillas) *)
<ESPACIO> ::= " "
<NUEVA_LINEA> ::= "\n"
<IDENTIFICADOR> ::= (<LETRA> | <DIGITO> | "_")+
<NUMERO> ::= <DIGITO>+ [ "." <DIGITO>+ ]
<ENTERO> ::= <DIGITO>+
<BOOLEANO> ::= "true" | "false"
<CADENA_TEXTO> ::= "\"" (<CARACTER_CUALQUIERA> - "\"")* "\"" (* Texto entre comillas dobles *)
                  | (<CARACTER_CUALQUIERA> - <NUEVA_LINEA> - "," - ":")+ (* Texto sin comillas para categorias/valores, hasta coma, nueva linea o dos puntos *)

(* Estructura General del Grafico Mermaid *)
<GRAFICO_MERMAID> ::= [ <BLOQUE_YAML> ] [ <BLOQUE_INIT> ] <DEFINICION_GRAFICO>

(* Bloque YAML (opcional) *)
<BLOQUE_YAML> ::= "---" <NUEVA_LINEA> <CONTENIDO_YAML> "---" <NUEVA_LINEA>
<CONTENIDO_YAML> ::= <MAPA_OPCIONES>

(* Bloque Init (opcional) *)
<BLOQUE_INIT> ::= "%%{init:" <CONTENIDO_INIT> "}" <NUEVA_LINEA> "%%" <NUEVA_LINEA>
<CONTENIDO_INIT> ::= <MAPA_OPCIONES> (* El contenido de init se fusiona con las mismas opciones que el YAML principal *)


(* Definiciones Detalladas para Opciones de Configuracion (YAML / Init) *)
<MAPA_OPCIONES> ::= (<ESPACIO>* <PAR_CLAVE_VALOR> <NUEVA_LINEA>)+

<PAR_CLAVE_VALOR> ::= <CLAVE_OPCION_GENERAL> ":" <ESPACIO>? <VALOR_OPCION_GENERAL>
                    | <ESPACIO>* "plotOptions" ":" <NUEVA_LINEA> <ESPACIO>+ <OPCION_PLOT_OPTIONS>

(* Opciones Generales que la clase `MermaidXChartConverter` procesa *)
<CLAVE_OPCION_GENERAL> ::= "title"
                         | "width"
                         | "height"
                         | "xAxisLabel"
                         | "yAxisLabel"
                         | "legendPosition"
                         | "showLegend"
                         | "legendVisible"
                         | "xAxisMin"
                         | "xAxisMax"
                         | "yAxisMin"
                         | "yAxisMax"

<VALOR_OPCION_GENERAL> ::= <CADENA_TEXTO>
                         | <ENTERO>
                         | <NUMERO>
                         | <BOOLEANO>
                         | <CADENA_ENUM_LEYENDA>

(* Valores soportados para la posicion de la leyenda *)
<CADENA_ENUM_LEYENDA> ::= "bottom" | "top" | "left" | "right" | "outsideE" | "outsideW" | "outsideN" | "outsideS"
                         (* Otras posiciones validas en org.knowm.xchart.style.Styler.LegendPosition *)


(* Opciones Especificas de 'plotOptions' *)
<OPCION_PLOT_OPTIONS> ::= "pie" ":" <NUEVA_LINEA> (<ESPACIO>+ <OPCION_PIE> <NUEVA_LINEA>)+

<OPCION_PIE> ::= "donut" ":" <ESPACIO>? <BOOLEANO>
               (* Se podrian anadir otras opciones de PieStyler si se implementan *)


(* Definicion del Tipo de Grafico Principal *)
<DEFINICION_GRAFICO> ::= <GRAFICO_TARTA> | <GRAFICO_BARRAS> | <GRAFICO_LINEAS> | <GRAFICO_XY>

(* Patrones Comunes de Datos *)
<LISTA_NUMEROS> ::= <NUMERO> ( "," <ESPACIO>? <NUMERO> )*
<LISTA_CATEGORIAS> ::= <CADENA_TEXTO> ( "," <ESPACIO>? <CADENA_TEXTO> )*

(* 1. Grafico de Tarta (pie) *)
<GRAFICO_TARTA> ::= "pie" <NUEVA_LINEA> <ENTRADA_DATOS_TARTA>*
<ENTRADA_DATOS_TARTA> ::= <CADENA_TEXTO> ":" <ESPACIO>? <NUMERO> <NUEVA_LINEA>

(* 2. Grafico de Barras (barChart) *)
<GRAFICO_BARRAS> ::= "barChart" <NUEVA_LINEA> <DECLARACION_EJE_X_BARRAS> <DECLARACION_SERIE_BARRAS>*
<DECLARACION_EJE_X_BARRAS> ::= "xAxis" <ESPACIO> <LISTA_CATEGORIAS> <NUEVA_LINEA>
<DECLARACION_SERIE_BARRAS> ::= "series" <ESPACIO> <CADENA_TEXTO> <NUEVA_LINEA> <LISTA_NUMEROS> <NUEVA_LINEA>

(* 3. Grafico de Lineas (lineChart) *)
<GRAFICO_LINEAS> ::= "lineChart" <NUEVA_LINEA> <DECLARACION_EJE_X_LINEAS> <DECLARACION_SERIE_LINEAS>*
<DECLARACION_EJE_X_LINEAS> ::= "x-axis" <ESPACIO> <LISTA_CATEGORIAS> <NUEVA_LINEA>
(* Nota: Si 'x-axis' no define categorias, el codigo intentara crear un XYChart numerico,
   asignando indices secuenciales como valores X para cada serie. *)
<DECLARACION_SERIE_LINEAS> ::= "series" <ESPACIO> <CADENA_TEXTO> <NUEVA_LINEA> <LISTA_NUMEROS> <NUEVA_LINEA>

(* 4. Grafico XY (xychart-beta) *)
<GRAFICO_XY> ::= "xychart-beta" <NUEVA_LINEA> <TIPO_EJE_XY>* <DECLARACION_EJE_XY>* <DECLARACION_SERIE_XY>*

<TIPO_EJE_XY> ::= ("x" | "y") "-axis-type" <ESPACIO> ("numeric" | "category") <NUEVA_LINEA>

<DECLARACION_EJE_XY> ::= ("x" | "y") "-axis" <ESPACIO> ( <RANGO_NUMERICO> | <LISTA_CATEGORIAS> ) <NUEVA_LINEA>
<RANGO_NUMERICO> ::= <NUMERO> <ESPACIO> "-->" <ESPACIO> <NUMERO>

<DECLARACION_SERIE_XY> ::= <TIPO_SERIE_XY> <ESPACIO> <CADENA_TEXTO> [ <ESPACIO> <LISTA_NUMEROS_O_VACIO> ] <NUEVA_LINEA> [ <LISTA_NUMEROS> <NUEVA_LINEA> ]
(* Nota sobre <DECLARACION_SERIE_XY>: *)
(* - Si [ <ESPACIO> <LISTA_NUMEROS_O_VACIO> ] este presente en la misma linea que la declaracion de la serie:
     - Si contiene valores, se interpretan como los valores Y, y los valores X se indexan desde 0 (0,1,2...).
     - Si esta vacio (solo espacios o nada), se espera que las DOS lineas siguientes contengan la lista de valores X y luego la lista de valores Y.
*)
<LISTA_NUMEROS_O_VACIO> ::= <LISTA_NUMEROS> | (* vacio *)

<TIPO_SERIE_XY> ::= "line" | "bar" (* "bar" se mapea a 'Area' en XYChart de XChart 3.6.0 *)
</pre>
 * 
 */
public class MermaidXChartConverter {

    private static final Pattern YAML_BLOCK_PATTERN = Pattern.compile(
            "---(?s)(.*?)(?:\\n)?---(.*)", Pattern.DOTALL
    );

    private static final Pattern INIT_CONFIG_PATTERN = Pattern.compile(
            "%%\\{init:\\s*([^}]+?)}\\s*%%"
    );

    /**
     * Parsea una cadena de sintaxis Mermaid y devuelve un objeto Chart de XChart.
     *
     * @param mermaidSyntax La cadena que contiene la definicion del grafico Mermaid.
     * @return Un objeto Chart configurado segun la sintaxis Mermaid.
     * @throws IllegalArgumentException Si la sintaxis Mermaid no es reconocida o es invalida.
     */
    public Chart<?, ?> parseMermaidToXChart(String mermaidSyntax) {
        if (mermaidSyntax == null || mermaidSyntax.trim().isEmpty()) {
            throw new IllegalArgumentException("La sintaxis Mermaid no puede estar vacia.");
        }

        mermaidSyntax = mermaidSyntax.replaceAll("^\\s*%%.*$", "").trim();

        Map<String, Object> yamlConfig = new HashMap<>();
        Matcher yamlMatcher = YAML_BLOCK_PATTERN.matcher(mermaidSyntax);
        String chartContent = mermaidSyntax;

        if (yamlMatcher.find()) {
            String yamlString = yamlMatcher.group(1).trim();
            chartContent = yamlMatcher.group(2).trim();
            Yaml yaml = new Yaml();
            try {
                Map<String, Object> parsedYaml = yaml.load(new StringReader(yamlString));
                if (parsedYaml != null) {
                    yamlConfig.putAll(parsedYaml);
                }
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudo parsear el bloque YAML. " + e.getMessage());
            }
        }

        Matcher initMatcher = INIT_CONFIG_PATTERN.matcher(chartContent);
        if (initMatcher.find()) {
            String initJsonOrYaml = initMatcher.group(1).trim();
            try {
                Yaml yaml = new Yaml();
                Map<String, Object> initMap = yaml.load(new StringReader(initJsonOrYaml));
                if (initMap != null) {
                    yamlConfig.putAll(initMap);
                }
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudo parsear la configuracion 'init'. " + e.getMessage());
            }
            chartContent = chartContent.replace(initMatcher.group(0), "").trim();
        }

        String[] lines = chartContent.split("\\n");
        String chartTypeLine = lines[0].trim();

        Chart<?, ?> chart;
        if (chartTypeLine.startsWith("pie")) {
            chart = parsePieChart(chartContent, yamlConfig);
        } else if (chartTypeLine.startsWith("barChart")) {
            chart = parseBarChart(chartContent, yamlConfig);
        } else if (chartTypeLine.startsWith("lineChart")) {
            chart = parseLineChart(chartContent, yamlConfig);
        } else if (chartTypeLine.startsWith("xychart-beta")) {
            chart = parseXYChart(chartContent, yamlConfig);
        } else {
            throw new IllegalArgumentException("Tipo de grafico Mermaid no soportado o invalido: " + chartTypeLine);
        }

        applyGeneralStyling(chart.getStyler(), yamlConfig);

        return chart;
    }

    /**
     * Aplica estilos generales a un ChartStyler basandose en un mapa de configuracion YAML.
     * Adaptado para la API de XChart 3.6.0.
     *
     * @param styler El Styler del grafico al que se aplicacion los estilos.
     * @param config El mapa de configuracion YAML.
     */
    private void applyGeneralStyling(Styler styler, Map<String, Object> config) {
        // Opciones de leyenda (presentes en la interfaz Styler)
        if (config.containsKey("legendPosition") && config.get("legendPosition") instanceof String) {
            try {
                LegendPosition position = LegendPosition.valueOf(
                        ((String) config.get("legendPosition")).toUpperCase()
                );
                styler.setLegendPosition(position);
            } catch (IllegalArgumentException e) {
                System.err.println("Advertencia: Posicion de leyenda invalida en YAML: " + config.get("legendPosition"));
            }
        }

        if (config.containsKey("showLegend") && config.get("showLegend") instanceof Boolean) {
            styler.setLegendVisible((Boolean) config.get("showLegend"));
        } else if (config.containsKey("legendVisible") && config.get("legendVisible") instanceof Boolean) {
            styler.setLegendVisible((Boolean) config.get("legendVisible"));
        }

        // --- Soporte de temas eliminado para XChart 3.6.0 ---
        // Se asume que el tema por defecto del constructor o del builder es suficiente.

        // Metodos de rangos de ejes (presentes en AxesChartStyler)
        if (styler instanceof AxesChartStyler) {
            AxesChartStyler axesStyler = (AxesChartStyler) styler;
            if (config.containsKey("xAxisMin") && config.get("xAxisMin") instanceof Number) {
                axesStyler.setXAxisMin(((Number) config.get("xAxisMin")).doubleValue());
            }
            if (config.containsKey("xAxisMax") && config.get("xAxisMax") instanceof Number) {
                axesStyler.setXAxisMax(((Number) config.get("xAxisMax")).doubleValue());
            }
            if (config.containsKey("yAxisMin") && config.get("yAxisMin") instanceof Number) {
                axesStyler.setYAxisMin(((Number) config.get("yAxisMin")).doubleValue());
            }
            if (config.containsKey("yAxisMax") && config.get("yAxisMax") instanceof Number) {
                axesStyler.setYAxisMax(((Number) config.get("yAxisMax")).doubleValue());
            }
        }
    }


    /**
     * Parsea la sintaxis de un grafico de Tarta Mermaid.
     *
     * @param chartContent El contenido del grafico Mermaid (despues de eliminar el YAML).
     * @param yamlConfig   Configuracion adicional del bloque YAML.
     * @return Un objeto PieChart de XChart.
     */
    private org.knowm.xchart.PieChart parsePieChart(String chartContent, Map<String, Object> yamlConfig) {
        String title = "Pie Chart";
        if (yamlConfig.containsKey("title")) {
            title = String.valueOf(yamlConfig.get("title"));
        }

        org.knowm.xchart.PieChartBuilder builder = new org.knowm.xchart.PieChartBuilder().title(title);
        if (yamlConfig.containsKey("width")) {
            builder.width(getIntegerFromConfig(yamlConfig, "width"));
        }
        if (yamlConfig.containsKey("height")) {
            builder.height(getIntegerFromConfig(yamlConfig, "height"));
        }
        org.knowm.xchart.PieChart chart = builder.build();

        PieStyler pieStyler = chart.getStyler();
        pieStyler.setDonutThickness(0.0); // No donut por defecto

        if (yamlConfig.containsKey("plotOptions") && yamlConfig.get("plotOptions") instanceof Map) {
            Map<?, ?> plotOptionsMap = (Map<?, ?>) yamlConfig.get("plotOptions");
            if (plotOptionsMap.containsKey("pie") && plotOptionsMap.get("pie") instanceof Map) {
                Map<?, ?> pieOptions = (Map<?, ?>) plotOptionsMap.get("pie");
                if (pieOptions.containsKey("donut") && pieOptions.get("donut") instanceof Boolean) {
                    if (((Boolean) pieOptions.get("donut")).booleanValue()) {
                        pieStyler.setDonutThickness(0.4); // Grosor por defecto para donut
                    } else {
                        pieStyler.setDonutThickness(0.0); // No donut
                    }
                }
            }
        }

        Pattern dataPattern = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:\\s*(\\d+(\\.\\d+)?)\\s*$", Pattern.MULTILINE);
        Matcher matcher = dataPattern.matcher(chartContent);

        while (matcher.find()) {
            String label = matcher.group(1);
            double value = Double.parseDouble(matcher.group(2));
            chart.addSeries(label, value);
        }

        return chart;
    }

    /**
     * Parsea la sintaxis de un grafico de Barras Mermaid.
     *
     * @param chartContent El contenido del grafico Mermaid.
     * @param yamlConfig   Configuracion adicional del bloque YAML.
     * @return Un objeto CategoryChart de XChart.
     */
    private org.knowm.xchart.CategoryChart parseBarChart(String chartContent, Map<String, Object> yamlConfig) {
        String title = "Bar Chart";
        if (yamlConfig.containsKey("title")) {
            title = String.valueOf(yamlConfig.get("title"));
        }

        org.knowm.xchart.CategoryChartBuilder builder = new org.knowm.xchart.CategoryChartBuilder().title(title);
        if (yamlConfig.containsKey("width")) {
            builder.width(getIntegerFromConfig(yamlConfig, "width"));
        }
        if (yamlConfig.containsKey("height")) {
            builder.height(getIntegerFromConfig(yamlConfig, "height"));
        }
        org.knowm.xchart.CategoryChart chart = builder.build();

        if (yamlConfig.containsKey("xAxisLabel") && yamlConfig.get("xAxisLabel") instanceof String) {
            chart.setXAxisTitle(String.valueOf(yamlConfig.get("xAxisLabel")));
        }
        if (yamlConfig.containsKey("yAxisLabel") && yamlConfig.get("yAxisLabel") instanceof String) {
            chart.setYAxisTitle(String.valueOf(yamlConfig.get("yAxisLabel")));
        }

        chart.getStyler().setOverlapped(true);

        List<String> categories = new ArrayList<>();
        Map<String, List<Double>> seriesData = new LinkedHashMap<>();
        String currentSeriesName = null;

        String[] lines = chartContent.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("barChart") || line.isEmpty()) {
                continue;
            }

            if (line.startsWith("xAxis ")) {
                String xAxisContent = line.substring("xAxis ".length()).trim();
                categories.addAll(parseCategories(xAxisContent));
            } else if (line.startsWith("series \"")) {
                Pattern seriesPattern = Pattern.compile("^series\\s*\"([^\"]+)\"\\s*$");
                Matcher seriesMatcher = seriesPattern.matcher(line);
                if (seriesMatcher.find()) {
                    currentSeriesName = seriesMatcher.group(1);
                    seriesData.put(currentSeriesName, new ArrayList<>());
                }
            } else if (currentSeriesName != null && line.matches("^[\\d.,\\s\\[\\]]+$")) {
                seriesData.get(currentSeriesName).addAll(parseValues(line));
            }
        }

        for (Map.Entry<String, List<Double>> entry : seriesData.entrySet()) {
            chart.addSeries(entry.getKey(), categories, entry.getValue());
        }

        return chart;
    }

    /**
     * Parsea la sintaxis de un grafico de Lineas Mermaid.
     *
     * @param chartContent El contenido del grafico Mermaid.
     * @param yamlConfig   Configuracion adicional del bloque YAML.
     * @return Un objeto Chart de XChart (puede ser XYChart o CategoryChart).
     */
    private Chart<?, ?> parseLineChart(String chartContent, Map<String, Object> yamlConfig) {
        String title = "Line Chart";
        if (yamlConfig.containsKey("title")) {
            title = String.valueOf(yamlConfig.get("title"));
        }

        List<String> xLabels = new ArrayList<>();
        Map<String, List<Double>> seriesData = new LinkedHashMap<>();
        String currentSeriesName = null;

        String[] lines = chartContent.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("lineChart") || line.isEmpty()) {
                continue;
            }

            if (line.startsWith("x-axis ")) {
                String xAxisContent = line.substring("x-axis ".length()).trim();
                xLabels.addAll(parseCategories(xAxisContent));
            } else if (line.startsWith("series \"")) {
                Pattern seriesPattern = Pattern.compile("^series\\s*\"([^\"]+)\"\\s*$");
                Matcher seriesMatcher = seriesPattern.matcher(line);
                if (seriesMatcher.find()) {
                    currentSeriesName = seriesMatcher.group(1);
                    seriesData.put(currentSeriesName, new ArrayList<>());
                }
            } else if (currentSeriesName != null && line.matches("^[\\d.,\\s\\[\\]]+$")) {
                seriesData.get(currentSeriesName).addAll(parseValues(line));
            }
        }

        if (!xLabels.isEmpty()) {
            org.knowm.xchart.CategoryChartBuilder builder = new org.knowm.xchart.CategoryChartBuilder().title(title);
            if (yamlConfig.containsKey("width")) {
                builder.width(getIntegerFromConfig(yamlConfig, "width"));
            }
            if (yamlConfig.containsKey("height")) {
                builder.height(getIntegerFromConfig(yamlConfig, "height"));
            }
            org.knowm.xchart.CategoryChart categoryChart = builder.build();

            if (yamlConfig.containsKey("xAxisLabel") && yamlConfig.get("xAxisLabel") instanceof String) {
                categoryChart.setXAxisTitle(String.valueOf(yamlConfig.get("xAxisLabel")));
            }
            if (yamlConfig.containsKey("yAxisLabel") && yamlConfig.get("yAxisLabel") instanceof String) {
                categoryChart.setYAxisTitle(String.valueOf(yamlConfig.get("yAxisLabel")));
            }

            applyGeneralStyling(categoryChart.getStyler(), yamlConfig);
            for (Map.Entry<String, List<Double>> entry : seriesData.entrySet()) {
                CategorySeries series = categoryChart.addSeries(entry.getKey(), xLabels, entry.getValue());
                // setSmooth no existe para CategorySeries en XChart 3.6.0
            }
            return categoryChart;
        } else {
            org.knowm.xchart.XYChartBuilder builder = new org.knowm.xchart.XYChartBuilder().title(title);
            if (yamlConfig.containsKey("width")) {
                builder.width(getIntegerFromConfig(yamlConfig, "width"));
            }
            if (yamlConfig.containsKey("height")) {
                builder.height(getIntegerFromConfig(yamlConfig, "height"));
            }
            org.knowm.xchart.XYChart xyChart = builder.build();

            if (yamlConfig.containsKey("xAxisLabel") && yamlConfig.get("xAxisLabel") instanceof String) {
                xyChart.setXAxisTitle(String.valueOf(yamlConfig.get("xAxisLabel")));
            }
            if (yamlConfig.containsKey("yAxisLabel") && yamlConfig.get("yAxisLabel") instanceof String) {
                xyChart.setYAxisTitle(String.valueOf(yamlConfig.get("yAxisLabel")));
            }

            applyGeneralStyling(xyChart.getStyler(), yamlConfig);
            for (Map.Entry<String, List<Double>> entry : seriesData.entrySet()) {
                List<Number> xValues = new ArrayList<>();
                for (int i = 0; i < entry.getValue().size(); i++) {
                    xValues.add(i);
                }
                XYSeries series = xyChart.addSeries(entry.getKey(), xValues, entry.getValue());
                // setSmooth no existe para XYSeries en XChart 3.6.0
            }
            return xyChart;
        }
    }


    /**
     * Parsea la sintaxis de un grafico xychart-beta de Mermaid, que permite mezclar barras y lineas.
     *
     * @param chartContent El contenido del grafico Mermaid.
     * @param yamlConfig   Configuracion adicional del bloque YAML.
     * @return Un objeto Chart de XChart (puede ser XYChart o CategoryChart).
     */
    private Chart<?, ?> parseXYChart(String chartContent, Map<String, Object> yamlConfig) {
        String title = "XY Chart";
        if (yamlConfig.containsKey("title")) {
            title = String.valueOf(yamlConfig.get("title"));
        }

        boolean isCategoryXAxis = false;
        String xAxisType = null;

        Pattern axisTypePattern = Pattern.compile("^\\s*(x|y)-axis-type\\s*(numeric|category)\\s*$", Pattern.MULTILINE);
        Matcher axisTypeMatcher = axisTypePattern.matcher(chartContent);
        while (axisTypeMatcher.find()) {
            if (axisTypeMatcher.group(1).equals("x")) {
                xAxisType = axisTypeMatcher.group(2);
                if ("category".equalsIgnoreCase(xAxisType)) {
                    isCategoryXAxis = true;
                }
            }
        }

        org.knowm.xchart.XYChart xyChart = null;
        org.knowm.xchart.CategoryChart categoryChart = null;
        Styler styler;

        // Builders
        org.knowm.xchart.CategoryChartBuilder categoryBuilder = null;
        org.knowm.xchart.XYChartBuilder xyBuilder = null;

        if (isCategoryXAxis) {
            categoryBuilder = new org.knowm.xchart.CategoryChartBuilder().title(title);
            if (yamlConfig.containsKey("width")) {
                categoryBuilder.width(getIntegerFromConfig(yamlConfig, "width"));
            }
            if (yamlConfig.containsKey("height")) {
                categoryBuilder.height(getIntegerFromConfig(yamlConfig, "height"));
            }
            categoryChart = categoryBuilder.build();
            styler = categoryChart.getStyler();
        } else {
            xyBuilder = new org.knowm.xchart.XYChartBuilder().title(title);
            if (yamlConfig.containsKey("width")) {
                xyBuilder.width(getIntegerFromConfig(yamlConfig, "width"));
            }
            if (yamlConfig.containsKey("height")) {
                xyBuilder.height(getIntegerFromConfig(yamlConfig, "height"));
            }
            xyChart = xyBuilder.build();
            styler = xyChart.getStyler();
        }

        // Aplicar titulos de ejes directamente al Chart principal
        if (isCategoryXAxis) {
            if (yamlConfig.containsKey("xAxisLabel") && yamlConfig.get("xAxisLabel") instanceof String) {
                categoryChart.setXAxisTitle(String.valueOf(yamlConfig.get("xAxisLabel")));
            }
            if (yamlConfig.containsKey("yAxisLabel") && yamlConfig.get("yAxisLabel") instanceof String) {
                categoryChart.setYAxisTitle(String.valueOf(yamlConfig.get("yAxisLabel")));
            }
        } else {
            if (yamlConfig.containsKey("xAxisLabel") && yamlConfig.get("xAxisLabel") instanceof String) {
                xyChart.setXAxisTitle(String.valueOf(yamlConfig.get("xAxisLabel")));
            }
            if (yamlConfig.containsKey("yAxisLabel") && yamlConfig.get("yAxisLabel") instanceof String) {
                xyChart.setYAxisTitle(String.valueOf(yamlConfig.get("yAxisLabel")));
            }
        }

        applyGeneralStyling(styler, yamlConfig);

        Pattern xAxisPattern = Pattern.compile("^\\s*x-axis\\s*([^\n]+)$", Pattern.MULTILINE);
        Pattern yAxisPattern = Pattern.compile("^\\s*y-axis\\s*([^\n]+)$", Pattern.MULTILINE);
        // Ajustado el patron de series para que la parte de datos sea opcional, si viene en la siguiente linea
        Pattern seriesPattern = Pattern.compile("^\\s*(line|bar)\\s*\"([^\"]+)\"\\s*([^\\n]*)$", Pattern.MULTILINE);
        Pattern rangePattern = Pattern.compile("^\\s*(\\d+(\\.\\d+)?)\\s*-->\\s*(\\d+(\\.\\d+)?)\\s*$");

        List<String> xDataGlobalCategories = new ArrayList<>(); // Para el eje X de categoria si se define globalmente
        Map<String, List<Number>> seriesExplicitXData = new LinkedHashMap<>(); // Datos X explicitos por serie
        Map<String, List<Number>> seriesYData = new LinkedHashMap<>(); // Datos Y por serie
        Map<String, String> seriesTypeMap = new HashMap<>(); // Tipo de renderizado por serie

        String[] linesArray = chartContent.split("\\n"); // Renombrado para evitar conflicto con "lines" en el main
        for (int i = 0; i < linesArray.length; i++) {
            String line = linesArray[i].trim();
            if (line.startsWith("xychart-beta") || line.isEmpty()) {
                continue;
            }

            Matcher xAxisMatcher = xAxisPattern.matcher(line);
            Matcher yAxisMatcher = yAxisPattern.matcher(line);
            Matcher seriesMatcher = seriesPattern.matcher(line);

            if (xAxisMatcher.find()) {
                String xAxisContent = xAxisMatcher.group(1).trim();
                Matcher rangeMatcher = rangePattern.matcher(xAxisContent);
                if (rangeMatcher.find()) {
                    double min = Double.parseDouble(rangeMatcher.group(1));
                    double max = Double.parseDouble(rangeMatcher.group(3));
                    if (styler instanceof AxesChartStyler) {
                        ((AxesChartStyler) styler).setXAxisMin(min);
                        ((AxesChartStyler) styler).setXAxisMax(max);
                    }
                } else { // X-axis con categorias explicitas (ej. "Q1", "Q2")
                    xDataGlobalCategories.addAll(parseCategories(xAxisContent));
                }
            } else if (yAxisMatcher.find()) {
                String yAxisContent = yAxisMatcher.group(1).trim();
                Matcher rangeMatcher = rangePattern.matcher(yAxisContent);
                if (rangeMatcher.find()) {
                    double min = Double.parseDouble(rangeMatcher.group(1));
                    double max = Double.parseDouble(rangeMatcher.group(3));
                    if (styler instanceof AxesChartStyler) {
                        ((AxesChartStyler) styler).setYAxisMin(min);
                        ((AxesChartStyler) styler).setYAxisMax(max);
                    }
                }
            } else if (seriesMatcher.find()) {
                String type = seriesMatcher.group(1);
                String name = seriesMatcher.group(2);
                String inlineData = seriesMatcher.group(3).trim(); // Datos en la misma linea, puede estar vacio

                List<Number> currentSeriesX = new ArrayList<>();
                List<Number> currentSeriesY = new ArrayList<>();

                if (!inlineData.isEmpty()) {
                    // Caso 1: Datos X e Y en linea, como X1,Y1,X2,Y2,... o solo Y1,Y2,...
                    List<Double> parsedValues = parseValues(inlineData);
                    if (parsedValues.size() % 2 == 0) { // Asumir pares X,Y
                        for (int j = 0; j < parsedValues.size(); j += 2) {
                            currentSeriesX.add(parsedValues.get(j));
                            currentSeriesY.add(parsedValues.get(j + 1));
                        }
                    } else { // Asumir solo valores Y, X es implicito (0,1,2...)
                        currentSeriesY.addAll(parsedValues);
                        for (int j = 0; j < currentSeriesY.size(); j++) {
                            currentSeriesX.add(j);
                        }
                    }
                } else {
                    // Caso 2: Datos X e Y en lineas subsiguientes (como en los ejemplos del usuario)
                    // Consumir la siguiente linea para valores X
                    if (i + 1 < linesArray.length) {
                        List<Double> parsedXValues = parseValues(linesArray[i + 1].trim());
                        currentSeriesX.addAll(parsedXValues);
                        i++; // Avanzar el indice para consumir la linea de datos X
                    }
                    // Consumir la siguiente linea para valores Y
                    if (i + 1 < linesArray.length) {
                        List<Double> parsedYValues = parseValues(linesArray[i + 1].trim());
                        currentSeriesY.addAll(parsedYValues);
                        i++; // Avanzar el indice para consumir la linea de datos Y
                    }
                }

                seriesExplicitXData.put(name, currentSeriesX);
                seriesYData.put(name, currentSeriesY);
                seriesTypeMap.put(name, type);
            }
        }

        // Agregar series al grafico
        if (isCategoryXAxis) {
            // Asegurarse de que tenemos un CategoryChart
            if (categoryChart == null) { // Esto no deberia pasar si isCategoryXAxis es true
                throw new IllegalStateException("CategoryChart no fue inicializado correctamente.");
            }

            for (Map.Entry<String, List<Number>> entry : seriesYData.entrySet()) {
                String seriesName = entry.getKey();
                List<Double> yValues = entry.getValue().stream().mapToDouble(Number::doubleValue).boxed().collect(Collectors.toList());

                // Para CategoryChart, los valores X vienen de las categorias globales.
                // Asegurar que las cantidades de X global y Y para la serie coinciden.
                if (xDataGlobalCategories.isEmpty()) {
                    System.err.println("Advertencia: No se encontraron categorias para el eje X en el grafico de categoria XY. Generando indices.");
                    List<String> generatedX = new ArrayList<>();
                    for(int j = 0; j < yValues.size(); j++) generatedX.add(String.valueOf(j));
                    // Aqui asumimos que generatedX.size() == yValues.size()
                    CategorySeries series = categoryChart.addSeries(seriesName, generatedX, yValues);
                    String type = seriesTypeMap.get(seriesName);
                    if ("bar".equalsIgnoreCase(type)) {
                        series.setChartCategorySeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
                    } else if ("line".equalsIgnoreCase(type)) {
                        series.setChartCategorySeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Line);
                    }
                } else if (xDataGlobalCategories.size() != yValues.size()) {
                    throw new IllegalArgumentException("El numero de categorias del eje X (" + xDataGlobalCategories.size() + ") no coincide con el numero de valores Y (" + yValues.size() + ") para la serie " + seriesName + ".");
                } else {
                    CategorySeries series = categoryChart.addSeries(seriesName, xDataGlobalCategories, yValues);
                    String type = seriesTypeMap.get(seriesName);
                    if ("bar".equalsIgnoreCase(type)) {
                        series.setChartCategorySeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);
                    } else if ("line".equalsIgnoreCase(type)) {
                        series.setChartCategorySeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Line);
                    }
                }
            }
            return categoryChart;

        } else { // Eje X numerico (XYChart)
            // Asegurarse de que tenemos un XYChart
            if (xyChart == null) { // Esto no deberia pasar si isCategoryXAxis es false
                throw new IllegalStateException("XYChart no fue inicializado correctamente.");
            }

            for (Map.Entry<String, List<Number>> entry : seriesYData.entrySet()) {
                String seriesName = entry.getKey();
                List<Number> xValuesForSeries = seriesExplicitXData.get(seriesName);
                List<Double> yValuesForSeries = entry.getValue().stream().mapToDouble(Number::doubleValue).boxed().collect(Collectors.toList());

                // Si los valores X no se especificaron explicitamente para la serie, generarlos.
                if (xValuesForSeries == null || xValuesForSeries.isEmpty()) {
                    xValuesForSeries = new ArrayList<>();
                    for (int j = 0; j < yValuesForSeries.size(); j++) {
                        xValuesForSeries.add(j); // Generar valores X secuenciales (0, 1, 2...)
                    }
                }

                if (xValuesForSeries.size() != yValuesForSeries.size()) {
                    throw new IllegalArgumentException("El numero de valores X (" + xValuesForSeries.size() + ") no coincide con el numero de valores Y (" + yValuesForSeries.size() + ") para la serie " + seriesName + ".");
                }

                XYSeries series = xyChart.addSeries(seriesName, xValuesForSeries, yValuesForSeries);

                String type = seriesTypeMap.get(seriesName);
                if ("bar".equalsIgnoreCase(type)) {
                    series.setXYSeriesRenderStyle(XYSeriesRenderStyle.Area); // Mapeado a Area
                } else if ("line".equalsIgnoreCase(type)) {
                    series.setXYSeriesRenderStyle(XYSeriesRenderStyle.Line);
                    // setSmooth no existe para XYSeries en XChart 3.6.0
                }
            }
            return xyChart;
        }
    }


    private List<String> parseCategories(String input) {
        List<String> categories = new ArrayList<>();
        input = input.replaceAll("^\\[|\\]$", "").trim();
        // El patron \\s*,\\s* permite comas con espacios alrededor
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|([^,]+)"); // Ajustado para permitir comas dentro de las comillas dobles
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group(1); // Cadenas entre comillas dobles
            if (match == null) {
                match = matcher.group(2); // Cadenas sin comillas dobles
            }
            if (match != null) { // Asegurarse de que el match no sea nulo antes de anadir
                match = match.trim();
                if (!match.isEmpty()) {
                    categories.add(match);
                }
            }
        }
        return categories;
    }


    private List<Double> parseValues(String input) {
        List<Double> values = new ArrayList<>();
        input = input.replaceAll("^\\[|\\]$", "").trim();
        // Ahora el split es mas robusto para manejar comas con espacios y multiplos
        String[] parts = input.split("\\s*,\\s*");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) { // Ignorar partes vacias si hay comas consecutivas o al inicio/final
                continue;
            }
            try {
                values.add(Double.parseDouble(part));
            } catch (NumberFormatException e) {
                System.err.println("Advertencia: Valor numerico invalido encontrado: '" + part + "'");
            }
        }
        return values;
    }

    private int getIntegerFromConfig(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                System.err.println("Advertencia: No se pudo convertir '" + value + "' a entero para la clave '" + key + "'.");
            }
        }
        return 0;
    }

    /**
     * Metodo main para probar la funcionalidad de conversion de Mermaid a XChart.
     * Incluye varios ejemplos de graficos Mermaid.
     */
    public static void main(String[] args) {
        MermaidXChartConverter converter = new MermaidXChartConverter();

        // --- Ejemplo 1: Grafico de Tarta (Pie Chart) ---
        String pieChartMermaid =
                "---\n" +
                "title: Ventas por Region\n" +
                "width: 600\n" +
                "height: 400\n" +
                "plotOptions:\n" +
                "  pie:\n" +
                "    donut: true\n" +
                "---\n" +
                "pie\n" +
                "\"Norte\": 42.96\n" +
                "\"Sur\": 50.58\n" +
                "\"Este\": 15.42\n" +
                "\"Oeste\": 10.04";
        try {
            Chart<?, ?> pieChart = converter.parseMermaidToXChart(pieChartMermaid);
            new SwingWrapper<>(pieChart).setTitle("Ejemplo 1: Grafico de Tarta").displayChart();
        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear grafico de tarta: " + e.getMessage());
        }

        // --- Ejemplo 2: Grafico de Barras (Category Chart) ---
        String barChartMermaid =
                "---\n" +
                "title: Productos Mas Vendidos\n" +
                "xAxisLabel: Productos\n" +
                "yAxisLabel: Cantidad\n" +
                "width: 700\n" +
                "height: 500\n" +
                "---\n" +
                "barChart\n" +
                "xAxis \"Manzanas\", \"Naranjas\", \"Platanos\"\n" +
                "series \"Tienda A\"\n" +
                "100, 200, 150\n" +
                "series \"Tienda B\"\n" +
                "80, 120, 250";
        try {
            Chart<?, ?> barChart = converter.parseMermaidToXChart(barChartMermaid);
            new SwingWrapper<>(barChart).setTitle("Ejemplo 2: Grafico de Barras").displayChart();
        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear grafico de barras: " + e.getMessage());
        }

        // --- Ejemplo 3: Grafico de Lineas (Category X-Axis) ---
        String lineChartMermaidCategory =
                "---\n" +
                "title: Crecimiento Mensual\n" +
                "xAxisLabel: Mes\n" +
                "yAxisLabel: Porcentaje\n" +
                "width: 800\n" +
                "height: 600\n" +
                "---\n" +
                "lineChart\n" +
                "x-axis \"Ene\", \"Feb\", \"Mar\", \"Abr\"\n" +
                "series \"Serie 1\"\n" +
                "10, 20, 15, 25\n" +
                "series \"Serie 2\"\n" +
                "5, 12, 18, 22";
        try {
            Chart<?, ?> lineChartCat = converter.parseMermaidToXChart(lineChartMermaidCategory);
            new SwingWrapper<>(lineChartCat).setTitle("Ejemplo 3: Grafico de Lineas (Eje X Categoria)").displayChart();
        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear grafico de lineas (categoria): " + e.getMessage());
        }

        // --- Ejemplo 4: Grafico XYChart-beta (Numeric X-Axis, Line and Area) ---
        // 'Bar' se mapea a 'Area' en XChart 3.6.0 para XYSeries.
        // Los datos se asumen en formato X_valores \n Y_valores
        String xyChartMermaidNumeric =
                "---\n" +
                "title: Rendimiento por Tiempo\n" +
                "xAxisLabel: Tiempo (s)\n" +
                "yAxisLabel: Valor\n" +
                "width: 750\n" +
                "height: 550\n" +
                "xAxisMin: 0\n" +
                "xAxisMax: 10\n" +
                "yAxisMin: 0\n" +
                "yAxisMax: 100\n" +
                "---\n" +
                "xychart-beta\n" +
                "x-axis-type numeric\n" +
                "x-axis 0 --> 10\n" + // Rango numerico para el styler
                "y-axis 0 --> 100\n" + // Rango numerico para el styler
                "line \"Curva de Crecimiento\"\n" + // Sin datos en linea, espera X e Y en siguientes
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 10\n" + // X values
                "10, 15, 22, 30, 45, 55, 60, 68, 75, 82\n" + // Y values
                "bar \"Puntos de Rendimiento\"\n" + // 'Bar' mapeado a 'Area'
                "1.5, 3.5, 5.5, 7.5, 9.5\n" + // X values
                "20, 40, 60, 80, 95"; // Y values
        try {
            Chart<?, ?> xyChartNumeric = converter.parseMermaidToXChart(xyChartMermaidNumeric);
            new SwingWrapper<>(xyChartNumeric).setTitle("Ejemplo 4: Grafico XY (Eje X Numurico, Linea y area)").displayChart();
        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear grafico XY (numerico): " + e.getMessage());
            e.printStackTrace(); // Imprimir stack trace para depuracion
        }

        // --- Ejemplo 5: Grafico XYChart-beta (Category X-Axis, Line and Bar) ---
        // Los datos se asumen en formato Y_valores, y los X son tomados del "x-axis" global
        String xyChartMermaidCategory =
                "---\n" +
                "title: Datos por Trimestre\n" +
                "xAxisLabel: Trimestre\n" +
                "yAxisLabel: Valor\n" +
                "width: 700\n" +
                "height: 500\n" +
                "---\n" +
                "xychart-beta\n" +
                "x-axis-type category\n" +
                "x-axis \"Q1\", \"Q2\", \"Q3\", \"Q4\"\n" +
                "line \"Ingresos\" 120, 150, 130, 160\n" + // Aqui solo se esperan valores Y
                "bar \"Gastos\" 80, 90, 75, 85"; // Aqui solo se esperan valores Y
        try {
            Chart<?, ?> xyChartCategory = converter.parseMermaidToXChart(xyChartMermaidCategory);
            new SwingWrapper<>(xyChartCategory).setTitle("Ejemplo 5: Grafico XY (Eje X Categoria, Linea y Barras)").displayChart();

        } catch (IllegalArgumentException e) {
            System.err.println("Error al parsear grafico XY (categoria): " + e.getMessage());
            e.printStackTrace(); // Imprimir stack trace para depuracion
        }
    }
}

