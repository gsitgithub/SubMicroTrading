/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.DisconnectedException;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.Receiver;
import com.rr.core.session.SessionException;
import com.rr.core.session.ThreadedReceiver;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.core.utils.file.FileLog;


public class BaseFileSession extends AbstractSession {

    protected static final Logger       _log = LoggerFactory.create( BaseFileSession.class );

    private final String[]              _filesIn;
    private final FileLog               _fileOut;

    private       Receiver              _receiver;

    private       BufferedInputStream   _fileIn;
    private       byte[]                _buf = new byte[ Constants.MAX_BUF_LEN ];
    
    private       boolean               _allRead;       // ALL FILES FINISHED

    private       int                   _nextFileIdx = 0;

    private       String                _curFileName;

    
    public BaseFileSession( String              name, 
                            MessageRouter       inboundRouter, 
                            FileSessionConfig   config, 
                            MessageDispatcher   dispatcher, 
                            Encoder             encoder, 
                            Decoder             decoder,
                            Decoder             recoveryDecoder ) {

        super( name, inboundRouter, config, dispatcher, encoder, decoder, recoveryDecoder );
        
        _filesIn    = config.getFilesIn();
        _fileOut    = config.getFileOut();
    }

    @Override
    public synchronized void stop() {
        super.stop();
        
        FileUtils.close( _fileIn );

        _fileOut.close();
    }

    @Override
    public synchronized void connect() {

        if ( !isConnected() ) {
            openNext();
    
            if ( _receiver == null ) {
                _receiver = new ThreadedReceiver( this, ThreadPriority.Other );
            }
            
            _receiver.start();
            _outboundDispatcher.start();
            
            _fileOut.open();
            
            setState( State.Connected );
        }
    }

    @Override
    public void processNextInbound() throws Exception {
        if ( isPaused() ) return;
        
        int b1 = _fileIn.read();
        
        while( b1 == -1 && !_allRead ) {
            openNext();
            
            b1 = _fileIn.read();
        }

        if ( _allRead ) {
            throw new SMTRuntimeException( "No more files to read" );
        }
        
        int b2 = _fileIn.read();
        int b3 = _fileIn.read();
        int b4 = _fileIn.read();
        
        int bytes = (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;

        if ( bytes == 0 ) return;
        
        if ( bytes > _buf.length ) {
            throw new SMTRuntimeException( "Cant read record as length of " + bytes + " greater than bufSize of " + _buf.length );
        }
        
        int read = _fileIn.read( _buf, 0, bytes );
        
        if ( read != bytes ) {
            throw new SMTRuntimeException( "Only able to read " + read + " bytes not the expected length of " + bytes );
        }
        
        Message msg = _decoder.decode( _buf, 0, bytes );

        logInEvent( null );
        
        // @TODO should check actual decode left and shift left / set prebuffered for remaining
        // CME only put 1 message per packet
        
        if ( msg != null ) {
            logInEventPojo( msg );

            invokeController( msg );
        }
    }

    @Override
    public void handleForSync( Message msg ) {
        handleNow( msg );
    }

    /**
     * processIncoming is NOT used by the NON blocking multiplexors
     */
    @Override
    public void processIncoming() { 
        try {
            while( ! _allRead ) { 
                processNextInbound();
            }
            
        } catch( SessionException e ) {

            logInboundError( e );
            disconnect( !e.isForcedLogout() ); // pause session on forced logout
            
        } catch( DisconnectedException e ) {

            disconnect( true );

        } catch( IOException e ) {

            disconnect( true );
            openNext();

        } catch( RuntimeDecodingException e ) {
            logInboundDecodingError( e );
        } catch( Exception e ) {
            // not a socket error dont drop socket
            logInboundError( e );
        }
    }

    @Override
    public boolean canHandle() {
        return isConnected();
    }

    @Override
    public void startWork() {
        connect();
    }

    @Override
    public void stopWork() {
        stop();
    }

    @Override
    public void attachReceiver( Receiver receiver ) {
        _receiver = receiver;
    }

    @Override
    public void logInboundError( Exception e ) {
        if ( e instanceof IOException ) {
            _log.info( "IO Error in " + getComponentId() + ", file=" + _curFileName + ", switch to next if avail" );
            openNext();
        } else {
            _log.error( ERR_IN_MSG, "Error in " + getComponentId() + ", file=" + _curFileName + " : " + e.getMessage(), e );
        }
    }

    protected void invokeController( Message msg ) {
        dispatchInbound( msg );
    }

    @Override
    protected void disconnectCleanup() {
        stop();
    }

    @Override
    protected void sendNow( Message msg ) throws IOException {
        _encoder.encode( msg );

        if ( _logStats ) {
            lastSent( Utils.nanoTime() );
        }
        
        _fileOut.log( _encoder.getBytes(), _encoder.getOffset(), _encoder.getLength() );
        
        if ( _chainSession != null && _chainSession.isConnected() ) {
            _chainSession.handle( msg );
        } else {
            outboundRecycle( msg );
        }
    }

    private void openNext() {
        if ( _nextFileIdx > _filesIn.length ) {
            _allRead = true;
            throw new SMTRuntimeException( "AllDone" );
        }

        _curFileName = _filesIn[ _nextFileIdx ];
        
        try {
            _fileIn = new BufferedInputStream(new FileInputStream( new File( _curFileName ) ) );
        } catch( FileNotFoundException e ) {
            throw new SMTRuntimeException( "FileNonBlockaingSession Unable to open " + _curFileName + " : " + e.getMessage(), e );
        }
        
        ++_nextFileIdx;
    }

    @Override
    public boolean discardOnDisconnect( Message msg ) {
        return false;
    }

    @Override
    protected void sendChain( Message msg, boolean canRecycle ) {
        if ( getChainSession() != null ) {
            getChainSession().handle( msg );
        }
    }

    @Override
    protected void logInEvent( ZString event ) {
        if ( _logEvents ) {
            _log.infoLarge( event );
        }
    }

    @Override
    protected void logOutEvent( ZString event ) {
        if ( _logEvents ) {
            _log.infoLarge( event );
        }
    }

    @Override public void init() throws PersisterException { /* nothing */ }
    @Override public void internalConnect() { /* nothing */ }
    @Override public void persistLastInboundMesssage() { /* nothing */ }
    @Override public void threadedInit() { /* nothing */ }
    @Override protected Message recoveryDecode( byte[] buf, int offset, int len, boolean inBound ) { return null; }
    @Override protected Message recoveryDecodeWithContext( byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, boolean inBound ) { return null; }
    @Override protected void persistIntegrityCheck( boolean inbound, long key, Message msg ) { /* nothing */ }
}
