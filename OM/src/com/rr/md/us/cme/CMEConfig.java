/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rr.core.component.SMTComponent;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.SMTRuntimeException;

public final class CMEConfig implements SMTComponent {
    
    private static final Logger _log = LoggerFactory.create( CMEConfig.class );

    public static class Product {
        private final ZString    _productCode;    // eg : XS2 
        private final ZString    _groupCode;      // eg : YS
        private final SubChannel _subChannel;     // 1 .. 4
        
        public Product( ZString productCode, ZString groupCode, SubChannel subChannel ) {
            super();
            _productCode = productCode;
            _groupCode   = groupCode;
            _subChannel  = subChannel;
        }

        public ZString getProductCode() {
            return _productCode;
        }

        
        public ZString getGroupCode() {
            return _groupCode;
        }

        
        public SubChannel getSubChannel() {
            return _subChannel;
        }
    }
    
    public static class Products {

        private final Map<ZString,Product>  _products = new HashMap<ZString,Product>();
        
        public void add( Product product ) {
            _products.put( product.getProductCode(), product );
        }
        
        public Collection<Product> getProducts() {
            return _products.values();
        }
    }
    
    public static class Channel {
        private final Integer           _channel;
        private final String            _category;
        private final CMEConnections    _conns = new CMEConnections();
        private final Products          _prods = new Products();
        
        public Channel( Integer channel, String category ) {
            super();
            _channel = channel;
            _category = category;
        }

        @Override
        public String toString() {
            return "Channel [_channel=" + _channel + ", _category=" + _category + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_channel == null) ? 0 : _channel.hashCode());
            return result;
        }

        @Override
        public boolean equals( Object obj ) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            Channel other = (Channel) obj;
            if ( _channel == null ) {
                if ( other._channel != null )
                    return false;
            } else if ( !_channel.equals( other._channel ) )
                return false;
            return true;
        }

        public Integer getChannelId() {
            return _channel;
        }

        public String getCategory() {
            return _category;
        }
        
        public CMEConnections getConns() {
            return _conns;
        }
        
        public Products getProds() {
            return _prods;
        }

        public void addProduct( Product product ) {
            _prods.add( product );
        }

        public void addConnection( CMEConnection conn ) {
            _conns.add( conn );
        }

        public Iterator<CMEConnection> getConnectionIterator() {
            return _conns.getConnectionIterator();
        }
    }
    
    private final String  _id;
    private final ZString _env;
    private final Map<Integer, Channel> _channels  = new HashMap<Integer, Channel>();
    private final Map<ZString, Integer> _prodToCat = new HashMap<ZString, Integer>();
    
    public CMEConfig( String id, ZString env ) {
        super();
        _env = env;
        _id = id;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    public void add( Channel channel ) {
        
        Channel existingChannel = _channels.put( channel.getChannelId(), channel );
        
        if ( existingChannel != null ) {
            throw new SMTRuntimeException( "Attempt to add duplicate channel=" + channel + ", but already have channel:" + existingChannel );
        }
        
        for( Product p : channel.getProds().getProducts() ) {
            ReusableString key = new ReusableString();
            makeProdGroupKey( p.getProductCode(), p.getGroupCode(), key );
                    
            Integer existing = _prodToCat.get( key );

            if ( existing == null ) {
                _prodToCat.put( key, channel.getChannelId() );
                
            } else if ( existing != channel.getChannelId() ) {
                _log.warn( "Expected the product/group code combination to be used in one category, prodGroupCode=" + key +
                           " already used in channel:" + existing + ", dropped from channel:" + channel.getChannelId() );
            }
        }
    }

    private void makeProdGroupKey( ZString prodCode, ZString groupCode, ReusableString key ) {
        key.append( prodCode ).append( ":" ).append( groupCode );
    }

    public Integer get( ZString prodCode, ZString groupCode ) {
        ReusableString key = new ReusableString();
        makeProdGroupKey( prodCode, groupCode, key );
        
        return _prodToCat.get( prodCode );
    }

    public Channel get( Integer channelId ) {
        return _channels.get( channelId );
    }

    public ZString getEnv() {
        return _env;
    }

    public Iterator<Channel> getChannelIterator() {
        return _channels.values().iterator();
    }
}
