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
package org.vanilladb.bench.server.procedure.as2;

import org.vanilladb.bench.benchmarks.as2.As2BenchConstants;
import org.vanilladb.bench.server.param.as2.UpdatePriceProcParamHelper;
import org.vanilladb.bench.server.procedure.StoredProcedureHelper;
import org.vanilladb.core.query.algebra.Scan;
import org.vanilladb.core.sql.storedprocedure.StoredProcedure;
import org.vanilladb.core.storage.tx.Transaction;

public class UpdatePriceTxnProc extends StoredProcedure<UpdatePriceProcParamHelper> {

	public UpdatePriceTxnProc() {
		super(new UpdatePriceProcParamHelper());
	}

	@Override
	protected void executeSql() {
		UpdatePriceProcParamHelper paramHelper = getParamHelper();
		Transaction tx = getTransaction();
		
		// SELECT
		for (int idx = 0; idx < paramHelper.getReadCount(); idx++) {
			int iid = paramHelper.getReadItemId(idx);
			Scan s = StoredProcedureHelper.executeQuery(
				"SELECT i_name, i_price FROM item WHERE i_id = " + iid,
				tx
			);
			s.beforeFirst();
			if (s.next()) {
				String sql;
				String name = (String) s.getVal("i_name").asJavaVal();
				double price = (Double) s.getVal("i_price").asJavaVal();
				
				if(price > As2BenchConstants.MAX_PRICE) {
					sql = "UPDATE item SET i_price=" + As2BenchConstants.MIN_PRICE + "WHERE i_id=" + iid;
					StoredProcedureHelper.executeUpdate(sql, tx);
				}else {
					double new_price = price + paramHelper.get_price_raise(idx);
					sql = "UPDATE item SET i_price=" + new_price + "WHERE i_id=" + iid;
					StoredProcedureHelper.executeUpdate(sql, tx);
				}
				

				paramHelper.setItemName(name, idx);
				paramHelper.setItemPrice(price, idx);
			} else
				throw new RuntimeException("Cloud not find item record with i_id = " + iid);

			s.close();
		}
	}
}
