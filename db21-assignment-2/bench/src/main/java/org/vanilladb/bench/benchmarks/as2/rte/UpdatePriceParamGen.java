package org.vanilladb.bench.benchmarks.as2.rte;

import java.util.ArrayList;

import org.vanilladb.bench.benchmarks.as2.As2BenchConstants;
import org.vanilladb.bench.benchmarks.as2.As2BenchTransactionType;
import org.vanilladb.bench.rte.TxParamGenerator;
import org.vanilladb.bench.util.BenchProperties;
import org.vanilladb.bench.util.RandomValueGenerator;

public class UpdatePriceParamGen implements TxParamGenerator<As2BenchTransactionType>{
	
	// Read Counts
	private static final int TOTAL_READ_COUNT;
	
	static {
		TOTAL_READ_COUNT = BenchProperties.getLoader()
				.getPropertyAsInteger(UpdatePriceParamGen.class.getName() + ".TOTAL_READ_COUNT", 10);
	}

	@Override
	public As2BenchTransactionType getTxnType() {
		return As2BenchTransactionType.UpdateItemPrice;
	}

	@Override
	public Object[] generateParameter() {
		RandomValueGenerator rvg = new RandomValueGenerator();
		ArrayList<Object> paramList = new ArrayList<Object>();
		
		// Set read count
		paramList.add(TOTAL_READ_COUNT);
		for (int i = 0; i < TOTAL_READ_COUNT; i++)
			paramList.add(rvg.number(1, As2BenchConstants.NUM_ITEMS));
		
		//random value for price raise
		for (int i = 0; i < TOTAL_READ_COUNT; i++)
			paramList.add(rvg.fixedDecimalNumber(2, 0.0f, 0.5f));

		return paramList.toArray(new Object[0]);
	}

}
