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
import com.rr.core.codec.binary.fastfix.common.FieldWriter;
import com.rr.core.codec.binary.fastfix.common.constant.decimal.DecimalMandWriterConst;
import com.rr.core.codec.binary.fastfix.common.constant.decimal.DecimalOptWriterConst;
import com.rr.core.codec.binary.fastfix.common.constant.int32.IntMandWriterConst;
import com.rr.core.codec.binary.fastfix.common.constant.int32.IntOptWriterConst;
import com.rr.core.codec.binary.fastfix.common.constant.int64.LongMandWriterConst;
import com.rr.core.codec.binary.fastfix.common.constant.int64.LongOptWriterConst;
import com.rr.core.codec.binary.fastfix.common.constant.string.StringMandWriterConst;
import com.rr.core.codec.binary.fastfix.common.constant.string.StringOptWriterConst;
import com.rr.core.codec.binary.fastfix.common.def.decimal.DecimalMandWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.decimal.DecimalOptWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.int32.IntMandWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.int32.IntOptWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.int32.UIntMandWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.int32.UIntOptWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.int64.LongMandWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.int64.LongOptWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.int64.ULongMandWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.int64.ULongOptWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.string.StringWriterDefault;
import com.rr.core.codec.binary.fastfix.common.noop.decimal.DecimalMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.decimal.DecimalOptWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.IntMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.IntOptWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.UIntMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.UIntOptWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.LongMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.LongOptWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.ULongMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.ULongOptWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.string.StringWriterNoOp;
import com.rr.core.codec.binary.fastfix.msgdict.copy.decimal.DecimalMandWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.decimal.DecimalOptWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.IntMandWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.IntOptWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntMandWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntOptWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int64.LongMandWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int64.LongOptWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int64.ULongMandWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int64.ULongOptWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.string.StringWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.custom.copyexp.delmant.MandCopyExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.copyexp.delmant.OptCopyExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.delmant.MandDefExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.delmant.OptDefExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.noopmant.MandDefExpNoOpMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.noopmant.OptDefExpNoOpMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.delexp.delmant.MandDelExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.delexp.delmant.OptDelExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.noopexp.delmant.MandNoOpExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.noopexp.delmant.OptNoOpExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.noopexp.noopmant.MandNoOpExpNoOpMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.noopexp.noopmant.OptNoOpExpNoOpMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.delta.decimal.DecimalMandWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.decimal.DecimalOptWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int32.IntMandWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int32.IntOptWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int32.UIntMandWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int32.UIntOptWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int64.LongMandWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int64.LongOptWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int64.ULongMandWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int64.ULongOptWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.string.StringWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.IntMandWriterIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.IntOptWriterIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.UIntMandWriterIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.UIntOptWriterIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.LongMandWriterIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.LongOptWriterIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.ULongMandWriterIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.ULongOptWriterIncrement;
import com.rr.core.utils.SMTRuntimeException;

public class WriterFieldClassLookup {

    @SuppressWarnings( "unchecked" )
    public static <T extends FieldWriter> Class<T> getCustomWriterClass( FieldOperator expOp, FieldOperator mantOp, boolean isOptional ) {
        if ( expOp == FieldOperator.COPY && mantOp == FieldOperator.DELTA ) {
            if ( isOptional ) {
                return (Class<T>) OptCopyExpDeltaMantDecimalWriter.class;
            } 
            return (Class<T>) MandCopyExpDeltaMantDecimalWriter.class;
        }

        if ( expOp == FieldOperator.DELTA && mantOp == FieldOperator.DELTA ) {
            if ( isOptional ) {
                return (Class<T>) OptDelExpDeltaMantDecimalWriter.class;
            } 
            return (Class<T>) MandDelExpDeltaMantDecimalWriter.class;
        }

        if ( expOp == FieldOperator.DEFAULT && mantOp == FieldOperator.DELTA ) {
            if ( isOptional ) {
                return (Class<T>) OptDefExpDeltaMantDecimalWriter.class;
            } 
            return (Class<T>) MandDefExpDeltaMantDecimalWriter.class;
        }

        if ( expOp == FieldOperator.NOOP && mantOp == FieldOperator.DELTA ) {
            if ( isOptional ) {
                return (Class<T>) OptNoOpExpDeltaMantDecimalWriter.class;
            } 
            return (Class<T>) MandNoOpExpDeltaMantDecimalWriter.class;
        }

        if ( expOp == FieldOperator.DEFAULT && mantOp == FieldOperator.NOOP ) {
            if ( isOptional ) {
                return (Class<T>) OptDefExpNoOpMantDecimalWriter.class;
            } 
            return (Class<T>) MandDefExpNoOpMantDecimalWriter.class;
        }

        if ( expOp == FieldOperator.NOOP && mantOp == FieldOperator.NOOP ) {
            if ( isOptional ) {
                return (Class<T>) OptNoOpExpNoOpMantDecimalWriter.class;
            } 
            return (Class<T>) MandNoOpExpNoOpMantDecimalWriter.class;
        }

        throw new SMTRuntimeException( "Unable to get custom decimal Writer for expOp=" + expOp + ", mantOp=" + mantOp + ", opt=" + isOptional );
    }
    
    public static <T extends FieldWriter> Class<T> getWriterClass( FieldOperator op, FieldDataType type, boolean isOptional ) {
        switch( op ) {
        case CONSTANT:
            if ( isOptional ) {
                return getConstOptionalWriter( type );
            } 
            return getConstMandWriter( type );
        case COPY:
            if ( isOptional ) {
                return getCopyOptionalWriter( type );
            } 
            return getCopyMandWriter( type );
        case DEFAULT:
            if ( isOptional ) {
                return getDefaultOptionalWriter( type );
            } 
            return getDefaultMandWriter( type );
        case DELTA:
            if ( isOptional ) {
                return getDeltaOptionalWriter( type );
            } 
            return getDeltaMandWriter( type );
        case INCREMENT:
            if ( isOptional ) {
                return getIncrementOptionalWriter( type );
            } 
            return getIncrementMandWriter( type );
        case NOOP:
            if ( isOptional ) {
                return getNoOpOptionalWriter( type );
            } 
            return getNoOpMandWriter( type );
        default:
            break;
        }
        
        throw new SMTRuntimeException( "Unable to get writer for op=" + op + ", type=" + type + ", opt=" + isOptional );
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getConstMandWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandWriterConst.class;
        case int64:
            return (Class<T>) LongMandWriterConst.class;
        case decimal:
            return (Class<T>) DecimalMandWriterConst.class;
        case length:
            return (Class<T>) IntMandWriterConst.class;
        case string:
            return (Class<T>) StringMandWriterConst.class;
        case uInt32:
        case uInt64:
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getConstOptionalWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptWriterConst.class;
        case int64:
            return (Class<T>) LongOptWriterConst.class;
        case decimal:
            return (Class<T>) DecimalOptWriterConst.class;
        case length:
            return (Class<T>) IntOptWriterConst.class;
        case string:
            return (Class<T>) StringOptWriterConst.class;
        case uInt32:
        case uInt64:
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getCopyMandWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandWriterCopy.class;
        case int64:
            return (Class<T>) LongMandWriterCopy.class;
        case decimal:
            return (Class<T>) DecimalMandWriterCopy.class;
        case length:
            return (Class<T>) IntMandWriterCopy.class;
        case string:
            return (Class<T>) StringWriterCopy.class;
        case uInt32:
            return (Class<T>) UIntMandWriterCopy.class;
        case uInt64:
            return (Class<T>) ULongMandWriterCopy.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getCopyOptionalWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptWriterCopy.class;
        case int64:
            return (Class<T>) LongOptWriterCopy.class;
        case decimal:
            return (Class<T>) DecimalOptWriterCopy.class;
        case length:
            return (Class<T>) IntOptWriterCopy.class;
        case string:
            return (Class<T>) StringWriterCopy.class;
        case uInt32:
            return (Class<T>) UIntOptWriterCopy.class;
        case uInt64:
            return (Class<T>) ULongOptWriterCopy.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getDefaultMandWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandWriterDefault.class;
        case int64:
            return (Class<T>) LongMandWriterDefault.class;
        case decimal:
            return (Class<T>) DecimalMandWriterDefault.class;
        case length:
            return (Class<T>) IntMandWriterDefault.class;
        case string:
            return (Class<T>) StringWriterDefault.class;
        case uInt32:
            return (Class<T>) UIntMandWriterDefault.class;
        case uInt64:
            return (Class<T>) ULongMandWriterDefault.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getDefaultOptionalWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptWriterDefault.class;
        case int64:
            return (Class<T>) LongOptWriterDefault.class;
        case decimal:
            return (Class<T>) DecimalOptWriterDefault.class;
        case length:
            return (Class<T>) IntOptWriterDefault.class;
        case string:
            return (Class<T>) StringWriterDefault.class;
        case uInt32:
            return (Class<T>) UIntOptWriterDefault.class;
        case uInt64:
            return (Class<T>) ULongOptWriterDefault.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getDeltaMandWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandWriterDelta.class;
        case int64:
            return (Class<T>) LongMandWriterDelta.class;
        case decimal:
            return (Class<T>) DecimalMandWriterDelta.class;
        case length:
            return (Class<T>) IntMandWriterDelta.class;
        case string:
            return (Class<T>) StringWriterDelta.class;
        case uInt32:
            return (Class<T>) UIntMandWriterDelta.class;
        case uInt64:
            return (Class<T>) ULongMandWriterDelta.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getDeltaOptionalWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptWriterDelta.class;
        case int64:
            return (Class<T>) LongOptWriterDelta.class;
        case decimal:
            return (Class<T>) DecimalOptWriterDelta.class;
        case length:
            return (Class<T>) IntOptWriterDelta.class;
        case string:
            return (Class<T>) StringWriterDelta.class;
        case uInt32:
            return (Class<T>) UIntOptWriterDelta.class;
        case uInt64:
            return (Class<T>) ULongOptWriterDelta.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getIncrementMandWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandWriterIncrement.class;
        case int64:
            return (Class<T>) LongMandWriterIncrement.class;
        case length:
            return (Class<T>) IntMandWriterIncrement.class;
        case uInt32:
            return (Class<T>) UIntMandWriterIncrement.class;
        case uInt64:
            return (Class<T>) ULongMandWriterIncrement.class;
        case decimal:
        case string:
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getIncrementOptionalWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptWriterIncrement.class;
        case int64:
            return (Class<T>) LongOptWriterIncrement.class;
        case length:
            return (Class<T>) IntOptWriterIncrement.class;
        case uInt32:
            return (Class<T>) UIntOptWriterIncrement.class;
        case uInt64:
            return (Class<T>) ULongOptWriterIncrement.class;
        case decimal:
        case string:
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getNoOpMandWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntMandWriterNoOp.class;
        case int64:
            return (Class<T>) LongMandWriterNoOp.class;
        case decimal:
            return (Class<T>) DecimalMandWriterNoOp.class;
        case length:
            return (Class<T>) IntMandWriterNoOp.class;
        case string:
            return (Class<T>) StringWriterNoOp.class;
        case uInt32:
            return (Class<T>) UIntMandWriterNoOp.class;
        case uInt64:
            return (Class<T>) ULongMandWriterNoOp.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static <T extends FieldWriter> Class<T> getNoOpOptionalWriter( FieldDataType type ) {
        switch( type ) {
        case int32:
            return (Class<T>) IntOptWriterNoOp.class;
        case int64:
            return (Class<T>) LongOptWriterNoOp.class;
        case decimal:
            return (Class<T>) DecimalOptWriterNoOp.class;
        case length:
            return (Class<T>) IntOptWriterNoOp.class;
        case string:
            return (Class<T>) StringWriterNoOp.class;
        case uInt32:
            return (Class<T>) UIntOptWriterNoOp.class;
        case uInt64:
            return (Class<T>) ULongOptWriterNoOp.class;
        default:
            throw new SMTRuntimeException( "FieldFactory unsuppported type " + type );
        }
    }
}
