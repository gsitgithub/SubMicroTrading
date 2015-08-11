/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

public class XMLHelper {

    private final String   _file;
    private       Document _dom;
    private       Element  _root;

    public XMLHelper( String file ) {
        _file = file;
    }

    public void parse() throws XMLException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();

            _dom = db.parse( _file );

        } catch( SAXParseException e ) {
            throw new XMLException( "Unable to parse " + _file + ", err=" + e.getMessage() + ", line=" + e.getLineNumber() + ", col=" + e.getColumnNumber(), e );
        } catch( Exception e ) {
            throw new XMLException( "Unable to parse " + _file + ", err=" + e.getMessage(), e );
        }

        _root = _dom.getDocumentElement();
    }
    
    public Element getRoot() {
        return _root;
    }
    
    /**
     * search from root for an element identified by tagName
     * @param tagName
     * @return 
     * @throws XMLMissingException 
     * @throws XMLDuplicateNodeException 
     */
    public Element getElement( String tagName ) throws XMLDuplicateNodeException, XMLMissingException {
        return getElement( _root, tagName, false );
    }

    public Element getElement( String tagName, boolean isMand ) throws XMLDuplicateNodeException, XMLMissingException {
        return getElement( _root, tagName, isMand );
    }

    /**
     * 
     * @param tagName
     * @param isMand
     * @return
     */
    public Element getElement( Element from, 
                               String  tagName, 
                               boolean isMand ) throws XMLDuplicateNodeException, XMLMissingException {

        //get a nodelist of  elements
        NodeList nl = from.getElementsByTagName( tagName );

        Element e = null;
        
        if ( nl == null || nl.getLength() <= 0 ) {
            if ( isMand ) throw new XMLMissingException( "No node with tag " + tagName + " under element " + from.getNodeName(), from );
        } else {
            if ( nl.getLength() > 1 ) throw new XMLDuplicateNodeException( "Found " + nl.getLength() + " nodes with tag " + tagName + 
                                                                           " under element " + from.getNodeName(), from );
            
            e = (Element) nl.item(0);
        }
        
        return e;
    }
    
    public NodeList getElements( String tagName, boolean isMand ) throws XMLMissingException {
        return getElements( _root, tagName, isMand ); 
    }

    public NodeList getElements( Element from, String tagName, boolean isMand ) throws XMLMissingException { 
        
        //get a nodelist of  elements
        NodeList nl = from.getElementsByTagName( tagName );

        if ( nl == null || nl.getLength() == 0 ) {
            if ( isMand ) throw new XMLMissingException( "No node with tag " + tagName + " under element " + from.getNodeName(), from );
        }

        return nl;
    }

    public String getAttr( Node node, String attr, boolean isMand, String defaultVal ) throws XMLException {
        
        String val = getAttr( node, attr, isMand );
        
        if ( val == null ) {
            val = defaultVal;
        }
        
        return val;
    }
    
    public String getAttr( Node node, String attr, boolean isMand ) throws XMLMissingException {
        
        NamedNodeMap attrs = node.getAttributes();
        String val = null;
        
        if ( attrs == null ) {
            if ( isMand ) throw new XMLMissingException( "Node " + node.getNodeName() + " has no attributes so cant get attr " + attr, node );
        } else {
            Node nval = attrs.getNamedItem( attr );

            if ( nval == null) {
                if ( isMand ) throw new XMLMissingException( "No attribute " + attr + " under node " + node.getNodeName(), node );
            } else {
                val = nval.getNodeValue();
            }
        }
        
        return (val == null) ? null : val.trim();
    }

    public static String getAttrsAsString( Node node ) {
        
        if ( node == null ) return "";
        
        NamedNodeMap attrs = node.getAttributes();
        StringBuilder val = new StringBuilder();
        
        if ( attrs != null ) {
            for( int i=0; i < attrs.getLength() ; i++ ) {
                Node nval = attrs.item( i );
    
                if ( nval != null) {
                    if ( i > 0 ) val.append( ", " );
                    
                    val.append( nval.getNodeName() ).append( "=" ).append( nval.getNodeValue() );
                }
            }
        }
        
        return val.toString().trim();
    }

    
    public int getAttrInt( Node node, String attr, boolean isMand ) throws XMLException {
        
        return getAttrInt( node, attr, isMand, 0 );
    }

    public static String getNodeDesc( Node node ) {

        String tree = "";

        if ( node == null ) return tree;
        
        if ( node.getParentNode() != null ) {
            tree += getNodeDesc( node.getParentNode() ) + ".";
        }
        
        return tree + node.getNodeName();
    }

    public String getChildElementValue( Node node, String tagName, boolean isMand ) throws XMLException {
        
        Node n = getChildElement( node, tagName, isMand );
        
        return getElementTextValue( n );
    }

    public String getElementTextValue( Node n ) {
        String val = null;
        
        if ( n != null ) {
            
            val = n.getNodeValue();

            if ( val == null ) {
                if ( n.getNodeType() == Node.ELEMENT_NODE ) {
                    Element em = (Element) n;
                    
                    val = em.getTextContent();
                }
            }
        }
        
        return (val == null) ? null : val.trim();
    }

    public String[] getElementValues( String tagName, boolean isMand ) throws XMLMissingException {
        
        NodeList nl = getElements( tagName, isMand );
        String[] val;
        
        if ( nl != null && nl.getLength() > 0 ) {
            
            int size = nl.getLength();
            
            val = new String[ size ];
            
            for ( int i=0 ; i < size ; ++i ) {
                val[i] = getElementTextValue( nl.item( i ) );
            }
            
        } else {
            val = new String[0];
        }
        
        return val;
    }

    public List<Node> getChildElements( Node node, String tagName, boolean isMand ) throws XMLMissingException {
        
        List<Node> list = new ArrayList<Node>();

        if ( node != null ) {
            NodeList children = node.getChildNodes();
            
            for ( int i=0 ; i < children.getLength() ; ++i ) {
                Node n = children.item( i );
                
                if ( tagName.equals( n.getNodeName() ) ) {
                    list.add( n );
                }
            }
        }
         
        if ( isMand && list.size() == 0 ) {
            throw new XMLMissingException( "Missing mandatory child nodes with element name " + tagName, node );
        }
        
        return list;
    }

    public String[] getChildElementValues( Node node, String tagName, boolean isMand ) throws XMLMissingException {
        
        List<String> list = new ArrayList<String>();

        if ( node != null ) {
            NodeList children = node.getChildNodes();
            
            for ( int i=0 ; i < children.getLength() ; ++i ) {
                Node n = children.item( i );
                
                if ( tagName.equals( n.getNodeName() ) ) {
                    String v = getElementTextValue( n );
                    
                    if ( v != null ) {
                        list.add( n.getNodeValue() );
                    }
                }
            }
        }
         
        if ( isMand && list.size() == 0 ) {
            throw new XMLMissingException( "Missing mandatory child nodes with element name " + tagName, node );
        }
        
        return list.toArray( new String[ list.size() ] );
    }

    /**
     * if present there should be only one child node with the designated tag
     * 
     * @param node
     * @param tagName
     * @param isMand
     * @return
     * @throws XMLMissingException 
     * @throws XMLDuplicateNodeException 
     */
    public Element getChildElement( Node node, String tagName, boolean isMand ) throws XMLMissingException, XMLDuplicateNodeException {
        NodeList children = node.getChildNodes();
        Element found = null;
        
        int cnt=0;
        
        for ( int i=0 ; i < children.getLength() ; ++i ) {
            Node n = children.item( i );
            
            if ( n.getNodeType() == Node.ELEMENT_NODE && tagName.equals( n.getNodeName() ) ) {
                found = (Element) n;
                ++cnt;
            }
        }
     
        if ( isMand && cnt == 0 ) {
            throw new XMLMissingException( "Missing mandatory child nodes with element name " + tagName, node );
        }
        
        if ( cnt > 1 ) {
            throw new XMLDuplicateNodeException( "Duplicate child nodes with element name " + tagName, node );
        }
        
        return found;
    }

    public boolean getAttrBool( Node entry, String attrName, boolean isMand, boolean defaultVal ) throws XMLException {

        boolean val = defaultVal;
        
        String sVal = getAttr( entry, attrName, isMand );
        
        if ( sVal != null && sVal.length() > 0 ){
            
            sVal = sVal.trim();
            
            char first = Character.toUpperCase( sVal.charAt( 0 ) );
            
            if ( first == 'Y' ) return true;
            if ( first == 'N' ) return false;
            
            try {
                val = Boolean.parseBoolean( sVal );
            } catch( Exception e ) {
                throw new XMLException( "Unable to parse boolean val=" + sVal + ", attr=" + attrName, entry );
            }
        }
        
        return val;
    }

    public int getAttrInt( Node node, String attr, boolean isMand, int defaultVal ) throws XMLException {
        int val = defaultVal;
        
        String sVal = getAttr( node, attr, isMand );
        
        if ( sVal != null ) {
            try {
                val = Integer.parseInt( sVal );
            } catch( NumberFormatException e ) {
                throw new XMLException( "Attr " + attr + " is not numeric, val=" + sVal + ", node=" + node.getNodeName(), node );
            }
        }
        
        return val;
    }
}
