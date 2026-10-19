#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
find src -name '*.java' > sources.txt
if command -v javac >/dev/null 2>&1; then
    javac -d out @sources.txt
else
    java -m jdk.compiler/com.sun.tools.javac.Main -d out @sources.txt
fi
if [ "${1:-}" = "test" ]; then
    java -cp out Tests
else
    java -cp out Main "$@"
fi
