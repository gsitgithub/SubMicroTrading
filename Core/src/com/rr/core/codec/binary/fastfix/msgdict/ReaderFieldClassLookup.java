/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict;

import com.rr.core.codec.binary.fastfix.common.FieldDataType;
import com.rr.core.codec.binary.fastfix.common.FieldOperator;
import com.rr.core.codec.binary.fastfix.common.FieldReader;
import com.rr.core.codec.binary.fastfix.common.constant.decimal.DecimalMandReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.decimal.DecimalOptReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.int32.IntMandReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.int32.IntOptReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.int32.UIntMandReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.int32.UIntOptReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.int64.LongMandReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.int64.LongOptReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.string.StringMandReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.string.StringOptReaderConst;
import com.rr.core.codec.binary.fastfix.common.def.decimal.DecimalMandReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.decimal.DecimalOptReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.int32.IntMandReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.int32.IntOptReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.int32.UIntMandReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.int32.UIntOptReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.int64.LongMandReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.int64.LongOptReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.int64.ULongMandReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.int64.ULongOptReaderDefault;
import com.rr.core.codec.binary.fastfix.common.def.string.StringReaderDefault;
import com.rr.core.codec.binary.fastfix.common.noop.decimal.DecimalMandReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.decimal.DecimalOptReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.IntMandReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.IntOptReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.UIntMandReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.UIntOptReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.LongMandReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.LongOptReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.ULongMandReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.ULongOptReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.string.StringReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.vector.ByteVectorMandReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.vector.ByteVectorOptionalReaderNoOp;
import com.rr.core.codec.binary.fastfix.msgdict.copy.decimal.DecimalMandReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.decimal.DecimalOptReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.IntMandReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.IntOptReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntMandReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntOptReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int64.LongMandReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int64.LongOptReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int64.ULongMandReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int64.ULongOptReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.string.StringReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.custom.copyexp.delmant.MandCopyExpDeltaMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.copyexp.delmant.OptCopyExpDeltaMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.delmant.MandDefExpDeltaMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.delmant.OptDefExpDeltaMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.noopmant.MandDefExpNoOpMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.noopmant.OptDefExpNoOpMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.delexp.delmant.MandDelExpDeltaMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.delexp.delmant.OptDelExpDeltaMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.noopexp.delmant.MandNoOpExpDeltaMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.noopexp.delmant.OptNoOpExpDeltaMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.noopexp.noopmant.MandNoOpExpNoOpMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.custom.noopexp.noopmant.OptNoOpExpNoOpMantDecimalReader;
import com.rr.core.codec.binary.fastfix.msgdict.delta.decimal.DecimalMandReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.decimal.DecimalOptReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int32.IntMandReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int32.IntOptReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int32.UIntMandReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int32.UIntOptReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int64.LongMandReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int64.LongOptReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int64.ULongMandReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int64.ULongOptReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.string.StringReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.IntMandReaderIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.IntOptReaderIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.UIntMandReaderIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.UIntOptReaderIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.LongMandReaderIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.LongOptReaderIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.ULongMandReaderIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.ULongOptReaderIncrement;
import com.rr.core.utils.SMTRuntimeException;

public class ReaderFieldClassLookup {

    @SuppressWarnings( "unchecked" )
    public static <T extends FieldReader> Class<T> getCustomReaderClass( FieldOperator expOp, FieldOperator mantOp, boolean isOptional ) {
        if ( expOp == FieldOperator.COPY && mantOp == FieldOperator.DELTA ) {
            if ( isOptional ) {
                return (Class<T>) OptCopyExpDeltaMantDecimalReader.class;
            } 
            return (Class<T>) MandCopyExpDeltaMantDecimalReader.class;
        }

        if ( expOp == FieldOperator.DELTA && mantOp == FieldOperator.DELTA ) {
            if ( isOptional ) {
                return (Class<T>) OptDelExpDeltaMantDecimalReader.class;
            } 
            return (Class<T>) MandDelExpDeltaMantDecimalReader.class;
        }

        if ( expOp == FieldOperator.DEFAULT && mantOp == FieldOperator.DELTA ) {
            if ( isOptional ) {
                return (Class<T>) OptDefExpDeltaMantDecimalReader.class;
            } 
            return (Class<T>) MandDefExpDeltaMantDecimalReader.class;
        }

        if ( expOp == FieldOperator.NOOP && mantOp == FieldOperator.DELTA ) {
            if ( isOptional ) {
                return (Class<T>) OptNoOpExpDeltaMantDecimalReader.class;
            } 
            return (Class<T>) MandNoOpExpDeltaMantDecimalReader.class;
        }

        if ( expOp == FieldOperator.DEFAULT && mantOp == FieldOperator.NOOP ) {
            if ( isOptional ) {
                return (Class<T>) OptDefExpNoOpMantDecimalReader.class;
            } 
            return (Class<T>) MandDefExpNoOpMantDecimalReader.class;
        }

        if ( expOp == FieldOperator.NOOP && mantOp == FieldOperator.NOOP ) {
            if ( isOptional ) {
                return (Class<T>) OptNoOpExpNoOpMantDecimalReader.class;
            } 
            return (Class<T>) MandNoOpExpNoOpMantDecimalReader.class;
        }

        throw new SMTRuntimeException( "Unable to get custom decimal reader for expOp=" + expOp + ", mantOp=" + mantOp + ", opt=" + isOptional );
    }
    
    public static <T extends FieldReader> Class<T> getReaderClass( FieldOperator op, FieldDataType type, boolean isOptional ) {
        switch( op ) {
        case CONSTANT:
            if ( isOptional ) {
                return getConstOptionalReader( type );
            } 
            return getConstMandReader( type );
        case COPY:
            if ( isOptional ) {
                return getCopyOptionalReader( type );
            } 
            return getCopyMandReader( type );
        case DEFAULT:
            if ( isOptional ) {
                return getDefaultOptionalReader( type );
            } 
            return getDefaultMandReader( type );
        case DELTA:
            if ( isOptional ) {
                return getDeltaOptionalReader( type );
            } 
            return getDeltaMandReader( type );
        case INCREMENT:
            if ( isOptional ) {
                return getIncrementOptionalReader( type );
            } 
            return getIncrementMandReader( type );
        case NOOP:
            if ( isOptional ) {
                return getNoOpOptionalReader( type );
            } 
            return getNoOpMandReader( type );
        default:
            break;
        }
        
        throw new SMTRuntimeException( "Unable to get reader for op=" + op + ", type=" + type + ", opt=" + isOptional );
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getConstMandReader( FieldDataType type ) {
        switch( type ) {
        case int32:
        case uInt32:
            return (Class<T>) IntMandReaderConst.class;
        case int64:
        case uInt64:
            return (Class<T>) LongMandReaderConst.class;
        case decimal:
            return (Class<T>) DecimalMandReaderConst.class;
        case length:
            return (Class<T>) UIntMandReaderConst.class;
        case string:
            return (Class<T>) StringMandReaderConst.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getConstOptionalReader( FieldDataType type ) {
        switch( type ) {
        case int32:
        case uInt32:
            return (Class<T>) IntOptReaderConst.class;
        case int64:
        case uInt64:
            return (Class<T>) LongOptReaderConst.class;
        case decimal:
            return (Class<T>) DecimalOptReaderConst.class;
        case length:
            return (Class<T>) UIntOptReaderConst.class;
        case string:
            return (Class<T>) StringOptReaderConst.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getCopyMandReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandReaderCopy.class;
        case int64:
            return (Class<T>) LongMandReaderCopy.class;
        case decimal:
            return (Class<T>) DecimalMandReaderCopy.class;
        case length:
            return (Class<T>) UIntMandReaderCopy.class;
        case string:
            return (Class<T>) StringReaderCopy.class;
        case uInt32:
            return (Class<T>) UIntMandReaderCopy.class;
        case uInt64:
            return (Class<T>) ULongMandReaderCopy.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getCopyOptionalReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptReaderCopy.class;
        case int64:
            return (Class<T>) LongOptReaderCopy.class;
        case decimal:
            return (Class<T>) DecimalOptReaderCopy.class;
        case length:
            return (Class<T>) UIntOptReaderCopy.class;
        case string:
            return (Class<T>) StringReaderCopy.class;
        case uInt32:
            return (Class<T>) UIntOptReaderCopy.class;
        case uInt64:
            return (Class<T>) ULongOptReaderCopy.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getDefaultMandReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandReaderDefault.class;
        case int64:
            return (Class<T>) LongMandReaderDefault.class;
        case decimal:
            return (Class<T>) DecimalMandReaderDefault.class;
        case length:
            return (Class<T>) UIntMandReaderDefault.class;
        case string:
            return (Class<T>) StringReaderDefault.class;
        case uInt32:
            return (Class<T>) UIntMandReaderDefault.class;
        case uInt64:
            return (Class<T>) ULongMandReaderDefault.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getDefaultOptionalReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptReaderDefault.class;
        case int64:
            return (Class<T>) LongOptReaderDefault.class;
        case decimal:
            return (Class<T>) DecimalOptReaderDefault.class;
        case length:
            return (Class<T>) UIntOptReaderDefault.class;
        case string:
            return (Class<T>) StringReaderDefault.class;
        case uInt32:
            return (Class<T>) UIntOptReaderDefault.class;
        case uInt64:
            return (Class<T>) ULongOptReaderDefault.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getDeltaMandReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandReaderDelta.class;
        case int64:
            return (Class<T>) LongMandReaderDelta.class;
        case decimal:
            return (Class<T>) DecimalMandReaderDelta.class;
        case length:
            return (Class<T>) UIntMandReaderDelta.class;
        case string:
            return (Class<T>) StringReaderDelta.class;
        case uInt32:
            return (Class<T>) UIntMandReaderDelta.class;
        case uInt64:
            return (Class<T>) ULongMandReaderDelta.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getDeltaOptionalReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptReaderDelta.class;
        case int64:
            return (Class<T>) LongOptReaderDelta.class;
        case decimal:
            return (Class<T>) DecimalOptReaderDelta.class;
        case length:
            return (Class<T>) UIntOptReaderDelta.class;
        case string:
            return (Class<T>) StringReaderDelta.class;
        case uInt32:
            return (Class<T>) UIntOptReaderDelta.class;
        case uInt64:
            return (Class<T>) ULongOptReaderDelta.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getIncrementMandReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandReaderIncrement.class;
        case int64:
            return (Class<T>) LongMandReaderIncrement.class;
        case length:
            return (Class<T>) UIntMandReaderIncrement.class;
        case uInt32:
            return (Class<T>) UIntMandReaderIncrement.class;
        case uInt64:
            return (Class<T>) ULongMandReaderIncrement.class;
        case decimal:
        case string:
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getIncrementOptionalReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptReaderIncrement.class;
        case int64:
            return (Class<T>) LongOptReaderIncrement.class;
        case length:
            return (Class<T>) UIntOptReaderIncrement.class;
        case uInt32:
            return (Class<T>) UIntOptReaderIncrement.class;
        case uInt64:
            return (Class<T>) ULongOptReaderIncrement.class;
        case decimal:
        case string:
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getNoOpMandReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandReaderNoOp.class;
        case int64:
            return (Class<T>) LongMandReaderNoOp.class;
        case decimal:
            return (Class<T>) DecimalMandReaderNoOp.class;
        case length:
            return (Class<T>) UIntMandReaderNoOp.class;
        case string:
            return (Class<T>) StringReaderNoOp.class;
        case byteVector:
            return (Class<T>) ByteVectorMandReaderNoOp.class;
        case uInt32:
            return (Class<T>) UIntMandReaderNoOp.class;
        case uInt64:
            return (Class<T>) ULongMandReaderNoOp.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldReader> Class<T> getNoOpOptionalReader( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptReaderNoOp.class;
        case int64:
            return (Class<T>) LongOptReaderNoOp.class;
        case decimal:
            return (Class<T>) DecimalOptReaderNoOp.class;
        case length:
            return (Class<T>) UIntOptReaderNoOp.class;
        case string:
            return (Class<T>) StringReaderNoOp.class;
        case byteVector:
            return (Class<T>) ByteVectorOptionalReaderNoOp.class;
        case uInt32:
            return (Class<T>) UIntOptReaderNoOp.class;
        case uInt64:
            return (Class<T>) ULongOptReaderNoOp.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }
}
