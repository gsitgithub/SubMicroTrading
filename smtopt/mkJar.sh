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
rm -fr distOpt
mkdir distOpt
mkdir distOpt/class
mkdir distOpt/jars
mkdir distOpt/smt
mkdir distOpt/tmp
mkdir distOpt/lib
mkdir distOpt/scripts

cp -r ./smtopt/native/core distOpt
rm -fr distOpt/core/target/*.o


for DIR in smtopt
do
	cd $DIR/bin

	cp -r * ../../distOpt/class

	cd ../..
done

cd distOpt/class
jar -cvf smtopt.jar *
cp smtopt.jar ../../smt
mv smtopt.jar ../smt
cd ..
rm -fr class
cd ..

find ./distOpt -name .svn -exec rm -fr {} \;

} 2>&1 | tee mkOpt.log
