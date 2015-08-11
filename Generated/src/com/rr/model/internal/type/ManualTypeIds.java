/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.internal.type;

public interface ManualTypeIds {
    public int EXECTYPE_NEW = 0;
    public int EXECTYPE_PARTIALFILL = 1;
    public int EXECTYPE_FILL = 2;
    public int EXECTYPE_DONEFORDAY =3;
    public int EXECTYPE_CANCELED = 4;
    public int EXECTYPE_REPLACED = 5;
    public int EXECTYPE_PENDINGCANCEL = 6;
    public int EXECTYPE_STOPPED = 7;
    public int EXECTYPE_REJECTED = 8;
    public int EXECTYPE_SUSPENDED = 9;
    public int EXECTYPE_PENDINGNEW = 10;
    public int EXECTYPE_CALCULATED = 11;
    public int EXECTYPE_EXPIRED = 12;
    public int EXECTYPE_RESTATED = 13;
    public int EXECTYPE_PENDINGREPLACE = 14;
    public int EXECTYPE_TRADE = 15;
    public int EXECTYPE_TRADECORRECT = 16;
    public int EXECTYPE_TRADECANCEL = 17;
    public int EXECTYPE_ORDERSTATUS = 18;
    public int EXECTYPE_UNKNOWN = 19;
}
