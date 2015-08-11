/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rr.core.component.SMTComponent;
import com.rr.core.component.SMTSingleComponentLoader;
import com.rr.core.lang.RTStartupException;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.utils.SMTException;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.md.us.cme.CMEConfig.Channel;
import com.rr.md.us.cme.CMEConfig.Product;
import com.rr.md.us.cme.CMEConnection.Protocol;
import com.rr.model.xml.XMLDuplicateNodeException;
import com.rr.model.xml.XMLException;
import com.rr.model.xml.XMLHelper;
import com.rr.model.xml.XMLMissingException;

/**
 <configuration environment="Production" updated="2013/09/26-21:10:16">
        <channel id="7" label="CME Globex Equity Futures">
                <products>
                        <product code="ES">
                                <group code="ES">
                                        <subchannel>1</subchannel>
                                </group>
                        </product>
                </products>
                <connections>
                        <connection id="7H3A">
                                <type feed-type="H">Historical Replay</type>
                                <protocol>TCP/IP</protocol>
                                <host-ip>205.209.222.43</host-ip>
                                <port>10000</port>
                                <feed>A</feed>
                        </connection>
                        <connection id="7H4A">
                                <type feed-type="H">Historical Replay</type>
                                <protocol>TCP/IP</protocol>
                                <host-ip>205.209.220.44</host-ip>
                                <port>10000</port>
                                <feed>A</feed>
                        </connection>
 */

public class XMLCMEConfigLoader implements SMTSingleComponentLoader {

    private String      _configFile;
    private XMLHelper   _helper;
    private String      _id;

    public XMLCMEConfigLoader( String configFile ) {
        _configFile = configFile;
    }

    public XMLCMEConfigLoader() {
    }

    @Override
    public SMTComponent create( String id ) throws SMTException {
        
        _id = id;
        
        CMEConfig cmeConfig = load();
        
        return cmeConfig;
    }
    
    public CMEConfig load() throws RTStartupException {
        _helper = new XMLHelper( _configFile );
        
        CMEConfig config = null;
        
        try {
            _helper.parse();

            Element configuration = _helper.getRoot();
            
            String name = _helper.getAttr( configuration, "environment", true );

            config = new CMEConfig( _id, new ViewString(name) );
            
            loadConfiguration( config, configuration );

        } catch( Exception e ) {
            
            throw new RTStartupException( ", file=" + _configFile + " : " + e.getMessage(), e );
        }
        
        return config;
    }


    private void loadConfiguration( CMEConfig config, Element templates ) throws XMLException {
        List<Node> list = _helper.getChildElements( templates, "channel", true );

        for( Node node : list ) {
            if ( node.getNodeType() == Node.ELEMENT_NODE ) {
                loadChannel( config, (Element)node );
            }
        }
    }

    private void loadChannel( CMEConfig config, Element node ) throws XMLException {
        
        // <channel id="7" label="CME Globex Equity Futures">

        int    id    = _helper.getAttrInt( node, "id", true );
        String label = _helper.getAttr( node, "label", true );
        
        @SuppressWarnings( "boxing" )
        CMEConfig.Channel channel = new CMEConfig.Channel( id, label );
        
        addNodesToCat( channel, node );
        
        config.add( channel );
    }

    private void addNodesToCat( Channel channel, Element node ) throws XMLMissingException, XMLException, XMLDuplicateNodeException {
        addProducts( channel, node );
        addConnections( channel, node );
    }

    private void addProducts( Channel channel, Element node ) throws XMLException {
        List<Node> products = _helper.getChildElements( node, "products", true );
        
        if ( products.size() != 1 ) {
            throw new SMTRuntimeException( "Expected a single products element found " + products.size() );
        }
        
        Node productsNode = products.get( 0 );
        
        NodeList nodeList = productsNode.getChildNodes();
        for( int i=0 ; i < nodeList.getLength() ; i++ ) {
            Node subNode = nodeList.item( i );
            if ( subNode.getNodeType() == Node.ELEMENT_NODE ) {
                Element e = (Element)subNode;
                
                // <product code="ES">
                String code = _helper.getAttr( e, "code", true );
                
                CMEConfig.Product product = getProduct( e, code );
                
                channel.addProduct( product );
            }
        }
    }

    /*
                         <connection id="7H3A">
                                <type feed-type="H">Historical Replay</type>
                                <protocol>TCP/IP</protocol>
                                <host-ip>205.209.222.43</host-ip>
                                <port>10000</port>
                                <feed>A</feed>
                        </connection>
                        <connection id="7IB">
                                <type feed-type="I">Incremental</type>
                                <protocol>UDP/IP</protocol>
                                <ip>224.0.27.1</ip>
                                <host-ip>205.209.211.46</host-ip>
                                <host-ip>205.209.212.46</host-ip>
                                <port>10001</port>
                                <feed>B</feed>
                        </connection>
                        

     */
    private void addConnections( Channel channel, Element node ) throws XMLException {
        List<Node> conns = _helper.getChildElements( node, "connections", true );
        
        if ( conns.size() != 1 ) {
            throw new SMTRuntimeException( "Expected a single connections element found " + conns.size() );
        }
        
        Node connsNode = conns.get( 0 );
        
        NodeList nodeList = connsNode.getChildNodes();
        for( int i=0 ; i < nodeList.getLength() ; i++ ) {
            Node subNode = nodeList.item( i );
            if ( subNode.getNodeType() == Node.ELEMENT_NODE ) {
                Element connNode = (Element)subNode;
                
                String id = _helper.getAttr( connNode, "id", true );

                FeedType ft    = getFeedType( connNode );
                String ip      = _helper.getChildElementValue( connNode, "ip", false );
                
                String[] hostIpList = _helper.getChildElementValues( connNode, "host-ip", true );

                String portStr = _helper.getChildElementValue( connNode, "port", true );
                String feedStr = _helper.getChildElementValue( connNode, "feed", true );
                
                Protocol protocol = getProtocol( connNode );
                int      port     = Integer.parseInt( portStr );
                Feed     feed     = Feed.valueOf( feedStr );
                
                ZString[] hostIPs = new ZString[ hostIpList.length ];
                int next = 0;
                
                for( String val : hostIpList ) {
                    hostIPs[ next++ ] = new ViewString( val );
                }
                
                CMEConnection conn = new CMEConnection( new ViewString(id), ft, protocol, new ViewString(ip), hostIPs, port, feed );
                
                channel.addConnection( conn );
            }
        }
    }

    private Protocol getProtocol( Element connNode ) throws XMLException {
        String protocolStr = _helper.getChildElementValue( connNode, "protocol", true );

        if ( "TCP/IP".equals( protocolStr ) ) return Protocol.TCP;
        if ( "UDP/IP".equals( protocolStr ) ) return Protocol.UDP;
        
        throw new SMTRuntimeException( "Invalid protocol string " + protocolStr );
    }

    private FeedType getFeedType( Element connNode ) throws XMLMissingException, XMLDuplicateNodeException {
        // <type feed-type="H">Historical Replay</type>

        Node ftn = _helper.getChildElement( connNode, "type", true );
        String type = _helper.getAttr( ftn, "feed-type", true );
        
        FeedType f = FeedType.lookup( (byte)type.charAt( 0 ) );
        
        return f;
    }

    /**
                <group code="ES">
                        <subchannel>1</subchannel>
                </group>
     *  
     */
    private Product getProduct( Element e, String prodCode ) throws XMLException {
        List<Node> groupList = _helper.getChildElements( e, "group", true );
        
        if ( groupList.size() != 1 ) {
            throw new SMTRuntimeException( "Expected a single group element found " + groupList.size() );
        }
        
        Node groupNode = groupList.get( 0 );
        String groupCode = _helper.getAttr( groupNode, "code", true );
        
        List<Node> subchannelList = _helper.getChildElements( groupNode, "subchannel", true );
        
        if ( subchannelList.size() != 1 ) {
            throw new SMTRuntimeException( "Expected a single group element found " + subchannelList.size() );
        }
        
        String subchannelStr = _helper.getChildElementValue( groupNode, "subchannel", true );

        SubChannel subChannel = SubChannel.get( Integer.parseInt(subchannelStr) );
        Product p = new Product( new ViewString(prodCode), new ViewString(groupCode), subChannel );
        
        return p;
    }

}
