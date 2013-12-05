package sas.core;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 基于{@link Octets}的可扩展字节流的类型<p>
 * 包括各种所需的序列化/反序列化
 * @formatter:off
 */
public class OctetsStream extends Octets
{
	protected int               pos;           // 当前的读写位置
	protected transient boolean exceptionInfo; // 是否需要详细的异常信息(默认不需要,可以加快unmarshal失败的性能)

	public static OctetsStream wrap(byte[] data, int size)
	{
		OctetsStream os = new OctetsStream();
		os.buffer = data;
		if(size > data.length)  os.count = data.length;
		else if(size < 0)       os.count = 0;
		else                    os.count = size;
		return os;
	}

	public static OctetsStream wrap(byte[] data)
	{
		OctetsStream os = new OctetsStream();
		os.buffer = data;
		os.count = data.length;
		return os;
	}

	public static OctetsStream wrap(Octets o)
	{
		OctetsStream os = new OctetsStream();
		os.buffer = o.buffer;
		os.count = o.count;
		return os;
	}

	public OctetsStream()
	{
	}

	public OctetsStream(int size)
	{
		super(size);
	}

	public OctetsStream(Octets o)
	{
		super(o);
	}

	public OctetsStream(byte[] data)
	{
		super(data);
	}

	public OctetsStream(byte[] data, int pos, int size)
	{
		super(data, pos, size);
	}

	public boolean eos()
	{
		return pos >= count;
	}

	public int position()
	{
		return pos;
	}

	public void setPosition(int pos)
	{
		this.pos = pos;
	}

	public int remain()
	{
		return count - pos;
	}

	public boolean hasExceptionInfo()
	{
		return exceptionInfo;
	}

	public void setExceptionInfo(boolean enable)
	{
		exceptionInfo = enable;
	}

	@Override
	public OctetsStream clone()
	{
		OctetsStream os = new OctetsStream(this);
		os.pos = pos;
		os.exceptionInfo = exceptionInfo;
		return os;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ pos;
	}

	@Override
	public int compareTo(Octets o)
	{
		if(!(o instanceof OctetsStream)) return 1;
		OctetsStream os = (OctetsStream)o;
		int c = super.compareTo(os);
		if(c != 0) return c;
		return pos - os.pos;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(!(o instanceof OctetsStream)) return false;
		OctetsStream os = (OctetsStream)o;
		return pos == os.pos && super.equals(os);
	}

	@Override
	public String toString()
	{
		return "[" + pos + '/' + count + ']';
	}

	@Override
	public StringBuilder dump(StringBuilder s)
	{
		if(s == null) s = new StringBuilder(count * 3 + 16);
		return super.dump(s.append('{')).append(':').append(pos).append('}');
	}

	public OctetsStream marshal1(byte x)
	{
		int count_new = count + 1;
		reserve(count_new);
		buffer[count] = x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal2(int x)
	{
		int count_new = count + 2;
		reserve(count_new);
		buffer[count    ] = (byte)(x >> 8);
		buffer[count + 1] = (byte)x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal3(int x)
	{
		int count_new = count + 3;
		reserve(count_new);
		buffer[count    ] = (byte)(x >> 16);
		buffer[count + 1] = (byte)(x >> 8);
		buffer[count + 2] = (byte)x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal4(int x)
	{
		int count_new = count + 4;
		reserve(count_new);
		buffer[count    ] = (byte)(x >> 24);
		buffer[count + 1] = (byte)(x >> 16);
		buffer[count + 2] = (byte)(x >> 8);
		buffer[count + 3] = (byte)x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal5(byte b, int x)
	{
		int count_new = count + 5;
		reserve(count_new);
		buffer[count    ] = b;
		buffer[count + 1] = (byte)(x >> 24);
		buffer[count + 2] = (byte)(x >> 16);
		buffer[count + 3] = (byte)(x >> 8);
		buffer[count + 4] = (byte)x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal5(long x)
	{
		int count_new = count + 5;
		reserve(count_new);
		buffer[count    ] = (byte)(x >> 32);
		buffer[count + 1] = (byte)(x >> 24);
		buffer[count + 2] = (byte)(x >> 16);
		buffer[count + 3] = (byte)(x >> 8);
		buffer[count + 4] = (byte)x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal6(long x)
	{
		int count_new = count + 6;
		reserve(count_new);
		buffer[count    ] = (byte)(x >> 40);
		buffer[count + 1] = (byte)(x >> 32);
		buffer[count + 2] = (byte)(x >> 24);
		buffer[count + 3] = (byte)(x >> 16);
		buffer[count + 4] = (byte)(x >> 8);
		buffer[count + 5] = (byte)x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal7(long x)
	{
		int count_new = count + 7;
		reserve(count_new);
		buffer[count    ] = (byte)(x >> 48);
		buffer[count + 1] = (byte)(x >> 40);
		buffer[count + 2] = (byte)(x >> 32);
		buffer[count + 3] = (byte)(x >> 24);
		buffer[count + 4] = (byte)(x >> 16);
		buffer[count + 5] = (byte)(x >> 8);
		buffer[count + 6] = (byte)x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal8(long x)
	{
		int count_new = count + 8;
		reserve(count_new);
		buffer[count    ] = (byte)(x >> 56);
		buffer[count + 1] = (byte)(x >> 48);
		buffer[count + 2] = (byte)(x >> 40);
		buffer[count + 3] = (byte)(x >> 32);
		buffer[count + 4] = (byte)(x >> 24);
		buffer[count + 5] = (byte)(x >> 16);
		buffer[count + 6] = (byte)(x >> 8);
		buffer[count + 7] = (byte)x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal9(byte b, long x)
	{
		int count_new = count + 9;
		reserve(count_new);
		buffer[count    ] = b;
		buffer[count + 1] = (byte)(x >> 56);
		buffer[count + 2] = (byte)(x >> 48);
		buffer[count + 3] = (byte)(x >> 40);
		buffer[count + 4] = (byte)(x >> 32);
		buffer[count + 5] = (byte)(x >> 24);
		buffer[count + 6] = (byte)(x >> 16);
		buffer[count + 7] = (byte)(x >> 8);
		buffer[count + 8] = (byte)x;
		count = count_new;
		return this;
	}

	public OctetsStream marshal(boolean b)
	{
		int count_new = count + 1;
		reserve(count_new);
		buffer[count] = (byte)(b ? 1 : 0);
		count = count_new;
		return this;
	}

	public OctetsStream marshal(byte x)
	{
		return marshal((int)x);
	}

	public OctetsStream marshal(short x)
	{
		return marshal((int)x);
	}

	public OctetsStream marshal(int x)
	{
		if(x >= 0)
		{
		    if(x < 0x40)        return marshal1((byte)x);           // 00xx xxxx
		    if(x < 0x2000)      return marshal2(x + 0x4000);        // 010x xxxx +1B
		    if(x < 0x100000)    return marshal3(x + 0x600000);      // 0110 xxxx +2B
		    if(x < 0x8000000)   return marshal4(x + 0x70000000);    // 0111 0xxx +3B
		                        return marshal5((byte)0x78, x);     // 0111 1000 +4B
		}
		if(x >= -0x40)          return marshal1((byte)x);           // 11xx xxxx
		if(x >= -0x2000)        return marshal2(x - 0x4000);        // 101x xxxx +1B
		if(x >= -0x100000)      return marshal3(x - 0x600000);      // 1001 xxxx +2B
		if(x >= -0x8000000)     return marshal4(x - 0x70000000);    // 1000 1xxx +3B
		                        return marshal5((byte)0x87, x);     // 1000 0111 +4B
	}

	public OctetsStream marshal(long x)
	{
		if(x < 0x8000000 && x >= -0x8000000) return marshal((int)x);
		if(x >= 0)
		{
		    if(x < 0x400000000L)        return marshal5(x + 0x7800000000L);         // 0111 10xx +4B
		    if(x < 0x20000000000L)      return marshal6(x + 0x7c0000000000L);       // 0111 110x +5B
		    if(x < 0x1000000000000L)    return marshal7(x + 0x7e000000000000L);     // 0111 1110 +6B
		    if(x < 0x80000000000000L)   return marshal8(x + 0x7f00000000000000L);   // 0111 1111 0+7B
		                    return marshal9((byte)0x7f, x + 0x8000000000000000L);   // 0111 1111 1+8B
		}
		if(x >= -0x400000000L)          return marshal5(x - 0x7800000000L);         // 1000 01xx +4B
		if(x >= -0x20000000000L)        return marshal6(x - 0x7c0000000000L);       // 1000 001x +5B
		if(x >= -0x1000000000000L)      return marshal7(x - 0x7e000000000000L);     // 1000 0001 +6B
		if(x >= -0x80000000000000L)     return marshal8(x - 0x7f00000000000000L);   // 1000 0000 1+7B
		                    return marshal9((byte)0x80, x - 0x8000000000000000L);   // 1000 0000 0+8B
	}

	public OctetsStream marshalUInt(int x)
	{
		if(x < 0x80)        return marshal1((byte)x);           // 0xxx xxxx
		if(x < 0x4000)      return marshal2(x + 0x8000);        // 10xx xxxx +1B
		if(x < 0x200000)    return marshal3(x + 0xc00000);      // 110x xxxx +2B
		if(x < 0x1000000)   return marshal4(x + 0xe0000000);    // 1110 xxxx +3B
		                    return marshal5((byte)0xf0, x);     // 1111 0000 +4B
	}

	public OctetsStream marshal(float x)
	{
		return marshal4(Float.floatToRawIntBits(x));
	}

	public OctetsStream marshal(double x)
	{
		return marshal8(Double.doubleToRawLongBits(x));
	}

	public OctetsStream marshal(byte[] bytes)
	{
		marshalUInt(bytes.length);
		append(bytes, 0, bytes.length);
		return this;
	}

	public OctetsStream marshal(Octets o)
	{
		marshalUInt(o.count);
		append(o.buffer, 0, o.count);
		return this;
	}

	public OctetsStream marshal(String str)
	{
		if(str.isEmpty())
		{
			marshal((byte)0);
			return this;
		}
		return marshal(str.getBytes(Const.stringCharset));
	}

	public OctetsStream marshal(String str, Charset charset)
	{
		if(str.isEmpty())
		{
			marshal((byte)0);
			return this;
		}
		return marshal(str.getBytes(charset));
	}

	public OctetsStream marshal(String str, String charset)
	{
		if(str.isEmpty())
		{
			marshal((byte)0);
			return this;
		}
		try
		{
			return marshal(str.getBytes(charset));
		}
		catch(UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

	public OctetsStream marshal(Bean<?> m)
	{
		return m.marshal(this);
	}

	public OctetsStream marshalProtocol(Bean<?> m)
	{
		return m.marshalProtocol(this);
	}

	public static int getKVType(Object o)
	{
		if(o instanceof Number)
		{
			if(o instanceof Float) return 4;
			if(o instanceof Double) return 5;
			return 0;
		}
		if(o instanceof Boolean || o instanceof Character) return 0;
		if(o instanceof Bean) return 2;
		return 1;
	}

	public OctetsStream marshalVar(int id, Object o)
	{
		if(id < 1 || id > 62) throw new IllegalArgumentException("id must be in [1,62]: " + id);
		if(o instanceof Number)
		{
			if(o instanceof Float)
			{
				float v = (Float)o;
				if(v != 0) marshal2((0xc0 + id) << 8).marshal(v);
			}
			else if(o instanceof Double)
			{
				double v = (Double)o;
				if(v != 0) marshal2(((0xc0 + id) << 8) + 1).marshal(v);
			}
			else
			{
				long v = ((Number)o).longValue();
				if(v != 0) marshal1((byte)id).marshal(v);
			}
		}
		else if(o instanceof Boolean)
		{
			boolean v = (Boolean)o;
			if(v) marshal2((id << 8) + 1);
		}
		else if(o instanceof Character)
		{
			int v = (Character)o;
			if(v != 0) marshal1((byte)id).marshal(v);
		}
		else if(o instanceof Octets)
		{
			Octets oct = (Octets)o;
			if(!oct.empty()) marshal1((byte)(0x40 + id)).marshal(oct);
		}
		else if(o instanceof String)
		{
			String str = (String)o;
			if(!str.isEmpty()) marshal1((byte)(0x40 + id)).marshal(str);
		}
		else if(o instanceof Bean)
		{
			int n = count;
			((Bean<?>)o).marshal(marshal1((byte)(0x80 + id)));
			if(count - n < 3) resize(n);
		}
		else if(o instanceof Collection)
		{
			Collection<?> list = (Collection<?>)o;
			if(!list.isEmpty())
			{
				int vtype = getKVType(list.iterator().next());
				marshal2(((0xc0 + id) << 8) + 0x80 + vtype).marshalUInt(list.size());
				for(Object v : list)
					marshalKV(vtype, v);
			}
		}
		else if(o instanceof Map)
		{
			Map<?, ?> map = (Map<?, ?>)o;
			if(!map.isEmpty())
			{
				Entry<?, ?> et = map.entrySet().iterator().next();
				int ktype = getKVType(et.getKey());
				int vtype = getKVType(et.getValue());
				marshal2(((0xc0 + id) << 8) + 0xc0 + (ktype << 3) + vtype).marshalUInt(map.size());
				for(Entry<?, ?> e : map.entrySet())
					marshalKV(ktype, e.getKey()).marshalKV(vtype, e.getValue());
			}
		}
		return this;
	}

	public OctetsStream marshalKV(int kvtype, Object o)
	{
		switch(kvtype)
		{
		case 0:
			if(o instanceof Number) marshal(((Number)o).longValue());
			else if(o instanceof Boolean) marshal1((Boolean)o ? (byte)1 : (byte)0);
			else if(o instanceof Character) marshal(((Character)o).charValue());
			else marshal1((byte)0);
			break;
		case 1:
			if(o instanceof Octets) marshal((Octets)o);
			else marshal(o.toString());
			break;
		case 2:
			if(o instanceof Bean) marshal((Bean<?>)o);
			else marshal1((byte)0);
			break;
		case 4:
			marshal((o instanceof Float) ? (Float)o : 0.0f);
			break;
		case 5:
			marshal((o instanceof Double) ? (Double)o : 0.0);
			break;
		default:
			throw new IllegalArgumentException("kvtype must be in {0,1,2,4,5}: " + kvtype);
		}
		return this;
	}

	public boolean unmarshalBoolean() throws MarshalException
	{
		if(pos >= count) throw MarshalException.createEOF(exceptionInfo);
		return buffer[pos++] != 0;
	}

	public byte unmarshalByte() throws MarshalException
	{
		if(pos >= count) throw MarshalException.createEOF(exceptionInfo);
		return buffer[pos++];
	}

	public int unmarshalShort() throws MarshalException
	{
		int pos_new = pos + 2;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		byte b0 = buffer[pos    ];
		byte b1 = buffer[pos + 1];
		pos = pos_new;
		return (b0 << 8) + (b1 & 0xff);
	}

	public char unmarshalChar() throws MarshalException
	{
		int pos_new = pos + 2;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		byte b0 = buffer[pos    ];
		byte b1 = buffer[pos + 1];
		pos = pos_new;
		return (char)((b0 << 8) + (b1 & 0xff));
	}

	public int unmarshalInt3() throws MarshalException
	{
		int pos_new = pos + 3;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		byte b0 = buffer[pos    ];
		byte b1 = buffer[pos + 1];
		byte b2 = buffer[pos + 2];
		pos = pos_new;
		return  ((b0 & 0xff) << 16) +
		        ((b1 & 0xff) <<  8) +
		         (b2 & 0xff);
	}

	public int unmarshalInt4() throws MarshalException
	{
		int pos_new = pos + 4;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		byte b0 = buffer[pos    ];
		byte b1 = buffer[pos + 1];
		byte b2 = buffer[pos + 2];
		byte b3 = buffer[pos + 3];
		pos = pos_new;
		return  ( b0         << 24) +
		        ((b1 & 0xff) << 16) +
		        ((b2 & 0xff) <<  8) +
		         (b3 & 0xff);
	}

	public long unmarshalLong5() throws MarshalException
	{
		int pos_new = pos + 5;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		byte b0 = buffer[pos    ];
		byte b1 = buffer[pos + 1];
		byte b2 = buffer[pos + 2];
		byte b3 = buffer[pos + 3];
		byte b4 = buffer[pos + 4];
		pos = pos_new;
		return  ((b0 & 0xffL) << 32) +
		        ((b1 & 0xffL) << 24) +
		        ((b2 & 0xff ) << 16) +
		        ((b3 & 0xff ) <<  8) +
		         (b4 & 0xff );
	}

	public long unmarshalLong6() throws MarshalException
	{
		int pos_new = pos + 6;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		byte b0 = buffer[pos    ];
		byte b1 = buffer[pos + 1];
		byte b2 = buffer[pos + 2];
		byte b3 = buffer[pos + 3];
		byte b4 = buffer[pos + 4];
		byte b5 = buffer[pos + 5];
		pos = pos_new;
		return  ((b0 & 0xffL) << 40) +
		        ((b1 & 0xffL) << 32) +
		        ((b2 & 0xffL) << 24) +
		        ((b3 & 0xff ) << 16) +
		        ((b4 & 0xff ) <<  8) +
		         (b5 & 0xff );
	}

	public long unmarshalLong7() throws MarshalException
	{
		int pos_new = pos + 7;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		byte b0 = buffer[pos    ];
		byte b1 = buffer[pos + 1];
		byte b2 = buffer[pos + 2];
		byte b3 = buffer[pos + 3];
		byte b4 = buffer[pos + 4];
		byte b5 = buffer[pos + 5];
		byte b6 = buffer[pos + 6];
		pos = pos_new;
		return  ((b0 & 0xffL) << 48) +
		        ((b1 & 0xffL) << 40) +
		        ((b2 & 0xffL) << 32) +
		        ((b3 & 0xffL) << 24) +
		        ((b4 & 0xff ) << 16) +
		        ((b5 & 0xff ) <<  8) +
		         (b6 & 0xff );
	}

	public long unmarshalLong8() throws MarshalException
	{
		int pos_new = pos + 8;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		byte b0 = buffer[pos    ];
		byte b1 = buffer[pos + 1];
		byte b2 = buffer[pos + 2];
		byte b3 = buffer[pos + 3];
		byte b4 = buffer[pos + 4];
		byte b5 = buffer[pos + 5];
		byte b6 = buffer[pos + 6];
		byte b7 = buffer[pos + 7];
		pos = pos_new;
		return  ((long)b0     << 56) +
		        ((b1 & 0xffL) << 48) +
		        ((b2 & 0xffL) << 40) +
		        ((b3 & 0xffL) << 32) +
		        ((b4 & 0xffL) << 24) +
		        ((b5 & 0xff ) << 16) +
		        ((b6 & 0xff ) <<  8) +
		         (b7 & 0xff );
	}

	public float unmarshalFloat() throws MarshalException
	{
		return Float.intBitsToFloat(unmarshalInt4());
	}

	public double unmarshalDouble() throws MarshalException
	{
		return Double.longBitsToDouble(unmarshalLong8());
	}

	public OctetsStream unmarshalSkip(int n) throws MarshalException
	{
		if(n < 0) throw MarshalException.create(exceptionInfo);
		int pos_new = pos + n;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		pos = pos_new;
		return this;
	}

	public void unmarshalSkipOctets() throws MarshalException
	{
		unmarshalSkip(unmarshalUInt());
	}

	public void unmarshalSkipBean() throws MarshalException
	{
		for(;;)
		{
			int tag = unmarshalByte();
			if(tag == 0) return;
			unmarshalSkipVar((tag >> 6) & 3);
		}
	}

	public void unmarshalSkipVar(int type) throws MarshalException
	{
		if(type == 0) // int/long: [1~9]
			unmarshalSkipInt();
		else if(type == 3) // float/double/collection/map: ...
			unmarshalSkipVarSub(unmarshalByte());
		else if(type == 2) // bean: ... 00
			unmarshalSkipBean();
		else if(type == 1) // octets: n [n]
			unmarshalSkipOctets();
		else
			throw MarshalException.create(exceptionInfo);
	}

	public Object unmarshalVar(int type) throws MarshalException
	{
		switch(type)
		{
			case 0: return unmarshalLong();
			case 1: return unmarshalOctets();
			case 2: { DynBean db = new DynBean(); db.unmarshal(this); return db; }
			case 3: return unmarshalVarSub(unmarshalByte());
			default: throw MarshalException.create(exceptionInfo);
		}
	}

	public void unmarshalSkipVarSub(int subtype) throws MarshalException
	{
		int t = subtype & 0xc0; // [ttkkkvvv] [4] / [8] / <n>[kv*n]
		if(t == 0x80) // collection: <n>[v*n]
		{
			subtype &= 7;
			for(int n = unmarshalUInt(); n > 0; --n)
				unmarshalSkipKV(subtype);
		}
		else if(t == 0xc0) // map: <n>[kv*n]
		{
			int keytype = (subtype >> 3) & 7;
			subtype &= 7;
			for(int n = unmarshalUInt(); n > 0; --n)
			{
				unmarshalSkipKV(keytype);
				unmarshalSkipKV(subtype);
			}
		}
		else if(subtype == 0) // float: [4]
			unmarshalSkip(4);
		else if(subtype == 1) // double: [8]
			unmarshalSkip(8);
		else
			throw MarshalException.create(exceptionInfo);
	}

	public Object unmarshalVarSub(int subtype) throws MarshalException
	{
		if(subtype == 0) return unmarshalFloat();
		if(subtype == 1) return unmarshalDouble();
		int t = subtype & 0xc0;
		if(t == 0x80)
		{
			subtype &= 7;
			int n = unmarshalUInt();
			Collection<Object> list = new ArrayList<>(n < 0x10000 ? n : 0x10000);
			for(; n > 0; --n)
				list.add(unmarshalKV(subtype));
			return list;
		}
		if(t == 0xc0)
		{
			int keytype = (subtype >> 3) & 7;
			subtype &= 7;
			int n = unmarshalUInt();
			Map<Object, Object> map = new HashMap<>(n < 0x10000 ? n : 0x10000);
			for(; n > 0; --n)
				map.put(unmarshalKV(keytype), unmarshalKV(subtype));
			return map;
		}
		throw MarshalException.create(exceptionInfo);
	}

	public void unmarshalSkipKV(int kvtype) throws MarshalException
	{
		if(kvtype == 0) // int/long: [1~9]
			unmarshalSkipInt();
		else if(kvtype == 2) // bean: ... 00
			unmarshalSkipBean();
		else if(kvtype == 1) // octets: n [n]
			unmarshalSkipOctets();
		else if(kvtype == 4) // float: [4]
			unmarshalSkip(4);
		else if(kvtype == 5) // double: [8]
			unmarshalSkip(8);
		else
			throw MarshalException.create(exceptionInfo);
	}

	public Object unmarshalKV(int kvtype) throws MarshalException
	{
		switch(kvtype)
		{
			case 0: return unmarshalLong();
			case 1: return unmarshalOctets();
			case 2: { DynBean db = new DynBean(); db.unmarshal(this); return db; }
			case 4: return unmarshalFloat();
			case 5: return unmarshalDouble();
			default: throw MarshalException.create(exceptionInfo);
		}
	}

	public void unmarshalSkipInt() throws MarshalException
	{
		int b = unmarshalByte() & 0xff;
		switch(b >> 3)
		{
		case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
		case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f: return;
		case 0x08: case 0x09: case 0x0a: case 0x0b: case 0x14: case 0x15: case 0x16: case 0x17: unmarshalSkip(1); return;
		case 0x0c: case 0x0d: case 0x12: case 0x13: unmarshalSkip(2); return;
		case 0x0e: case 0x11: unmarshalSkip(3); return;
		case 0x0f:
			switch(b & 7)
			{
			case 0: case 1: case 2: case 3: unmarshalSkip(4); return;
			case 4: case 5:                 unmarshalSkip(5); return;
			case 6:                         unmarshalSkip(6); return;
			default: unmarshalSkip(6 - (unmarshalByte() >> 7)); return;
			}
		default: // 0x10
			switch(b & 7)
			{
			case 4: case 5: case 6: case 7: unmarshalSkip(4); return;
			case 2: case 3:                 unmarshalSkip(5); return;
			case 1:                         unmarshalSkip(6); return;
			default: unmarshalSkip(7 + (unmarshalByte() >> 7)); return;
			}
		}
	}

	public int unmarshalInt() throws MarshalException
	{
		int b = unmarshalByte();
		switch((b >> 3) & 0x1f)
		{
		case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
		case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f: return b;
		case 0x08: case 0x09: case 0x0a: case 0x0b: return ((b - 0x40) <<  8) + (unmarshalByte()  & 0xff);
		case 0x14: case 0x15: case 0x16: case 0x17: return ((b + 0x40) <<  8) + (unmarshalByte()  & 0xff);
		case 0x0c: case 0x0d:                       return ((b - 0x60) << 16) + (unmarshalShort() & 0xffff);
		case 0x12: case 0x13:                       return ((b + 0x60) << 16) + (unmarshalShort() & 0xffff);
		case 0x0e:                                  return ((b - 0x70) << 24) +  unmarshalInt3();
		case 0x11:                                  return ((b + 0x70) << 24) +  unmarshalInt3();
		case 0x0f:
			switch(b & 7)
			{
			case 0: case 1: case 2: case 3: return unmarshalInt4();
			case 4: case 5:                 return unmarshalSkip(1).unmarshalInt4();
			case 6:                         return unmarshalSkip(2).unmarshalInt4();
			default: return unmarshalSkip(2 - (unmarshalByte() >> 7)).unmarshalInt4();
			}
		default: // 0x10
			switch(b & 7)
			{
			case 4: case 5: case 6: case 7: return unmarshalInt4();
			case 2: case 3:                 return unmarshalSkip(1).unmarshalInt4();
			case 1:                         return unmarshalSkip(2).unmarshalInt4();
			default: return unmarshalSkip(3 + (unmarshalByte() >> 7)).unmarshalInt4();
			}
		}
	}

	public long unmarshalLong() throws MarshalException
	{
		int b = unmarshalByte();
		switch((b >> 3) & 0x1f)
		{
		case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
		case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f: return b;
		case 0x08: case 0x09: case 0x0a: case 0x0b: return ((b - 0x40) <<  8) + (unmarshalByte()  & 0xff);
		case 0x14: case 0x15: case 0x16: case 0x17: return ((b + 0x40) <<  8) + (unmarshalByte()  & 0xff);
		case 0x0c: case 0x0d:                       return ((b - 0x60) << 16) + (unmarshalShort() & 0xffff);
		case 0x12: case 0x13:                       return ((b + 0x60) << 16) + (unmarshalShort() & 0xffff);
		case 0x0e:                                  return ((b - 0x70) << 24) +  unmarshalInt3();
		case 0x11:                                  return ((b + 0x70) << 24) +  unmarshalInt3();
		case 0x0f:
			switch(b & 7)
			{
			case 0: case 1: case 2: case 3: return ((long)(b - 0x78) << 32) | unmarshalInt4();
			case 4: case 5:                 return ((long)(b - 0x7c) << 40) + unmarshalLong5();
			case 6:                         return unmarshalLong6();
			default: long r = unmarshalLong7(); return r < 0x80000000000000L ?
					r : ((r - 0x80000000000000L) << 8) + (unmarshalByte() & 0xff);
			}
		default: // 0x10
			switch(b & 7)
			{
			case 4: case 5: case 6: case 7: return ((long)(b + 0x78) << 32) | unmarshalInt4();
			case 2: case 3:                 return ((long)(b + 0x7c) << 40) + unmarshalLong5();
			case 1:                         return 0xff00000000000000L + unmarshalLong6();
			default: long r = unmarshalLong7(); return r >= 0x80000000000000L ?
					0xff00000000000000L + r : ((r + 0x80000000000000L) << 8) + (unmarshalByte() & 0xff);
			}
		}
	}

	public int unmarshalUInt() throws MarshalException
	{
		int b = unmarshalByte() & 0xff;
		switch(b >> 4)
		{
		case  0: case  1: case  2: case  3: case 4: case 5: case 6: case 7: return b;
		case  8: case  9: case 10: case 11: return ((b & 0x3f) <<  8) + (unmarshalByte() & 0xff);
		case 12: case 13:                   return ((b & 0x1f) << 16) + (unmarshalShort() & 0xffff);
		case 14:                            return ((b & 0x0f) << 24) +  unmarshalInt3();
		default: int r = unmarshalInt4(); if(r < 0) throw MarshalException.create(exceptionInfo); return r;
		}
	}

	public int unmarshalInt(int type) throws MarshalException
	{
		if(type == 0) return unmarshalInt();
		if(type == 3)
		{
			type = unmarshalByte();
			if(type == 0) return (int)unmarshalFloat();
			if(type == 1) return (int)unmarshalDouble();
			unmarshalSkipVarSub(type);
			return 0;
		}
		unmarshalSkipVar(type);
		return 0;
	}

	public long unmarshalLong(int type) throws MarshalException
	{
		if(type == 0) return unmarshalLong();
		if(type == 3)
		{
			type = unmarshalByte();
			if(type == 0) return (long)unmarshalFloat();
			if(type == 1) return (long)unmarshalDouble();
			unmarshalSkipVarSub(type);
			return 0;
		}
		unmarshalSkipVar(type);
		return 0;
	}

	public float unmarshalFloat(int type) throws MarshalException
	{
		if(type == 3)
		{
			type = unmarshalByte();
			if(type == 0) return unmarshalFloat();
			if(type == 1) return (float)unmarshalDouble();
			unmarshalSkipVarSub(type);
			return 0;
		}
		if(type == 0) return unmarshalLong();
		unmarshalSkipVar(type);
		return 0;
	}

	public double unmarshalDouble(int type) throws MarshalException
	{
		if(type == 3)
		{
			type = unmarshalByte();
			if(type == 1) return unmarshalDouble();
			if(type == 0) return unmarshalFloat();
			unmarshalSkipVarSub(type);
			return 0;
		}
		if(type == 0) return unmarshalLong();
		unmarshalSkipVar(type);
		return 0;
	}

	public int unmarshalIntKV(int type) throws MarshalException
	{
		if(type == 0) return unmarshalInt();
		if(type == 4) return (int)unmarshalFloat();
		if(type == 5) return (int)unmarshalDouble();
		unmarshalSkipKV(type);
		return 0;
	}

	public long unmarshalLongKV(int type) throws MarshalException
	{
		if(type == 0) return unmarshalLong();
		if(type == 4) return (long)unmarshalFloat();
		if(type == 5) return (long)unmarshalDouble();
		unmarshalSkipKV(type);
		return 0;
	}

	public float unmarshalFloatKV(int type) throws MarshalException
	{
		if(type == 4) return unmarshalFloat();
		if(type == 5) return (float)unmarshalDouble();
		unmarshalSkipKV(type);
		return 0;
	}

	public double unmarshalDoubleKV(int type) throws MarshalException
	{
		if(type == 5) return unmarshalDouble();
		if(type == 4) return unmarshalFloat();
		unmarshalSkipKV(type);
		return 0;
	}

	public byte[] unmarshalBytes() throws MarshalException
	{
		int size = unmarshalUInt();
		if(size <= 0) return EMPTY;
		int pos_new = pos + size;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		if(pos_new < pos) throw MarshalException.create(exceptionInfo);
		byte[] r = new byte[size];
		System.arraycopy(buffer, pos, r, 0, size);
		pos = pos_new;
		return r;
	}

	public Octets unmarshalOctets() throws MarshalException
	{
		return Octets.wrap(unmarshalBytes());
	}

	public Octets unmarshalOctetsKV(int type) throws MarshalException
	{
		if(type == 1) return unmarshalOctets();
		unmarshalSkipKV(type);
		return new Octets();
	}

	public OctetsStream unmarshal(Octets o) throws MarshalException
	{
		int size = unmarshalUInt();
		if(size <= 0)
		{
			o.clear();
			return this;
		}
		int pos_new = pos + size;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		if(pos_new < pos) throw MarshalException.create(exceptionInfo);
		o.replace(buffer, pos, size);
		pos = pos_new;
		return this;
	}

	public OctetsStream unmarshal(Octets o, int type) throws MarshalException
	{
		if(type == 1) return unmarshal(o);
		unmarshalSkipVar(type);
		return this;
	}

	public Octets unmarshalRaw(int size) throws MarshalException
	{
		int pos_new = pos + size;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		if(pos_new < pos) throw MarshalException.create(exceptionInfo);
		Octets o = new Octets(buffer, pos, size);
		pos = pos_new;
		return o;
	}

	public OctetsStream unmarshal(Bean<?> m) throws MarshalException
	{
		return m.unmarshal(this);
	}

	public OctetsStream unmarshalProtocol(Bean<?> m) throws MarshalException
	{
		return m.unmarshalProtocol(this);
	}

	public OctetsStream unmarshalBean(Bean<?> m, int type) throws MarshalException
	{
		if(type == 2) return m.unmarshal(this);
		unmarshalSkipVar(type);
		return this;
	}

	public <K extends Bean<K>> K unmarshalBean(K m) throws MarshalException
	{
		m.unmarshal(this);
		return m;
	}

	public <K extends Bean<K>> K unmarshalProtocolBean(K m) throws MarshalException
	{
		m.unmarshalProtocol(this);
		return m;
	}

	public <K extends Bean<K>> K unmarshalBeanKV(K m, int type) throws MarshalException
	{
		if(type == 2)
			m.unmarshal(this);
		else
		{
			unmarshalSkipKV(type);
			m.reset();
		}
		return m;
	}

	public byte[] unmarshalBytes(int type) throws MarshalException
	{
		if(type == 1) return unmarshalBytes();
		unmarshalSkipVar(type);
		return EMPTY;
	}

	public byte[] unmarshalBytesKV(int type) throws MarshalException
	{
		if(type == 1) return unmarshalBytes();
		unmarshalSkipKV(type);
		return EMPTY;
	}

	public String unmarshalString() throws MarshalException
	{
		int size = unmarshalUInt();
		if(size <= 0) return "";
		int pos_new = pos + size;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		if(pos_new < pos) throw MarshalException.create(exceptionInfo);
		int cur = pos;
		pos = pos_new;
		return new String(buffer, cur, size, Const.stringCharset);
	}

	public String unmarshalString(Charset charset) throws MarshalException
	{
		int size = unmarshalUInt();
		if(size <= 0) return "";
		int pos_new = pos + size;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		if(pos_new < pos) throw MarshalException.create(exceptionInfo);
		int cur = pos;
		pos = pos_new;
		return new String(buffer, cur, size, charset);
	}

	public String unmarshalString(String charset) throws MarshalException
	{
		int size = unmarshalUInt();
		if(size <= 0) return "";
		int pos_new = pos + size;
		if(pos_new > count) throw MarshalException.createEOF(exceptionInfo);
		if(pos_new < pos) throw MarshalException.create(exceptionInfo);
		int cur = pos;
		pos = pos_new;
		try
		{
			return new String(buffer, cur, size, charset);
		}
		catch(UnsupportedEncodingException e)
		{
			throw MarshalException.create(e, exceptionInfo);
		}
	}

	public String unmarshalString(int type) throws MarshalException
	{
		if(type == 1) return unmarshalString(Const.stringCharset);
		unmarshalSkipVar(type);
		return "";
	}

	public String unmarshalString(Charset charset, int type) throws MarshalException
	{
		if(type == 1) return unmarshalString(charset);
		unmarshalSkipVar(type);
		return "";
	}

	public String unmarshalString(String charset, int type) throws MarshalException
	{
		if(type == 1) return unmarshalString(charset);
		unmarshalSkipVar(type);
		return "";
	}

	public String unmarshalStringKV(int type) throws MarshalException
	{
		if(type == 1) return unmarshalString(Const.stringCharset);
		unmarshalSkipKV(type);
		return "";
	}

	public String unmarshalStringKV(Charset charset, int type) throws MarshalException
	{
		if(type == 1) return unmarshalString(charset);
		unmarshalSkipKV(type);
		return "";
	}

	public String unmarshalStringKV(String charset, int type) throws MarshalException
	{
		if(type == 1) return unmarshalString(charset);
		unmarshalSkipKV(type);
		return "";
	}
}