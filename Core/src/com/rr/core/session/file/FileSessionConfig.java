/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session.file;

import com.rr.core.recycler.MessageRecycler;
import com.rr.core.session.SessionConfig;
import com.rr.core.utils.file.FileLog;


public class FileSessionConfig extends SessionConfig {
    private String[]            _filesIn;
    private FileLog             _fileOut;
    
    public FileSessionConfig() {
        super();
    }
    
    public FileSessionConfig( Class<? extends MessageRecycler> recycler ) {
        super( recycler );
    }
    
    public FileSessionConfig( String id ) {
        super( id );
    }

    public String[] getFilesIn() {
        return _filesIn;
    }
    
    public FileLog getFileOut() {
        return _fileOut;
    }
    
    public void setFilesIn( String[] filesIn ) {
        _filesIn = filesIn;
    }
    
    public void setFileOut( FileLog fileOut ) {
        _fileOut = fileOut;
    }
}
