// This file is generated by genbeans tool. Do NOT edit it! @formatter:off
package jane.bean;

import jane.core.Bean;
import jane.core.MarshalException;
import jane.core.OctetsStream;
import jane.core.SContext;

/**
 * 测试空bean
 */
public final class TestEmpty extends Bean<TestEmpty>
{
	private static final long serialVersionUID = 0xbeac245da40b43f8L;
	public  static final int BEAN_TYPE = 4;
	public  static final TestEmpty BEAN_STUB = new TestEmpty();

	@Override
	public void reset()
	{
	}

	/** @param b unused */
	@Override
	public void assign(TestEmpty b)
	{
	}

	@Override
	public int type()
	{
		return 4;
	}

	@Override
	public TestEmpty stub()
	{
		return BEAN_STUB;
	}

	@Override
	public TestEmpty create()
	{
		return new TestEmpty();
	}

	@Override
	public int initSize()
	{
		return 0;
	}

	@Override
	public int maxSize()
	{
		return 0;
	}

	@Override
	public OctetsStream marshal(OctetsStream s)
	{
		return s.marshal1((byte)0);
	}

	@Override
	public OctetsStream unmarshal(OctetsStream s) throws MarshalException
	{
		for(;;) { int i = s.unmarshalInt1() & 0xff, t = i & 3; switch(i >> 2)
		{
			case 0: return s;
			default: s.unmarshalSkipVar(t);
		}}
	}

	@Override
	public TestEmpty clone()
	{
		return new TestEmpty();
	}

	@Override
	public int hashCode()
	{
		return 4 * 0x9e3779b1;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == this) return true;
		if(!(o instanceof TestEmpty)) return false;
		return true;
	}

	@Override
	public int compareTo(TestEmpty b)
	{
		if(b == this) return 0;
		if(b == null) return 1;
		return 0;
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder(16 + 0 * 2).append('{');
		return s.append('}').toString();
	}

	@Override
	public StringBuilder toJson(StringBuilder s)
	{
		if(s == null) s = new StringBuilder(1024);
		s.append('{');
		return s.append('}');
	}

	@Override
	public StringBuilder toLua(StringBuilder s)
	{
		if(s == null) s = new StringBuilder(1024);
		s.append('{');
		return s.append('}');
	}

	@Override
	public Safe safe(SContext.Safe<?> parent)
	{
		return new Safe(this, parent);
	}

	@Override
	public Safe safe()
	{
		return new Safe(this, null);
	}

	public static final class Safe extends SContext.Safe<TestEmpty>
	{
		private Safe(TestEmpty bean, SContext.Safe<?> parent)
		{
			super(bean, parent);
		}
	}
}
