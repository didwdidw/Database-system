package org.vanilladb.core.query.algebra;


import org.vanilladb.core.sql.Constant;
import org.vanilladb.core.sql.Schema;
import org.vanilladb.core.sql.Type;

public class ExplainScan implements Scan {
	private Scan s;
	private boolean first;
	Schema schema;
	String explainStr;
	
	public ExplainScan(Scan s, String explainStr, Schema schema) {
		this.s = s;
		this.first = true;
		this.schema = schema;
		this.explainStr = explainStr;
	}

	@Override
	public Constant getVal(String fldName) {
		return Constant.newInstance(Type.VARCHAR(500), explainStr.getBytes());
	}

	@Override
	public void beforeFirst() {
		s.beforeFirst();
	}

	@Override
	public boolean next() {
		if(first) {
			first = false;
			return true;
		}
		
		return false;
	}

	@Override
	public void close() {
		s.close();
	}

	@Override
	public boolean hasField(String fldName) {
		if(schema.hasField(fldName))
			return true;
		return false;
	}

}
