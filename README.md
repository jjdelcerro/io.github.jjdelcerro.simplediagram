
# Simple Diagram Framework for gvSIG

[![Licencia](https://img.shields.io/badge/licencia-GPL--3.0-blue.svg)](https://github.com/jjdelcerro/simplediagram/blob/main/LICENSE)
[![Versión](https://img.shields.io/badge/versión-1.0.0--SNAPSHOT-orange.svg)](https://github.com/jjdelcerro/simplediagram/releases)
[![gvSIG](https://img.shields.io/badge/gvSIG-2.2.0%2B-green.svg)](https://gvsig.com/)


Este proyecto es un plugin para **gvSIG Desktop** que proporciona un framework para generar y visualizar diagramas y gráficos a partir de definiciones basadas en texto. Integra potentes librerías de código abierto como **PlantUML** y **XChart** para renderizar diagramas UML y una gran variedad de gráficos directamente en el entorno de gvSIG.

El objetivo es poder describir un diagrama complejo (como un diagrama de secuencia o un gráfico de barras) con una sintaxis simple y que el framework se encargue de mostrarlo como una imagen.

## Características Principales

*   **Múltiples Motores de Diagramas**: Soporte nativo para:
    *   **PlantUML**: Para todo tipo de diagramas UML (secuencia, clases, casos de uso, etc.).
    *   **Sintaxis tipo Mermaid para Gráficos**: Parsea una sintaxis inspirada en Mermaid para generar gráficos de tarta, barras, líneas y XY, utilizando la librería XChart para el renderizado.
*   **Integración Transparente con gvSIG**:
    *   Funciona como un plugin estándar de gvSIG.
    *   Proporciona una `Extension` para invocar la visualización de diagramas desde otras partes de la aplicación.
    *   Añade nuevos tipos de campos para los formularios dinámicos (`JDynFormField`) que pueden mostrar diagramas a partir de ficheros, URLs o arrays de bytes.
*   **Visor Swing Reutilizable**: Incluye un componente `SimpleDiagramViewer` que puede ser fácilmente integrado en cualquier aplicación Swing para visualizar los diagramas.
*   **Detección Automática**: El sistema detecta automáticamente si la definición de entrada corresponde a PlantUML o a un tipo de gráfico Mermaid.

## Tipos de Diagramas Soportados

### 1. PlantUML

Puedes usar la sintaxis completa de PlantUML. El texto debe empezar con `@startuml` y terminar con `@enduml`.

**Ejemplo (Diagrama de Secuencia):**
```
@startuml
Alice -> Bob: Authentication Request
Bob --> Alice: Authentication Response
Alice -> Bob: Another message
@enduml
```

### 2. Gráficos (Sintaxis tipo Mermaid)

El framework incluye un conversor personalizado que transforma una sintaxis similar a la de Mermaid en gráficos de la librería XChart.

**Ejemplo (Gráfico de Tarta):**
```
---
title: Ventas por Región
---
pie
"Norte": 42.96
"Sur": 50.58
"Este": 15.42
"Oeste": 10.04
```

**Ejemplo (Gráfico de Barras):**
```
---
title: Productos Más Vendidos
xAxisLabel: Productos
yAxisLabel: Cantidad
---
barChart
xAxis "Manzanas", "Naranjas", "Plátanos"
series "Tienda A"
100, 200, 150
series "Tienda B"
80, 120, 250
```

**Ejemplo (Gráfico XY Mixto - Líneas y Barras):**
```
---
title: Datos por Trimestre
xAxisLabel: Trimestre
yAxisLabel: Valor
---
xychart-beta
x-axis-type category
x-axis "Q1", "Q2", "Q3", "Q4"
line "Ingresos" 120, 150, 130, 160
bar "Gastos" 80, 90, 75, 85
```

## Arquitectura del Proyecto

El proyecto está organizado en módulos de Maven siguiendo la estructura API/Implementación:

*   `io.github.jjdelcerro.simplediagram.lib`: El núcleo del framework.
    *   `lib.api`: Define las interfaces principales (`SimpleDiagram`, `SimpleDiagramManager`).
    *   `lib.impl`: Contiene la lógica de implementación, incluyendo `DiagramDetector` para identificar el tipo de diagrama y `MermaidXChartConverter` para la conversión de texto a gráficos XChart.
*   `io.github.jjdelcerro.simplediagram.swing`: Componentes de interfaz de usuario.
    *   `swing.api`: Interfaces para los componentes Swing (`SimpleDiagramViewer`).
    *   `swing.impl`: Implementación del visor (`SimpleDiagramViewerImpl`) y de los campos de formulario dinámicos para gvSIG.
*   `io.github.jjdelcerro.simplediagram.app`: El plugin principal de gvSIG.
    *   `app.mainplugin`: Contiene la `DiagramExtension` que se integra con el menú y las acciones de gvSIG.

## Cómo Compilar

El proyecto utiliza Maven. Para compilarlo y empaquetarlo, simplemente ejecuta el siguiente comando desde la raíz del proyecto:

```sh
mvn clean install
```
Esto generará los ficheros JAR de cada módulo en su respectivo directorio `target/`. El paquete del plugin de gvSIG se encontrará en `io.github.jjdelcerro.simplediagram.app/io.github.jjdelcerro.simplediagram.app.mainplugin/target/`.

## Dependencias Clave

*   [gvSIG Desktop](http.www.gvsig.com/): El proyecto está construido sobre el framework de gvSIG.
*   [PlantUML](https://plantuml.com/): Para la generación de diagramas UML.
*   [XChart](https://knowm.org/open-source/xchart/): Para la creación de gráficos de alta calidad.
*   [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml/src/master/): Para procesar las cabeceras de configuración en la sintaxis de los gráficos.

## Licencia

Este proyecto está licenciado bajo la **GNU General Public License v3.0**. Consulta el archivo [LICENSE](LICENSE) para más detalles.

## Autor

*   **Joaquín del Cerro** - *Desarrollo inicial* - [jjdelcerro](https://github.com/jjdelcerro)

