/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import com.rr.core.lang.ErrorCode;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;

public class FileUtils {
    
    private static final Logger           _log          = LoggerFactory.console( FileUtils.class );
    private static final SimpleDateFormat _fileDateFmt  = new SimpleDateFormat( "yyyyMMdd_HHmm" );
    private static final ErrorCode        RENAME_ERR    = new ErrorCode( "FUT100", "Unable to rename file");
    

    public static void mkDir( String dir ) throws FileException {
        
        if ( dir == null ) return;
        
        File file = new File( dir );
        
        if ( ! file.exists() ) {
            
            boolean created = file.mkdirs();
            
            if ( !created ) {
                throw new FileException( "mkDir() failed to create dir "+ dir );
            }
            
        } else if ( !file.isDirectory() ) {
            throw new FileException( "mkDir() error " + dir + " exists and is not a directory" );
        }
    }

    public static void mkDirIfNeeded( String fileName ) throws FileException {
        String dir = getDirName( fileName );
        mkDir( dir );
    }

    public static void rm( String fileName ) throws FileException {
        File f = new File( fileName );

        if ( !f.exists() ) return; 

        if ( !f.canWrite() ) throw new FileException( "rm() file is write protected " + fileName );

        if ( f.isDirectory() ) {
            String[] files = f.list();
            
            if ( files.length > 0 ) {
                throw new FileException( "rm() directory is not empty " + fileName );
            }
        }

        boolean deleted = f.delete();

        if ( !deleted ) throw new FileException( "Delete: deletion failed for " + fileName );

        _log.info( "DELETED : " + fileName );
    }
    
    public static void rmRecurse( String fileName, boolean log ) throws FileException {
        
        if ( log ) _log.info( "RECURSIVE DELETE : " + fileName );
        		
        File f = new File( fileName );

        if ( !f.exists() ) return; 

        if ( !f.canWrite() ) throw new FileException( "rm() file is write protected " + fileName );

        if ( f.isDirectory() ) {
            String[] files = f.list();
            
            for( String child : files ) {
                rmRecurse( child, log );
            }
        }

        boolean deleted = f.delete();

        if ( !deleted ) throw new FileException( "Delete: deletion failed" );
    }
    
    public static void archive( String fromFile, String destFile ) throws FileException { 
        int len;
        byte[] buf = new byte[8192];

        try {
            File inFile = new File( fromFile );
            
            if ( !inFile.exists() ) throw new FileException( "archive() file " + fromFile + " does not exist" );
            
            if ( !inFile.canWrite() ) throw new FileException( "archive() file " + fromFile +" is write protected so cant archive" );
            
            FileInputStream in = new FileInputStream( fromFile );
            GZIPOutputStream out = new GZIPOutputStream( new FileOutputStream( destFile ) );
        
            while ( (len = in.read(buf)) > 0 ) {
                out.write( buf, 0, len );
            }
            
            in.close();

            out.finish();
            out.close();
            
            rm( fromFile );
            
        } catch( IOException e ) {
            throw new FileException( "archiveFile() from=" + fromFile + ", to=" + destFile + " failed " + e.getMessage(), e );
        }
    }

    public static void checkDirExists( String dir, boolean mustBeEmpty ) throws FileException {
        File f = new File( dir );

        if ( !f.exists() ) throw new FileException( "checkDirExists() directory " + dir + " doesnt exist" ); 

        if ( f.isDirectory() ) {
            String[] files = f.list();
            
            if ( mustBeEmpty && files.length > 0 ) {
                throw new FileException( "checkDirExists() directory " + dir + " is not empty " );
            }
        } else {
            throw new FileException( "checkDirExists() " + dir + " is NOT a directory" );
        }

        if ( !f.canWrite() ) throw new FileException( "checkDirExists() directory " + dir + " is write protected" );
    }

    public static void close( BufferedWriter bufferedWriter ) {
        try {
            if ( bufferedWriter != null ) {
                 bufferedWriter.flush();
                 bufferedWriter.close();
            }
        } catch( IOException e ) {
            // ignore
        }
    }

    public static void rmIgnoreError( String tmpFile ) {
        try {
            rm( tmpFile );
        } catch( FileException e ) {
            // ignore
        }
    }

    public static void rmForceRecurse( String tmpFile, boolean log ) {
        try {
            if ( tmpFile != null               && 
                 !tmpFile.equals( "/" )        && 
                 !tmpFile.equals( "./" )       && 
                 !tmpFile.equals( "./." )      && 
                 !tmpFile.startsWith( "./.."  ) ) {
                
                rmRecurse( tmpFile, log );
            }
        } catch( FileException e ) {
            // ignore
        }
    }

    public static String getDirName( String name ) {
        int idx = name.lastIndexOf( '/' );
        String dir = null;
        
        if ( idx != -1  ) {
            dir = name.substring( 0, idx );
        } else {
            idx = name.lastIndexOf( '\\' );
            
            if ( idx != -1  ) {
                dir = name.substring( 0, idx );
            }
        }
        
        return dir;
    }

    public static String formRollableFileName( String fname, int rollNumber, String extension ) {

        String date;

        synchronized( _fileDateFmt ) {
            date = _fileDateFmt.format( new Date() );
        }

        String base = fname.toString();

        int idx = base.indexOf( extension );

        if ( idx > 0 ) {
            base = base.substring( 0, idx );
        }

        return base + "_" + date + "_" + rollNumber + extension;
    }

    public static boolean backup( String fname ) {
        String bkupName = formRollableFileName( fname, 1, ".bkup" );
        
        File file     = new File( fname );
        
        try {
            rmIgnoreError( bkupName );

            File bkupFile = new File( bkupName );
            
            if ( file.renameTo( bkupFile ) ) {
                return true;
            }
            
            _log.error( RENAME_ERR, " " + fname + " to " + bkupName );
            
        } catch( Exception e ) {
            _log.error( RENAME_ERR, " " + fname + " to " + bkupName, e );
        }
        
        return false;
    }

    public static boolean isFile( String fname ) {
        File f = new File( fname );
        return f.exists() && f.isFile();
    }

    public static void close( Closeable resource ) {
        try {
            if ( resource != null ) {
                 resource.close();
            }
        } catch( IOException e ) {
            // ignore
        }
    }

    /**
     * read the file and return array of String with each line having an array entry
     *
     * @param lines list to put each line read from file, WILL BE CLEARED FIRST
     * @param fileName
     * @param stripComments if true then exclude all lines starting with '#'
     * @param stripWhiteSpace if true strip out blank lines and whitespace around the lines
     * @throws IOException 
     */
    public static void read( List<String> lines, String fileName, boolean stripComments, boolean stripWhiteSpace ) throws IOException {

        if ( fileName == null )  throw new IOException( "Missing filename" );
        
        BufferedReader rdr = new BufferedReader( new InputStreamReader( new FileInputStream( fileName ) ) );

        lines.clear();
        
        _log.info( "FileUtils.read  file " + fileName );

        try {
            int count = 0;
                    
            String line;
            while( (line = rdr.readLine()) != null ) {
                if ( stripWhiteSpace ) line = line.trim();
                
                if ( line.startsWith( "#" ) && stripComments ) {
                    // strip
                } else {
                    ++count;
                    lines.add( line );
                }
            }

            _log.info( "FileUtils.read entries=" + count );
            
        } finally {
            FileUtils.close( rdr );
        }
    }
}
