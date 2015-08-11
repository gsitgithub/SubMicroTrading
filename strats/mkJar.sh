#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
#!/bin/ksh

{
cd ..
rm -fr dist
mkdir dist
mkdir dist/class
mkdir dist/jars
mkdir dist/smt
mkdir dist/logs
mkdir dist/tmp
mkdir dist/lib
mkdir dist/scripts

cp -r OM/setup dist

cp -r CoreStrats/config dist
cp -r CoreStrats/scripts/* dist/scripts
cp -r CoreStrats/data dist

cp -r strats/config dist
cp -r strats/scripts/* dist/scripts
cp -r strats/data dist
cp -r strats/var dist
cp -r strats/setup dist

cp Core/native/core/bin/linux dist/lib
cp OM/scripts/run/hotspot_compiler dist/.hotspot_compiler
chmod +x dist/scripts/*sh

cp -r ./Core/native/core dist
rm -fr dist/core/target/*.o
cp Core/native/core/bin/linux/*so dist/lib


for DIR in Core Generated model OM CoreStrats strats
do
	cd $DIR/bin

	cp -r * ../../dist/class

	cd ../..
done

cd dist/class
jar -cvf smt.jar *
cp smt.jar ../../smt
mv smt.jar ../smt
cd ..
rm -fr class
cd ..

cp Core/exlib/jars/* dist/jars

find ./dist -name .svn -exec rm -fr {} \;

} 2>&1 | tee mk.log
