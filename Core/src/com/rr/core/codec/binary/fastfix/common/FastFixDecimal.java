/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.common;

import com.rr.core.lang.Constants;
import com.rr.core.utils.NumberFormatUtils;

public class FastFixDecimal {

    private long _mantissa = Constants.UNSET_LONG;
    private int  _exponent = Constants.UNSET_INT;
    
    public FastFixDecimal() {
        //
    }

    public FastFixDecimal( int exponent, long mantissa ) {
        
        _mantissa = mantissa;
        _exponent = exponent;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (_mantissa ^ (_mantissa >>> 32));
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
        FastFixDecimal other = (FastFixDecimal) obj;
        if ( _exponent != other._exponent )
            return false;
        if ( _mantissa != other._mantissa )
            return false;
        return true;
    }
    
    public void set( double val ) {
        
        if ( val == Constants.UNSET_DOUBLE ) {
            _mantissa = Constants.UNSET_LONG;
            _exponent = Constants.UNSET_INT;
            return;
        }
        
        _mantissa = (long) val; // get whole number part
        
        double fraction = val - _mantissa;
        
        if ( Math.abs( fraction ) < Constants.TICK_WEIGHT ) {
            _exponent = 0;
            normalise();
            return;
        }
        
        // fudge the fraction before existing weight bias before multiplication
        if ( fraction > 0 ) {
            fraction += Constants.WEIGHT;
        } else {
            fraction -= Constants.WEIGHT;
        }
        
        // max digits in long is 18 
        
        int curLen = NumberFormatUtils.getLongLen( _mantissa );
        
        int maxFraction = NumberFormatUtils.MAX_DOUBLE_DIGITS - curLen;

        if ( maxFraction > Constants.PRICE_DP ) {
            maxFraction = Constants.PRICE_DP;
        }
        
        long remainder;
        
        switch( maxFraction ) {
        case 20:
        case 19:
        case 18:
        case 17:
        case 16:
        case 15:
        case 14:
        case 13:
        case 12:
        case 11:
        case 10:
        case 9:
        case 8:
            _exponent = -8;
            _mantissa *= 100000000;
            remainder = (long) (fraction * 100000000);
            _mantissa += remainder;
            break;
        case 7:
            _exponent = -7;
            _mantissa *= 10000000;
            remainder = (long) (fraction * 10000000);
            _mantissa += remainder;
            break;
        case 6:
            _exponent = -6;
            _mantissa *= 1000000;
            remainder = (long) (fraction * 1000000);
            _mantissa += remainder;
            break;
        case 5:
            _exponent = -5;
            _mantissa *= 100000;
            remainder = (long) (fraction * 100000);
            _mantissa += remainder;
            break;
        case 4:
            _exponent = -4;
            _mantissa *= 10000;
            remainder = (long) (fraction * 10000);
            _mantissa += remainder;
            break;
        case 3:
            _exponent = -3;
            _mantissa *= 1000;
            remainder = (long) (fraction * 1000);
            _mantissa += remainder;
            break;
        case 2:
            _exponent = -2;
            _mantissa *= 100;
            remainder = (long) (fraction * 100);
            _mantissa += remainder;
            break;
        case 1:
            _exponent = -1;
            _mantissa *= 10;
            remainder = (long) (fraction * 10);
            _mantissa += remainder;
            break;
        }
        normalise();        
    }
    
    private void normalise() {
        if ( _mantissa > 10 ) {
            // @TODO optimise ... tho only used on encoding
            while( _mantissa % 10 == 0 ) {
                _mantissa /= 10;
                ++_exponent;
            }
        }
    }

    public double get() {
        if ( _mantissa == Constants.UNSET_LONG ) {
            return Constants.UNSET_DOUBLE;
        }

        double v = _mantissa;
        
        if ( _exponent > 0 ) {
            int pExp = _exponent;
            
            switch( pExp ) {
            case 1:  v *= 10           ; break;
            case 2:  v *= 100          ; break;
            case 3:  v *= 1000         ; break;
            case 4:  v *= 10000        ; break;
            case 5:  v *= 100000       ; break;
            case 6:  v *= 1000000      ; break;
            case 7:  v *= 10000000     ; break;
            case 8:  v *= 100000000    ; break;
            case 9:  v *= 1000000000   ; break;
            case 10: v *= 10000000000L ; break;
            default:
                while( pExp-- > 0 ) {
                    v *= 10;
                }
                break;
            }
        } else if ( _exponent < 0 ){
            int pExp = Math.abs( _exponent );
            
            switch( pExp ) {
            case 1:  v /= 10           ; break;
            case 2:  v /= 100          ; break;
            case 3:  v /= 1000         ; break;
            case 4:  v /= 10000        ; break;
            case 5:  v /= 100000       ; break;
            case 6:  v /= 1000000      ; break;
            case 7:  v /= 10000000     ; break;
            case 8:  v /= 100000000    ; break;
            case 9:  v /= 1000000000   ; break;
            case 10: v /= 10000000000L ; break;
            default:
                while( pExp-- > 0 ) {
                    v /= 10;
                }
                break;
            }
        }
        
        return v;
    }

    public void set( FastFixDecimal tmpValue ) {
        _mantissa = tmpValue._mantissa;
        _exponent = tmpValue._exponent;
    }

    public long getMantissa() {
        return _mantissa;
    }

    public int getExponent() {
        return _exponent;
    }

    public boolean isNull() {
        return _mantissa == Constants.UNSET_LONG;
    }

    public void setNull() {
        _mantissa = Constants.UNSET_LONG;
        _exponent = Constants.UNSET_INT;
    }

    public double set( int exponent, long mantissa ) {
        
        _mantissa = mantissa;
        _exponent = exponent;
        
        return get();
    }
    
    
}
