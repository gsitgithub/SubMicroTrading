/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.fastfix;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rr.core.codec.binary.fastfix.common.FieldDataType;
import com.rr.core.codec.binary.fastfix.common.FieldOperator;
import com.rr.core.lang.RTStartupException;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.md.fastfix.meta.DecimalMetaFieldEntry;
import com.rr.md.fastfix.meta.MetaBaseEntry;
import com.rr.md.fastfix.meta.MetaFieldEntry;
import com.rr.md.fastfix.meta.MetaGroupEntry;
import com.rr.md.fastfix.meta.MetaSequenceEntry;
import com.rr.md.fastfix.meta.MetaTemplate;
import com.rr.md.fastfix.meta.MetaTemplates;
import com.rr.md.fastfix.template.FastFixTemplateReader;
import com.rr.md.fastfix.template.TemplateClassRegister;
import com.rr.model.xml.XMLDuplicateNodeException;
import com.rr.model.xml.XMLException;
import com.rr.model.xml.XMLHelper;
import com.rr.model.xml.XMLMissingException;

public class XMLFastFixTemplateLoader {
    private static final Logger       _log = LoggerFactory.create( XMLFastFixTemplateLoader.class );

    private final  String _configFile;
    private XMLHelper _helper;

    private boolean _debug = false;

    public XMLFastFixTemplateLoader( String configFile ) {
        _configFile = configFile;
    }
    
    public void setDebug( boolean debug ) {
        _debug = debug;
    }
    
    public void load( TemplateClassRegister reg, MetaTemplates metaTemplates ) throws RTStartupException {
        _helper = new XMLHelper( _configFile );
        
        try {
            _helper.parse();

            Element templates = _helper.getRoot();
            
            loadTemplates( reg, templates, metaTemplates );

        } catch( Exception e ) {
            
            throw new RTStartupException( ", file=" + _configFile + " : " + e.getMessage(), e );
        }
    }


    private void loadTemplates( TemplateClassRegister reg, Element templates, MetaTemplates metaTemplates ) throws XMLException {
        List<Node> templateList = _helper.getChildElements( templates, "template", true );

        for( Node node : templateList ) {
            if ( node.getNodeType() == Node.ELEMENT_NODE ) {
                loadTemplate( reg, (Element)node, metaTemplates );
            }
        }
    }

    private void loadTemplate( TemplateClassRegister reg, Element node, MetaTemplates metaTemplates ) throws XMLException {
        
        //  <template name="MDIncRefresh_81" id="81" dictionary="81"

        String          name          = _helper.getAttr( node, "name", true );
        int             id            = _helper.getAttrInt( node, "id", true );
        String          dictionaryId  = _helper.getAttr( node, "dictionary", false, "DICT" + id );
        
        Class<? extends FastFixTemplateReader> reader = reg.findReader( name, id );
        
        if ( reader != null ) {
            return; // already have class for that template
        }

        if ( _debug ) {
            _log.info( "No Registered Template for name=" + name + ", id=" + id + "  GENERATING Template" );
        }
        
        MetaTemplate template = new MetaTemplate( name, id, dictionaryId );
        
        addNodesToTemplate( node, template );
        
        metaTemplates.add( template );
        
        if ( _debug ) _log.info( "Added template : " + template.toString() );
    }

    private void addNodesToTemplate( Element node, MetaTemplate template ) throws XMLMissingException, XMLException, XMLDuplicateNodeException {
        NodeList nodeList = node.getChildNodes();
        for( int i=0 ; i < nodeList.getLength() ; i++ ) {
            Node subNode = nodeList.item( i );
            if ( subNode.getNodeType() == Node.ELEMENT_NODE ) {
                Element e = (Element)subNode;
                String          subName          = _helper.getAttr( e, "name", false );
                int             subId            = _helper.getAttrInt( e, "id", false );
                String          presence         = _helper.getAttr( e, "presence", false );
                
                boolean optional = "optional".equals( presence );

                addNode( e, subName, subId, optional, template );
            }
        }
    }

    private void addNode( Element e, String name, int id, boolean optional, MetaTemplate template ) throws XMLException {
        FieldDataType t = getNodeType( e.getNodeName() );

        MetaBaseEntry fe = null;
        
        switch ( t ) {
        case int32:
        case int64:
        case string:
        case uInt32:
        case uInt64:
            fe = new MetaFieldEntry( name, id, optional, t );
            setOperator( e, (MetaFieldEntry) fe );
            break;
        case decimal:
            fe = getDecimal( name, id, optional, e );
            break;
        case group:
            fe = getGroup( name, id, optional, e );
            break;
        case sequence:
            fe = getSequence( name, id, optional, e );
            break;
        case length:
            if ( template instanceof MetaSequenceEntry ) {
                MetaSequenceEntry se = (MetaSequenceEntry)template;
                
                se.setLengthField( new MetaFieldEntry( name, id, (optional | se.isOptional()), t ) );
                setOperator( e, se.getLengthField() );
                
                return;
            }
            break;
        default:
            break;
        }
        
        if ( fe != null ) {
            template.addEntry( fe );
        }
    }

    private MetaBaseEntry getSequence( String name, int id, boolean optionalSeq, Element parent ) throws XMLException {
        MetaSequenceEntry se = new MetaSequenceEntry( name, id, optionalSeq );
        
        NodeList nodeList = parent.getChildNodes();
        for( int i=0 ; i < nodeList.getLength() ; i++ ) {
            Node subNode = nodeList.item( i );
            if ( subNode.getNodeType() == Node.ELEMENT_NODE ) {
                Element e = (Element)subNode;
                String          subName          = _helper.getAttr( e, "name", false );
                int             subId            = _helper.getAttrInt( e, "id", false );
                String          presence         = _helper.getAttr( e, "presence", false );
                
                boolean optional = "optional".equals( presence );

                addNode( e, subName, subId, optional, se );
            }
        }
        
        return se;
    }

    private MetaBaseEntry getGroup( String name, int id, boolean optionalGroup, Element parent ) throws XMLException {
        MetaGroupEntry se = new MetaGroupEntry( name, id, optionalGroup );
        
        NodeList nodeList = parent.getChildNodes();
        for( int i=0 ; i < nodeList.getLength() ; i++ ) {
            Node subNode = nodeList.item( i );
            if ( subNode.getNodeType() == Node.ELEMENT_NODE ) {
                Element e = (Element)subNode;
                String          subName          = _helper.getAttr( e, "name", false );
                int             subId            = _helper.getAttrInt( e, "id", false );
                String          presence         = _helper.getAttr( e, "presence", false );
                
                boolean optional = "optional".equals( presence );

                addNode( e, subName, subId, optional, se );
            }
        }
        
        return se;
    }

    private MetaBaseEntry getDecimal( String name, int id, boolean optional, Element e ) throws XMLMissingException, XMLDuplicateNodeException {
        /**
         *      <exponent>
                    <default value="-2" />
                </exponent>
                <mantissa>
                    <delta />
                </mantissa>

         */
        Element exp  = _helper.getChildElement( e, "exponent", false );
        
        if ( exp != null ) {
            Element mant = _helper.getChildElement( e, "mantissa", true );
            
            DecimalMetaFieldEntry df = new DecimalMetaFieldEntry( name, id, optional );
    
            MetaFieldEntry expFld  = new MetaFieldEntry( name + "_exp",  id, optional, FieldDataType.int32 );
            MetaFieldEntry mantFld = new MetaFieldEntry( name + "_mant", id, optional, FieldDataType.int64 );
            
            setOperator( exp,  expFld );
            setOperator( mant, mantFld );
            
            df.setExp( expFld );
            df.setMant( mantFld );
            
            return df;
        } 
        
        DecimalMetaFieldEntry df = new DecimalMetaFieldEntry( name, id, optional );
        
        MetaFieldEntry expFld  = new MetaFieldEntry( name + "_exp",  id, optional, FieldDataType.int32 );
        MetaFieldEntry mantFld = new MetaFieldEntry( name + "_mant", id, optional, FieldDataType.int64 );
        
        setOperator( e,  expFld );
        setOperator( e, mantFld );
        
        df.setExp( expFld );
        df.setMant( mantFld );
        
        return df;
    }

    private void setOperator( Element node, MetaFieldEntry fe ) {
        NodeList nodeList = node.getChildNodes();
        for( int i=0 ; i < nodeList.getLength() ; i++ ) {
            Node subNode = nodeList.item( i );
            if ( subNode.getNodeType() == Node.ELEMENT_NODE ) {
                Element e = (Element)subNode;
                String type = e.getNodeName();
                
                try {
                    FieldOperator fo = FieldOperator.valueOf( type.toUpperCase() );
                    
                    fe.setOperator( fo );
                    
                    String initVal = _helper.getAttr( e, "value", false );
                    
                    fe.setInitValue( initVal );
                
                    return;
                    
                } catch( Exception e1 ) {
                    // dont care
                }
            }
        }
    }

    private FieldDataType getNodeType( String subName ) {
        FieldDataType f = null;
        
        try {
            f = Enum.valueOf( FieldDataType.class, subName );
        } catch( Exception e ) {
            // dont care
        }
        
        return f;
    }
}
