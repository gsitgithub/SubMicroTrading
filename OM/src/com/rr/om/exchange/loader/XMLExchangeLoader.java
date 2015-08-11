/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.exchange.loader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.rr.core.lang.RTStartupException;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.model.xml.XMLException;
import com.rr.model.xml.XMLHelper;
import com.rr.model.xml.XMLMissingException;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.model.instrument.Auction;
import com.rr.om.model.instrument.ExchangeSession;
import com.rr.om.model.instrument.MultiMarketExchangeSession;
import com.rr.om.model.instrument.SingleExchangeSession;

/**
 * XMLExchangeLoader loads XML definitions from file and merges with the hard coded info to instantiate the
 * exchange instance. 
 *
 */
public class XMLExchangeLoader {

    private final  String _configFile;
    private XMLHelper _helper;
    
    public XMLExchangeLoader( String configFile ) {
        _configFile = configFile;
    }
    
    public void load() throws RTStartupException {
        _helper = new XMLHelper( _configFile );
        
        try {
            _helper.parse();

            Element exchanges = _helper.getElement( "Exchanges", true );
            
            loadExchanges( exchanges );
            
        } catch( Exception e ) {
            
            throw new RTStartupException( ", file=" + _configFile + " : " + e.getMessage(), e );
        }
    }

    private void loadExchanges( Element exchanges ) throws XMLException {
        List<Node> exchangeList = _helper.getChildElements( exchanges, "Exchange", true );

        for( Node node : exchangeList ) {
            if ( node.getNodeType() == Node.ELEMENT_NODE ) {
                loadExchange( (Element)node );
            }
        }
    }

    /**
        <Exchange mic="XLON" timezone="Europe/London">
            <HalfDays>24/12/2010,31/12/2010</HalfDays>
            <OpenTime>07:50:00</OpenTime>
            <CloseTime>17:00:00</CloseTime>
            <HalfDayClose>13:00:00</HalfDayClose>
            <Sessions expireTimeForSendEODEvents="17:00:00">
                <Session>
                    <OpenAuction  ref="OpenTime"  startOffset="+00:00:00" endOffset="+00:10:00"/>
                    <CloseAuction ref="CloseTime" startOffset="-00:29:30" endOffset="-00:00:00"/>
                </Session>
                <Segmets set="IOB,IOBE,IOBU,ITBB,ITBU">
                    <OpenAuction  ref="OpenTime"  startOffset="+00:10:00" endOffset="+00:25:00"/>
                    <CloseAuction ref="CloseTime" startOffset="-01:29:30" endOffset="-00:00:00"/>
                </Segments>
            </Sessions>
        </Exchange>
     * @throws XMLMissingException 
    */
    private void loadExchange( Element node ) throws XMLException {
        String          micCode          = getMIC( node );
        TimeZone        timezone         = getTimeZone( node );
        List<Long>      halfDays         = getHalfDays( node,  timezone );
        Calendar        openTime         = getTime( node, "OpenTime", timezone );                    
        Calendar        closeTime        = getTime( node, "CloseTime", timezone );                    
        Calendar        halfDayCloseTime = getTime( node, "HalfDayClose", timezone );                    
        Element         sessions         = _helper.getChildElement( node, "Sessions", true );
        ExchangeSession session          = getExchangeSession( sessions, openTime, halfDayCloseTime, closeTime, timezone );
                                                               
        Calendar        eodSendEventTime = getAttrTime( sessions, "expireTimeForSendEODEvents", timezone );                    
        
        if ( eodSendEventTime == null || eodSendEventTime.getTimeInMillis() == 0 ) {
            throw new XMLException( "XMLExchangeLoader.loadExchange() missing expireTimeForSendEODEvents in Sessions node for " + micCode );
        }
        
        instantiateExchange( micCode, timezone, halfDays, eodSendEventTime, session );
    }

    private String getMIC( Element node ) throws XMLMissingException {
        return _helper.getAttr( node, "mic", true );
    }

    private List<Long> getHalfDays( Element node, TimeZone timezone ) throws XMLException {
        List<Long> list = new ArrayList<Long>();
        String halfDays = _helper.getChildElementValue( node, "HalfDays", false );
        if ( halfDays != null ) {
            String[] dates = halfDays.split( "," );
            for( String date : dates ) {
                list.add( new Long( TimeZoneCalculator.ddmmyyyyToUTC( timezone, date ) ) );
            }
        }
        return list;
    }

    private TimeZone getTimeZone( Element node ) throws XMLMissingException {
        String tzs = _helper.getAttr( node, "timezone", true );
        TimeZone t = TimeZone.getTimeZone( tzs );
        return t;
    }

    private Calendar getTime( Element node, String elemName, TimeZone timezone ) throws XMLException {
        String time = _helper.getChildElementValue( node, elemName, false );
        Calendar cal = null;
        if ( time != null ) {
            cal = TimeZoneCalculator.getTimeAsToday( time, timezone );
        }
        return cal;
    }

    private Calendar getAttrTime( Element node, String attrName, TimeZone timezone ) throws XMLException {
        String time = _helper.getAttr( node, attrName, false );
        Calendar cal = null;
        if ( time != null ) {
            cal = TimeZoneCalculator.getTimeAsToday( time, timezone );
        }
        return cal;
    }

    /**
     * sample multi session exchange :-
     * 
           <Sessions expireTimeForSendEODEvents="17:00:00">
                <Session>
                    <OpenAuction  ref="OpenTime"  startOffset="+00:00:00" endOffset="+00:10:00"/>
                    <CloseAuction ref="CloseTime" startOffset="-00:29:30" endOffset="-00:00:00"/>
                </Session>
                <Segments id="id" set="IOB,IOBE,IOBU,ITBB,ITBU">
                    <OpenAuction  ref="OpenTime"  startOffset="+00:10:00" endOffset="+00:25:00"/>
                    <CloseAuction ref="CloseTime" startOffset="-01:29:30" endOffset="-00:00:00"/>
                </Segments>
           </Sessions>
     * @param node
     * @param openTime
     * @param closeTime
     * @param closeTime2 
     * @param timezone
     * @return
     * @throws XMLMissingException 
     */
    private ExchangeSession getExchangeSession( Element  sessions, 
                                                Calendar openTime, 
                                                Calendar halfDayCloseAt, 
                                                Calendar closeTime, 
                                                TimeZone timezone ) throws XMLException {
        
    
        Element session         = _helper.getChildElement(  sessions, "Session",  false );
        List<Node> segmentList  = _helper.getChildElements( sessions, "Segments", false );
        
        SingleExchangeSession sess;
        
        ZString id = new ViewString( "default" );
        if ( session == null )  {
            sess = new SingleExchangeSession( id, openTime, halfDayCloseAt, closeTime, null, null );
        } else if ( segmentList == null ) {
            Auction openAuction  = getAuction( session, "OpenAuction",  Auction.Type.Open,  openTime, closeTime );
            Auction closeAuction = getAuction( session, "CloseAuction", Auction.Type.Close, openTime, closeTime );
            
            sess = new SingleExchangeSession( id, openTime, halfDayCloseAt, closeTime, openAuction, closeAuction );
        } else {

            Map<ZString, ExchangeSession> sessMap = new HashMap<ZString, ExchangeSession>();

            Auction openAuc  = getAuction( session, "OpenAuction",  Auction.Type.Open,  openTime, closeTime );
            Auction closeAuc = getAuction( session, "CloseAuction", Auction.Type.Close, openTime, closeTime );
            
            for( Node segmentNode : segmentList ) {
                if ( segmentNode.getNodeType() == Node.ELEMENT_NODE ) {

                    String segId      = _helper.getAttr( segmentNode, "id", true );
                    String segmentSet = _helper.getAttr( segmentNode, "set", true );
                    
                    Auction openSegAuction  = getAuction( segmentNode, "OpenAuction",  Auction.Type.Open,  openTime, closeTime );
                    Auction closeSegAuction = getAuction( segmentNode, "CloseAuction", Auction.Type.Close, openTime, closeTime );
                    
                    ExchangeSession segSess = new SingleExchangeSession( new ViewString(segId), openTime, halfDayCloseAt, 
                                                                         closeTime, openSegAuction, closeSegAuction );

                    String[] segments = segmentSet.split( "," );
                    
                    for( String segment : segments ) {
                        sessMap.put( new ViewString(segment), segSess );
                    }
                }
            }
            
            sess = new MultiMarketExchangeSession( id, openTime, null, null, halfDayCloseAt, closeTime, openAuc, null, closeAuc, sessMap );
        }
        
        return sess;
    }

    /**
         ref="OpenTime"  startOffset="+00:00:00" endOffset="+00:10:00"

     * @param session
     * @param string
     * @param openTime
     * @param closeTime
     * @return
     * @throws XMLMissingException 
     */
    private Auction getAuction( Node         sessionElem, 
                                String       auctionElemName, 
                                Auction.Type type, 
                                Calendar     openTime, 
                                Calendar     closeTime ) throws XMLException {
        
        Element auctionElem = _helper.getChildElement( sessionElem, auctionElemName,  false );
        String  ref         = _helper.getAttr( auctionElem, "ref",         true );
        String  startOffset = _helper.getAttr( auctionElem, "startOffset", true );
        String  endOffset   = _helper.getAttr( auctionElem, "endOffset",   true );

        if ( ref.equalsIgnoreCase( "OpenTime" ) ) {
            return getAuction( type, openTime, startOffset, endOffset );
        } else if ( ref.equalsIgnoreCase( "CloseTime" ) ) {
            return getAuction( type, closeTime, startOffset, endOffset );
        } else {
            throw new XMLException( "XMLExchangeLoader.getAuction() ref attr must be one either OpenTime or CloseTime not " + ref );
        }
    }

    private Auction getAuction( Auction.Type type, Calendar base, String startOffset, String endOffset ) {
        Calendar startAuction = TimeZoneCalculator.adjust( (Calendar)base.clone(), startOffset );
        Calendar endAuction   = TimeZoneCalculator.adjust( (Calendar)base.clone(), endOffset );
        
        return new Auction( startAuction, endAuction, type );
    }

    private void instantiateExchange( String            micCode, 
                                      TimeZone          timezone, 
                                      List<Long>        halfDays, 
                                      Calendar          eodExpireEventSend,
                                      ExchangeSession   session ) {

        ExchangeManager.instance().register( new ViewString(micCode), timezone, halfDays, eodExpireEventSend, session );
        
    }
}
