#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
#!/bin/bash

cd model
rm -fr internal/version/1.0.0/src/* fix/version/1.0.0/src/* 
rm -fr codec-client/version/1.0.0/src/* codec-exchange/version/1.0.0/src/* codec-base/version/1.0.0/src/*
rm -fr codec-factory/version/1.0.0/src/* 
rm -fr codec-binary/version/1.0.0/src/* 
rm -fr binary/version/1.0.0/src/* 

rm -fr internal/version/1.0.0/src/.svn fix/version/1.0.0/src/.svn 
rm -fr codec-client/version/1.0.0/src/.svn codec-exchange/version/1.0.0/src/.svn codec-base/version/1.0.0/src/.svn
rm -fr codec-factory/version/1.0.0/src/.svn
rm -fr codec-binary/version/1.0.0/src/.svn
rm -fr binary/version/1.0.0/src/.svn

