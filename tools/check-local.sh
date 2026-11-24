#!/usr/bin/env bash
# Read-only checks by default. --build also writes Gradle output/caches.
set -euo pipefail

require_java=0
build=0
for arg in "$@"; do
    case "$arg" in
        --require-java) require_java=1 ;;
        --build) build=1; require_java=1 ;;
        -h|--help)
            printf '%s\n' \
                'Uso: bash tools/check-local.sh [--require-java] [--build]' \
                '  Sin flags: guardas y suites Python; Java se ejecuta si hay JDK.' \
                '  --require-java: falla antes de las pruebas si falta java o javac.' \
                '  --build: exige JDK y ejecuta también Gradle build.' \
                'No instala herramientas, no cambia de rama ni publica releases.'
            exit 0 ;;
        *) printf 'Opción desconocida: %s\n' "$arg" >&2; exit 2 ;;
    esac
done

# Resolve from this script, not the caller's working directory.
cd -- "${BASH_SOURCE[0]%/*}/.."
command -v python3 >/dev/null || { printf 'Falta python3.\n' >&2; exit 1; }
command -v git >/dev/null || { printf 'Falta git.\n' >&2; exit 1; }
if command -v java >/dev/null && command -v javac >/dev/null; then
    printf '%s\n' 'JDK detectado: las suites ejecutarán sus escenarios Java.'
elif (( require_java )); then
    printf '%s\n' 'Falta JDK (java y javac). No se ejecutaron las pruebas.' >&2
    exit 1
else
    printf '%s\n' 'AVISO: sin JDK, los escenarios Java se omiten; esta validación es parcial.' >&2
fi

python3 tools/verify_mirrors.py
python3 tools/lint_java.py
python3 -m unittest discover -s tools -p 'test_*.py'
python3 tools/ci/test_release_gate.py
python3 tools/ci/test_installer.py
git diff --check
if (( build )); then
    bash ./gradlew --no-daemon build
fi
printf '%s\n' 'Comprobaciones solicitadas terminadas. Revisa los skips; esto no sustituye una prueba en Minecraft.'
