#!/usr/bin/env python3
"""
Calcula en local la metrica de cobertura tal y como la define SonarQube/SonarCloud,
que NO coincide con ninguno de los tres porcentajes que muestra el informe HTML de
Jacoco (instrucciones, ramas y lineas).

    Sonar coverage = (lineas_cubiertas + condiciones_cubiertas)
                     -------------------------------------------
                     (lineas_ejecutables + condiciones_totales)

Sirve para saber si el quality gate va a pasar SIN tener que abrir un PR, util
cuando el plan de SonarCloud solo analiza la rama principal.

Aviso: el resultado es una cota SUPERIOR. Sonar calcula las lineas ejecutables con
su propio parser de Java, asi que en clases con Lombok o records suele contar mas
lineas que Jacoco y su porcentaje real queda 1-3 puntos por debajo de este.

Uso:
    python3 tools/sonar-coverage.py [ruta/al/jacoco.xml]
"""

import fnmatch
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

# Debe reflejar la propiedad <sonar.coverage.exclusions> del pom.xml.
POM = Path(__file__).resolve().parent.parent / "pom.xml"
DEFAULT_REPORT = Path(__file__).resolve().parent.parent / "target/site/jacoco/jacoco.xml"


def exclusions_from_pom():
    if not POM.exists():
        return []
    match = re.search(r"<sonar\.coverage\.exclusions>(.*?)</sonar\.coverage\.exclusions>",
                      POM.read_text(encoding="utf-8"), re.S)
    return [p.strip() for p in match.group(1).split(",")] if match else []


def excluded(path, patterns):
    return any(fnmatch.fnmatch(path, p.replace("**/", "*")) for p in patterns)


def main():
    report = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_REPORT
    if not report.exists():
        sys.exit(f"No existe {report}. Ejecuta primero: ./mvnw verify")

    patterns = exclusions_from_pom()
    lines_ok = lines_total = cond_ok = cond_total = 0
    uncovered = []

    for package in ET.parse(report).getroot().findall("package"):
        for sourcefile in package.findall("sourcefile"):
            path = f"{package.get('name')}/{sourcefile.get('name')}"
            if excluded(path, patterns):
                continue

            misses = []
            for line in sourcefile.findall("line"):
                mi, ci = int(line.get("mi", 0)), int(line.get("ci", 0))
                mb, cb = int(line.get("mb", 0)), int(line.get("cb", 0))

                lines_total += 1
                if ci > 0:
                    lines_ok += 1
                cond_total += mb + cb
                cond_ok += cb

                if mi > 0 or mb > 0:
                    misses.append(line.get("nr"))

            if misses:
                uncovered.append((path, misses))

    covered = lines_ok + cond_ok
    total = lines_total + cond_total

    print(f"Informe: {report}")
    if patterns:
        print(f"Excluido (sonar.coverage.exclusions): {', '.join(patterns)}")
    print()
    print(f"  Lineas       {lines_ok}/{lines_total}  ({lines_ok / lines_total:.1%})")
    if cond_total:
        print(f"  Condiciones  {cond_ok}/{cond_total}  ({cond_ok / cond_total:.1%})")
    print(f"  SONAR        {covered}/{total}  ({covered / total:.1%})   <- la que evalua el quality gate")

    if uncovered:
        print("\nSin cubrir (linea:parcial o no ejecutada):")
        for path, misses in sorted(uncovered):
            print(f"  {path}: {', '.join(misses)}")
    else:
        print("\nTodo cubierto.")


if __name__ == "__main__":
    main()
