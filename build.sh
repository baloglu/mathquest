#!/usr/bin/env bash
# Builds dist/MathQuest.jar. Needs a JDK (21+) and the server's Paper API + Floodgate jars.
# By default the jars are copied from the local server; set SERVER=/path/to/server to change it.
set -euo pipefail
cd "$(dirname "$0")"
SERVER="${SERVER:-/opt/minecraft/server}"

if [ ! -d lib ] || [ -z "$(ls -A lib 2>/dev/null)" ]; then
  mkdir -p lib
  echo "Copying compile-time jars from $SERVER (needs sudo)..."
  sudo find "$SERVER/libraries" -name '*.jar' \
      \( -path '*paper-api*' -o -path '*kyori*' -o -path '*guava/guava*' -o -path '*bungeecord-chat*' \) \
      -exec cp {} lib/ \;
  sudo cp "$SERVER/plugins/floodgate.jar" lib/
  sudo chown -R "$(id -u):$(id -g)" lib
fi

rm -rf build && mkdir -p build/classes dist
javac -nowarn --release 21 -cp "lib/*" -d build/classes $(find src/main/java -name '*.java')
cp src/main/resources/* build/classes/
jar cf dist/MathQuest.jar -C build/classes .
echo "Built dist/MathQuest.jar"
