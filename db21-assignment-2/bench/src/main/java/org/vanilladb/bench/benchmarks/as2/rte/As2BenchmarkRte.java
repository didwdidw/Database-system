/*******************************************************************************
 * Copyright 2016, 2018 vanilladb.org contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.vanilladb.bench.benchmarks.as2.rte;

import org.vanilladb.bench.StatisticMgr;
import org.vanilladb.bench.benchmarks.as2.As2BenchTransactionType;
import org.vanilladb.bench.remote.SutConnection;
import org.vanilladb.bench.rte.RemoteTerminalEmulator;
import org.vanilladb.bench.util.BenchProperties;

public class As2BenchmarkRte extends RemoteTerminalEmulator<As2BenchTransactionType> {
	
	// Read Counts
		private static final double READ_WRITE_TX_RATE;
		
		static {
			READ_WRITE_TX_RATE = BenchProperties.getLoader()
					.getPropertyAsDouble(As2BenchmarkRte.class.getName() + ".READ_WRITE_TX_RATE", 0.5);
		}
	
	private As2BenchmarkTxExecutor executor;
	private As2BenchmarkTxExecutor executor_update;

	public As2BenchmarkRte(SutConnection conn, StatisticMgr statMgr) {
		super(conn, statMgr);
		executor = new As2BenchmarkTxExecutor(new As2ReadItemParamGen());
		executor_update= new As2BenchmarkTxExecutor(new UpdatePriceParamGen());
	}
	
	protected As2BenchTransactionType getNextTxType() {
		if(Math.random()<=READ_WRITE_TX_RATE)
		{
			//first_write=false;
			return As2BenchTransactionType.UpdateItemPrice;
		}
		else return As2BenchTransactionType.READ_ITEM;
	}
	
	protected As2BenchmarkTxExecutor getTxExeutor(As2BenchTransactionType type) {
		if(type==As2BenchTransactionType.UpdateItemPrice)return executor_update;
		else return executor;
	}
}
