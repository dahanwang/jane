// This file is generated by genbeans tool. Do NOT edit it! @formatter:off
package jane.bean;

import jane.core.RpcBean;

/**
 * RPC的注释
 */
public final class TestRpcBean extends RpcBean<TestBean, TestType, TestRpcBean>
{
	private static final long serialVersionUID = 0xbeac34fc308a6e36L;
	public  static final int BEAN_TYPE = 4;
	public  static final String BEAN_TYPENAME = "TestRpcBean";
	public  static final TestRpcBean BEAN_STUB = new TestRpcBean();
	public TestRpcBean() {}
	public TestRpcBean(TestBean a) { _arg = a; }
	@Override public int type() { return BEAN_TYPE; }
	@Override public String typeName() { return BEAN_TYPENAME; }
	@Override public TestRpcBean stub() { return BEAN_STUB; }
	@Override public TestRpcBean create() { return new TestRpcBean(); }
	@Override public int initSize() { return 0; }
	@Override public TestBean createArg() { return new TestBean(); }
	@Override public TestType createRes() { return new TestType(); }
}
