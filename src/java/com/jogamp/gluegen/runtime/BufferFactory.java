/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */
package com.jogamp.gluegen.runtime;

import java.nio.*;

/**
 * @author Kenneth Russel
 * @author Sven Gothel
 * @author Michael Bien
 */
public class BufferFactory {

    public static final int SIZEOF_BYTE     = 1;
    public static final int SIZEOF_SHORT    = 2;
    public static final int SIZEOF_CHAR     = 2;
    public static final int SIZEOF_INT      = 4;
    public static final int SIZEOF_FLOAT    = 4;
    public static final int SIZEOF_LONG     = 8;
    public static final int SIZEOF_DOUBLE   = 8;

    public static boolean isLittleEndian() {
        return Platform.isLittleEndian();
    }

    /**
     * Helper routine to create a direct ByteBuffer with native order
     */
    public static ByteBuffer newDirectByteBuffer(int size) {
        return nativeOrder(ByteBuffer.allocateDirect(size));
    }

    /**
     * Helper routine to set a ByteBuffer to the native byte order, if
     * that operation is supported by the underlying NIO
     * implementation.
     */
    public static ByteBuffer nativeOrder(ByteBuffer buf) {
        if (Platform.isJavaSE()) {
            return buf.order(ByteOrder.nativeOrder());
        } else {
            // JSR 239 does not support the ByteOrder class or the order methods.
            // The initial order of a byte buffer is the platform byte order.
            return buf;
        }
    }

    /**
     * Helper routine to tell whether a buffer is direct or not. Null
     * pointers are considered NOT direct. isDirect() should really be
     * public in Buffer and not replicated in all subclasses.
     */
    public static boolean isDirect(Object buf) {
        if (buf == null) {
            return true;
        }
        if (buf instanceof ByteBuffer) {
            return ((ByteBuffer) buf).isDirect();
        } else if (buf instanceof FloatBuffer) {
            return ((FloatBuffer) buf).isDirect();
        } else if (buf instanceof IntBuffer) {
            return ((IntBuffer) buf).isDirect();
        } else if (buf instanceof ShortBuffer) {
            return ((ShortBuffer) buf).isDirect();
        } else if (buf instanceof PointerBuffer) {
            return ((PointerBuffer) buf).isDirect();
        } else if (Platform.isJavaSE()) {
            if (buf instanceof DoubleBuffer) {
                return ((DoubleBuffer) buf).isDirect();
            } else if (buf instanceof LongBuffer) {
                return ((LongBuffer) buf).isDirect();
            }else if (buf instanceof CharBuffer) {
                return ((CharBuffer) buf).isDirect();
            }
        }
        throw new RuntimeException("Unexpected buffer type " + buf.getClass().getName());
    }

    /**
     * Helper routine to get the Buffer byte offset by taking into
     * account the Buffer position and the underlying type.  This is
     * the total offset for Direct Buffers.
     */
    public static int getDirectBufferByteOffset(Object buf) {
        if (buf == null) {
            return 0;
        }
        if (buf instanceof Buffer) {
            int pos = ((Buffer) buf).position();
            if (buf instanceof ByteBuffer) {
                return pos;
            } else if (buf instanceof FloatBuffer) {
                return pos * SIZEOF_FLOAT;
            } else if (buf instanceof IntBuffer) {
                return pos * SIZEOF_INT;
            } else if (buf instanceof ShortBuffer) {
                return pos * SIZEOF_SHORT;
            }else if(Platform.isJavaSE()) {
                if (buf instanceof DoubleBuffer) {
                    return pos * SIZEOF_DOUBLE;
                } else if (buf instanceof LongBuffer) {
                    return pos * SIZEOF_LONG;
                } else if (buf instanceof CharBuffer) {
                    return pos * SIZEOF_CHAR;
                }
            }
        } else if (buf instanceof PointerBuffer) {
            PointerBuffer pointerBuffer = (PointerBuffer) buf;
            return pointerBuffer.position() * PointerBuffer.elementSize();
        }

        throw new RuntimeException("Disallowed array backing store type in buffer "
                + buf.getClass().getName());
    }

    /**
     * Helper routine to return the array backing store reference from
     * a Buffer object.
     */
    public static Object getArray(Object buf) {
        if (buf == null) {
            return null;
        }
        if (buf instanceof ByteBuffer) {
            return ((ByteBuffer) buf).array();
        } else if (buf instanceof FloatBuffer) {
            return ((FloatBuffer) buf).array();
        } else if (buf instanceof IntBuffer) {
            return ((IntBuffer) buf).array();
        } else if (buf instanceof ShortBuffer) {
            return ((ShortBuffer) buf).array();
        } else if (buf instanceof PointerBuffer) {
            return ((PointerBuffer) buf).array();
        }else if(Platform.isJavaSE()) {
            if (buf instanceof DoubleBuffer) {
                return ((DoubleBuffer) buf).array();
            } else if (buf instanceof LongBuffer) {
                return ((LongBuffer) buf).array();
            } else if (buf instanceof CharBuffer) {
                return ((CharBuffer) buf).array();
            }
        }

        throw new RuntimeException("Disallowed array backing store type in buffer "
                + buf.getClass().getName());
    }

    /**
     * Helper routine to get the full byte offset from the beginning of
     * the array that is the storage for the indirect Buffer
     * object.  The array offset also includes the position offset
     * within the buffer, in addition to any array offset.
     */
    public static int getIndirectBufferByteOffset(Object buf) {
        if (buf == null) {
            return 0;
        }
        if (buf instanceof Buffer) {
            int pos = ((Buffer) buf).position();
            if (buf instanceof ByteBuffer) {
                return (((ByteBuffer) buf).arrayOffset() + pos);
            } else if (buf instanceof FloatBuffer) {
                return (SIZEOF_FLOAT * (((FloatBuffer) buf).arrayOffset() + pos));
            } else if (buf instanceof IntBuffer) {
                return (SIZEOF_INT * (((IntBuffer) buf).arrayOffset() + pos));
            } else if (buf instanceof ShortBuffer) {
                return (SIZEOF_SHORT * (((ShortBuffer) buf).arrayOffset() + pos));
            }else if(Platform.isJavaSE()) {
                if (buf instanceof DoubleBuffer) {
                    return (SIZEOF_DOUBLE * (((DoubleBuffer) buf).arrayOffset() + pos));
                } else if (buf instanceof LongBuffer) {
                    return (SIZEOF_LONG * (((LongBuffer) buf).arrayOffset() + pos));
                } else if (buf instanceof CharBuffer) {
                    return (SIZEOF_CHAR * (((CharBuffer) buf).arrayOffset() + pos));
                }
            }
        } else if (buf instanceof PointerBuffer) {
            PointerBuffer pointerBuffer = (PointerBuffer) buf;
            return PointerBuffer.elementSize() * (pointerBuffer.arrayOffset() + pointerBuffer.position());
        }

        throw new RuntimeException("Unknown buffer type " + buf.getClass().getName());
    }

    public static void rangeCheck(byte[] array, int offset, int minElementsRemaining) {
        if (array == null) {
            return;
        }

        if (array.length < offset + minElementsRemaining) {
            throw new ArrayIndexOutOfBoundsException("Required " + minElementsRemaining + " elements in array, only had " + (array.length - offset));
        }
    }

    public static void rangeCheck(char[] array, int offset, int minElementsRemaining) {
        if (array == null) {
            return;
        }

        if (array.length < offset + minElementsRemaining) {
            throw new ArrayIndexOutOfBoundsException("Required " + minElementsRemaining + " elements in array, only had " + (array.length - offset));
        }
    }

    public static void rangeCheck(short[] array, int offset, int minElementsRemaining) {
        if (array == null) {
            return;
        }

        if (array.length < offset + minElementsRemaining) {
            throw new ArrayIndexOutOfBoundsException("Required " + minElementsRemaining + " elements in array, only had " + (array.length - offset));
        }
    }

    public static void rangeCheck(int[] array, int offset, int minElementsRemaining) {
        if (array == null) {
            return;
        }

        if (array.length < offset + minElementsRemaining) {
            throw new ArrayIndexOutOfBoundsException("Required " + minElementsRemaining + " elements in array, only had " + (array.length - offset));
        }
    }

    public static void rangeCheck(long[] array, int offset, int minElementsRemaining) {
        if (array == null) {
            return;
        }

        if (array.length < offset + minElementsRemaining) {
            throw new ArrayIndexOutOfBoundsException("Required " + minElementsRemaining + " elements in array, only had " + (array.length - offset));
        }
    }

    public static void rangeCheck(float[] array, int offset, int minElementsRemaining) {
        if (array == null) {
            return;
        }

        if (array.length < offset + minElementsRemaining) {
            throw new ArrayIndexOutOfBoundsException("Required " + minElementsRemaining + " elements in array, only had " + (array.length - offset));
        }
    }

    public static void rangeCheck(double[] array, int offset, int minElementsRemaining) {
        if (array == null) {
            return;
        }

        if (array.length < offset + minElementsRemaining) {
            throw new ArrayIndexOutOfBoundsException("Required " + minElementsRemaining + " elements in array, only had " + (array.length - offset));
        }
    }

    public static void rangeCheck(Buffer buffer, int minElementsRemaining) {
        if (buffer == null) {
            return;
        }

        if (buffer.remaining() < minElementsRemaining) {
            throw new IndexOutOfBoundsException("Required " + minElementsRemaining + " remaining elements in buffer, only had " + buffer.remaining());
        }
    }

    public static void rangeCheckBytes(Object buffer, int minBytesRemaining) {
        if (buffer == null) {
            return;
        }

        int bytesRemaining = 0;
        if (buffer instanceof Buffer) {
            int elementsRemaining = ((Buffer) buffer).remaining();
            if (buffer instanceof ByteBuffer) {
                bytesRemaining = elementsRemaining;
            } else if (buffer instanceof FloatBuffer) {
                bytesRemaining = elementsRemaining * SIZEOF_FLOAT;
            } else if (buffer instanceof IntBuffer) {
                bytesRemaining = elementsRemaining * SIZEOF_INT;
            } else if (buffer instanceof ShortBuffer) {
                bytesRemaining = elementsRemaining * SIZEOF_SHORT;
            }else if(Platform.isJavaSE()) {
                if (buffer instanceof DoubleBuffer) {
                    bytesRemaining = elementsRemaining * SIZEOF_DOUBLE;
                } else if (buffer instanceof LongBuffer) {
                    bytesRemaining = elementsRemaining * SIZEOF_LONG;
                } else if (buffer instanceof CharBuffer) {
                    bytesRemaining = elementsRemaining * SIZEOF_CHAR;
                }
            }
        } else if (buffer instanceof PointerBuffer) {
            PointerBuffer pointerBuffer = (PointerBuffer) buffer;
            bytesRemaining = pointerBuffer.remaining() * PointerBuffer.elementSize();
        }
        if (bytesRemaining < minBytesRemaining) {
            throw new IndexOutOfBoundsException("Required " + minBytesRemaining + " remaining bytes in buffer, only had " + bytesRemaining);
        }
    }
}
