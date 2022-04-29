#!/usr/bin/env bash
pushd $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

EXECUTABLE=inf101v2022.out
mvn package assembly:single
echo "#!/usr/bin/java -jar" > $EXECUTABLE
cat target/*-jar-with-dependencies.jar >> $EXECUTABLE
chmod +x $EXECUTABLE

for ruleset in src/games/*.java ; do
  echo -n "Building ${ruleset} ..."
  javac $ruleset -d games -cp $EXECUTABLE && echo " ok"
done

popd
